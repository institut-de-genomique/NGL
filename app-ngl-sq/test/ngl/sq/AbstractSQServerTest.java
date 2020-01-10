package ngl.sq;

import org.junit.BeforeClass;

import fr.cea.ig.play.test.AbstractServerTest;
import fr.cea.ig.play.test.CompleteTestServer;
import play.Application;

class CompleteSQTestServer extends CompleteTestServer {
	
	@Override
	public Application createApplication() { 
		return Global.devapp();
	}
	
}

public class AbstractSQServerTest extends AbstractServerTest {
	
	/**
	 * Initialize test application.
	 */
	@BeforeClass
	public static void startTestApplication() {
		initFrom(new CompleteSQTestServer());
	}

}
