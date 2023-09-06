package workflows.container;

import static ngl.refactoring.state.ContainerStateNames.N;
import static ngl.refactoring.state.ContainerStateNames.A;
import static ngl.refactoring.state.ContainerStateNames.A_PF;
import static ngl.refactoring.state.ContainerStateNames.A_QC;
import static ngl.refactoring.state.ContainerStateNames.A_TF;
import static ngl.refactoring.state.ContainerStateNames.A_TM;
import static ngl.refactoring.state.ContainerStateNames.IS;
import static ngl.refactoring.state.ContainerStateNames.IU;
import static ngl.refactoring.state.ContainerStateNames.IW_D;
import static ngl.refactoring.state.ContainerStateNames.IW_E;
import static ngl.refactoring.state.ContainerStateNames.IW_P;
import static ngl.refactoring.state.ContainerStateNames.UA;
import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.container.instance.ContainerValidationHelper;

//@Singleton
//public class ContSupportWorkflows extends Workflows<ContainerSupport> {
//
//	private static final play.Logger.ALogger logger = play.Logger.of(ContSupportWorkflows.class);
//
//	private final ContWorkflows containerWorkflows;
//	private final LazyRules6Actor      rulesActor;
//	 
//	@Inject
//	public ContSupportWorkflows(NGLApplication app, ContWorkflows containerWorkflows) {
//		rulesActor              = app.rules6Actor();
//		this.containerWorkflows = containerWorkflows;
//	}
//
//	@Override
//	public void applyPreStateRules(ContextValidation validation, ContainerSupport exp, State nextState) {
//	}
//
//	@Override
//	public void applyPreValidateCurrentStateRules(ContextValidation validation, ContainerSupport object) {
//	}
//	
//	@Override
//	public void applyPostValidateCurrentStateRules(ContextValidation validation, ContainerSupport object) {
//	}
//
//	@Override
////	@Deprecated
//	public void applySuccessPostStateRules(ContextValidation validation, ContainerSupport containerSupport) {
//		
////		if ("IS".equals(containerSupport.state.code) || "UA".equals(containerSupport.state.code) || "IW-P".equals(containerSupport.state.code)) {
//		if (IS.equals(containerSupport.state.code) || UA.equals(containerSupport.state.code) || IW_P.equals(containerSupport.state.code)) {
//			// GA: improve the extraction of fromTransformationTypeCodes after refactoring inputProcessCodes and processTypeCode
//
//			boolean unsetFromExperimentTypeCodes = false;
//			if (containerSupport.fromTransformationTypeCodes != null && containerSupport.fromTransformationTypeCodes.size() == 1) {
//				String code = containerSupport.fromTransformationTypeCodes.iterator().next();
//				if (code.startsWith("ext")) unsetFromExperimentTypeCodes = true;
//			} else if (containerSupport.fromTransformationTypeCodes != null && containerSupport.fromTransformationTypeCodes.size() > 1) {
//				logger.error("several fromTransformationTypeCodes not managed");
//			}
//
//			if (unsetFromExperimentTypeCodes) {
//				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, Container.class,
//						          DBQuery.is("code",containerSupport.code), 
//						          DBUpdate.unset("fromTransformationTypeCodes"));
//			}	
//		}
//		if (Boolean.TRUE.equals(validation.getObject(FIELD_UPDATE_CONTAINER_STATE)) && 
////				("IW-P".equals(containerSupport.state.code) || "IS".equals(containerSupport.state.code) || "UA".equals(containerSupport.state.code))) {
//				(IW_P.equals(containerSupport.state.code) || IS.equals(containerSupport.state.code) || UA.equals(containerSupport.state.code))) {
//			State nextState = cloneState(containerSupport.state, validation.getUser());
//			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", containerSupport.code))
//			.cursor.forEach(c -> {				
//				containerWorkflows.setState(validation, c, nextState);
//			});
//		}
//		callWorkflowRules(validation,containerSupport);		
//
//		if (validation.hasErrors()) {
//			// TO DO: probably use : validation.displayErrors(logger);
//			logger.error("Problem on ContSupportWorkflow.applySuccessPostStateRules : " + validation.getErrors().toString());
//		}
//	}
//	
//	public  void callWorkflowRules(ContextValidation validation, ContainerSupport containerSupport) {
//		rulesActor.tellMessage("workflow", containerSupport, validation);
//	}
//
//	@Override
//	public void applyErrorPostStateRules(ContextValidation validation, ContainerSupport container, State nextState) {
//		if (validation.hasErrors()) {
//			// TO DO: probably use : validation.displayErrors(logger);
//			logger.error("Problem on ContSupportWorkflow.applyErrorPostStateRules : " + validation.getErrors().toString());
//		}
//	}
//
//	@Override
//	public void setState(ContextValidation contextValidation, ContainerSupport containerSupport, State nextState) {
////		ContextValidation currentCtxValidation = new ContextValidation(contextValidation.getUser());
////		currentCtxValidation.setUpdateMode();
//		ContextValidation currentCtxValidation = ContextValidation.createUpdateContext(contextValidation.getUser());
//		currentCtxValidation.setContextObjects(contextValidation.getContextObjects());
//		
//		ContainerSupportValidationHelper.validateNextState(currentCtxValidation, containerSupport, nextState);
//		if(!currentCtxValidation.hasErrors() && !nextState.code.equals(containerSupport.state.code)){
//			applyPreStateRules(currentCtxValidation, containerSupport, nextState);
//			currentCtxValidation.putObject(FIELD_STATE_CODE , nextState.code);
//			// GA: improve performance to validate only field impacted by state
//			// containerSupport.validate(contextValidation); //in comment because no field are state dependant
//			// GA: what is the rules to change the support state, need a support state ??
//			if (!currentCtxValidation.hasErrors()) {
//				boolean goBack = goBack(containerSupport.state, nextState);
//				if (goBack)
//					logger.debug(containerSupport.code+" : back to the workflow. "+containerSupport.state.code +" -> "+nextState.code);		
//				containerSupport.state = updateHistoricalNextState(containerSupport.state, nextState);
//				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,  Container.class, 
//						DBQuery.is("code", containerSupport.code),
//						DBUpdate.set("state", containerSupport.state).set("traceInformation", containerSupport.traceInformation));
//				
//				applySuccessPostStateRules(currentCtxValidation, containerSupport);
//				nextState(currentCtxValidation, containerSupport);
//			} else {
//				applyErrorPostStateRules(currentCtxValidation, containerSupport, nextState);
//			}
//		}
//		if (currentCtxValidation.hasErrors()) {
//			contextValidation.addErrors(currentCtxValidation.getErrors());
//		}
//	}
//
//	public void setStateFromContainers(ContextValidation contextValidation, ContainerSupport containerSupport){
//		State nextStep = getNextStateFromContainerStates(contextValidation.getUser(), ContainerSupportValidationHelper.getContainerStates(containerSupport));
//		setState(contextValidation, containerSupport, nextStep);
//	}
//
////	public State getNextStateFromContainerStates(String username, Set<String> containerStates) {
////		State nextStep = null;
////		logger.debug("States = " + containerStates);
////		if (containerStates.contains("IW-D")) {
//////			nextStep = newState("IW-D", username);			
////			nextStep = new State("IW-D", username);			
////		} else if (containerStates.contains("IU")) {
//////			nextStep = newState("IU", username);			
////			nextStep = new State("IU", username);			
////		} else if (containerStates.contains("IW-E")) {
//////			nextStep = newState("IW-E", username);			
////			nextStep = new State("IW-E", username);			
////		} else if (containerStates.contains("A-TM") && !containerStates.contains("A-QC") && !containerStates.contains("A-PF") && !containerStates.contains("A-TF")) {
//////			nextStep = newState("A-TM", username);			
////			nextStep = new State("A-TM", username);			
////		} else if (containerStates.contains("A-QC") && !containerStates.contains("A-TM") && !containerStates.contains("A-PF") && !containerStates.contains("A-TF")) {
//////			nextStep = newState("A-QC", username);			
////			nextStep = new State("A-QC", username);			
////		} else if (containerStates.contains("A-PF") && !containerStates.contains("A-TM") && !containerStates.contains("A-QC") && !containerStates.contains("A-TF")) {
//////			nextStep = newState("A-PF", username);			
////			nextStep = new State("A-PF", username);			
////		} else if (containerStates.contains("A-TF") && !containerStates.contains("A-TM") && !containerStates.contains("A-QC") && !containerStates.contains("A-PF")) {
//////			nextStep = newState("A-TF", username);			
////			nextStep = new State("A-TF", username);			
////		} else if (containerStates.contains("A-TF") || containerStates.contains("A-TM") || containerStates.contains("A-QC") || containerStates.contains("A-PF")) {
//////			nextStep = newState("A", username);			
////			nextStep = new State("A", username);			
////		} else if (containerStates.contains("IW-P")) {
//////			nextStep = newState("IW-P", username);			
////			nextStep = new State("IW-P", username);			
////		} else if (containerStates.contains("IS")) {
//////			nextStep = newState("IS", username);			
////			nextStep = new State("IS", username);			
////		} else if (containerStates.contains("UA")) {
//////			nextStep = newState("UA", username);			
////			nextStep = new State("UA", username);			
////		} else {
////			throw new RuntimeException("setStateFromContainer : states " + containerStates + " not managed");
////		}
////		logger.debug("nextStep = {}", nextStep.code);
////		return nextStep;
////	}
//	
//	public State getNextStateFromContainerStates(String username, Set<String> containerStates) {
//		State nextStep = null;
//		logger.debug("States = " + containerStates);
//		if (containerStates.contains(IW_D)) {
//			nextStep = new State(IW_D, username);			
//		} else if (containerStates.contains(IU)) {
//			nextStep = new State(IU, username);			
//		} else if (containerStates.contains(IW_E)) {
//			nextStep = new State(IW_E, username);			
//		} else if (containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_PF) && !containerStates.contains(A_TF)) {
//			nextStep = new State(A_TM, username);			
//		} else if (containerStates.contains(A_QC) && !containerStates.contains(A_TM) && !containerStates.contains(A_PF) && !containerStates.contains(A_TF)) {
//			nextStep = new State(A_QC, username);			
//		} else if (containerStates.contains(A_PF) && !containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_TF)) {
//			nextStep = new State(A_PF, username);			
//		} else if (containerStates.contains(A_TF) && !containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_PF)) {
//			nextStep = new State(A_TF, username);			
//		} else if (containerStates.contains(A_TF) || containerStates.contains(A_TM) || containerStates.contains(A_QC) || containerStates.contains(A_PF)) {
//			nextStep = new State(A, username);			
//		} else if (containerStates.contains(IW_P)) {
//			nextStep = new State(IW_P, username);			
//		} else if (containerStates.contains(IS)) {
//			nextStep = new State(IS, username);			
//		} else if (containerStates.contains(UA)) {
//			nextStep = new State(UA, username);			
//		} else {
//			throw new RuntimeException("setStateFromContainer : states " + containerStates + " not managed");
//		}
//		logger.debug("nextStep = {}", nextStep.code);
//		return nextStep;
//	}
//
//	@Override
//	public void nextState(ContextValidation contextValidation, ContainerSupport object) {
//	}
//
//}


