package fr.cea.ig.play.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;

// Prototype: may not be used
/**
 * Extended WSClient that provides NGL specific methods. It is more or less 
 * a request factory.
 * <p>
 * Testing as 'bot' relies on the construction of the appropriately
 * configured instance. If a NGLWSclient is available:
 * <code>
 * wsBot = ws.asBot(); 
 * </code>
 * <br>
 * If a WSclient is available import static NGLWSClient and use:
 * <code>
 * ws = nws(ws);
 * wsBot = nws(ws).asBot();
 * </code>
 * 
 * @author vrd
 *
 */
public class NGLWSClient {

	/**
	 * Play WS client.
	 */
	private WSClient ws;
	
	/**
	 * Request modifiers.
	 */
	private List<Function<WSRequest,WSRequest>> mods;
	
	/**
	 * Constructor.
	 * @param c WS client
	 */
	public NGLWSClient(WSClient c) {
		ws   = c;
		mods = new ArrayList<>();
	}
	
	/**
	 * Thin constructor wrapper to be used with import static.
	 * @param c WS client
	 * @return  NGL WS client
	 */
	public static NGLWSClient nws(WSClient c) {
		return new NGLWSClient(c);
	}
	
	/**
	 * Copy constructor.
	 * @param c client to copy
	 */
	protected NGLWSClient(NGLWSClient c) {
		ws   = c.ws;
		mods = new ArrayList<>(c.mods);
	}
	
	/**
	 * Build a clone.
	 * @return cloned client
	 */
	@Override
	protected NGLWSClient clone() {
		return new NGLWSClient(this);
	}

	/**
	 * Add a modification that will be applied to requests.
	 * @param f request modification
	 * @return  client copy with added modification
	 */
	public NGLWSClient mod(Function<WSRequest,WSRequest> f) {
		NGLWSClient c = clone();
		c.mods.add(f);
		return c;
	}
	
	/**
	 * Adds a "bot" user agent in the request header.
	 * @return client copy with the bot user agent
	 */
	public NGLWSClient asBot() {
		return mod(r -> r.addHeader("User-Agent", "bot"));
	}
	
	/**
	 * Adds the application/json and UTF-8 content types.
	 * @return client copy with added modification
	 */
	public NGLWSClient asJSON() {
		return mod(r -> r.setContentType("application/json;charset=UTF-8"));
	}
	
	/**
	 * Close the client.
	 * @throws IOException an error occured when closing the client
	 */
	public void close() throws IOException {
		ws.close();
	}
	
	/**
	 * Assert WSResponse status.
	 * @param message  on failure message
	 * @param response response to check 
	 * @param status   status to assert
	 * @return         response or an assertion failure exception
	 */
	public static WSResponse assertResponseStatus(String message, WSResponse response, int status) {
		assertEquals(message + " " + response.getBody(), status, response.getStatus());
		return response;
	}

	/**
	 * Extract response from a completion stage (request response), all exceptions
	 * are converted to runtime exceptions.
	 * @param completionStage completion stage to wait for completion
	 * @return                response
	 */
	public static WSResponse getResponse(CompletionStage<WSResponse> completionStage) {
		try {
			return  completionStage.toCompletableFuture().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Build a request for an URL.
	 * @param url URL
	 * @return    request
	 */
	public WSRequest url(String url) {
		WSRequest r = ws.url(url);
		for (Function<WSRequest,WSRequest> f : mods)
			r = f.apply(r);
		return r;
	}
	
	// --------------------------------------------------------------
	// ---- GET
	
	/**
	 * Execute an HTTP GET on the provided URL.
	 * @param url URL
	 * @return    response
	 */
	public WSResponse get(String url) {
		return getResponse(url(url).get());
	}

	/**
	 * Execute an HTTP GET on the URL expecting a specific HTTP response
	 * status.
	 * @param url    URL
	 * @param status expected response status
	 * @return       response
	 */
	public WSResponse get(String url, int status) {
		return assertResponseStatus("GET " + url, get(url), status);
	}

	/**
	 * Get an object from an URL JSON response, asserting an OK status.
	 * @param <T>   type of encoded object
	 * @param url   URL
	 * @param clazz class of JSON encoded object
	 * @return      Object read from GET URL 
	 */
	public <T> T getObject(String url, Class<T> clazz) {
		return Json.fromJson(Json.parse(get(url,Status.OK).getBody()),clazz);
	}

	// -------------------------------------------------------------
	// ---- PUT

	/**
	 * Execute PUT on the given URL with the given data.
	 * @param url     URL to PUT
	 * @param payload text to send
	 * @return        response
	 */
	public WSResponse put(String url, String payload) {
		return getResponse(url(url).setContentType("application/json;charset=UTF-8").put(payload));
	}
		
	public WSResponse putObject(WSClient ws, String url, Object payload) {
		return put(url, Json.toJson(payload));
	}
	
	public WSResponse put(String url, String payload, int status) {
		return assertResponseStatus("PUT " + url, put(url, payload), status);
	}

	public WSResponse putObject(String url, Object payload, int status) {
		return put(url, Json.toJson(payload), status);
	}
	
	public WSResponse put(String url, JsonNode payload) {
		return put(url, payload.toString());
	}
	
	public WSResponse put(String url, JsonNode payload, int status) {
		return put(url, payload.toString(), status);
	}
	
	// -------------------------------------------------------------
	// ------ POST
	
	public WSResponse post(String url, String payload) {
		return getResponse(url(url).setContentType("application/json;charset=UTF-8").post(payload));
	}

	public WSResponse post(String url, String payload, int status) {
		return assertResponseStatus("POST " + url, post(url, payload), status);
	}

	public WSResponse post(String url, JsonNode payload) {
		return post(url, payload.toString());
	}

	public WSResponse post(String url, JsonNode payload, int status) {
		return assertResponseStatus("POST " + url, post(url,payload), status);
	}
	
	public WSResponse postObject(String url, Object object) {
		return post(url,Json.toJson(object));
	}
	
	public WSResponse postObject(String url, Object object, int status) {
		return assertResponseStatus("POST " + url, postObject(url,object), status);
	}

	// --------------------------------------------------------------------
	// -------- DELETE
	
	public WSResponse delete(String url) {
		return getResponse(url(url).delete());
	}
	
	public WSResponse delete(String url, int status) {
		WSResponse r = delete(url);
		assertEquals("DELETE " + url + " " + r.getBody(), status, r.getStatus());
		return r;
	}

	// ---------------------------------------------------------------------------
	// ---- HEAD
	
	public WSResponse head(String url) {
		return getResponse(url(url).head());
	}

	public WSResponse head(String url, int status) {
		WSResponse r = head(url);
		assertEquals("HEAD " + url + " " + r.getBody(), status, r.getStatus());
		return r; 
	}
	
}
