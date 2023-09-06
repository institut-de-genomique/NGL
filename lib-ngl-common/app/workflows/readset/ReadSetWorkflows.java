package workflows.readset;

import static models.laboratory.common.instance.TBoolean.*;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.State;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.ReadSetValidationHelper;
import workflows.Workflows;

@Singleton
public class ReadSetWorkflows extends Workflows<ReadSet> {

	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetWorkflows.class);
	
	private static final String ruleFQC  = "F_QC_1";
	private static final String ruleFRG  = "F_RG_1";
//	private static final String ruleIPS  = "IP_S_1";
//	private static final String ruleFV   = "F_V_1";
	private static final String ruleFVQC = "F_VQC_1";
	private static final String ruleIWBA = "IW_BA_1";
//	private static final String ruleFBA  = "F_BA_1";
	private static final String ruleAUA  = "A-UA_1";
	private static final String ruleA    = "A_1";
	private static final String ruleFTF  = "F_TF_1";
	private static final String ruleIWTF  = "IW_TF_1";
	private static final String ruleN    = "N_1";
	
	private final ReadSetWorkflowsHelper readSetWorkflowsHelper;
//	private final LazyRules6Actor        rulesActor;
	private final IDrools6Actor          rulesActor;
//	private final NGLContext ctx;
	private final NGLApplication         app;
	
