package controllers.keyDescriptions.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.authentication.keys.KeyUtils;
import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.authorization.IKeyDescriptionAuthorizator;
import fr.cea.ig.authorization.Authorized.AdminAction;
import play.Logger;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.Headers;
import play.mvc.Http.Session;

public interface AuthorizedApiKey {
	
	@With(RenewAction.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@Inherited
	@Documented
	public @interface Renew {	}
	
	public static class RenewAction extends AdminAction {
		
		static final Pattern URL_KEY_CODE_REGEX = Pattern.compile("/api/keys/(.{" + KeyUtils.CODE_LENGTH + "})/renew");

		@Inject
		public RenewAction(IAuthorizator authorizator, IKeyDescriptionAuthorizator keyAuthorizator) {
			super(authorizator, keyAuthorizator);
		}
		
		private String getApiKeyCode(final play.mvc.Http.Context context) {
			Headers headers = context.request().getHeaders();
			String apiKey = KeyUtils.HeaderUtils.getApiKey(headers);
			return KeyUtils.DecodeUtils.getCodeFromKey(apiKey);
		}
		
		private String getUrlCode(final play.mvc.Http.Context context) {
			String urlPath = context.request().path();
			Matcher pathMatcher = URL_KEY_CODE_REGEX.matcher(urlPath);
			if(!pathMatcher.find()) throw new IllegalStateException("Invalid url path for renew : " + String.valueOf(urlPath));
			return pathMatcher.group(1);
		}
		
		private boolean isKeyCodeMatchingUrlCode(final play.mvc.Http.Context context) {
			String apiKeyCode = getApiKeyCode(context);
			String urlCode = getUrlCode(context);
			Logger.debug("Verify if apiKey '{}' can renew '{}'", apiKeyCode, urlCode);
			return apiKeyCode.equals(urlCode);
		}
		
		private boolean isApiKeySelfRenewal(final play.mvc.Http.Context context) {
			Session session = context.session();
			return Authentication.isAuthenticatedApiKeySession(session) && isKeyCodeMatchingUrlCode(context);
		}

		@Override
		public CompletionStage<Result> call(final play.mvc.Http.Context context) {
			return isApiKeySelfRenewal(context) ? delegate.call(context) : super.call(context);
		}

	}

}
