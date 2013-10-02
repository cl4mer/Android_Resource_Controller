package com.omf.resourcecontroller.OMF;

import java.io.IOException;

import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.omf.resourcecontroller.generator.XMLMessage;
import com.omf.resourcecontroller.parser.XMPPParseError;
import com.omf.resourcecontroller.parser.XMPPParser;

class OMFEventCoordinator implements ItemEventListener<PayloadItem<XMLMessage>> {
	public static final String TAG = "OMFEventCoordinator";
	
	private OMFMessageHandler handler;
	
	public OMFEventCoordinator(OMFMessageHandler handler) {
		this.handler = handler;
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

					if (RecentMessageIds.isNewId(omfMessage.getMessageId()))	{
						Log.d(TAG, "Message ID " + omfMessage.getMessageId() + " is new");							
						handler.handle(omfMessage);
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
}
