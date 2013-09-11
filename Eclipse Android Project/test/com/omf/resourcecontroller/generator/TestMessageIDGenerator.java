package com.omf.resourcecontroller.generator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestMessageIDGenerator {

	@Before
	public void setUp() {
		MessageIDGenerator.setPrefix("com.omf.android.test@1234.");
	}
	
	@Test
	public void testMessageIDGenerator1() {
		System.out.println(MessageIDGenerator.nextId());
		assertTrue(true);
	}

	@Test
	public void testMessageIDGenerator2() {
		System.out.println(MessageIDGenerator.nextId());
		System.out.println(MessageIDGenerator.nextId());
		System.out.println(MessageIDGenerator.nextId());
		System.out.println(MessageIDGenerator.nextId());
		assertTrue(true);
	}

}
