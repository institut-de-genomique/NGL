package services.smrtlink;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.ngl.NGLApplication;
import play.libs.Json;
import play.libs.ws.WSResponse;

public class SMRTLinkServices {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SMRTLinkServices.class);
	
	private static final String AUTH_HEADER = "Authorization";
	
	private final NGLApplication app;
	private final SMRTLinkAuthenticator authenticator;
	private final SMRTLinkConfig config;
	
	@Inject
	public SMRTLinkServices(NGLApplication app, SMRTLinkAuthenticator authenticator, SMRTLinkConfig config) {
		this.app = app;
		this.authenticator = authenticator;
		this.config = config;
	}
	
	private CompletionStage<WSResponse> submit(String endpoint, String content) throws SMRTLinkAuthorizationException {
		String auth = authenticator.getAuthorizationTokenHeader();
		SMRTLinkBody body = new SMRTLinkBody(content);
		JsonNode jsonBody = body.toJson();
		return app.ws().url(endpoint).addHeader(AUTH_HEADER, auth).post(jsonBody);
	}
	
	private CompletionStage<WSResponse> query(String endpoint) throws SMRTLinkAuthorizationException {
		String auth = authenticator.getAuthorizationTokenHeader();
		return app.ws().url(endpoint).addHeader(AUTH_HEADER, auth).get();
	}
	
	public CompletionStage<WSResponse> getStatus() throws SMRTLinkAuthorizationException {
		String endpoint = config.statusEndpoint();
		return query(endpoint);
	}
	
	public CompletionStage<WSResponse> getRunDesign(String uuid) throws SMRTLinkAuthorizationException {
		String endpoint = config.runDesignEndpoint(uuid);
		return query(endpoint);
	}
	
	public CompletionStage<WSResponse> importRunDesign(RunDesign runDesign) throws SMRTLinkAuthorizationException {
		String endpoint = config.importRunDesignEndpoint();
		String csv = runDesign.asCSV();
		return submit(endpoint, csv);
	}

	private static final class SMRTLinkBody {
		
		@SuppressWarnings("unused")
		public final String content;
		
		public SMRTLinkBody(String content) {
			this.content = content;
		}
		
		public JsonNode toJson() {
			return Json.toJson(this);
		}
	}

}
