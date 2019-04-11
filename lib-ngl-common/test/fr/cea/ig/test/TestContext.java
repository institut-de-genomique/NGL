package fr.cea.ig.test;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.api.APIs;
import fr.cea.ig.play.test.NGLWSClient;
import play.Application;

public class TestContext {

	private final Application app;
	private final NGLWSClient client;
	private final APIs apis;
	
	@Inject
	public TestContext(Application app, NGLWSClient client) {
		this.app    = app;
		this.client = client;
		this.apis   = this.app.injector().instanceOf(APIs.class);
	}

	public Application app() {
		return app;
	}

	public NGLWSClient client() {
		return client;
	}

	public APIs apis() {
		return apis;
	}
	
	
}
