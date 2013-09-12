package com.omf.resourcecontroller.OMF;

import java.util.HashMap;
import java.util.Map;

import com.omf.resourcecontroller.generator.MessageType;

public class OMFMessage {
	private MessageType messageType;
	private String messageId;
	private long ts;
	private String src;
	Map<String, String> properties;
	Map<String, String> guard;
	private String protocolId;
	private String topic;
	
	public OMFMessage() {
		messageType = null;
		messageId = null;
		ts = -1;
		src = null;
		protocolId = null;
		properties = new HashMap<String, String>();
		guard = new HashMap<String, String>();
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public Map<String, String> getGuard() {
		return guard;
	}

	public void setMessageType(MessageType type) {
		this.messageType = type;
	}
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
		
	public String getMessageId() {
		return this.messageId;
	}
	
	public void setTs(long timestamp) {
		this.ts = timestamp;
	}
	
	public long getTs() {
		return this.ts;
	}
	
	public void setSrc(String source) {
		this.src = source;
	}
	
	public String getSrc() {
		return this.src;
	}

	
	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}

	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	
	public Map<String, String> getProperties() {		
		return this.properties;
	}
	
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	public String toString() {
		String s = "Message type: "+messageType.toString()+"\n"+
					"Message ID:"+messageId+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+ts+"\n"+
					"Properties: "+properties.toString()+"\n";
					
					
		if(messageType == MessageType.inform() || messageType == MessageType.release())
			s+="Itype: "+messageType+"\n";
		else if(messageType == MessageType.create())
			s+="Rtype: "+messageType+"\n";
		
		return s;
	}
	
	/*
	public boolean equals(String mid) {		
		return this.messageId.equalsIgnoreCase(mid);
	}
	*/
	
	public boolean isEmpty()
	{
		return messageType == null;
	}
	
	/*
	public void OMFCreate() {
		//Code to create something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFConfigure() {
		//Code to configure something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFRequest() {
		//Code to reply to a certain request
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFInform() {
		//Code to inform someone of something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFRelease() {
		//Code to inform someone of something
		System.out.println(this.messageType);
		return;
	}
	*/
}
