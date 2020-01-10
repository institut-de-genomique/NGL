package models.utils.dao;

public class DAOException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DAOException() {
		super();
	}

	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public DAOException(String message) {
		super(message);
	}

	public DAOException(Throwable cause) {
		super(cause);
	}

	public static void daoAssertNotNull(String name, Object value) throws DAOException {
		if (value == null)
			throw new DAOIllegalArgumentException(name);
	}

	public static DAOException wrap(Exception e) {
		return new DAOException(e);
	}
	
}
