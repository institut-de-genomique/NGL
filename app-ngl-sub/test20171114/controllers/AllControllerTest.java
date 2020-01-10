package controllers;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ConfigurationTest.class,ExperimentsTest.class, SamplesTest.class, StudiesTest.class, SubmissionsTest.class})
public class AllControllerTest {
	
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for controllers");
		//$JUnit-BEGIN$
		
		//$JUnit-END$
		return suite;
	}

}
