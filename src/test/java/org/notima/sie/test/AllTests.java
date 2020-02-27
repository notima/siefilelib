package org.notima.sie.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@SuiteClasses({ 
		SIEFileTest.class,
		TestVerRec.class
		 })
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for se.notima.sie.test");
		//$JUnit-BEGIN$

		//$JUnit-END$
		return suite;
	}

}
