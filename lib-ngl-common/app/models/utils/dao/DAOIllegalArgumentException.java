package models.utils.dao;

/**
 * Null passed as a required object instance.
 * 
 * @author vrd
 *
 */
public class DAOIllegalArgumentException extends DAOException {

	/**
	 * Eclipse requested.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Illegal null argument value.
	 * @param argName argument name
	 */
	public DAOIllegalArgumentException(String argName) {
		this(argName,null);
	}
	
	/**
	 * Illegal value for given argument name.
	 * @param argName argument name
	 * @param value   illegal value
	 */
	public DAOIllegalArgumentException(String argName, Object value) {
		super("illegal value for " + argName + " (" + value + ")");
	}
	
}
