/**
 * 
 */
package com.appreadygo.sdk;

import java.util.ArrayList;
import java.util.Properties;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;



/**
 * 
 * 
 * @author Philip Belder
 * @param 
 *
 */
class FingerPrintService {


	// - TAG for current log
	private static final String TAG = FingerPrintService.class.getSimpleName();

	public WSSessionData m_sessionData;
	
	private static ArrayList<WSSessionData> m_activeSession;

	enum OperationState
	{
		Started,
		Finished,
		Sending
	}

	protected void OnClick(String tag) {
		if (ApplicationConstants.fingerPrintOn
				&& m_sessionData.getState() == OperationState.Started)
		{
			m_sessionData.addClick(new Click(tag));
		}
	}
	
	/**
	 * Handle touch event
	 * When user only touch the screen - save coordinate as a touch
	 * When user scroll on screen - save first point and last point as a scroll data
	 * <Logic>
	 * OnTouchEvent check action ID
	 * 1. If Actions is ACTION_DOWN - touch event is started
	 * 	1.a save action ID
	 * 	1.b save touch coordinates as initial coordinates
	 * 2. Else if Action is ACTION_MOVE - touch event is continuous 
	 * 	2.a Set action id to ACTION_MOVE - scrolling 
	 * 3. Else if Action is ACTION_UP - touch event is finished 
	 * 	3.a If action was regular touch - save initial coordinate 
	 * 	3.b If action was scrolling save initial coordinate and current coordinates
	 * </Logic>
	 * @param view
	 * @param event
	 */
	void OnTouch(final View view, final MotionEvent event) {
		
		if (ApplicationConstants.fingerPrintOn
				&& m_sessionData.getState() == OperationState.Started)
		{
			final int action = event.getAction();
			if(action == MotionEvent.ACTION_DOWN){	
				//action started - the screen is pressed
				if (isNewEvent(event) && validateEvent(event)){
					m_sessionData.set_actionId(MotionEvent.ACTION_DOWN);
					
					m_sessionData.setInitialTouch(new WSInnerTouchData(view,event, m_sessionData.getSessionId()));
				}
			}//Action DOWN
			else if(action == MotionEvent.ACTION_MOVE){
				// TODO : do not update if we are already in MOVE
				m_sessionData.set_actionId(MotionEvent.ACTION_MOVE);
			}//Action MOVE
			else if(action == MotionEvent.ACTION_UP){
				
				if(m_sessionData.get_actionId() == MotionEvent.ACTION_DOWN  && m_sessionData.getInitialTouch() != null){
				
						m_sessionData.addInitialTouch();//current touch coordinates will be saved
						//DO NOT DELETE - MAY BE REANABLED IF SCHECKSCROLL is disabled.
						//m_sessionData.addViewPart(view);
						m_sessionData.set_actionId(WSSessionData.IdleState);
					
				}
				else if (m_sessionData.get_actionId() == MotionEvent.ACTION_MOVE)
					m_sessionData.addSwipe(view,event);
		}
		else{
			InternalLog.d(TAG, "The Service is not started, the touch is not recorded");
		}
		
	  }
	}//EOM OnTouch


	private boolean validateEvent(MotionEvent event) {
		if (event != null && event.getRawX() > 0 && event.getRawY() > 0){
			return true;
		}
		else{
			return false;
		}
	}


	private boolean isNewEvent(MotionEvent event) {
		
		boolean result;
		
		WSInnerTouchData initialTouch = m_sessionData.getInitialTouch();
		if (m_sessionData.getInitialTouch() == null)
		{
			result = true;
		}
		else {
			result = (initialTouch.getEventTime() != event.getEventTime());
		}
		
		return result;
		
	}


	/**
	 *
	 * Finish service
	 * If the service is started and Wifi is ON 
	 * Send data to server
	 * if the service was started - perform service started logic
	 * @param 
	 * 		currContext
	 * @return
	 */
	Boolean finishService(Context currContext, String uri) {
		if (ApplicationConstants.fingerPrintOn){
			try
			{
				InternalLog.d(TAG, "Finishing Service");
				//1.
				finishCurrentSession(currContext, uri);
				
				if(sendFinishedSessions(currContext)){
					return true;
				}// the service was started 
				else{	
					InternalLog.d(TAG, "Data to send is empty");
					return false;
				}
			}
			finally{
				InternalLog.d(TAG, "The service is finished");
			}
		}
		else
		{
			InternalLog.d(TAG, "ArgAnalytics is off");
			return false;
		}
		
	}//EOM

