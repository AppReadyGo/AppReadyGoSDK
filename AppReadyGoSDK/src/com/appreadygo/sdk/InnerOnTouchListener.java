package com.appreadygo.sdk;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class InnerOnTouchListener implements OnTouchListener{ 
	
	private static final String TAG = InnerOnTouchListener.class.getSimpleName();
	
    @Override 
    public boolean onTouch(View view, MotionEvent event) { 
        InternalLog.d(TAG,"Proceeding through inner touch");
		FingerPrint.onTouch(view, event);
		return false; 
    }
}
