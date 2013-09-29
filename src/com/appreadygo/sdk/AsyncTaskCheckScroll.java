package com.appreadygo.sdk;

import android.os.AsyncTask;
import android.widget.ScrollView;


class AsyncTaskCheckScroll extends AsyncTask<AsyncTaskCheckScrollInput, Void, Boolean> {
	
	final String TAG = AsyncTaskCheckScroll.class.getSimpleName();
	
	final int initialMillisecondToWait = 2500;
	final int intervalMillisecondToWait = 1500;
	
	@Override
	protected Boolean doInBackground(AsyncTaskCheckScrollInput... inputArray) {
		
		try{
			Thread.sleep(initialMillisecondToWait);
			AsyncTaskCheckScrollInput input = inputArray[0];
			
			ScrollView scrollView = input.getScrollView();
		    WSSessionData sessionData = input.getSession();
			while (true){
		    	Thread.sleep(intervalMillisecondToWait);
		    	InternalLog.d(TAG,"Checking scroll....");
		    		sessionData.addViewPart(scrollView);
		    }
		    	
		}
		catch (Exception e) {
			InternalLog.d(TAG, "Unepxected Error");
			return false;
		}
	}
	
	 protected void onPostExecute(Boolean result) {
         InternalLog.d(TAG, "Finished");
     }
	 
}
