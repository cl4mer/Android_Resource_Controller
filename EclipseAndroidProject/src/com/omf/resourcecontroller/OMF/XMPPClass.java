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
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.MessageType;
import com.omf.resourcecontroller.parser.XMPPParseError;
import com.omf.resourcecontroller.parser.XMPPParser;

/**
 * 
 * 
 *
 */



public class XMPPClass {

	public static final String TAG = "XMPPClass";

	// XMPP variables
	// 	XMPP CONNECTION VAR
	private ConnectionConfiguration connConfig = null;		//  XMPP CONFIGURATION
	private PubSubManager pubmgr = null;					// 	XMPP PUB SUB MANAGER
	//private Node eventNode = null;							// 	XMPP Eventnode

	private XMPPConnectionListener connectionListener = null;							
	private String username = null;							//	Username for XMPP login
	private String password = null;							//	Password for XMPP login
	private String topic = null;
	private XMPPConnection xmpp = null;

	//XMPP Parser 
	private XMPPParser parser = null;

	//OMF message object
	private OMFMessage omfMessage = null;


	//Node and Node Listener HashMap
	HashMap<String, ItemEventCoordinator> nodeListeners;
	HashMap<String, Node> nodes;

	//flag
	private boolean flag;
	Handler handler;

	public XMPPClass(String username, String password, String topicName, Handler handler){
		
		this.nodeListeners = new HashMap<String, ItemEventCoordinator>();
		this.nodes = new HashMap<String, Node>();
		this.username = username;
		this.password = password;
		this.topic = topicName;		
		this.flag = true;
		this.handler = handler;
	}
	
	private class ConnectRunnable implements Runnable {

		@Override
		public void run() {
			
			//Open XMPP Connection
			try{
				xmpp.connect(); 				
				//Add connection listener
				if(xmpp.isConnected()){
					Log.i(TAG,"XMPP connected");
					connectionListener = new XMPPConnectionListener();
					xmpp.addConnectionListener(connectionListener);
				}

				//Add ping manager to deal with disconnections (after 6 minutes idle xmpp disconnects)
				PingManager.getInstanceFor(xmpp).setPingIntervall(5*60*1000);	//5 minutes (5*60*1000 in millisecons)
			}catch(XMPPException e){
				Log.e(TAG, "XMPP connection failed");
				e.printStackTrace();
				xmpp = null;
			}

			
		}
		
	}

	public XMPPConnection XMPPCreateConnection(Context appContext){
		//Init aSmack
		SmackAndroid.init(appContext);
		//SmackConfiguration.setDefaultPingInterval(100);	
		// XMPP CONNECTION
		connConfig = new ConnectionConfiguration(Constants.SERVER,Constants.PORT);
		xmpp = new XMPPConnection(connConfig);
		
		Thread connectionThread = new Thread(new ConnectRunnable());
		connectionThread.start();

		//Do Login
		XMPPLogin(xmpp,username,password);

		//If xmpp is logged in declare your presence
		if(xmpp.isAuthenticated()){
			//Declare presence
			Presence presence = new Presence(Presence.Type.available);
			xmpp.sendPacket(presence);
		}

		//Add pubsub manager
		if(xmpp.isAuthenticated())
		{
			pubmgr = new PubSubManager(xmpp);
		}
		//CreateTopic
		createTopic(topic, false);

		return xmpp;
	}

