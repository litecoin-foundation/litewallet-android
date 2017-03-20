package com.breadwallet;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.presenter.activities.IntroActivity;
import com.breadwallet.presenter.entities.PaymentRequestEntity;
import com.breadwallet.presenter.entities.PaymentRequestWrapper;
import com.breadwallet.presenter.fragments.FingerprintDialogFragment;
import com.breadwallet.presenter.fragments.FragmentBreadPin;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.security.KeyStoreManager;
import com.breadwallet.wallet.BRWalletManager;

import java.util.concurrent.TimeUnit;

import static android.R.attr.mode;
import static com.breadwallet.presenter.activities.BreadActivity.app;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/22/15.
 * Copyright (c) 2016 breadwallet LLC
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

public class BreadWalletApp extends Application {
    private static final String TAG = BreadWalletApp.class.getName();
    public static boolean unlocked = false;
    public boolean allowKeyStoreAccess;
    public static int DISPLAY_HEIGHT_PX;
    FingerprintManager mFingerprintManager;


    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int DISPLAY_WIDTH_PX = size.x;
        DISPLAY_HEIGHT_PX = size.y;
        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

    }



//    public void setTopMiddleView(int view, String text) {
//        if(app == null) return;
//        switch (view) {
//            case BRConstants.BREAD_WALLET_IMAGE:
//                if (app.viewFlipper.getDisplayedChild() == 1) {
//                    app.viewFlipper.showPrevious();
//                }
//                break;
//            case BRConstants.BREAD_WALLET_TEXT:
//                if (app.viewFlipper.getDisplayedChild() == 0) {
//                    app.viewFlipper.showNext();
//                }
//                ((TextView) app.viewFlipper.getCurrentView()).setText(text);
//                ((TextView) app.viewFlipper.getCurrentView()).setTextSize(20);
//                break;
//        }
//    }

//    public void setLockerPayButton(int view) {
//        switch (view) {
//            case BRConstants.LOCKER_BUTTON:
//                if (app.lockerPayFlipper.getDisplayedChild() == 1) {
//                    app.lockerPayFlipper.showPrevious();
//
//                } else if (app.lockerPayFlipper.getDisplayedChild() == 2) {
//                    app.lockerPayFlipper.showPrevious();
//                    app.lockerPayFlipper.showPrevious();
//                }
//                app.lockerButton.setVisibility(unlocked ? View.INVISIBLE : View.VISIBLE);
//                break;
//            case BRConstants.PAY_BUTTON:
//                if (app.lockerPayFlipper.getDisplayedChild() == 0)
//                    app.lockerPayFlipper.showNext();
//                if (app.lockerPayFlipper.getDisplayedChild() == 2)
//                    app.lockerPayFlipper.showPrevious();
//                break;
//            case BRConstants.REQUEST_BUTTON:
//                if (app.lockerPayFlipper.getDisplayedChild() == 0) {
//                    app.lockerPayFlipper.showNext();
//                    app.lockerPayFlipper.showNext();
//                } else if (app.lockerPayFlipper.getDisplayedChild() == 1) {
//                    app.lockerPayFlipper.showNext();
//                }
//                break;
//        }
//
//    }

    public void showDeviceNotSecuredWarning(final Activity context) {
        Log.e(TAG, "WARNING device is not secured!");
        new AlertDialog.Builder(context)
                .setMessage(R.string.encryption_needed_for_wallet)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        context.finish();
                    }
                })
                .show();
    }




    public void setUnlocked(boolean b) {
//        unlocked = b;
//        if (app != null) {
//            app.lockerButton.setVisibility(b ? View.GONE : View.VISIBLE);
//            app.lockerButton.setClickable(!b);
//            MiddleViewAdapter.resetMiddleView(app, null);
//        }
    }

    public void allowKeyStoreAccessForSeconds() {
        allowKeyStoreAccess = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                allowKeyStoreAccess = false;
            }
        }, 2 * 1000);
    }

    public void hideKeyboard(Context app) {
        if (app != null) {
            View view = ((Activity) app).getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return;
            }
        }
        Log.e(TAG, "hideKeyboard: FAILED");
    }

//    public boolean hasInternetAccess() {
//        return isNetworkAvailable();
//    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