//	@Inject
//	public ReadSetWorkflows(NGLContext ctx, ReadSetWorkflowsHelper readSetWorkflowsHelper) {
//		this.ctx                    = ctx;
//		this.readSetWorkflowsHelper = readSetWorkflowsHelper;
//		rulesActor                  = ctx.rules6Actor();
//	}
	
	@Inject
	public ReadSetWorkflows(NGLApplication app, ReadSetWorkflowsHelper readSetWorkflowsHelper) {
		this.app                    = app;
		this.readSetWorkflowsHelper = readSetWorkflowsHelper;
		rulesActor                  = app.rules6Actor();
	}

	@Override
	public void applyPreStateRules(ContextValidation validation, ReadSet readSet, State nextState) {
		if ("N".equals(readSet.state.code)) {
			ArrayList<Object> facts = new ArrayList<>();
			facts.add(readSet);
			facts.add(validation);
			app.callRulesWithGettingFacts(ruleN, facts);
		}
	}

	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, ReadSet object) {
	}

	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, ReadSet object) {
	}

	@Override
	public void applySuccessPostStateRules(ContextValidation validation, ReadSet readSet) {
		if ("IP-RG".equals(readSet.state.code)) {
			readSetWorkflowsHelper.updateContainer(readSet);		
		} else if("F-RG".equals(readSet.state.code)) {
			readSetWorkflowsHelper.updateDispatch(readSet);
			rulesActor.tellMessage(ruleFRG, readSet);
		} else if("F-QC".equals(readSet.state.code)) {			
			rulesActor.tellMessage(ruleFQC, readSet);
		} else if("F-VQC".equals(readSet.state.code)) {
			if (readSet.bioinformaticValuation.is(UNSET)) {
				if (readSetWorkflowsHelper.isHasBA(readSet) && readSet.productionValuation.is(FALSE)){
					readSetWorkflowsHelper.updateBioinformaticValuation(readSet);
				}else if(!readSetWorkflowsHelper.isHasBA(readSet)){
					readSetWorkflowsHelper.updateBioinformaticValuation(readSet);
				}
			}
			rulesActor.tellMessage(ruleFVQC, readSet);
		} else if("A".equals(readSet.state.code) || "UA".equals(readSet.state.code)) {
			readSetWorkflowsHelper.updateFiles(readSet, validation);
			//if change valuation when final step
			rulesActor.tellMessage(ruleAUA, readSet);
		}
	}

	@Override
	public void applyErrorPostStateRules(ContextValidation validation, ReadSet exp, State nextState) {
	}

	@Override
	public void setState(ContextValidation contextValidation, ReadSet readSet, State nextState) {
		contextValidation.setUpdateMode();
		CommonValidationHelper.validateStateRequired(contextValidation, readSet.typeCode, nextState);
		ReadSetValidationHelper.validateNextState(contextValidation, readSet, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(readSet.state.code)) {
//			boolean goBack = goBack(readSet.state, nextState);
			boolean backward = models.laboratory.common.description.State.find.get().isBackward(readSet.state.code, nextState.code);
			if (backward) 
				// Logger.debug(readSet.code + " : back to the workflow. "+readSet.state.code +" -> "+nextState.code);
				logger.debug("{} : back to the workflow. {} -> {}", readSet.code, readSet.state.code, nextState.code);
//			readSet.traceInformation = updateTraceInformation(readSet.traceInformation, nextState); 
			readSet.traceInformation.forceModificationStamp(nextState.user, nextState.date); 
//			readSet.state = updateHistoricalNextState(readSet.state, nextState);
			readSet.state = nextState.createHistory(readSet.state);
			
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  Run.class, 
					DBQuery.is("code", readSet.code),
					DBUpdate.set("state", readSet.state).set("traceInformation",readSet.traceInformation));
			applySuccessPostStateRules(contextValidation, readSet);
			nextState(contextValidation, readSet);
		}	
	}

	@Override
	public void nextState(ContextValidation contextValidation, ReadSet readSet) {
//		State nextStep = cloneState(readSet.state, contextValidation.getUser());
		State nextStep = new State(readSet.state.code, contextValidation.getUser());
		if ("F-RG".equals(readSet.state.code)) {
			nextStep.code = "IW-QC";
		} else if("F-QC".equals(readSet.state.code)) {
			nextStep.code = "IW-VQC";
		} else if("IW-VQC".equals(readSet.state.code)) {
			if (readSet.productionValuation.isnt(UNSET)) {
				nextStep.code = "F-VQC";
			}
		} else if("IP-VQC".equals(readSet.state.code)) {
			if (readSet.productionValuation.isnt(UNSET)) {
				nextStep.code = "F-VQC";
			}		
		} else if("F-VQC".equals(readSet.state.code)) {
			if (readSetWorkflowsHelper.isHasBA(readSet) && readSet.productionValuation.is(TRUE) && readSet.bioinformaticValuation.is(UNSET)) {
				nextStep.code = "IW-BA";
			} else {
				if (readSet.bioinformaticValuation.is(TRUE)) {
					nextStep.code = "A";
				} else if(readSet.bioinformaticValuation.is(FALSE)) {
					nextStep.code = "UA";
				}
			}
		} else if("IW-BA".equals(readSet.state.code)) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
			ArrayList<Object> facts = new ArrayList<>();
			facts.add(nextStep);
			facts.add(project);
			facts.add(readSet);
			app.callRulesWithGettingFacts(ruleIWBA, facts);
		} else if("F-BA".equals(readSet.state.code)) {
			nextStep.code = "IW-VBA";
		} else if("IW-VBA".equals(readSet.state.code)) {
			if(readSet.bioinformaticValuation.isnt(UNSET)) {
				nextStep.code = "F-VBA";
			}		
		} else if("F-VBA".equals(readSet.state.code)) {
			if (readSet.bioinformaticValuation.is(TRUE)) {
				nextStep.code = "A";
			} else if(readSet.bioinformaticValuation.is(FALSE)) {
				nextStep.code = "UA";
			}					
		} else if("A".equals(readSet.state.code) || "UA".equals(readSet.state.code)) {			
			if (readSet.bioinformaticValuation.is(TRUE)) {
				nextStep.code = "A";
				//Call rules for Transfert CCRT
				//Get project to identify sent to CCRT
				Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
				ArrayList<Object> facts = new ArrayList<>();
				facts.add(nextStep);
				facts.add(project);
				facts.add(readSet);
				app.callRulesWithGettingFacts(ruleA, facts);
			} else { //FALSE or UNSET
				nextStep.code = "UA";
			}			
		} else if("F-TF".equals(readSet.state.code)) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
			ArrayList<Object> facts = new ArrayList<>();
			facts.add(nextStep);
			facts.add(project);
			app.callRulesWithGettingFacts(ruleFTF, facts);
		}else if("IW-TF".equals(readSet.state.code) && readSet.bioinformaticValuation.is(UNSET)) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
			ArrayList<Object> facts = new ArrayList<>();
			facts.add(nextStep);
			facts.add(project);
			facts.add(readSet);
			app.callRulesWithGettingFacts(ruleIWTF, facts);
			//nextStep.code = "UA";
		}
		setState(contextValidation, readSet, nextStep);
	}

}
