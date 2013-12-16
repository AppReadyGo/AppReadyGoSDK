package com.appreadygo.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appreadygo.sdk.ApplicationConstants.TargetEnvironment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;

class CCServices {

	private static String TAG = CCServices.class.getSimpleName();

	/**
	 * Server URL
	 */
	private static final String Server_URL = "http://api.appreadygo.com/analytics/submitpackage";//"http://api.finger.mobillify.com/et/submit";

	/**
	 * QA URL
	 */
	private static final String QA_URL = "http://api.qa.appreadygo.com/analytics/submitpackage";
	
	/**
	 * Local server URL
	 */
	private static final String Local_URL = "http://192.168.1.100/analytics/submitpackage";
	
	/**
	 * Local server URL2
	 */
	private static final String Local_URL2 = "http://192.168.1.100/analytics/submitpackage";
	
	/**
	 * QA2 URL
	 */
	private static final String QA_URL2 = "http://api.qa.appreadygo.com/analytics/submitpackage";
	

	static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();

	}

	/**
	 * 
	 * @param pobjContext
	 * @return
	 */
	static boolean IsWifiConnectionEnabled(Context pobjContext) {

		try {
			ConnectivityManager conMan = (ConnectivityManager) pobjContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();

			if (wifi == NetworkInfo.State.CONNECTED) {
				// wifi
				return true;
			}
			return false;
		} catch (Exception ex) {
			InternalLog.d(TAG, ex.getMessage());
			return false;
		}
	}

	

	/**
	 * 
	 * @return
	 */
	static String getTimeinMilliseconds() {
		Calendar c = Calendar.getInstance();
		return Long.toString(c.getTimeInMillis());
	}

	/**
	 * 
	 * @return
	 */
	static String getGMTDateTimeAsString() {
		String dateMask = "EEE, dd MMM yyyy HH:mm:ss"; // dd/MM/yyyy
														// HH:mm:ss";//
		Date pdt = new Date();
	
		// Local time zone
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateMask,Locale.ENGLISH);
		String res = dateFormat.format(pdt) + " GMT";
		return res;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isDebugMode(Context context) {

		PackageManager pm = context.getPackageManager();
		try {

			ApplicationInfo info = pm.getApplicationInfo(
					context.getPackageName(), 0);
			return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

		} catch (NameNotFoundException e) {

			InternalLog.d(TAG, "isDebugMode NameNotFoundExceprion");
		}

		return false;
	}

	public static void buildJsonFromArray(JSONObject json,
			ArrayList<? extends IWSJsonConvert> array, String index)
			throws JSONException {

		JSONArray jArr = new JSONArray();

		for (int i = 0; i < array.size(); i++) {
			if (array.get(i) != null){
				JSONObject tmpJSON = array.get(i).ConvertToJson();
				jArr.put(tmpJSON);
			}
		}

		json.put(index, jArr);
	}

	protected static View findMainUserFrameView(Activity userActivity) {

		Window window = userActivity.getWindow();
		ViewGroup mainFrameLayout = (ViewGroup) window
				.findViewById(Window.ID_ANDROID_CONTENT);
		View mainUserView = mainFrameLayout.getChildAt(0);

		return mainUserView;

	}

	protected static View getTitleBarView(Activity userActivity) {

		ViewGroup mainViewGroup = (ViewGroup) userActivity.getWindow()
				.getDecorView();
		ViewGroup textFrameLayout = (ViewGroup) ((ViewGroup) mainViewGroup
				.getChildAt(0)).getChildAt(0);
		View titleBarView = textFrameLayout.getChildAt(0);
		return titleBarView;

	}

	protected static int getContentViewOffset(Activity userActivity) {

		int contentViewOffset = 0;
		try {
			Rect rectangle = new Rect();
			Window window = userActivity.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
			contentViewOffset = window.findViewById(Window.ID_ANDROID_CONTENT)
					.getTop();
		} catch (Exception e) {
			InternalLog
					.d(TAG,
							"Unexpected error in getContentViewOffset: Could not find View size");
		}

		return contentViewOffset;

	}

	protected static int[] getOversllHeightWidth(Activity userActivity) {

		int viewHeight = ApplicationConstants.sh;
		int viewWidth = ApplicationConstants.sw;
		
		try {
			View upperUserView = findMainUserFrameView(userActivity);
			if (upperUserView instanceof ScrollView) {
				View theOnlyScrollChild = ((ViewGroup) upperUserView)
						.getChildAt(0);
				viewHeight = theOnlyScrollChild.getMeasuredHeight()
						+ getContentViewOffset(userActivity);
				viewWidth = theOnlyScrollChild.getMeasuredWidth();
			}

		} catch (Exception e) {
			InternalLog
					.d(TAG,
							"Unexpected error in getUpperViewHeightWidth: Could not find View size");
		}

		int[] heightWidthArray = { viewHeight, viewWidth };
		return heightWidthArray;

	}

