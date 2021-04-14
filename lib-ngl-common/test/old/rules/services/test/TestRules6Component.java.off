package rules.services.test;

import javax.inject.Inject;

import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;
import rules.services.Rules6Component;

public class TestRules6Component extends Rules6Component {

	private static final Logger.ALogger logger = Logger.of(Rules6Component.class);
	
	@Inject
	public TestRules6Component(Application app, ApplicationLifecycle lifecycle) {
		super(app, lifecycle);
		logger.debug("Enable drools for Tests");
	}

//	@Override
//	public void onStart(Application app, ApplicationLifecycle lifecycle) {
//		super.onStart(app, lifecycle);
//	}

}
