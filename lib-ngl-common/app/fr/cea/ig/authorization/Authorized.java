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
import play.mvc.Result;
import play.mvc.With;

public interface Authorized {
	
	@With(AdminAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface Admin {	}
	
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

		public AbstractAction(IAuthorizator authorizator) {
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
			return CompletableFuture.supplyAsync(() -> forbidden());
		}

		public abstract Permission getRequiredPermission();
		
	}

	public static class AdminAction extends AbstractAction {

		@Inject
		public AdminAction(IAuthorizator authorizator) {
			super(authorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Admin; 
		}

	}

	public static class ReadAction extends AbstractAction {

		@Inject
		public ReadAction(IAuthorizator authorizator) {
			super(authorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Read;
		}

	}

	public static class WriteAction extends AbstractAction {

		@Inject
		public WriteAction(IAuthorizator authorizator) {
			super(authorizator);
		}

		@Override
		public Permission getRequiredPermission() {
			return Permission.Write;
		}

	}
	
}
