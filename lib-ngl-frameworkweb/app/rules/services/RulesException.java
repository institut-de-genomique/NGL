package rules.services;

public class RulesException extends Exception {

	/**
	 * Serialization UID.
	 */
	private static final long serialVersionUID = 1L;

	public RulesException() {
	}

	public RulesException(String message, Throwable cause) {
		super(message, cause);
	}

	public RulesException(String message) {
		super(message);
	}

	public RulesException(Throwable cause) {
		super(cause);
	}
	
}
