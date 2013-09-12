package com.omf.resourcecontroller.generator;

import java.util.Map;

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
	private StringBuffer buf;
	
	public Properties(MessageType messageType, String xmlns, String url) {
		super();
		this.buf = new StringBuffer();
		this.xmlns = xmlns;
		this.buf.append("  <").append(messageTypeToString(messageType))
		    .append(" xmlns:").append(xmlns).append("=\"").append(url).append("\">\n");
	}

	private void openKey(String keyName, KeyType type) {
		buf.append("    <").append(xmlns).append(":").append(keyName)
		   .append(" type=\"").append(keyTypeToString(type)).append("\">");
	}
	
	private void closeKey(String keyName) {
		buf.append("</").append(xmlns).append(":").append(keyName).append(">\n");
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

	public void addKey(String keyName, boolean b) {
		openKey(keyName, KeyType.BOOLEAN);
		buf.append(booleanToString(b));
		closeKey(keyName);
	}
	
	public void addKey(String keyName, double d) {
		openKey(keyName, KeyType.FIXNUM);
		buf.append(d);
		closeKey(keyName);
	}
	
	public void addKey(String keyName, String s) {
		openKey(keyName, KeyType.STRING);
		buf.append(s); // WARNING: No escaping?
		closeKey(keyName);
	}
	
	public void addKey(String keyName, String[] a, KeyType elementType) {
		openKey(keyName, KeyType.ARRAY);
		buf.append('\n');
		addArray(a, elementType);
		buf.append("    ");
		closeKey(keyName);
	}

	public void addKey(String keyName, Map<String, String> m, KeyType keyType) {
		openKey(keyName, KeyType.HASH);
		buf.append('\n');
		addHash(m, keyType);
		buf.append("    ");
		closeKey(keyName);
	}
	
	private void addHash(Map<String, String> m, KeyType keyType) {
		String type = keyTypeToString(keyType);
		for (String k : m.keySet()) {
			buf.append("      <key").append(k).append(" type=\"").append(type).append("\">")
			   .append(m.get(k)).append("</key").append(k).append(">\n");
		}
	}

	private void addArray(String[] a, KeyType elementType) {
		String type = keyTypeToString(elementType);
		for (int i = 0; i < a.length; i++) {
			buf.append("      <it type=\"").append(type).append("\">").append(a[i]).append("</it>\n");
		}
	}
	
	public String toString() {
		buf.append("  </props>\n");
		return buf.toString();
	}
}
