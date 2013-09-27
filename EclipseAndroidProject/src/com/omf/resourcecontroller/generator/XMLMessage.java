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

import org.jivesoftware.smack.packet.PacketExtension;


public class XMLMessage implements PacketExtension {
	private static final int protocolMajor = 6;
	private static final int protocolMinor = 0;

	//public static final String messageEncoding = "utf-8";
	//private static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"" + messageEncoding + "\"?>";
	
	private StringBuffer buf;
	private MessageType type;
	private String messageId;
	
	public XMLMessage(MessageType type, String rid, String topic) {
		super();
		this.type = type;
		
		this.messageId = MessageIDGenerator.nextId();
		this.buf = new StringBuffer();
		buf/*.append(xmlDeclaration).append("\n")*/.append("<").append(getElementName())
		   .append(" xmlns=\"").append(getNamespace()).append("\" mid=\"")
		   .append(messageId).append("\">\n  <src>")
		   .append(rid).append("</src>\n  <ts>").append(TimestampGenerator.getTimestamp()).append("</ts>\n");
		if (topic != null)
			buf.append("  <replyto>").append(topic).append("</replyto>\n");		
	}
	
	protected StringBuffer getBuf() {
		return buf;
	}
	
	@Override
	public String getElementName() {
		return type.toString();
	}

	@Override
	public String getNamespace() {
		return "http://schema.mytestbed.net/omf/" + protocolMajor + '.' + protocolMinor + "/protocol";
	}

	@Override
	public String toXML() {
		buf.append("</").append(getElementName()).append(">\n");
		return buf.toString();
	}

	public String getMessageId() {
		return messageId;
	}	
}
