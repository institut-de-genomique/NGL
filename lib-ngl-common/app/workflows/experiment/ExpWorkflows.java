package workflows.experiment;

import static ngl.refactoring.state.ExperimentStateNames.F;
import static ngl.refactoring.state.ExperimentStateNames.IP;
import static ngl.refactoring.state.ExperimentStateNames.N;
import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;
import static validation.common.instance.CommonValidationHelper.OBJECT_IN_DB;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.authorisation.PermissionHelper;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import validation.experiment.instance.ExperimentValidationHelper;
import validation.utils.ValidationConstants;

@Singleton
public class ExpWorkflows {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(ExpWorkflows.class);
	
	private final ExpWorkflowsHelper expWorkflowsHelper;
	
	@Inject
	public ExpWorkflows(ExpWorkflowsHelper expWorkflowsHelper) {
		this.expWorkflowsHelper = expWorkflowsHelper;
	}
	
	/*
	 * Context parameter "updateContentProperties".
	 */
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Experiment exp) {
		if (N.equals(exp.state.code)) {
			expWorkflowsHelper.updateXCodes(exp);
			if (validation.isUpdateMode()) {
				expWorkflowsHelper.updateRemoveContainersFromExperiment(exp, validation, new State(ExperimentCategory.getContainerStateFromExperimentCategory(exp.categoryCode), validation.getUser())); 
				expWorkflowsHelper.updateAddContainersToExperiment     (exp, validation, new State("IW-E", validation.getUser()));				
			}			 						
		} else if (IP.equals(exp.state.code)) {
			expWorkflowsHelper.updateXCodes(exp); // GA 22/01/2016 hack for old experiment without contents, remove in 03/2016
			expWorkflowsHelper.updateOutputContainerCode(exp);
			expWorkflowsHelper.updateOutputContainerCodes(exp);
			expWorkflowsHelper.updateATMContainerContents(exp);		
			expWorkflowsHelper.updateWithNewSampleCodesIfNeeded(exp);
		} else if (F.equals(exp.state.code)) {
			expWorkflowsHelper.updateATMContainerContents(exp);		
			expWorkflowsHelper.updateWithNewSampleCodesIfNeeded(exp);
			if (PermissionHelper.checkPermission(validation.getUser(), "admin") && validation.getObject("updateContentProperties") != null){
				Experiment dbExp = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, exp.code);
				validation.putObject(OBJECT_IN_DB, dbExp); 
			}
		}
		expWorkflowsHelper.updateStatus  (exp, validation);
		expWorkflowsHelper.updateComments(exp, validation);		
	}

	/*
	 * Context parameter "updateContentProperties" .
	 */
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Experiment exp) {
		if ("F".equals(exp.state.code)) {
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)) {
				expWorkflowsHelper.updateQCResultInInputContainers(validation, exp);
			}
			if (PermissionHelper.checkPermission(validation.getUser(), "admin") && validation.getObject("updateContentProperties") != null) {
				Experiment oldExp = (Experiment) validation.getObject(OBJECT_IN_DB);
				expWorkflowsHelper.updateNewSamplesIfNeeded(validation, exp);
				expWorkflowsHelper.updateContentPropertiesWithExperimentContentProperties(validation, exp, oldExp);
			}
		}	
	}
	
	/**
	 * Before update hook. 
	 * @param validation validation context
	 * @param exp        experiment
	 * @param nextState  next state
	 */
	public void applyPreStateRules(ContextValidation validation, Experiment exp, State nextState) {		
		exp.traceInformation.forceModificationStamp(nextState.user, nextState.date); 			
		expWorkflowsHelper.updateStatus(exp, validation);
		if (N.equals(nextState.code)) {
			expWorkflowsHelper.updateComments(exp, validation);
			expWorkflowsHelper.updateXCodes(exp); 	
		} else if (IP.equals(nextState.code)) {
			expWorkflowsHelper.updateATMs(exp, false);	
			expWorkflowsHelper.createNewSampleCodesIfNeeded(exp, validation);
			expWorkflowsHelper.createNewSamplesIfNeeded(exp, validation);
			expWorkflowsHelper.updateOutputContainerCodes(exp);
		} else if (F.equals(nextState.code)) {
			ExperimentValidationHelper.validateGlobalStorageCodes(exp.categoryCode, exp.atomicTransfertMethods, validation);
			expWorkflowsHelper.updateATMs(exp, false);
			expWorkflowsHelper.createNewSampleCodesIfNeeded(exp, validation);
			expWorkflowsHelper.createNewSamplesIfNeeded(exp, validation);
			expWorkflowsHelper.updateOutputContainerCodes(exp);
			expWorkflowsHelper.createOutputContainerSupports(exp, validation);
		}
	}

	/**
	 * After update success hook (context parameter {@link ContainerValidationHelper#FIELD_UPDATE_CONTAINER_SUPPORT_STATE}).
	 * @param validation validation context
	 * @param exp        experiment
	 * @deprecated use {@link #applySuccessPostStateRules(ContextValidation, Experiment, boolean)}
	 */
	@Deprecated
	public void applySuccessPostStateRules(ContextValidation validation, Experiment exp) {
		boolean updateContainerSupports = Boolean.TRUE.equals(validation.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		applySuccessPostStateRules(validation, exp, updateContainerSupports);
	}
	
	/**
	 * Post update 'update' (asynchronous drools execution).
	 * @param validation              validation context
	 * @param exp                     experiment
	 * @param updateContainerSupports update experiment container supports 
	 */
	public void applySuccessPostStateRules(ContextValidation validation, Experiment exp, boolean updateContainerSupports) {
		expWorkflowsHelper.linkExperimentWithProcesses(exp, validation);
		if (N.equals(exp.state.code)) {
			expWorkflowsHelper.updateStateOfInputContainers(validation, exp, new State("IW-E", validation.getUser()), updateContainerSupports);
			expWorkflowsHelper.updateStateOfInputContainerSupports(exp, validation);					
		} else if (IP.equals(exp.state.code)) {		
			expWorkflowsHelper.updateStateOfInputContainers(validation, exp, new State("IU", validation.getUser()), updateContainerSupports);
			expWorkflowsHelper.updateStateOfInputContainerSupports(exp, validation);
			expWorkflowsHelper.updateStateOfProcesses(exp,  new State("IP", validation.getUser()), validation);			
		} else if (F.equals(exp.state.code)) {
			expWorkflowsHelper.updateStateOfInputContainers(validation, exp, new State("IW-D", validation.getUser()), updateContainerSupports);
			expWorkflowsHelper.updateStateOfInputContainerSupports(exp, validation);				
			expWorkflowsHelper.updateStateOfProcesses(exp, new State("IP", validation.getUser()), validation);
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)) {
				expWorkflowsHelper.updateInputContainers(exp, validation);
			}
		}
		expWorkflowsHelper.callWorkflowRules(validation, exp);
		if (validation.hasErrors()) {
			logger.error("Problem on ExpWorkflow.applySuccessPostStateRules : "+validation.getErrors().toString());
		}
	}
	
	public void applyErrorPostStateRules(ContextValidation validation, Experiment exp, State nextState){
		ContextValidation errorValidation = ContextValidation.createUndefinedContext(validation.getUser());
		errorValidation.setContextObjects(validation.getContextObjects());
		
		if (N.equals(nextState.code)) {
			//
		} else if (IP.equals(nextState.code)) {
			//NGL-2830 les codes containers de sortie générées ne sont plus nettoyés de l'expérience car non sauvegardés en base
			//expWorkflowsHelper.removeOutputContainerCode(exp, errorValidation);
			expWorkflowsHelper.deleteSamplesIfNeeded(exp, errorValidation); //Need to clean the output container and replace new sample by old sample => DONE NGL-2830
		} else if (F.equals(nextState.code)) {
			expWorkflowsHelper.deleteOutputContainerSupports(exp, errorValidation);
			expWorkflowsHelper.deleteSamplesIfNeeded(exp, errorValidation);
		}
		if (errorValidation.hasErrors()) {
			logger.error("Problem on ExpWorkflow.applyErrorPostStateRules : "+errorValidation.getErrors().toString());
		}
	}
	
	public void setState(ContextValidation contextValidation, Experiment exp, State nextState) {
		contextValidation.setUpdateMode();
		CommonValidationHelper.validateStateRequired(contextValidation, exp.typeCode, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(exp.state.code)) {
			applyPreStateRules(contextValidation, exp, nextState);
			contextValidation.putObject(FIELD_STATE_CODE , nextState.code);
			exp.validate(contextValidation, nextState.code);
			if (!contextValidation.hasErrors()) {
				boolean backward = models.laboratory.common.description.State.find.get().isBackward(exp.state.code, nextState.code);
				if (backward) logger.debug("{} : back to the workflow. {} -> {}", exp.code, exp.state.code, nextState.code);
				exp.state = nextState.createHistory(exp.state);
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME,  Experiment.class, 
						          DBQuery.is("code", exp.code),
						          DBUpdate.set("state",            exp.state)
						                  .set("traceInformation", exp.traceInformation));
				applySuccessPostStateRules(contextValidation, exp);
				nextState(contextValidation, exp);				
			} else {
				applyErrorPostStateRules(contextValidation, exp, nextState);				
			}
		}		
	}

	/**
	 * Empty method.
	 * @param contextValidation validation context
	 * @param exp               experiment
	 */
	private void nextState(ContextValidation contextValidation,	Experiment exp) {
		// in case of experiment nothing to do !	
	}

	public void delete(ContextValidation contextValidation,	Experiment exp) {
		if (N.equals(exp.state.code) || IP.equals(exp.state.code)) {
			Set<String> containerCodes = exp.inputContainerCodes;
			expWorkflowsHelper.rollbackOnContainers(contextValidation, new State(ExperimentCategory.getContainerStateFromExperimentCategory(exp.categoryCode), contextValidation.getUser()), exp.code, containerCodes);
			expWorkflowsHelper.deleteNewSampleAndRollbackProject(contextValidation, exp);			
			if (!contextValidation.hasErrors()) {
				MongoDBDAO.delete(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			}
		} else {
			contextValidation.addError("state.code", ValidationConstants.ERROR_BADSTATE_MSG, exp.state.code);
		}
	}
	
}
