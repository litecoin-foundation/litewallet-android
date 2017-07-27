package com.breadwallet.tools.util;

import android.content.Context;

import com.breadwallet.tools.manager.SharedPreferencesManager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import static com.breadwallet.tools.util.BRConstants.CURRENT_UNIT_BITS;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/28/16.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BRCurrency {
    public static final String TAG = BRCurrency.class.getName();


//    public static String getMiddleTextExchangeString(double rate, String iso, Activity ctx) {
////        Log.e(TAG, "result of the exchange rate calculation: " + result);
//        if (rate == 0) rate = 1;
//        if (ctx == null) ctx = MainActivity.app;
//        if (ctx == null) return null;
//        long result = BRWalletManager.getInstance().bitcoinAmount(100, new BigDecimal(String.valueOf(rate)).multiply(new BigDecimal("100")).doubleValue());
//        return getFormattedCurrencyString(iso, 100) + " = " +
//                getFormattedCurrencyString("BTC", result);
//    }

//    public static String getBitsAndExchangeString(BigDecimal rate, String iso, BigDecimal target, Activity ctx) {
//        if (rate.doubleValue() == 0) rate = new BigDecimal("1");
//        long exchange = BRWalletManager.getInstance().localAmount(target.longValue(),
//                new BigDecimal(String.valueOf(rate)).multiply(new BigDecimal("100")).doubleValue());
//        return getFormattedCurrencyString(ctx, "BTC", target) + " = " +
//                getFormattedCurrencyString(ctx, iso, new BigDecimal(exchange));
//    }

//    public static String getExchangeForAmount(BigDecimal rate, String iso, BigDecimal targetAmount, Context ctx) {
//        if (rate.doubleValue() == 0) rate = new BigDecimal("1");
//
//        long exchange = BRWalletManager.getInstance().localAmount(targetAmount.longValue(), rate.doubleValue());
//
//        return getFormattedCurrencyString(ctx, iso, new BigDecimal(exchange));
//    }

//    public static String getCurrentBalanceText(Activity ctx) {
//        CurrencyFetchManager cm = CurrencyFetchManager.getInstance(ctx);
//        String iso = SharedPreferencesManager.getIso(ctx);
//        double rate = SharedPreferencesManager.getRate(ctx);
//        long exchange = BRWalletManager.getInstance().localAmount(BRWalletManager.getInstance().getCatchedBalance(),
//                new BigDecimal(String.valueOf(rate)).multiply(new BigDecimal("100")).doubleValue());
//
//        return getFormattedCurrencyString(ctx, "BTC", new BigDecimal(BRWalletManager.getInstance().getCatchedBalance())) + " (" +
//                getFormattedCurrencyString(ctx, iso, new BigDecimal(exchange)) + ")";
//    }

    // amount is in currency or BTC (bits, mBTC or BTC)
    public static String getFormattedCurrencyString(Context app, String isoCurrencyCode, BigDecimal amount) {
//        Log.e(TAG, "amount: " + amount);
        DecimalFormat currencyFormat;

        // This formats currency values as the user expects to read them (default locale).
        currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        // This specifies the actual currency that the value is in, and provide
        // s the currency symbol.
        DecimalFormatSymbols decimalFormatSymbols;
        Currency currency;
        String symbol = null;
        decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
//        int decimalPoints = 0;
        if (Objects.equals(isoCurrencyCode, "BTC")) {
            symbol = BRExchange.getBitcoinSymbol(app);
        } else {
            try {
                currency = Currency.getInstance(isoCurrencyCode);
            } catch (IllegalArgumentException e) {
                currency = Currency.getInstance(Locale.getDefault());
            }
            symbol = currency.getSymbol();
//            decimalPoints = currency.getDefaultFractionDigits();
        }
        decimalFormatSymbols.setCurrencySymbol(symbol);
//        currencyFormat.setMaximumFractionDigits(decimalPoints);
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setMaximumFractionDigits(SharedPreferencesManager.getCurrencyUnit(app) == BRConstants.CURRENT_UNIT_BITCOINS ? 8 : 2);
        currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        currencyFormat.setNegativePrefix(decimalFormatSymbols.getCurrencySymbol() + "-");
        currencyFormat.setNegativeSuffix("");
        return currencyFormat.format(amount.doubleValue());
    }


    public static int getNumberOfDecimalPlaces(String amount) {
        int index = amount.indexOf(".");
        return index < 0 ? 0 : amount.length() - index - 1;
    }

    public static String getSymbolByIso(Context app, String iso) {
        String symbol;
        if (Objects.equals(iso, "BTC")) {
            String currencySymbolString = BRConstants.bitcoinLowercase;
            if (app != null) {
                int unit = SharedPreferencesManager.getCurrencyUnit(app);
                switch (unit) {
                    case CURRENT_UNIT_BITS:
                        currencySymbolString = BRConstants.bitcoinLowercase;
                        break;
                    case BRConstants.CURRENT_UNIT_MBITS:
                        currencySymbolString = "m" + BRConstants.bitcoinUppercase;
                        break;
                    case BRConstants.CURRENT_UNIT_BITCOINS:
                        currencySymbolString = BRConstants.bitcoinUppercase;
                        break;
                }
            }
            symbol = currencySymbolString;
        } else {
            Currency currency;
            try {
                currency = Currency.getInstance(iso);
            } catch (IllegalArgumentException e) {
                currency = Currency.getInstance(Locale.getDefault());
            }
            symbol = currency.getSymbol();
        }
        return Utils.isNullOrEmpty(symbol) ? iso : symbol;
    }

    public static int getMaxDecimalPlaces(String iso) {
        if (Utils.isNullOrEmpty(iso)) return 8;

        if (iso.equalsIgnoreCase("BTC")) {
            return 8;
        } else {
            Currency currency = Currency.getInstance(iso);
            return currency.getDefaultFractionDigits();
        }

    }


}
