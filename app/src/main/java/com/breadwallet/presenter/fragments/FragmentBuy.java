package com.breadwallet.presenter.fragments;

import static com.breadwallet.tools.util.BRConstants.LW_API_HOST;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.breadwallet.BuildConfig;
import com.breadwallet.R;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;

import java.util.Date;

import timber.log.Timber;

public class FragmentBuy extends Fragment {
    private static final int FILE_CHOOSER_REQUEST_CODE = 15423;
    public LinearLayout backgroundLayout;
    private ProgressBar progress;
    private WebView webView;
    private String onCloseUrl;
    private static final String CURRENCY_KEY = "currency_code_key";
    private static final String PARTNER_KEY = "partner_key";
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    public static Fragment newInstance(String currency, Partner partner) {
        Bundle bundle = new Bundle();
        bundle.putString(CURRENCY_KEY, currency);
        bundle.putSerializable(PARTNER_KEY, partner);
        Fragment fragment = new FragmentBuy();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    closePayment();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> closePayment());
        backgroundLayout = rootView.findViewById(R.id.background_layout);
        progress = rootView.findViewById(R.id.progress);
        webView = rootView.findViewById(R.id.web_view);
        webView.setWebChromeClient(mWebChromeClient);
        webView.setWebViewClient(mWebViewClient);

        WebSettings webSettings = webView.getSettings();
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

        // App (in Java)
        WebMessageListener bitrefillListener = new WebMessageListener() {
            @Override
            public void onPostMessage(WebView view, WebMessageCompat message, Uri sourceOrigin,
                                      boolean isMainFrame, JavaScriptReplyProxy replyProxy) {
                // do something about view, message, sourceOrigin and isMainFrame.
                replyProxy.postMessage("Got it!");
            }
        };
		
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(webView, "bitrefillPostObj", rules, bitrefillListener);
        }

        String currency = getArguments().getString(CURRENCY_KEY);
        Partner partner = (Partner) getArguments().getSerializable(PARTNER_KEY);

        String bitrefillRef = "bAshL935";
        String utmSource = "LitewalletAndroid";
        String bitrefillUrl = String.format( BRConstants.BITREFILL_AFFILIATE_LINK + "/embed/?paymentMethod=litecoin&ref=%s&utm_source=%s", bitrefillRef,utmSource);

        String buyUrl = partner == Partner.BITREFILL ? bitrefillUrl : url(getContext(), partner, currency);

        Timber.d("URL %s", buyUrl);
        webView.loadUrl(buyUrl);

        return rootView;
    }

        public static String url(Context context, Partner partner, String currency) {
        String walletAddress = BRSharedPrefs.getReceiveAddress(context);
        Long timestamp = new Date().getTime();
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String prefix = partner == Partner.MOONPAY ? "/moonpay/buy" : "";
        return String.format(LW_API_HOST + prefix + "?address=%s&code=%s&idate=%s&uid=%s", walletAddress, currency, timestamp, uuid);
    }

    private void closePayment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onStop() {
        super.onStop();
        BRAnimator.animateBackgroundDim(backgroundLayout, true);
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        // For Android API < 11 (3.0 OS)
        public void openFileChooser(ValueCallback<Uri> valueCallback) {
            uploadMessage = valueCallback;
            openImageChooserActivity();
        }

        // For Android API >= 11 (3.0 OS)
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            uploadMessage = valueCallback;
            openImageChooserActivity();
        }

        // For Android API >= 21 (5.0 OS)
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            openImageChooserActivity();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progress.setProgress(newProgress, true);
            } else progress.setProgress(newProgress);
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            Timber.d("shouldOverrideUrlLoading: URL=%s\nMethod=%s", url, request.getMethod());
            if (url.equalsIgnoreCase(onCloseUrl)) {
                closePayment();
                onCloseUrl = null;
            } else if (url.contains("close")) {
                closePayment();
            } else {
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Timber.d("onPageStarted: %s", url);
            super.onPageStarted(view, url, favicon);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Timber.d("onPageFinished %s", url);
            progress.setVisibility(View.GONE);
        }
    };

    private void openImageChooserActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Image Chooser"), FILE_CHOOSER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessageAboveL != null) {
                Uri[] results = getResultAboveL(resultCode, data);
                uploadMessageAboveL.onReceiveValue(results);
            } else if (uploadMessage != null) {
                Uri result = data != null && resultCode == Activity.RESULT_OK ? data.getData() : null;
                uploadMessage.onReceiveValue(result);
            }
            uploadMessageAboveL = null;
            uploadMessage = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Uri[] getResultAboveL(int resultCode, Intent intent) {
        Uri[] results = null;
        if (intent != null && resultCode == Activity.RESULT_OK) {
            String dataString = intent.getDataString();
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results[i] = item.getUri();
                }
            } else if (dataString != null) {
                results = new Uri[]{Uri.parse(dataString)};
            }
        }
        return results;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
    }

    public enum Partner {
        SIMPLEX, MOONPAY, BITREFILL
    }
}