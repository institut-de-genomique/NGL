package fr.cea.ig.play.test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import fr.cea.ig.DBObject;
// import play.libs.Json;
import play.libs.ws.WSClient;
// import play.libs.ws.WSResponse;
import play.mvc.Http.Status;

// import com.fasterxml.jackson.databind.JsonNode;

public class ReadUpdateReadObjectTest<T extends DBObject> {
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(ReadUpdateReadObjectTest.class);
	
	/**
	 * URL to test.
	 */
	private String url;

	private Class<T> clazz;
	
	private Consumer<T> beforeUpdate;
		
	private BiConsumer<T,T> assertion;

	public ReadUpdateReadObjectTest(String url, Class<T> clazz) {
		this.url        = url;
		this.clazz      = clazz;
		beforeUpdate    = j -> { };
		assertion       = (jOld,jNew) -> { };
	}

	/**
	 * Run test using the provided web client.
	 * @param ws web client to use to run the test.
	 */
	public void run(WSClient ws) {
		// Read object from URL
		logger.debug("GET - " + url);
		T t0 = WSHelper.getObject(ws,url,clazz);
		// Apply modification to read object
		beforeUpdate.accept(t0);
		// Update
		logger.debug("PUT - " + url);		
		WSHelper.putObject(ws,url,t0,Status.OK);
		// Read updated data
		logger.debug("GET - " + url);
		T t1 = WSHelper.getObject(ws,url,clazz);
		// Run assertion
		assertion.accept(t0, t1);
	}

	// User friendly (inference) factory method 
	public static <T extends DBObject> ReadUpdateReadObjectTest<T> build(String url, Class<T> clazz) {
		return new ReadUpdateReadObjectTest<>(url,clazz);
	}
	
	/**
	 * Add an alteration to the read object before the update request is done.
	 * @param modify object alteration to add
	 * @return       this to chain fluent calls
	 */
	public ReadUpdateReadObjectTest<T> beforeUpdate(Consumer<T> modify) {
		beforeUpdate = beforeUpdate.andThen(modify);
		return this;
	}

	/**
	 * Add an assertion to this test.
	 * @param assertion assertion to add
	 * @return          this to chain fluent calls
	 */
	public ReadUpdateReadObjectTest<T> assertion(BiConsumer<T,T> assertion) {
		this.assertion = this.assertion.andThen(assertion);
		return this;
	}

}
