package services.print;


public class PrintServicesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public PrintServicesException() {
		super();		
	}

	/*
	 * @param message
	 */
	public PrintServicesException(String message) {
		super(message);
	}

	/*
	 * @param cause
	 */
	public PrintServicesException(Throwable cause) {
		super(cause);
	}

	/*
	 * @param message
	 * @param cause
	 */
	public PrintServicesException(String message, Throwable cause) {
		super(message, cause);		
	}

}
