package com.appreadygo.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;


class NetworkStatus {
	
	boolean connected = false;
	boolean wifiConnected = false;
	 
	private static final String TAG = NetworkStatus.class.getSimpleName();

	public NetworkStatus(Context ctx) {
		 try {
		        ConnectivityManager connectivityManager;
		      
		        connectivityManager = (ConnectivityManager) ctx
		                        .getSystemService(Context.CONNECTIVITY_SERVICE);
		
		        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		        //any connection
		        connected = networkInfo != null && networkInfo.isAvailable() &&
		                networkInfo.isConnected();
		        
		       
		        State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
								.getState();
		
				if (wifi == NetworkInfo.State.CONNECTED) {
					// wifi
					wifiConnected = true;
				}
				InternalLog.d(TAG, "Netwrok status 3G=" + connected + " wifi=" + wifiConnected);
				        
		    }
	        catch (Exception ex) {
				InternalLog.d(TAG, "Can`t check netwrok status. Set to disconnected");
				wifiConnected = false;
				connected = false;
			}
	        
	}
	
	boolean isConnected() {
		return connected;
	}

	boolean isWifiConnected() {
		return wifiConnected;
	}

}
