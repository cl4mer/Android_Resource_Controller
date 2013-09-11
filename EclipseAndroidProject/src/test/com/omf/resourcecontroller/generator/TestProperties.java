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
		Properties p = new Properties(Properties.MessageType.PROPS, "ethz", "http://www.ethz.ch");
		p.addKey(true);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties2() {
		String[] values = { "hi", "ho" };
		Properties p = new Properties(Properties.MessageType.PROPS, "ethz", "http://www.ethz.ch");
		p.addKey("my_string_value");
		p.addKey(5.6);
		p.addKey(true);
		p.addKey(values, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties3() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("A", "value_for_keyA");
		m.put("B", "true");
		m.put("C", "123");
		Properties p = new Properties(Properties.MessageType.PROPS, "ethz", "http://www.ethz.ch");
		p.addKey(m, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

}
