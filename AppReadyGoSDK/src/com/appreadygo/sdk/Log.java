package com.appreadygo.sdk;

class InternalLog {
	
	 private static final boolean enableLog = ApplicationConstants.debugMode;
	
	 private static final String TAG = InternalLog.class.getSimpleName();
	 /**
	  * 
	  * @param tag
	  * @param msg
	  */
	 public static void e(String tag, String msg)
	 {
	    if (enableLog) android.util.Log.e(TAG,tag + " " + msg);
	 }

	 /**
	  * 
	  * @param tag
	  * @param msg
	  */
	 public static void w(String tag, String msg)
     {
	    if (enableLog) android.util.Log.w(TAG, tag + " " + msg);
     }

	 
	 /**
	  * 
	  * @param tag
	  * @param msg
	  */
     public static void i(String tag, String msg)
     {
    	if (enableLog) android.util.Log.i(TAG, tag + " " + msg);
     }

     
     /**
      * 
      * @param tag
      * @param msg
      */
     public static void d(String tag, String msg)
     {
    	if (enableLog) android.util.Log.d(TAG, tag + " " + msg);
     }

}
