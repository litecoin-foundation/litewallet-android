package com.breadwallet.tools.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.tools.listeners.SyncReceiver;
import com.breadwallet.tools.util.BRConstants;
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
        double minutesValue = ((double) elapsed / 1_000.0  / 60.0);
        String minutesString = String.format( "%3.2f mins", minutesValue);
        String millisecString = String.format( "%5d msec", elapsed);
        Timber.d("timber: ||\nprogress: %s\nThread: %s\nrunning lastSyncingTime: %s\nelapsed: %s | %s", String.format( "%.2f", progress * 100.00),Thread.currentThread().getName(),String.valueOf(BRSharedPrefs.getLastSyncTimestamp(app)), millisecString, minutesString);

    }

    private synchronized void markFinishedSyncData(Context app) {
        Timber.d("timber: || markFinish threadname:%s", Thread.currentThread().getName());
        final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app));
        long startSync = BRSharedPrefs.getStartSyncTimestamp(app);
        long lastSync = BRSharedPrefs.getLastSyncTimestamp(app);
        long elapsed = BRSharedPrefs.getSyncTimeElapsed(app);
        double minutesValue = ((double) elapsed / 1_000.0  / 60.0);
        String minutesString = String.format( "%3.2f mins", minutesValue);
        String millisecString = String.format( "%5d msec", elapsed);
        Timber.d("timber: ||\ncompletedprogress: %s\nstartSyncTime: %s\nlastSyncingTime: %s\ntotalTimeelapsed: %s | %s", String.format( "%.2f", progress * 100.00),String.valueOf(startSync),String.valueOf(lastSync), millisecString, minutesString);

        Bundle params = new Bundle();
        params.putDouble("sync_time_elapsed", minutesValue);
        params.putLong("sync_start_timestamp", startSync);
        params.putLong("sync_last_timestamp", lastSync);
        AnalyticsManager.logCustomEventWithParams(BRConstants._20230407_DCS, params);
    }

    public synchronized void stopSyncingProgressThread(Context app) {

        if (app == null) {
            Timber.i("timber: || stopSyncingProgressThread: ctx is null");
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
                            if (TxManager.getInstance().syncingProgressViewHolder != null)
                                TxManager.getInstance().syncingProgressViewHolder.progress.setProgress((int) (progressStatus * 100));
                            if (TxManager.getInstance().syncingProgressViewHolder != null) {
                                TxManager.getInstance().syncingProgressViewHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                TxManager.getInstance().syncingProgressViewHolder.label.setText(BreadActivity.getApp().getString(R.string.SyncingView_header));
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
                        final int currentBlockHeight = BRPeerManager.getCurrentBlockHeight();
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (TxManager.getInstance().currentPrompt != PromptManager.PromptItem.SYNCING) {
                                    Timber.d("timber: run: currentPrompt != SYNCING, showPrompt(SYNCING) ....");
                                    TxManager.getInstance().showPrompt(app, PromptManager.PromptItem.SYNCING);
                                }
                                if (TxManager.getInstance().syncingProgressViewHolder != null) {
                                    TxManager.getInstance().syncingProgressViewHolder.progress.setProgress((int) (progressStatus * 100));
                                    TxManager.getInstance().syncingProgressViewHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                    String progressString = String.format("%3.2f%%", progressStatus * 100);
                                    TxManager.getInstance().syncingProgressViewHolder.label.setText(String.format("%s %s - %d",BreadActivity.getApp().getString(R.string.SyncingView_header),progressString, currentBlockHeight));
                                }
                            }
                        });

                    } else {
                        app = BreadActivity.getApp();
                    }

                    ///DEV kcw-grunt 26-10-24
                    /// DUMB sleep was slowing sync dramatically
                    /// Why is this here?
                    /// Reduced it from 500msec to 100msec until refactor
                    /// Poor control flow, loop should continue for the next task
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Timber.e(e, "timber:run: SyncManager.run Thread.sleep was Interrupted:%s", Thread.currentThread().getName());
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
