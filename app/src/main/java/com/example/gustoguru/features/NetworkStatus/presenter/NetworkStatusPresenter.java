package com.example.gustoguru.features.NetworkStatus.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.example.gustoguru.features.NetworkStatus.view.NetworkStatusView;
import com.example.gustoguru.model.network.NetworkUtil;

public class NetworkStatusPresenter {
    private final Context context;
    private NetworkStatusView view;
    private BroadcastReceiver networkReceiver;

    public NetworkStatusPresenter(Context context, NetworkStatusView view) {
        this.context = context;
        this.view = view;
    }

    public void checkInitialStatus() {
        boolean isConnected = NetworkUtil.isNetworkAvailable(context);
        view.updateNetworkStatus(isConnected);
    }

    public void registerNetworkReceiver() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = NetworkUtil.isNetworkAvailable(context);
                view.updateNetworkStatus(isConnected);
            }
        };

        context.registerReceiver(
                networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    public void unregisterReceiver() {
        if (networkReceiver != null) {
            context.unregisterReceiver(networkReceiver);
        }
    }


}