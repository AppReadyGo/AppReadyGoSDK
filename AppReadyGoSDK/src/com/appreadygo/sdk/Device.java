package com.appreadygo.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

public class Device {
	
	enum DeviceType
	{
		Phone,
		Tablet
	}
	
	private final static String TAG = Device.class.getSimpleName();

	static DeviceType getDeviceType(Context ctx) {
	    if ((ctx.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE ){
	    	return DeviceType.Tablet;
	    }else{
	    	return DeviceType.Phone;
	    }
	}
	
	/**
	 * 
	 * @param ctx
	 * @return
	 */
	static int getDeviceOrientation(Context ctx) {

		try {
			Display display = ((WindowManager) ctx
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();

			return display.getOrientation();
		} catch (Exception ex) {
			InternalLog.d(TAG,
					"Error while getting Orientation " + ex.getMessage());
			return 0;
		}
	}
}
