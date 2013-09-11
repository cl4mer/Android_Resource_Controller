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
	
	private static MessageType inform = new MessageType(Type.INFORM);
	private static MessageType configure = new MessageType(Type.CONFIGURE);
	private static MessageType request = new MessageType(Type.REQUEST);
	private static MessageType create = new MessageType(Type.CREATE);
	private static MessageType release = new MessageType(Type.RELEASE);
	
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

	public static MessageType inform() {
		return inform;
	}

	public static MessageType configure() {
		return configure;
	}

	public static MessageType request() {
		return request;
	}

	public static MessageType create() {
		return create;
	}

	public static MessageType release() {
		return release;
	}

	public static MessageType fromString(String tag) {
		if (tag.equalsIgnoreCase("inform"))
			return inform();
		else if (tag.equalsIgnoreCase("configure"))
			return configure();
		else if (tag.equalsIgnoreCase("request"))
			return request();
		else if (tag.equalsIgnoreCase("create"))
			return create();
		else if (tag.equalsIgnoreCase("release"))
			return release();
		else
			return null;
	}

}