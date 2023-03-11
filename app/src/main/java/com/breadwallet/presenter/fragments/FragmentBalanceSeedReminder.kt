package com.breadwallet.presenter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.fragment.app.Fragment
import com.breadwallet.R
import com.breadwallet.tools.animation.BRAnimator
import com.breadwallet.wallet.BRWalletManager
import java.util.*


class FragmentBalanceSeedReminder : Fragment() {
    private lateinit var backgroundLayout: ScrollView
    private lateinit var signalLayout: LinearLayout
    private lateinit var showSeedButton: Button
    private lateinit var currentBalanceTextView: TextView
    private lateinit var seedPhraseTextView: TextView
    private lateinit var closeButton: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_balance_seed_reminder, container, false)
        backgroundLayout = rootView.findViewById(R.id.background_layout)
        signalLayout = rootView.findViewById(R.id.signal_layout)
        signalLayout = rootView.findViewById<View>(R.id.signal_layout) as LinearLayout
        currentBalanceTextView = rootView.findViewById(R.id.current_balance)
        seedPhraseTextView = rootView.findViewById(R.id.seed_phrase)
        closeButton = rootView.findViewById(R.id.close_button)
        showSeedButton = rootView.findViewById(R.id.show_seed_button)
//        signalLayout.layoutTransition = BRAnimator.getDefaultTransition()

        return rootView
    }

    private fun setListeners() {

        showSeedButton.setOnClickListener {
            seedPhraseTextView.visibility = View.VISIBLE
        }

        closeButton.setOnClickListener {
            animateClose()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val observer = signalLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (observer.isAlive) {
                    observer.removeOnGlobalLayoutListener(this)
                }
                BRAnimator.animateBackgroundDim(backgroundLayout, false)
                BRAnimator.animateSignalSlide(signalLayout, false) {
//                    val bundle = arguments
//                    if (bundle?.getString("url") != null) setUrl(bundle.getString("url"))
                }
            }
        })
        setListeners()
        fetchWalletInfo()
    }
    fun fetchWalletInfo() {
        val walletManager = BRWalletManager.getInstance()
        val balance = walletManager.getBalance(requireContext())
        currentBalanceTextView.text = "$balance"
        seedPhraseTextView.text = walletManager.getSeedPhrase(requireContext())
    }

    private fun animateClose() {

        BRAnimator.animateBackgroundDim(backgroundLayout, true)
        BRAnimator.animateSignalSlide(signalLayout, true) {
     //       val prev: Fragment = parentFragmentManager().findFragmentByTag("fragment_dialog")
//            if (prev != null) {
//                val df: DialogFragment = prev as DialogFragment
//                df.dismiss()
//            }

//            fun onBackPressed() {
//                if (fragmentManager!!.backStackEntryCount > 0) {
//                    fragmentManager!!.popBackStack()
//                } else if (AuthManager.getInstance().isWalletDisabled(this@DisabledActivity)) {
//                    SpringAnimator.failShakeAnimation(this@DisabledActivity, disabled)
//                } else {
//                    BRAnimator.startBreadActivity(this@DisabledActivity, true)
//                }
//                overridePendingTransition(R.anim.fade_up, R.anim.fade_down)
//            }

        }
    }
}