package fr.cea.ig.authorization;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

//import controllers.authorisation.Permission;
import fr.cea.ig.authentication.Authentication;
import play.mvc.Action;
import play.mvc.Result;

public abstract class AuthorizedAction extends Action.Simple {

	/**
	 * Authorizator delegate.
	 */
	private final IAuthorizator authorizator;

	@Inject
	public AuthorizedAction(IAuthorizator authorizator) {
		this.authorizator = authorizator;
	}

	@Override
	public CompletionStage<Result> call(final play.mvc.Http.Context context) {
		if (!Authentication.isAuthenticatedSession(context.session()))
			return CompletableFuture.supplyAsync(() -> unauthorized("not authenticated"));
		String username = Authentication.getUser(context.session());
		// We run the authorizator implementation.
		if (authorizator.authorize(username, new String[] { getRequiredPermission().getAlias() }))
			return delegate.call(context);
		else
			return CompletableFuture.supplyAsync(() -> forbidden());
	}

	public abstract Permission getRequiredPermission();
	
}

