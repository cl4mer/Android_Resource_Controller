/* Contains contributions that are copyright (c) 2013 ETH Zürich. All rights reserved.
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
package com.omf.resourcecontroller.OMF;

import com.omf.resourcecontroller.generator.IType;
import com.omf.resourcecontroller.generator.MessageType;
import com.omf.resourcecontroller.generator.Properties;

public class OMFMessage {
	private MessageType messageType;
	private String messageId;
	private long ts;
	private String src;
	private Properties properties;
	private Properties guard;
	private String protocolId;
	private String topic;
	private IType itype;
	private String cid;
	private String res_id;
	private String rtype;
	
	public OMFMessage() {
		this.messageType = null;
		this.messageId = null;
		this.ts = -1;
		this.src = null;
		this.protocolId = null;
		this.properties = null;
		this.guard = null;
		this.protocolId = null;
		this.topic = null;
		this.itype = null;
		this.cid = null;
		this.res_id = null;
		this.rtype = null;
	}

	public void setResId(String res_id) {
		this.res_id = res_id;
	}

	public String getResId() {
		return res_id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public Properties getGuard() {
		return guard;
	}

	public void setGuard(Properties guard) {
		this.guard = guard;
	}

	public void setMessageType(MessageType type) {
		this.messageType = type;
	}
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
		
	public String getMessageId() {
		return this.messageId;
	}
	
	public void setItype(String text) {
		itype = new IType(text);
	}
	
	public IType getItype() {
		return itype;
	}

	public void setCid(String text) {
		this.cid = text;
	}

	public String getCid() {
		return cid;
	}

	public void setTs(long timestamp) {
		this.ts = timestamp;
	}
	
	public long getTs() {
		return this.ts;
	}
	
	public void setSrc(String source) {
		this.src = source;
	}
	
	public String getSrc() {
		return this.src;
	}

	public void setRtype(String text) {
		this.rtype = text;
	}

	public String getRtype() {
		return rtype;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	public void setProperties(Properties p) {
		this.properties = p;
	}
	
	public boolean isEmpty() {
		return messageType == null;
	}
	
	public String toString() {
		String s = "Message type: "+messageType.toString()+"\n"+
					"Message ID:"+messageId+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+ts+"\n"+
					"Properties: "+properties.toString()+"\n";
					
					
		if(messageType == MessageType.inform || messageType == MessageType.release)
			s+="Itype: "+messageType+"\n";
		else if(messageType == MessageType.create)
			s+="Rtype: "+messageType+"\n";
		
		return s;
	}	
}
