package models.laboratory.common.instance;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.common.description.ObjectType.CODE;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

// This link : {@link models.laboratory.common.instance.State}

/**
 * State, at least embedded in 
 * <ul>
 *   <li>{@link models.laboratory.container.instance.Container}.</li>
 *   <li>{@link models.laboratory.container.instance.ContainerSupport}</li>
 * </ul>
 * 
 * @author vrd
 *
 */
public class State implements IValidation {

	/**
	 * State code, possible values are defined in {@link models.laboratory.common.description.State}.
	 */
	public String code;
	
	/**
	 * Creation date.
	 */
	public Date date;
	
	/**
	 * Creation user name.
	 */
	public String user;
	
	/**
	 * State extraneous info (from some mongo definition ~Resolution).
	 */
	public Set<String> resolutionCodes;

	/**
	 * History of previous states. There does not seems to be any reason
	 * for this to be a set instead of a list expect for some MongoDB query.
	 */
	public Set<TransientState> historical;

//	public State() {
//		date = new Date();
//	}
	/**
	 * Construct a state with no code and no user but with the current date.
	 */
//	public State() {
//		date = new Date();
//	}
	public State() {
		this(null, null, new Date());
	}
	
	/**
	 * Construct a state with a code and a user and the current date.
	 * @param code code
	 * @param user user
	 */
//	public State(String code, String user) {
//		this.code = code;
//		this.user = user;
//		date      = new Date();
//	}
	public State(String code, String user) {
		this(code, user , new Date());
	}
	
	/**
	 * Construct a state with a code, a user and a date.
	 * @param code code
	 * @param user user 
	 * @param date date
	 */
	public State(String code, String user, Date date) {
		this.code = code;
		this.user = user;
		this.date = date;
	}

	/**
	 * Validate state (context parameter {@link CommonValidationHelper#FIELD_TYPE_CODE} 
	 * or {@link CommonValidationHelper#FIELD_OBJECT_TYPE_CODE}).
	 */
	@JsonIgnore
	@Override
	@Deprecated
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper.validateStateCodeRequired(contextValidation, code);
		ValidationHelper      .validateNotEmpty         (contextValidation, date, "date");
		ValidationHelper      .validateNotEmpty         (contextValidation, user, "user");
		CommonValidationHelper.validateResolutionCodes  (contextValidation,	resolutionCodes);
	}
	
	public void validate(ContextValidation contextValidation, String typeCode) {
		CommonValidationHelper.validateStateCodeRequired(contextValidation, typeCode, code);
		ValidationHelper      .validateNotEmpty         (contextValidation, date, "date");
		ValidationHelper      .validateNotEmpty         (contextValidation, user, "user");
		CommonValidationHelper.validateResolutionCodes  (contextValidation,	typeCode, resolutionCodes);
	}
	
	public void validate(ContextValidation contextValidation, CODE objectTypeCode) {
		CommonValidationHelper.validateStateCodeRequired(contextValidation, objectTypeCode, code);
		ValidationHelper      .validateNotEmpty         (contextValidation, date, "date");
		ValidationHelper      .validateNotEmpty         (contextValidation, user, "user");
		CommonValidationHelper.validateResolutionCodes  (contextValidation,	objectTypeCode, resolutionCodes);
	}
	
	/**
	 * Clone State without historical.
	 * @param  state state to clone
	 * @param  user  user identity
	 * @return       state clone
	 */
	public static State cloneState(State state, String user) {
//		State nextState = new State();
//		nextState.code = state.code;
//		nextState.date = new Date();
//		nextState.user = user;
//		return nextState;
		return new State(state.code, user);
	}
	
	/**
	 * Clone State without historical.
	 * @param  state state to clone
	 * @return       state clone
	 */
	public static State cloneState(State state) {
//		State nextState = new State();
//		nextState.code = state.code;
//		nextState.date = state.date;
//		nextState.user = state.user;
//		return nextState;
		return new State(state.code, state.user, state.date);
	}

	/**
	 * Create history from a previous state.
	 * @param previousState history base
	 * @return              this to chain calls
	 */
	public State createHistory(State previousState) {
		if (previousState.historical == null) {
			historical = new HashSet<>(0);
			historical.add(new TransientState(previousState, historical.size()));
		} else {
			historical = new HashSet<>();
			historical.addAll(previousState.historical);
		}
		historical.add(new TransientState(this, historical.size()));
		return this;
	}

}
