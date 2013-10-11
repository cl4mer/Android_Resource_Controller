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

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import com.omf.resourcecontroller.generator.Properties;
import com.omf.resourcecontroller.generator.Properties.KeyType;

import org.junit.Test;

public class TestProperties {

	@Test
	public void testProperties1() {
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.setNamespace("ethz", "http://www.ethz.ch");
		p.addKey("still-not-false", "true", KeyType.BOOLEAN);
		System.out.println(p.toString());
		assertTrue(true);
	}

	@Test
	public void testProperties2() {
		String[] values = { "hi", "ho" };
		Properties p = new Properties(Properties.MessageType.PROPS);
		p.setNamespace("ethz", "http://www.ethz.ch");
		p.addKey("some-string", "my_string_value", KeyType.STRING);
		p.addKey("seven-times-eight", "5.6", KeyType.FIXNUM);
		p.addKey("not-false", "true", KeyType.BOOLEAN);
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
