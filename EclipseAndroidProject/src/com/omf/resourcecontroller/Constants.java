package com.omf.resourcecontroller;

public interface Constants {
	// For Stephan's tests
	//public static final String SERVER = "nitlab.inf.uth.gr";
	//public static final String SERVICE_NAME = null;
	public static final String SERVER = "82.130.103.8";
	public static final String SERVICE_NAME = "pubsub.82.130.103.8";
	
	// For Paolo's (and probably also Abdullah's) tests
	//public static final String SERVER = "172.30.42.162";
	//public static final String SERVICE_NAME = null;

	public static final int PORT = 5222;
	
	public static final int MESSAGE_READ = 10;
	public static final int MESSAGE_CONNECTION_SUCCESS = 20;
	public static final int MESSAGE_CONNECTION_FAILED = 30;
}
