package com.breadwallet.analytics;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import com.breadwallet.presenter.activities.intro.IntroActivity;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRConstants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AnalyticsTests {
    public static final String TAG = AnalyticsTests.class.getName();
    @Rule
    public ActivityTestRule<IntroActivity> mActivityRule = new ActivityTestRule<>(IntroActivity.class);

    @Before
    public void setUp() {
        Log.e(TAG, "setUp: ");
    }

    @After
    public void tearDown() {
    }

    /// This needs to be debugged. Some logs:
    /// Error: WARNING: The option setting is experimental and unsupported
    /// Manifest merger failed with multiple errors, see logs
    /// Execution failed for task ':app:processLoafDebugAndroidTestManifest'.

    @Test
    public void testFirebaseAnalyticsConstants() {
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20191105_AL));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20191105_VSC));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20202116_VRC));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20191105_DSL));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20191105_DTBT));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_RNI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_FNI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_TNI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_WNI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_PNI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200111_UTST));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200112_ERR));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200112_DSR));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200125_DSRR));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20201118_DTGS));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWP));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200223_DD));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200225_DCD));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200301_DUDFPK));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20201121_SIL));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20201121_DRIA));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20201121_FRIA));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20200207_DTHB));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210405_TAWDF));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210804_TAA2FAC));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210804_TAWDS));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210804_TAULI));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210804_TAULO));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20210427_HCIEEH));
        Assert.assertNotNull(AnalyticsManager.logCustomEvent(BRConstants._20220822_UTOU));
    }
}