	public boolean XMPPLogin(XMPPConnection Xmpp, String username,String pass){

		if(Xmpp.isConnected()){
			try {
				//1st try
				Xmpp.login(username, pass);
				Log.i(TAG,"XMPP Logged in");
			} catch (XMPPException e) {
				Log.e(TAG, "XMPP login failed");
				Log.i(TAG, "Creating new account");

				try {
					if(registerUser(Xmpp,username,pass)){
						try {
							Xmpp.login(username, pass);
							flag=true;
							Log.i(TAG,"XMPP Logged in");
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



	public void createTopic(String topicName , boolean isProxy){

		if(xmpp.isAuthenticated()){
			//New node
			Node eventNode = null;
			//New node listener
			ItemEventCoordinator eventListener = null;


			//Node configuration form
			ConfigureForm f = new ConfigureForm(FormType.submit);
			/**
			 * Configure form
			 */
			f.setPersistentItems(false);				//false
			f.setPublishModel(PublishModel.open);		//open
			f.setNotifyRetract(false);					//false
			f.setSubscribe(true);						//true

			try{
				eventNode = pubmgr.getNode(topicName);
			}catch(XMPPException e){
				//e.printStackTrace();
				Log.e(TAG, "Problem getting node "+ topicName);
				//If node doesn't exist create it
				try {
					Log.i(TAG, "Creating node "+topicName);
					eventNode = pubmgr.createNode(topicName,f);
					//Put node to hashmap
					nodes.put(topicName,eventNode);

				} catch (XMPPException e1) {
					//e1.printStackTrace();
					Log.e(TAG, "Problem creating event "+topicName);

				}
			}


			try {
				//Add event listener
				eventListener = new ItemEventCoordinator(isProxy);
				eventNode.addItemEventListener(eventListener);
				//Put node listener created in a hashMap
				nodeListeners.put(topicName, eventListener);

				//Subscribe to the node
				eventNode.subscribe(xmpp.getUser());
			} catch (XMPPException e) {
				e.printStackTrace();
			}

		}
		//return newNode;
	}

	/**
	 * Register a new user in xmpp
	 * @param mycon : XMPPConnection object
	 * @param username : String
	 * @param pass : String
	 * @throws XMPPException
	 * @returns boolean: False if registration failed, else true
	 */
	public boolean registerUser(XMPPConnection mycon, String username, String  pass) throws XMPPException{
		if(mycon != null && username!= null && pass != null){
			AccountManager mgr = mycon.getAccountManager();

			if (mgr.supportsAccountCreation ())
			{
				mgr.createAccount (username, pass);
			}
			Log.i(TAG, "Account created: "+username);

			//XMPP refresh connection
			if(mycon!= null)
			{
				flag=false;
				mycon.disconnect();
				mycon.connect(); 
				//flag=true;
				Log.i(TAG, "XMPP connection refresh ");
			}
			return true;
		}
		return false;
	}


	public void OMFHandler(OMFMessage message){

		if(message.getMessageType() == MessageType.create)
		{
			//message.OMFCreate();

			//createTopic(message.getProperty("uid"),true);

		}
		else if (message.getMessageType() == MessageType.configure)
		{
			//message.OMFConfigure();

		}
		else if (message.getMessageType() == MessageType.request)
		{
			//message.OMFRequest();

		}
		else if (message.getMessageType() == MessageType.inform)
		{
			//message.OMFInform();

		}
		else if (message.getMessageType() == MessageType.release)
		{
			//message.OMFInform();

		}

		return;
	}

	public void destroyConnection(){

		//remove connection listener
		xmpp.removeConnectionListener(connectionListener);

		//destroy all topics and remove their listeners
		destroyTopics();
		if(xmpp != null)
			xmpp.disconnect();

		xmpp = null;
	}

	public void destroySingleTopic(String topicName){
		Node node = nodes.get(topicName); 
		ItemEventCoordinator nodeListener = nodeListeners.get(topicName);
		node.removeItemEventListener(nodeListener);
		nodes.remove(topicName);
	}

	public void destroyTopics(){
		//eventNode.removeItemEventListener(eventListener);


		for (String key : nodes.keySet()) {
			Node node = nodes.get(key);
			ItemEventCoordinator nodeListener = nodeListeners.get(key);
			node.removeItemEventListener(nodeListener);
			try {
				pubmgr.deleteNode(key);
			} catch (XMPPException e) {
				Log.e(TAG, "Node deletion problem");
				e.printStackTrace();
			}
		}
	}


	/**Item Listener
	 * 
	 * @author Polychronis
	 * 
	 */
	@SuppressWarnings("rawtypes")
	class ItemEventCoordinator  implements ItemEventListener <PayloadItem>
	{
		private static final int nDuplicateMessageCheck = 10;

		//Variables,arrays to handle duplicate messages
		private String[] duplicateCheck;
		private boolean duplicateFlag;
		private int in;
		private boolean isProxy;
		
		public ItemEventCoordinator(boolean isProxy){
			duplicateCheck = new String[nDuplicateMessageCheck];
			duplicateFlag = false;
			this.isProxy = isProxy;

			in = 0;
			for (int j = 0; j < duplicateCheck.length; j++)
				duplicateCheck[j] = null; 
		}

		@Override
		public void handlePublishedItems(ItemPublishEvent <PayloadItem> items) {
			parser = new XMPPParser();
			
			for(PayloadItem item : items.getItems()) {
				if (!items.isDelayed()) {
					try {
						omfMessage = parser.XMLParse(item.toXML());

						assert !omfMessage.isEmpty(); 
						duplicateFlag = false;
						for (int i = 0; i < duplicateCheck.length; i++) {	
							if(omfMessage.getMessageId().equals(duplicateCheck[i]))	{
								duplicateFlag = true;
								break;
							}
						}

						if (!duplicateFlag)	{
							duplicateCheck[in] = omfMessage.getMessageId();
							in = (in + 1) % duplicateCheck.length;
							
							if(isProxy)
								System.out.println("This is a resource proxy");
							else
								OMFHandler(omfMessage);
							
							System.out.println(omfMessage.toString());
						}
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

	class XMPPConnectionListener implements ConnectionListener{
		public void connectionClosed() {
			Log.d("SMACK","Connection closed ()");
			if (flag){
				//XMPPCreateConnection();
			}
		}

		public void connectionClosedOnError(Exception e) {
			Log.d("SMACK","Connection closed due to an exception");
			e.printStackTrace();
		}
		public void reconnectionFailed(Exception e) {
			Log.d("SMACK","Reconnection failed due to an exception");
			e.printStackTrace();
		}
		public void reconnectionSuccessful() {
			Log.d("SMACK","Connection reconnected");
			if (flag){
				if(!xmpp.isAuthenticated()){
					XMPPLogin(xmpp,username,password);
				}
			}
		}
		public void reconnectingIn(int seconds) {
			Log.d("SMACK","Connection will reconnect in " + seconds);
		}
	}



}