@Singleton
public class ContSupportWorkflows {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ContSupportWorkflows.class);

	private final ContWorkflows   containerWorkflows;
	
//	private final LazyRules6Actor rulesActor;
//	 
//	@Inject
//	public ContSupportWorkflows(NGLApplication app, ContWorkflows containerWorkflows) {
//		rulesActor              = app.rules6Actor();
//		this.containerWorkflows = containerWorkflows;
//	}
	
	private final IDrools6Actor rulesActor;
	 
	@Inject
	public ContSupportWorkflows(IDrools6Actor rulesActor, ContWorkflows containerWorkflows) {
		this.rulesActor         = rulesActor;
		this.containerWorkflows = containerWorkflows;
	}

	/**
	 * Do nothing.
	 * @param validation       validation context
	 * @param containerSupport container support
	 * @param nextState        next state
	 */
	private void applyPreStateRules(ContextValidation validation, ContainerSupport containerSupport, State nextState) {
	}

	// NGL-2937 startsWith("ext-to-")
	private void applySuccessPostStateRules(ContextValidation validation, ContainerSupport containerSupport, String context) {
		if (IS.equals(containerSupport.state.code) || UA.equals(containerSupport.state.code) || IW_P.equals(containerSupport.state.code)) {
			// GA: improve the extraction of fromTransformationTypeCodes after refactoring inputProcessCodes and processTypeCode
			boolean unsetFromExperimentTypeCodes = false;
			if (containerSupport.fromTransformationTypeCodes != null && containerSupport.fromTransformationTypeCodes.size() == 1) {
				String code = containerSupport.fromTransformationTypeCodes.iterator().next();
				if (code.startsWith("ext-to-")) 
					unsetFromExperimentTypeCodes = true;
			} else if (containerSupport.fromTransformationTypeCodes != null && containerSupport.fromTransformationTypeCodes.size() > 1) {
				logger.error("several fromTransformationTypeCodes not managed");
			}
			if (unsetFromExperimentTypeCodes) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, Container.class,
						          DBQuery.is("code",containerSupport.code), 
						          DBUpdate.unset("fromTransformationTypeCodes"));
			}	
		}
		if (Boolean.TRUE.equals(validation.getObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_STATE)) && 
				(IW_P.equals(containerSupport.state.code) || IS.equals(containerSupport.state.code) || UA.equals(containerSupport.state.code))) {
			State nextState = new State(containerSupport.state.code, validation.getUser());
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("support.code", containerSupport.code))
			          .cursor
			          .forEach(c -> containerWorkflows.setState(validation, c, nextState, context, true));
		}
		callWorkflowRules(validation, containerSupport);		
		if (validation.hasErrors()) {
			logger.error("Problem on ContSupportWorkflow.applySuccessPostStateRules : " + validation.getErrors().toString());
		}
	}
	
	private  void callWorkflowRules(ContextValidation validation, ContainerSupport containerSupport) {
		rulesActor.tellMessage("workflow", containerSupport, validation);
	}

	private void applyErrorPostStateRules(ContextValidation validation, ContainerSupport container, State nextState) {
		if (validation.hasErrors()) {
			logger.error("Problem on ContSupportWorkflow.applyErrorPostStateRules : " + validation.getErrors().toString());
		}
	}

	public void setState(ContextValidation contextValidation, ContainerSupport containerSupport, State nextState, String context) {	
		ContextValidation currentCtxValidation = ContextValidation.createUpdateContext(contextValidation.getUser());
		currentCtxValidation.setContextObjects(contextValidation.getContextObjects());
		
//		ContainerSupportValidationHelper.validateNextState(currentCtxValidation, containerSupport, nextState);
		ContainerSupportValidationHelper.validateNextState(currentCtxValidation, containerSupport, nextState, context);
		if (!currentCtxValidation.hasErrors() && !nextState.code.equals(containerSupport.state.code)) {
			applyPreStateRules(currentCtxValidation, containerSupport, nextState);
			currentCtxValidation.putObject(FIELD_STATE_CODE , nextState.code);
			// GA: improve performance to validate only field impacted by state
			// containerSupport.validate(contextValidation); //in comment because no field are state dependant
			// GA: what is the rules to change the support state, need a support state ??
			if (!currentCtxValidation.hasErrors()) {
				boolean backward = models.laboratory.common.description.State.find.get().isBackward(containerSupport.state.code, nextState.code);
				if (backward)
					logger.debug("{} : back to the workflow. {} -> {}", containerSupport.code, containerSupport.state.code, nextState.code);		
				containerSupport.state = nextState.createHistory(containerSupport.state); 
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,  Container.class, 
						          DBQuery.is("code", containerSupport.code),
						          DBUpdate.set("state",            containerSupport.state)
						                  .set("traceInformation", containerSupport.traceInformation));
				
				applySuccessPostStateRules(currentCtxValidation, containerSupport, context);
				nextState(currentCtxValidation, containerSupport);
			} else {
				applyErrorPostStateRules(currentCtxValidation, containerSupport, nextState);
			}
		}
		if (currentCtxValidation.hasErrors()) {
			contextValidation.addErrors(currentCtxValidation.getErrors());
		}
	}

	public void setStateFromContainers(ContextValidation contextValidation, ContainerSupport containerSupport, String context) {
		State nextStep = getNextStateFromContainerStates(contextValidation.getUser(), ContainerSupportValidationHelper.getContainerStates(containerSupport));
		setState(contextValidation, containerSupport, nextStep, context);
	}
	
	public State getNextStateFromContainerStates(String username, Set<String> containerStates) {
		State nextStep = null;
		logger.debug("States = {}", containerStates);
		if (containerStates.contains(N)) {
			nextStep = new State(N, username);			
		} else if (containerStates.contains(IW_D)) {
			nextStep = new State(IW_D, username);			
		} else if (containerStates.contains(IU)) {
			nextStep = new State(IU, username);			
		} else if (containerStates.contains(IW_E)) {
			nextStep = new State(IW_E, username);			
		} else if (containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_PF) && !containerStates.contains(A_TF)) {
			nextStep = new State(A_TM, username);			
		} else if (containerStates.contains(A_QC) && !containerStates.contains(A_TM) && !containerStates.contains(A_PF) && !containerStates.contains(A_TF)) {
			nextStep = new State(A_QC, username);			
		} else if (containerStates.contains(A_PF) && !containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_TF)) {
			nextStep = new State(A_PF, username);			
		} else if (containerStates.contains(A_TF) && !containerStates.contains(A_TM) && !containerStates.contains(A_QC) && !containerStates.contains(A_PF)) {
			nextStep = new State(A_TF, username);			
		} else if (containerStates.contains(A_TF) || containerStates.contains(A_TM) || containerStates.contains(A_QC) || containerStates.contains(A_PF)) {
			nextStep = new State(A, username);			
		} else if (containerStates.contains(IW_P)) {
			nextStep = new State(IW_P, username);			
		} else if (containerStates.contains(IS)) {
			nextStep = new State(IS, username);			
		} else if (containerStates.contains(UA)) {
			nextStep = new State(UA, username);			
		} else {
			throw new RuntimeException("setStateFromContainer : states " + containerStates + " not managed");
		}
		logger.debug("nextStep = {}", nextStep.code);
		return nextStep;
	}

	/**
	 * No operation.
	 * @param contextValidation validation context
	 * @param containerSupport  container support
	 */
	private void nextState(ContextValidation contextValidation, ContainerSupport containerSupport) {
	}

}
