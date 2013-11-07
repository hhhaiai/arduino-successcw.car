package com.example.mycar;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

public class ActivityHelper {
    public static void initialize(Activity activity) {

/*        int loadedOrientation = activity.getResources().getConfiguration().orientation;
        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (loadedOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (loadedOrientation == Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        activity.setRequestedOrientation(requestedOrientation);*/
    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
    public static void uninitialize(Activity activity) {

    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
	public static boolean getCurrentPeers() {
		if (MyCarListener.peers == null || MyCarListener.peerList == null){
			return false;
		}
		MyCarListener.peers.clear();
		MyCarListener.peers.addAll(MyCarListener.peerList.getDeviceList());
        return true;
	}
	
	public static void cancelDisconnect(WifiP2pManager wdManager, Channel wdChannel) {

		/*
		 * A cancel abort request by user. Disconnect i.e. removeGroup if
		 * already connected. Else, request WifiP2pManager to abort the ongoing
		 * request
		 */
		if (wdManager != null) {

			wdManager.cancelConnect(wdChannel, new ActionListener() {

				public void onSuccess() {
					Log.d(MyCarActivity.TAG,"Aborting connection");
				}

				public void onFailure(int reasonCode) {
					Log.d(MyCarActivity.TAG,"Connect abort request failed. Reason Code: "
							+ reasonCode);
				}
			});
		}
	}
	
	 /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public static void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        MyCarListener.isWifiP2pEnabled = isWifiP2pEnabled;
    }
	
}
