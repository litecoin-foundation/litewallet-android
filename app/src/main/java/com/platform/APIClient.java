package com.platform;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.NetworkOnMainThreadException;

import com.breadwallet.BreadApp;
import com.breadwallet.BuildConfig;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRConstants;

import static com.breadwallet.tools.util.BRCompressor.gZipExtract;

public class APIClient {

    // proto is the transport protocol to use for talking to the API (either http or https)
    private static final String PROTO = "https";

    // convenience getter for the API endpoint
    public static String BASE_URL = PROTO + "://" + BreadApp.HOST;
    //feePerKb url
    private static final String FEE_PER_KB_URL = "/v1/fee-per-kb";
    //singleton instance
    private static APIClient ourInstance;


    private static final String BUNDLES = "bundles";

    private static final String BUNDLES_FOLDER = String.format("/%s", BUNDLES);

    private static final boolean PRINT_FILES = false;

    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    private boolean platformUpdating = false;
    private AtomicInteger itemsLeftToUpdate = new AtomicInteger(0);

    private Context ctx;

    public static synchronized APIClient getInstance(Context context) {
        if (ourInstance == null) ourInstance = new APIClient(context);
        return ourInstance;
    }

    private APIClient(Context context) {
        ctx = context;
        itemsLeftToUpdate = new AtomicInteger(0);
    }

    //returns the fee per kb or 0 if something went wrong
    public long feePerKb() {
        if (ActivityUTILS.isMainThread()) {
            throw new NetworkOnMainThreadException();
        }
        Response response = null;
        try {
            String strUtl = BASE_URL + FEE_PER_KB_URL;
            Request request = new Request.Builder().url(strUtl).get().build();
            String body = null;
            try {
                response = sendRequest(request, false, 0);
                body = response.body().string();
                Timber.d("timber: fee per kb %s",body);
            } catch (IOException e) {
                Timber.e(e);
                AnalyticsManager.logCustomEvent(BRConstants._20200111_RNI);
            }
            JSONObject object = null;
            object = new JSONObject(body);
            return (long) object.getInt("fee_per_kb");
        } catch (JSONException e) {
            Timber.e(e);
        } finally {
            if (response != null) response.close();
        }
        return 0;
    }

    public Response sendRequest(Request locRequest, boolean needsAuth, int retryCount) {
        if (retryCount > 1)
            throw new RuntimeException("sendRequest: Warning retryCount is: " + retryCount);
        if (ActivityUTILS.isMainThread()) {
            throw new NetworkOnMainThreadException();
        }
        boolean isTestNet = BuildConfig.LITECOIN_TESTNET;
        String lang = getCurrentLocale(ctx);
        Request request = locRequest.newBuilder().header("X-Litecoin-Testnet", isTestNet ? "true" : "false").header("Accept-Language", lang).build();

        Response response;
        ResponseBody postReqBody;
        byte[] data = new byte[0];
        try {
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).connectTimeout(60, TimeUnit.SECONDS)/*.addInterceptor(new LoggingInterceptor())*/.build();

            // DEV Uncomment to see values
            // Timber.d("timber: sendRequest: headers for : %s \n %s", request.url(), request.headers());

            String agent = Utils.getAgentString(ctx, "OkHttp/3.4.1");
            request = request.newBuilder().header("User-agent", agent).build();
            response = client.newCall(request).execute();
            try {
                data = response.body().bytes();
            } catch (IOException e) {
                Timber.e(e);
            }

            if (response.isRedirect()) {
                String newLocation = request.url().scheme() + "://" + request.url().host() + response.header("location");
                Uri newUri = Uri.parse(newLocation);
                if (newUri == null) {
                    Timber.d("timber: sendRequest: redirect uri is null");
                } else if (!newUri.getHost().equalsIgnoreCase(BreadApp.HOST) || !newUri.getScheme().equalsIgnoreCase(PROTO)) {
                    Timber.d("timber: sendRequest: WARNING: redirect is NOT safe: %s", newLocation);
                } else {
                    Timber.d("timber: redirecting: %s >>> %s", request.url(), newLocation);
                    response.close();
                    return sendRequest(new Request.Builder().url(newLocation).get().build(), needsAuth, 0);
                }
                return new Response.Builder().code(500).request(request).body(ResponseBody.create(null, new byte[0])).message("Internal Error").protocol(Protocol.HTTP_1_1).build();
            }
        } catch (IOException e) {
            Timber.e(e);
            return new Response.Builder().code(599).request(request).body(ResponseBody.create(null, new byte[0])).protocol(Protocol.HTTP_1_1).message("Network Connection Timeout").build();
        }

        if (response.header("content-encoding") != null && response.header("content-encoding").equalsIgnoreCase("gzip")) {
            Timber.d("timber: sendRequest: the content is gzip, unzipping");
            byte[] decompressed = gZipExtract(data);
            postReqBody = ResponseBody.create(null, decompressed);
            try {
                if (response.code() != 200) {
                    Timber.d("timber: sendRequest: (%s)%s, code (%d), mess (%s), body (%s)", request.method(),
                            request.url(), response.code(), response.message(), new String(decompressed, "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                Timber.e(e);
            }
            return response.newBuilder().body(postReqBody).build();
        } else {
            try {
                if (response.code() != 200) {
                    Timber.d("timber: sendRequest: (%s)%s, code (%d), mess (%s), body (%s)", request.method(),
                            request.url(), response.code(), response.message(), new String(data, "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                Timber.e(e);
            }
        }

        postReqBody = ResponseBody.create(null, data);

        return response.newBuilder().body(postReqBody).build();
    }

    public String buildUrl(String path) {
        return BASE_URL + path;
    }

    private void itemFinished() {
        int items = itemsLeftToUpdate.incrementAndGet();
        if (items >= 4) {
            Timber.d("timber: PLATFORM ALL UPDATED: %s", items);
            platformUpdating = false;
            itemsLeftToUpdate.set(0);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public String getCurrentLocale(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            //noinspection deprecation
            return ctx.getResources().getConfiguration().locale.getLanguage();
        }
    }
}
