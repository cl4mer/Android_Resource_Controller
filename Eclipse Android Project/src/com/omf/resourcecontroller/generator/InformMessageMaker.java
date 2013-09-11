package com.omf.resourcecontroller.generator;

public class InformMessageMaker extends XMLMessageMaker {
	public enum StandardMessageType {
		CREATION_OK,
		CREATION_FAILED,
		STATUS,
		RELEASED,
		ERROR,
		WARN,	
	}
	
	StringBuffer buf;
	
	public InformMessageMaker(String rid, String topic, StandardMessageType type, String cid) {
		super(MessageType.INFORM, rid, topic);
		buf = getBuf();
		appendStandardType(type);
		appendCid(cid);
	}

	public InformMessageMaker(String rid, String topic, String type, String cid) {
		super(MessageType.INFORM, rid, topic);
		buf = getBuf();
		appendNonstandardType(type);
		appendCid(cid);
	}

	private String standardMessageTypeToString(StandardMessageType type) {
		switch(type) {
		case CREATION_OK: return "CREATION.OK";
		case CREATION_FAILED: return "CREATION.FAILED";
		case STATUS: return "STATUS";
		case RELEASED: return "RELEASED";
		case ERROR: return "ERROR";
		case WARN: return "WARN";
		}
		return "unknown";
	}
	
	public void addProperties(Properties p) {
		buf.append(p.toString());
	}
	
	private void appendStandardType(StandardMessageType type) {
		buf.append("  <itype>").append(standardMessageTypeToString(type)).append("</itype>\n");
	}
	
	private void appendNonstandardType(String type) {
		buf.append("  <itype>").append(type).append("</itype>\n");
	}
	
	private void appendCid(String cid) {
		if (cid != null)
			buf.append("  <cid>").append(cid).append("</cid>\n");
	}
	
}
