package com.appreadygo.sdk;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.appreadygo.sdk.FingerPrintService.OperationState;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;


/**
 * 
 * This class represents all session data for single using of application screen 
 * 
 * @author Philip 
 *
 */
class WSSessionData implements IWSJsonConvert
{
	
	static final int IdleState = 121212123;
	
	private static final String TAG = WSSessionData.class.getSimpleName();
	
	// - page url (viewId), the parameter is optional for websites, and must for application. This is unique path to page, for example:"/{Application Name}/{Page Name}"
	private String m_viewId; 
	// - screen width, the parameter is width of device screen (if application is full screen) or size of viewable part of a document.
	int m_sw;		
	// - screen height
	int m_sh; 	
	// - client width, the parameter is width of a document.
	private int m_cw;		
	// - client height
	private int m_ch;			
	// - scrolls 
	private ArrayList<WSScrollData> m_scrollData;
	
	private ArrayList<WSInnerTouchData> m_touches;
	
	private ArrayList<WSViewAreaData> m_viewParts;
	
	private ArrayList<Click> m_clicks;
	
	// - current action 
	private int  m_actionId;
	
	private WSInnerTouchData m_initialTouch;	
	// - start session time, start using the view
	private String m_StartTime;
	// - closing session time, stop using the view
	private String m_EndTime;
	
	private WSViewAreaData m_lastView;
	
	//Session Id
	private String m_sessionId; 
	
	private FingerPrintService.OperationState m_state;
	
	private AsyncTaskCheckScroll checkScroll;
	
	
	
	
	/**
	 * 
	 * @param clientId
	 * @param viewId (uri)
	 * @param width - display width 
	 * @param height - display height
	 */
	WSSessionData(String viewId) {
		super();
        
		this.m_viewId = viewId;
		this.m_cw = ApplicationConstants.sw;// -1; //Content size - SHOULD NOT BE USED DUE TO CONTSTANTING SCREEN SIZES AND SCREENSHOTS 
		this.m_ch = ApplicationConstants.sh; //Content Size - - SHOULD NOT BE USED DUE TO CONTSTANTING SCREEN SIZES AND SCREENSHOTS 
		this.m_StartTime = CCServices.getGMTDateTimeAsString();
		this.setScrollData(new ArrayList<WSScrollData>());
		this.m_touches = new ArrayList<WSInnerTouchData>();
		this.m_clicks = new ArrayList<Click>();
		this.m_viewParts = new ArrayList<WSViewAreaData>();
		this.m_sessionId = CCServices.getTimeinMilliseconds();	
		
		
		
	}

	protected void startCheckScroll(Activity activity) {
		if (!ApplicationConstants.disableScrollCheck){
			ScrollView scrollView = CCServices.findScrollView(activity);
			if (scrollView != null){
				checkScroll = new AsyncTaskCheckScroll();
				checkScroll.execute(new AsyncTaskCheckScrollInput(scrollView,this));
			}
		}
	}
	
	WSSessionData(String viewId, int cw,int ch,String startTime,String endTime, String sessionId) 
	{
		super();
        
		this.m_viewId = viewId;
		this.m_cw = cw; 
		this.m_ch = ch; 
		this.m_StartTime = startTime;
		this.m_EndTime = endTime;
		this.m_sessionId = sessionId;
		this.setScrollData(new ArrayList<WSScrollData>());
		this.m_touches = new ArrayList<WSInnerTouchData>();
		this.m_viewParts = new ArrayList<WSViewAreaData>();
		this.m_clicks = new ArrayList<Click>();
	}



	/**
	 * Add new touch data to session data
	 * @param leftOffset
	 * @param topOffest
	 * @param time
	 */
	Boolean addInitialTouch() {
			getTouches().add(getInitialTouch());
			setInitialTouch(null);
			return true;
		
	
	}


	/**
	 * 
	 * Add new scroll data to session data
	 * @param scrollX
	 * @param scrollY
	 */
	Boolean addSwipe(View view, MotionEvent event) {
		
		InternalLog.d(TAG, "Adding Scroll");
		
		WSInnerTouchData initialTouch = getInitialTouch();
		WSInnerTouchData currentTouch = new WSInnerTouchData(view,event, m_sessionId);
		String swipeId = CCServices.getTimeinMilliseconds();
		
		if (initialTouch != null && currentTouch != null && 
		currentTouch.getX() != 0 && currentTouch.getY() != 0
			&& initialTouch.getX() != 0 && initialTouch.getY() != 0)
		{
			//setting the link to the initial touch as we are dealing  
			initialTouch.setScrollId(swipeId);
			InternalLog.d(TAG, "Adding Initial Touch, Scroll Id " + swipeId);
			getTouches().add(initialTouch);
			
			//Saving current touch
			
			
			currentTouch.setScrollId(swipeId);
			InternalLog.d(TAG, "Adding Touch, Scroll Id " + swipeId);
			getTouches().add(currentTouch);
			
			getScrollData().add(new WSScrollData(initialTouch, currentTouch));
			
			setInitialTouch(null);
			return true;
		}
		else{
			InternalLog.d(TAG, " Adding scroll is failed - no initial touch");
			return false;
		}
	}

	
	/**
	 * @param m_actionId the m_actionId to set
	 */
	void set_actionId(int m_actionId) {
		this.m_actionId = m_actionId;
	}

