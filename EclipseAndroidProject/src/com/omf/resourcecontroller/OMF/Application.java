package com.omf.resourcecontroller.OMF;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.InformXMLMessage;
import com.omf.resourcecontroller.generator.Properties;
import com.omf.resourcecontroller.generator.Properties.KeyType;
import com.omf.resourcecontroller.generator.Properties.MessageType;
import com.omf.resourcecontroller.generator.XMLMessage;

public class Application implements OMFMessageHandler {
	private static final String TAG = "Application";
	
	private String appName;
	private LeafNode homeNode;
	private LeafNode membershipNode;
	/*
	private XMPPConnection conn;
	private PubSubManager pubmgr;
	*/
	private String resourceId;
	private String mappedResourceId;
	private AppRunnable app;

	private int seq;

	private boolean isGood;
	
	private enum AppState {
		STATE_INIT,
		STATE_RUNNING,
		STATE_EXITING,
		STATE_EXITED
	};
	
	private class AppRunnable extends Thread {
		private AppState appState;
		
		public AppRunnable() {
			 appState = AppState.STATE_INIT;
		}
		
		@Override
		public void run() {
			// Wait until RUNNING or EXITING
			synchronized(this) {
				while (appState != AppState.STATE_RUNNING && appState != AppState.STATE_EXITING) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						; // Empty
					}
				}
			}
			
			// Now RUNNING or EXITING
			// If RUNNING, the thread does its thing, checking appState occasionally
			if (appState != AppState.STATE_EXITING) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					; // Empty
				}
			}
			if (appState != AppState.STATE_EXITING)
				signalStdout("Hi, this is an app\n");
			
			// Wait until EXITING
			synchronized(this) {
				while (appState != AppState.STATE_EXITING) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						; // Empty
					}
				}
			}
			
			appState = AppState.STATE_EXITED;
			signalStateChange(appState);
		}
		
		public void startApp() {
			appState = AppState.STATE_RUNNING;
			this.notify();
			signalStateChange(appState);
		}
		
		public void stopApp() {
			appState = AppState.STATE_EXITING;
			this.notify();
			signalStateChange(appState);
		}
	}
	

	public Application(XMPPConnection conn, PubSubManager pubmgr, String appName, String resourceId, String membership) {
		super();
		this.appName = appName;
		this.resourceId = resourceId;
		this.mappedResourceId = XMPPHelper.mapResource(resourceId, Constants.SERVER);
		
		/*
		this.conn = conn;
		this.pubmgr = pubmgr;
		*/
		this.seq = 1;
		this.app = new AppRunnable();
		
		this.homeNode = XMPPHelper.createTopic(conn, pubmgr, resourceId);
		if (homeNode != null) {
			homeNode.addItemEventListener(new OMFEventCoordinator(this));
			
			this.membershipNode = XMPPHelper.subscribeTo(conn, pubmgr, membership);
			if (membershipNode != null)
				membershipNode.addItemEventListener(new OMFEventCoordinator(this));
		}
		
		if (membershipNode != null && homeNode != null) {
			sendCreationOk(membership);
			this.isGood = true;
		} else
			this.isGood = false;
	}

	public boolean isGood() {
		return isGood;
	}

	private void sendCreationOk(String membership) {
		Log.i(TAG, "About to publish Creation.OK");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.creationOk, null);
				
		Properties p = new Properties(MessageType.PROPS);
		p.addKey("app_id", "", KeyType.STRING);
		p.addKey("state", "stopped", KeyType.SYMBOL);
		p.addKey("res_id", mappedResourceId, KeyType.STRING);
		p.addKey("membership", membership , KeyType.STRING);
		p.addKey("event_sequence", "0", KeyType.INTEGER);
		p.addKey("type", "application", KeyType.SYMBOL);
		inform.addProperties(p);
		
		PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
		membershipNode.publish(payload);
		Log.i(TAG, "State change message published");
		
		seq++;
	}

	private void signalStateChange(AppState newState) {
		Log.i(TAG, "About to publish state change message");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.status, null);
		
		Properties p = new Properties(MessageType.PROPS);
		p.addKey("status_type", "APP_EVENT", KeyType.STRING);
		p.addKey("event", stateToString(newState), KeyType.STRING);
		p.addKey("msg", appName, KeyType.STRING);
		p.addKey("seq", Integer.toString(seq), KeyType.INTEGER);
		p.addKey("uid", resourceId, KeyType.STRING);
		p.addKey("hrn", appName, KeyType.STRING);
		inform.addProperties(p);
		
		PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
		homeNode.publish(payload);
		Log.i(TAG, "State change message published");
		
		seq++;
	}
	
	private static String stateToString(AppState newState) {
		switch (newState) {
		case STATE_INIT: return "STOPPED";
		case STATE_RUNNING: return "STARTED";
		case STATE_EXITING: return "EXITING";
		case STATE_EXITED: return "EXITED";
		}
		return null;
	}

	private void signalStdout(String msg) {
		Log.i(TAG, "About to publish state change message");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.status, null);
		
		Properties p = new Properties(MessageType.PROPS);
		p.addKey("status_type", "APP_EVENT", KeyType.STRING);
		p.addKey("event", "STDOUT", KeyType.STRING);
		p.addKey("msg", msg, KeyType.STRING);
		p.addKey("seq", Integer.toString(seq), KeyType.INTEGER);
		p.addKey("uid", resourceId, KeyType.STRING);
		p.addKey("hrn", appName, KeyType.STRING);
		inform.addProperties(p);
		
		PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
		homeNode.publish(payload);
		Log.i(TAG, "State change message published");
		
		seq++;		
	}
	
	public void startApp() {		
		app.start();
		app.startApp();
	}
	
	public void stopApp() {
		app.stopApp();
		
		boolean joined = false;
		
		while (!joined) {
			try {
				app.join();
				joined = true;
			} catch (InterruptedException e) {
				; // empty
			}
		}
	}

	public void handleConfigure(OMFMessage message) {
		
	}
	
	@Override
	public void handle(OMFMessage message) {
		switch (message.getMessageType().getType()) {
		case CREATE:
			Log.d(TAG, "Received <create> message (can't handle, so I'm not doing anything about it)");
			break;
		case CONFIGURE:
			Log.d(TAG, "Received <configure> messages");
			handleConfigure(message);
			break;
		case REQUEST:
			Log.d(TAG, "Received <request> message (can't handle, so I'm not doing anything about it)");
			break;
		case INFORM:
			Log.d(TAG, "Received <inform> message (can't handle, so I'm not doing anything about it)");
			break;
		case RELEASE:
			Log.d(TAG, "Received <release> message (can't handle, so I'm not doing anything about it)");
			break;
		}

		return;		
	}
}
