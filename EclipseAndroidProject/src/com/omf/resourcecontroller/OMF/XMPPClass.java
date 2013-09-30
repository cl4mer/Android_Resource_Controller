/* Contains contributions that are copyright (c) 2013 ETH Zürich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of ETH Zürich nor the names of other contributors 
 *      may be used to endorse or promote products derived from this software 
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT 
 * HOLDERBE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.omf.resourcecontroller.OMF;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.InformXMLMessage;
import com.omf.resourcecontroller.generator.Properties;
import com.omf.resourcecontroller.generator.Properties.KeyType;
import com.omf.resourcecontroller.generator.Properties.MessageType;
import com.omf.resourcecontroller.generator.XMLMessage;
import com.omf.resourcecontroller.parser.XMPPParseError;
import com.omf.resourcecontroller.parser.XMPPParser;

public class XMPPClass {

	public static final String TAG = "XMPPClass";
	
	private AndroidConnectionConfiguration connConfig;
	private PubSubManager pubmgr;
	
	private XMPPConnection xmppConn;
	private XMPPConnectionListener connectionListener;							
	private String username;
	private String password;
	private String mappedResourceId;

	HashMap<String, Subscription> subscriptions;
	LeafNode myHomeNode; 
	String myTopic;
	
	Handler handler;

	public XMPPClass(String username, String password, String rid, Handler handler) {
		this.subscriptions = new HashMap<String, Subscription>();
		this.myTopic = rid;
		this.mappedResourceId = mapResourceId(rid);
		this.username = username;
		this.password = password;
		this.handler = handler;
		this.myHomeNode = null;
	}
	
	private static String mapResourceId(String rid) {
		return "xmpp://" + rid + "@" + Constants.SERVER;
	}
	
	private class ConnectRunnable implements Runnable {

		@Override
		public void run() {
			
			Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION_FAILED, -1, -1, null);
			//Open XMPP Connection
			try {
				Log.i(TAG,"attempting connection");
				xmppConn.connect(); 		
				Log.i(TAG,"connected");
				//Add connection listener
				if(xmppConn.isConnected()){
					Log.i(TAG,"XMPP connected");
					connectionListener = new XMPPConnectionListener();
					xmppConn.addConnectionListener(connectionListener);
					
					xmppLogin(xmppConn, username, password);
					if (subscribeTo(null, myTopic, true) || createTopic(myTopic)) {
						//Add ping manager to deal with disconnections (after 6 minutes idle xmpp disconnects)
						PingManager.getInstanceFor(xmppConn).setPingIntervall(5*60*1000);	//5 minutes (5*60*1000 in millisecons)					
						msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION_SUCCESS, -1, -1, null);
						myHomeNode = subscriptions.get(myTopic).getNode();
					}
				}      				
			} catch(XMPPException e) {				
				Log.e(TAG, "XMPP connection failed",e);				
				xmppConn = null;
			} finally {
				msg.sendToTarget();
			}			
		}		
	}
	

	public void createConnection(Context appContext) {
		//Init aSmack
		SmackAndroid.init(appContext);
		//SmackConfiguration.setDefaultPingInterval(100);	
		// XMPP CONNECTION		
		try {
			connConfig = new AndroidConnectionConfiguration(Constants.SERVER, Constants.PORT);		
			connConfig.setReconnectionAllowed(false);
			connConfig.setSendPresence(true);
			connConfig.setDebuggerEnabled(true);
			
			xmppConn = new XMPPConnection(connConfig);
			
			if (Constants.SERVICE_NAME != null)
				pubmgr = new PubSubManager(xmppConn, Constants.SERVICE_NAME);
			else
				pubmgr = new PubSubManager(xmppConn);
			
			Thread connectionThread = new Thread(new ConnectRunnable());
			connectionThread.start();
		} catch (XMPPException e) {
			 handler.obtainMessage(Constants.MESSAGE_CONNECTION_FAILED, -1, -1, null).sendToTarget();
		}
		
	}

	public void xmppLogin(XMPPConnection xmpp, String username, String pass) throws XMPPException{

		if(xmpp.isConnected()){
			try {
				//1st try
				Log.d(TAG,"performing login with username " + username);
				xmpp.login(username, pass);
				Log.d(TAG,"XMPP Logged in");
			} 
			catch (XMPPException e) {
				Log.e(TAG, "XMPP first login failed");
				Log.d(TAG, "Creating new account");

				if(registerUser(xmpp,username,pass)){
					try {
						xmpp.login(username, pass);						
						Log.i(TAG,"XMPP Logged in");
						//return true;
					} catch (XMPPException e1) {	
						Log.e(TAG,"XMPP Login failed",e);
						//return false;
					}
				}
			}
		}
	}





/*
	private LeafNode findTopic(String topic) {
		LeafNode ret = null;
		try {
			ret = pubmgr.getNode(topic);
		} catch(XMPPException e) {
			Log.e(TAG, "Problem getting node " + topic + ": " + e.getMessage());
			Log.e(TAG, "Error message is " + e.getXMPPError().getMessage());
		} 
		return ret;
	}
*/


	/**
	 * Register a new user in xmpp
	 * @param connection : XMPPConnection object
	 * @param username : String
	 * @param pass : String
	 * @throws XMPPException
	 * @returns boolean: False if registration failed, else true
	 */
	private boolean registerUser(XMPPConnection connection, String username, String  pass) throws XMPPException {
		assert connection != null;
		assert username != null;
		assert pass != null;
		
		AccountManager mgr = connection.getAccountManager();

		if (mgr.supportsAccountCreation ())
			mgr.createAccount (username, pass);
		Log.d(TAG, "Account created for " + username);		
		return true;
	}

	


	private void omfHandler(OMFMessage message) {
		switch (message.getMessageType().getType()) {
		case CREATE:
			Log.d(TAG, "Received <create> message");
			handleCreate(message);
			break;
		case CONFIGURE:
			Log.d(TAG, "Received <configure> message");
			handleConfigure(message);
			break;
		case REQUEST:
			handleRequest(message);
			Log.d(TAG, "Received <request> message");
			break;
		case INFORM:
			Log.d(TAG, "Received <inform> message");
			break;
		case RELEASE:
			handleRelease(message);
			Log.d(TAG, "Received <release> message");
			break;
		}

		return;
	}

	private boolean createTopic(String topic) {
		boolean ret = true;
		
		Log.i(TAG, "creating topic \"" + topic + "\"");
		
		if (xmppConn.isAuthenticated() && !subscriptions.containsKey(topic)) {
			ConfigureForm f = new ConfigureForm(FormType.submit);

			f.setPersistentItems(false);
			f.setPublishModel(PublishModel.open);
			f.setNotifyRetract(false);
			f.setSubscribe(true);

			try {
				LeafNode node = (LeafNode) pubmgr.createNode(topic, f);
				addSubscription(topic, node);
				Log.i(TAG, "created topic \"" + topic + "\"");
			} catch (XMPPException e) {
				/* 409 == conflict (i.e., node already exists) */
				if (e.getXMPPError().getCode() == 409) {
					Log.i(TAG, "Topic \"" + topic + "\" already exists, subscribing");
					ret = subscribeTo(null, topic, true);
				} else {
					Log.e(TAG, "Problem creating topic \"" + topic + "\": " + e.getMessage());
					Log.e(TAG, "Error message is: " + e.getXMPPError().getMessage());
				}
			}
		} else 
			Log.i(TAG, "I am already subscribed to \"" + topic + "\"");
		return ret;
	}

	private void addSubscription(String topic, LeafNode node) {
		ItemEventCoordinator eventListener = new ItemEventCoordinator();
		node.addItemEventListener(eventListener);
		subscriptions.put(topic, new Subscription(topic, node, eventListener));
	}

	private String topicFromMembership(String membership) {
		// Membership string is "xmpp://topic@host".
		return membership.replaceAll("xmpp://([^@]+)@.*", "$1");
	}
	
	private boolean subscribeTo(String cid, String topic, boolean beQuiet) {
		boolean ret = true;
		
		Log.i(TAG, "Subscribing to topic " + topic);
		try {
			LeafNode node = pubmgr.getNode(topic);
			node.subscribe(xmppConn.getUser());
			Log.i(TAG, "Subscribed to topic " + topic);
			addSubscription(topic, node);
		} catch (XMPPException e) {
			Log.e(TAG, "Problem subscribing to topic " + topic + ": " + e.getMessage());
			ret = false;
		}
		
		if (ret && cid != null && !beQuiet)
			publishMembershipInformation(cid, topic);

		return ret;
	}
	
	private void handleConfigure(OMFMessage message) {
		Properties p = message.getProperties();
		
		if (p == null) {
			// Shouldn't happen
			Log.wtf(TAG, "Configure message with null properties?");
			return;
		}
		
		if (p.containsKey("membership")) {
			Log.i(TAG, "Configuring membership");
			String topic = topicFromMembership(p.getValue("membership")); 

			publishMembershipInformation(message.getMessageId(), null);

			if (!subscriptions.containsKey(topic) && !subscribeTo(message.getMessageId(), topic, false))
				Log.e(TAG, "Can't subscribe to " + topic);
		} else if (p.containsKey("state")) {
			if (messageAppliesToMe(message)) {
				String desiredState = p.getValue("state");
				if (desiredState.equalsIgnoreCase("running"))
					startApplication();
			}
		}
	}

	private void startApplication() {
		// TODO Auto-generated method stub
		
	}

	/** Checks the guard inside a &lt;configure&gt; message to see if it applies to this RC.
	 * 
	 * This is only a very simplified version of a &lt;guard&gt; check
	 * @param message the message containing the guard
	 * @return true if the message applies to this RC, false if not
	 */
	private boolean messageAppliesToMe(OMFMessage message) {
		Properties g = message.getGuard();
		
		return g.containsKey("type") && g.getValue("type").equalsIgnoreCase("application")
				&& g.containsKey("name") && g.getValue("name").contains("twimight");
	}

	private String[] getMemberships() {
		String[] ret = new String[subscriptions.size() - 1];
		int i = 0;
		
		for (String s : subscriptions.keySet()) {
			if (!s.equals(myTopic))
				ret[i++] = mapResourceId(s);
		}
		return ret;
	}
	
	private void publishMembershipInformation(String cid, String topic) {
		Log.i(TAG, "Publishing membership info");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.status, cid);
		
		Properties p = new Properties(MessageType.PROPS);
		p.addKey("membership", getMemberships(), KeyType.STRING);
		inform.addProperties(p);

		PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
		subscriptions.get(myTopic).getNode().publish(payload);
		Log.i(TAG, "Membership message published to home node");
		myHomeNode.publish(payload);
		Log.i(TAG, "Membership message published to home node");

		if (topic != null) {
			// Must be fresh
			inform = new InformXMLMessage(mappedResourceId, null, IType.status, cid);
			inform.addProperties(p);
			
			payload = new PayloadItem<InformXMLMessage>(inform);
			subscriptions.get(topic).getNode().publish(payload);
			Log.i(TAG, "Membership message published to newly subscribed node");
		}
	}
	
	private void handleCreate(OMFMessage message) {
		Properties p = message.getProperties();
		String what = p.getValue("type");
		
		if (what != null) {
			
			Log.i(TAG, "Handling <create> message for \"" + what + "\"");
			
			if (what.equalsIgnoreCase("application")) {
				Log.i(TAG, "Creating an application resource.");
				createApplication(message.getMessageId(), p);
			}
		} else
			Log.i(TAG, "No type in <props>, don't know what to do.");
	}

	private void createApplication(String cid, Properties p) {
		String topic = topicFromMembership(p.getValue("membership"));
		subscribeTo(cid, topic, true);
		
		String appResourceId = "twimight";
		if (createTopic(appResourceId))
			publishApplicationCreation(cid, appResourceId, p);
		else
			publishApplicationCreationFailure(cid);
	}

	private void publishApplicationCreationFailure(String cid) {
		Log.i(TAG, "About to publish creation failure message");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.creationFailed, cid);
		myHomeNode.publish(new PayloadItem<InformXMLMessage>(inform));
		Log.i(TAG, "Creation failure message published to home node");
	}

	private void publishApplicationCreation(String cid, String appResourceId, Properties originalProperties) {
		Log.i(TAG, "About to publish creation OK message");
		InformXMLMessage inform = new InformXMLMessage(mappedResourceId, null, IType.creationOk, cid);
		
		Properties p = new Properties(MessageType.PROPS);
		p.setNamespace("application", "http://schema.mytestbed.net/omf/6.0/protocol/application");
		p.addKey("res_id", mapResourceId(appResourceId), KeyType.STRING);
		p.addKey("membership", originalProperties.getValue("membership"), KeyType.STRING);
		inform.addProperties(p);
		
		Log.i(TAG, "Message 2" + inform.toXML());
		PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
		Log.i(TAG, "Payload created");
		try {
			List<Item> items = myHomeNode.getItems();
			Log.i(TAG, "List of items has " + items.size() + " elements");
		} catch (XMPPException e) {
			Log.d(TAG, "Can't get item list for home node");
		}
		myHomeNode.publish(payload);
		Log.i(TAG, "Creation message published to home node");
	}

	private void handleRequest(OMFMessage message) {
		// TODO Auto-generated method stub
		
	}

	private void handleRelease(OMFMessage message) {
		// TODO Auto-generated method stub
		
	}

	public class DisconnectRunnable implements Runnable {

		@Override
		public void run() {
			xmppConn.disconnect();
			xmppConn = null;
			
		}
		
	}
	
	public void destroyConnection() {			
		
		if(xmppConn != null) {
			//remove connection listener
			xmppConn.removeConnectionListener(connectionListener);
			//destroy all topics and remove their listeners
			destroyTopics();
			new Thread(new DisconnectRunnable()).start();
		}

		

	}

	public void destroySingleTopic(String topic){
		Subscription s = subscriptions.get(topic);
		LeafNode node = s.getNode(); 
		ItemEventCoordinator nodeListener = s.getCoordinator();
		node.removeItemEventListener(nodeListener);
		subscriptions.remove(topic);
	}

	public void destroyTopics() {
		
		for (String topic : subscriptions.keySet()) {
			Subscription s = subscriptions.get(topic);
			LeafNode node = s.getNode();
			ItemEventCoordinator nodeListener = s.getCoordinator();
			node.removeItemEventListener(nodeListener);
			try {
				pubmgr.deleteNode(topic);
			} catch (XMPPException e) {
				Log.e(TAG, "Node deletion problem");				
			} catch (IllegalStateException e) {
				Log.e(TAG, "Node deletion problem",e);				
			}
		}
		
		subscriptions.clear();
	}


	/** Item Listener.
	 * 
	 * @author Polychronis
	 */
	class ItemEventCoordinator implements ItemEventListener<PayloadItem<XMLMessage>> {
		private static final int nRecentMessageIDs = 10;

		private String[] recentMessageIDs;
		private int in;
		private boolean isProxy;
		
		public ItemEventCoordinator() {
			this.recentMessageIDs = new String[nRecentMessageIDs];

			this.in = 0;
			for (int j = 0; j < recentMessageIDs.length; j++)
				this.recentMessageIDs[j] = null; 
		}

		private boolean isNewId(String messageId) {
			boolean ret = true;
			for (int i = 0; i < recentMessageIDs.length; i++) {	
				if (messageId.equals(recentMessageIDs[i]))	{
					ret = false;
					break;
				}
			}

			return ret;
		}
	
		private void addMessageId(String messageId) {
			recentMessageIDs[in] = messageId;
			in = (in + 1) % recentMessageIDs.length;
		}

		@Override
		public void handlePublishedItems(ItemPublishEvent<PayloadItem<XMLMessage>> items) {
			XMPPParser parser = new XMPPParser();
			
			for(PayloadItem<XMLMessage> item : items.getItems()) {
				if (!items.isDelayed()) {
					try {
						Log.d(TAG, "Calling parser");
						OMFMessage omfMessage = parser.XMLParse(item.toXML());

						assert !omfMessage.isEmpty();
						
						if (!sentByMe(omfMessage) && isNewId(omfMessage.getMessageId()))	{
							Log.d(TAG, "Message ID " + omfMessage.getMessageId() + " is new");
							
							addMessageId(omfMessage.getMessageId());
							
							if (isProxy)
								System.out.println("This is a resource proxy");
							else
								omfHandler(omfMessage);
							
							System.out.println(omfMessage.toString());
						} else 
							Log.d(TAG, "Message ID " + omfMessage.getMessageId() + " is a duplicate");
					} catch (XmlPullParserException e) {
						Log.e(TAG, "PullParser exception: " + e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, "IO exception");
					} catch (XMPPParseError e) {
						Log.e(TAG, "XMPP parse error: " + e.getMessage());						
					}
				}
			}  
		}

		private boolean sentByMe(OMFMessage omfMessage) {
			return omfMessage.getSrc().equalsIgnoreCase(xmppConn.getUser());
		}
	}

	class XMPPConnectionListener implements ConnectionListener {
		private static final String TAG = "SMACK";
		
		public void connectionClosed() {
			Log.d(TAG, "Connection closed ()");
		}

		public void connectionClosedOnError(Exception e) {
			Log.d(TAG, "Connection closed due to an exception");
			e.printStackTrace();
		}

		public void reconnectionFailed(Exception e) {
			Log.d(TAG, "Reconnection failed due to an exception");
			e.printStackTrace();
		}

		public void reconnectionSuccessful() {
			Log.d("SMACK","Connection reconnected");
			if(!xmppConn.isAuthenticated()){
				try {
					xmppLogin(xmppConn,username,password);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public void reconnectingIn(int seconds) {
			Log.d(TAG, "Connection will reconnect in " + seconds);
		}
	}


	public void resubscribe() {
		// TODO Auto-generated method stub
		
	}
	
}

