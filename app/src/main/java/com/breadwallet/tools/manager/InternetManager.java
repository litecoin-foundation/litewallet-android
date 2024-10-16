package com.breadwallet.tools.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.breadwallet.wallet.BRPeerManager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class InternetManager extends BroadcastReceiver {

    public static List<ConnectionReceiverListener> connectionReceiverListeners;

    private static InternetManager instance;

    private InternetManager() {
        connectionReceiverListeners = new ArrayList<>();
    }

    public static InternetManager getInstance() {
        if (instance == null) {
            instance = new InternetManager();
        }
        return instance;
    }

    public static void addConnectionListener(ConnectionReceiverListener listener) {
        if (!connectionReceiverListeners.contains(listener))
            connectionReceiverListeners.add(listener);
    }
    @Override
    public void onReceive(final Context context, final Intent intent) {
        boolean connected = false;
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                connected = true;
                BRPeerManager.getInstance().networkChanged(true);
            } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                BRPeerManager.getInstance().networkChanged(false);
                connected = false;
            }

            for (ConnectionReceiverListener listener : connectionReceiverListeners) {
                listener.onConnectionChanged(connected);
            }
            Timber.d("timber: onReceive: %s", connected);
        }
    }

    public boolean isConnected(Context app) {
        if (app == null) return false;
        ConnectivityManager cm = (ConnectivityManager) app.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public interface ConnectionReceiverListener {
        void onConnectionChanged(boolean isConnected);
    }
}