package ngl.bi;

import org.junit.BeforeClass;

import fr.cea.ig.play.test.AbstractServerTest;
import fr.cea.ig.play.test.CompleteTestServer;
import fr.cea.ig.play.test.NGLWSClient;
import play.Application;

class CompleteBITestServer extends CompleteTestServer {
	
	@Override
	public Application createApplication() { 
		return Global.devapp();
	}
	
}

public class AbstractBIServerTest extends AbstractServerTest {
	
	protected static NGLWSClient wsBot;
	
	/**
	 * Initialize test application.
	 */
	@BeforeClass
	public static void startTestApplication() {
		initFrom(new CompleteBITestServer());
		wsBot = ws.asBot();
	}

}
