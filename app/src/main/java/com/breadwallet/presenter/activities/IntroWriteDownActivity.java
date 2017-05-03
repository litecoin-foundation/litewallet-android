package com.breadwallet.presenter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.settings.DefaultCurrencyActivity;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.security.PostAuthenticationProcessor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.wallet.BRWalletManager;

public class IntroWriteDownActivity extends Activity {
    private static final String TAG = IntroWriteDownActivity.class.getName();
    private Button writeButton;
    public static boolean appVisible = false;
    private static IntroWriteDownActivity app;

    public static IntroWriteDownActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_down);

        writeButton = (Button) findViewById(R.id.button_write_down);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;

                PostAuthenticationProcessor.getInstance().onPhraseCheckAuth(IntroWriteDownActivity.this, false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        ActivityUTILS.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            BRAnimator.startBreadActivity(this, false);
            if (!isDestroyed()) finish();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BRConstants.SHOW_PHRASE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuthenticationProcessor.getInstance().onPhraseCheckAuth(this, true);
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

}