	/**
	 * @return the m_actionId
	 */
	int get_actionId() {
		return m_actionId;
	}
	/**
	 * Finished session: sets endTime, state and closes the thread 
	*/
	protected boolean finish()
	{
		try{
			this.setClosingSessionTime(CCServices.getGMTDateTimeAsString());
			this.setState(OperationState.Finished);
			if (checkScroll != null){
				checkScroll.cancel(true);
			}
			return true;
			
		} catch (Exception e) {
			InternalLog.e(TAG, "Unexpected Error in finishSession");
			return false;
		}
		
	}
	
	/**
	 * Convert Init Data to JsonObject
	 * si :                              //session info structure 
		{
		        uri:"signsWiki"   // - string- concrete view ID
		        ch: 100           //client height 
		        cw:               //client width
		        ss:               // -date - session start -  user has started to use the app view 
		                          //"EEE, dd MMM yyyy HH:mm:ss GMT"
		        sc:               // -date - session closing -  user has ed to use the app view 
		        td: [],           //touch details array
		        sd: [],           //scroll details array
		}
	 * @param initData
	 * @return
	 */
	public JSONObject ConvertToJson() {

		JSONObject json = new JSONObject();
		
		try {
			json.put("uri", m_viewId);
			json.put("ch", getCh()); 
			json.put("cw", getCw());
			json.put("ss", m_StartTime);
			json.put("sc", (m_EndTime != null)? m_EndTime : m_StartTime);
			
			CCServices.buildJsonFromArray(json,this.m_touches,"tda");
			CCServices.buildJsonFromArray(json,this.m_scrollData,"sda");
			CCServices.buildJsonFromArray(json,this.m_viewParts,"vwa");
			CCServices.buildJsonFromArray(json,this.m_clicks,"clks");
			
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return json;
	}//convert to json

	
	/**
	 * @param initSessionTime the initSessionTime to set
	 */
	public void setInitSessionTime(String initSessionTime) {
		this.m_StartTime = initSessionTime;
	}

	/**
	 * @return the initSessionTime
	 */
	public String getInitSessionTime() {
		return m_StartTime;
	}

	/**
	 *  Set time when using screen session is ended 
	 * @param closingSessionTime the closingSessionTime to set
	 */
	public void setClosingSessionTime(String closingSessionTime) {
		
		m_lastView.setSessionEnd(closingSessionTime);
		this.m_EndTime = closingSessionTime;
		
	}

	
	/**
	 * @return
	 */
	public String getViewId() {
		
		return this.m_viewId;
	}

	
	/**
	 * @return
	 */
	public String getSessionStart() {
		
		return this.m_StartTime;
	}

	/**
	 * @return
	 */
	public String getSessionEnd() {
		
		return m_EndTime;
	}
	

	public void setInitialTouch(WSInnerTouchData initialTouch) {
		this.m_initialTouch = initialTouch;
	}

	public WSInnerTouchData getInitialTouch() {
		return m_initialTouch;
	}

	public void setTouches(ArrayList<WSInnerTouchData> m_touches) {
		this.m_touches = m_touches;
	}
	

	public ArrayList<WSInnerTouchData> getTouches() {
		return m_touches;
	}
	
	public void setSessionId(String m_sessionId) {
		this.m_sessionId = m_sessionId;
	}

	public String getSessionId() {
		return m_sessionId;
	}
    
	/**
	 * 
	 * Add new viewPart to session data, return true if added
	 * @param view
	 */
	public boolean addViewPart(View view) {
		
		int offsetX = view.getScrollX();
		int offsetY = view.getScrollY();
		//if one of the offsets changed, then add new view part   
		if (this.m_lastView == null || m_lastView.getViewPartLeft() != offsetX || m_lastView.getViewPartTop() != offsetY) 
		{
			InternalLog.d(TAG, "offsetX: "+ offsetX +", offsetY: " + offsetY);
			int orientation =  CCServices.getDeviceOrientation(view.getContext());
			String sessionEnd = CCServices.getGMTDateTimeAsString();
			
			WSViewAreaData currentViewpart = new WSViewAreaData(offsetX, offsetY, orientation, sessionEnd);
		    getViewParts().add(currentViewpart);
		    if (m_lastView != null){
		        m_lastView.setSessionEnd(sessionEnd);
		    }
		    this.m_lastView = currentViewpart;
		    return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean addClick(Click click){
		
		this.m_clicks.add(click);
		
		return true;
	}
		

	public void setViewParts(ArrayList<WSViewAreaData> m_viewParts) {
		this.m_viewParts = m_viewParts;
	}

	public ArrayList<WSViewAreaData> getViewParts() {
		return m_viewParts;
	}

	public void setScrollData(ArrayList<WSScrollData> m_scrollData) {
		this.m_scrollData = m_scrollData;
	}

	public ArrayList<WSScrollData> getScrollData() {
		return m_scrollData;
	}

	public void setState(FingerPrintService.OperationState m_state) {
		this.m_state = m_state;
	}

	public FingerPrintService.OperationState getState() {
		return m_state;
	}

	public void setCw(int m_cw) {
		this.m_cw = m_cw;
	}

	public int getCw() {
		return m_cw;
	}

	public void setCh(int m_ch) {
		this.m_ch = m_ch;
	}

	public int getCh() {
		return m_ch;
	}

	public void setClicks(ArrayList<Click> m_clicks) {
		this.m_clicks = m_clicks;
	}

	public ArrayList<Click> getClicks() {
		return m_clicks;
	}

}
