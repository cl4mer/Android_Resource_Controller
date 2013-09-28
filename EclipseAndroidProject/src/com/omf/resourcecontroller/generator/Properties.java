/* Copyright (c) 2013 ETH Zürich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of ETH Zürich nor the names of other contributors 
 *      may be used to endorse or promote products derived from this software 
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT 
 * HOLDERBE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.omf.resourcecontroller.generator;

import java.util.HashMap;
import java.util.Map;

public class Properties implements Cloneable {
	
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
	

	public String getXmlns() {
		return xmlns;
	}

	public String getUrl() {
		return url;
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

	public boolean containsKey(String name) {
		return elements.containsKey(name);
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
		Key k = new Key(a, KeyType.STRING);
		elements.put(name, k);
	}

	public void addKey(String name, Map<String, String> m, KeyType keyType) {
		// FIXME: Handle element type
		Key k = new Key(m, KeyType.ARRAY);
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
				addArray(buf, (String[]) k.value, KeyType.STRING);
			} else if (k.value instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<String, String> hash = (Map<String, String>) k.value;
				buf.append(keyTypeToString(KeyType.HASH)).append("\">\n");
				addHash(buf, hash, KeyType.STRING);
			}
			
			buf.append("</").append(xmlnsKey()).append(s).append(">\n");
		}
		
		buf.append("  </").append(messageTypeToString()).append(">\n");
		
		return buf.toString();
	}
}
