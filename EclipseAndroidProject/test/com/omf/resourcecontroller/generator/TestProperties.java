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
		p.addKey("flag", true);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties2() {
		String[] values = { "hi", "ho" };
		Properties p = new Properties(Properties.MessageType.PROPS, "ethz", "http://www.ethz.ch");
		p.addKey("reason", "my_string_value");
		p.addKey("point-seven-times-eight", 5.6);
		p.addKey("flag", true);
		p.addKey("greetings", values, Properties.KeyType.STRING);
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
		p.addKey("values", m, Properties.KeyType.STRING);
		System.out.println(p.toString());
		assertTrue(true);
	}

}
