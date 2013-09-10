package com.omf.resourcecontroller.generator;

public class Properties {
	public enum KeyType {
		STRING,
		FIXNUM,
		BOOLEAN,
		HASH,
		ARRAY,
	}
	
	public enum MessageType {
		PROPS,
		GUARD,
	}
	
	private String xmlns;
	private int keyIndex;
	private StringBuffer buf;
	
	public Properties(MessageType messageType, String xmlns, String url) {
		super();
		this.buf = new StringBuffer();
		this.xmlns = xmlns;
		this.keyIndex = 1;
		this.buf.append("<").append(messageTypeToString(messageType))
		    .append(" xmlns:").append(xmlns).append("=\"").append(url).append("\">\n");
	}

	private void openKey(KeyType type) {
		buf.append("  <").append(xmlns).append(":key").append(keyIndex)
		   .append(" type=\"").append(keyTypeToString(type)).append("\">");
	}
	
	private void closeKey() {
		buf.append("</").append(xmlns).append(":key").append(keyIndex).append(">\n");
		keyIndex++;
	}

	private String messageTypeToString(MessageType messageType) {
		switch (messageType) {
		case PROPS: return "props";
		case GUARD: return "guard";
		}
		// NOTREACHED
		return "unknown";
	}

	private String keyTypeToString(KeyType keyType) {
		switch (keyType) {
		case STRING: return "string";
		case FIXNUM: return "fixnum";
		case BOOLEAN: return "boolean";
		case HASH: return "hash";
		case ARRAY: return "array";
		}
		// NOTREACHED
		return "unknown";
	}

	private String booleanToString(boolean b) {
		return b ? "true" : "false";
	}

	public void addKey(boolean b) {
		openKey(KeyType.BOOLEAN);
		buf.append(booleanToString(b));
		closeKey();
	}
	
	public void addKey(double d) {
		openKey(KeyType.FIXNUM);
		buf.append(d);
		closeKey();
	}
	
	public void addKey(String s) {
		openKey(KeyType.STRING);
		buf.append(s); // WARNING: No escaping?
		closeKey();
	}
	
	public void addKey(String[] a, KeyType elementType) {
		openKey(KeyType.ARRAY);
		buf.append('\n');
		addArray(a, elementType);
		buf.append("  ");
		closeKey();
	}

	private void addArray(String[] a, KeyType elementType) {
		String type = keyTypeToString(elementType);
		for (int i = 0; i < a.length; i++) {
			buf.append("    <it type=\"").append(type).append("\">").append(a[i]).append("</it>\n");
		}
	}
	
	public String toString() {
		buf.append("</props>\n");
		return buf.toString();
	}
}
