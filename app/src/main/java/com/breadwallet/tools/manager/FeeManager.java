package com.breadwallet.tools.manager;

import static com.breadwallet.tools.util.BRConstants.LW_API_HOST;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.StringDef;

import com.breadwallet.presenter.entities.Fee;
import com.breadwallet.tools.util.Utils;
import com.platform.APIClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Litewallet
 * Created by Mohamed Barry on 3/10/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
public final class FeeManager {

    // this is the default that matches the mobile-api if the server is unavailable
    private static final long defaultEconomyFeePerKB = 2_500L; // From legacy minimum. default min is 1000 as Litecoin Core version v0.17.1
    private static final long defaultRegularFeePerKB = 2_5000L;
    private static final long defaultLuxuryFeePerKB = 66_746L;
    private static final long defaultTimestamp = 1583015199122L;

    private Fee defaultValues = new Fee(defaultLuxuryFeePerKB, defaultRegularFeePerKB, defaultEconomyFeePerKB, defaultTimestamp);

    private static final FeeManager instance;

    private String feeType;
    public Fee currentFees;

    public static FeeManager getInstance() {
        return instance;
    }

    static {
        instance = new FeeManager();
        instance.initWithDefaultValues();
    }

    private void initWithDefaultValues() {
        currentFees = defaultValues;
        feeType = REGULAR;
    }

    private FeeManager() {
    }

    public void setFeeType(@FeeType String feeType) {
        this.feeType = feeType;
    }

    public void resetFeeType() {
        this.feeType = REGULAR;
    }

    public boolean isRegularFee() {
        return feeType.equals(REGULAR);
    }

    public static final String LUXURY = "luxury";
    public static final String REGULAR = "regular";
    public static final String ECONOMY = "economy";

    public void setFees(long luxuryFee, long regularFee, long economyFee) {
        // TODO: to be implemented when feePerKB API will be ready
    }

    public static void updateFeePerKb(Context app) {

        String jsonString = "{'fee_per_kb': 10000, 'fee_per_kb_economy': 2500, 'fee_per_kb_luxury': 66746}";
        try {
            JSONObject obj = new JSONObject(jsonString);
            // TODO: Refactor when mobile-api v0.4.0 is in prod
            long regularFee = obj.optLong("fee_per_kb");
            long economyFee = obj.optLong("fee_per_kb_economy");
            long luxuryFee = obj.optLong("fee_per_kb_luxury");
            FeeManager.getInstance().setFees(luxuryFee, regularFee, economyFee);
            BRSharedPrefs.putFeeTime(app, System.currentTimeMillis()); //store the time of the last successful fee fetch
        } catch (JSONException e) {
            Timber.e(new IllegalArgumentException("updateFeePerKb: FAILED: " + jsonString, e));
        }
    }

    // createGETRequestURL
    // Creates the params and headers to make a GET Request
    private static String createGETRequestURL(Context app, String myURL) {
        Request request = new Request.Builder()
                .url(myURL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-agent", Utils.getAgentString(app, "android/HttpURLConnection"))
                .get().build();
        String response = null;
        Response resp = APIClient.getInstance(app).sendRequest(request, false, 0);

        try {
            if (resp == null) {
                Timber.i("timber: urlGET: %s resp is null", myURL);
                return null;
            }
            response = resp.body().string();
            String strDate = resp.header("date");
            if (strDate == null) {
                Timber.i("timber: urlGET: strDate is null!");
                return response;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            Date date = formatter.parse(strDate);
            long timeStamp = date.getTime();
            BRSharedPrefs.putSecureTime(app, timeStamp);
        } catch (ParseException | IOException e) {
            Timber.e(e);
        } finally {
            if (resp != null) resp.close();
        }
        return response;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LUXURY, REGULAR, ECONOMY})
    public @interface FeeType {
    }
}
