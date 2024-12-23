package com.breadwallet.tools.manager;

import static com.breadwallet.tools.util.BRConstants.LW_API_HOST;
import static com.breadwallet.tools.util.BRConstants.LW_API_HOST_NEW;
import static com.breadwallet.tools.util.BRConstants.LW_BACKUP_API_HOST;
import static com.breadwallet.tools.util.BRConstants.LW_BACKUP_API_HOST_NEW;
import static com.breadwallet.tools.util.BRConstants._20230113_BAC;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.tools.sqlite.CurrencyDataSource;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.Utils;
import com.litewallet.data.source.RemoteConfigSource;
import com.platform.APIClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class BRApiManager {
    private Timer timer;

    private TimerTask timerTask;

    private Handler handler;

    private RemoteConfigSource remoteConfigSource;

    public BRApiManager(RemoteConfigSource remoteConfigSource) {
        this.remoteConfigSource = remoteConfigSource;
        this.handler = new Handler();
    }

    private Set<CurrencyEntity> getCurrencies(Activity context) {
        Set<CurrencyEntity> set = new LinkedHashSet<>();
        try {
            JSONArray arr = fetchRates(context);
            FeeManager.updateFeePerKb(context);
            if (arr != null) {
                String selectedISO = BRSharedPrefs.getIsoSymbol(context);
                int length = arr.length();
                for (int i = 0; i < length; i++) {
                    CurrencyEntity tmp = new CurrencyEntity();
                    try {
                        JSONObject tmpObj = (JSONObject) arr.get(i);
                        tmp.name = tmpObj.getString("code");
                        tmp.code = tmpObj.getString("code");
                        tmp.rate = (float) tmpObj.getDouble("n");
                        if (tmp.code.equalsIgnoreCase(selectedISO)) {
                            BRSharedPrefs.putIso(context, tmp.code);
                            BRSharedPrefs.putCurrencyListPosition(context, i - 1);
                        }
                    } catch (JSONException e) {
                        Timber.e(e);
                    }
                    set.add(tmp);
                }
            } else {
                Timber.d("timber: getCurrencies: failed to get currencies");
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        List tempList = new ArrayList<>(set);
        Collections.reverse(tempList);
        return new LinkedHashSet<>(set);
    }


    private void initializeTimerTask(final Context context) {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                Set<CurrencyEntity> tmp = getCurrencies((Activity) context);
                                CurrencyDataSource.getInstance(context).putCurrencies(tmp);
                            }
                        });
                    }
                });
            }
        };
    }

    public void startTimer(Context context) {
        //set a new Timer
        if (timer != null) return;
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask(context);

        //schedule the timer, after the first 0ms the TimerTask will run every 60000ms
        timer.schedule(timerTask, 0, 4000);
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public JSONArray fetchRates(Activity activity) {
        String jsonString = createGETRequestURL(activity, getBaseUrlProd() + "/api/v1/rates");
        JSONArray jsonArray = null;
        if (jsonString == null) return null;
        try {
            jsonArray = new JSONArray(jsonString);
            // DEV Uncomment to view values
//            Timber.d("timber: baseUrlProd: %s", getBaseUrlProd());
//            Timber.d("timber: JSON %s",jsonArray.toString());

        } catch (JSONException ex) {
            Timber.e(ex);
        }
        return jsonArray == null ? backupFetchRates(activity) : jsonArray;
    }

    public JSONArray backupFetchRates(Activity activity) {
        String baseUrlDev = remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_DEV_NEW_ENABLED) ? LW_BACKUP_API_HOST_NEW : LW_BACKUP_API_HOST;
        String jsonString = createGETRequestURL(activity, baseUrlDev + "/api/v1/rates");

        AnalyticsManager.logCustomEvent(_20230113_BAC);

        JSONArray jsonArray = null;
        if (jsonString == null) return null;
        try {
            jsonArray = new JSONArray(jsonString);

        } catch (JSONException e) {
            Timber.e(e);
        }
        return jsonArray;
    }

    // createGETRequestURL
    // Creates the params and headers to make a GET Request
    private String createGETRequestURL(Context app, String myURL) {
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

    public String getBaseUrlProd() {
        return remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED) ? LW_API_HOST_NEW : LW_API_HOST;
    }
}
