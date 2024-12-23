package com.breadwallet.tools.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.Currency;
import java.util.concurrent.ThreadLocalRandom;
import androidx.core.app.ActivityCompat;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.intro.IntroActivity;
import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.sqlite.CurrencyDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import timber.log.Timber;
import org.json.*;
import  java.io.InputStream;

import static android.content.Context.FINGERPRINT_SERVICE;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.IntStream;
import android.content.res.AssetManager;
public class Utils {

    public static boolean isUsingCustomInputMethod(Activity context) {
        if (context == null) return false;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
        final int N = mInputMethodProperties.size();
        for (int i = 0; i < N; i++) {
            InputMethodInfo imi = mInputMethodProperties.get(i);
            if (imi.getId().equals(
                    Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.DEFAULT_INPUT_METHOD))) {
                if ((imi.getServiceInfo().applicationInfo.flags &
                        ApplicationInfo.FLAG_SYSTEM) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void printPhoneSpecs() {
        Timber.d("timber: ***************************PHONE SPECS***************************");
        Timber.d("timber: * screen X: %d , screen Y: %s", IntroActivity.screenParametersPoint.x, IntroActivity.screenParametersPoint.y);
        Timber.d("timber: * Build.CPU_ABI: %s", Build.CPU_ABI);
        Timber.d("timber: * maxMemory:%s", Runtime.getRuntime().maxMemory());
        Timber.d("timber: ----------------------------PHONE SPECS----------------------------");
    }

    public static boolean isEmulatorOrDebug(Context app) {
        String fing = Build.FINGERPRINT;
        boolean isEmulator = false;
        if (fing != null) {
            isEmulator = fing.contains("vbox") || fing.contains("generic");
        }
        return isEmulator || (0 != (app.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static String getFormattedDateFromLong(Context app, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("M/d@ha", Locale.getDefault());
        boolean is24HoursFormat = false;
        if (app != null) {
            is24HoursFormat = android.text.format.DateFormat.is24HourFormat(app.getApplicationContext());
            if (is24HoursFormat) {
                formatter = new SimpleDateFormat("M/d H", Locale.getDefault());
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String result = formatter.format(calendar.getTime()).toLowerCase().replace("am", "a").replace("pm", "p");
        if (is24HoursFormat) result += "h";
        return result;
    }

    public static String formatTimeStamp(long time, String pattern) {
        return android.text.format.DateFormat.format(pattern, time).toString();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(byte[] arr) {
        return arr == null || arr.length == 0;
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static int getPixelsFromDps(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String createBitcoinUrl(String address, long satoshiAmount, String label, String message, String rURL) {

        Uri.Builder builder = new Uri.Builder();
        builder = builder.scheme("litecoin");
        if (address != null && !address.isEmpty())
            builder = builder.appendPath(address);
        if (satoshiAmount != 0)
            builder = builder.appendQueryParameter("amount", new BigDecimal(satoshiAmount).divide(new BigDecimal(100000000), 8, BRConstants.ROUNDING_MODE).toPlainString());
        if (label != null && !label.isEmpty())
            builder = builder.appendQueryParameter("label", label);
        if (message != null && !message.isEmpty())
            builder = builder.appendQueryParameter("message", message);
        if (rURL != null && !rURL.isEmpty())
            builder = builder.appendQueryParameter("r", rURL);

        return builder.build().toString().replaceFirst("/", "");

    }

    public static void hideKeyboard(Context app) {
        if (app != null) {
            View view = ((Activity) app).getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

    }

    public static String getAgentString(Context app, String cfnetwork) {
        int versionNumber = 0;
        if (app != null) {
            try {
                PackageInfo pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                versionNumber = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Timber.e(e);
            }
        }
        return String.format(Locale.ENGLISH, "%s/%d %s Android/%s", "Litewallet", versionNumber, cfnetwork, Build.VERSION.RELEASE);
    }

    public static String reverseHex(String hex) {
        if (hex == null) return null;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= hex.length() - 2; i = i + 2) {
            result.append(new StringBuilder(hex.substring(i, i + 2)).reverse());
        }
        return result.reverse().toString();
    }

    public static String join(String[] array, CharSequence separator) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            stringBuilder.append(array[i]);
            stringBuilder.append(separator);
        }
        stringBuilder.append(array[array.length - 1]);
        return stringBuilder.toString();
    }
    public static String fetchPartnerKey(Context app, PartnerNames name) {

        JSONObject keyObject;
        AssetManager assetManager = app.getAssets();
        try (InputStream inputStream = assetManager.open("partner-keys.json")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder sb = new StringBuilder();
                String line;
                String opsString = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                keyObject = new JSONObject(sb.toString()).getJSONObject("keys");

                if (name == PartnerNames.LITEWALLETOPS) {
                   JSONArray array = new JSONArray(keyObject.get(name.getKey()).toString());
                    int randomNum = ThreadLocalRandom.current().nextInt(0, array.length() - 1);
                    return array.getString(randomNum);
                }
                else if (name == PartnerNames.OPSALL) {
                    JSONArray opsArray = new JSONArray(keyObject.get(name.getKey()).toString());

                    if (opsArray.length() > 0) {
                        for (int i=0;i<opsArray.length();i++){
                            opsString = (new StringBuilder())
                                 .append(opsString)
                                 .append(opsArray.getString(i))
                                 .append(",")
                                 .toString();
                        }
                    } else {
                        Timber.e("timber: ops element fail");
                    }
                    return opsString.replaceAll("\\s+","");
                }
                else if (name == PartnerNames.AFDEVID) {
                    return keyObject.optString(name.getKey());
                }
                else if (name == PartnerNames.PUSHER) {
                    JSONObject jsonObj = new JSONObject(keyObject.get(name.getKey()).toString());
                    return jsonObj.toString();
                }
                else if (name == PartnerNames.PUSHERSTAGING) {
                    JSONObject jsonObj = new JSONObject(keyObject.get(name.getKey()).toString());
                    return jsonObj.toString();
                }
                Timber.d("timber: fetchPartnerKey name key found %s",name.getKey());

                return keyObject.get(name.getKey()).toString();
            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("timber: fetchPartnerKey IOEXception");

            } catch (JSONException e) {
                e.printStackTrace();
                Timber.d("timber: fetchPartnerKey JSONException");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bundle   params = new Bundle();
        params.putString("lwa_error_message: %s Key not found", name.getKey());
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR,params);
        Timber.d("timber: fetchPartnerKey lwa_error_message");
        return "";
    }
    /// Description: 1715876807
    public static long tieredOpsFee(Context app, long sendAmount) {

        double doubleRate = 83.000;
        double sendAmountDouble = new Double(String.valueOf(sendAmount));
        String usIso = Currency.getInstance(new Locale("en", "US")).getCurrencyCode();
        CurrencyEntity currency = CurrencyDataSource.getInstance(app).getCurrencyByIso(usIso);
        if (currency != null) {
            doubleRate = currency.rate;
        }
        double usdInLTC = sendAmountDouble * doubleRate / 100_000_000.0;
        usdInLTC = Math.floor(usdInLTC * 100) / 100;

        if (isBetween(usdInLTC, 0.00, 20.00))  {
            double lowRate = usdInLTC * 0.01;
            return (long) ((lowRate / doubleRate) * 100_000_000.0);
        }
        else if (isBetween(usdInLTC, 20.00, 50.00)) {
            return (long) ((0.30 / doubleRate) * 100_000_000.0);
        }
        else if (isBetween(usdInLTC, 50.00, 100.00)) {
             return (long) ((1.00 / doubleRate) * 100_000_000.0);
         }
        else if (isBetween(usdInLTC, 100.00, 500.00)) {
             return (long) ((2.00 / doubleRate) * 100_000_000.0);
        }
        else if (isBetween(usdInLTC, 500.00, 1000.00)) {
             return (long) ((2.50 / doubleRate) * 100_000_000.0);
        }
        else if ( usdInLTC > 1000.00) {
             return (long) ((3.00 / doubleRate) * 100_000_000.0);
        }
        else {
             return (long) ((3.00 / doubleRate) * 100_000_000.0);
        }
    }
    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }
    public static Set<String> litewalletOpsSet(Context app) {
        List<String> addressList = Collections.singletonList(Utils.fetchPartnerKey(app, PartnerNames.LITEWALLETOPS));
        return new HashSet<String>(addressList);
    }

    private class UInt64 {
        public UInt64(BigInteger bigInteger) {
        }
    }
}
