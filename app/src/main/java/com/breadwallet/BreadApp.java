package com.breadwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.LocaleHelper;
import com.breadwallet.tools.util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    public void onCreate() {
        super.onCreate();

        keyStoreManager = new KeyStoreManager(
            this,
            new KeyStoreKeyGenerator.Impl()
        );

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        DISPLAY_HEIGHT_PX = Resources.getSystem()
            .getDisplayMetrics()
            .heightPixels;
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
        super.attachBaseContext(
            LocaleHelper.Companion.getInstance().setLocale(base)
        );
    }

    public interface OnAppBackgrounded {
        void onBackgrounded();
    }
}
