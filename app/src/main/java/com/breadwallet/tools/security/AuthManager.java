package com.breadwallet.tools.security;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.DisabledActivity;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.fragments.FragmentFingerprint;
import com.breadwallet.presenter.fragments.FragmentPin;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AuthManager {
    private static AuthManager instance;
    private String previousTry;

    private AuthManager() {
        previousTry = "";
    }

    public static AuthManager getInstance() {
        if (instance == null)
            instance = new AuthManager();
        return instance;
    }

    public boolean checkAuth(CharSequence passSequence, Context context) {
        Timber.d("timber: checkAuth: ");
        String tempPass = passSequence.toString();
        if (!previousTry.equals(tempPass)) {
            int failCount = BRKeyStore.getFailCount(context);
            BRKeyStore.putFailCount(failCount + 1, context);
        }
        previousTry = tempPass;

        String pass = BRKeyStore.getPinCode(context);
        boolean match = pass != null && tempPass.equals(pass);
        if (!match) {
            if (BRKeyStore.getFailCount(context) >= 3) {
                setWalletDisabled((Activity) context);
            }
        }

        return match;
    }

    //when pin auth success
    public void authSuccess(final Context app) {
        //put the new total limit in 3 seconds, leave some time for the core to register any new tx
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
                AuthManager.getInstance().setTotalLimit(app, BRWalletManager.getInstance().getTotalSent()
                        + BRKeyStore.getSpendLimit(app));
            }
        });

        BRKeyStore.putFailCount(0, app);
        BRKeyStore.putLastPinUsedTime(System.currentTimeMillis(), app);
    }

    public void authFail(Context app) {
        //TODO: Check if this cruft from the BRD implementation
    }

    public boolean isWalletDisabled(Activity app) {
        int failCount = BRKeyStore.getFailCount(app);
        return failCount >= 3 && disabledUntil(app) > BRSharedPrefs.getSecureTime(app);
    }

    public long disabledUntil(Activity app) {
        int failCount = BRKeyStore.getFailCount(app);
        long failTimestamp = BRKeyStore.getFailTimeStamp(app);
        double pow = Math.pow(6, failCount - 3) * 60;
        return (long) ((failTimestamp + pow * 1000));
    }

    public void setWalletDisabled(Activity app) {
        if (!(app instanceof DisabledActivity))
            ActivityUTILS.showWalletDisabled(app);
    }

    public void setPinCode(String pass, Activity context) {
        BRKeyStore.putFailCount(0, context);
        BRKeyStore.putPinCode(pass, context);
        BRKeyStore.putLastPinUsedTime(System.currentTimeMillis(), context);
        setSpendingLimitIfNotSet(context);
    }

    /**
     * Returns the total current limit that cannot be surpass without a pin
     */
    public long getTotalLimit(Context activity) {
        return BRKeyStore.getTotalLimit(activity);
    }

    /**
     * Sets the total current limit that cannot be surpass without a pin
     */
    public void setTotalLimit(Context activity, long limit) {
        BRKeyStore.putTotalLimit(limit, activity);
    }

    private void setSpendingLimitIfNotSet(final Activity activity) {
        if (activity == null) return;
        long limit = AuthManager.getInstance().getTotalLimit(activity);
        if (limit == 0) {
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    long totalSpent = BRWalletManager.getInstance().getTotalSent();
                    long totalLimit = totalSpent + BRKeyStore.getSpendLimit(activity);
                    setTotalLimit(activity, totalLimit);
                }
            });
        }
    }

    public void updateDots(Context context, int pinLimit, String pin, View dot1, View dot2, View dot3, View dot4, View dot5, View dot6, int emptyPinRes, final OnPinSuccess onPinSuccess) {
        if (dot1 == null || context == null) return;
        int selectedDots = pin.length();

        if (pinLimit == 6) {
            dot6.setVisibility(View.VISIBLE);
            dot1.setVisibility(View.VISIBLE);
            dot1.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
            selectedDots--;
        } else {
            dot6.setVisibility(View.GONE);
            dot1.setVisibility(View.GONE);
        }

        dot2.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot3.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot4.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot5.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        if (pinLimit == 6) {
            selectedDots--;
            dot6.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        }

        if (pin.length() == pinLimit) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPinSuccess.onSuccess();
                }
            }, 100);
        }
    }

    public void authPrompt(final Context context, String title, String message, boolean forcePin, boolean forceFingerprint, BRAuthCompletion completion) {
        if (context == null || !(context instanceof Activity)) {
            Timber.i("timber: authPrompt: context is null or not Activity: %s", context);
            return;
        }
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);

        boolean useFingerPrint = isFingerPrintAvailableAndSetup(context);

        if (BRKeyStore.getFailCount(context) != 0) {
            useFingerPrint = false;
        }
        long passTime = BRKeyStore.getLastPinUsedTime(context);

        if (passTime + TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS) <= System.currentTimeMillis()) {
            useFingerPrint = false;
        }

        if (forceFingerprint)
            useFingerPrint = true;

        if (forcePin)
            useFingerPrint = false;

        final FragmentActivity app = (FragmentActivity) context;

        FragmentFingerprint fingerprintFragment;
        FragmentPin breadPin;

        if (keyguardManager.isKeyguardSecure()) {
            if (useFingerPrint) {
                fingerprintFragment = new FragmentFingerprint();
                Bundle args = new Bundle();
                args.putString("title", title);
                args.putString("message", message);
                fingerprintFragment.setArguments(args);
                fingerprintFragment.setCompletion(completion);
                androidx.fragment.app.FragmentTransaction transaction = app.getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(0, 0, 0, R.animator.plain_300);
                transaction.add(android.R.id.content, fingerprintFragment, FragmentFingerprint.class.getName());
                transaction.addToBackStack(null);
                if (!app.isDestroyed() && !app.isFinishing())
                    transaction.commit();
            } else {
                breadPin = new FragmentPin();
                Bundle args = new Bundle();
                args.putString("title", title);
                args.putString("message", message);
                breadPin.setArguments(args);
                breadPin.setCompletion(completion);
                FragmentTransaction transaction = app.getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(0, 0, 0, R.animator.plain_300);
                transaction.add(android.R.id.content, breadPin, breadPin.getClass().getName());
                transaction.addToBackStack(null);
                if (!app.isDestroyed() && !app.isFinishing()) {
                    transaction.commit();
                }
            }
        } else {
            BRDialog.showCustomDialog(app,
                    "",
                    app.getString(R.string.Prompts_NoScreenLock_body_android),
                    app.getString(R.string.AccessibilityLabels_close),
                    null,
                    new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            app.finish();
                        }
                    }, null, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            app.finish();
                        }
                    }, 0);
        }

    }

    public static boolean isFingerPrintAvailableAndSetup(Context context) {
        return Utils.isFingerprintAvailable(context) && Utils.isFingerprintEnrolled(context);
    }

    public interface OnPinSuccess {
        void onSuccess();
    }
}
