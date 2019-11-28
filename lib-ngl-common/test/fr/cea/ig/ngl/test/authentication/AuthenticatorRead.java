package fr.cea.ig.ngl.test.authentication;

import java.util.concurrent.CompletionStage;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.authentication.authenticators.AbstractSimpleAuthenticator;
import play.mvc.Action;
import play.mvc.Result;
import play.mvc.Http.Context;

/**
 * Authenticates as {@link Identity#Read}.
 * 
 * @author vrd
 *
 */
public class AuthenticatorRead extends AbstractSimpleAuthenticator {

	@Override
	public CompletionStage<Result> authenticate(Context context, Action<?> delegate) {
		Authentication.authenticateSession(context.session(), Identity.Read.toString());
		return delegate.call(context);
	}

}
