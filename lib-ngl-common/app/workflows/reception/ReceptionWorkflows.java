package workflows.reception;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.laboratory.reagent.instance.AbstractReception;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

public class ReceptionWorkflows extends Workflows<AbstractReception> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReceptionWorkflows.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyPreStateRules(ContextValidation contextValidation, AbstractReception reception, State nextState) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation contextValidation, AbstractReception reception) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation contextValidation, AbstractReception reception) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applySuccessPostStateRules(ContextValidation contextValidation, AbstractReception reception) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyErrorPostStateRules(ContextValidation contextValidation, AbstractReception reception, State nextState) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setState(ContextValidation contextValidation, AbstractReception reception, State nextState) {
		contextValidation.setUpdateMode();
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.ReagentReception, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(reception.state.code)) {
			boolean backward = models.laboratory.common.description.State.find.get().isBackward(reception.state.code, nextState.code);
			if (backward) 
				logger.debug("{} : back to the workflow. {} -> {}", reception.code, reception.state.code, nextState.code);
			reception.traceInformation.forceModificationStamp(nextState.user, nextState.date); 
			reception.state = nextState.createHistory(reception.state);
			
			MongoDBDAO.update(InstanceConstants.REAGENT_RECEPTION_COLL_NAME,  AbstractReception.class, 
					DBQuery.is("code", reception.code),
					DBUpdate.set("state", reception.state).set("traceInformation",reception.traceInformation));
			applySuccessPostStateRules(contextValidation, reception);
			nextState(contextValidation, reception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void nextState(ContextValidation contextValidation, AbstractReception reception) {
		
	}

	

}
