package models.laboratory.common.instance;

/**
 * Boolean with 3 states : UNSET, TRUE or FALSE.
 * 
 * @author galbini
 *
 */
public enum TBoolean {
	
	TRUE ( 1),
	FALSE( 0),
	UNSET(-1);

	public final Integer value;

	TBoolean(Integer value) {
		this.value = value;
	}

	/**
	 * Natural conversion from boolean to TBoolean.
	 * @param value value to convert
	 * @return      TBollean value
	 */
	public static TBoolean valueOf(boolean value) {
		if (value)
			return TRUE;
		return FALSE;
	}
	
	/**
	 * Natural conversion from Boolean to TBoolean.
	 * @param value value to convert
	 * @return      TBollean value
	 */
	public static TBoolean valueOf(Boolean value) {
		if (value == null)
			return UNSET;
		if (value)
			return TRUE;
		return FALSE;
	}
	
	
}
