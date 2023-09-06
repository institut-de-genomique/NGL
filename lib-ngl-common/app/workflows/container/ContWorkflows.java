package workflows.container;

import static ngl.refactoring.state.ContainerStateNames.IS;
import static ngl.refactoring.state.ContainerStateNames.IW_P;
import static ngl.refactoring.state.ContainerStateNames.UA;
import static validation.common.instance.CommonValidationHelper.FIELD_PREVIOUS_STATE_CODE;
import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.google.inject.Provider;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import ngl.refactoring.state.StateNames;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import workflows.process.ProcWorkflows;

/**
 * Container state machine implementation. 
 */
@Singleton
public class ContWorkflows {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(ContWorkflows.class);
	
	private final Provider<ContSupportWorkflows> contSupportWorkflows;
	private final ProcWorkflows                  procSupportWorkflows;
	private final IDrools6Actor                rulesActor;
	
	@Inject
	public ContWorkflows(IDrools6Actor rulesActor, Provider<ContSupportWorkflows> contSupportWorkflows, ProcWorkflows procSupportWorkflows) {
		this.rulesActor           = rulesActor;
		this.contSupportWorkflows = contSupportWorkflows;
		this.procSupportWorkflows = procSupportWorkflows;
	}

	/**
	 * Empty method.
	 * @param validation validation context
	 * @param container  container
	 * @param nextState  next state
	 */
	private void applyPreStateRules(ContextValidation validation, Container container, State nextState) {
	}
	// NGL-2937 startsWith("ext-to-")	
	private void applySuccessPostStateRules(ContextValidation validation, Container container, String context, String previousStateCode, boolean updateContainerSupport) {
		// purg when pass to IS, UA or IW-P
		if (IS.equals(container.state.code) || UA.equals(container.state.code) || IW_P.equals(container.state.code)) {
			// GA improve the extraction of fromTransformationTypeCodes
			if (container.fromTransformationTypeCodes != null && container.fromTransformationTypeCodes.size() == 1) {
				String code = container.fromTransformationTypeCodes.iterator().next();
				if (code.startsWith("ext-to-")) {
					container.fromTransformationTypeCodes = null;
				}
			} else if (container.fromTransformationTypeCodes != null && container.fromTransformationTypeCodes.size() > 1) {
				logger.error("several fromTransformationTypeCodes not managed");
			}
			// put all process to F when pass container A-* to IS, UA or IW-P (only in manual mode not with dispatch popup)
			if (previousStateCode.startsWith("A")) {
				validation.addKeyToRootKeyName("processes");
				
				// reset to IW-C all process that are initiate by the container with no experiment
				MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
						        DBQuery.in("code", container.processCodes)
						               .is("state.historical.code", StateNames.IW_C)
						               .notExists("experimentCodes"))
				          .cursor
				          .forEach(process -> {
				        	  validation.addKeyToRootKeyName(process.code);
				        	  procSupportWorkflows.setState(validation, process, new State(StateNames.IW_C, validation.getUser()));
				        	  validation.removeKeyFromRootKeyName(process.code);
				          });
				
				// closed all process that are linked with the container
				MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
						        DBQuery.in("code", container.processCodes)
							           .or(DBQuery.notEquals("state.historical.code", StateNames.IW_C), 
							        	   DBQuery.is("state.historical.code", StateNames.IW_C)
							        	          .exists("experimentCodes.0")))
				          .cursor
				          .forEach(process -> {
				        	  validation.addKeyToRootKeyName(process.code);
				        	  procSupportWorkflows.setState(validation, process, new State(StateNames.F, validation.getUser()));
				        	  validation.removeKeyFromRootKeyName(process.code);
				          });
				validation.removeKeyFromRootKeyName("processes");
			}			
			container.processCodes     = null;
			container.processTypeCodes = null;
			container.contents.parallelStream().forEach(c -> { c.processProperties = null; c.processComments = null; });
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
		}
		
