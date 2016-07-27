
package com.breadwallet.presenter.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.breadwallet.R;
import com.breadwallet.BreadWalletApp;
import com.breadwallet.presenter.fragments.IntroNewRecoverFragment;
import com.breadwallet.presenter.fragments.IntroNewWalletFragment;
import com.breadwallet.presenter.fragments.IntroRecoverWalletFragment;
import com.breadwallet.presenter.fragments.IntroWarningFragment;
import com.breadwallet.presenter.fragments.IntroWelcomeFragment;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.manager.SharedPreferencesManager;
import com.breadwallet.tools.animation.BackgroundMovingAnimator;
import com.breadwallet.tools.animation.DecelerateOvershootInterpolator;
import com.breadwallet.tools.security.KeyStoreManager;
import com.breadwallet.tools.security.PostAuthenticationProcessor;
import com.breadwallet.wallet.BRWalletManager;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on 8/4/15.
 * Copyright (c) 2016 breadwallet llc <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class IntroActivity extends FragmentActivity {
    private static final String TAG = IntroActivity.class.getName();
    public static IntroActivity app;
    private Button leftButton;

    //loading the native library
    static {
        System.loadLibrary("core");
    }

    private boolean backNotAllowed = false;

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        app = this;
        // Activity being restarted from stopped state
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        app = this;

        leftButton = (Button) findViewById(R.id.intro_left_button);
        leftButton.setVisibility(View.GONE);
        leftButton.setClickable(false);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        byte[] masterPubKey = KeyStoreManager.getMasterPublicKey(this);
        boolean isFirstAddressCorrect = false;
        if (masterPubKey != null && masterPubKey.length != 0) {
            isFirstAddressCorrect = checkFirstAddress(masterPubKey);
        }
        Log.e(TAG, "isFirstAddressCorrect: " + isFirstAddressCorrect);
        if (!isFirstAddressCorrect) {
            Log.e(TAG, "CLEARING THE WALLET");
            BRWalletManager.getInstance(this).wipeWalletButKeystore(this);
        }
        getFragmentManager().beginTransaction().add(R.id.intro_layout, new IntroWelcomeFragment(),
                IntroWelcomeFragment.class.getName()).commit();

        setStatusBarColor();

        String canary = KeyStoreManager.getKeyStoreCanary(this, BRConstants.CANARY_REQUEST_CODE);
        if (canary.equalsIgnoreCase(KeyStoreManager.NO_AUTH)) return;
        if (!canary.equalsIgnoreCase(BRConstants.CANARY_STRING)) {
            Log.e(TAG, "!canary.equalsIgnoreCase(BRConstants.CANARY_STRING)");
            String phrase = KeyStoreManager.getKeyStorePhrase(this, BRConstants.CANARY_REQUEST_CODE);
            if (phrase.equalsIgnoreCase(KeyStoreManager.NO_AUTH)) return;
            if (phrase.isEmpty()) {
                Log.e(TAG, "phrase == null || phrase.isEmpty() : " + phrase);
                BRWalletManager m = BRWalletManager.getInstance(this);
                m.wipeKeyStore();
                m.wipeWalletButKeystore(this);
                BRAnimator.resetFragmentAnimator();
            } else {
                Log.e(TAG, "phrase != null : " + phrase);
                KeyStoreManager.putKeyStoreCanary(BRConstants.CANARY_STRING, this, 0);
            }
        }
        startTheWalletIfExists();

    }

    private void setStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.intro_status_bar));
    }

    public boolean checkFirstAddress(byte[] mpk) {
        String addressFromPrefs = SharedPreferencesManager.getFirstAddress(this);
        String generatedAddress = BRWalletManager.getFirstAddress(mpk);
        Log.e(TAG, "addressFromPrefs: " + addressFromPrefs);
        Log.e(TAG, "generatedAddress: " + generatedAddress);
        return addressFromPrefs.equals(generatedAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //testSQLiteConnectivity(this);   //do some SQLite testing
        app = this;
        //animates the orange BW background moving.
        ImageView background = (ImageView) findViewById(R.id.intro_bread_wallet_image);
        background.setScaleType(ImageView.ScaleType.MATRIX);
        BackgroundMovingAnimator.animateBackgroundMoving(background);

    }

    @Override
    protected void onPause() {
        super.onPause();
        BackgroundMovingAnimator.stopBackgroundMoving();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    private void showNewRecoverWalletFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        IntroWelcomeFragment introWelcomeFragment = (IntroWelcomeFragment) fragmentManager.
                findFragmentByTag(IntroWelcomeFragment.class.getName());
        if (introWelcomeFragment != null) {
            final IntroNewRecoverFragment introNewRecoverFragment = new IntroNewRecoverFragment();
            fragmentTransaction.replace(introWelcomeFragment.getId(), introNewRecoverFragment, IntroNewRecoverFragment.class.getName());
            fragmentTransaction.commitAllowingStateLoss();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TranslateAnimation trans = new TranslateAnimation(app.getResources().getInteger(R.integer.standard_screen_width), 0, 0, 0);
                    trans.setDuration(BRAnimator.horizontalSlideDuration);
                    trans.setInterpolator(new DecelerateOvershootInterpolator(1f, 0.5f));
                    View view = introNewRecoverFragment.getView();
                    Log.e(TAG, "startAnimation");
                    if (view != null)
                        view.startAnimation(trans);
                }
            }, 1);
        }

    }

    public void showNewWalletFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setClickable(true);
        final IntroNewRecoverFragment introNewRecoverFragment = (IntroNewRecoverFragment) fragmentManager.
                findFragmentByTag(IntroNewRecoverFragment.class.getName());

        final IntroNewWalletFragment introNewWalletFragment = new IntroNewWalletFragment();
        fragmentTransaction.replace(introNewRecoverFragment.getId(), introNewWalletFragment, IntroNewWalletFragment.class.getName()).
                addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation trans = new TranslateAnimation(app.getResources().getInteger(R.integer.standard_screen_width), 0, 0, 0);
                trans.setDuration(BRAnimator.horizontalSlideDuration);
                trans.setInterpolator(new DecelerateOvershootInterpolator(1f, 0.5f));
                View view = introNewWalletFragment.getView();
                if (view != null)
                    view.startAnimation(trans);
            }
        }, 1);
    }

    public void showRecoverWalletFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setClickable(true);
        IntroNewRecoverFragment introNewRecoverFragment = (IntroNewRecoverFragment) fragmentManager.
                findFragmentByTag(IntroNewRecoverFragment.class.getName());
        final IntroRecoverWalletFragment introRecoverWalletFragment = new IntroRecoverWalletFragment();
        fragmentTransaction.replace(introNewRecoverFragment.getId(), introRecoverWalletFragment, IntroRecoverWalletFragment.class.getName()).
                addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation trans = new TranslateAnimation(app.getResources().getInteger(R.integer.standard_screen_width), 0, 0, 0);
                trans.setDuration(BRAnimator.horizontalSlideDuration);
                trans.setInterpolator(new DecelerateOvershootInterpolator(1f, 0.5f));
                View view = introRecoverWalletFragment.getView();
                if (view != null)
                    view.startAnimation(trans);
            }
        }, 1);
    }

    public void showWarningFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        IntroNewWalletFragment introNewWalletFragment = (IntroNewWalletFragment) fragmentManager.
                findFragmentByTag(IntroNewWalletFragment.class.getName());
        fragmentTransaction.replace(introNewWalletFragment.getId(), new IntroWarningFragment(), IntroWarningFragment.class.getName());
        introNewWalletFragment.introGenerate.setClickable(false);
        leftButton.setVisibility(View.GONE);
        leftButton.setClickable(false);
        fragmentTransaction.commitAllowingStateLoss();
        backNotAllowed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void startMainActivity() {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        if (!IntroActivity.this.isDestroyed()) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "IntroActivity, onActivityResult: " + requestCode);
        switch (requestCode) {
            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuthenticationProcessor.getInstance().onCreateWalletAuth(this);
                } else {
                    BRWalletManager m = BRWalletManager.getInstance(this);
                    m.wipeKeyStore();
                    m.wipeWalletButKeystore(this);
                    BRAnimator.resetFragmentAnimator();
                    finish();
                }
                break;
            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuthenticationProcessor.getInstance().onRecoverWalletAuth(this);
                } else {
                    finish();
                }
                break;
            case BRConstants.CANARY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuthenticationProcessor.getInstance().onCanaryCheck(this);
                } else {
                    finish();
                }
                break;

        }

    }

    public void startIntroShowPhrase() {
        Intent intent;
        intent = new Intent(this, IntroShowPhraseActivity.class);
        startActivity(intent);
        if (!IntroActivity.this.isDestroyed()) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (backNotAllowed) return;
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            if (backStackEntryCount == 1) {
                leftButton.setVisibility(View.GONE);
                leftButton.setClickable(false);
            }
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    public void startTheWalletIfExists() {
        final BRWalletManager m = BRWalletManager.getInstance(this);
        if (!m.isPasscodeEnabled(this)) {
            //Device passcode/password should be enabled for the app to work
            ((BreadWalletApp) getApplication()).showDeviceNotSecuredWarning(this);
        } else {
            if (m.noWallet(app)) {
                //now check if there is a wallet or should we create/restore one.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showNewRecoverWalletFragment();
                    }
                }, 800);
            } else {
                startMainActivity();
            }

        }
    }
}
