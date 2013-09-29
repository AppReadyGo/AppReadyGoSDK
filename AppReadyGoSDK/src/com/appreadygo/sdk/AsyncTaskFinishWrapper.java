package com.appreadygo.sdk;

import android.os.AsyncTask;


/**
 * 
 * @author Pavel
 *
 */
class AsyncTaskFinishWrapper extends AsyncTask<AsyncTaskFinishWrapperInput, Void, Boolean> {

	@Override
	protected Boolean doInBackground(AsyncTaskFinishWrapperInput... inputArray) {
		
		final String TAG = AsyncTaskFinishWrapper.class.getSimpleName();
		
		try{
			AsyncTaskFinishWrapperInput input = inputArray[0];
		    return FingerPrint.finishService(input.getCtx(),input.getUri());
		}
		catch (Exception e) {
			InternalLog.d(TAG, "Unepxected Error");
			ApplicationConstants.sendingToServer = false;
			return false;
		}
	}
	
	 protected void onPostExecute(Boolean result) {
         ApplicationConstants.sendingToServer = false;
     }

}
