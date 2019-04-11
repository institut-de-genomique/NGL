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
	
}
