package com.appreadygo.sdk;

import org.json.JSONObject;

public class Click implements IWSJsonConvert{
	
	private String controlTag;
	private String timestamp;
	
	public Click(String controlTag) {
		
		this.controlTag = controlTag;
		this.timestamp = CCServices.getGMTDateTimeAsString();
	}

	public Click(String controlTag, String clickTimestamp) {
		
		this.controlTag = controlTag;
		this.timestamp =  clickTimestamp;
	}

	public String getControlTag() {
		return controlTag;
	}

	@Override
	public JSONObject ConvertToJson() {
		JSONObject json = new JSONObject();
		try{
			json.put("tag", this.controlTag);
			json.put("dc", this.timestamp);
		}
		catch (Exception e) {
			InternalLog.d(controlTag, "Wrong JSON");
		}
		return json;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}
	
}
