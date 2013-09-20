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

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
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
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.InformMessageMaker;
import com.omf.resourcecontroller.generator.Properties;
import com.omf.resourcecontroller.parser.XMPPParseError;
import com.omf.resourcecontroller.parser.XMPPParser;

public class XMPPClass {

	public static final String TAG = "XMPPClass";

	private String username;
	private String password;
	private String topic;
	
	private XMPPConnection connection = null;
	private PubSubManager pubmgr = null;
	private XMPPConnectionListener connectionListener = null;							
	private boolean loginFinished = false;

	HashMap<String, Subscription> subscriptions;
	HashMap<String, LeafNode> nodes;
	LeafNode myHomeNode = null; 
	String myAddress = null;
	
	Handler handler;

	public XMPPClass(String username, String password, String topicName, Handler handler) {
		this.subscriptions = new HashMap<String, Subscription>();
		this.nodes = new HashMap<String, LeafNode>();
		this.username = username;
		this.password = password;
		this.topic = topicName;		
		this.handler = handler;
		
		Log.d(TAG, "XMPPClass instantiated for user " + username + ", password " + password + ", topic " + topic);
	}
	
    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
    	@Override
    	public Boolean doInBackground(Void... voids) {
    		Boolean ret = Boolean.TRUE;
    		try {
    			connection.connect();                                 

        		if (connection.isConnected()) {
        			Log.d(TAG, "XMPP connected");
        			connectionListener = new XMPPConnectionListener();
        			connection.addConnectionListener(connectionListener);
        		
        			//Add ping manager to deal with disconnects
        			PingManager.getInstanceFor(connection).setPingIntervall(5*60*1000);
        		
        			login(connection, username, password);
        			Log.d(TAG, "Logged in");

        			if (connection.isAuthenticated()) {
        				Presence presence = new Presence(Presence.Type.available);
        				connection.sendPacket(presence);
        				
        				Log.d(TAG, "Presence announced");
        				
        				pubmgr = new PubSubManager(connection);
        				
        				myHomeNode = createTopic(topic, false);
        				myAddress = "xmpp://" + topic + "@" + Constants.SERVER;
        				Log.d(TAG, "Topic created");
        			} else {
        				Log.e(TAG, "Login failed");
        				ret = Boolean.FALSE;
        			}
        		} else {
        			Log.e(TAG, "Connection failed");
        			ret = Boolean.FALSE;
        		}
    		} catch(XMPPException e) {
    			Log.e(TAG, "XMPP connection failed");
    			e.printStackTrace();
    			ret = Boolean.FALSE;
    		}
    		
    		Log.d(TAG, "LoginTask returns " + ret.toString());
    		synchronized (connection) {
    			// Order of assignment and notify() is important
    			loginFinished = true;
    			connection.notify();
    		}
    		return ret;
    	}    	
    }


	public XMPPConnection connect(Context appContext) {
		Log.d(TAG, "ENTER connect");

		SmackAndroid.init(appContext);

		Log.d(TAG, "After SmackAndroid.init()");

		connection = new XMPPConnection(new ConnectionConfiguration(Constants.SERVER, Constants.PORT));
		new LoginTask().execute();

		synchronized (connection) {
			while (!loginFinished) {
				try {
					connection.wait();
				} catch (InterruptedException e) {
					// Empty
				}
			}
		}
		
		return connection;
	}

	private boolean login(XMPPConnection xmpp, String username, String pass) {
		if (xmpp.isConnected()) {
			try {
				xmpp.login(username, pass);
				Log.d(TAG,"XMPP Logged in");
			} catch (XMPPException e) {
				Log.e(TAG, "XMPP login failed");
				Log.i(TAG, "Creating new account");

				try {
					if (registerUser(xmpp,username,pass)) {
						try {
							xmpp.login(username, pass);
							Log.d(TAG,"XMPP Logged in");
							return true;
						} catch (XMPPException e1) {	
							Log.e(TAG,"XMPP Login failed");
							return false;
						}	
					}
				} catch (XMPPException e1) {
					Log.e(TAG,"Registration failed");
					return false;
				}
			}
		}
		return false;
	}

	private LeafNode findTopic(String topicName) {
		LeafNode ret = null;
		try {
			ret = pubmgr.getNode(topicName);
			/* This won't do anything if the topic already exists. */
			nodes.put(topicName, ret);
		} catch(XMPPException e) {
			Log.e(TAG, "Problem getting node " + topicName + "; trying to create");
			ret = null;
		}

		return ret;
	}
	
	private LeafNode createTopic(String topicName, boolean isProxy) {
		if(connection.isAuthenticated()){
			LeafNode eventNode = null;
			ItemEventCoordinator eventListener = null;
			boolean nodeCreated = false;
			
			eventNode = findTopic(topicName); 
			if (eventNode == null) {
				try {
					ConfigureForm f = new ConfigureForm(FormType.submit);

					f.setPersistentItems(false);
					f.setPublishModel(PublishModel.open);
					f.setNotifyRetract(false);
					f.setSubscribe(true);
					
					Log.d(TAG, "Creating node " + topicName);
					eventNode = (LeafNode) pubmgr.createNode(topicName, f);
					nodes.put(topicName, eventNode);
					nodeCreated = true;
				} catch (XMPPException e) {
					Log.e(TAG, "Problem creating event " + topicName + ": " + e.getMessage());
				}
			}

			if (!nodeCreated)
				return null;

			try {
				eventListener = new ItemEventCoordinator(isProxy);
				eventNode.addItemEventListener(eventListener);
				subscriptions.put(topicName, eventListener);

				eventNode.subscribe(connection.getUser());
			} catch (XMPPException e) {
				Log.e(TAG, "Problem subscribing to topic " + topicName + ": " + e.getMessage());
			}
			
			return eventNode;
		} else
			return null;
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

		if (connection != null)	{
			connection.disconnect();
			connection.connect(); 
			//flag=true;
			Log.d(TAG, "XMPP connection refresh ");
		}
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
				InformMessageMaker maker = new InformMessageMaker(myAddress, null, IType.creationOk, message.getMessageId());
				PayloadItem<InformMessageMaker> payload = new PayloadItem<InformMessageMaker>(maker);
				node.publish(payload);
				myHomeNode.publish(payload);
				Log.i(TAG, "Membership messages published");
			} else 
				Log.e(TAG, "Can't subscribe to " + topic);
		}
	}

	public void destroyConnection() {

		//remove connection listener
		connection.removeConnectionListener(connectionListener);

		//destroy all topics and remove their listeners
		destroyTopics();
		if(connection != null)
			connection.disconnect();

		connection = null;
	}

	public void destroySingleTopic(String topicName){
		Node node = nodes.get(topicName); 
		ItemEventCoordinator nodeListener = subscriptions.get(topicName);
		node.removeItemEventListener(nodeListener);
		nodes.remove(topicName);
	}

	public void destroyTopics() {
		//eventNode.removeItemEventListener(eventListener);


		for (String key : nodes.keySet()) {
			LeafNode node = nodes.get(key);
			ItemEventCoordinator nodeListener = subscriptions.get(key);
			node.removeItemEventListener(nodeListener);
			try {
				pubmgr.deleteNode(key);
			} catch (XMPPException e) {
				Log.e(TAG, "Node deletion problem");
				e.printStackTrace();
			}
		}
	}


	/** Item Listener.
	 * 
	 * @author Polychronis
	 */
	class ItemEventCoordinator implements ItemEventListener<PayloadItem> {
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
		public void handlePublishedItems(ItemPublishEvent <PayloadItem> items) {
			XMPPParser parser = new XMPPParser();
			
			for(PayloadItem item : items.getItems()) {
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
			Log.d(TAG, "Connection reconnected");
			if (!connection.isAuthenticated()) {
				login(connection, username, password);
			}
		}

		public void reconnectingIn(int seconds) {
			Log.d(TAG, "Connection will reconnect in " + seconds);
		}
	}



}