//		if (Boolean.TRUE.equals(validation.getObject(FIELD_UPDATE_CONTAINER_SUPPORT_STATE))) {
		if (updateContainerSupport) {
			ContainerSupport containerSupport = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,container.support.code);
			contSupportWorkflows.get().setStateFromContainers(validation, containerSupport, context);
		}
		callWorkflowRules(validation, container);
		
		if (validation.hasErrors()) {
			logger.error("Problem on ContWorkflow.applySuccessPostStateRules : " + validation.getErrors().toString());
		}
	}
	
	private void callWorkflowRules(ContextValidation validation, Container container) {
		rulesActor.tellMessage("workflow", container, validation);
	}
	
	/**
	 * Log errors.
	 * @param validation validation context
	 * @param container  container
	 * @param nextState  next state
	 */
	private void applyErrorPostStateRules(ContextValidation validation, Container container, State nextState) {
		if (validation.hasErrors()) {
			logger.error("Problem on ContWorkflow.applyErrorPostStateRules : " + validation.getErrors().toString());
		}
	}

	@Deprecated
	public void setState(ContextValidation contextValidation, Container container, State nextState) {
		String context = contextValidation.getTypedObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
		boolean updateContainerSupport = Boolean.TRUE.equals(contextValidation.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		setState(contextValidation, container, nextState, context, updateContainerSupport);
	}
	
	@Deprecated
	public void setState(ContextValidation contextValidation, Container container, State nextState, String context) {
		boolean updateContainerSupport = Boolean.TRUE.equals(contextValidation.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE));
		setState(contextValidation, container, nextState, context, updateContainerSupport);
	}
	
	/**
	 * Change container state to the provided next state.
	 * @param contextValidation       validation context
	 * @param container               container             
	 * @param nextState               next state
	 * @param context                 context
	 * @param updateContainerSupport update container supports ?
	 */
	public void setState(ContextValidation contextValidation, Container container, State nextState, String context, boolean updateContainerSupport) {
		ContextValidation currentCtxValidation = ContextValidation.createUpdateContext(contextValidation.getUser());
		currentCtxValidation.setContextObjects(contextValidation.getContextObjects());		
		ContainerValidationHelper.validateNextState(currentCtxValidation, container, nextState, context);
		if (!currentCtxValidation.hasErrors() && !nextState.code.equals(container.state.code)) {
			applyPreStateRules(currentCtxValidation, container, nextState);
			String previousStateCode = container.state.code;
			currentCtxValidation.putObject(FIELD_PREVIOUS_STATE_CODE , container.state.code);
			currentCtxValidation.putObject(FIELD_STATE_CODE , nextState.code);
			// GA improve performance to validate only field impacted by state
			//container.validate(currentCtxValidation); //in comment because no field are state dependant
			if(!currentCtxValidation.hasErrors()){
				// boolean goBack = goBack(container.state, nextState);
				boolean goBack = models.laboratory.common.description.State.find.get().isBackward(container.state.code, nextState.code);
				// if (goBack) logger.debug(container.code+" : back to the workflow. "+container.state.code +" -> "+nextState.code);
				if (goBack) logger.debug("{} : back to the workflow. {} -> {}", container.code, container.state.code, nextState.code);
				
				container.state = nextState.createHistory(container.state);
				
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,  Container.class, 
						          DBQuery.is("code", container.code),
						          DBUpdate.set("state", container.state)
						                  .set("traceInformation", container.traceInformation));
				
				applySuccessPostStateRules(currentCtxValidation, container, context, previousStateCode, updateContainerSupport);
				nextState(currentCtxValidation, container);
			} else {
				applyErrorPostStateRules(currentCtxValidation, container, nextState);
			}
		}
		if (currentCtxValidation.hasErrors()) {
			contextValidation.addErrors(currentCtxValidation.getErrors());
		}
	}

	/**
	 * Empty method.
	 * @param contextValidation validation context
	 * @param container            container
	 */
	private void nextState(ContextValidation contextValidation, Container container) {
	}

	/**
	 * Return the available container state for a experiment category code.
	 * @param categoryCode experiment category code
	 * @return             container state code
	 * @deprecated use {@link ExperimentCategory#getContainerStateFromExperimentCategory(String)}
	 */
	@Deprecated
	public String getContainerStateFromExperimentCategory(String categoryCode) {
		return ExperimentCategory.getContainerStateFromExperimentCategory(categoryCode);
	}

}
