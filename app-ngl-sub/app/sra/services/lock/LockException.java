package sra.services.lock;

public class LockException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public LockException(String message, Throwable e){
		super(message, e);
	}
	public LockException(String message){
		super(message);
	}
}
