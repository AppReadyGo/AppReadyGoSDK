package com.appreadygo.sdk;


import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * @author Philip
 *
 */
class WSScrollData implements IWSJsonConvert{
		

	// - scroll position from left bound.
	private WSInnerTouchData initialClick;	// - sl
	// - scroll position from top bound.
	private WSInnerTouchData finalClick;	    // - st	
	
    /**
     * Create new scroll 
     * @param sessionStart
     * @param sessionEnd
     * @param scrollLeft
     * @param scrollTop
     */
	WSScrollData(WSInnerTouchData init, WSInnerTouchData closing) {
		this.initialClick = init;
		this.finalClick = closing;	
	}
	

	/**
	 * Convert to JSON object
	 * format 
	 * sd: //scroll details
	 *	{
  	 *		std: // - td- data of first touch in scroll  
  	 *		ctd: // - td- data of last touch in scroll 
	 *	}
	 */
	public JSONObject ConvertToJson(){
		
		JSONObject json = new JSONObject();
		try {
			json.put("std", initialClick.ConvertToJson());
			json.put("ctd", finalClick.ConvertToJson());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
}
