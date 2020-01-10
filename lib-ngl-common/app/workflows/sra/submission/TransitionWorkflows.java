package workflows.sra.submission;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

// TransitionWorkflows sous classe de workflows qui utilise un argument T qui implemente DBObject et TransitionObject
// ecriture si utilisation generique qui impose extends
//public class TransitionWorkflows <T extends DBObject implements TransitionObject> extends Workflows<T> {

public abstract class TransitionWorkflows <T extends DBObject & TransitionObject> extends Workflows<T> {

	@Override public void applyPreStateRules(ContextValidation validation, T exp,	State nextState)     { }
	@Override public void applyPreValidateCurrentStateRules(ContextValidation validation, T object)      { }
	@Override public void applyPostValidateCurrentStateRules(ContextValidation validation, T object)     { }
	@Override public void applySuccessPostStateRules(ContextValidation validation, T exp)                { }
	@Override public void applyErrorPostStateRules(ContextValidation validation, T exp, State nextState) { }
	@Override public void nextState(ContextValidation contextValidation, T object)                       { }

	protected final play.Logger.ALogger logger;
	
	public TransitionWorkflows() {
		logger = play.Logger.of(getClass());
	}
	
	//public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
	// pour gerer les differents etats de l'objet en fonction de l'avancement dans le workflow de la soumission
	//public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 

	public abstract Transition<T> get(String currentStateCode, String nextStateCode);
	//public abstract String getObjectType();
	public abstract ObjectType.CODE getObjectType();
	//InstanceConstants.SRA_SUBMISSION_COLL_NAME
	public abstract String getCollectionName();
	//Submission.class
	public abstract Class <T> getElementClass();
	
	@Override
	public void setState(ContextValidation contextValidation, T t, State nextState) {
		logger.debug("dans setState object code {} transition de {} vers {} ", t.getCode(), t.getState().code, nextState.code);

		contextValidation.setUpdateMode();
		
		String currentStateCode = t.getState().code;
		String nextStateCode = nextState.code;
		Transition<T> tr = get(currentStateCode, nextStateCode);  //trs.get(currentStateCode, nextStateCode);
		// TODO
		// implementer sous-classe de RuntimeException : SbmissionTransitionException avec champs currentStateCode 
		// et champs nextStateCode
		if (tr == null) {
			throw new RuntimeException("Pas de transition de " + currentStateCode + " vers "+ nextStateCode);
		}	
		
		//CommonValidationHelper.validateState(ObjectType.CODE.SRASubmission, nextState, contextValidation); 
		CommonValidationHelper.validateState(getObjectType(), nextState, contextValidation); 
		
		if (contextValidation.hasErrors()) { 
			throw new RuntimeException();
		}
		
		tr.execute(contextValidation, t, nextState); 
		
		// --------------------------
		
		
		if (!contextValidation.hasErrors()) {
			updateTraceInformation(t.getTraceInformation(), nextState);			
			// installation du nouveau state et mise à jour de l'historique :
			// t.state = updateHistoricalNextState(t.state, nextState);
			t.setState(updateHistoricalNextState(t.getState(), nextState));	
			// sauver le state dans la base avec traceInformation
			MongoDBDAO.update(getCollectionName(), getElementClass(), 
					DBQuery.is("code", t.getCode()),
					DBUpdate.set("state", t.getState()).set("traceInformation", t.getTraceInformation()));
			tr.success(contextValidation, t, nextState);
			if (contextValidation.hasErrors()) {
				
			}
		} else {
			tr.error(contextValidation, t, nextState);
		}

	}

	
}
