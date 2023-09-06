package controllers.reagents.io.receptions.helpers;

import java.util.Date;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.reagent.instance.AbstractReception;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import ngl.refactoring.state.States;

public class ReceptionFiller {
	
	public static final String DEFAULT_RECEPTION_STATE = States.IW_U.code;
	
	private State getReceptionDefaultState(String currentUser) {
		// Default state for receptions is 'IP'
		return new State(DEFAULT_RECEPTION_STATE, currentUser, new Date());
	}
	
	public void fillReceptionInfos(AbstractReception reception, String currentUser) {
		reception.code = ReagentCodeHelper.getInstance().generateReceptionCode();
		reception.state = getReceptionDefaultState(currentUser);
		reception.traceInformation = new TraceInformation(currentUser);
	}

}
