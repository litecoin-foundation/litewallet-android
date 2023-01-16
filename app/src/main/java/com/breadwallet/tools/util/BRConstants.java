package com.breadwallet.tools.util;
import com.breadwallet.BuildConfig;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Pair;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.StringDef;

import com.breadwallet.BuildConfig;
import com.breadwallet.tools.manager.BRSharedPrefs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

public class BRConstants {

    public static String getAppNameAndCode() {
        final int versionCode = BuildConfig.VERSION_CODE;
        final String versionName = BuildConfig.VERSION_NAME;
        return String.format("%s (%03d)", versionName, versionCode);
    }
    /**
     * Native library name
     */
    public static final String NATIVE_LIB_NAME = "core-lib";

    /**
     * Permissions
     */
    public static final int CAMERA_REQUEST_ID = 34;

    /**
     * Request codes for auth
     */
    public static final int SHOW_PHRASE_REQUEST_CODE = 111;
    public static final int PAY_REQUEST_CODE = 112;
    public static final int CANARY_REQUEST_CODE = 113;
    public static final int PUT_PHRASE_NEW_WALLET_REQUEST_CODE = 114;
    public static final int PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE = 115;
    public static final int PAYMENT_PROTOCOL_REQUEST_CODE = 116;
    public static final int PROVE_PHRASE_REQUEST = 119;

    /**
     * Request codes for take picture
     */
    public static final int SCANNER_REQUEST = 201;

    public static final String CANARY_STRING = "canary";
    public static final String FIRST_ADDRESS = "firstAddress";
    public static final String SECURE_TIME_PREFS = "secureTime";
    public static final String FEE_KB_PREFS = "feeKb";
    public static final String ECONOMY_FEE_KB_PREFS = "EconomyFeeKb";

    public static String SUPPORT_EMAIL = "support@litecoinfoundation.zendesk.com";

    public static final int ONE_BITCOIN = 100000000;

    /**
     * BRSharedPrefs
     */
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String RECEIVE_ADDRESS = "receive_address";
    public static final String START_HEIGHT = "startHeight";
    public static final String LAST_BLOCK_HEIGHT = "lastBlockHeight";
    public static final String CURRENT_UNIT = "currencyUnit";
    public static final String CURRENT_CURRENCY = "currentCurrency";
    public static final String POSITION = "position";
    public static final String PHRASE_WRITTEN = "phraseWritten";
    public static final String ALLOW_SPEND = "allowSpend";
    public static final String USER_ID = "userId";
    public static final String GEO_PERMISSIONS_REQUESTED = "geoPermissionsRequested";

    /**
     * Currency units
     */
    public static final int CURRENT_UNIT_PHOTONS = 0; // formerly CURRENT_UNIT_BITS
    public static final int CURRENT_UNIT_LITES = 1; // formerly CURRENT_UNIT_MBITS
    public static final int CURRENT_UNIT_LITECOINS = 2;

    public static final String bitcoinLowercase = "\u0142";
    public static final String bitcoinUppercase = "\u0141";

    public static boolean PLATFORM_ON = true;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final boolean WAL = true;

    /**
     * Donation
     */
    public static final String DONATION_ADDRESS =  "MJ4W7NZya4SzE7R6xpEVdamGCimaQYPiWu";
    public static final long DONATION_AMOUNT = 1_400_000;

    /**
     * Support Center article ids.
     */
    public static final String loopBug = "android-loop-bug";

    public static final String TWITTER_LINK = "https://twitter.com/Litewallet_App";

    public static final String INSTAGRAM_LINK = "https://www.instagram.com/litewallet.app";

    public static final String WEB_LINK = "https://litewallet.io";
    public static final String TOS_LINK = "https://litewallet.io/privacy/policy.html";
    public static String CUSTOMER_SUPPORT_LINK = "https://support.litewallet.io";
    public static String BITREFILL_AFFILIATE_LINK = "https://www.bitrefill.com/";

