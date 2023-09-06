package models.laboratory.common.instance;

import java.util.Date;

public class TransientValuation {
	
	/**
	 * Index in a valuation history list.
	 */
	public Integer index;
	
	/**
	 * Validity.
	 */
	public TBoolean valid = TBoolean.UNSET;
	
	/**
	 * User.
	 */
	public String user;
	
	/**
	 * Date.
	 */
	public Date date;
	
	public TransientValuation() {
	}

	public TransientValuation(Valuation valuation, Integer index) {
		this.index 		= index;
		this.valid      = valuation.valid;
		this.date       = valuation.date;
		this.user       = valuation.user;
	}

}
