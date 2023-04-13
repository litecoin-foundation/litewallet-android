package com.breadwallet.tools.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRPeerManager;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SyncManager {
    private static SyncManager instance;
    private static final long SYNC_PERIOD = TimeUnit.HOURS.toMillis(24);
    private static SyncProgressTask syncTask;
    public boolean running;

    public static SyncManager getInstance() {
        if (instance == null) instance = new SyncManager();
        return instance;
    }

    private SyncManager() {
    }

    private void createAlarm(Context app, long time) {
        //Add another flag
        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(app, SyncReceiver.class);
        intent.setAction(SyncReceiver.SYNC_RECEIVER);//my custom string action name
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getService(app, 1001, intent, flags);
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, time, time + TimeUnit.MINUTES.toMillis(1), pendingIntent);//first start will start asap
    }

    public synchronized void updateAlarms(Context app) {
        createAlarm(app, System.currentTimeMillis() + SYNC_PERIOD);
    }

    public synchronized void startSyncingProgressThread() {
        Timber.d("timber: startSyncingProgressThread:%s", Thread.currentThread().getName());

        try {
            if (syncTask != null) {
                if (running) {
                    Timber.d("timber: startSyncingProgressThread: syncTask.running == true, returning");
                    return;
                }
                syncTask.interrupt();
                syncTask = null;
            }
            syncTask = new SyncProgressTask();
            syncTask.start();
        } catch (IllegalThreadStateException ex) {
            Timber.e(ex);
        }
    }

    public synchronized void stopSyncingProgressThread() {
        Timber.d("timber: stopSyncingProgressThread");
        final BreadActivity ctx = BreadActivity.getApp();
        if (ctx == null) {
            Timber.i("timber: stopSyncingProgressThread: ctx is null");
            return;
        }
        try {
            if (syncTask != null) {
                syncTask.interrupt();
                syncTask = null;
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    private class SyncProgressTask extends Thread {
        public double progressStatus = 0;
        private BreadActivity app;

        public SyncProgressTask() {
            progressStatus = 0;
        }

        @Override
        public void run() {
            if (running) return;
            try {
                app = BreadActivity.getApp();
                progressStatus = 0;
                running = true;
                Timber.d("timber: run: starting: %s", progressStatus);

                if (app != null) {
                    final long lastBlockTimeStamp = BRPeerManager.getInstance().getLastBlockTimestamp() * 1000;
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (TxManager.getInstance().syncingHolder != null)
                                TxManager.getInstance().syncingHolder.progress.setProgress((int) (progressStatus * 100));
                            if (TxManager.getInstance().syncingHolder != null)
                                TxManager.getInstance().syncingHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                        }
                    });
                }

                while (running) {
                    if (app != null) {
                        int startHeight = BRSharedPrefs.getStartHeight(app);
                        progressStatus = BRPeerManager.syncProgress(startHeight);
                        if (progressStatus == 1) {
                            running = false;
                            continue;
                        }
                        final long lastBlockTimeStamp = BRPeerManager.getInstance().getLastBlockTimestamp() * 1000;
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (TxManager.getInstance().currentPrompt != PromptManager.PromptItem.SYNCING) {
                                    Timber.d("timber: run: currentPrompt != SYNCING, showPrompt(SYNCING) ....");
                                    TxManager.getInstance().showPrompt(app, PromptManager.PromptItem.SYNCING);
                                }

                                if (TxManager.getInstance().syncingHolder != null)
                                    TxManager.getInstance().syncingHolder.progress.setProgress((int) (progressStatus * 100));
                                if (TxManager.getInstance().syncingHolder != null)
                                    TxManager.getInstance().syncingHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                            }
                        });

                    } else {
                        app = BreadActivity.getApp();
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Timber.e(e, "timber:run: Thread.sleep was Interrupted:%s", Thread.currentThread().getName());
                    }
                }
                Timber.d("timber: run: SyncProgress task finished:%s", Thread.currentThread().getName());
            } finally {
                running = false;
                progressStatus = 0;
                if (app != null)
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TxManager.getInstance().hidePrompt(app, PromptManager.PromptItem.SYNCING);
                        }
                    });
            }
        }
    }
}
