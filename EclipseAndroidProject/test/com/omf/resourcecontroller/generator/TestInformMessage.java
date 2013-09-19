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
