package workflows.process;

import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;
import static validation.common.instance.CommonValidationHelper.OBJECT_IN_DB;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.google.inject.Provider;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.processes.instance.ProcessValidationHelper;

/**
 * Process work flow. 
 */
@Singleton
public class ProcWorkflows { // extends Workflows<Process> {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(ProcWorkflows.class);
	
	/**
	 * Process work flow helper.
	 */
	private final Provider<ProcWorkflowHelper> procWorkflowsHelper;
	
	@Inject
	public ProcWorkflows(Provider<ProcWorkflowHelper> procWorkflowsHelper) {
		this.procWorkflowsHelper = procWorkflowsHelper;
	}
	
//	@Override
	public void applyPreStateRules(ContextValidation validation, Process process, State nextState) {
//		process.traceInformation = updateTraceInformation(process.traceInformation, nextState); 			
		process.traceInformation.forceModificationStamp(nextState.user, nextState.date); 			
		if("IW-C".equals(nextState.code)){
			procWorkflowsHelper.get().setIWCConfiguration(validation ,process);			
		} else if("N".equals(nextState.code)){
			procWorkflowsHelper.get().updateSampleOnContainer(validation ,process);			
		}
	}

//	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Process process) {
		Process dbProcess = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, process.code);
		validation.putObject(OBJECT_IN_DB, dbProcess);
	}
	
//	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Process process) {
		if (!"IW-C".equals(process.state.code)) {
			procWorkflowsHelper.get().updateContentProcessPropertiesAttribute(validation, process);
			procWorkflowsHelper.get().updateContentProcessCommentsAttribute(validation, process); // ajoutee pour NGL-29-13
			procWorkflowsHelper.get().updateContentPropertiesWithContentProcessProperties(validation, process);
		} else if("IW-C".equals(process.state.code)) {
			nextState(validation, process);
		}
	}
	
//	@Override
	public void applySuccessPostStateRules(ContextValidation validation, Process process) {
		if("N".equals(process.state.code)){
			procWorkflowsHelper.get().updateInputContainerToStartProcess(validation, process);			
		}
	}

//	@Override
	public void applyErrorPostStateRules(ContextValidation validation, Process process, State nextState) {
	}

//	@Override
	public void setState(ContextValidation contextValidation, Process process, State nextState) {
//		ContextValidation currentCtxValidation = new ContextValidation(contextValidation.getUser());
//		currentCtxValidation.setUpdateMode();
		ContextValidation currentCtxValidation = ContextValidation.createUpdateContext(contextValidation.getUser());
		ProcessValidationHelper.validateNextState(process, nextState, currentCtxValidation);
		if (!currentCtxValidation.hasErrors() && !nextState.code.equals(process.state.code)) {
			applyPreStateRules(currentCtxValidation, process, nextState);
			currentCtxValidation.putObject(FIELD_STATE_CODE , nextState.code);
			// GA: improve performance to validate only field impacted by state
			//process.validate(contextValidation); //in comment because no field are state dependant maybe state of container			
			if(!currentCtxValidation.hasErrors()){
//				boolean goBack = goBack(process.state, nextState);
				boolean backward = models.laboratory.common.description.State.find.get().isBackward(process.state.code, nextState.code);
				if (backward) logger.debug("{} : back to the workflow. {} -> {}", process.code, process.state.code, nextState.code);
				
//				process.state = updateHistoricalNextState(process.state, nextState);
				process.state = nextState.createHistory(process.state);
				
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME,  Process.class, 
						DBQuery.is("code", process.code),
						DBUpdate.set("state", process.state).set("traceInformation", process.traceInformation));
				
				applySuccessPostStateRules(currentCtxValidation, process);
				nextState(currentCtxValidation, process);
			} else {
				applyErrorPostStateRules(currentCtxValidation, process, nextState);
			}
		}
		if (currentCtxValidation.hasErrors()) {
			contextValidation.addErrors(currentCtxValidation.getErrors());
		}
	}

	private void nextState(ContextValidation contextValidation, Process process) {
		State nextState = new State(process.state.code, contextValidation.getUser());
		if ("IW-C".equals(process.state.code) && process.inputContainerCode != null) {
			nextState.code = "N";
		}
		setState(contextValidation, process, nextState);
	}

}
