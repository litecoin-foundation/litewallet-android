package com.breadwallet.wallet;

import static java.lang.Math.abs;

import android.content.Context;

import androidx.annotation.WorkerThread;

import android.os.Bundle;
import android.util.Log;

import com.breadwallet.BreadApp;
import com.breadwallet.presenter.entities.BlockEntity;
import com.breadwallet.presenter.entities.PeerEntity;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.SyncManager;
import com.breadwallet.tools.sqlite.MerkleBlockDataSource;
import com.breadwallet.tools.sqlite.PeerDataSource;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.TrustedNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class BRPeerManager {
    private static BRPeerManager instance;

    private static final List<OnTxStatusUpdate> statusUpdateListeners = new ArrayList<>();
    private static OnSyncSucceeded onSyncFinished;

    static long syncStartDate = new Date().getTime();
    static long syncCompletedDate = new Date().getTime();

    private BRPeerManager() {
    }

    public static BRPeerManager getInstance() {
        if (instance == null) {
            instance = new BRPeerManager();
        }
        return instance;
    }

    /**
     * void BRPeerManagerSetCallbacks(BRPeerManager *manager, void *info,
     * void (*syncStarted)(void *info),
     * void (*syncSucceeded)(void *info),
     * void (*syncFailed)(void *info, BRPeerManagerError error),
     * void (*txStatusUpdate)(void *info),
     * void (*saveBlocks)(void *info, const BRMerkleBlock blocks[], size_t count),
     * void (*savePeers)(void *info, const BRPeer peers[], size_t count),
     * int (*networkIsReachable)(void *info))
     */

    public static void syncStarted() {
        Context ctx = BreadApp.getBreadContext();
        int startHeight = BRSharedPrefs.getStartHeight(ctx);
        int lastHeight = BRSharedPrefs.getLastBlockHeight(ctx);
        if (startHeight > lastHeight) BRSharedPrefs.putStartHeight(ctx, lastHeight);
        SyncManager.getInstance().startSyncingProgressThread(ctx);
    }

    public static void syncSucceeded() {
        Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRSharedPrefs.putLastSyncTimestamp(ctx, System.currentTimeMillis());
        SyncManager.getInstance().updateAlarms(ctx);
        BRSharedPrefs.putAllowSpend(ctx, true);
        SyncManager.getInstance().stopSyncingProgressThread(ctx);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRSharedPrefs.putStartHeight(ctx, getCurrentBlockHeight());
            }
        });
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }

    public static void syncFailed() {
        Context ctx = BreadApp.getBreadContext();
        SyncManager.getInstance().stopSyncingProgressThread(ctx);

        if (ctx == null) return;
        Timber.d("timber: Network Not Available, showing not connected bar");

        SyncManager.getInstance().stopSyncingProgressThread(ctx);
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }

    public static void txStatusUpdate() {
        Timber.d("timber: txStatusUpdate");

        for (OnTxStatusUpdate listener : statusUpdateListeners) {
            if (listener != null) listener.onStatusUpdate();
        }
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateLastBlockHeight(getCurrentBlockHeight());
            }
        });
    }

    public static void saveBlocks(final BlockEntity[] blockEntities, final boolean replace) {
        Timber.d("timber: saveBlocks: %s", blockEntities.length);

        final Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (replace) MerkleBlockDataSource.getInstance(ctx).deleteAllBlocks();
                MerkleBlockDataSource.getInstance(ctx).putMerkleBlocks(blockEntities);
            }
        });

    }

    public static void savePeers(final PeerEntity[] peerEntities, final boolean replace) {
        Timber.d("timber: savePeers: %s", peerEntities.length);
        final Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (replace) PeerDataSource.getInstance(ctx).deleteAllPeers();
                PeerDataSource.getInstance(ctx).putPeers(peerEntities);
            }
        });
    }

    public static boolean networkIsReachable() {
        Timber.d("timber: networkIsReachable");
        return BRWalletManager.getInstance().isNetworkAvailable(BreadApp.getBreadContext());
    }

    public static void deleteBlocks() {
        Timber.d("timber: deleteBlocks");
        final Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                MerkleBlockDataSource.getInstance(ctx).deleteAllBlocks();
            }
        });
    }

    public static void deletePeers() {
        Timber.d("timber: deletePeers");
        final Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                PeerDataSource.getInstance(ctx).deleteAllPeers();
            }
        });
    }

    public void updateFixedPeer(Context ctx) {
        String node = BRSharedPrefs.getTrustNode(ctx);
        String host = TrustedNode.getNodeHost(node);
        int port = TrustedNode.getNodePort(node);
        boolean success = setFixedPeer(host, port);
        if (!success) {
            Timber.i("timber: updateFixedPeer: Failed to updateFixedPeer with input: %s", node);
        } else {
            Timber.d("timber: updateFixedPeer: succeeded");
        }
        connect();
    }

    public void networkChanged(boolean isOnline) {
        if (isOnline)
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    BRPeerManager.getInstance().connect();
                }
            });
    }

    public void addStatusUpdateListener(OnTxStatusUpdate listener) {
        if (statusUpdateListeners.contains(listener)) return;
        statusUpdateListeners.add(listener);
    }

    public void removeListener(OnTxStatusUpdate listener) {
        statusUpdateListeners.remove(listener);
    }

    public static void setOnSyncFinished(OnSyncSucceeded listener) {
        onSyncFinished = listener;
    }

    public interface OnTxStatusUpdate {
        void onStatusUpdate();
    }

    public interface OnSyncSucceeded {
        void onFinished();
    }

    public static void updateLastBlockHeight(int blockHeight) {
        final Context ctx = BreadApp.getBreadContext();
        if (ctx == null) return;
        BRSharedPrefs.putLastBlockHeight(ctx, blockHeight);
    }

    public native String getCurrentPeerName();

    public native void create(int earliestKeyTime, int blockCount, int peerCount, double fpRate);

    public native void connect();

    public native void putPeer(byte[] peerAddress, byte[] peerPort, byte[] peerTimeStamp);

    public native void createPeerArrayWithCount(int count);

    public native void putBlock(byte[] block, int blockHeight);

    public native void createBlockArrayWithCount(int count);

    public native static double syncProgress(int startHeight);

    public native static int getCurrentBlockHeight();

    public native static int getRelayCount(byte[] hash);

    public native boolean setFixedPeer(String node, int port);

    public native static int getEstimatedBlockHeight();

    public native boolean isCreated();

    public native boolean isConnected();

    public native void peerManagerFreeEverything();

    public native long getLastBlockTimestamp();

    public native void rescan();
}