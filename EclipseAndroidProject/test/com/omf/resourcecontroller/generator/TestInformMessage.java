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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.omf.resourcecontroller.generator.Properties.KeyType;
import com.omf.resourcecontroller.generator.Properties.MessageType;

public class TestInformMessage {

	@Before
	public void setUp() {
		MessageIDGenerator.clearPrefix();
		MessageIDGenerator.setPrefix("inform-test.");
	}
	
	@Test
	public void testInform1() {
		InformMessageMaker i = new InformMessageMaker("hi@ho:123", null, IType.creationOk, null);
		Properties p = new Properties(MessageType.PROPS);
		p.setNamespace("tik", "http://www.tik.ee.ethz.ch/");
		p.addKey("slippermen-line-1", "I wandered lonely as a cloud", KeyType.STRING);
		p.addKey("slippermen-line-2", "Till I came upon this dirty street", KeyType.STRING);
		i.addProperties(p);
		System.out.println(i.getXMLMessage());
		assertTrue(true);
	}

	@Test
	public void testInform2() {
		InformMessageMaker i = new InformMessageMaker("hi@ho:123", null, IType.creationOk, "bla@blubb:456");
		Properties p = new Properties(MessageType.PROPS);
		p.setNamespace("tik", "http://www.tik.ee.ethz.ch/");
		p.addKey("slippermen-line-1", "I wandered lonely as a cloud", KeyType.STRING);
		p.addKey("slippermen-line-2", "Till I came upon this dirty street", KeyType.STRING);
		i.addProperties(p);
		System.out.println(i.getXMLMessage());
		assertTrue(true);
	}

}
