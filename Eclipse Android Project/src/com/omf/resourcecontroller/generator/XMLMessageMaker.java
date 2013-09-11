package com.omf.resourcecontroller.generator;


public class XMLMessageMaker {
	private static final int protocolMajor = 6;
	private static final int protocolMinor = 0;

	public static final String messageEncoding = "utf-8";
	private static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"" + messageEncoding + "\"?>";
	
	private StringBuffer buf;
	private MessageType type;
	
	public XMLMessageMaker(MessageType type, String rid, String topic) {
		super();
		this.type = type;
		this.buf = new StringBuffer();
		buf.append(xmlDeclaration).append("\n<").append(type.toString())
		   .append(" xmlns=\"http://schema.mytestbed.net/omf/")
		   .append(protocolMajor).append('.').append(protocolMinor)
		   .append("/protocol\" mid=\"").append(MessageIDGenerator.nextId()).append("\">\n  <src>")
		   .append(rid).append("</src>\n  <ts>").append(TimestampGenerator.getTimestamp()).append("</ts>\n");
		if (topic != null)
			buf.append("  <replyto>").append(topic).append("</replyto>\n");		
	}
	
	protected StringBuffer getBuf() {
		return buf;
	}
	
	public String getXMLMessage () {
		buf.append("</").append(type.toString()).append(">\n");
		return buf.toString();
	}

}
