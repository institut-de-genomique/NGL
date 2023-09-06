package rules.services;

import javax.inject.Inject;

import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;

public class Rules6ComponentDisable extends Rules6Component {

	private static final Logger.ALogger logger = Logger.of(Rules6Component.class);
	
	@Inject
	public Rules6ComponentDisable(Application app, ApplicationLifecycle lifecycle) {
		super(app, lifecycle);
	}

	@Override
	public void onStart(Application app, ApplicationLifecycle lifecycle) {
		//Override Super Class method to disable Drools
		logger.debug("Do not start Drools");
		// By doing that we can override default binding in ngl-common for Unit Tests
	}

}
