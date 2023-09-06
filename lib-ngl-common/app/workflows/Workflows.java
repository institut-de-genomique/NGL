package workflows;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import validation.ContextValidation;

public abstract class Workflows<T> {
	
	public abstract void applyPreStateRules(ContextValidation validation, T exp, State nextState);
	
	//public abstract void applyCurrentStateRules(ContextValidation validation, T object);
	
	public abstract void applyPreValidateCurrentStateRules(ContextValidation validation, T object);
	
	public abstract void applyPostValidateCurrentStateRules(ContextValidation validation, T object);
	
	public abstract void applySuccessPostStateRules(ContextValidation validation, T exp);
	
	public abstract void applyErrorPostStateRules(ContextValidation validation, T exp, State nextState);
	
	/**
	 * Tente de positionner l'etat nextState dans l'objet.
	 * @param contextValidation contexte de validation
	 * @param object            objet
	 * @param nextState         etat à positionner dans l'objet
	 */
	public abstract void setState(ContextValidation contextValidation, T object, State nextState);
	
	public abstract void nextState(ContextValidation contextValidation, T object);
	
	/**
	 * Update the given trace information using the next state user and date. 
	 * @param traceInformation trace information to update
	 * @param nextState        state to fetch trace information from 
	 * @return                 the updated input trace information
	 * @deprecated use {@link TraceInformation#forceModificationStamp(String, java.util.Date)}
	 */
	@Deprecated
	protected TraceInformation updateTraceInformation(TraceInformation traceInformation, State nextState) {		
		traceInformation.modifyDate = nextState.date;
		traceInformation.modifyUser = nextState.user;		
		return traceInformation;
	}

	/**
	 * Is the next state a backward state in the state order ?
	 * @param previousState previous state
	 * @param nextState     next state
	 * @return              true if the state transition is backward
	 * @deprecated use {@link models.laboratory.common.description.dao.StateDAO#isBackward(String, String)} (see {@link models.laboratory.common.description.State#find})
	 */
	@Deprecated
//	protected boolean goBack(State previousState, State nextState) {
//		models.laboratory.common.description.State nextStateDesc     = models.laboratory.common.description.State.find.get().findByCode(nextState.code);
//		models.laboratory.common.description.State previousStateDesc = models.laboratory.common.description.State.find.get().findByCode(previousState.code);
////		boolean goBack = false;
////		if (nextStateDesc.position < previousStateDesc.position) {
////			goBack = true;
////		}
////		return goBack;
//		return nextStateDesc.position < previousStateDesc.position;
//	}
	protected boolean goBack(State previousState, State nextState) {
		return models.laboratory.common.description.State.find.get().isBackward(previousState.code, nextState.code);
	}

	/*
	 * Clone State without historical
	 * @param state
	 * @return
	 */
//	protected State cloneState(State state, String user) {
//		State nextState = new State();
//		nextState.code  = state.code;
//		nextState.user  = user;
//		nextState.date  = new Date();
//		return nextState;
//	}
	
	/**
	 * Create a state with the given state code.
	 * @param state state to get code from
	 * @param user  user
	 * @return      new state with the given state code, user and the current date ({@link State#State(String, String)}) 
	 */
	@Deprecated
	protected State cloneState(State state, String user) {
		return new State(state.code, user);
	}
	
	/**
	 * Constructor call.
	 * @param stateCode state code
	 * @param user      user
	 * @return          new state
	 * @deprecated use {@link State#State(String, String)}
	 */
	@Deprecated
//	protected static State newState(String stateCode, String user) {
//		State nextState = new State();
//		nextState.code  = stateCode;
//		nextState.user  = user;
//		nextState.date  = new Date();
//		return nextState;
//	}
	protected static State newState(String stateCode, String user) {
		return new State(stateCode, user);
	}
	
	/**
	 * Create the history in the next state from the previous state history.
	 * @param previousState previous state
	 * @param nextState     next state to create history for
	 * @return              next state
	 * @deprecated use {@link State#createHistory(State)}
	 */
	@Deprecated
//	protected State updateHistoricalNextState(State previousState, State nextState) {
//		if (previousState.historical == null) {
//			nextState.historical = new HashSet<>(0);
//			nextState.historical.add(new TransientState(previousState, nextState.historical.size()));
//		} else {
//			nextState.historical = previousState.historical;
//		}
//		nextState.historical.add(new TransientState(nextState, nextState.historical.size()));		
//		return nextState;
//	}
	public static State updateHistoricalNextState(State previousState, State nextState) {
		return nextState.createHistory(previousState);
	}
	
}
