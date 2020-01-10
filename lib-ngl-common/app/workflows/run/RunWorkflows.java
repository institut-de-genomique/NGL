package workflows.run;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

@Singleton
public class RunWorkflows extends Workflows<Run> {

	private static final play.Logger.ALogger logger = play.Logger.of(RunWorkflows.class);
	
	private static final String ruleFRG = "F_RG_1";
	private static final String ruleIPS = "IP_S_1";
	private static final String ruleFS = "F_S_1";
	private static final String ruleFV  = "F_V_1";
	
//	private final LazyRules6Actor    rulesActor;
	private final IDrools6Actor      rulesActor;
	private final RunWorkflowsHelper runWorkflowsHelper;
	
	@Inject
	public RunWorkflows(NGLApplication app, RunWorkflowsHelper runWorkflowsHelper) {
		rulesActor = app.rules6Actor();
		this.runWorkflowsHelper = runWorkflowsHelper;
	}

	@Override
	public void applyPreStateRules(ContextValidation validation, Run exp, State nextState) {
	}
	
	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Run object) {
	}
	
	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Run object) {
	}
	
//	@Override
//	public void applySuccessPostStateRules(ContextValidation validation, Run run) {
//		if ("IP-S".equals(run.state.code)) {
//			rulesActor.tellMessage(ruleIPS, run);
//		} else if("F-RG".equals(run.state.code)) {
//			runWorkflowsHelper.updateDispatchRun(run);
//			runWorkflowsHelper.updateReadSetLane(run, validation, ruleFRG, true);
//			rulesActor.tellMessage(ruleFRG, run);
//		} else if("F-V".equals(run.state.code)) {
//			rulesActor.tellMessage(ruleFV, run);
//			runWorkflowsHelper.invalidateReadSetLane(run, validation, ruleFV, false);
//		}	
//	}
	
	@Override
	public void applySuccessPostStateRules(ContextValidation validation, Run run) {
		if (run.state.code == null)
			return;
		switch (run.state.code) {
		case "IP-S" :
			rulesActor.tellMessage(ruleIPS, run);
			break;
		case "F-RG" :
			runWorkflowsHelper.updateDispatchRun(run);
			runWorkflowsHelper.updateReadSetLane(run, validation, ruleFRG, true);
			rulesActor.tellMessage(ruleFRG, run);
			break;
		case "F-V" :
			rulesActor.tellMessage(ruleFV, run);
			runWorkflowsHelper.invalidateReadSetLane(run, validation, ruleFV, false);
			break;
		case "F-S" : 
			ArrayList<Object> facts = new ArrayList<>();
			facts.add(run);
			facts.add(validation);
			//Get container
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
			facts.addAll(containers);
			rulesActor.tellMessage(ruleFS, facts);
			break;
		default:
			break;
		}	
	}
	
	@Override
	public void applyErrorPostStateRules(ContextValidation validation, Run exp, State nextState) {
	}
	
	@Override
	public void setState(ContextValidation contextValidation, Run run, State nextState) {
		contextValidation.setUpdateMode();
		CommonValidationHelper.validateStateRequired(contextValidation, run.typeCode, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(run.state.code)) {
//			boolean goBack = goBack(run.state, nextState);
			boolean backward = models.laboratory.common.description.State.find.get().isBackward(run.state.code, nextState.code);
			if (backward) logger.debug(run.code+" : back to the workflow. "+run.state.code +" -> "+nextState.code);		

//			run.traceInformation = updateTraceInformation(run.traceInformation, nextState); 
			run.traceInformation.forceModificationStamp(nextState.user, nextState.date); 
//			run.state = updateHistoricalNextState(run.state, nextState);
			run.state = nextState.createHistory(run.state);

			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME,  Run.class, 
					DBQuery.is("code", run.code),
					DBUpdate.set("state", run.state).set("traceInformation",run.traceInformation));

			applySuccessPostStateRules(contextValidation, run);
			nextState(contextValidation, run);
		}	
	}
	
	@Override
	public void nextState(ContextValidation contextValidation, Run run) {
//		State nextStep = cloneState(run.state, contextValidation.getUser());
		State nextStep = new State(run.state.code, contextValidation.getUser());
		if ("F-RG".equals(run.state.code)) {
			nextStep.code = "IW-V";
		} else if ("F-S".equals(run.state.code)) {
			nextStep.code = "IW-RG";
//		} else if ("IW-V".equals(run.state.code) && runWorkflowsHelper.atLeastOneValuation(run)) {
		} else if ("IW-V".equals(run.state.code) && run.atLeastOneValuation()) {
			nextStep.code = "IP-V";
//		} else if ("IP-V".equals(run.state.code) && runWorkflowsHelper.isRunValuationComplete(run)) {
		} else if ("IP-V".equals(run.state.code) && run.isValuationComplete()) {
			nextStep.code = "F-V";
//		} else if ("F-V".equals(run.state.code) && !runWorkflowsHelper.isRunValuationComplete(run)) {
		} else if ("F-V".equals(run.state.code) && !run.isValuationComplete()) {
			nextStep.code = "IP-V";
		}
		setState(contextValidation, run, nextStep);
	}
	
}
