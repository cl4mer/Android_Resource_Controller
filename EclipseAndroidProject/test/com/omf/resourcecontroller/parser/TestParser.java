package com.omf.resourcecontroller.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.parser.XMPPParser;

public class TestParser {

	private XMPPParser parser;
	
	@Before
	public void setUp() {
		parser = new XMPPParser();
	}
	
	@Test
	public void test() throws XmlPullParserException, IOException {
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

}
