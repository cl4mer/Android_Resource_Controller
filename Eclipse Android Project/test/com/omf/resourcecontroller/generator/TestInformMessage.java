package com.omf.resourcecontroller.generator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.omf.resourcecontroller.generator.InformMessageMaker.StandardMessageType;
import com.omf.resourcecontroller.generator.Properties.MessageType;

public class TestInformMessage {

	@Before
	public void setUp() {
		MessageIDGenerator.clearPrefix();
		MessageIDGenerator.setPrefix("inform-test.");
	}
	
	@Test
	public void testInform1() {
		InformMessageMaker i = new InformMessageMaker("hi@ho:123", null, StandardMessageType.CREATION_OK, null);
		Properties p = new Properties(MessageType.PROPS, "tik", "http://www.tik.ee.ethz.ch/");
		p.addKey("slippermen-line-1", "I wandered lonely as a cloud");
		p.addKey("slippermen-line-2", "Till I came upon this dirty street");
		i.addProperties(p);
		System.out.println(i.getXMLMessage());
		assertTrue(true);
	}

}