//	static void writeToFile(String inputString) throws FileNotFoundException,
//			IOException {
//
//		String sFileName = "fingerprint" + CCServices.getTimeinMilliseconds()
//				+ ".txt";
//
//		File targetFile = new File(Environment.getExternalStorageDirectory(),
//				"FingerPrint");
//		if (!targetFile.exists()) {
//			targetFile.mkdirs();
//		}
//		File gpxfile = new File(targetFile, sFileName);
//		FileWriter writer = new FileWriter(gpxfile);
//		writer.append(inputString);
//		writer.flush();
//		writer.close();
//	}

	/**
	 * Return LocalURL if local/debug mode return ServerURL if server/production
	 * mode
	 * 
	 * @return
	 */
	static public String getServiceURL() {
		//return Server_URL;
		if (ApplicationConstants.targetEnvironmentMode == TargetEnvironment.QA)
			return QA_URL;
		else if (ApplicationConstants.targetEnvironmentMode == TargetEnvironment.Server)
			return Server_URL;
		else if (ApplicationConstants.targetEnvironmentMode == TargetEnvironment.Local)
			return Local_URL;
		else if (ApplicationConstants.targetEnvironmentMode == TargetEnvironment.Local2)
			return Local_URL2;
		else if (ApplicationConstants.targetEnvironmentMode == TargetEnvironment.QA2)
			return QA_URL2;
		else 
			return Local_URL;
	}

	/**
	 * Return Activity name
	 * mode
	 * 
	 * @return
	 */
	static public String getActivityName (Activity activity) {
		try{
			String activityname = activity.getClass().getName();
			return activityname.substring(activityname.lastIndexOf(".") + 1);
		
  	    }catch (Exception e) {
  	    	InternalLog.d(TAG, "Unexpected error in getActivityName, unable to get Activity name");
  	    	return null;
	    }
	}
	
	protected static ScrollView findScrollView(Activity activity) {
	  try{	
		  
		View currentView = findMainUserFrameView(activity);
	    
		while (currentView != null){
			
	    	if (currentView instanceof ScrollView){
				return (ScrollView) currentView;
			}
	    	
	    	if (currentView instanceof ViewGroup){
	    		currentView = ((ViewGroup)currentView).getChildAt(0);
	    	}
	    	else{
	    		return null;
	    	}
	    	
	    }
	    return null;
	    
	  }catch (Exception e) {
	    	InternalLog.d(TAG, "Unexpected error in findScrollView, unable to find scrollView");
	    	return null;
	  }
	}

	/*
	 * Author: Philip
	 * Returns Task ID filled by DollarTask via Content Provider 
	 */
	public static int getTaskId(Activity activity) {
		
		ApplicationData appData = ContentProvideAccessLayer.getApplicationData(activity);
		if (appData != null) { 
			return appData.getTaskId();
		}
		else {
			InternalLog.d(TAG, "Task id is null, returning 0");
			return 0;
		}
	}
	
	


}
