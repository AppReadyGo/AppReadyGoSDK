package com.appreadygo.sdk;



import org.json.JSONObject;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;


class WSInnerTouchData implements IWSJsonConvert{
	
	private final static String TAG = WSInnerTouchData.class.getSimpleName();
	  
	/**
	 * 
	 * @param touchTimestamp
	 * @param coordX
	 * @param coordY
	 * @param press
	 * @param scrollId
	 * @param orientation
	 */
	public WSInnerTouchData(String touchTimestamp, float coordX, float coordY, float press,String scrollId, int orientation) 
	{
		this.X = coordX;
		this.Y = coordY;
		this.mTimestamp = touchTimestamp;
		this.mPressure = press;
		this.mScrollId= scrollId;
		this.mOrientation = orientation;
	}
	
	/**
	 * 
	 * @param view
	 * @param event
	 */
	public WSInnerTouchData(View view, MotionEvent event, String sessionId)
	{
		try
		{
			float leftOffset,topOffest,offsetX,offsetY = 0; 
			float yPrecision, xPresision;

         	if (view instanceof ListView){
			   //PLEASE DO NOT CLEAR THIS CODE. 
         		//IT TAKSE CARE of DYNAMIC LIST VIEW COORDINATES and MAY BE NEEDED IN THE NEAR FUTURE
         		
				/*View viewIn = ((ViewGroup) view).getChildAt(0); 
				
				if (viewIn != null) {
					/*Offset is: all the elements we already scrolled (first visible position) multiplied by height of each element
					 * PLUS (getTop returns -...) the number of pixels of the first visible position entry we don't see
					 * as first visible position returns the first partially visible element  
					 */
				 /*   offsetY =  ((ListView)view).getFirstVisiblePosition() * viewIn.getHeight() - viewIn.getTop();
				}*/
				offsetX = 0;
			}
			else{
			   View tmpView  = findScrollView(view);
			   if (tmpView != null){
				   ScrollView scrollView =  (ScrollView) tmpView;
				   offsetX = scrollView.getScrollX();
				   offsetY = scrollView.getScrollY();
			   }
			   else{
				   offsetX = view.getScrollX();
				   offsetY = view.getScrollY();
			   }
			}
			xPresision =  event.getXPrecision();
			yPrecision =  event.getYPrecision();
			
			leftOffset = (event.getRawX() + offsetX);
			topOffest = (event.getRawY() + offsetY);
			
			if (xPresision != 0) {
				leftOffset = leftOffset / xPresision;
			}
			if (yPrecision != 0) {
				topOffest = topOffest / yPrecision;
			}
			
			mOrientation = Device.getDeviceOrientation(view.getContext());
			X = leftOffset;
			Y = topOffest;
			mPressure = event.getPressure();
			mScreenId = Integer.toString(view.getId());
			mOffsetX = offsetX;
			mOffsetY = offsetY;
			mScrollId = " ";
			mTimestamp = CCServices.getGMTDateTimeAsString();
			mEventTime = event.getEventTime();
		    mSessionId = sessionId;
			InternalLog.d(TAG,"X coordinate is " + leftOffset + " Y coordinate is " + topOffest);	
		}
		catch(Exception e){
		   InternalLog.d (TAG, "Unexpected error " + e.getMessage());
		   return;
		}
	}
   
	/**
	 * go over all menus and find a first scroll menu
	 * @param view
	 * @return
	 */
	private View findScrollView(View view) {
		
		if (view instanceof ScrollView) return view;
		
		View v2 = (View) view.getParent();
		try {
			while (v2!= null ){
			   if (v2 instanceof ScrollView) return v2;
			   v2 = (View)v2.getParent();   
			}//while
			return v2;
		}//try
		catch (Exception e){
			InternalLog.d(TAG,"Scroll view is not found");
			return null;
		}
	}
	
	

	public float getX() {
		return X;
	}

	public float getY() {
		return Y;
	}

	public void setmPressure(float mPressure) {
		this.mPressure = mPressure;
	}

	public float getmPressure() {
		return mPressure;
	}

	public void setmWidth(float mWidth) {
		this.mWidth = mWidth;
	}

	public float getmWidth() {
		return mWidth;
	}

	public String getTimestamp() {
		return mTimestamp;
	}



	public String getSessionId() {
		return mSessionId;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	public void setmApplicationId(String mApplicationId) {
		this.mApplicationId = mApplicationId;
	}

	public String getmApplicationId() {
		return mApplicationId;
	}

	public void setmScreenId(String mScreenId) {
		this.mScreenId = mScreenId;
	}

	public String getmScreenId() {
		return mScreenId;
	}

	public float getmOffsetX() {
		return mOffsetX;
	}

	public void setmOffsetY(float offsetY) {
		this.mOffsetY = offsetY;
	}

	public float getmOffsetY() {
		return mOffsetY;
	}

	public  void setScrollId(String scrollId) {
		this.mScrollId = scrollId;
	}

	public String getScrollId() {
		return mScrollId;
	}
	

	public long getEventTime() {
		return mEventTime;
	}



	@Override
	public JSONObject ConvertToJson() {
        
		JSONObject json = new JSONObject();
		
		try{
			//DecimalFormat df = new DecimalFormat("0.0000");
			json.put("cx", (int)this.X);
			json.put("cy", (int)this.Y);
			json.put("d", this.mTimestamp);
			json.put("p", (int)(this.mPressure*10000));
			json.put("o", this.mOrientation);
		}
		catch (Exception e) {
			InternalLog.d(TAG, "Wrong JSON");
		}
		return json;
	}

	public int getOrientation() {
		return mOrientation;
	}
	

	/*
	 * 
	 */
	private float X;
	/*
	 * 
	 */
	private float Y;
	/*
	 * 
	 */
	private float mPressure;
	/*
	 * 
	 */
	private float mWidth;
	/*
	 * 
	 */
	private String mTimestamp;
	/*
	 * 
	 */
	private String mSessionId;
	/*
	 * 
	 */
	private int sequence;
	/*
	 * 
	 */
	private String mApplicationId;
	/*
	 * 
	 */
	private String mScreenId;
	/*
	 * 
	 */
	private float mOffsetX;
	/*
	 * 
	 */
	private float mOffsetY;
	/*
	 * 
	 */
	private String mScrollId;
	/*
	 * 
	 */
    private int mOrientation;
    
    private long mEventTime;

	
}
