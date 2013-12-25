package com.appreadygo.sdk;

public class ApplicationData {
	
	 private int taskId;
	 private String appId;
     private String appName;
     private int userId;
     private String packageName;
     
     public ApplicationData(Integer taskId,String appId, String appName,Integer userId, String packageName) {

		 this.taskId = taskId;
		 this.appId = appId;
		 this.appName = appName;
		 this.userId = userId;
		 this.packageName = packageName;
	}
	
	public Integer getTaskId() {
 		return taskId;
 	}

	public String getAppId() {
		return appId;
	}


	public String getAppName() {
		return appName;
	}


	public int getUserId() {
		return userId;
	}


	public String getPackageName() {
		return packageName;
	}
     
}
