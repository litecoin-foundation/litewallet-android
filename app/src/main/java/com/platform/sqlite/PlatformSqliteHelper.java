package com.platform.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.breadwallet.tools.util.BRConstants;

import timber.log.Timber;

public class PlatformSqliteHelper extends SQLiteOpenHelper {
    private static PlatformSqliteHelper instance;

    public static final String DATABASE_NAME = "platform.db";
    private static final int DATABASE_VERSION = 3;

    public static synchronized PlatformSqliteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (instance == null) {
            instance = new PlatformSqliteHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * KV Store table
     */

    public static final String KV_STORE_TABLE_NAME = "kvStoreTable";
    public static final String KV_VERSION = "version";
    public static final String KV_REMOTE_VERSION = "remote_version";
    public static final String KV_KEY = "key";
    public static final String KV_VALUE = "value";
    public static final String KV_TIME = "thetime";
    public static final String KV_DELETED = "deleted";

    private static final String KV_DATABASE_CREATE = String.format("CREATE TABLE IF NOT EXISTS %s(" +
            "   %s         INTEGER  NOT NULL, " +
            "   %s  INTEGER  NOT NULL DEFAULT 0, " +
            "   %s             TEXT    NOT NULL, " +
            "   %s           BLOB    NOT NULL, " +
            "   %s         INTEGER  NOT NULL, " + // server unix timestamp in MS
            "   %s         INTEGER    NOT NULL, " +
            "   PRIMARY KEY (%s, %s) " +
            ");", KV_STORE_TABLE_NAME, KV_VERSION, KV_REMOTE_VERSION, KV_KEY, KV_VALUE, KV_TIME, KV_DELETED, KV_KEY, KV_VERSION);

    private PlatformSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(BRConstants.WAL);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Timber.d("timber: onCreate: %s", KV_DATABASE_CREATE);
        database.execSQL(KV_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("timber: Upgrading database from version %d to %d, which will destroy all old data", oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + KV_STORE_TABLE_NAME);
        //recreate the dbs
        onCreate(db);
    }
}
