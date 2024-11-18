package com.breadwallet.presenter.fragments

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.breadwallet.R
import com.breadwallet.presenter.customviews.BRKeyboard
import com.breadwallet.presenter.customviews.BRLinearLayoutWithCaret
import com.breadwallet.presenter.entities.PartnerNames
import com.breadwallet.presenter.entities.TransactionItem
import com.breadwallet.tools.animation.BRAnimator
import com.breadwallet.tools.animation.BRDialog
import com.breadwallet.tools.animation.SlideDetector
import com.breadwallet.tools.animation.SpringAnimator
import com.breadwallet.tools.manager.*
import com.breadwallet.tools.security.BRSender
import com.breadwallet.tools.security.BitcoinUrlHandler
import com.breadwallet.tools.threads.BRExecutor
import com.breadwallet.tools.util.*
import com.breadwallet.wallet.BRWalletManager
import com.google.common.math.Quantiles.scale
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern

class FragmentSend : Fragment() {
    private lateinit var signalLayout: LinearLayout; private lateinit var keyboardLayout: LinearLayout
    private lateinit var scanButton: Button; private lateinit var pasteButton: Button; private lateinit var sendButton: Button; private lateinit var isoCurrencySymbolButton: Button
    private lateinit var commentEdit: EditText; private lateinit var addressEdit: EditText;private lateinit var amountEdit: EditText
    private lateinit var isoCurrencySymbolText: TextView; private lateinit var balanceText: TextView; private lateinit var feeText: TextView; private lateinit var feeDescription: TextView; private lateinit var warningText: TextView
    private var amountLabelOn = true; private var ignoreCleanup = false; private var feeButtonsShown = false
    private lateinit var edit: ImageView
    private var currentBalance: Long = 0
    private var keyboardIndex = 0
    private lateinit var keyboard: BRKeyboard
    private lateinit var closeButton: ImageButton
    private lateinit var amountLayout: ConstraintLayout
    private lateinit var feeLayout: BRLinearLayoutWithCaret
    private var selectedIsoCurrencySymbol: String? = null
    private lateinit var backgroundLayout: ScrollView
    private lateinit var amountBuilder: StringBuilder
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_send, container, false)
        backgroundLayout = rootView.findViewById(R.id.background_layout)
        signalLayout = rootView.findViewById<View>(R.id.signal_layout) as LinearLayout
        keyboard = rootView.findViewById<View>(R.id.keyboard) as BRKeyboard
        keyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button)
        keyboard.setBRKeyboardColor(R.color.white)
        isoCurrencySymbolText = rootView.findViewById<View>(R.id.iso_text) as TextView
        addressEdit = rootView.findViewById<View>(R.id.address_edit) as EditText
        scanButton = rootView.findViewById<View>(R.id.scan) as Button
        pasteButton = rootView.findViewById<View>(R.id.paste_button) as Button

        sendButton = rootView.findViewById<View>(R.id.send_button) as Button
        commentEdit = rootView.findViewById<View>(R.id.comment_edit) as EditText
        amountEdit = rootView.findViewById<View>(R.id.amount_edit) as EditText
        balanceText = rootView.findViewById<View>(R.id.balance_text) as TextView
        feeText = rootView.findViewById<View>(R.id.fee_text) as TextView
        edit = rootView.findViewById<View>(R.id.edit) as ImageView
        isoCurrencySymbolButton = rootView.findViewById<View>(R.id.iso_button) as Button
        keyboardLayout = rootView.findViewById<View>(R.id.keyboard_layout) as LinearLayout
        amountLayout = rootView.findViewById<View>(R.id.amount_layout) as ConstraintLayout
        feeLayout = rootView.findViewById<View>(R.id.fee_buttons_layout) as BRLinearLayoutWithCaret
        feeDescription = rootView.findViewById<View>(R.id.fee_description) as TextView
        warningText = rootView.findViewById<View>(R.id.warning_text) as TextView
        closeButton = rootView.findViewById<View>(R.id.close_button) as ImageButton
        selectedIsoCurrencySymbol =
            if (BRSharedPrefs.getPreferredLTC(context)) "LTC" else BRSharedPrefs.getIsoSymbol(context)
        amountBuilder = StringBuilder(0)
        setListeners()

        /// Setup Currency Button that switches between LTC and the preferred fiat (e.g.; "USD")
        isoCurrencySymbolText.text = getString(R.string.Send_amountLabel)
        isoCurrencySymbolText.textSize = 18f
        isoCurrencySymbolText.setTextColor(requireContext().getColor(R.color.light_gray))
        isoCurrencySymbolText.requestLayout()

        /// Setup Fees Description
        feeText.text = ""

        signalLayout.setOnTouchListener(SlideDetector(signalLayout) { animateClose() })
        AnalyticsManager.logCustomEvent(BRConstants._20191105_VSC)
        setupFeesSelector(rootView)
        showFeeSelectionButtons(feeButtonsShown)
        edit.setOnClickListener {
            feeButtonsShown = !feeButtonsShown
            showFeeSelectionButtons(feeButtonsShown)
        }
        keyboardIndex = signalLayout.indexOfChild(keyboardLayout)
        // TODO: all views are using the layout of this button. Views should be refactored without it
        // Hiding until layouts are built.
        showKeyboard(false)
        signalLayout.layoutTransition = BRAnimator.getDefaultTransition()
        
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun setupFeesSelector(rootView: View) {
        val feesSegment = rootView.findViewById<RadioGroup>(R.id.fees_segment)
        feesSegment.setOnCheckedChangeListener { _, checkedTypeId -> onFeeTypeSelected(checkedTypeId) }
        onFeeTypeSelected(R.id.regular_fee_but)
    }

    private fun onFeeTypeSelected(checkedTypeId: Int) {
        val feeManager = FeeManager.getInstance()
        when (checkedTypeId) {
            R.id.regular_fee_but -> {
                feeManager.setFeeType(FeeManager.REGULAR)
                BRWalletManager.getInstance().setFeePerKb(feeManager.currentFees.regular)
                setFeeInformation(R.string.FeeSelector_regularTime, 0, 0, View.GONE)
            }
            R.id.economy_fee_but -> {
                feeManager.setFeeType(FeeManager.ECONOMY)
                BRWalletManager.getInstance().setFeePerKb(feeManager.currentFees.economy)
                setFeeInformation(
                    R.string.FeeSelector_economyTime,
                    R.string.FeeSelector_economyWarning,
                    R.color.red_text,
                    View.VISIBLE,
                )
            }
            R.id.luxury_fee_but -> {
                feeManager.setFeeType(FeeManager.LUXURY)
                BRWalletManager.getInstance().setFeePerKb(feeManager.currentFees.luxury)
                setFeeInformation(
                    R.string.FeeSelector_luxuryTime,
                    R.string.FeeSelector_luxuryMessage,
                    R.color.light_gray,
                    View.VISIBLE,
                )
            }
            else -> {
            }
        }
        updateText()
    }

    private fun setFeeInformation(
        @StringRes deliveryTime: Int,
        @StringRes warningStringId: Int,
        @ColorRes warningColorId: Int,
        visibility: Int,
    ) {
        feeDescription.text =
            getString(R.string.FeeSelector_estimatedDeliver, getString(deliveryTime))
        if (warningStringId != 0) {
            warningText.setText(warningStringId)
        }
        if (warningColorId != 0) {
            warningText.setTextColor(resources.getColor(warningColorId, null))
        }
        warningText.visibility = visibility
    }

    private fun setListeners() {
        amountEdit.setOnClickListener {
            showKeyboard(true)
            if (amountLabelOn) { // only first time
                amountLabelOn = false
                amountEdit.hint = "0"
                amountEdit.textSize = 24f
                balanceText.visibility = View.VISIBLE
                feeText.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                isoCurrencySymbolText.setTextColor(requireContext().getColor(R.color.almost_black))
                isoCurrencySymbolText.text = BRCurrency.getSymbolByIso(activity, selectedIsoCurrencySymbol)
                isoCurrencySymbolText.textSize = 28f
                val scaleX = amountEdit.scaleX
                amountEdit.scaleX = 0f
                val tr = AutoTransition()
                tr.interpolator = OvershootInterpolator()
                tr.addListener(
                    object : Transition.TransitionListener {
                        override fun onTransitionStart(transition: Transition) {}

                        override fun onTransitionEnd(transition: Transition) {
                            amountEdit.requestLayout()
                            amountEdit.animate().setDuration(100).scaleX(scaleX)
                        }

                        override fun onTransitionCancel(transition: Transition) {}

                        override fun onTransitionPause(transition: Transition) {}

                        override fun onTransitionResume(transition: Transition) {}
                    },
                )
                val set = ConstraintSet()
                set.clone(amountLayout)
                TransitionManager.beginDelayedTransition(amountLayout, tr)
                val px4 = Utils.getPixelsFromDps(context, 4)
                set.connect(
                    balanceText.id,
                    ConstraintSet.TOP,
                    isoCurrencySymbolText.id,
                    ConstraintSet.BOTTOM,
                    px4,
                )
                set.connect(
                    feeText.id,
                    ConstraintSet.TOP,
                    balanceText.id,
                    ConstraintSet.BOTTOM,
                    px4,
                )
                set.connect(
                    feeText.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    px4,
                )
                set.connect(
                    isoCurrencySymbolText.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    px4,
                )
                set.connect(isoCurrencySymbolText.id, ConstraintSet.BOTTOM, -1, ConstraintSet.TOP, -1)
                set.applyTo(amountLayout)
            }
        }

        // needed to fix the overlap bug
        commentEdit.setOnKeyListener(

            View.OnKeyListener { v, keyCode, event ->

                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    amountLayout.requestLayout()
                    return@OnKeyListener true
                }
                false
            },
        )
        pasteButton.setOnClickListener(
            View.OnClickListener {
                if (!BRAnimator.isClickAllowed()) return@OnClickListener
                val bitcoinUrl = BRClipboardManager.getClipboard(activity)
                if (Utils.isNullOrEmpty(bitcoinUrl) || !isInputValid(bitcoinUrl)) {
                    showClipboardError()
                    return@OnClickListener
                }
                val obj = BitcoinUrlHandler.getRequestFromString(bitcoinUrl)
                if (obj?.address == null) {
                    showClipboardError()
                    return@OnClickListener
                }
                val address = obj.address
                val wm = BRWalletManager.getInstance()
                if (BRWalletManager.validateAddress(address)) {
                    val app: Activity? = activity
                    if (app == null) {
                        Timber.e("timber:paste onClick: app is null")
                        return@OnClickListener
                    }
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
                        if (wm.addressContainedInWallet(address)) {
                            app.runOnUiThread(
                                Runnable {
                                    BRDialog.showCustomDialog(
                                        requireActivity(),
                                        "",
                                        resources.getString(R.string.Send_containsAddress),
                                        resources.getString(R.string.AccessibilityLabels_close),
                                        null,
                                        { brDialogView -> brDialogView.dismiss() },
                                        null,
                                        null,
                                        0,
                                    )
                                    BRClipboardManager.putClipboard(activity, "")
                                },
                            )
                        } else if (wm.addressIsUsed(address)) {
                            app.runOnUiThread(
                                Runnable {
                                    BRDialog.showCustomDialog(
                                        requireActivity(),
                                        getString(R.string.Send_UsedAddress_firstLine),
                                        getString(R.string.Send_UsedAddress_secondLIne),
                                        "Ignore",
                                        "Cancel",
                                        { brDialogView ->
                                            brDialogView.dismiss()
                                            addressEdit.setText(address)
                                        },
                                        { brDialogView -> brDialogView.dismiss() },
                                        null,
                                        0,
                                    )
                                },
                            )
                        } else {
                            app.runOnUiThread(Runnable { addressEdit.setText(address) })
                        }
                    }
                } else {
                    showClipboardError()
                }
            },
        )
        isoCurrencySymbolButton.setOnClickListener {
            selectedIsoCurrencySymbol =
                if (selectedIsoCurrencySymbol.equals(BRSharedPrefs.getIsoSymbol(context), ignoreCase = true)) {
                    "LTC"
                } else {
                    BRSharedPrefs.getIsoSymbol(context)
                }
            updateText()
        }
        scanButton.setOnClickListener(
            View.OnClickListener {
                if (!BRAnimator.isClickAllowed()) return@OnClickListener
                saveMetaData()
                BRAnimator.openScanner(activity, BRConstants.SCANNER_REQUEST)
            },
        )

        sendButton.setOnClickListener(
            View.OnClickListener {
                if (!BRAnimator.isClickAllowed()) {
                    return@OnClickListener
                }
                var allFilled = true
                val sendAddress = addressEdit.text.toString()
                val amountStr = amountBuilder.toString()
                val iso = selectedIsoCurrencySymbol
                val comment = commentEdit.text.toString()

                // get amount in satoshis from any isos
                val bigAmount = BigDecimal(if (Utils.isNullOrEmpty(amountStr)) "0" else amountStr)
                val litoshiAmount = BRExchange.getLitoshisFromAmount(activity, iso, bigAmount)
                if (sendAddress.isEmpty() || !BRWalletManager.validateAddress(sendAddress)) {
                    allFilled = false
                    SpringAnimator.failShakeAnimation(activity, addressEdit)
                }
                if (amountStr.isEmpty()) {
                    allFilled = false
                    SpringAnimator.failShakeAnimation(activity, amountEdit)
                }
                if (litoshiAmount.toLong() > BRWalletManager.getInstance().getBalance(activity)) {
                    SpringAnimator.failShakeAnimation(activity, balanceText)
                    SpringAnimator.failShakeAnimation(activity, feeText)
                }
                if (allFilled) {
                    BRSender.getInstance().sendTransaction(
                        context,
                        TransactionItem(sendAddress,
                            Utils.fetchPartnerKey(context, PartnerNames.LITEWALLETOPS),
                            null,
                            litoshiAmount.toLong(),
                            Utils.tieredOpsFee(context, litoshiAmount.toLong()),
                            null,
                            false,
                            comment
                        ),
                    )
                    AnalyticsManager.logCustomEvent(BRConstants._20191105_DSL)
                    BRSharedPrefs.incrementSendTransactionCount(context)
                }
            },
        )
        backgroundLayout.setOnClickListener(
            View.OnClickListener {
                if (!BRAnimator.isClickAllowed()) return@OnClickListener
                animateClose()
            },
        )
        closeButton.setOnClickListener {
            animateClose()
        }
        addressEdit.setOnEditorActionListener { _, actionId, event ->
            showKeyboard(false)
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                Utils.hideKeyboard(activity)
                Handler().postDelayed({ showKeyboard(true) }, 500)
            }
            false
        }
        keyboard.addOnInsertListener { key -> handleClick(key) }
    }

    private fun showKeyboard(b: Boolean) {
        val curIndex = keyboardIndex
        if (!b) {
            signalLayout.removeView(keyboardLayout)
        } else {
            Utils.hideKeyboard(activity)
            if (signalLayout.indexOfChild(keyboardLayout) == -1) {
                signalLayout.addView(
                    keyboardLayout,
                    curIndex,
                )
            } else {
                signalLayout.removeView(keyboardLayout)
            }
        }
    }

    private fun showClipboardError() {
        BRDialog.showCustomDialog(
            requireActivity(),
            getString(R.string.Send_emptyPasteboard),
            resources.getString(R.string.Send_invalidAddressTitle),
            getString(R.string.AccessibilityLabels_close),
            null,
            { brDialogView -> brDialogView.dismiss() },
            null,
            null,
            0,
        )
        BRClipboardManager.putClipboard(activity, "")
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val observer = signalLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (observer.isAlive) {
                        observer.removeOnGlobalLayoutListener(this)
                    }
                    BRAnimator.animateBackgroundDim(backgroundLayout, false)
                    BRAnimator.animateSignalSlide(signalLayout, false) {
                        val bundle = arguments
                        if (bundle?.getString("url") != null) setUrl(bundle.getString("url"))
                    }
                }
            },
        )
    }

    override fun onStop() {
        super.onStop()
        FeeManager.getInstance().resetFeeType()
    }

    override fun onResume() {
        super.onResume()
        loadMetaData()
    }

    override fun onPause() {
        super.onPause()
        Utils.hideKeyboard(activity)
        if (!ignoreCleanup) {
            savedIsoCurrencySymbol = null
            savedAmount = null
            savedMemo = null
        }
    }

    private fun handleClick(key: String?) {
        if (key == null) {
            Timber.d("timber: handleClick: key is null! ")
            return
        }
        when {
            key.isEmpty() -> {
                handleDeleteClick()
            }
            Character.isDigit(key[0]) -> {
                handleDigitClick(key.substring(0, 1).toInt())
            }
            key[0] == '.' -> {
                handleSeparatorClick()
            }
        }
    }

    private fun handleDigitClick(dig: Int) {
        val currentAmount = amountBuilder.toString()
        val iso = selectedIsoCurrencySymbol
        if (BigDecimal(currentAmount + dig.toString()).toDouble()
            <= BRExchange.getMaxAmount(activity, iso).toDouble()
        ) {
            // do not insert 0 if the balance is 0 now
            if (currentAmount.equals("0", ignoreCase = true)) amountBuilder = StringBuilder("")
            if (currentAmount.contains(".") && currentAmount.length - currentAmount.indexOf(".") >
                BRCurrency.getMaxDecimalPlaces(
                    iso,
                )
            ) {
                return
            }
            amountBuilder.append(dig)
            updateText()
        }
    }

    private fun handleSeparatorClick() {
        val currentAmount = amountBuilder.toString()
        if (currentAmount.contains(".") || BRCurrency.getMaxDecimalPlaces(selectedIsoCurrencySymbol) == 0) return
        amountBuilder.append(".")
        updateText()
    }

    private fun handleDeleteClick() {
        val currentAmount = amountBuilder.toString()
        if (currentAmount.isNotEmpty()) {
            amountBuilder.deleteCharAt(currentAmount.length - 1)
            updateText()
        }
    }

    private fun updateText() {
        if (activity == null) return
        var tempDoubleAmountValue = 0.0
        if (amountBuilder.toString() != "" && amountBuilder.toString() != "." ) {
            tempDoubleAmountValue = amountBuilder.toString().toDouble()
        }
        val scaleValue = 4
        setAmount()

        // Fetch/Set Current ISOSymbol
        val selectedISOSymbol = selectedIsoCurrencySymbol
        val currencySymbol = BRCurrency.getSymbolByIso(activity, selectedIsoCurrencySymbol)
        if (!amountLabelOn) isoCurrencySymbolText.text = currencySymbol
        isoCurrencySymbolButton.text = String.format("%s(%s)",
                                        BRCurrency.getCurrencyName(activity, selectedIsoCurrencySymbol),
                                        currencySymbol)

        // Balance depending on ISOSymbol
        currentBalance = BRWalletManager.getInstance().getBalance(activity)
        val balanceForISOSymbol = BRExchange.getAmountFromLitoshis(activity, selectedISOSymbol, BigDecimal(currentBalance))
        val formattedBalance = BRCurrency.getFormattedCurrencyString(activity, selectedISOSymbol, balanceForISOSymbol)

        // Current amount depending on ISOSymbol
        val currentAmountInLitoshis =
            if (selectedIsoCurrencySymbol.equals("LTC", ignoreCase = true)) {
                BRExchange.convertltcsToLitoshis(tempDoubleAmountValue).toLong()
            } else {
                BRExchange.getLitoshisFromAmount(activity,selectedIsoCurrencySymbol,BigDecimal(tempDoubleAmountValue)).toLong()
            }
        Timber.d("timber: updateText: currentAmountInLitoshis %d",currentAmountInLitoshis)

        // Network Fee depending on ISOSymbol
        var networkFee = if(currentAmountInLitoshis > 0) { BRWalletManager.getInstance().feeForTransactionAmount(currentAmountInLitoshis) }
        else { 0 } //Amount is zero so network fee is also zero
        val networkFeeForISOSymbol =
          BRExchange.getAmountFromLitoshis(activity,selectedISOSymbol, BigDecimal(networkFee)).setScale(scaleValue, RoundingMode.HALF_UP)
        val formattedNetworkFee = BRCurrency.getFormattedCurrencyString(activity, selectedISOSymbol, networkFeeForISOSymbol)

        // Service Fee depending on ISOSymbol
        var serviceFee = Utils.tieredOpsFee(activity, currentAmountInLitoshis)
        val serviceFeeForISOSymbol =
          BRExchange.getAmountFromLitoshis(activity,selectedISOSymbol,BigDecimal(serviceFee)).setScale(scaleValue, RoundingMode.HALF_UP)
        val formattedServiceFee = BRCurrency.getFormattedCurrencyString(activity, selectedISOSymbol, serviceFeeForISOSymbol)

        // Total Fees depending on ISOSymbol
        val totalFees = networkFee + serviceFee
        val totalFeeForISOSymbol =
            BRExchange.getAmountFromLitoshis( activity,selectedISOSymbol,BigDecimal(totalFees)).setScale(scaleValue, RoundingMode.HALF_UP)
        val formattedTotalFees = BRCurrency.getFormattedCurrencyString(activity, selectedISOSymbol, totalFeeForISOSymbol)

        // Update UI with alert red when over balance
        if (BigDecimal(currentAmountInLitoshis).toDouble() > currentBalance.toDouble()) {
            balanceText.setTextColor(requireContext().getColor(R.color.warning_color))
            feeText.setTextColor(requireContext().getColor(R.color.warning_color))
            amountEdit.setTextColor(requireContext().getColor(R.color.warning_color))
            if (!amountLabelOn) isoCurrencySymbolText.setTextColor(requireContext().getColor(R.color.warning_color))
        }
        else {
            balanceText.setTextColor(requireContext().getColor(R.color.light_gray))
            feeText.setTextColor(requireContext().getColor(R.color.light_gray))
            amountEdit.setTextColor(requireContext().getColor(R.color.almost_black))
            if (!amountLabelOn) isoCurrencySymbolText.setTextColor(requireContext().getColor(R.color.almost_black))
        }

        balanceText.text = getString(R.string.Send_balance, formattedBalance)
        feeText.text = String.format("(%s + %s): %s + %s = %s",
            getString(R.string.Network_feeLabel),
            getString(R.string.Fees_Service),
            formattedNetworkFee,
            formattedServiceFee,
            formattedTotalFees)
        amountLayout.requestLayout()
    }

    fun setUrl(url: String?) {
        val obj = BitcoinUrlHandler.getRequestFromString(url) ?: return
        if (obj.address != null) {
            addressEdit.setText(obj.address.trim { it <= ' ' })
        }
        if (obj.message != null) {
            commentEdit.setText(obj.message)
        }
        if (obj.amount != null) {
            val iso = selectedIsoCurrencySymbol
            val satoshiAmount = BigDecimal(obj.amount).multiply(BigDecimal(100000000))
            amountBuilder =
                StringBuilder(
                    BRExchange.getAmountFromLitoshis(activity, iso, satoshiAmount).toPlainString(),
                )
            updateText()
        }
    }

    private fun showFeeSelectionButtons(b: Boolean) {
        if (!b) {
            signalLayout.removeView(feeLayout)
        } else {
            signalLayout.addView(feeLayout, signalLayout.indexOfChild(amountLayout) + 1)
        }
    }

    private fun setAmount() {
        val tmpAmount = amountBuilder.toString()
        var divider = tmpAmount.length
        if (tmpAmount.contains(".")) {
            divider = tmpAmount.indexOf(".")
        }
        val newAmount = StringBuilder()
        for (i in tmpAmount.indices) {
            newAmount.append(tmpAmount[i])
            if (divider > 3 && divider - 1 != i && divider > i && (divider - i - 1) % 3 == 0) {
                newAmount.append(",")
            }
        }
        
        amountEdit.setText(newAmount.toString())
    }

    private fun isInputValid(input: String): Boolean {
        return Pattern.matches("[a-zA-Z0-9]*", input)
    }

    // from the link above
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Timber.d("timber: onConfigurationChanged: hidden")
            showKeyboard(true)
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Timber.d("timber: onConfigurationChanged: shown")
            showKeyboard(false)
        }
    }

    private fun saveMetaData() {
        if (commentEdit.text.toString().isNotEmpty()) savedMemo = commentEdit.text.toString()
        if (amountBuilder.toString().isNotEmpty()) savedAmount = amountBuilder.toString()
        savedIsoCurrencySymbol = selectedIsoCurrencySymbol
        ignoreCleanup = true
    }

    private fun loadMetaData() {
        ignoreCleanup = false
        if (!Utils.isNullOrEmpty(savedMemo)) commentEdit.setText(savedMemo)
        if (!Utils.isNullOrEmpty(savedIsoCurrencySymbol)) selectedIsoCurrencySymbol = savedIsoCurrencySymbol
        if (!Utils.isNullOrEmpty(savedAmount)) {
            amountBuilder = StringBuilder(savedAmount!!)
            Handler().postDelayed({
                amountEdit.performClick()
                updateText()
            }, 500)
        }
    }

    private fun animateClose() {
        BRAnimator.animateBackgroundDim(backgroundLayout, true)
        BRAnimator.animateSignalSlide(signalLayout, true) { close() }
    }

    private fun close() {
        if (activity != null && activity?.isFinishing != true) {
            activity?.onBackPressed()
        }
    }

    companion object {
        private var savedMemo: String? = null
        private var savedIsoCurrencySymbol: String? = null
        private var savedAmount: String? = null
    }
}



///DEV WIP

//      val approximateNetworkFee = BRCurrency.getFormattedCurrencyString(activity, selectedISOSymbol, feeForISOSymbol)

//
//        var currentAmountValue = 0L
//        val amountString = amountEdit.getText().toString()
//        if (amountString != "") {
//            currentAmountValue = amountString.toDouble().toLong()
//            if (selectedIsoCurrencySymbol.equals("LTC")) {
//                currentAmountValue = BRExchange.getAmountFromLitoshis(
//                    activity,
//                    selectedISOSymbol,
//                    BigDecimal(currentAmountValue)
//                ).toLong()
//            } else {
//                currentAmountValue = BRExchange.getLitoshisFromAmount(
//                    activity,
//                    selectedISOSymbol,
//                    BigDecimal(currentAmountValue)
//                ).toLong()
//            }
//
