package com.breadwallet.presenter.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.breadwallet.R;
import timber.log.Timber;

/**
 * Activity to display Nexus Wallet information and benefits when users try to create a new wallet
 */
public class NexusWalletInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nexus_wallet_info);

        setupButtons();
    }

    private void setupButtons() {
        // Get Nexus Wallet button
        Button getNexusWalletButton = findViewById(R.id.get_nexus_wallet_button);
        getNexusWalletButton.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.litecoin.nexus"));
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to web browser if Play Store is not available
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.litecoin.nexus"));
                startActivity(intent);
            }
        });

        // Learn more button
        Button learnMoreButton = findViewById(R.id.learn_more_button);
        learnMoreButton.setOnClickListener(view -> {
            try {
                WebViewActivity.start(
                    this,
                    "https://support.nexuswallet.com/hc/nexus-help-center/articles/1749345713-start-using-nexus-wallet-a-simple-guide-for-litewallet-users",
                    "Nexus Wallet Guide"
                );
            } catch (Exception e) {
                Timber.e("Failed to open Nexus Help Center: %s", e.getMessage());
            }
        });

    }
}
