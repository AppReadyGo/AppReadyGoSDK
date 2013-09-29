package com.appreadygo.sdk;

import android.widget.ScrollView;

class AsyncTaskCheckScrollInput {
	
	public AsyncTaskCheckScrollInput(ScrollView scrollView, WSSessionData sessionData) {
		
		this.scrollView = scrollView;
		this.sessionData = sessionData;
	}

    public ScrollView getScrollView() {
		return this.scrollView;
	}

	private ScrollView scrollView;
	private WSSessionData sessionData;

	public WSSessionData getSession() {
		return sessionData;
	}

}
