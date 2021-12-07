package com.breadwallet.tools.util;

import timber.log.Timber;

public class CustomLogger {

    public static void logThis(String... args) {
        StringBuilder strToLog = new StringBuilder();
        int i = 0;
        for (String arg : args) {
            if (i++ % 2 == 0) {
                strToLog.append(" | ").append(arg).append(": ");
            } else {
                strToLog.append(arg);
            }
            if (i % 4 == 0) strToLog.append("\n");
        }
        Timber.d(strToLog.toString());
    }
}
