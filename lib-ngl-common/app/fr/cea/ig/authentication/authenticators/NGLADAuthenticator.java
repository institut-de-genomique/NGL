package fr.cea.ig.authentication.authenticators;

import javax.inject.Inject;

import com.typesafe.config.Config;

import play.data.FormFactory;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.twirl.api.Html;

/**
 * Custom rendering of active directory authentication. This is not needed as the view can be
 * be configured using configuration but it's still a proof of concept. 
 * 
 * @author vrd
 *
 */
public class NGLADAuthenticator extends ADAuthenticator {

	/**
	 * DI constructor.
	 * @param config               configuration
	 * @param injector             injector
	 * @param formFactory          form factory
	 * @param httpExecutionContext HTTP execution context
	 */
	@Inject
	public NGLADAuthenticator(Config config, Injector injector, FormFactory formFactory,
			HttpExecutionContext httpExecutionContext) {
		super(config, injector, formFactory, httpExecutionContext);
	}
	
	/**
	 * Custom login form with an error message, null being no error.
	 */
	@Override
	public Html getDefaultLoginForm(String redirect, String message) { 
		return views.html.nglLoginPassForm.render(redirect, message);
	}

}
