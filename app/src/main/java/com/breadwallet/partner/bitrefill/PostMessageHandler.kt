package com.breadwallet.partner.bitrefill

import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebMessagePortCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class PostMessageHandler(webView: WebView) {

    private val nativeToJsPorts = WebViewCompat.createWebMessageChannel(webView)
    private var nativeToJs: WebMessagePortCompat.WebMessageCallbackCompat? = null

    init {

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_CALLBACK_ON_MESSAGE)) {
            nativeToJs = object : WebMessagePortCompat.WebMessageCallbackCompat() {
                override fun onMessage(port: WebMessagePortCompat, message: WebMessageCompat?) {
                    super.onMessage(port, message)
                    Toast.makeText(webView.context, message!!.data, Toast.LENGTH_SHORT).show()
                }
            }
        }

        var destPort = arrayOf(nativeToJsPorts[1])
        nativeToJsPorts[0].setWebMessageCallback(nativeToJs!!)
        WebViewCompat.postWebMessage(webView, WebMessageCompat("capturePort", destPort), Uri.EMPTY)

    }

}