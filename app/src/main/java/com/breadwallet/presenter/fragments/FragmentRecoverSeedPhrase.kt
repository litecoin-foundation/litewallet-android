package com.breadwallet.presenter.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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


class FragmentRecoverSeedPhrase : Fragment() {
    private lateinit var backgroundLayout: ScrollView
    private lateinit var signalLayout: LinearLayout
    private lateinit var showSeedButton: Button
    private lateinit var seedPhraseTextView: TextView
    private lateinit var closeButton: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_recover_seed_phrase, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val observer = signalLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (observer.isAlive) {
                    observer.removeOnGlobalLayoutListener(this)
                }
                BRAnimator.animateBackgroundDim(backgroundLayout, false)
                BRAnimator.animateSignalSlide(signalLayout, false) {}
            }
        })
        setListeners()
        fetchWalletInfo()
    }

    fun fetchWalletInfo() {
        val walletManager = BRWalletManager.getInstance()
        seedPhraseTextView.text = walletManager.getSeedPhrase(requireContext())
        seedPhraseTextView.setOnClickListener {
            if (it.visibility != View.VISIBLE) return@setOnClickListener
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("seed phrase", seedPhraseTextView.text)
            clipboardManager.setPrimaryClip(clipData)
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
}