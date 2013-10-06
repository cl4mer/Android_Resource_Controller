package com.omf.resourcecontroller;


import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.XMPPClass;

public class BackgroundService extends Service {

	public static final String TAG = "BackgroundService";
	private static final String WAKE_LOCK = "backgroundServiceWakeLock";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager  telephonyMgr = null;
	private static WakeLock wakeLock;
	
	
	XMPPClass xmppHelper = null;
		
	//OMF message object
	OMFMessage omfMessage = null;
	
	//Username & password
	private String uNamePass = null;

	//TopicName
	private String topicName = null;

	static final String ACTION_OMF_REMOTE_SERVICE = "OMFBackgroundService";	


	Messenger externalMessenger = null;

	/** Flag indicating whether we have called bind on the service. */
	boolean mBound;   


	@Override
	public void onCreate() {
		super.onCreate();	

		Log.i(TAG, "onCreate()");
		
		notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		telephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);;
		topicName = telephonyMgr.getDeviceId();

		uNamePass = "android.omf." + topicName;
		xmppHelper  = new XMPPClass(uNamePass, uNamePass, topicName,  xmppHandler);			
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		xmppHelper.createConnection(getApplicationContext());
		bindToRemoteOmfService();
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if(xmppHelper  != null)
			xmppHelper.destroyConnection();
		xmppHelper  = null;
		
		disableDeliveredMessenger();
		unbindService();			
		displayNotificationMessage("XMPP stopped");
		
		Log.i(TAG,"XMPP stopped");
	}


	private void unbindService() {
		if (mBound) {
			unbindService(mConnection);
		}		
	}
	
	private void disableDeliveredMessenger() {
		if (externalMessenger != null) {
			try {
				Message msg = Message.obtain(null,Constants.MSG_UNREGISTER_CLIENT);
				msg.replyTo = localMessenger;
				externalMessenger.send(msg);
			} catch (RemoteException e) {
				// There is nothing special we need to do if the service
				// has crashed.
			}
			
		}
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
     * Handler of incoming messages from service.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	
            switch (msg.what) {
                case Constants.MESSAGE_COUNTER_UPDATE:    
                	Log.i(TAG, "counter update received,value = " + msg.arg1);
                	int counter = msg.arg1;
                	xmppHelper.counterUpdate(counter);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger localMessenger= new Messenger(new IncomingHandler());
	
	/**
	 *  The Handler that gets information back from the XMPP class
	 */
	private final Handler xmppHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {          

			case Constants.MESSAGE_READ:  				
				processMessage(msg.obj);					
				break; 	
			case Constants.MESSAGE_CONNECTION_SUCCESS: 	
				Log.i(TAG,"connection success received");
				//bindToRemoteOmfService();				
				break; 	
			case Constants.MESSAGE_CONNECTION_FAILED:  				
				Log.i(TAG,"Message connection failed received");
				handleFailure();				
				break; 	
			case Constants.MESSAGE_START_THIRDPARTY_APP:  				
				bindToRemoteOmfService();
				break; 	
			case Constants.MESSAGE_THIRDPARTY_APP_DATA:  				
				//sendMessage();
				break; 			
			}			
		}

	};

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) { 		
			
			externalMessenger = new Messenger(service);			
			mBound = true;
			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,Constants.MSG_REGISTER_CLIENT);
				msg.replyTo = localMessenger;
				Log.i(TAG,"sending local Messenger");
				externalMessenger.send(msg);
				
			} catch (RemoteException e) {
				
			}
			sendMessage(Constants.MSG_START_APP);			
		}

        public void onServiceDisconnected(ComponentName className) {            
        	externalMessenger = null;
        	mBound = false;
        }
    };

    public void sendMessage(int message) {
    	if (!mBound || externalMessenger == null) return;        
    	Message msg = Message.obtain(null, message);
    	try {
    		externalMessenger.send(msg);
    		Log.i(TAG,"Message sent to Twimight");
    	} catch (RemoteException e) {    		
    	}
    }  


    protected void processMessage(Object obj) {
    	Log.i(TAG, "Got message!");
    }

    protected void bindToRemoteOmfService() {
    	Intent intent = new Intent(ACTION_OMF_REMOTE_SERVICE);
    	bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

  	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void resubscribe() {
		xmppHelper.resubscribe();
	}

	protected void handleFailure() {
		// TODO Auto-generated method stub	

	}
	
	/**
	 * Acquire the Wake Lock
	 * @param context
	 */
	public static void getWakeLock(Context context){

		releaseWakeLock();

		PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , WAKE_LOCK); 
		wakeLock.acquire();
	}

	/**
	 * We have to make sure to release the wake lock after the TDSThread is done!
	 * @param context
	 */
	public static void releaseWakeLock(){
		if(wakeLock != null)
			if(wakeLock.isHeld())
				wakeLock.release();
	}
}
