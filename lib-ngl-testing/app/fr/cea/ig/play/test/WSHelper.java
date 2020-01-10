package fr.cea.ig.play.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;

// import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;

/**
 * WSClient shortcuts. Should be replaced by NGLWSClient.
 * 
 * @author vrd
 *
 */
public class WSHelper {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(WSHelper.class);
	
	/**
	 * Shortcut for HTTP get. Exceptions are converted to runtime
	 * exceptions.
	 * @param ws    web client to use
	 * @param url   URL to get 
	 * @param asbot use bot identity if true
	 * @return      web response for the given URL
	 */
	public static WSResponse get(WSClient ws, String url, boolean asbot) { // throws InterruptedException,ExecutionException {
		try {
			CompletionStage<WSResponse> completionStage = addUserAgent(asbot, ws.url(url)).get();
			WSResponse response = completionStage.toCompletableFuture().get();	
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * add user-agent definition in http request header if asbot parameter is true
	 * "User-Agent": ["bot"]
	 * @param asbot   set bot identity in request
	 * @param request base request
	 * @return        new updated request
	 */
	private static WSRequest addUserAgent(boolean asbot, WSRequest request) {
		if (asbot) {
			Map<String, List<String>> header = new HashMap<>();
			header.put("User-Agent", Arrays.asList("bot"));
			logger.debug("set User-Agent to bot");
			return request.setHeaders(header);
		} else {
			return request;
		}
	}

	/**
	 * Execute a get request and assert the response status. 
	 * @param ws     web client
	 * @param url    url to get
	 * @param status status to assert
	 * @return       request response
	 */
	public static WSResponse get(WSClient ws, String url, int status) {
		return assertResponseStatus("GET " + url, get(ws, url, false), status);
	}
	
	//TODO (Adrien) refactor authentification and permissions annotations into controllers to supress all *AsBot methods calls into Tests
	public static WSResponse getAsBot(WSClient ws, String url, int status) { 
		return assertResponseStatus("GET " + url, get(ws, url, true), status);
	}
	public static WSResponse putAsBot(WSClient ws, String url, String payload, int status) {
		return assertResponseStatus("PUT " + url, put(ws, url, payload, true), status);
	}
	public static WSResponse headAsBot(WSClient ws, String url, int status) {
		WSResponse r = head(ws, url, true);
		assertEquals("GET " + url + " " + r.getBody(), status, r.getStatus());
		return r;  
	}
	public static WSResponse postAsBot(WSClient ws, String url, String payload, int status) {
		return assertResponseStatus("POST " + url, post(ws, url, payload, true), status);
	}
	public static WSResponse deleteAsBot(WSClient ws, String url, int status) {
		WSResponse r = delete(ws, url, true);
		assertEquals("DELETE " + url + " " + r.getBody(), status, r.getStatus());
		return r;
	}
	public static WSResponse putObjectAsBot(WSClient ws, String url, Object payload, int status) {
		return put(ws,url,Json.toJson(payload), true);
	}

	// assumes HTTP OK 
	public static <T> T getObject(WSClient ws, String url, Class<T> clazz) {
		return Json.fromJson(Json.parse(get(ws,url,Status.OK).getBody()),clazz);
	}
	
	/**
	 * Short for http put with some payload.
	 * @param ws      web client to use
	 * @param url     url to put to
	 * @param payload payload to send along the put request
	 * @param asbot   use bot identity if true
	 * @return        web response
	 */
	public static WSResponse put(WSClient ws, String url, String payload, boolean asbot) { // throws InterruptedException,ExecutionException {
		try {
			CompletionStage<WSResponse> completionStage = addUserAgent(asbot, ws.url(url)).setContentType("application/json;charset=UTF-8").put(payload);
			WSResponse response = completionStage.toCompletableFuture().get();	
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static WSResponse put(WSClient ws, String url, JsonNode payload, boolean asbot) {
		return put(ws, url, payload.toString(), asbot);
	}
	
	public static WSResponse putObject(WSClient ws, String url, Object payload) {
		return put(ws, url, Json.toJson(payload), false);
	}
	
	
	/**
	 * Short for http put with some payload.
	 * @param ws      web client to use
	 * @param url     url to put to
	 * @param payload payload to send along the put request
	 * @param status  expected http status
	 * @return        web response
	 */
	public static WSResponse put(WSClient ws, String url, String payload, int status) {
		return put(ws, url, payload, status, false);
	}
	
	public static WSResponse put(WSClient ws, String url, String payload, int status, boolean asbot) {
		return assertResponseStatus("PUT " + url, put(ws, url, payload, asbot), status);
	}

	public static WSResponse put(WSClient ws, String url, JsonNode payload, int status, boolean asbot) {
		return put(ws, url, payload.toString(), status, asbot);
	}
	public static WSResponse putObject(WSClient ws, String url, Object payload, int status) {
		return put(ws, url, Json.toJson(payload), status, false);
	}
	public static WSResponse putObject(WSClient ws, String url, Object payload, int status, boolean asbot) {
		return put(ws, url, Json.toJson(payload), status, asbot);
	}
	
	/**
	 * Short for http post with some payload.
	 * @param ws      web client to use
	 * @param url     url to post to
	 * @param payload payload to send along the post request
	 * @param asbot   use bot identity if true
	 * @return        web response
	 */
	public static WSResponse post(WSClient ws, String url, String payload, boolean asbot) { // throws InterruptedException,ExecutionException {
		try {
			CompletionStage<WSResponse> completionStage = addUserAgent(asbot, ws.url(url)).setContentType("application/json;charset=UTF-8").post(payload);
			WSResponse response = completionStage.toCompletableFuture().get();	
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Short for http post with some payload.
	 * @param ws      web client to use
	 * @param url     url to post to
	 * @param payload payload to send along the post request
	 * @param status  expected http status
	 * @return        web response
	 */
	public static WSResponse post(WSClient ws, String url, String payload, int status) {
		return assertResponseStatus("POST " + url, post(ws, url, payload, false), status);
	}

	public static WSResponse post(WSClient ws, String url, JsonNode payload) {
		return post(ws, url, payload.toString(), false);
	}

	public static WSResponse post(WSClient ws, String url, JsonNode payload, int status) {
		return assertResponseStatus("POST " + url, post(ws,url,payload), status);
	}
	
	public static WSResponse postObject(WSClient ws, String url, Object object) {
		return post(ws,url,Json.toJson(object));
	}
	
	public static WSResponse postObject(WSClient ws, String url, Object object, int status) {
		return assertResponseStatus("POST " + url, postObject(ws,url,object), status);
	}
	
	public static WSResponse assertResponseStatus(String message, WSResponse response, int status) {
		assertEquals(message + " " + response.getBody(), status, response.getStatus());
		return response;
	}
	
	/**
	 * Short for http delete with some payload.
	 * @param ws      web client to use
	 * @param url     url to put to
	 * @param asbot   use bot identity if true
	 * @return        web response
	 */
	public static WSResponse delete(WSClient ws, String url, boolean asbot) { 
		try {
			CompletionStage<WSResponse> completionStage = addUserAgent(asbot, ws.url(url)).delete();
			WSResponse response = completionStage.toCompletableFuture().get();	
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Short for http delete 
	 * @param ws      web client to use
	 * @param url     url to put to
	 * @param status  expected http status
	 * @return        web response
	 */
	public static WSResponse delete(WSClient ws, String url, int status) {
		WSResponse r = delete(ws, url, false);
		assertEquals("DELETE " + url + " " + r.getBody(), status, r.getStatus());
		return r;
	}
	

	/**
	 * Execute a head request and assert the response status. 
	 * @param ws     web client
	 * @param url    url to get
	 * @param status status to assert
	 * @return       request response
	 */
	public static WSResponse head(WSClient ws, String url, int status) {
		WSResponse r = head(ws, url, false);
		assertEquals("GET " + url + " " + r.getBody(), status, r.getStatus());
		return r; 
	}
	
	/**
	 * Shorcut for http head. Exceptions are converted to runtime
	 * exceptions.
	 * @param ws    web client to use
	 * @param url   url to get 
	 * @param asbot use bot identity if true
	 * @return      web response for the given url
	 */
	public static WSResponse head(WSClient ws, String url, boolean asbot) { // throws InterruptedException,ExecutionException {
		try {
			CompletionStage<WSResponse> completionStage = addUserAgent(asbot, ws.url(url)).head();
			WSResponse response = completionStage.toCompletableFuture().get();	
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
