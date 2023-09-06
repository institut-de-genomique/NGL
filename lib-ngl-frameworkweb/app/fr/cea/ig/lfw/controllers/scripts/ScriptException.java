package fr.cea.ig.lfw.controllers.scripts;

import play.mvc.Http.RequestBody;

/**
 * Script internal execution problems.
 *  
 * @author vrd
 *
 */
public class ScriptException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct a new exception with the given message.
	 * @param message message
	 */
	public ScriptException(String message) {
		super(message);
	}
	
	/**
	 * Throws an exception if the body is not empty. 
	 * @param body request body to test
	 */
	public static void assertEmpty(RequestBody body) {
		if (body.asRaw() == null)
			return;
		if (body.asRaw().size() != 0)
			throw new ScriptException("body is not empty");
	}
	
}