    boolean finishCurrentSession(Context currContext, String uri) {
		
	    try {
	    	
			WSSessionData sessionToClose = findStartedSession(uri);
			
			if (sessionToClose != null){
				return finishSession(currContext, sessionToClose);
			}
			else {
				return false;
			}
	    }
	    catch (Exception e) {
			InternalLog.e(TAG, "Unexpected Error in finishCurrentSession");
			return false;
	  }
	}


	private WSSessionData findStartedSession(String uri) 
	{
		int i;
		WSSessionData currentSession = null;
		
		for (i = 0; i < m_activeSession.size(); i++)
		{
			currentSession = m_activeSession.get(i);
		  
			if (currentSession.getState() == OperationState.Started && currentSession.getViewId().compareTo(uri)==0 ){
				   return currentSession;
				
			}
		}
		
		return currentSession;
	}

	boolean finishSession(Context currContext, WSSessionData session) {
		
		
		try {
			session.finish();
			
			int [] dimensions = CCServices.getOversllHeightWidth((Activity) currContext);
			if (dimensions != null){
				session.setCh( dimensions[0]);
				session.setCw( dimensions[1]);
			
				if (saveData(session, currContext)){
					m_activeSession.remove(session);
				}
				return true;
			}
			else{
				return false;
			}
			
		} catch (Exception e) {
			InternalLog.e(TAG, "Unexpected Error in finish()");
			return false;
		}
	}


	/**
	 * 1. save closing time
	 * 2. if Wifi is ON - send data to server
	 * 	2.a if sending was failed - save data 
	 * 3. otherwise - save data at database
	 * @param 
	 * 		currContext
	 * @param sessionToClose 
	 * @return
	 */
	
	
	private boolean sendFinishedSessions(Context currContext) {
		
		InternalLog.d(TAG, "The service is ON - perform finish session logic");
		NetworkStatus netwrokStatus = new NetworkStatus(currContext);
		
		if( (netwrokStatus.isWifiConnected() || 
		    (netwrokStatus.isConnected() && ApplicationConstants.allowSendIn3g))
			&& !ApplicationConstants.sendingToServer){
			//2.	
			try{
				InternalLog.d(TAG, "There session data will be sent to the server");
				ApplicationConstants.sendingToServer = true;
				if(sendData(currContext) == true){
					return true;//if succeeded  to send data - return
				}
			}
			catch(Exception e){
				InternalLog.e(TAG, "Problem with sending - data will be saved");
				ApplicationConstants.sendingToServer = false;
				return false;
			}
		}//if
		else
		{
			InternalLog.d(TAG, "There is no internet connection the data will be saved");
            return true;
		}
		
		return false;
		
	}
	

	/**
	 * Save current session data to DB is the service is in batch mode 
	 * @param sessionToClose 
	 * 
	 * 
	 * @return
	 * 		True id succeeded 
	 */
	private boolean saveData(WSSessionData sessionToClose, Context ctx)
	{

		if(ApplicationConstants.cacheInDatabase == true)
		{
			InternalLog.d(TAG, sessionToClose.getSessionId()+ " Saving Session Data");
			ArrayList<WSInnerTouchData> touchList = new ArrayList<WSInnerTouchData>();
			ArrayList<WSViewAreaData> viewPartList = new ArrayList<WSViewAreaData>();
			ArrayList<Click> clicks = new ArrayList<Click>();
			
			touchList = sessionToClose.getTouches();
			viewPartList = sessionToClose.getViewParts();
			clicks = sessionToClose.getClicks();
			
			int i;
			DbAdapter db = new DbAdapter(ctx); 		
			try{
				db.open();
				db.createSessionData(sessionToClose);
				for (i =0; i < touchList.size(); i++)
					db.createTouch(touchList.get(i));
				
				for (i =0; i < viewPartList.size(); i++)
					db.createViewPart(viewPartList.get(i),sessionToClose.getSessionId());
				
				for (i =0; i < clicks.size(); i++)
					db.createClick(clicks.get(i),sessionToClose.getSessionId());
				
				db.close();
			}
			catch(Exception e){
				InternalLog.e(TAG, "save session data failed " + e.getMessage());
				return false;
			}
			finally{
				db.close();			
			}
		}
		else
		{
			InternalLog.d(TAG, "The service is in data mode, the data was not saved");
			return false;
		}
		return true;
	}
	


