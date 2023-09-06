package services.smrtlink;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.ngl.NGLApplication;
import play.libs.ws.WSResponse;

public class SMRTLinkAuthenticator {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SMRTLinkAuthenticator.class);
	
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String AUTH_HEADER = "Authorization";
	
	private final NGLApplication app;
	private final SMRTLinkConfig config;
	
	@Inject
	public SMRTLinkAuthenticator(NGLApplication app, SMRTLinkConfig config) {
		this.app = app;
		this.config = config;
	}
	
	private boolean isAuthenticationFailed(WSResponse response) {
		return response.getStatus() != 200;
	}
	
	private WSResponse requestAuthToken(String auth, String body) throws SMRTLinkAuthorizationException {
		String tokenEndpoint = config.tokenEndpoint();
		try {
			WSResponse response = app.ws().url(tokenEndpoint).setContentType(CONTENT_TYPE).addHeader(AUTH_HEADER, auth).post(body).toCompletableFuture().get();
			if(isAuthenticationFailed(response)) {
				throw new SMRTLinkAuthorizationException("Error getting auth token from " + tokenEndpoint + " : replied with HTTP status " + response.getStatus());
			}
			return response;
		} catch (InterruptedException | ExecutionException e) {
			throw new SMRTLinkAuthorizationException("Error getting auth token from " + tokenEndpoint, e);
		}
	}
	
	private String getAuthorizationToken() throws SMRTLinkAuthorizationException  {
		String body = SMRTLinkKeyCloackHelper.getAuthenticationBody(config.getUsername(), config.getPassword());
		String auth = SMRTLinkKeyCloackHelper.getAuthenticationHeader(config.getUsername(), config.getPassword());
		WSResponse response = requestAuthToken(auth, body);	
		JsonNode jsonResponse = response.asJson();
		return jsonResponse.get("access_token").asText();
	}
	
	public String getAuthorizationTokenHeader() throws SMRTLinkAuthorizationException {
		String token = getAuthorizationToken();
		return "BEARER " + token;
	}

}
