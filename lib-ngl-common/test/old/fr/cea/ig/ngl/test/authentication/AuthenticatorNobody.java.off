package fr.cea.ig.ngl.test.authentication;

import java.util.concurrent.CompletionStage;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.authentication.authenticators.AbstractSimpleAuthenticator;
import play.mvc.Action;
import play.mvc.Result;
import play.mvc.Http.Context;

/**
 * Authenticates as {@link fr.cea.ig.ngl.test.authentication.Identity#Nobody}.
 * 
 * @author vrd
 *
 */
public class AuthenticatorNobody extends AbstractSimpleAuthenticator {

	@Override
	public CompletionStage<Result> authenticate(Context context, Action<?> delegate) {
		Authentication.authenticateSession(context.session(), Identity.Nobody.toString());
		return delegate.call(context);
	}

}