	/**
	 * Send data to server 
	 * @param sessionToClose 
	 * @return
	 * 		true if succeeded 
	 */
	private boolean sendData(Context ctx) {
		try{			
			WSSessionData[] sessData = null;
			boolean sentSuccessfull = false;
		    sessData = createDataToSend(ctx);
		   
			if(sessData != null){// check if session data was created 
				//send data and screen params
				InternalLog.d(TAG, "Start Sending Data...");		
				sentSuccessfull = WSReportProxy.sendDatatoServer(sessData);

				if (sentSuccessfull)
				{
					InternalLog.d(TAG, "Data was sent");
					
					if (ApplicationConstants.cacheInDatabase){
						DbAdapter db = new DbAdapter(ctx); 
					    db.deleteSessionData(sessData);
					}
					return true; 
				}
				else{
					InternalLog.e(TAG, "sendData: problem with sending data");
					return false;
				}
			}
			else
				return false;
		}//try
		catch(Exception e){
		   InternalLog.e (TAG,"Unexpected error in finish()");
		   return false;
		}
	}




	
	/**
	 * 
	 * a. if the service works in the batch data mode 
	 * 	Fetch session data from the DB
	 * 	Add current session data to the end of the array and return 
	 * 	1. Fetch session data to temp array 
	 * 	2. Create new array size temp + 1
	 * 	3. Copy temp array to new array
	 * 	4. Add current session data to array
	 * 	5. return 
	 * 
	 *b. otherwise
	 * 	send only current data 
	 * @param sessionToClose 
	 * 	
	 * @return
	 */
	
	private WSSessionData[] createDataToSend(Context ctx) {
		//a.
		if (ApplicationConstants.cacheInDatabase == true) {
			
			try{// Fetch Data from Data base
			
				if( getNumberOfSavedSessions(ctx) < ApplicationConstants.minSessionsToSend){
					InternalLog.e(TAG, "there is not enought touches, data will not be sent");
					return null;
			    }
			    else{
				    //Create  data structures 
				    WSSessionData savedData[] = fetchSavedData(ctx);
					WSSessionData[] sData =  new WSSessionData[savedData.length]; // (array of view part objects),
					System.arraycopy(savedData, 0, sData, 0, savedData.length);
					//sData[savedData.length] = sessionToClose;
					return sData;
			    }
			}
		    catch(Exception e){		
				//TODO : may be here we need to send only current data and don`t sent it to the DB
			   InternalLog.e (TAG,"Failed to get touches/scrolls from db" + e.getMessage());
			   return null;
		    }
		    
		}//if isBatchMode
		else{
			//b.
			return createDataBasedOnCache();
		
		}
	}


	private WSSessionData[] createDataBasedOnCache() {
		try{
			InternalLog.d (TAG,"The service is memory data mode");
			int i = 0;
			int size =  m_activeSession.size();
			WSSessionData[] sData;
			if (size >= ApplicationConstants.minSessionsToSend){
				sData = new WSSessionData[size]; 
				for (i = size - 1 ; i >= 0; i--)
				{
					WSSessionData currentSession = m_activeSession.get(i);
					
					if (currentSession != null && currentSession.getState() == OperationState.Finished){
						currentSession.setState(OperationState.Sending);
						sData[i] = currentSession;
						m_activeSession.remove(currentSession);
					}
				 }
			}
			else{
				InternalLog.d (TAG,"The number of session is less than minimum session to send");
				return null;
			}
				
			return sData;
		}
		catch(Exception e){
			InternalLog.e (TAG,"Failed to build data from cache: " + e.getMessage());
			return null;
		}
		
	}



	/**
	 * 
	 * @param ctx
	 * @return
	 */
	private long getNumberOfSavedSessions(Context ctx) {
		
		long numberOfSessions = 0;
		if (ApplicationConstants.cacheInDatabase == true){
			DbAdapter db = new DbAdapter(ctx);
			try {
				 db.open();
				 numberOfSessions = db.fetchTableCount(DbAdapter.TABLE_SESSION);
				
			} catch (SQLException e) {
				InternalLog.e(TAG, "Failed to get number of sessions");
				return 0;
			}
	
			finally	{
				db.close();
			}
	   }	
	   return numberOfSessions;
	}





