package com.omf.resourcecontroller.generator;


public class XMLMessageMaker {
	private static final int protocolMajor = 6;
	private static final int protocolMinor = 0;

	public static final String messageEncoding = "utf-8";
	private static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"" + messageEncoding + "\"?>";
	
	public enum MessageType {
		INFORM,
		CONFIGURE,
		REQUEST,
		CREATE,
		RELEASE
	}

	private StringBuffer buf;
	private MessageType type;
	
	public XMLMessageMaker(MessageType type, String rid, String topic) {
		super();
		this.type = type;
		this.buf = new StringBuffer();
		buf.append(xmlDeclaration).append("\n<").append(messageTypeToString(type))
		   .append(" xmlns=\"http://schema.mytestbed.net/omf/")
		   .append(protocolMajor).append('.').append(protocolMinor)
		   .append("/protocol\" mid=\"").append(MessageIDGenerator.nextId()).append("\">\n  <src>")
		   .append(rid).append("</src>\n  <ts>").append(TimestampGenerator.getTimestamp()).append("</ts>\n");
		if (topic != null)
			buf.append("  <replyto>").append(topic).append("</replyto>\n");		
	}
	
	private String messageTypeToString(MessageType type) {
		switch(type) {
		case INFORM: return "inform";
		case CONFIGURE: return "configure";
		case REQUEST: return "request";
		case CREATE: return "create";
		case RELEASE: return "release";
		}
		return "unknown";
	}
	
	protected StringBuffer getBuf() {
		return buf;
	}
	
	public String getXMLMessage () {
		buf.append("</").append(messageTypeToString(type)).append(">\n");
		return buf.toString();
	}

}
