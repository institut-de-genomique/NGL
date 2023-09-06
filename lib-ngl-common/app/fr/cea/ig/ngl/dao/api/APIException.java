package fr.cea.ig.ngl.dao.api;

public class APIException extends Exception {

	/**
	 * ISerializable.
	 */
	private static final long serialVersionUID = 1L;

	public APIException(String message) {
		super(message);
	}
	
	public APIException(String message, Throwable t) {
		super(message,t);
	}
	
}
