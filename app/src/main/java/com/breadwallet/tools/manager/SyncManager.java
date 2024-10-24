package com.breadwallet.tools.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.breadwallet.R;
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

    public synchronized void startSyncingProgressThread(Context app) {
        try {
            if (syncTask != null) {
                if (running) {
                    updateStartSyncData(app);
                    return;
                }
                syncTask.interrupt();
                syncTask = null;
            }
            syncTask = new SyncProgressTask();
            syncTask.start();
            BRSharedPrefs.putStartSyncTimestamp(app, System.currentTimeMillis());
            BRSharedPrefs.putSyncTimeElapsed(app, 0L);
            updateStartSyncData(app);
        } catch (IllegalThreadStateException ex) {
            Timber.e(ex);
        }
    }

    private synchronized void updateStartSyncData(Context app) {
        final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app));
        Timber.d("timber: || progress: %s syncingProgressThread: %s, running",String.format( "%.2f", progress * 100.00),Thread.currentThread().getName());


        long startSync = BRSharedPrefs.getStartSyncTimestamp(app);
        long lastSync = BRSharedPrefs.getLastSyncTimestamp(app);
        long elapsed = BRSharedPrefs.getSyncTimeElapsed(app);

        if (elapsed > 0L) {
            elapsed = (System.currentTimeMillis() - lastSync) + elapsed;
        }
        else {
            elapsed = 1L;
        }
        BRSharedPrefs.putLastSyncTimestamp(app, System.currentTimeMillis());
        BRSharedPrefs.putSyncTimeElapsed(app, elapsed);
        String minutes = String.valueOf((double) elapsed / 1_000.0  / 60.0);
        Timber.d("timber: ||\nrunning lastSyncingTime: %s\nelapsed (msecs|mins): %s | %s", String.valueOf(BRSharedPrefs.getLastSyncTimestamp(app)), String.valueOf(elapsed), minutes);

    }

    private synchronized void markFinishedSyncData(Context app) {

    }

    public synchronized void stopSyncingProgressThread(Context app) {
        Timber.d("timber: || stopSyncingProgressThread:%s", Thread.currentThread().getName());

        if (app == null) {
            Timber.i("timber: || stopSyncingProgressThread: ctx is null");
            markFinishedSyncData(app);
            return;
        }
        try {
            if (syncTask != null) {
                syncTask.interrupt();
                syncTask = null;
                markFinishedSyncData(app);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
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
                            if (TxManager.getInstance().syncingHolder != null) {
                                TxManager.getInstance().syncingHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                TxManager.getInstance().syncingHolder.label.setText(BreadActivity.getApp().getString(R.string.SyncingView_header));
                            }
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
                                if (TxManager.getInstance().syncingHolder != null) {
                                    TxManager.getInstance().syncingHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                    TxManager.getInstance().syncingHolder.label.setText(BreadActivity.getApp().getString(R.string.SyncingView_header));
                                }

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
