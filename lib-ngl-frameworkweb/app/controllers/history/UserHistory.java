package controllers.history;

import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.RequestBody;
import play.mvc.Http.Session;
import play.mvc.Result;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.LFWConfig;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

/** 
 * Write user action into database.
 * 
 * Use with :  @With(UserHistory.class)
 *
 * This class handle the request of the user and write the action to database
 *
 * @author ydeshayes
 * 
 */
public class UserHistory extends Action.Simple {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(UserHistory.class);
	
	/**
	 * Configuration path for user action tracing.
	 */
	public static final String USER_ACTION_TRACE_PATH = "useraction.trace";
	
	/**
	 * Configuration.
	 */
	// private final IGConfig config;
	private final LFWConfig config;
	
	/**
	 * Constructor.
	 * @param config play configuration to use
	 */
	@Inject
	public UserHistory(LFWConfig config) {
		this.config = config;
	}
	
	private String getUserLogin(Session session) {
		return Authentication.isApiKeyUser() ? Authentication.getApiKeyUser(session) : Authentication.getUser(session);
	}
	
	private String getQueryStringAsJson(Context context) {
		Map<String, String[]> queryString = context.request().queryString();
		JsonNode json = Json.toJson(queryString);
		return json.toString();
	}
	
	@Override
	//function called by play
	// public  F.Promise<Result> call(Http.Context context) throws Throwable {
	public CompletionStage<Result> call(final play.mvc.Http.Context context) {
		// if (Play.application().configuration().getBoolean("useraction.trace") != null
		if (config.getBoolean(USER_ACTION_TRACE_PATH,false)) {
			// && Play.application().configuration().getBoolean("useraction.trace") != false){
			
			// F.Promise<Result> res = null;
			CompletionStage<Result> res = null;
			
			if (context.request().uri().startsWith("/api/") && !context.request().uri().contains("/authentication")) {
				// String login = context.request().username();
				// String login = fr.cea.ig.authentication.Helper.username(context.request());
				// String login = fr.cea.ig.authentication.Helper.username(context.session());
				String login = getUserLogin(context.session());
				String params = getQueryStringAsJson(context);
				String action = context.request().toString();
				
				String body = "{}";
				RequestBody rb = context.request().body();
				if(rb != null && rb.asJson() != null){
					body = rb.asJson().toString();
				}
				
				logger.debug("running delegate " + delegate);
				long start = System.currentTimeMillis();
				res = delegate.call(context);
				long timeRequest = (System.currentTimeMillis() - start);
				logger.info("(" + login + ") - " + action + " -> " + (System.currentTimeMillis() - start) + " ms.");
				MongoDBDAO.save("UserHistory", new UserAction(login,params,body,action,timeRequest));
				logger.debug("saved action, user:" + login + ", action:" + action);
			} else {
				res = delegate.call(context);
			}
			return res;
		} else {
			return delegate.call(context);
		}
	}
	
}


