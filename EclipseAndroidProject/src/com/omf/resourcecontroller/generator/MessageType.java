package com.omf.resourcecontroller.generator;

public class MessageType {
	public enum Type {
		INFORM,
		CONFIGURE,
		REQUEST,
		CREATE,
		RELEASE
	}
	private Type type;
	
	public static final MessageType inform = new MessageType(Type.INFORM);
	public static final MessageType configure = new MessageType(Type.CONFIGURE);
	public static final MessageType request = new MessageType(Type.REQUEST);
	public static final MessageType create = new MessageType(Type.CREATE);
	public static final MessageType release = new MessageType(Type.RELEASE);
	
	private MessageType(Type type) {
		this.type = type;
	}

	public String toString() {
		switch(type) {
		case INFORM: return "inform";
		case CONFIGURE: return "configure";
		case REQUEST: return "request";
		case CREATE: return "create";
		case RELEASE: return "release";
		}
		return "unknown";
	}

	public static MessageType fromString(String tag) {
		if (tag.equalsIgnoreCase("inform"))
			return inform;
		else if (tag.equalsIgnoreCase("configure"))
			return configure;
		else if (tag.equalsIgnoreCase("request"))
			return request;
		else if (tag.equalsIgnoreCase("create"))
			return create;
		else if (tag.equalsIgnoreCase("release"))
			return release;
		else
			return null;
	}

}