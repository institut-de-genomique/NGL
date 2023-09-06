package workflows.analyses;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;
import rules.services.IDrools6Actor;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

@Singleton
public class AnalysisWorkflows extends Workflows<Analysis> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(AnalysisWorkflows.class);
	
	private static final String ruleFBA ="F_BA_1";

	private final AnalysisWorkflowsHelper analysisWorkflowsHelper;
//	private final LazyRules6Actor         rulesActor;
//	
//	@Inject
//	public AnalysisWorkflows(NGLApplication ctx, AnalysisWorkflowsHelper analysisWorkflowsHelper) {
//		this.analysisWorkflowsHelper = analysisWorkflowsHelper;
//		rulesActor                   = ctx.rules6Actor();
//	}

	private final IDrools6Actor rulesActor;
	
	@Inject
	public AnalysisWorkflows(IDrools6Actor rulesActor, AnalysisWorkflowsHelper analysisWorkflowsHelper) {
		this.analysisWorkflowsHelper = analysisWorkflowsHelper;
		this.rulesActor              = rulesActor;
	}	
	
	@Override
	public void applyPreStateRules(ContextValidation validation, Analysis exp, State nextState) { 
	}

	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Analysis object) {
	}

	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Analysis object) {
	}

	@Override
	public void applySuccessPostStateRules(ContextValidation validation, Analysis analysis) {
		// AnalysisWorkflowsHelper analysisWorkflowsHelper = wc.analysisWorkflowsHelper();
		if ("IP-BA".equals(analysis.state.code)) {
			analysisWorkflowsHelper.updateStateMasterReadSetCodes(analysis, validation, "IP-BA");
		} else if ("F-BA".equals(analysis.state.code)) {
			rulesActor.tellMessage(ruleFBA, analysis);
			analysisWorkflowsHelper.updateStateMasterReadSetCodes(analysis, validation, "F-BA",true);
		} else if ("IW-V".equals(analysis.state.code)) {
			analysisWorkflowsHelper.updateBioinformaticValuationMasterReadSetCodes(analysis, validation, TBoolean.UNSET, null, null);	
			analysisWorkflowsHelper.updateStateMasterReadSetCodes(analysis, validation, "IW-VBA", false);
		} else if ("F-V".equals(analysis.state.code)) {
			analysisWorkflowsHelper.updateBioinformaticValuationMasterReadSetCodes(analysis, validation,  TBoolean.TRUE, validation.getUser(), new Date(),true);	
			analysisWorkflowsHelper.updateStateMasterReadSetCodes(analysis, validation, "F-VBA",false);			
		}
	}

	@Override
	public void applyErrorPostStateRules(ContextValidation validation, Analysis exp, State nextState) {
	}

	@Override
	public void setState(ContextValidation contextValidation, Analysis analysis, State nextState) {
		contextValidation.setUpdateMode();
		CommonValidationHelper.validateStateRequired(contextValidation, analysis.typeCode, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(analysis.state.code)) {
//			boolean goBack = goBack(analysis.state, nextState);
			boolean backward = models.laboratory.common.description.State.find.get().isBackward(analysis.state.code, nextState.code);
			if (backward)
				logger.debug(analysis.code + " : back to the workflow. " + analysis.state.code + " -> " + nextState.code);		

//			analysis.traceInformation = updateTraceInformation(analysis.traceInformation, nextState); 
			analysis.traceInformation.forceModificationStamp(nextState.user, nextState.date); 
//			analysis.state = updateHistoricalNextState(analysis.state, nextState);
			analysis.state = nextState.createHistory(analysis.state);

			MongoDBDAO.update(InstanceConstants.ANALYSIS_COLL_NAME,  Analysis.class, 
					          DBQuery.is("code", analysis.code),
					          DBUpdate.set("state",            analysis.state)
					                  .set("traceInformation", analysis.traceInformation));

			applySuccessPostStateRules(contextValidation, analysis);
			nextState(contextValidation, analysis);
		}		
	}

	@Override
	public void nextState(ContextValidation contextValidation, Analysis analysis) {
//		State nextStep = cloneState(analysis.state, contextValidation.getUser());
		State nextStep = new State(analysis.state.code, contextValidation.getUser());
		if ("F-BA".equals(analysis.state.code)) {
			nextStep.code = "IW-V";
		} else if ("IW-V".equals(analysis.state.code) && analysis.valuation.isnt(TBoolean.UNSET)) {
			nextStep.code = "F-V";
		} else if ("F-V".equals(analysis.state.code) && analysis.valuation.is(TBoolean.UNSET)) {
			nextStep.code = "IW-V";
		}
		setState(contextValidation, analysis, nextStep);
	}
	
//	@Override
//	public void nextState(ContextValidation contextValidation, Analysis analysis) {
//		State nextStep = cloneState(analysis.state, contextValidation.getUser());
//		switch (analysis.state.code) {
//		case F_BA :                                                       nextStep.code = IW_V; break;
//		case IW_V : if (!TBoolean.UNSET.equals(analysis.valuation.valid)) nextStep.code = F_V;  break;
//		case F_V  : if ( TBoolean.UNSET.equals(analysis.valuation.valid)) nextStep.code = IW_V; break;
//		}
//		setState(contextValidation, analysis, nextStep);
//	}

}
