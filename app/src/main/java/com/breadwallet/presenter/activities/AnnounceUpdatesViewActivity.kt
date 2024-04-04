package com.breadwallet.presenter.activities

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.breadwallet.R
import com.breadwallet.databinding.ActivityAnnounceUpdatesViewBinding

class AnnounceUpdatesViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnnounceUpdatesViewBinding
    private var url: String = "https://litewallet.io/mobile-signup/signup.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnnounceUpdatesViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView: WebView = findViewById(R.id.webViewEmail)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)

        val buttonNoThanks: Button = findViewById(R.id.btnNoThanks)
        buttonNoThanks.setOnClickListener {
            finish()
        }
    }
}