	private WSSessionData[] fetchSavedData(Context ctx) {
		
	    WSSessionData[] sessionData;
		int i = 0;
		
		float viewPartX,viewPartY,coordX,coordY, press,orientation;
		
		String touchTimestamp, startTimestamp, endTimestamp,viewId,sessionId,scrollId,clickTimestamp,controlTag;
	    Cursor cAllSessions;
	    Cursor cAllTocuhes = null;
	    Cursor cAllViewParts = null;
	    Cursor cAllCliks = null; 
	    
		float clientWidth, clientHeight;
		WSInnerTouchData currentTouch = null;
		WSInnerTouchData previousTouch = null;
		WSViewAreaData currentViewPart = null;
		Click currentClick = null;
		
		DbAdapter db = new DbAdapter(ctx);
	    db.open();
		
		try
		{
			cAllSessions = db.fetchSessions(ApplicationConstants.rowsToFetch);
		
		}
		catch(Exception e)
		{
		   InternalLog.e (TAG, "Failed to get sessions/touches/scrolls from db" + e.getMessage());
		   return null;
		}
		
		
	    sessionData = new WSSessionData[cAllSessions.getCount()];
		
		try 
		{
			
		  while (cAllSessions.moveToNext())
		  {
			    
				viewId = (cAllSessions.getString(cAllSessions.getColumnIndex(DbAdapter.KEY_VIEWID)));
				startTimestamp = (cAllSessions.getString(cAllSessions.getColumnIndex(DbAdapter.KEY_TIMESTAMP_START)));
				endTimestamp = (cAllSessions.getString(cAllSessions.getColumnIndex(DbAdapter.KEY_TIMESTAMP_END)));
				clientWidth = (cAllSessions.getFloat(cAllSessions.getColumnIndex(DbAdapter.KEY_CLIENTW)));
				clientHeight = (cAllSessions.getFloat(cAllSessions.getColumnIndex(DbAdapter.KEY_CLIENTH)));
				sessionId = (cAllSessions.getString(cAllSessions.getColumnIndex(DbAdapter.KEY_SESSION_ID)));
				
				sessionData[i] = new WSSessionData(viewId, (int)clientWidth, (int) clientHeight, startTimestamp, endTimestamp,sessionId);
				
				ArrayList<WSScrollData> scrolls = sessionData[i].getScrollData();
				ArrayList<WSInnerTouchData> touches = sessionData[i].getTouches();
				ArrayList<WSViewAreaData> viewParts = sessionData[i].getViewParts();
				ArrayList<Click> clicks = sessionData[i].getClicks();
				
				cAllTocuhes = db.fetchTouches(ApplicationConstants.rowsToFetch,sessionId);
				cAllViewParts = db.fetchViewParts(ApplicationConstants.rowsToFetch,sessionId);
				
				cAllCliks = db.fetchClicks (ApplicationConstants.rowsToFetch,sessionId);
			
						
				while (cAllTocuhes.moveToNext())
				{
					touchTimestamp = (cAllTocuhes.getString(cAllTocuhes.getColumnIndex(DbAdapter.KEY_TIMESTAMP)));
					coordX = (cAllTocuhes.getFloat(cAllTocuhes.getColumnIndex(DbAdapter.KEY_XCOORD)));
					coordY = (cAllTocuhes.getFloat(cAllTocuhes.getColumnIndex(DbAdapter.KEY_YCOORD)));
					press =  (cAllTocuhes.getFloat(cAllTocuhes.getColumnIndex(DbAdapter.KEY_PRESSURE)));
					orientation = (cAllTocuhes.getFloat(cAllTocuhes.getColumnIndex(DbAdapter.KEY_ORIENTATION)));
					scrollId = (cAllTocuhes.getString(cAllTocuhes.getColumnIndex(DbAdapter.KEY_SCROLL_ID)));
					
					currentTouch = new WSInnerTouchData(touchTimestamp, coordX, coordY,press,scrollId,(int)orientation);
					
					touches.add(currentTouch);
					
					//Creating scroll if scroll is not " " and previous scrollId equals to current scroll
					if (previousTouch != null && scrollId.compareTo(" ") != 0 && scrollId.compareTo(previousTouch.getScrollId()) == 0){
						
						scrolls.add (new WSScrollData(previousTouch, currentTouch));
					}
						
					previousTouch = new WSInnerTouchData(touchTimestamp, coordX, coordY,press,scrollId,(int)orientation);
					
				}
				
				while (cAllViewParts.moveToNext())
				{
					startTimestamp = (cAllViewParts.getString(cAllViewParts.getColumnIndex(DbAdapter.KEY_TIMESTAMP)));
					endTimestamp = (cAllViewParts.getString(cAllViewParts.getColumnIndex(DbAdapter.KEY_TIMESTAMP_END)));
					viewPartX = (cAllViewParts.getFloat(cAllViewParts.getColumnIndex(DbAdapter.KEY_VIEW_PART_X)));
					viewPartY = (cAllViewParts.getFloat(cAllViewParts.getColumnIndex(DbAdapter.KEY_VIEW_PART_Y)));
					orientation = (cAllViewParts.getFloat(cAllTocuhes.getColumnIndex(DbAdapter.KEY_ORIENTATION)));
					currentViewPart = new WSViewAreaData(startTimestamp,endTimestamp,(int)viewPartX,(int)viewPartY,(int)orientation);
					
					viewParts.add(currentViewPart);
				}
				
				while (cAllCliks.moveToNext())
				{
					clickTimestamp = (cAllCliks.getString(cAllCliks.getColumnIndex(DbAdapter.KEY_TIMESTAMP)));
					controlTag = (cAllCliks.getString(cAllCliks.getColumnIndex(DbAdapter.KEY_CONTROL_TAG)));
					currentClick = new Click(controlTag, clickTimestamp);
					clicks.add(currentClick);
				}
				
				if (cAllViewParts != null && !cAllViewParts.isClosed())
						cAllViewParts.close();
				
				if (cAllTocuhes != null && !cAllTocuhes.isClosed())
						cAllTocuhes.close();
				
				if (cAllCliks != null && !cAllCliks.isClosed())
					cAllCliks.close();
				
				cAllTocuhes.close();	
				i++;
			  }
		  if (cAllSessions != null && !cAllSessions.isClosed())
			  cAllSessions.close();
		}
		
		catch(Exception e){
		   InternalLog.e (TAG,"Failed to initialized the arrays before sending" + e.getMessage());
		   return null;
		}
		
		finally{
			db.close();
		}
	    return sessionData;
	    
	    
			
	}
	
