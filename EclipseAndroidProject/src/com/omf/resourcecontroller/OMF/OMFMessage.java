package com.omf.resourcecontroller.OMF;

import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.MessageType;
import com.omf.resourcecontroller.generator.Properties;

public class OMFMessage {
	private MessageType messageType;
	private String messageId;
	private long ts;
	private String src;
	Properties properties;
	Properties guard;
	private String protocolId;
	private String topic;
	private IType itype;
	private String cid;
	private String rtype;
	
	public OMFMessage() {
		messageType = null;
		messageId = null;
		ts = -1;
		src = null;
		protocolId = null;
		properties = null;
		guard = null;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public Properties getGuard() {
		return guard;
	}

	public void setGuard(Properties guard) {
		this.guard = guard;
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
	
	public void setItype(String text) {
		itype = new IType(text);
	}
	
	public IType getItype() {
		return itype;
	}

	public void setCid(String text) {
		this.cid = text;
	}

	public String getCid() {
		return cid;
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

	public void setRtype(String text) {
		this.rtype = text;
	}

	public String getRtype() {
		return rtype;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	public void setProperties(Properties p) {
		this.properties = p;
	}
	
	public boolean isEmpty() {
		return messageType == null;
	}
	
	public String toString() {
		String s = "Message type: "+messageType.toString()+"\n"+
					"Message ID:"+messageId+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+ts+"\n"+
					"Properties: "+properties.toString()+"\n";
					
					
		if(messageType == MessageType.inform || messageType == MessageType.release)
			s+="Itype: "+messageType+"\n";
		else if(messageType == MessageType.create)
			s+="Rtype: "+messageType+"\n";
		
		return s;
	}	
}
