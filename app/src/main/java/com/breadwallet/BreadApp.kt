package com.breadwallet

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Point
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.breadwallet.di.component.DaggerAppComponent
import com.breadwallet.presenter.activities.util.BRActivity
import com.breadwallet.presenter.entities.PartnerNames
import com.breadwallet.tools.listeners.SyncReceiver
import com.breadwallet.tools.manager.AnalyticsManager
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.LocaleHelper
import com.breadwallet.tools.util.LocaleHelper.Companion.init
import com.breadwallet.tools.util.LocaleHelper.Companion.instance
import com.breadwallet.tools.util.Utils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.IOException
import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger
import com.pusher.pushnotifications.PushNotifications
import timber.log.Timber

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)
class BreadApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().build().inject(this)
        var enableCrashlytics = true
        if (Utils.isEmulatorOrDebug(this)) {
            enableCrashlytics = false
        }

        // setup Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableCrashlytics)
        AnalyticsManager.init(this)
        AnalyticsManager.logCustomEvent(BRConstants._20191105_AL)
        loadAdvertisingAndPush(this)
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        DISPLAY_HEIGHT_PX = size.y
        mFingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager?
    }

    companion object {
        var DISPLAY_HEIGHT_PX = 0
        var mFingerprintManager: FingerprintManager? = null
        var listeners: MutableList<OnAppBackgrounded>? = null
        var currentActivity: Activity? = null
        val activityCounter = AtomicInteger()
        val HOST = "api.loafwallet.org"
        var isBackgroundChecker: Timer? = null
        var backgroundedTime: Long = 0

        fun getBreadContext(): Context? {
            return if (currentActivity == null) SyncReceiver.app else currentActivity
        }

        fun setBreadContext(app: Activity?) {
            currentActivity = app
        }

        fun addOnBackgroundedListener(listener: OnAppBackgrounded) {
            if (listeners == null) listeners = ArrayList<OnAppBackgrounded>()
            if (!listeners!!.contains(listener)) listeners!!.add(listener)
        }

        fun isAppInBackground(context: Context?): Boolean {
            return context == null || activityCounter.get() <= 0
        }
        //call onStop on evert activity so
        fun onStop(app: BRActivity?) {
            if (isBackgroundChecker != null) isBackgroundChecker!!.cancel()
            isBackgroundChecker = Timer()
            val backgroundCheck: TimerTask = object : TimerTask() {
                override fun run() {
                    if (isAppInBackground(app)) {
                        backgroundedTime = System.currentTimeMillis()
                        Timber.d("timber: App went in background!")
                        // APP in background, do something
                        isBackgroundChecker!!.cancel()
                        fireListeners()
                    }
                }
            }
            isBackgroundChecker!!.schedule(backgroundCheck, 500, 500)
        }

        fun fireListeners() {
            if (listeners == null) return
            for (lis in listeners!!) lis.onBackgrounded()
        }
    }


    override fun attachBaseContext(base: Context?) {
        init(base!!)
        super.attachBaseContext(instance.setLocale(base))
    }

    interface OnAppBackgrounded {
        fun onBackgrounded()
    }

    open fun loadAdvertisingAndPush(app: Context) {
        val thr = Thread {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(app)
                finishedLoadingPushService(adInfo)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            }
            finishedLoadingPushService(null)
        }
        thr.start()
    }

    open fun finishedLoadingPushService(adInfo: AdvertisingIdClient.Info?) {
        if (adInfo != null) {

            // setup Pusher Interests
            val adInfoString = adInfo.id
            val generalAndroidInterest = "general-android"
            val debugGeneralAndroidInterest = "debug-general-android"

            // setup Push Notifications
            val pusherInstanceID = Utils.fetchPartnerKey(this, PartnerNames.PUSHERSTAGING)
            PushNotifications.start(getApplicationContext(), pusherInstanceID)
            PushNotifications.addDeviceInterest(generalAndroidInterest)
            PushNotifications.addDeviceInterest(debugGeneralAndroidInterest)

            //Send params for pusher setup
            val params = Bundle()
            params.putString("general-interest", generalAndroidInterest)
            params.putString("debug-general-interest", debugGeneralAndroidInterest)
            params.putString("device-ad-info", adInfoString)
            AnalyticsManager.logCustomEventWithParams(BRConstants._20240123_RAGI, params)
        }
    }

    class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            val t = throwable ?: Exception(message)

            // Firebase Crash Reporting
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey(Companion.CRASHLYTICS_KEY_PRIORITY, priority)
            crashlytics.setCustomKey(Companion.CRASHLYTICS_KEY_TAG, tag!!)
            crashlytics.setCustomKey(Companion.CRASHLYTICS_KEY_MESSAGE, message)
            crashlytics.recordException(t)
        }

        companion object {
            private const val CRASHLYTICS_KEY_PRIORITY = "priority"
            private const val CRASHLYTICS_KEY_TAG = "tag"
            private const val CRASHLYTICS_KEY_MESSAGE = "message"
        }
    }


}