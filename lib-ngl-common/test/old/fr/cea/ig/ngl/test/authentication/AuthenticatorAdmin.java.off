package fr.cea.ig.ngl.test.authentication;

import java.util.concurrent.CompletionStage;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.authentication.authenticators.AbstractSimpleAuthenticator;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Authenticates as {@link Identity#Admin}.
 * 
 * @author vrd
 *
 */
public class AuthenticatorAdmin extends AbstractSimpleAuthenticator {

	@Override
	public CompletionStage<Result> authenticate(Context context, Action<?> delegate) {
		Authentication.authenticateSession(context.session(), Identity.Admin.toString());
		return delegate.call(context);
	}

}
