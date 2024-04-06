package com.breadwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.datastore.preferences.protobuf.Internal;

import com.breadwallet.di.component.DaggerAppComponent;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.LocaleHelper;
import com.breadwallet.tools.util.Utils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import com.pusher.pushnotifications.PushNotifications;

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

        // setup Timber
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableCrashlytics);
        AnalyticsManager.init(this);
        AnalyticsManager.logCustomEvent(BRConstants._20191105_AL);
        loadAdvertisingAndPush(this);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DISPLAY_HEIGHT_PX = size.y;
        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
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
    private void loadAdvertisingAndPush(Context app) {
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(app);
                    finishedLoadingPushService(adInfo);

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                finishedLoadingPushService(null);
            }
        });
        thr.start();
    }
    private void finishedLoadingPushService(final AdvertisingIdClient.Info adInfo) {
        if(adInfo!=null) {

            // setup Pusher Interests
            String adInfoString = adInfo.getId();
            String generalAndroidInterest = "general-android";
            String debugGeneralAndroidInterest = "debug-general-android";

            // setup Push Notifications
            String pusherInstanceID = Utils.fetchPartnerKey(this, PartnerNames.PUSHERSTAGING);
            PushNotifications.start(getApplicationContext(), pusherInstanceID);
            PushNotifications.addDeviceInterest(generalAndroidInterest);
            PushNotifications.addDeviceInterest(debugGeneralAndroidInterest);

            String opsAll = Utils.fetchPartnerKey(this, PartnerNames.OPSALL);
            Timber.d("timber:  ops All  %s",opsAll);
            //Send params for pusher setup
            Bundle params = new Bundle();
            params.putString("general-interest",generalAndroidInterest);
            params.putString("debug-general-interest",debugGeneralAndroidInterest);
            params.putString("device-ad-info",adInfoString);
            AnalyticsManager.logCustomEventWithParams(BRConstants._20240123_RAGI, params);
        }
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
