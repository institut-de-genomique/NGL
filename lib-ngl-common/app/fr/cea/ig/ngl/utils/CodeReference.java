package fr.cea.ig.ngl.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import fr.cea.ig.DBObject;

/**
 * Explicit type for references by codes of other objects.
 * Should be dropped in place as a replacement for untyped strings.
 * This is not possible as the type is used by rules that will not warn
 * of bad types until execution.
 * 
 * @author vrd
 *
 * @param <T> type of referenced objects
 */
// @SuppressWarnings("unused") // The type is used to provide information so it's actually not used
public class CodeReference<T> {

	private String code;
	
	@JsonValue
	public String getCode() {
		return code;
	}
	
	@JsonCreator
	public static <T> CodeReference<T> of(final String code) {
		CodeReference<T> ref = new CodeReference<>();
		ref.code = code;
		return ref;
	}
	
	public static <T extends DBObject> CodeReference<T> of(final T t) {
		return of(t.getCode());
	}
	
}
