package com.omf.resourcecontroller.generator;

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import com.omf.resourcecontroller.generator.Properties;
import org.junit.Test;

public class TestProperties {

	@Test
	public void testProperties1() {
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.setNamespace("ethz", "http://www.ethz.ch");
		p.addKey("still-not-false", true);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties2() {
		String[] values = { "hi", "ho" };
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.setNamespace("ethz", "http://www.ethz.ch");
		p.addKey("some-string", "my_string_value");
		p.addKey("seven-times-eight", 5.6);
		p.addKey("not-false", true);
		p.addKey("some-values", values, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties3() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("my-key", "value_for_keyA");
		m.put("another-key", "true");
		m.put("a-third-key", "123");
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.setNamespace("ethz", "http://www.ethz.ch");
		p.addKey("some-string-values", m, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties4() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("my-key", "value_for_keyA");
		m.put("another-key", "true");
		m.put("a-third-key", "123");
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.addKey("some-string-values", m, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

}
