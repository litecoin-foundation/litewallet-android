package com.breadwallet.tools.manager;

import android.content.Context;
import android.os.Bundle;

import com.breadwallet.tools.util.BRConstants;
import com.google.firebase.analytics.FirebaseAnalytics;

public final class AnalyticsManager {

    private static FirebaseAnalytics instance;

    private AnalyticsManager() {
        // NO-OP
    }

    public static void init(Context context) {
        instance = FirebaseAnalytics.getInstance(context);
    }

    public static Object logCustomEvent(@BRConstants.Event String customEvent) {
        instance.logEvent(customEvent, null);
        return null;
    }

    public static void logCustomEventWithParams(@BRConstants.Event String customEvent, Bundle params) {
        instance.logEvent(customEvent, params);
    }
}




