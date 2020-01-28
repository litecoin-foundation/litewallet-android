package com.breadwallet.presenter.fragments;

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.breadwallet.R;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.Utils;

import java.util.Date;

/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class FragmentBuy extends Fragment {
    private static final String TAG = FragmentBuy.class.getName();
    public LinearLayout backgroundLayout;
    WebView webView;
    public static boolean appVisible = false;
    private String onCloseUrl;
    private static String URL_BUY_LTC = "https://buy.loafwallet.org";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        backgroundLayout = rootView.findViewById(R.id.background_layout);
        webView = rootView.findViewById(R.id.web_view);
        webView.setWebChromeClient(new BRWebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl());
                Log.d(TAG, "shouldOverrideUrlLoading: " + request.getMethod());
                if (onCloseUrl != null && request.getUrl().toString().equalsIgnoreCase(onCloseUrl)) {
                    getActivity().onBackPressed();
                    onCloseUrl = null;
                } else if (request.getUrl().toString().contains("close")) {
                    getActivity().onBackPressed();
                } else {
                    view.loadUrl(request.getUrl().toString());
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("TEST", url);
            }
        });

        webView.addJavascriptInterface(this, "Android");

        WebSettings webSettings = webView.getSettings();
        if (0 != (getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        String walletAddress = BRSharedPrefs.getReceiveAddress(getContext());
        String currency = "USD";
        Long timestamp = new Date().getTime();
        String uuid = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        String buyUrl = url(walletAddress, currency, timestamp, uuid);
        Log.d("BASEURL", "onCreate: theUrl: " + buyUrl);
        webView.loadUrl(buyUrl);
        return rootView;
    }

    private String url(Object... args) {
        return String.format(URL_BUY_LTC + "/?address=%s&code=%s&idate=%s&uid=%s", args);
    }


    @Override
    public void onStop() {
        super.onStop();
        BRAnimator.animateBackgroundDim(backgroundLayout, true);
    }

    private class BRWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e(TAG, "onConsoleMessage: consoleMessage: " + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.e(TAG, "onJsAlert: " + message + ", url: " + url);
            return super.onJsAlert(view, url, message, result);
        }
    }

    @JavascriptInterface
    public void handleMessage(String message) {
        Log.e(TAG, "handle message: " + message);
    }

    @JavascriptInterface
    public void postMessage(String json) {
        Log.d("TEST", json);
    }

    @JavascriptInterface
    public void onData(String value) {
        //TODO: do something with the data
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
    }
}