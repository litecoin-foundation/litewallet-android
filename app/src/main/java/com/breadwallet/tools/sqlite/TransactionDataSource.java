package com.breadwallet.tools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;

import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.entities.BRTransactionEntity;
import com.breadwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class TransactionDataSource implements BRDataSourceInterface {
    private static final String TAG = TransactionDataSource.class.getName();
    private AtomicInteger mOpenCounter = new AtomicInteger();

    // Database fields
    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            BRSQLiteHelper.TX_COLUMN_ID,
            BRSQLiteHelper.TX_BUFF,
            BRSQLiteHelper.TX_BLOCK_HEIGHT,
            BRSQLiteHelper.TX_TIME_STAMP
    };

    public interface OnTxAddedListener {
        void onTxAdded();
    }

    List<OnTxAddedListener> listeners = new ArrayList<>();

    public void addTxAddedListener(OnTxAddedListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(OnTxAddedListener listener) {
        listeners.remove(listener);

    }

    private static TransactionDataSource instance;

    public static TransactionDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionDataSource(context);
        }
        return instance;
    }


    private TransactionDataSource(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    public BRTransactionEntity putTransaction(BRTransactionEntity transactionEntity) {
        Cursor cursor = null;
        try {
            database = openDatabase();
            ContentValues values = new ContentValues();
            values.put(BRSQLiteHelper.TX_COLUMN_ID, transactionEntity.getTxHash());
            values.put(BRSQLiteHelper.TX_BUFF, transactionEntity.getBuff());
            values.put(BRSQLiteHelper.TX_BLOCK_HEIGHT, transactionEntity.getBlockheight());
            values.put(BRSQLiteHelper.TX_TIME_STAMP, transactionEntity.getTimestamp());

            database.beginTransaction();
            database.insert(BRSQLiteHelper.TX_TABLE_NAME, null, values);
            cursor = database.query(BRSQLiteHelper.TX_TABLE_NAME,
                    allColumns, null, null, null, null, null);
            cursor.moveToFirst();
            BRTransactionEntity transactionEntity1 = cursorToTransaction(cursor);

            database.setTransactionSuccessful();
            for (OnTxAddedListener listener : listeners) {
                if (listener != null) listener.onTxAdded();
            }
            return transactionEntity1;
        } catch (Exception ex) {
            Timber.e(ex, "Error inserting into SQLite");
        } finally {
            database.endTransaction();
            closeDatabase();
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public void deleteAllTransactions() {
        try {
            database = openDatabase();
            database.delete(BRSQLiteHelper.TX_TABLE_NAME, null, null);
        } finally {
            closeDatabase();
        }
    }

    public List<BRTransactionEntity> getAllTransactions() {
        List<BRTransactionEntity> transactions = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.TX_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BRTransactionEntity transactionEntity = cursorToTransaction(cursor);
                transactions.add(transactionEntity);
                cursor.moveToNext();
            }
        } finally {
            closeDatabase();
            if (cursor != null)
                cursor.close();
        }
        return transactions;
    }

    private BRTransactionEntity cursorToTransaction(Cursor cursor) {
        return new BRTransactionEntity(cursor.getBlob(1), cursor.getInt(2), cursor.getLong(3), cursor.getString(0));
    }

    public void updateTxBlockHeight(String hash, int blockHeight, int timeStamp) {
        try {
            database = openDatabase();
            Timber.d("timber: transaction updated with id: %s", hash);
            String strFilter = "_id=\'" + hash + "\'";
            ContentValues args = new ContentValues();
            args.put(BRSQLiteHelper.TX_BLOCK_HEIGHT, blockHeight);
            args.put(BRSQLiteHelper.TX_TIME_STAMP, timeStamp);

            database.update(BRSQLiteHelper.TX_TABLE_NAME, args, strFilter, null);
        } finally {
            closeDatabase();
        }
    }

    public void deleteTxByHash(String hash) {
        try {
            database = openDatabase();
            Timber.d("timber: transaction deleted with id: %s", hash);
            database.delete(BRSQLiteHelper.TX_TABLE_NAME, BRSQLiteHelper.TX_COLUMN_ID
                    + " = \'" + hash + "\'", null);
        } finally {
            closeDatabase();
        }
    }

    @Override
    public SQLiteDatabase openDatabase() {
        if (ActivityUTILS.isMainThread()) throw new NetworkOnMainThreadException();
        // Opening new database
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
        return database;
    }

    @Override
    public void closeDatabase() {
//        if (mOpenCounter.decrementAndGet() == 0) {
//            // Closing database
//            database.close();

//        }
//        Log.d("Database open counter: " , String.valueOf(mOpenCounter.get()));
    }
}