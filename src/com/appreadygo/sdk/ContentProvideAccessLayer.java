package com.appreadygo.sdk;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

public class ContentProvideAccessLayer {
	
	private final static String TAG = ContentProvideAccessLayer.class.getSimpleName();
	private final static Uri DOLLAR_TASK_URI_TASK_DATA =   Uri.parse("content://"+ "com.dollartask.contentprovider.dollartaskprovider" + "/"+"TaskData");
	
	private static final String KEY_PACKAGE_NAME = "PackageName";
	private static final String KEY_APPLICATION_NAME = "ApplicationName";
	private static final String KEY_APPLICATION_ID = "ApplicationId";
	private static final String KEY_TASK_ID = "TaskId";
	private static final String KEY_USER_ID = "UserId";
	    
	/*
	 * Author: Philip
	 * Returns Task Data filled by DollarTask via Content Provider by pre-defined selection criteria 
	 */
	private static ApplicationData getApplicationData(Activity activity, String selection)
    {
		try {
	    	ApplicationData appData = null;
	        
	        int taskId, userId;
	        String appName = null, packageNameValue = null, appId = null;
	        
	        Cursor cApplicationData = activity.getContentResolver().query(DOLLAR_TASK_URI_TASK_DATA, null, selection/* KEY_PACKAGE_NAME +" = " + packageNameSearchCriteria*/ , null, null);
	        
	        activity.startManagingCursor(cApplicationData);
	        
	        if (cApplicationData != null && cApplicationData.moveToFirst())
	        {	
	        	appId 	=  cApplicationData.getString(cApplicationData.getColumnIndex(KEY_APPLICATION_ID));
	        	taskId =  cApplicationData.getInt(cApplicationData.getColumnIndex(KEY_TASK_ID));
	        	appName = cApplicationData.getString(cApplicationData.getColumnIndex(KEY_APPLICATION_NAME));
	        	userId = cApplicationData.getInt(cApplicationData.getColumnIndex(KEY_USER_ID));
	        	packageNameValue = cApplicationData.getString(cApplicationData.getColumnIndex(KEY_PACKAGE_NAME));
	      
	        	InternalLog.d(TAG, "Task id:" + taskId);
	        	
	        	appData = new ApplicationData(taskId, appId, appName, userId, packageNameValue);
	        	
	        	if (cApplicationData != null && !cApplicationData.isClosed())
	            	cApplicationData.close();
	        }
	        else{
	        	InternalLog.d(TAG, "Task id not found!");
	        }
	        return appData;
		}
	        catch (Exception e) {
		    	InternalLog.d(TAG, "Unexpected error occured");
		    	return null;
		}
    }
	
	/*
	 * Author: Philip
	 * Returns Task Data filled by DollarTask via Content Provider by ApplicationName from FingerPrint file 
	 */
	public static ApplicationData getApplicationData(Activity activity){
		
		String selection = KEY_APPLICATION_NAME + " = " + "'" + CCServices.getApplicationName(activity) + "'";
		return getApplicationData(activity, selection);
		
	}

}
