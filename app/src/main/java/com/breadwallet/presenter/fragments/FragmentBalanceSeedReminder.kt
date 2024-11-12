package com.breadwallet.presenter.fragments

import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.fragment.app.Fragment
import com.breadwallet.R
import com.breadwallet.tools.animation.BRAnimator
import com.breadwallet.tools.manager.AnalyticsManager
import com.breadwallet.tools.manager.TxManager
import com.breadwallet.tools.security.BRKeyStore
import com.breadwallet.tools.util.BRConstants
import timber.log.Timber
import java.util.*

class FragmentBalanceSeedReminder : Fragment() {
    private lateinit var backgroundLayout: ScrollView
    private lateinit var signalLayout: LinearLayout
    private lateinit var showSeedButton: Button
    private lateinit var seedPhraseTextView: TextView
    private lateinit var closeButton: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_balance_seed_reminder, container, false)
        backgroundLayout = rootView.findViewById(R.id.background_layout)
        signalLayout = rootView.findViewById(R.id.signal_layout)
        signalLayout = rootView.findViewById<View>(R.id.signal_layout) as LinearLayout
        seedPhraseTextView = rootView.findViewById(R.id.seed_phrase)
        closeButton = rootView.findViewById(R.id.close_button)
        showSeedButton = rootView.findViewById(R.id.show_seed_button)
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
                    }
                }
            },
        )
        setListeners()
        fetchSeedPhrase()
    }
    private fun registerAnalyticsError(errorString: String) {
        Timber.d("Fragment Balance Seed: RegisterError : %s", errorString)
        val params = Bundle()
        params.putString("lwa_error_message", errorString);
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR, params)
    }
    fun fetchSeedPhrase() {
        seedPhraseTextView.text = "NO_PHRASE"
        if (this.activity == null) {
            registerAnalyticsError("null_in_fragment_balance_fetch_seed")
        }
        else {
            seedPhraseTextView.text = runCatching { BRKeyStore.getPhrase(this.activity, 0) }
                .getOrNull()?.decodeToString() ?: "NO_PHRASE"
        }
    }
    private fun animateClose() {
        BRAnimator.animateBackgroundDim(backgroundLayout, true)
        BRAnimator.animateSignalSlide(signalLayout, true) { close() }
    }

    private fun close() {
        if (activity != null && activity?.isFinishing != true) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
}
