package models.utils.instance;

import java.util.HashSet;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TransientState;
import models.utils.InstanceHelpers;

public class StateHelper extends InstanceHelpers {

	public static State updateHistoricalNextState(State previousState, State nextState) {
		if (null == previousState.historical) {
			nextState.historical = new HashSet<>(0);
			nextState.historical.add(new TransientState(previousState, nextState.historical.size()));
		} else {
			nextState.historical = previousState.historical;
		}
		nextState.historical.add(new TransientState(nextState, nextState.historical.size()));		
		return nextState;
	}

	public static State cloneState(State state) {
		State nextState = new State();
		nextState.code = state.code;
		nextState.date = state.date;
		nextState.user = state.user;
		nextState.resolutionCodes = state.resolutionCodes;
		return nextState;
	}
	
}
