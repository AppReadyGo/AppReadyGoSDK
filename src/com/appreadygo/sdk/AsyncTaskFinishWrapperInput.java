package com.appreadygo.sdk;

import android.content.Context;

class AsyncTaskFinishWrapperInput {
	
	public AsyncTaskFinishWrapperInput(Context ctx, String uri) {
		
		this.ctx = ctx;
		this.uri = uri;
	}

    public Context getCtx() {
		return ctx;
	}

	
	public String getUri() {
		return uri;
	}

	private Context ctx;
	private String uri;

}
