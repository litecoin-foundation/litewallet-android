package com.breadwallet.tools.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

import timber.log.Timber;

public class BRSQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = BRSQLiteHelper.class.getName();
    private static BRSQLiteHelper instance;

    private BRSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static BRSQLiteHelper getInstance(Context context) {
        // Use the application context to ensure that we don't accidentally leak an Activity's context
        if (instance == null) instance = new BRSQLiteHelper(context.getApplicationContext());
        return instance;
    }

    private static final String DATABASE_NAME = "loafwallet.db";
    private static final int DATABASE_VERSION = 13;

    /**
     * MerkleBlock table
     */
    public static final String MB_TABLE_NAME = "merkleBlockTable";
    public static final String MB_BUFF = "merkleBlockBuff";
    public static final String MB_HEIGHT = "merkleBlockHeight";

    public static final String MB_COLUMN_ID = "_id";

    private static final String MB_DATABASE_CREATE = "create table if not exists " + MB_TABLE_NAME + "(" +
            MB_COLUMN_ID + " integer primary key autoincrement, " +
            MB_BUFF + " blob, " +
            MB_HEIGHT + " integer);";

    /**
     * Transaction table
     */

    public static final String TX_TABLE_NAME = "transactionTable";
    public static final String TX_COLUMN_ID = "_id";
    public static final String TX_BUFF = "transactionBuff";
    public static final String TX_BLOCK_HEIGHT = "transactionBlockHeight";
    public static final String TX_TIME_STAMP = "transactionTimeStamp";

    private static final String TX_DATABASE_CREATE = "create table if not exists " + TX_TABLE_NAME + "(" +
            TX_COLUMN_ID + " text, " +
            TX_BUFF + " blob, " +
            TX_BLOCK_HEIGHT + " integer, " +
            TX_TIME_STAMP + " integer );";

    /**
     * Peer table
     */

    public static final String PEER_TABLE_NAME = "peerTable";
    public static final String PEER_COLUMN_ID = "_id";
    public static final String PEER_ADDRESS = "peerAddress";
    public static final String PEER_PORT = "peerPort";
    public static final String PEER_TIMESTAMP = "peerTimestamp";

    private static final String PEER_DATABASE_CREATE = "create table if not exists " + PEER_TABLE_NAME + "(" +
            PEER_COLUMN_ID + " integer primary key autoincrement, " +
            PEER_ADDRESS + " blob," +
            PEER_PORT + " blob," +
            PEER_TIMESTAMP + " blob );";
    /**
     * Currency table
     */

    public static final String CURRENCY_TABLE_NAME = "currencyTable";
    public static final String CURRENCY_CODE = "code";
    public static final String CURRENCY_NAME = "name";
    public static final String CURRENCY_RATE = "rate";

    private static final String CURRENCY_DATABASE_CREATE = "create table if not exists " + CURRENCY_TABLE_NAME + "(" +
            CURRENCY_CODE + " text primary key," +
            CURRENCY_NAME + " text," +
            CURRENCY_RATE + " integer );";
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(MB_DATABASE_CREATE);
        database.execSQL(TX_DATABASE_CREATE);
        database.execSQL(PEER_DATABASE_CREATE);
        database.execSQL(CURRENCY_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Timber.e("timber: Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Clear DB tables to enable Bech32 features
        if (oldVersion == 12 && newVersion == 13) {
            Timber.e("timber: Delete start %s", new Date().toString());
            db.execSQL("DELETE FROM " + MB_TABLE_NAME);
            db.execSQL("DELETE FROM " + TX_TABLE_NAME);
            db.execSQL("DELETE FROM " + PEER_TABLE_NAME);
            db.execSQL("DELETE FROM " + CURRENCY_TABLE_NAME);
            Timber.e("timber: Delete Finish %s", new Date().toString());

        }
    }
}
