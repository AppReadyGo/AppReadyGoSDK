package com.appreadygo.sdk;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * Here comes the base description
 * 
 * @version 1.0
 * @since 1.0
 */

public class FingerPrint {

	private final static String TAG = FingerPrint.class.getSimpleName();

	private static FingerPrintService m_fingerService = new FingerPrintService();

	/**
	 * init() method should be called in onCreate() method of your activity
	 * 
	 * @param clientId
	 *            ApplicationName received from mobillify.com
	 * @param uri
	 *            viewUri received from mobillify.com
	 * @param activity
	 *            the Activity
	 */
	public static Boolean init(String clientId, String uri, Activity activity) {

		Boolean result = false;
		try {
			View mTopView = activity.getWindow().getDecorView().getRootView();

			if (m_fingerService.initializeActivity(activity, clientId,
					mTopView))
				result = true;
		} catch (Exception ex) {
			InternalLog.e(TAG, "init(client, uri, activity): Unepxected Error");
		}
		return result;
	}

	/**
	 * init() method should be called in onCreate() method of your activity
	 * 
	 * @param activity
	 *            the Activity object instance
	 */
	public static Boolean init(Activity activity) {

		Boolean result = false;

		try {
			String activityName = CCServices.getActivityName(activity);
			if (activityName != null)
				result = init(null, null, activity);
		} catch (Exception ex) {
			InternalLog.e(TAG, "init(Activity): Unepxected Error");
		}
		return result;
	}

	/**
	 * start() method should be called in onStart() method of your activity
	 * 
	 * @param activity
	 *            the Activity
	 * @param uri
	 *            viewUri received from mobillify.com
	 */
	public static Boolean start(Activity activity, String uri) {

		Boolean result = false;

		try {
			if (ApplicationConstants.initialized) {
				
				result = m_fingerService.initSession(activity, uri);
			}
		} catch (Exception ex) {
			InternalLog.e(TAG, "start(Activity, URI): Unepxected Error");
		}
		return result;
	}

	/**
	 * start() method should be called in onStart() method of your activity
	 * 
	 * @param activity
	 *            the Activity
	 */
	public static Boolean start(Activity activity) {

		Boolean result = false;

		try {
			String activityName = CCServices.getActivityName(activity);
			if (activityName != null)
				result = start(activity, activityName);
		} catch (Exception ex) {
			InternalLog.e(TAG, "start(Activity): Unepxected Error");
		}
		return result;
	}

	/**
	 * onTouch() method should be called only if you overload onTouch event in
	 * your activity,
	 * 
	 * @param view
	 *            param from the originally overloaded onTouch method
	 * @param event
	 *            param from the originally overloaded onTouch method
	 */
	public static boolean onTouch(View view, MotionEvent event) {

		try {
			m_fingerService.OnTouch(view, event);
		} catch (Exception e) {
			InternalLog.e(TAG, "onTouch: Unepxected Error");
		}
		return true;

	}
	
	/**
	 * onClick method should be called only when you track a button
	 * 
	 * @param view
	 *            param from the originally overloaded onTouch method
	 * @param event
	 *            param from the originally overloaded onTouch method
	 */
	public static boolean onClick(String tag) {

		try {
			m_fingerService.OnClick(tag);
		} catch (Exception e) {
			InternalLog.e(tag, "onClick: Unepxected Error");
		}
		return true;

	}

	/**
	 * 
	 * @param ctx
	 *            Context of the application
	 * @param uri
	 *            URI of current view
	 * @return TRUE if succeeded to finish
	 */
	protected static boolean finishService(Context ctx, String uri) {

		return m_fingerService.finishService(ctx, uri);
	}

	/**
	 * finish() method should be called only in onStop() method of your Activity
	 * 
	 * @param ctx
	 *            param is the application base context:
	 *            this.getApplicationContext()
	 * @param uri
	 *            viewUri received from mobillify.com
	 */
	public static boolean finish(Context ctx, String uri) {
		try {
			InternalLog.d(TAG, "Finishing servive " + uri);
			// InternalLog.d(tag, "time stop 1" +
			// CCServices.getGMTDateTimeAsString());
			if (ApplicationConstants.fingerPrintOn) {

				new AsyncTaskFinishWrapper()
						.execute(new AsyncTaskFinishWrapperInput(ctx, uri));
				return true;// m_fingerService.finishService(ctx,uri);
			} else {
				return false;
			}
		} catch (Exception e) {
			InternalLog.e(TAG,
					"stop() executed with exception " + e.getMessage());
			return false;
		} finally {
			InternalLog.d(TAG,
					"Finished");
		}
	}

	/**
	 * 
	 * finish() method should be called only in onStop() method of your Activity
	 * 
	 * @param activity
	 *            Instance of Activity class
	 * @return TRUE if succeeded to finish the service
	 */
	public static boolean finish(Activity activity) {

		Boolean result = false;
		try {
			Context ctx = (Context) activity;
			String activityName = CCServices.getActivityName(activity);
			result = finish(ctx, activityName);
		} catch (Exception ex) {
			InternalLog.e(TAG, "finish(Activity): exception");
		}
		return result;
	}

}
