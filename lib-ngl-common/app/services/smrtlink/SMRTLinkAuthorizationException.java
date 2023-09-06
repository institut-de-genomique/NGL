package services.smrtlink;

public class SMRTLinkAuthorizationException extends Exception {

	/**
	 * Eclipse warning
	 */
	private static final long serialVersionUID = 1L;
	
	public SMRTLinkAuthorizationException(String message) {
		super(message);
	}
	
	public SMRTLinkAuthorizationException(String message, Exception e) {
		super(message, e);
	}
	
}
