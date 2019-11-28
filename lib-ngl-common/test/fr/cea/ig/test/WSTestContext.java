package fr.cea.ig.test;

import fr.cea.ig.play.test.NGLWSClient;
import play.Application;

public class WSTestContext extends TestContext {

	private final NGLWSClient client;

	public WSTestContext(Application app, NGLWSClient client) {
		super(app);
		this.client = client;
	}
	
	public NGLWSClient client() {
		return client;
	}

}
