package com.example.mycar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

public class MyCarBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wdManager;
    private Channel wdChannel;
    private MyCarListener wdActivity;
	
    public MyCarBroadcastReceiver(WifiP2pManager manager, Channel channel, MyCarListener activity) {
        super();
        this.wdManager = manager;
        this.wdChannel = channel;
        this.wdActivity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct is enabled
            	Log.d(MyCarActivity.TAG, "WiFi Direct is enabled");
            	ActivityHelper.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi Direct is not enabled
            	Log.d(MyCarActivity.TAG, "WiFi Direct is disabled");
            	ActivityHelper.setIsWifiP2pEnabled(false);
            	wdActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (wdManager != null) {
                wdManager.requestPeers(wdChannel, (PeerListListener) wdActivity);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (wdManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                wdManager.requestConnectionInfo(wdChannel, wdActivity);
                MyCarListener.CONNECTED = true;
            } else {
                // It's a disconnect
                wdActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
