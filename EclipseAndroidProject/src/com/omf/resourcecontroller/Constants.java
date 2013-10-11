package com.omf.resourcecontroller;

public interface Constants {
	// For Stephan's tests	
	public static final String SERVICE_NAME = null;
	public static final String SERVER = "82.130.103.8";
	//public static final String SERVICE_NAME = "pubsub.82.130.103.8";
	
	// For Paolo's (and probably also Abdullah's) tests
	//public static final String SERVER = "172.30.42.162";
	//public static final String SERVICE_NAME = null;

	public static final int PORT = 5222;
	
	//messages sent to the external app
	static final int MSG_START_APP = 1;		
	static final int MSG_START_DIS_MODE = 2;	
	static final int MSG_STOP_DIS_MODE = 3;	
	static final int MSG_REGISTER_CLIENT = 4;   
	static final int MSG_UNREGISTER_CLIENT = 5;
	
	//messages received from the external app
	public static final int MESSAGE_COUNTER_UPDATE= 21;
	
	//messages for the xmpp helper class
	public static final int MESSAGE_READ = 11;
	public static final int MESSAGE_CONNECTION_SUCCESS = 12;
	public static final int MESSAGE_CONNECTION_FAILED = 13;	
	public static final int MESSAGE_START_THIRDPARTY_APP = 14;
	public static final int MESSAGE_THIRDPARTY_APP_DATA = 15;
}
