package com.breadwallet.tools.util;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BRDateUtil {

    public static String getCustomSpan(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();

        now.setTime(new Date());
        then.setTime(date);

        // Get the represented date in milliseconds
        long nowMs = now.getTimeInMillis();
        long thenMs = then.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = nowMs - thenMs;

        // Calculate difference in seconds
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffMinutes < 60) {
            return diffMinutes + " m";

        } else if (diffHours < 24) {
            return diffHours + " h";

        } else if (diffDays < 7) {
            return diffDays + " d";

        } else {
            SimpleDateFormat todate = new SimpleDateFormat("MMM dd",
                    Locale.getDefault());

            return todate.format(date);
        }
    }
}