    /**Initializes application data - all the variables that are relevant for all the application
	 * Return TRUE : if the application is initialized successfully
	 * Return FALSE : Otherwise
	 * @param activity
	 * @param clientId received from mobillify.com , provided as part of the registration process.
	 */   
    private static boolean initializeApplication(Activity activity,String clientId)
    {
        if (ApplicationConstants.initialized == false)
        {
        	
    	   	if (getProperties(activity)){
			
				try{
				     Display display = ((WindowManager)(activity.getApplicationContext()).getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
			 	     ApplicationConstants.sw = display.getWidth(); 
			         ApplicationConstants.sh = display.getHeight(); 
			 		 ApplicationConstants.cid = (ApplicationConstants.cid == null) ? clientId : ApplicationConstants.cid;
			         ApplicationConstants.systemInfo = new WSSystemInfoData(activity.getApplicationContext());
			         m_activeSession = new ArrayList<WSSessionData>();
			         
			         ApplicationConstants.initialized = validateInit();
				}
				catch (Exception e1){
					InternalLog.e (TAG,"Failed to initialize client data" + e1.getMessage());
					return false;
				}
    	   	}
    	   	else {
    	   		return false;
    	   	}
        }
        return ApplicationConstants.initialized;
    }


	private static boolean validateInit() {
		 if (ApplicationConstants.cid == null){
			 InternalLog.e(TAG, "Application Name is not initialized");
			 return false;
		 }
		 else {
			 return true;
		 }
	}


	private static boolean getProperties(Activity activity) {
		try {
			
			String fingerPrintOn, cacheInDatabase, targetEnvironment, allowdebugnow, allowSend3g;
			AssetManager assetManager =  activity.getResources().getAssets();
			Properties configFile = new Properties();
			
			configFile.load(assetManager.open("fingerprint.properties"));
			
			fingerPrintOn = configFile.getProperty("FingerPrint");
			if (fingerPrintOn == null){
				fingerPrintOn = configFile.getProperty("fingerprint");
			}
			cacheInDatabase = configFile.getProperty("CacheInDatabase");
			targetEnvironment = configFile.getProperty("ServerMode");
			allowdebugnow = configFile.getProperty("DebugMode");
			allowSend3g = configFile.getProperty("AllowSend3g");
			
			ApplicationConstants.cid = configFile.getProperty("ApplicationName");
			
			if(fingerPrintOn == null)
				ApplicationConstants.fingerPrintOn = false;
			else
				ApplicationConstants.fingerPrintOn = (fingerPrintOn.compareTo("1") == 0);
			
			if(cacheInDatabase == null)
			    ApplicationConstants.cacheInDatabase = false;
			else
				ApplicationConstants.cacheInDatabase = (cacheInDatabase.compareTo("1") == 0);
			/*
			 * initializing Target environment 
			 * 0 - Local
			 * 1 - Server
			 * 2 - QA 
			 */
			if(targetEnvironment == null)
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.Local;
			else if (targetEnvironment.compareTo("1") == 0) {
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.Server;
			}
			else if (targetEnvironment.compareTo("2") == 0) {
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.QA;
			}
			else if (targetEnvironment.compareTo("0") == 0) {
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.Local;
			}
			else if (targetEnvironment.compareTo("3") == 0) {
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.Local2;
			}
			else if (targetEnvironment.compareTo("4") == 0) {
				ApplicationConstants.targetEnvironmentMode = ApplicationConstants.TargetEnvironment.QA2;
			}
			
			if(allowdebugnow == null)
				ApplicationConstants.allowdebugnow = false;
			else
				ApplicationConstants.allowdebugnow = (allowdebugnow.compareTo("1") == 0);
			
			if(allowSend3g == null)
				ApplicationConstants.allowSendIn3g = false;
			else
				ApplicationConstants.allowSendIn3g = (allowSend3g.compareTo("1") == 0);
			
			
			ApplicationConstants.debugMode = CCServices.isDebugMode(activity.getApplicationContext()) && ApplicationConstants.allowdebugnow;
			return true;
		}//end of try
		catch (Exception e1){
			InternalLog.e (TAG,"Failed to load properties file" + e1.getMessage());
			return false;
		}
	}
    
    /**Initializes activity specific data - all the variables that are relevant for for the current activity
	 * Return TRUE : if the activity is initialized successfully
	 * Return FALSE : Otherwise
	 * @param activity
	 * @param clientId - received from mobillify.com , provided as part of the registration process.
	 */
	public boolean initializeActivity(Activity activity,String clientId, View view) {
		
		    if (initializeApplication(activity,clientId) == true)
		    {
		    	return setListeners(view);
		    }
		    else{
		       return false;
		    }
      }



	/**
	 * 
	 * @param view
	 * @return
	 */
	public boolean setListeners(View view) {
		try
		{
			InnerOnTouchListener l = new InnerOnTouchListener();
			setViewGroupListener(view, l);
		    view.setOnTouchListener(l);

        }
		catch(Exception e)
		{
		   InternalLog.e (TAG,"Failed to set listeners" + e.getMessage());
		   return false;
		}
		return true;
	}



	/**
	 * 
	 * @param view
	 * @param l
	 */
	private void setViewGroupListener(View view, InnerOnTouchListener l) {
		
		ViewGroup viewgroup = null;
		
		try {
			if (view instanceof ViewGroup){
			    viewgroup = (ViewGroup) view;
			}
			else
			{
				view.setOnTouchListener(l);
				return;
			}
		}
		catch (Exception e){
			view.setOnTouchListener(l);
			return;
		}
		
		if (viewgroup != null)
		{
			view.setOnTouchListener(l);
			int i,count;
			View tmpView;
			count = viewgroup.getChildCount();
			
		    for (i = 0; i < count; i++)
		    {
		    	tmpView = viewgroup.getChildAt(i);
		    	setViewGroupListener(tmpView,l);
		    }
			return;
		}
	   
	}


	/**
	 * 
	 * @param view - the Top View
	 * @param uri
	 * @return
	 */
	protected boolean initSession(Activity activity, String uri) {
		try{
			View view = activity.getWindow().getDecorView()
			.getRootView();
			
				m_sessionData = new WSSessionData(uri);
				m_sessionData.addViewPart(view);
				m_sessionData.setState(OperationState.Started);
				m_sessionData.startCheckScroll(activity);
				m_activeSession.add(m_sessionData);
			    InternalLog.d(TAG, "Session initilized " + uri + " " + m_sessionData.getSessionId());
		}	
		catch(Exception e){
			   InternalLog.e (TAG,"Failed to init Session" + e.getMessage());
			   return false;
		}
		
		return true;
	}


	
	

}
