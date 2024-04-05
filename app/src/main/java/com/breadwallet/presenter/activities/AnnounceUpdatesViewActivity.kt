package com.breadwallet.presenter.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.breadwallet.R
import com.breadwallet.databinding.ActivityAnnounceUpdatesViewBinding
import com.breadwallet.presenter.activities.intro.RecoverActivity

class AnnounceUpdatesViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnnounceUpdatesViewBinding
    private var url: String = "https://litewallet.io/mobile-signup/signup.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnnounceUpdatesViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isNewWallet : Boolean = intent.getBooleanExtra("isNewWallet", false)
        val buttonNoThanks: Button = findViewById(R.id.btnNoThanks)
        buttonNoThanks.setOnClickListener{
            nextActivity(isNewWallet)
        }

        val buttonClose: ImageButton = findViewById(R.id.btnClose)
        buttonClose.setOnClickListener{
            nextActivity(isNewWallet)
        }

        val webView: WebView = findViewById(R.id.webViewEmail)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }

    fun nextActivity(isNewWallet : Boolean) {
        if (isNewWallet) {
            val intent = Intent(this, SetPinActivity::class.java)
            startActivity(intent)
        }else {
            val intent = Intent(this, RecoverActivity::class.java)
            startActivity(intent)
        }
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )
        }
    }
}
