package fr.cea.ig.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import fr.cea.ig.authentication.Authentication;
import play.mvc.Action;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.With;

public interface Authorized {
	
	@With(AdminAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface Admin {	}

	@With(ChefProjetAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface ChefProjet {	}
	
	@With(ReadAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface Read { }
	
	@With(WriteAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface Write { }

	public abstract class AbstractAction extends Action.Simple {

		/**
		 * Authorizator delegate.
		 */
		private final IAuthorizator authorizator;
		
		/**
		 * KeyAuthorizator delegate
		 */
		private final IKeyDescriptionAuthorizator keyAuthorizator;

		public AbstractAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			this.authorizator = authorizator;
			this.keyAuthorizator = keyAuthorizator;
		}
		
		private CompletionStage<Result> handleNotAuthenticated() {
			return CompletableFuture.supplyAsync(() -> unauthorized("not authenticated"));
		}
		
		private boolean authorize(IAuthorizator authorizator, String userName) {
			String requiredPermission = this.getRequiredPermission().getAlias();
			String[] requiredPermissions = new String[] {requiredPermission};
			return authorizator.authorize(userName, requiredPermissions);
		}
		
		private CompletionStage<Result> handleAccessGranted(play.mvc.Http.Context context){
			return delegate.call(context);
		}
		
		private CompletionStage<Result> handleAccessForbidden(){
			return CompletableFuture.supplyAsync(() -> forbidden());
		}
		
		private CompletionStage<Result> handleAuthorization(play.mvc.Http.Context context, IAuthorizator authorizator, String userName) {
			// We run the authorizator implementation.
			if (authorize(authorizator, userName)) {
				return handleAccessGranted(context);
			} else {
				return handleAccessForbidden();
			}
		}
		
		private CompletionStage<Result> handleKeyAuthorization(play.mvc.Http.Context context, Session session){
			//Authentication.unauthenticateSession(session);
			String userName = Authentication.getApiKeyUser(session);
			return this.handleAuthorization(context, keyAuthorizator, userName);
		}
		
		private CompletionStage<Result> handleClassicAuthorization(play.mvc.Http.Context context, Session session){
			String userName = Authentication.getUser(session);
			return this.handleAuthorization(context, authorizator, userName);
		}

		@Override
		public CompletionStage<Result> call(final play.mvc.Http.Context context) {
			Session session = context.session();
			if (Authentication.isAuthenticatedApiKeySession(session)) {
				return handleKeyAuthorization(context, session);
			} else if (Authentication.isAuthenticatedSession(session)) {
				return handleClassicAuthorization(context, session);
			} else {
				return handleNotAuthenticated();
			}
		}

		public abstract Permission getRequiredPermission();
		
	}

	public static class AdminAction extends AbstractAction {

		@Inject
		public AdminAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			super(authorizator, keyAuthorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Admin; 
		}

	}

	public static class ChefProjetAction extends AbstractAction {

		@Inject
		public ChefProjetAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			super(authorizator, keyAuthorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.ChefProjet; 
		}

	}

	public static class ReadAction extends AbstractAction {

		@Inject
		public ReadAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			super(authorizator, keyAuthorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Read;
		}

	}

	public static class WriteAction extends AbstractAction {

		@Inject
		public WriteAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			super(authorizator, keyAuthorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Write;
		}

	}
	
}
