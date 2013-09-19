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
package com.omf.resourcecontroller.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.generator.Properties.KeyType;

public class TestParser {

	private XMPPParser parser;
	
	@Before
	public void setUp() {
		parser = new XMPPParser();
	}
	
	@Test
	public void test1() throws XmlPullParserException, IOException, XMPPParseError {
		String toParse = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<inform xmlns=\"http://schema.mytestbed.net/omf/6.0/protocol\" mid=\"inform-test.1571442862\">\n"
				+ "  <src>hi@ho:123</src>\n"
				+ "  <ts>1378902336</ts>\n"
				+ "</inform>\n";
		OMFMessage msg = parser.XMLParse(toParse);
		assertEquals(msg.getMessageId(), "inform-test.1571442862");
		assertEquals(msg.getSrc(), "hi@ho:123");
		assertEquals(msg.getTs(), 1378902336);
	}

	@Test
	public void test2() throws XmlPullParserException, IOException, XMPPParseError {
		String toParse = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<inform xmlns=\"http://schema.mytestbed.net/omf/6.0/protocol\" mid=\"inform-test.1655934430\">\n"
                + "  <src>hi@ho:123</src>\n"
                + "  <ts>1378982237</ts>\n"
                + "  <itype>CREATION.OK</itype>\n"
                + "  <props xmlns:tik=\"http://www.tik.ee.ethz.ch/\">\n"
                + "    <tik:slippermen-line-1 type=\"string\">I wandered lonely as a cloud</tik:slippermen-line-1>\n"
                + "    <tik:slippermen-line-2 type=\"string\">Till I came upon this dirty street</tik:slippermen-line-2>\n"
                + "  </props>\n"
                + "</inform>\n";
		
		OMFMessage msg = parser.XMLParse(toParse);
		assertEquals(msg.getMessageId(), "inform-test.1655934430");
		assertEquals(msg.getSrc(), "hi@ho:123");
		assertEquals(msg.getTs(), 1378982237);
	}

	@Test
	public void test3() throws XmlPullParserException, IOException, XMPPParseError {
		String toParse = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<inform xmlns=\"http://schema.mytestbed.net/omf/6.0/protocol\" mid=\"3ff462e5-430e-491c-b864-5367d802ade5\">\n"
				+ "  <props>\n"
				+ "    <status_type type=\"string\">APP_EVENT</status_type>\n"
				+ "    <event type=\"string\">EXIT</event>\n"
				+ "    <app type=\"string\">/bin/date</app>\n"
				+ "    <exit_code type=\"integer\">0</exit_code>\n"
				+ "    <msg type=\"integer\">0</msg>\n"
				+ "    <state type=\"string\">stopped</state>\n"
				+ "    <seq type=\"integer\">3</seq>\n"
				+ "    <uid type=\"string\">11eb6a7a-c294-4d7d-8158-9e3f7ace5511</uid>\n"
				+ "    <hrn type=\"string\">/bin/date</hrn>\n"
				+ "  </props>\n"
				+ "  <ts>1379506828</ts>\n"
				+ "  <src>xmpp://11eb6a7a-c294-4d7d-8158-9e3f7ace5511@neuhaust-nb</src>\n"
				+ "  <itype>STATUS</itype>\n"
				+ "</inform>\n";
		
		OMFMessage msg = parser.XMLParse(toParse);
		assertEquals(msg.getMessageId(), "3ff462e5-430e-491c-b864-5367d802ade5");
		assertEquals(msg.getSrc(), "xmpp://11eb6a7a-c294-4d7d-8158-9e3f7ace5511@neuhaust-nb");
		assertEquals(msg.getTs(), 1379506828);
		assertEquals(msg.getProperties().getType("event"), KeyType.STRING);
		assertEquals(msg.getProperties().getValue("event"), "EXIT");
		assertEquals(msg.getProperties().getType("exit_code"), KeyType.INTEGER);
		assertEquals(msg.getProperties().getValue("exit_code"), "0");
	}

	@Test
	public void test4() throws XmlPullParserException, IOException, XMPPParseError {
		String toParse = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<inform xmlns=\"http://schema.mytestbed.net/omf/6.0/protocol\" mid=\"38e4419b-9f36-49a0-942c-f5b275b71fe7\">\n"
				+ "  <props>\n"
				+ "    <membership type=\"array\">\n"
				+ "      <it type=\"string\">xmpp://1d895ab5-01c2-4fa3-9427-d58a0de40fb7@neuhaust-nb</it>\n"
				+ "    </membership>\n"
				+ "  </props>\n"
				+ "  <ts>1379501612</ts>\n"
				+ "  <src>xmpp://rc-test-0001@neuhaust-nb</src>\n"
				+ "  <itype>STATUS</itype>\n"
				+ "</inform>\n";
		
		OMFMessage msg = parser.XMLParse(toParse);
		assertEquals(KeyType.ARRAY, msg.getProperties().getType("membership"));
		String[] a = msg.getProperties().getArrayValue("membership");
		assertEquals(1, a.length);
		assertEquals("xmpp://1d895ab5-01c2-4fa3-9427-d58a0de40fb7@neuhaust-nb", a[0]);
	}

	@Test
	public void test5() throws XmlPullParserException, IOException, XMPPParseError {
		String toParse = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<inform xmlns=\"http://schema.mytestbed.net/omf/6.0/protocol\" mid=\"38e4419b-9f36-49a0-942c-f5b275b71fe7\">\n"
				+ "  <props>\n"
				+ "    <membership type=\"hash\">\n"
				+ "      <test type=\"string\">a test</test>\n"
				+ "    </membership>\n"
				+ "  </props>\n"
				+ "  <ts>1379501612</ts>\n"
				+ "  <src>xmpp://rc-test-0001@neuhaust-nb</src>\n"
				+ "  <itype>STATUS</itype>\n"
				+ "</inform>\n";
		
		OMFMessage msg = parser.XMLParse(toParse);
		assertEquals(KeyType.HASH, msg.getProperties().getType("membership"));
		
		Map<String, String> m = msg.getProperties().getHashValue("membership");
		assertEquals(1, m.keySet().size());
		assertTrue(m.containsKey("test"));
		assertEquals("a test", m.get("test"));
		assertFalse(m.containsKey("no-test"));
	}
}
