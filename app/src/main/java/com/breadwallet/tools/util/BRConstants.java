package com.breadwallet.tools.util;

import androidx.annotation.StringDef;
import com.breadwallet.BuildConfig;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.RoundingMode;
import java.util.Locale;

public class BRConstants {

    private BRConstants() {}

    /**
     * App Version and Version Code
     */

    public static final String APP_VERSION_NAME_CODE = String.format(
        Locale.US,
        "%1$s (%2$s)",
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE
    );

    /**
     * Native library name
     */
    public static final String NATIVE_LIB_NAME = "core-lib";

    /**
     * Permissions
     */
    public static final int CAMERA_REQUEST_ID = 34;
    public static final int GEO_REQUEST_ID = 35;

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
     * Request codes for taking pictures
     */
    public static final int SCANNER_REQUEST = 201;

    public static final String CANARY_STRING = "canary";
    public static final String FIRST_ADDRESS = "firstAddress";
    public static final String SECURE_TIME_PREFS = "secureTime";
    public static final String FEE_KB_PREFS = "feeKb";
    public static final String ECONOMY_FEE_KB_PREFS = "EconomyFeeKb";

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
    public static final String GEO_PERMISSIONS_REQUESTED =
        "geoPermissionsRequested";

    /**
     * Currency units
     */
    public static final int CURRENT_UNIT_PHOTONS = 0; // formerly CURRENT_UNIT_BITS
    public static final int CURRENT_UNIT_LITES = 1; // formerly CURRENT_UNIT_MBITS
    public static final int CURRENT_UNIT_LITECOINS = 2;

    public static final String litecoinLowercase = "\u0142";
    public static final String litecoinUppercase = "\u0141";

    public static boolean PLATFORM_ON = true;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final boolean WAL = true;

    public static final String loopBug = "android-loop-bug";

    /**
     * App External URLs
     */

    public static final String TWITTER_LINK =
        "https://twitter.com/ltcfoundation";
    public static final String INSTAGRAM_LINK =
        "https://www.instagram.com/ltcfoundation";
    public static final String WEB_LINK = "https://litecoin.com";
    public static final String TOS_LINK = "https://litewallet.io/privacy";
    public static String CUSTOMER_SUPPORT_LINK =
        "https://chat-mobile.litecoin.com/widget?website_token=1kCbkQay5t4CyvyP9JsrkJWh";
    public static String BITREFILL_AFFILIATE_LINK =
        "https://www.bitrefill.com/";

    /**
     * API Hosts
     */
    public static final String LW_API_HOST = "https://api.loafwallet.org";
    public static final String LW_BACKUP_API_HOST =
        "https://api.loafwallet.org";

    public static final String BLOCK_EXPLORER_BASE_URL =
        BuildConfig.LITECOIN_TESTNET
            ? "https://chain.so/tx/LTCTEST/"
            : "https://blockchair.com/litecoin/transaction/";

    public @interface Event {
    }

    /**
     * Analytics keys
     */

    public static final String START_TIME = "start_time";
    public static final String SUCCESS_TIME = "success_time";
    public static final String FAILURE_TIME = "failure_time";
    public static final String ERROR = "error";

    /**
     * False Positive rate keys
     */
    public static final float FALSE_POS_RATE_LOW_PRIVACY = 0.00005F;
    public static final float FALSE_POS_RATE_SEMI_PRIVACY = 0.00008F;
    public static final float FALSE_POS_RATE_ANONYMOUS = 0.0005F;
}
