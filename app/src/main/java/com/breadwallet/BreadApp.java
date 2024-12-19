package com.breadwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.appsflyer.AppsFlyerLib;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerLib;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.LocaleHelper;
import com.breadwallet.tools.util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.litewallet.di.Module;
import com.loafwallet.util.cryptography.KeyStoreKeyGenerator;
import com.loafwallet.util.cryptography.KeyStoreManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.litewallet.di.Module;
import com.litewallet.notification.NotificationHandlerKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class BreadApp extends Application {
    public static int DISPLAY_HEIGHT_PX;
    public static String HOST = "api.loafwallet.org";
    private static List<OnAppBackgrounded> listeners;
    private static Timer isBackgroundChecker;
    public static AtomicInteger activityCounter = new AtomicInteger();
    public static long backgroundedTime;
    private static Activity currentActivity;

    public static Module module;
    public static KeyStoreManager keyStoreManager;

    @Override
    public void onCreate() {
        super.onCreate();

        /// DEV:  Top placement requirement.
        boolean enableCrashlytics = !Utils.isEmulatorOrDebug(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableCrashlytics);

        module = new Module();
        module.getRemoteConfigSource().initialize();
        keyStoreManager = new KeyStoreManager(this, new KeyStoreKeyGenerator.Impl());

        NotificationHandlerKt.setupNotificationChannels(this);

        AnalyticsManager.init(this);
        AnalyticsManager.logCustomEvent(BRConstants._20191105_AL);

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        //dev: uncomment for debugging
//        FirebaseMessaging.getInstance()
//                .getToken()
//                .addOnSuccessListener(new OnSuccessListener<String>() {
//                    @Override
//                    public void onSuccess(String s) {
//                        Timber.d("timber: fcm getToken= %s", s);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Timber.d(e, "timber: fcm getToken failure");
//                    }
//                });

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        DISPLAY_HEIGHT_PX = Resources.getSystem().getDisplayMetrics().heightPixels;

        String afID = Utils.fetchPartnerKey(this, PartnerNames.AFDEVID);
        AppsFlyerLib appsFlyerLib = AppsFlyerLib.getInstance();
        appsFlyerLib.init(afID, null, this);
        appsFlyerLib.setDebugLog(BuildConfig.DEBUG);
        appsFlyerLib.setCollectAndroidID(true);
        appsFlyerLib.start(this);
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
}
