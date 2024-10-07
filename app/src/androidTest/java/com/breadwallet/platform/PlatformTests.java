package com.breadwallet.platform;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import com.breadwallet.BreadApp;
import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.tools.util.BRCompressor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;
import com.jniwrappers.BRKey;
import com.platform.APIClient;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class PlatformTests {
    public static final String TAG = PlatformTests.class.getName();
    @Rule
    public ActivityTestRule<BreadActivity> mActivityRule = new ActivityTestRule<>(
            BreadActivity.class);

    // proto is the transport protocol to use for talking to the API (either http or https)
    private static final String PROTO = "https";
    // host is the server(s) on which the API is hosted
    // convenience getter for the API endpoint
    private static final String BASE_URL = PROTO + "://" + BreadApp.HOST;
    //feePerKb url
    private static final String FEE_PER_KB_URL = "/v1/fee-per-kb";
    //token
    private static final String TOKEN = "/token";
    //me
    private static final String ME = "/me";

    private static final String GET = "GET";
    private static final String POST = "POST";

    //loading the native library
    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    @Test
    public void testFeePerKbFetch() {
        long fee = APIClient.getInstance(mActivityRule.getActivity()).feePerKb();
        System.out.println("testFeePerKbFetch: fee: " + fee);
        Assert.assertNotSame(fee, (long) 0);

    }

    @Test
    public void testGZIP() {
        String data = "Ladies and Gentlemen of the class of '99: If I could offer you only one tip 11111111for the future, " +
                "sunscreen would be it.";
        Assert.assertFalse(BRCompressor.isGZIPStream(data.getBytes()));
        byte[] compressedData = BRCompressor.gZipCompress(data.getBytes());
        Assert.assertTrue(BRCompressor.isGZIPStream(compressedData));
        Log.e(TAG, "testGZIP: " + new String(compressedData));
        Assert.assertNotNull(compressedData);
        Assert.assertTrue(compressedData.length > 0);
        byte[] decompressedData = BRCompressor.gZipExtract(compressedData);
        Assert.assertFalse(BRCompressor.isGZIPStream(decompressedData));
        Assert.assertNotNull(decompressedData);
        Assert.assertEquals(new String(decompressedData), data);
        Assert.assertNotEquals(compressedData.length, decompressedData.length);
    }

    @Test
    public void testBZip2() {
        String data = "Ladies and Gentlemen of the class of '99: If I could offer you only one tip 11111111for the future, " +
                "sunscreen would be it.";
        byte[] compressedData = new byte[0];
        try {
            compressedData = BRCompressor.bz2Compress(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(compressedData);
        Assert.assertTrue(compressedData.length > 0);
        byte[] decompressedData = BRCompressor.bz2Extract(compressedData);
        Assert.assertNotNull(decompressedData);
        Assert.assertEquals(new String(decompressedData), data);
        Assert.assertNotEquals(compressedData.length, decompressedData.length);
    }

}
