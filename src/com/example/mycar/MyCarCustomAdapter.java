package com.example.mycar;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

	public class MyCarCustomAdapter extends ArrayAdapter<WifiP2pDevice>{
	    private ArrayList<WifiP2pDevice> peers;
	    private Activity activity;
	 
	    public MyCarCustomAdapter(Activity a, int textViewResourceId, ArrayList<WifiP2pDevice> entries) {
	        super(a, textViewResourceId, entries);
	        this.peers = entries;
	        this.activity = a;
	    }
	   
	    private String getDeviceStatus(int deviceStatus) {
	        Log.d(MyCarActivity.TAG, "Peer status :" + deviceStatus);
	        switch (deviceStatus) {
	            case WifiP2pDevice.AVAILABLE:  	// 3
	                return "Available";
	            case WifiP2pDevice.INVITED:    	// 1
	                return "Invited";
	            case WifiP2pDevice.CONNECTED:  	// 0
	                return "Connected";
	            case WifiP2pDevice.FAILED:     	// 2
	                return "Failed";
	            case WifiP2pDevice.UNAVAILABLE:	// 4
	                return "Unavailable";
	            default:
	                return "Unknown";

	        }
	    }
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	Log.d(MyCarActivity.TAG, "getView");
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi =
	                (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.row_devices, null);
	        }
	 
	        WifiP2pDevice device = peers.get(position);
	        if (device != null) {
	            TextView top = (TextView) v.findViewById(R.id.device_name);
	            TextView bottom = (TextView) v.findViewById(R.id.device_details);
	            if (top != null) {
	                top.setText(device.deviceName);
	            }
	            if (bottom != null) {
	            	Log.d(MyCarActivity.TAG, "bottom set text");
	                bottom.setText(getDeviceStatus(device.status));
	            }
	        }
	        return v;
	    }
	 
	}
