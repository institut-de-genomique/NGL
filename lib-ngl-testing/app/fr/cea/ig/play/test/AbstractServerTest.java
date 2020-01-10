package fr.cea.ig.play.test;

// Scaladoc test import
// import scala.collection.generic.BitOperations.Int;

import java.io.IOException;

import org.junit.AfterClass;

import play.Application;
import play.test.TestServer;

/**
 * Base class for tests that require a heavy initialization.
 * Subclasses must implement a BeforeClass annotated method
 * <code>
 * {@literal @BeforeClass}
 * public static void startTestApplication() {
 *     initFrom(new CompleteTestServerSubClass());
 * }
 * </code>
 * that relies on a CompleteTestServerSubClass that extends CompleteTestServer
 * and defines the application factory method.
 * 
 * @author vrd
 *
 */
public class AbstractServerTest {
	
	/**
	 * Application created for the class defined tests.
	 */
	protected static Application  app;
	
	/**
	 * HTTP server.
	 */
	protected static TestServer   server;
	
//	/**
//	 * Web client.
//	 */
//	protected static WSClient     ws;

	protected static NGLWSClient     ws;
	
	/**
	 * Complete test server stack instance.
	 */
	protected static CompleteTestServer cts;
	
	/**
	 * Call this method with a proper CompleteTestServer subclass
	 * to setup this class static fields.
	 * @param a complete test server instance 
	 */
	public static void initFrom(CompleteTestServer a) {
		cts = a;
		a.start();
		app    = a.getApplication();
		server = a.getServer();
		ws     = a.getWSClient();
	}
	
	/**
	 * Stop the complete test server instance.
	 * @throws IOException error while stopping the server or application
	 */
	public static void stop() throws IOException {
		cts.stop();
	}
	 

	/**
	 * Shutdown test application, annotated for JUnit tests.
	 * @throws IOException error while stopping the server or the application
	 */
	@AfterClass
	public static void shutdownTestApplication() throws IOException {
		stop();
	}

}
