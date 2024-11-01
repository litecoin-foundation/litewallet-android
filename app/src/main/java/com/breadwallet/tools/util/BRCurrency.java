package com.breadwallet.tools.util;

import android.content.Context;

import com.breadwallet.tools.manager.BRSharedPrefs;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import static com.breadwallet.tools.util.BRConstants.CURRENT_UNIT_PHOTONS;

public class BRCurrency {
    public static final String TAG = BRCurrency.class.getName();

    // amount is in currency or LTC (bits, mLTC or LTC)
    public static String getFormattedCurrencyString(Context app, String isoCurrencyCode, BigDecimal amount) {
        // This formats currency values as the user expects to read them (default locale).
        DecimalFormat  currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        // This specifies the actual currency that the value is in, and provide
        // s the currency symbol.
        DecimalFormatSymbols decimalFormatSymbols;
        Currency currency;
        String symbol;
        decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
        if (Objects.equals(isoCurrencyCode, "LTC")) {
            symbol = BRExchange.getLitecoinSymbol(app);
        } else {
            try {
                currency = Currency.getInstance(isoCurrencyCode);
            } catch (IllegalArgumentException e) {
                currency = Currency.getInstance(Locale.getDefault());
            }
            symbol = currency.getSymbol();
        }
        decimalFormatSymbols.setCurrencySymbol(symbol);
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setMaximumFractionDigits(BRSharedPrefs.getCurrencyUnit(app) == BRConstants.CURRENT_UNIT_LITECOINS ? 8 : 2);
        currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        currencyFormat.setNegativePrefix(decimalFormatSymbols.getCurrencySymbol() + "-");
        currencyFormat.setNegativeSuffix("");
        return currencyFormat.format(amount.doubleValue());
    }

    public static String getSymbolByIso(Context app, String iso) {
        String symbol;
        if (Objects.equals(iso, "LTC")) {
            String currencySymbolString = BRConstants.litecoinLowercase;
            if (app != null) {
                int unit = BRSharedPrefs.getCurrencyUnit(app);
                switch (unit) {
                    case CURRENT_UNIT_PHOTONS:
                        currencySymbolString = BRConstants.litecoinLowercase;
                        break;
                    case BRConstants.CURRENT_UNIT_LITES:
                        currencySymbolString = "m" + BRConstants.litecoinUppercase;
                        break;
                    case BRConstants.CURRENT_UNIT_LITECOINS:
                        currencySymbolString = BRConstants.litecoinUppercase;
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

    //for now only use for BTC and Bits
    public static String getCurrencyName(Context app, String iso) {
        if (Objects.equals(iso, "LTC")) {
            if (app != null) {
                int unit = BRSharedPrefs.getCurrencyUnit(app);
                switch (unit) {
                    case CURRENT_UNIT_PHOTONS:
                        return "Bits";
                    case BRConstants.CURRENT_UNIT_LITES:
                        return "MBits";
                    case BRConstants.CURRENT_UNIT_LITECOINS:
                        return "LTC";
                }
            }
        }
        return iso;
    }

    public static int getMaxDecimalPlaces(String iso) {
        if (Utils.isNullOrEmpty(iso)) return 8;

        if (iso.equalsIgnoreCase("LTC")) {
            return 8;
        } else {
            Currency currency = Currency.getInstance(iso);
            return currency.getDefaultFractionDigits();
        }

    }


}
