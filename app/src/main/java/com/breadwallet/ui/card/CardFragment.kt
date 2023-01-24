package com.breadwallet.ui.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.breadwallet.R
import com.breadwallet.databinding.FragmentCardBinding

class CardFragment : Fragment() {

    lateinit var binding: FragmentCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_card, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webview.loadUrl("https://litecoin.dashboard.getblockcard.com")
        val webSettings: WebSettings = binding.webview.settings
        webSettings.javaScriptEnabled = true
        binding.webview.webViewClient = WebViewClient()
    }
}