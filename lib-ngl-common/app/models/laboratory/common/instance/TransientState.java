package models.laboratory.common.instance;

import java.util.Date;

/**
 * State history element.
 */
public class TransientState {

	/**
	 * Index in a state history list.
	 */
	public Integer index;
	
	/**
	 * State code.
	 */
	public String code;
	
	/**
	 * User.
	 */
	public String user;
	
	/**
	 * Date.
	 */
	public Date date;

	public TransientState() {
	}

	public TransientState(State state, Integer index) {
		this.index = index;
		code       = state.code;
		date       = state.date;
		user       = state.user;
	}

}
