package com.omf.resourcecontroller;


import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.XMPPClass;

public class BackgroundService extends Service {

	public static final String TAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager  telephonyMgr = null;
	
	
	XMPPClass xmppHelper = null;
		
	//OMF message object
	OMFMessage omfMessage = null;
	
	//Username & password
	private String uNamePass = null;
	
	//TopicName
	private String topicName = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}	

	@Override
	public void onCreate() {
		super.onCreate();	

		// Notification manager Service
		notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// TelephonyMgr
		telephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);;
		topicName = telephonyMgr.getDeviceId();

		uNamePass = "android.omf."+topicName;

		//MessageIDGenerator.setPrefix(uNamePass);	

		xmppHelper  = new XMPPClass(uNamePass, uNamePass, topicName,  mHandler);
		//connection will be created internally in a separate thread.
		xmppHelper.createConnection(getApplicationContext());
		

	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if(xmppHelper  != null)
			xmppHelper.destroyConnection();
		xmppHelper  = null;
		displayNotificationMessage("XMPP stopped");
		
		Log.i(TAG,"XMPP stopped");
	}
	

	// --- SERVICE CHECK CONTROL USING THE SYSTMEM
	// Check if the service is running
	public static boolean isServiceRunning(Context context, String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
		Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
		while (i.hasNext()) {
			ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i.next();
			if (runningServiceInfo.service.getShortClassName().equals(serviceName)) {
				serviceRunning = true;
			}
		}
		return serviceRunning;
	}


	/**
	 * 
	 * @param message : the message to be displayed as a notification
	 */
	private void displayNotificationMessage(String message) {
		Notification notify = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		notify.icon = R.drawable.resource_controller;

		// The service is not running
		if (!isServiceRunning(getApplicationContext(), ".BackgroundService")) {
			Intent start = new Intent();
			start.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			// Notification that does not redirect to other Activities
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(BackgroundService.this, "Resource Controller", message, contentIntent);
			notificationMgr.notify(R.string.app_notification_id, notify);
		} else {	// The service is running
			Intent start = new Intent(BackgroundService.this, StartUpActivity.class);
			start.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// Notification that redirects to another Activity
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(this, "Resource Controller", message, contentIntent);
			
			notificationMgr.notify(R.string.app_notification_id, notify);
		}
	}
	
	/**
	 *  The Handler that gets information back from the XMPP class
	 */
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {          

			case Constants.MESSAGE_READ:  				
				processMessage(msg.obj);					
				break; 	
			case Constants.MESSAGE_CONNECTION_SUCCESS:  				
				createTopic();
				break; 	
			case Constants.MESSAGE_CONNECTION_FAILED:  				
				handleFailure();
				break; 	
				
			
			}			
		}
	};

	protected void processMessage(Object obj) {
		Log.i(TAG, "Got message!");
	}

	protected void handleFailure() {
		// TODO Auto-generated method stub
		
	}

	protected void createTopic() {
		xmppHelper.createTopic();
		
	}

	
}
