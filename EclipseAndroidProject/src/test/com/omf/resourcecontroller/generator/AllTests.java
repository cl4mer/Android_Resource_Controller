package com.omf.resourcecontroller.generator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestProperties.class, TestInformMessage.class, TestMessageIDGenerator.class })
public class AllTests {

}