    /**
     * API Hosts
     */
    public static final String LW_API_HOST = "https://api-prod.lite-wallet.org";
    public static final String LW_BACKUP_API_HOST = "https://api-dev.lite-wallet.org";

    public static final String BLOCK_EXPLORER_BASE_URL = BuildConfig.LITECOIN_TESTNET ? "https://testnet.litecore.io/tx/" : "https://insight.litecore.io/tx/";


    public static final String RESET_CARD_PWD_LINK = "https://litecoin.dashboard.getblockcard.com/password/forgot";

    private BRConstants() {
    }

    public static final String _20191105_AL = "app_launched";
    public static final String _20191105_VSC = "visit_send_controller";
    public static final String _20202116_VRC = "visit_receive_controller";
    public static final String _20191105_DSL = "did_send_ltc";
    public static final String _20191105_DTBT = "did_tap_buy_tab";
    public static final String _20200111_RNI = "rate_not_initialized";
    public static final String _20200111_FNI = "feeperkb_not_initialized";
    public static final String _20200111_TNI = "transaction_not_initialized";
    public static final String _20200111_WNI = "wallet_not_initialized";
    public static final String _20200111_PNI = "phrase_not_initialized";
    public static final String _20200111_UTST = "unable_to_sign_transaction";
    public static final String _20200112_ERR = "error";
    public static final String _20200112_DSR = "did_start_resync";
    public static final String _20200125_DSRR = "did_show_review_request";
    public static final String _20201118_DTGS = "did_tap_get_support";
    public static final String _20200217_DUWP = "did_unlock_with_pin";
    public static final String _20200217_DUWB = "did_unlock_with_biometrics";
    public static final String _20200223_DD = "did_donate";
    public static final String _20200225_DCD = "did_cancel_donate";
    public static final String _20200301_DUDFPK = "did_use_default_fee_per_kb";
    public static final String _20201121_SIL = "started_IFPS_lookup";
    public static final String _20201121_DRIA = "did_resolve_IPFS_address";
    public static final String _20201121_FRIA = "failed_resolve_IPFS_address";
    public static final String _20230113_BAC = "backup_apiserver_called";

    ///Dev: These events not yet used
    public static final String _20200207_DTHB = "did_tap_header_balance";
    public static final String _20210405_TAWDF = "ternio_api_wallet_details_failure";
    public static final String _20210804_TAA2FAC = "ternio_API_auth_2FA_change";
    public static final String _20210804_TAWDS = "ternio_API_wallet_details_success";
    public static final String _20210804_TAULI = "ternio_API_user_log_in";
    public static final String _20210804_TAULO = "ternio_API_user_log_out";
    public static final String _20210427_HCIEEH = "heartbeat_check_if_event_even_happens";
    public static final String _20220822_UTOU = "user_tapped_on_ud";
    ///Dev: These events not yet used

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            _20191105_AL,
            _20191105_VSC,
            _20202116_VRC,
            _20191105_DSL,
            _20191105_DTBT,
            _20200111_RNI,
            _20200111_FNI,
            _20200111_TNI,
            _20200111_WNI,
            _20200111_PNI,
            _20200111_UTST,
            _20200112_ERR,
            _20200112_DSR,
            _20200125_DSRR,
            _20201118_DTGS,
            _20200217_DUWP,
            _20200217_DUWB,
            _20200223_DD,
            _20200225_DCD,
            _20200301_DUDFPK,
            _20201121_SIL,
            _20201121_DRIA,
            _20201121_FRIA,
            _20230113_BAC,
            _20200207_DTHB,
            _20210405_TAWDF,
            _20210804_TAA2FAC,
            _20210804_TAWDS,
            _20210804_TAULI,
            _20210804_TAULO,
            _20210427_HCIEEH,
            _20220822_UTOU
    })
    public @interface Event {
    }


    /**
     * Analytics keys
     */

    public static final String START_TIME = "start_time";
    public static final String SUCCESS_TIME = "success_time";
    public static final String FAILURE_TIME = "failure_time";
    public static final String ERROR = "error";
}
