package com.appreadygo.sdk;

class ApplicationConstants {
	
	/*
	 * Corresponding to properties file
	 * 0 - Local
	 * 1 - Server
	 * 2 - QA 
	 * 3 - Local2
	 * 4 - QA2
	 */
	
	enum TargetEnvironment
	{
		Server,
		Local,
		QA,
		Local2,
		QA2
	}
	/**
	 * 
	 */
	public static boolean fingerPrintOn = false;
	
	/**
	 * represent batching mode 
	 */
	public static boolean cacheInDatabase = false;
	
	/**
	 * 
	 */
	public static boolean initialized = false;
	
	/**
	 * Screen Height
	 */
	public static int sh = 0; 
	
	/**
	 * Screen Width
	 */
	public static int sw = 0; 
	
	/**
	 * client id, the id will be provided to our clients after registration process.
	 */
	public static String cid = "0";   
	
	/**
	 * task id, the id of the task client requested
	 */
	public static int task = 0;   
	
	/**
	 * username, username of the tester
	 */
	public static String username = "empty";
	
	/**
	 * 
	 */
	public static WSSystemInfoData systemInfo;

	/**
	 * 
	 */
	public final static int rowsToFetch = 150;
	
	/**
	 * 
	 */
	public final static int maxRowsInTable = 100;
	
	/**
	 * 
	 */
	public final static int sendIntervalInSeconds = 60;
	
	/**
	 * 
	 */
	//Minimal amount of data for sending to server
	public static final int minSessionsToSend = 1;
	
	/**
	 * 
	 */
	public static boolean debugMode = false;
	
	/**
	 * 
	 */
	public static boolean sendingToServer = false;
	
	
	/**
	 * this property represent service working mode 
	 * if true - the repost will be sent to server 
	 * if flase - the report will be sent to local IIS server 
	 */
	public static TargetEnvironment targetEnvironmentMode = TargetEnvironment.QA;

	public static boolean allowSendIn3g = false;

	public static boolean allowdebugnow = false;
	
	public static boolean disableScrollCheck = false;

	
}
