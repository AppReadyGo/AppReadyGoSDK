package com.appreadygo.sdk;

public class ApplicationData {
	
	 private Integer taskId;
	 private String appId;
     private String appName;
     private String username;
     private String packageName;
     
     public ApplicationData(Integer taskId,String appId, String appName,String username, String packageName) {

		 this.taskId = taskId;
		 this.appId = appId;
		 this.appName = appName;
		 this.username = username;
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


	public String getUsername() {
		return username;
	}


	public String getPackageName() {
		return packageName;
	}
     
}
