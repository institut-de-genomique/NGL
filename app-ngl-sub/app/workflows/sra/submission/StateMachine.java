package workflows.sra.submission;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.IStateReference;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

/**
 * State machine with a transition catalog implementation.
 * 
 * @author sgas
 *
 * @param <T> state machine state type
 */
public abstract class StateMachine<T extends DBObject & IStateReference> extends Workflows<T> {

	//protected final play.Logger.ALogger logger;
	private static final play.Logger.ALogger logger = play.Logger.of(StateMachine.class);

//	public StateMachine() {
//		logger = play.Logger.of(getClass());
//	}
	
	//public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
	// pour gerer les differents etats de l'objet en fonction de l'avancement dans le workflow de la soumission
	//public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 

	/**
	 * Get transition instance from current state code to next state code.
	 * @param currentStateCode current state code
	 * @param nextStateCode    next state code
	 * @return                 transition if it exists, null otherwise
	 */
	public abstract Transition<T> get(String currentStateCode, String nextStateCode);
	
	//public abstract String getObjectType();
	public abstract ObjectType.CODE getObjectType();
	//InstanceConstants.SRA_SUBMISSION_COLL_NAME
	public abstract String getCollectionName();
	//Submission.class
	public abstract Class <T> getElementClass();
	
	/**
	 * Seule methode à utiliser !
	 */
	@Override
	public void setState(ContextValidation contextValidation, T t, State nextState) throws SraValidationException, SraException {
		//logger.debug("DANS StateMachine.setState");
		if (t == null) {
			throw new SraException("setState", "objet passé en argument null");
		}
		if (nextState == null) {
			throw new SraException("setState", "nextState passé en argument null");
		}
		if (StringUtils.isBlank(nextState.user)) {
			throw new SraException("setState", "nextState passé en argument sans champs user");
		}
		if (nextState.date == null) {
			throw new SraException("setState", "nextState passé en argument sans champs date");
		}
		//logger.debug("dans setState object code=" + t.getCode() + ", transition de " + t.getState().code + " vers " + nextState.code);
		contextValidation.setUpdateMode();
		
		String currentStateCode = t.getState().code;
		String nextStateCode = nextState.code;
		Transition<T> tr = get(currentStateCode, nextStateCode);
		if (tr == null) 
			throw new RuntimeException("Pas de transition de " + currentStateCode + " vers "+ nextStateCode);
		// On suppose que l'etat courrant est valide, l'etat suivant est il valide pour
		// notre type d'objet ?
		CommonValidationHelper.validateStateRequired(contextValidation, getObjectType(), nextState); 
		if (contextValidation.hasErrors()) {
			//logger.debug("contextValidation.displayErrors");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("end contextValidation.displayErrors");
			throw new RuntimeException();
		}
		// ajout du 18-11-2019 ok
		try {
			//logger.debug("appel de tr.execute");
			tr.execute(contextValidation, t, nextState); 
			//logger.debug("tr.execute ok ");
		} catch (Exception e) {
			
			contextValidation.addError("SETSTATE:: EXECUTE", e.getMessage());
		}
		
		// fin ajout du 18-11-2019

		// --------------------------
				
		if (!contextValidation.hasErrors()) {
			//logger.debug("STATE = " + t.getState().code + ", NEXTSTATE = '" + nextState.code+ "' et isAutonomous" + tr.isAutonomous());
//			if (! t.getState().code.equals(nextState.code)) {
			if (! tr.isAutonomous()) {
				//logger.debug("Dans Non Autonomous");
				//			updateTraceInformation(t.getTraceInformation(), nextState);	
				if(StringUtils.isBlank(nextState.user)) {
					throw new RuntimeException("next");
				}
				t.getTraceInformation().forceModificationStamp(nextState.user, nextState.date);			
				// installation du nouveau state et mise à jour de l'historique :
				// t.state = updateHistoricalNextState(t.state, nextState);
				//			t.setState(updateHistoricalNextState(t.getState(), nextState));	
				t.setState(nextState.createHistory(t.getState()));	
				// sauver le state dans la base avec traceInformation
				MongoDBDAO.update(getCollectionName(), getElementClass(), 
						DBQuery.is("code", t.getCode()),
						DBUpdate.set("state", t.getState()).set("traceInformation", t.getTraceInformation()));
			} else {
				//logger.debug("DANS Autonomous");
				if (! t.getState().code.equals(nextStateCode)) {
					contextValidation.addError("state.code", "code attendu '" + nextStateCode + "' et code de l'objet '" + t.getState().code + "'");
					tr.error(contextValidation, t, nextState);	
					return;
				}
			}
			tr.success(contextValidation, t, nextState);
			if (contextValidation.hasErrors()) {
				
			} 
		} else {
			tr.error(contextValidation, t, nextState);
		}
	}
	
	// --------------------------------------------------------------------------
	// workflows#Workflows unsupported methods
	
	@Override 
	public void applyPreStateRules(ContextValidation validation, T exp,	State nextState) { 
		throw new UnsupportedOperationException();
	}
	
	@Override 
	public void applyPreValidateCurrentStateRules(ContextValidation validation, T object) { 
		throw new UnsupportedOperationException();		
	}
	
	@Override 
	public void applyPostValidateCurrentStateRules(ContextValidation validation, T object) {
		throw new UnsupportedOperationException();
	}
	
	@Override 
	public void applySuccessPostStateRules(ContextValidation validation, T exp) {
		throw new UnsupportedOperationException();
	}
	
	@Override 
	public void applyErrorPostStateRules(ContextValidation validation, T exp, State nextState) {
		throw new UnsupportedOperationException();
	}
	
	@Override 
	public void nextState(ContextValidation contextValidation, T object) {
		throw new UnsupportedOperationException();
	}

}
