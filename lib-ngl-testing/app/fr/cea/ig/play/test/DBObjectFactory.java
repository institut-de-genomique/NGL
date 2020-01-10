package fr.cea.ig.play.test;

import java.io.IOException;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;

/**
 * Provide basic support for DBObject modification. This is not a factory
 * but defines static factory helper methods.
 *  
 * @author vrd
 *
 */
public class DBObjectFactory {

	/**
	 * Applies a modification to an object that is represented as JSON.
	 * @param <T>          serialized object type
	 * @param jsonNode     JSON node that is to be changed
	 * @param clazz        type of the object the JSON represents
	 * @param modification modification to apply to the object
	 * @return             JSON representation of modified object
	 */
	public static <T> JsonNode apply(JsonNode jsonNode, Class<T> clazz, Consumer<T> modification) {
		T t = Json.fromJson(jsonNode, clazz);
		modification.accept(t);
		return Json.toJson(t);
	}
	
	/**
	 * Applies a modification to an object loaded from a JSON resource.
	 * @param <T>          serialized object type
	 * @param resourceName name of the JSON resource to load
	 * @param clazz        type of object to modify
	 * @param modification modification to apply to object
	 * @return             JSON representation of the modified object
	 * @throws IOException error while decoding JSON
	 */
	public static <T> JsonNode apply(String resourceName, Class<T> clazz, Consumer<T> modification) throws IOException {
		return apply(JsonHelper.getJson(resourceName),clazz,modification);
	}
	
	// TODO: comment
	public static <T> T from(String resourceName, Class<T> clazz) throws IOException {
		return Json.fromJson(JsonHelper.getJson(resourceName),clazz);
	}
	
}
