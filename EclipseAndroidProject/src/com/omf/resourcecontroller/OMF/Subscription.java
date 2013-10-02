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
package com.omf.resourcecontroller.OMF;

import org.jivesoftware.smackx.pubsub.LeafNode;

import android.util.Log;

public class Subscription {
	private String topic;
	private LeafNode node;
	private OMFEventCoordinator coordinator;
	private static final String TAG = "Subscription";
	
	public Subscription(String topic, LeafNode node,
			OMFEventCoordinator coordinator) {
		super();
		this.topic = topic;
		this.node = node;
		this.coordinator = coordinator;
	}

	public Subscription(String topic, LeafNode node) {
		super();
		this.topic = topic;
		this.node = node;
		this.coordinator = null;
	}
	
	public OMFEventCoordinator getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(OMFEventCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	public String getTopic() {
		Log.i(TAG , "getTopic(): " + topic);
		return topic;
	}
	
	public LeafNode getNode() {
		Log.i(TAG , "getNode(): " + node);
		return node;
	}
}
