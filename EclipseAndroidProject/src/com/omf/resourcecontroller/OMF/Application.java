package com.omf.resourcecontroller.OMF;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.InformXMLMessage;
import com.omf.resourcecontroller.generator.Properties;
import com.omf.resourcecontroller.generator.Properties.KeyType;
import com.omf.resourcecontroller.generator.Properties.MessageType;

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

	private boolean isGood;
	
	private enum AppState {
		STATE_INIT,
		STATE_RUNNING,
		STATE_EXITING,
		STATE_EXITED
	};
	
	private class AppRunnable extends Thread {
		private AppState appState;
		private int seq;
		
		public AppRunnable() {
			 this.appState = AppState.STATE_INIT;
			 this.seq = 1;
		}
		
		public AppState getAppState() {
			return this.appState;
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
			int i = 0;
			while (appState != AppState.STATE_EXITING && i < 5) {
				try {
					Thread.sleep(1000);
					signalStdout("Hi, this is an app, message " + i + "\n");
				} catch (InterruptedException e) {
					; // Empty
				}
				i++;
			}
			
			appState = AppState.STATE_EXITING;
			
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
			signalState();
		}
		
		public void startApp() {
			synchronized (this) {
				appState = AppState.STATE_RUNNING;
				this.notify();
			}
			signalState();
			this.start();
		}
		
		public void stopApp() {
			appState = AppState.STATE_EXITING;
			this.notify();
			signalState();
		}
		
		private synchronized void signalStdout(String msg) {
			Log.i(TAG, "About to publish state change message");
			InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.status, null);
			
			Properties p = new Properties(MessageType.PROPS);
			p.addKey("status_type", "APP_EVENT", KeyType.STRING);
			p.addKey("event", "STDOUT", KeyType.STRING);
			p.addKey("msg", msg, KeyType.STRING);
			p.addKey("seq", Integer.toString(seq), KeyType.INTEGER);
			p.addKey("uid", resourceId, KeyType.STRING);
			p.addKey("hrn", appName, KeyType.STRING);
			p.addKey("app", appName, KeyType.STRING);
			inform.addProperties(p);
			
			PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
			membershipNode.publish(payload);
			Log.i(TAG, "State change message published");
			
			seq++;		
		}
		
		private synchronized void signalState() {
			Log.i(TAG, "About to publish state change message");
			InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.status, null);
			
			Properties p = new Properties(MessageType.PROPS);
			p.addKey("status_type", "APP_EVENT", KeyType.STRING);
			p.addKey("event", stateToString(appState), KeyType.STRING);
			p.addKey("msg", appName, KeyType.STRING);
			p.addKey("seq", Integer.toString(seq), KeyType.INTEGER);
			p.addKey("uid", resourceId, KeyType.STRING);
			p.addKey("hrn", appName, KeyType.STRING);
			p.addKey("app", appName, KeyType.STRING);
			
			if (appState == AppState.STATE_EXITED) {
				p.addKey("exit_code", "0", KeyType.INTEGER);
				p.addKey("state", "stopped", KeyType.SYMBOL);
			}
			
			inform.addProperties(p);
			
			PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
			membershipNode.publish(payload);
			Log.i(TAG, "State change message published");
			
			seq++;
		}
		
		private String stateToString(AppState newState) {
			switch (newState) {
			case STATE_INIT: return "STOPPED";
			case STATE_RUNNING: return "STARTED";
			case STATE_EXITING: return "EXITING";
			case STATE_EXITED: return "EXIT";
			}
			return null;
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
		Log.i(TAG, "Creation.OK message published");
	}

	public void startApp() {		
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
		Properties p = message.getProperties();
		
		if (appliesToMe(message)) {
			if (p.containsKey("state")) {
				AppState appState = app.getAppState();
				String newState = p.getValue("state");
				
				if (newState.equalsIgnoreCase("running")) {
					if (appState == AppState.STATE_INIT)
						startApp();
					// potential else-ifs here
				}
			}
			
			// Give feedback even if state has not changed.
			//app.signalState();
		}
	}
	
	private boolean appliesToMe(OMFMessage message) {
		Properties g = message.getGuard();
		
		return g.containsKey("type") && g.getValue("type").equalsIgnoreCase("application")
				&& g.containsKey("name") && g.getValue("name").equalsIgnoreCase(appName);
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
