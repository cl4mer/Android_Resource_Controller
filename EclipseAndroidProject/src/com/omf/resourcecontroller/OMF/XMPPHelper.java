package com.omf.resourcecontroller.OMF;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;

import android.util.Log;

public class XMPPHelper {
	private static final String TAG = "XMPPHelper";
	
	public static LeafNode subscribeTo(XMPPConnection xmppConn, PubSubManager pubmgr, String topic) {
		LeafNode ret = null;
		
		Log.i(TAG, "Subscribing to topic " + topic);
		synchronized (xmppConn) {
			try {
				ret = pubmgr.getNode(topic);
				ret.subscribe(xmppConn.getUser());
				Log.i(TAG, "Subscribed to topic " + topic);
			} catch (XMPPException e) {
				Log.e(TAG, "Problem subscribing to topic " + topic + ": " + e.getMessage());
			}
		}
		return ret;
	}
	
	public static LeafNode createTopic(XMPPConnection xmppConn, PubSubManager pubmgr, String topic) {
		LeafNode ret = null;
		
		Log.i(TAG, "creating topic \"" + topic + "\"");
		
		synchronized(xmppConn) {
			Log.i(TAG, "in synch block");
			if (xmppConn.isAuthenticated()) {
				ConfigureForm f = new ConfigureForm(FormType.submit);
	
				f.setPersistentItems(false);
				f.setPublishModel(PublishModel.open);
				f.setNotifyRetract(false);
				f.setSubscribe(false);
	
				try {
					ret = (LeafNode) pubmgr.createNode(topic, f);
					Log.i(TAG, "created topic \"" + topic + "\"");
				} catch (XMPPException e) {
					/* 409 == conflict (i.e., node already exists) */
					if (e.getXMPPError().getCode() == 409) {
						Log.i(TAG, "Topic \"" + topic + "\" already exists, subscribing");
						ret = XMPPHelper.subscribeTo(xmppConn, pubmgr, topic); 
					} else {
						Log.e(TAG, "Problem creating topic \"" + topic + "\": " + e.getMessage());
						Log.e(TAG, "Error message is: " + e.getXMPPError().getMessage());
					}
				}
			} else 
				Log.i(TAG, "I am already subscribed to \"" + topic + "\"");
		}
		return ret;
	}

	public static String mapResource(String resource, String server) {
		return "xmpp://" + resource + "@" + server;
	}

}
