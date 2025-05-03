package com.example.gustoguru.features.NetworkStatus.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.gustoguru.R;
import com.example.gustoguru.model.network.NetworkUtil;

public class NetworkStatusFragment extends Fragment {

    private LinearLayout networkStatusContainer;
    private ImageView ivNetworkStatus;
    private TextView tvNetworkStatus;
    private BroadcastReceiver networkReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        updateNetworkStatus(NetworkUtil.isNetworkAvailable(requireContext()));
        registerNetworkReceiver();
    }

    private void initializeViews(View view) {
        networkStatusContainer = view.findViewById(R.id.networkStatusContainer);
        ivNetworkStatus = view.findViewById(R.id.ivNetworkStatus);
        tvNetworkStatus = view.findViewById(R.id.tvNetworkStatus);
    }

    private void registerNetworkReceiver() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = NetworkUtil.isNetworkAvailable(context);
                updateNetworkStatus(isConnected);
            }
        };

        requireContext().registerReceiver(
                networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    private void updateNetworkStatus(boolean isConnected) {
        requireActivity().runOnUiThread(() -> {
            if (isConnected) {
                networkStatusContainer.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.online_status_bg));
                ivNetworkStatus.setImageResource(R.drawable.ic_online);
                tvNetworkStatus.setText("Online - Fresh data");
            } else {
                networkStatusContainer.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.offline_status_bg));
                ivNetworkStatus.setImageResource(R.drawable.ic_offline);
                tvNetworkStatus.setText("Offline - Showing cached data");
            }
            networkStatusContainer.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkReceiver != null) {
            requireContext().unregisterReceiver(networkReceiver);
        }
    }
}
