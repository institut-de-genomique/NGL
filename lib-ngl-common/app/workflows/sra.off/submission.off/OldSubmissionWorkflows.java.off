package workflows.sra.submission;
//package workflows.sra.submission;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//// import org.springframework.beans.factory.annotation.Autowired;
//// import org.springframework.stereotype.Service;
//
//import fr.cea.ig.MongoDBDAO;
//import models.laboratory.common.description.ObjectType;
//import models.laboratory.common.instance.State;
//import models.sra.submit.common.instance.Submission;
//// import models.sra.submit.util.SraException;
//import models.utils.InstanceConstants;
////import play.Logger;
//import validation.ContextValidation;
//import validation.common.instance.CommonValidationHelper;
//import workflows.Workflows;
//
//// @Service
//@Singleton
//public class SubmissionWorkflows extends Workflows<Submission> {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionWorkflows.class);
//
//	/*@Autowired
//	SubmissionWorkflowsHelper submissionWorkflowsHelper;*/
//	
//	private final SubmissionWorkflowsHelper submissionWorkflowsHelper;
//	
//	@Inject
//	public SubmissionWorkflows(SubmissionWorkflowsHelper submissionWorkflowsHelper) {
//		this.submissionWorkflowsHelper = submissionWorkflowsHelper;
//	}
//	
//	@Override
//	public void applyPreStateRules(ContextValidation validation,
//			Submission submission, State nextState) {
//		if("IP-SUB-R".equals(submission.state.code) && "F-SUB".equals(nextState.code)){
//			logger.debug("call update submission Release");
//			submissionWorkflowsHelper.updateSubmissionRelease(submission);
//		}
//		
//		if("IW-SUB-R".equals(nextState.code)){
//			submissionWorkflowsHelper.createDirSubmission(submission, validation);
//		}
//
//		if("IW-SUB".equals(nextState.code)){
//			submissionWorkflowsHelper.activationPrimarySubmission(validation, submission);
//		}
//		if("IP-SUB".equals(submission.state.code) && "F-SUB".equals(nextState.code)){
//			logger.debug("call update submission Release");
//			submissionWorkflowsHelper.updateSubmissionForDates(submission);
//		}
//		logger.debug("dans apply pre state rules avec nextState = '" + nextState.code + "'");
//		updateTraceInformation(submission.traceInformation, nextState); 
//
//	}
//
//	@Override
//	public void applyPreValidateCurrentStateRules(ContextValidation validation, Submission object) {
//		// TODO Auto-generated method stub		
//	}
//
//	@Override
//	public void applyPostValidateCurrentStateRules(ContextValidation validation, Submission object) {
//		// TODO Auto-generated method stub		
//	}
//
//	@Override
//	public void applySuccessPostStateRules(ContextValidation validation, Submission submission) {
//		if (! submission.state.code.equalsIgnoreCase("N") && ! submission.state.code.equalsIgnoreCase("N-R") && !submission.state.code.equalsIgnoreCase("IW-SUB-R")){
//			submissionWorkflowsHelper.updateSubmissionChildObject(submission, validation);
//		}
//	}
//
//	@Override
//	public void applyErrorPostStateRules(ContextValidation validation,
//			Submission submission, State nextState) {
//		if("IW-SUB-R".equals(submission.code)){
//			submissionWorkflowsHelper.rollbackSubmission(submission, validation);
//		}
//		if(validation.hasErrors()){
//			logger.error("Problem on SubmissionWorkflow.applyErrorPostStateRules : "+validation.errors.toString());
//		}
//	}
//
//	@Override
//	public void setState(ContextValidation contextValidation, Submission submission, State nextState) {
//		logger.debug("dans setState avec submission" + submission.code +" et et submission.state="+submission.state.code+ " et nextState="+nextState.code);
//
//		contextValidation.setUpdateMode();
//		// verifier que le state à installer est valide avant de mettre à jour base de données : 
//		// verification qui ne passe pas par VariableSRA [SraValidationHelper.requiredAndConstraint(contextValidation, nextState.code , VariableSRA.mapStatus, "state.code")]		
//		// mais par CommonValidationHelper.validateState(ObjectType.CODE.SRASubmission, nextState, contextValidation); 
//		// pour uniformiser avec reste du code ngl
//		logger.debug("dans setState");
//		logger.debug("contextValidation.error avant validateState " + contextValidation.errors);
//
//		CommonValidationHelper.validateState(ObjectType.CODE.SRASubmission, nextState, contextValidation); 	
//		logger.debug("contextValidation.error apres validateState " + contextValidation.errors);
//
//		if (contextValidation.hasErrors()) { 
//			logger.error("ATTENTION ERROR :"+contextValidation.errors);
//		} else if (nextState.code.equals(submission.state.code)) {
//			logger.error("ATTENTION ERROR :submissionStateCode == {} et nextStateCode == {}", 
//						 submission.state.code, nextState.code);
//		} else {
//			applyPreStateRules(contextValidation, submission, nextState);
//			//submission.validate(contextValidation);
//			if (!contextValidation.hasErrors()) {
//				// Gerer l'historique des states :
//				submission.state = updateHistoricalNextState(submission.state, nextState);	
//				// sauver le state dans la base avec traceInformation
//				MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME,  Submission.class, 
//						DBQuery.is("code", submission.code),
//						DBUpdate.set("state", submission.state).set("traceInformation", submission.traceInformation));
//				applySuccessPostStateRules(contextValidation, submission);
//				nextState(contextValidation, submission);		
//			} else {
//				applyErrorPostStateRules(contextValidation, submission, nextState);	
//			}
//		}
//	}
//
//	@Override
//	public void nextState(ContextValidation contextValidation, Submission object) {
//		// TODO Auto-generated method stub
//
//	}
//
//
//}
