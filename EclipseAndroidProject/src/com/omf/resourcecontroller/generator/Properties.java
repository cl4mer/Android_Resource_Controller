package com.omf.resourcecontroller.generator;

import java.util.HashMap;
import java.util.Map;

public class Properties {
	
	public enum KeyType {
		STRING,
		FIXNUM,
		INTEGER,
		SYMBOL,
		BOOLEAN,
		HASH,
		ARRAY,
	}
	
	private class Key {
		public KeyType type;
		public Object value;
		
		public Key(String value, KeyType type) {
			this.type = type;
			this.value = value;
		}

		public Key(String[] a, KeyType type) {
			this.type = type;
			this.value = a;
		}
		
		public Key(Map<String, String> m, KeyType type) {
			this.type = type;
			this.value = m;
		}

		public boolean isSimple() {
			return this.type == KeyType.STRING
					|| this.type == KeyType.FIXNUM
					|| this.type == KeyType.INTEGER
					|| this.type == KeyType.SYMBOL
					|| this.type == KeyType.BOOLEAN;
		}

	}

	public enum MessageType {
		PROPS,
		GUARD,
	}

	private Map<String, Key> elements;
	
	private MessageType messageType;
	private String xmlns;
	private String url;
	
	public Properties(MessageType messageType) {
		super();
		
		this.elements = new HashMap<String, Key>();
		this.messageType = messageType;
		this.xmlns = null;
		this.url = null;
	}

	public void setNamespace(String xmlns, String url) {
		this.xmlns = xmlns;
		this.url = url;		
	}
	
	private String xmlnsKey() {
		if (this.xmlns == null)
			return "";
		else
			return this.xmlns + ":";
	}
	
	private String messageTypeToString() {
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
		case INTEGER: return "integer";
		case SYMBOL: return "symbol";
		case BOOLEAN: return "boolean";
		case HASH: return "hash";
		case ARRAY: return "array";
		}
		// NOTREACHED
		return "unknown";
	}

	public KeyType getType(String name) {
		if (elements.containsKey(name))
			return elements.get(name).type;
		else
			return null;
	}
	
	public String getValue(String name) {
		if (elements.containsKey(name)) {
			Key k = elements.get(name);
			if (k.isSimple())
				return (String) k.value;
			else
				return null;
		} else
			return null;
	}
	
	public String[] getArrayValue(String name) {
		if (elements.containsKey(name)) {
			Key k = elements.get(name);
			if (k.type == KeyType.ARRAY)
				return (String[]) k.value;
			else
				return null;
		} else
			return null;
		
	}

	public Map<String, String> getHashValue(String name) {
		if (elements.containsKey(name)) {
			Key k = elements.get(name);
			if (k.type == KeyType.HASH) {
				@SuppressWarnings("unchecked")
				Map<String, String> ret = (Map<String, String>) k.value; 
				return ret;
			} else
				return null;
		} else
			return null;
		
	}

	public void addKey(String name, String value, KeyType type) {
		Key k = new Key(value, type);
		elements.put(name, k);
	}
	
	public void addKey(String name, String[] a, KeyType elementType) {
		// FIXME: Handle element type
		Key k = new Key(a, KeyType.ARRAY);
		elements.put(name, k);
	}

	public void addKey(String name, Map<String, String> m, KeyType keyType) {
		// FIXME: Handle element type
		Key k = new Key(m, KeyType.HASH);
		elements.put(name, k);
	}
	
	private void addHash(StringBuffer buf, Map<String, String> m, KeyType keyType) {
		String type = keyTypeToString(keyType);
		for (String k : m.keySet()) {
			buf.append("      <").append(k).append(" type=\"").append(type).append("\">")
			   .append(m.get(k)).append("</").append(k).append(">\n");
		}
		buf.append("    ");
	}

	private void addArray(StringBuffer buf, String[] a, KeyType elementType) {
		String type = keyTypeToString(elementType);
		for (int i = 0; i < a.length; i++) {
			buf.append("      <it type=\"").append(type).append("\">").append(a[i]).append("</it>\n");
		}
		buf.append("    ");
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("  <").append(messageTypeToString());
		if (xmlns != null)
			buf.append(" xmlns:").append(xmlns).append("=\"").append(url).append("\">\n");
		else
			buf.append(">\n");
		
		for (String s : elements.keySet()) {
			Key k = elements.get(s);

			buf.append("    <").append(xmlnsKey()).append(s).append(" type=\"");
			if (k.value instanceof String) {
			   buf.append(keyTypeToString(k.type)).append("\">").append((String) k.value);
			} else if (k.value instanceof String[]) {
				buf.append(keyTypeToString(KeyType.ARRAY)).append("\">\n");
				addArray(buf, (String[]) k.value, k.type);
			} else if (k.value instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<String, String> hash = (Map<String, String>) k.value;
				buf.append(keyTypeToString(KeyType.HASH)).append("\">\n");
				addHash(buf, hash, k.type);
			}
			
			buf.append("</").append(xmlnsKey()).append(s).append(">\n");
		}
		
		buf.append("  </").append(messageTypeToString()).append(">\n");
		
		return buf.toString();
	}
}
