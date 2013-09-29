package com.appreadygo.sdk;

import org.json.JSONObject;

/**
 * 
 * @author Mobillify
 *
 */
class WSViewAreaData implements IWSJsonConvert{
	
    private final static String TAG = WSViewAreaData.class.getSimpleName();
		
	public WSViewAreaData(String sessionStart, String sessionEnd, int viewPartLeft, int viewPartTop, int orientation) {
		super();
		this.sd = sessionStart;
		this.fd = sessionEnd;
		this.sl = viewPartLeft;
		this.st = viewPartTop;
		this.o = orientation;
		
	}
	
	public WSViewAreaData(int viewPartLeft, int viewPartTop, int orientation, String timestamp) {
		this.sd = timestamp;
		this.fd = timestamp;
		this.sl = viewPartLeft;
		this.st = viewPartTop;
		this.o = orientation;
		
		
	}

	public String getSessionStart() {
		return sd;
	}
	public void setSessionStart(String sessionStart) {
		this.sd = sessionStart;
	}
	public String getSessionEnd() {
		return fd;
	}
	public void setSessionEnd(String sessionEnd) {
		this.fd = sessionEnd;
	}
	public int getViewPartLeft() {
		return sl;
	}
	public void setViewPartLeft(int viewPartLeft) {
		this.sl = viewPartLeft;
	}
	public int getViewPartTop() {
		return st;
	}
	public void setViewPartTop(int viewPartTop) {
		this.st = viewPartTop;
	}

	
	public int getOrientation() {
		return o;
	}
	
	String sd; //(UTC date in string format "Tue, 24 May 2011 19:44:58 GMT"), - start date of the view part.
	String fd; //(UTC date in string format "Tue, 24 May 2011 19:44:58 GMT"), - finish date of the view part.
	int sl;		// - scroll position from left bound.
	int st;		// - scroll position from top bound.
	int o;  	// - orientation

	@Override
	public JSONObject ConvertToJson() {
        
		JSONObject json = new JSONObject();
		
		try{
			json.put("cx", this.sl);
			json.put("cy", this.st);
			json.put("fd", this.fd);
			json.put("sd", this.sd);
			json.put("o", this.o);
		}
		catch (Exception e) {
			InternalLog.d(TAG, "Wrong JSON");
		}
		return json;
	}


	
}
