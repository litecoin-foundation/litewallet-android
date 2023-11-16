package com.breadwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.breadwallet.di.component.DaggerAppComponent;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.LocaleHelper;
import com.breadwallet.tools.util.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import com.pusher.pushnotifications.PushNotifications;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

public class BreadApp extends Application {
    public static int DISPLAY_HEIGHT_PX;
    FingerprintManager mFingerprintManager;
    public static String HOST = "api.loafwallet.org";
    private static List<OnAppBackgrounded> listeners;
    private static Timer isBackgroundChecker;
    public static AtomicInteger activityCounter = new AtomicInteger();
    public static long backgroundedTime;

    private static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder().build().inject(this);

        boolean enableCrashlytics = true;
        if (Utils.isEmulatorOrDebug(this)) {
            enableCrashlytics = false;
        }
        // setup Push Notifications
        //PushNotifications.start(getApplicationContext(), "06a438d5-27ba-4cc2-94df-554dc932a796");
        //PushNotifications.addDeviceInterest("");

//        // Pusher
//        pushNotifications.start(instanceId: Partner.partnerKeyPath(name: .pusherStaging))
//        // pushNotifications.registerForRemoteNotifications()
//        let generaliOSInterest = "general-ios"
//        let debugGeneraliOSInterest = "debug-general-ios"
//
//        try? pushNotifications
//                .addDeviceInterest(interest: generaliOSInterest)
//        try? pushNotifications
//                .addDeviceInterest(interest: debugGeneraliOSInterest)
//
//        let interests = pushNotifications.getDeviceInterests()?.joined(separator: "|") ?? ""
//        let device = UIDevice.current.identifierForVendor?.uuidString ?? "ID"
//        let interestesDict: [String: String] = ["device_id": device,
//                "pusher_interests": interests]
//
//        LWAnalytics.logEventWithParameters(itemName: ._20231202_RIGI, properties: interestesDict)

//        delay(4.0) {
//            self.appDelegate.pushNotifications.registerForRemoteNotifications()
//        }


        // setup Timber
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableCrashlytics);
        AnalyticsManager.init(this);

        AnalyticsManager.logCustomEvent(BRConstants._20191105_AL);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        }
        DISPLAY_HEIGHT_PX = size.y;
        //mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
    }


    public static Context getBreadContext() {
        return currentActivity == null ? SyncReceiver.app : currentActivity;
    }

    public static void setBreadContext(Activity app) {
        currentActivity = app;
    }

    public static void fireListeners() {
        if (listeners == null) return;
        for (OnAppBackgrounded lis : listeners) lis.onBackgrounded();
    }

    public static void addOnBackgroundedListener(OnAppBackgrounded listener) {
        if (listeners == null) listeners = new ArrayList<>();
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static boolean isAppInBackground(final Context context) {
        return context == null || activityCounter.get() <= 0;
    }

    //call onStop on evert activity so
    public static void onStop(final BRActivity app) {
        if (isBackgroundChecker != null) isBackgroundChecker.cancel();
        isBackgroundChecker = new Timer();
        TimerTask backgroundCheck = new TimerTask() {
            @Override
            public void run() {
                if (isAppInBackground(app)) {
                    backgroundedTime = System.currentTimeMillis();
                    Timber.d("timber: App went in background!");
                    // APP in background, do something
                    isBackgroundChecker.cancel();
                    fireListeners();
                }
                // APP in foreground, do something else
            }
        };

        isBackgroundChecker.schedule(backgroundCheck, 500, 500);
    }

    @Override
    protected void attachBaseContext(Context base) {
        LocaleHelper.Companion.init(base);
        super.attachBaseContext(LocaleHelper.Companion.getInstance().setLocale(base));
    }

    public interface OnAppBackgrounded {
        void onBackgrounded();
    }

    private static class CrashReportingTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, String tag, String message, Throwable throwable) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            Throwable t = throwable != null ? throwable : new Exception(message);

            // Firebase Crash Reporting
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority);
            crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag);
            crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message);

            crashlytics.recordException(t);
        }
    }
}
