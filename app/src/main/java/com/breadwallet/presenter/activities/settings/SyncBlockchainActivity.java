package com.breadwallet.presenter.activities.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.wallet.BRPeerManager;




public class SyncBlockchainActivity extends BRActivity {

    private static final String TAG = SyncBlockchainActivity.class.getName();
    private Button scanButton;
    private Button lowPrivacyButton;
    private Button semiPrivateButton;
    private Button anonymousButton;

    private ImageButton closeButton;

    private RadioGroup syncRadioGroup;

    public static boolean appVisible = false;
    private static TextView syncPreferenceTextView;

    private static SyncBlockchainActivity app;
    public static SyncBlockchainActivity getApp() {
        return app;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void updateSyncPreference() {
        float fprate =  BRSharedPrefs.getFalsePositivesRate(SyncBlockchainActivity.this);
        String rateAsString = String.format("%1.5f",fprate);
        syncPreferenceTextView.setText(getString(R.string.sync_preferences, rateAsString));

        if (fprate == BRConstants.FALSE_POS_RATE_LOW_PRIVACY) {
            syncRadioGroup.check(R.id.radio_low_privacy);
        }
        else if (fprate == BRConstants.FALSE_POS_RATE_SEMI_PRIVACY) {
            syncRadioGroup.check(R.id.radio_semi_private);
        }else  {
            ///Assumed the Anon option is preferred
            syncRadioGroup.check(R.id.radio_anonymous);
        }
    }
    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_blockchain);

        syncRadioGroup = (RadioGroup) findViewById(R.id.sync_radio_group);

        syncPreferenceTextView =  (TextView)  findViewById(R.id.sync_preferences);
        scanButton = (Button) findViewById(R.id.button_scan);
        lowPrivacyButton = (Button) findViewById(R.id.radio_low_privacy);
        semiPrivateButton = (Button) findViewById(R.id.radio_semi_private);
        anonymousButton = (Button) findViewById(R.id.radio_anonymous);
        closeButton = (ImageButton) findViewById(R.id.close_button);

        lowPrivacyButton.setText(getString(R.string.radio_low_privacy));
        semiPrivateButton.setText(getString(R.string.radio_semi_private));
        anonymousButton.setText(getString(R.string.radio_anonymous));

        updateSyncPreference();

        lowPrivacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BRSharedPrefs.putFalsePositivesRate(SyncBlockchainActivity.this, BRConstants.FALSE_POS_RATE_LOW_PRIVACY);
                updateSyncPreference();
            }
        });

        semiPrivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BRSharedPrefs.putFalsePositivesRate(SyncBlockchainActivity.this, BRConstants.FALSE_POS_RATE_SEMI_PRIVACY);
                updateSyncPreference();
            }
        });
        anonymousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BRSharedPrefs.putFalsePositivesRate(SyncBlockchainActivity.this, BRConstants.FALSE_POS_RATE_ANONYMOUS);
                updateSyncPreference();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRDialog.showCustomDialog(SyncBlockchainActivity.this, getString(R.string.ReScan_alertTitle),
                        getString(R.string.ReScan_footer), getString(R.string.ReScan_alertAction), getString(R.string.Button_cancel),
                        new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        BRSharedPrefs.putStartHeight(SyncBlockchainActivity.this, 0);
                                        BRSharedPrefs.putAllowSpend(SyncBlockchainActivity.this, false);
                                        BRPeerManager.getInstance().rescan();
                                        BRAnimator.startBreadActivity(SyncBlockchainActivity.this, false);
                                        AnalyticsManager.logCustomEvent(BRConstants._20200112_DSR);
                                    }
                                });
                            }
                        }, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, 0);
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        closeButton = (ImageButton) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

}
