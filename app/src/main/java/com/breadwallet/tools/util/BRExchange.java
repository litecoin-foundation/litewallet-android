package com.breadwallet.tools.util;

import android.content.Context;

import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.sqlite.CurrencyDataSource;
import com.breadwallet.wallet.BRWalletManager;
import java.math.BigDecimal;

import static com.breadwallet.tools.util.BRConstants.CURRENT_UNIT_PHOTONS;
import static com.breadwallet.tools.util.BRConstants.ROUNDING_MODE;

public class BRExchange {

    public static final long ONE_LITECOIN_OF_LITOSHIS = 100000000L;

    public static BigDecimal getMaxAmount(Context context, String iso) {
        final long MAX_LTC = 84000000;
        if (iso.equalsIgnoreCase("LTC"))
            return getLitecoinForLitoshis(context, new BigDecimal(MAX_LTC * 100000000));
        CurrencyEntity ent = CurrencyDataSource.getInstance(context).getCurrencyByIso(iso);
        if (ent == null) return new BigDecimal(Integer.MAX_VALUE);
        return new BigDecimal(ent.rate * MAX_LTC);
    }

    // amount in Litoshis
    public static BigDecimal getLitecoinForLitoshis(Context app, BigDecimal amount) {
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCurrencyUnit(app);
        switch (unit) {
            case CURRENT_UNIT_PHOTONS:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100"), 2, ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_LITES:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100000"), 5, ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_LITECOINS:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100000000"), 8, ROUNDING_MODE);
                break;
        }
        return result;
    }

    public static BigDecimal getLitoshisForLitecoin(Context app, BigDecimal amount) {
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCurrencyUnit(app);
        switch (unit) {
            case CURRENT_UNIT_PHOTONS:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100"));
                break;
            case BRConstants.CURRENT_UNIT_LITES:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100000"));
                break;
            case BRConstants.CURRENT_UNIT_LITECOINS:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100000000"));
                break;
        }
        return result;
    }

    //get an iso amount from litoshis
    public static BigDecimal getAmountFromLitoshis(Context app, String iso, BigDecimal amount) {
        BigDecimal result;
        if (iso.equalsIgnoreCase("LTC")) {
            result = getLitecoinForLitoshis(app, amount);
        } else {
            //multiply by 100 because core function localAmount accepts the smallest amount e.g. cents
            CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByIso(iso);
            if (ent == null) return new BigDecimal(0);
            BigDecimal rate = new BigDecimal(ent.rate).multiply(new BigDecimal(100));
            result = new BigDecimal(BRWalletManager.getInstance().localAmount(amount.longValue(), rate.doubleValue()))
                    .divide(new BigDecimal(100), 2, BRConstants.ROUNDING_MODE);
        }
        return result;
    }

    public static String getLitecoinSymbol(Context app) {
        String currencySymbolString = BRConstants.litecoinLowercase;
        if (app != null) {
            int unit = BRSharedPrefs.getCurrencyUnit(app);
            switch (unit) {
                case CURRENT_UNIT_PHOTONS:
                    currencySymbolString = "m" + BRConstants.litecoinLowercase;
                    break;
                case BRConstants.CURRENT_UNIT_LITES:
                    currencySymbolString = BRConstants.litecoinLowercase;
                    break;
                case BRConstants.CURRENT_UNIT_LITECOINS:
                    currencySymbolString = BRConstants.litecoinUppercase;
                    break;
            }
        }
        return currencySymbolString;
    }


    //get litoshis from an iso symbol amount
    public static BigDecimal getLitoshisFromAmount(Context app, String iso, BigDecimal amount) {
        BigDecimal result;
        if (iso.equalsIgnoreCase("LTC")) {
            result = BRExchange.getLitoshisForLitecoin(app, amount);
        } else {
            //multiply by 100 because core function localAmount accepts the smallest amount e.g. cents
            CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByIso(iso);
            if (ent == null) return new BigDecimal(0);
            BigDecimal rate = new BigDecimal(ent.rate).multiply(new BigDecimal(100));
            result = new BigDecimal(BRWalletManager.getInstance().bitcoinAmount(amount.multiply(new BigDecimal(100)).longValue(), rate.doubleValue()));
        }
        return result;
    }

    public static BigDecimal convertltcsToLitoshis(Double amountLtc) {
        return BigDecimal.valueOf(amountLtc).multiply(BigDecimal.valueOf(ONE_LITECOIN_OF_LITOSHIS));
    }
}
