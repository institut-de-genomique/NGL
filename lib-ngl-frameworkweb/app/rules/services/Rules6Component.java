package rules.services;

import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;
// import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Rules6Component {
	
	private static final Logger.ALogger logger = Logger.of(Rules6Component.class);
	
	@Inject
	public Rules6Component(Application          app, 
						   ApplicationLifecycle lifecycle) {
		logger.debug("injecting " + app);
		onStart(app,lifecycle);
		logger.debug("injected");
	}
	
	public void onStart(Application app, ApplicationLifecycle lifecycle) {
		logger.info("loading knowledge base");
		try {
			RulesServices6.initSingleton(app);
			logger.info("drools started");
		} catch (Exception e) {
			logger.error("error loading drools knowledge base " + e.getMessage(),e);
			// Shutdown application
			// play.Play.stop(app.getWrappedApplication());
			// app.getWrappedApplication().stop();
			// logger.info("shutting down app after drools initialization error");
			throw new RuntimeException("Rules6Component initialization failure");			
		}
	}
	
	// public void onStop(Application app) {
		// logger.info("NGL shutdown...");
	// }
	
}
