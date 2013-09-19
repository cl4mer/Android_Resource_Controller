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