package models.sra.submit.util;


public class SraException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SraException() {
		super();
	}
	public SraException(String message, Throwable cause) {
		super(message, cause);
	}

	public SraException(String message) {
		super(message);
	}

	public SraException(Throwable cause) {
		super(cause);
	}

	
}