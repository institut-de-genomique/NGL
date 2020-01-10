package fr.cea.ig.test;

import fr.cea.ig.ngl.dao.api.APIs;
import play.Application;

public class TestContext {

	private final Application app;
	private final APIs apis;
	
	public TestContext(Application app) {
		this.app    = app;
		this.apis   = this.app.injector().instanceOf(APIs.class);
	}

	public Application app() {
		return app;
	}

	public APIs apis() {
		return apis;
	}
	
}
