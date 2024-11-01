package com.platform.tools;

import android.content.Context;

import com.breadwallet.tools.crypto.CryptoHelper;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.util.BRCompressor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;
import com.platform.APIClient;
import com.platform.entities.TxMetaData;
import com.platform.entities.WalletInfo;
import com.platform.kvstore.CompletionObject;
import com.platform.kvstore.RemoteKVStore;
import com.platform.kvstore.ReplicatedKVStore;
import com.platform.sqlite.KVItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KVStoreManager {
    private static KVStoreManager instance;
    String walletInfoKey = "wallet-info";

    private KVStoreManager() {
    }

    public static KVStoreManager getInstance() {
        if (instance == null) instance = new KVStoreManager();
        return instance;
    }

    public WalletInfo getWalletInfo(Context app) {
        Timber.i("timber: Never initialized always null until WalletInfo struct to be removed");
        return null;
    }

    public TxMetaData getTxMetaData(Context app, byte[] txHash) {
        String key = txKey(txHash);

        RemoteKVStore remoteKVStore = RemoteKVStore.getInstance(APIClient.getInstance(app));
        ReplicatedKVStore kvStore = ReplicatedKVStore.getInstance(app, remoteKVStore);
        long ver = kvStore.localVersion(key).version;
        // DEV Uncomment to see values
        // Timber.d("timber: remoteKVStore: %s",remoteKVStore.toString());
        // Timber.d("timber: ReplicatedKVStore: %s",kvStore.toString());
        // Timber.d("timber: ver: %s",ver);

        CompletionObject obj = kvStore.get(key, ver);

        if (obj.kv == null) {
            return null;
        }

        return valueToMetaData(obj.kv.value);
    }

    public Map<String, TxMetaData> getAllTxMD(Context app) {
        Map<String, TxMetaData> mds = new HashMap<>();
        RemoteKVStore remoteKVStore = RemoteKVStore.getInstance(APIClient.getInstance(app));
        ReplicatedKVStore kvStore = ReplicatedKVStore.getInstance(app, remoteKVStore);
        List<KVItem> list = kvStore.getAllTxMdKv();
        for (int i = 0; i < list.size(); i++) {
            TxMetaData md = valueToMetaData(list.get(i).value);
            if (md != null) mds.put(list.get(i).key, md);
        }

        return mds;
    }

    public TxMetaData valueToMetaData(byte[] value) {
        TxMetaData result = new TxMetaData();
        JSONObject json;
        if (value == null) {
            Timber.d("timber: valueToMetaData: value is null!");
            AnalyticsManager.logCustomEvent(BRConstants._20200111_TNI);
            return null;
        }
        try {
            byte[] decompressed = BRCompressor.bz2Extract(value);
            if (decompressed == null) {
                Timber.d("timber: getTxMetaData: decompressed value is null");
                return null;
            }
            json = new JSONObject(new String(decompressed));
        } catch (JSONException e) {
            Timber.e(e);
            return null;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }

        try {
            result.classVersion = json.getInt("classVersion");
            result.blockHeight = json.getInt("bh");
            result.exchangeRate = json.getDouble("er");
            result.exchangeCurrency = json.getString("erc");
            result.fee = json.getLong("fr");
            result.txSize = json.getInt("s");
            result.creationTime = json.getInt("c");
            result.deviceId = json.getString("dId");
            result.comment = json.getString("comment");
        } catch (JSONException e) {
            Timber.e(e);
        }
        return result;
    }

    public void putTxMetaData(Context app, TxMetaData data, byte[] txHash) {
        String key = txKey(txHash);
        TxMetaData old = getTxMetaData(app, txHash);

        boolean needsUpdate = false;
        if (old == null) {
            needsUpdate = true;
            old = data;
        } else if (data != null) {
            String finalExchangeCurrency = getFinalValue(data.exchangeCurrency, old.exchangeCurrency);
            if (finalExchangeCurrency != null) {
                Timber.d("timber: putTxMetaData: finalExchangeCurrency:%s", finalExchangeCurrency);
                old.exchangeCurrency = finalExchangeCurrency;
                needsUpdate = true;
            }
            String finalDeviceId = getFinalValue(data.deviceId, old.deviceId);
            if (finalDeviceId != null) {
                Timber.d("timber: putTxMetaData: finalDeviceId:%s", finalDeviceId);
                old.deviceId = finalDeviceId;
                needsUpdate = true;
            }
            String finalComment = getFinalValue(data.comment, old.comment);
            if (finalComment != null) {
                Timber.d("timber: putTxMetaData: comment:%s", finalComment);
                old.comment = finalComment;
                needsUpdate = true;
            }
            int finalClassVersion = getFinalValue(data.classVersion, old.classVersion);
            if (finalClassVersion != -1) {
                old.classVersion = finalClassVersion;
                needsUpdate = true;
            }
            int finalCreationTime = getFinalValue(data.creationTime, old.creationTime);
            if (finalCreationTime != -1) {
                old.creationTime = finalCreationTime;
                needsUpdate = true;
            }
            double finalExchangeRate = getFinalValue(data.exchangeRate, old.exchangeRate);
            if (finalExchangeRate != -1) {
                old.exchangeRate = finalExchangeRate;
                needsUpdate = true;
            }
            int finalBlockHeight = getFinalValue(data.blockHeight, old.blockHeight);
            if (finalBlockHeight != -1) {
                old.blockHeight = finalBlockHeight;
                needsUpdate = true;
            }
            int finalTxSize = getFinalValue(data.txSize, old.txSize);
            if (finalTxSize != -1) {
                old.txSize = finalTxSize;
                needsUpdate = true;
            }
            long finalFee = getFinalValue(data.fee, old.fee);
            if (finalFee != -1) {
                old.fee = finalFee;
                needsUpdate = true;
            }
        }

        if (!needsUpdate) return;

        Timber.d("timber: putTxMetaData: updating txMetadata for : %s", key);

        byte[] result;
        try {
            JSONObject obj = new JSONObject();
            obj.put("classVersion", old.classVersion);
            obj.put("bh", old.blockHeight);
            obj.put("er", old.exchangeRate);
            obj.put("erc", old.exchangeCurrency == null ? "" : old.exchangeCurrency);
            obj.put("fr", old.fee);
            obj.put("s", old.txSize);
            obj.put("c", old.creationTime);
            obj.put("dId", old.deviceId == null ? "" : old.deviceId);
            obj.put("comment", old.comment == null ? "" : old.comment);
            result = obj.toString().getBytes();
        } catch (JSONException e) {
            Timber.e(e);
            return;
        }

        if (result.length == 0) {
            Timber.d("timber: putTxMetaData: FAILED: result is empty");
            return;
        }
        byte[] compressed;
        try {
            compressed = BRCompressor.bz2Compress(result);
        } catch (IOException e) {
            Timber.e(e);
            return;
        }
        RemoteKVStore remoteKVStore = RemoteKVStore.getInstance(APIClient.getInstance(app));
        ReplicatedKVStore kvStore = ReplicatedKVStore.getInstance(app, remoteKVStore);
        long localVer = kvStore.localVersion(key).version;
        long removeVer = kvStore.remoteVersion(key);
        CompletionObject compObj = kvStore.set(localVer, removeVer, key, compressed, System.currentTimeMillis(), 0);
        if (compObj.err != null) {
            Timber.d("timber: putTxMetaData: Error setting value for key: " + key + ", err: " + compObj.err);
        }
    }

    //null means no change
    private String getFinalValue(String newVal, String oldVal) {
        if (newVal == null) return null;
        if (oldVal == null) return newVal;
        return newVal.equals(oldVal) ? null : newVal;
    }

    // -1 means no change
    private int getFinalValue(int newVal, int oldVal) {
        if (newVal <= 0) return -1;
        if (oldVal <= 0) return newVal;
        return newVal == oldVal ? -1 : newVal;
    }

    // -1 means no change
    private long getFinalValue(long newVal, long oldVal) {
        if (newVal <= 0) return -1;
        if (oldVal <= 0) return newVal;
        return newVal == oldVal ? -1 : newVal;
    }

    // -1 means no change
    private double getFinalValue(double newVal, double oldVal) {
        if (newVal <= 0) return -1;
        if (oldVal <= 0) return newVal;
        return newVal == oldVal ? -1 : newVal;
    }

    private static String txKey(byte[] txHash) {
        if (Utils.isNullOrEmpty(txHash)) return null;
        String hex = Utils.bytesToHex(CryptoHelper.sha256(txHash));
        return Utils.isNullOrEmpty(hex) ? null : "txn2-" + hex;
    }
}
