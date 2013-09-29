package com.appreadygo.sdk;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;


//PM : ????
class InnerOnScrollStateChanged implements OnScrollListener {
	
	private final static String TAG = InnerOnScrollStateChanged.class.getSimpleName();
	
	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if (scrollState == SCROLL_STATE_IDLE)
		{
			InternalLog.d (TAG, "Finished Scrolling");
		}
	}

}
