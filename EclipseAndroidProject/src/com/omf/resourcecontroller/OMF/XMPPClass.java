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
import java.util.Iterator;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
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
	private String fullJid;


	HashMap<String, Subscription> subscriptions;
	LeafNode myHomeNode; 
	String myAddress;
	String myTopic;
	
	Handler handler;

	public XMPPClass(String username, String password, String topic, Handler handler) {
		this.subscriptions = new HashMap<String, Subscription>();
		this.myTopic = topic;
		this.username = username;
		this.password = password;
		this.fullJid = null;
		this.handler = handler;

	}
	
	private class ConnectRunnable implements Runnable {

		@Override
		public void run() {
			
			Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION_FAILED, -1, -1, null);
			//Open XMPP Connection
			try{
				Log.i(TAG,"attempting connection");
				xmppConn.connect(); 		
				Log.i(TAG,"connected");
				//Add connection listener
				if(xmppConn.isConnected()){
					Log.i(TAG,"XMPP connected");
					connectionListener = new XMPPConnectionListener();
					xmppConn.addConnectionListener(connectionListener);
					
					//Do Login
					performXMPPLogin(xmppConn, username, password);
					
					Iterator<Presence> i = xmppConn.getRoster().getPresences(username);
					
					while (i.hasNext()) {
						Log.i(TAG, "Presence " + i.next().getFrom());
					}
					Presence p = xmppConn.getRoster().getPresence(username);
					fullJid = p.getFrom();
					
					//Add ping manager to deal with disconnections (after 6 minutes idle xmpp disconnects)
					PingManager.getInstanceFor(xmppConn).setPingIntervall(5*60*1000);	//5 minutes (5*60*1000 in millisecons)					
					msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION_SUCCESS, -1, -1, null);		        	
				}      				
			}catch(XMPPException e){				
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
			xmppConn = new XMPPConnection(connConfig);
			
			Thread connectionThread = new Thread(new ConnectRunnable());
			connectionThread.start();
			
		} catch (XMPPException e) {
			 handler.obtainMessage(Constants.MESSAGE_CONNECTION_FAILED, -1, -1, null).sendToTarget();
		}
		
	}

	public void performXMPPLogin(XMPPConnection xmpp, String username,String pass) throws XMPPException{

		if(xmpp.isConnected()){
			try {
				//1st try
				Log.d(TAG,"performing login");
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






	private LeafNode findTopic(String topic) {
		LeafNode ret = null;
		try {
			ret = pubmgr.getNode(topic);
			/* This won't do anything if the topic already exists. */
			//nodes.put(topic, ret);

		} catch(XMPPException e) {
			Log.e(TAG, "Problem getting node " + topic + "; trying to create");

		} 
		return ret;

	}



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
			break;
		case CONFIGURE:
			Log.d(TAG, "Received <configure> message");
			handleConfigure(message);
			break;
		case REQUEST:
			Log.d(TAG, "Received <request> message");
			break;
		case INFORM:
			Log.d(TAG, "Received <inform> message");
			break;
		case RELEASE:
			Log.d(TAG, "Received <release> message");
			break;
		}

		return;
	}
	
	public void createHomeTopic(String topic) {
		Log.i(TAG, "creating home topic");
		if (topic == null)
			topic = myTopic;
		myHomeNode = createTopic(topic, false);
		myAddress = "xmpp://" + topic + "@" + Constants.SERVER;
		Log.i(TAG, "home topic created");
	}
	
	private LeafNode createTopic(String topic, boolean isProxy) {
		Log.i(TAG, "creating topic \"" + topic + "\"");
		
		if(xmppConn.isAuthenticated()){
			pubmgr = new PubSubManager(xmppConn);
			ItemEventCoordinator eventListener = null;
			LeafNode eventNode = findTopic(topic);		
			
			if (eventNode == null) {
				Log.i(TAG, "topic \"" + topic + "\" not found, creating");
				try {
					ConfigureForm f = new ConfigureForm(FormType.submit);

					f.setPersistentItems(false);
					f.setPublishModel(PublishModel.open);
					f.setNotifyRetract(false);
					f.setSubscribe(true);
					
					Log.d(TAG, "Creating node " + topic);
					eventNode = (LeafNode) pubmgr.createNode(topic, f);
				} catch (XMPPException e) {
					Log.e(TAG, "Problem creating topic " + topic + ": " + e.getMessage());
				}
			} else {
				Log.i(TAG, "topic \"" + topic + "\" found, subscribing");
				
				try {
					org.jivesoftware.smackx.pubsub.Subscription s = eventNode.subscribe(fullJid);
					Log.i(TAG, "subscribed to topic " + topic + " with status " + s.getState());
				} catch (XMPPException e) {
					Log.e(TAG, "Problem subscribing to existing topic " + topic + " with jid " + fullJid + ": " + e.getMessage());
					Log.e(TAG, "XMPP error: " + e.getXMPPError());
				}
			}

			if (eventNode != null) {
				eventListener = new ItemEventCoordinator(isProxy);
				eventNode.addItemEventListener(eventListener);
				
				try {
					eventNode.subscribe(xmppConn.getUser());
					subscriptions.put(topic, new Subscription(topic, eventNode, eventListener));
				} catch (XMPPException e) {
					Log.e(TAG, "Problem subscribing to topic " + topic + ": " + e.getMessage());
				}
			}
			
			return eventNode;
		} else
			return null;
	}

	private String topicFromMembership(String membership) {
		// Membership string is "xmpp://topic@host".
		return membership.replaceAll("xmpp://([^@]+)@.*", "$1");
	}
	
	private void handleConfigure(OMFMessage message) {
		Properties p = message.getProperties();
		
		if (p == null) {
			// Shouldn't happen
			Log.wtf(TAG, "Configure message with null properties?");
			return;
		}
		
		if (p.containsKey("membership")) {
			String topic = topicFromMembership(p.getValue("membership")); 
			LeafNode node = createTopic(topic, false);
			
			if (node != null) {
				InformXMLMessage inform = new InformXMLMessage(myAddress, null, IType.creationOk, message.getMessageId());
				PayloadItem<InformXMLMessage> payload = new PayloadItem<InformXMLMessage>(inform);
				node.publish(payload);
				Log.i(TAG, "Membership messages published to newly subscribed topic");
				myHomeNode.publish(payload);
				Log.i(TAG, "Membership messages published to home node");
			} else 
				Log.e(TAG, "Can't subscribe to " + topic);
		}
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
		
		public ItemEventCoordinator(boolean isProxy) {
			this.recentMessageIDs = new String[nRecentMessageIDs];
			this.isProxy = isProxy;

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
	

		@Override
		public void handlePublishedItems(ItemPublishEvent<PayloadItem<XMLMessage>> items) {
			XMPPParser parser = new XMPPParser();
			
			for(PayloadItem<XMLMessage> item : items.getItems()) {
				if (!items.isDelayed()) {
					try {
						Log.d(TAG, "Calling parser");
						OMFMessage omfMessage = parser.XMLParse(item.toXML());

						assert !omfMessage.isEmpty();
						
						if (isNewId(omfMessage.getMessageId()))	{
							Log.d(TAG, "Message ID " + omfMessage.getMessageId() + " is new");
							recentMessageIDs[in] = omfMessage.getMessageId();
							in = (in + 1) % recentMessageIDs.length;
							
							if (isProxy)
								System.out.println("This is a resource proxy");
							else
								omfHandler(omfMessage);
							
							System.out.println(omfMessage.toString());
						} else 
							Log.d(TAG, "Message ID " + omfMessage.getMessageId() + " is a duplicate");
					} catch (XmlPullParserException e) {
						Log.e(TAG, "PullParser exception");
					} catch (IOException e) {
						Log.e(TAG, "IO exception");
					} catch (XMPPParseError e) {
						Log.e(TAG, "XMPP parse error: " + e.getMessage());						
					}
				}
			}  
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
					performXMPPLogin(xmppConn,username,password);
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



			}

