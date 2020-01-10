//package sra.scripts;
//import javax.inject.Inject;
//
//import org.mongojack.DBQuery;
//
//import fr.cea.ig.MongoDBDAO;
//import fr.cea.ig.lfw.controllers.AbstractScript;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import models.laboratory.common.instance.State;
//import models.sra.submit.common.instance.Submission;
//import models.utils.InstanceConstants;
//import validation.ContextValidation;
//import workflows.sra.submission.SubmissionWorkflowsHelper;
//
//public class updateSubmissionState extends AbstractScript {
//	
//	private final SubmissionAPI submissionAPI;
//	private final SubmissionWorkflowsHelper submissionWorkflowsHelper;
//	
//	@Inject
//	public updateSubmissionState (SubmissionAPI submissionAPI, 
//								  SubmissionWorkflowsHelper submissionWorkflowsHelper) {
//		this.submissionAPI = submissionAPI;
//		this.submissionWorkflowsHelper = submissionWorkflowsHelper;
//		
//	}
//	
//	
//	@Override
//	public void execute() throws Exception {
//		String submissionCode = "GSC_BRI_34OB1147O";
//		
//		Submission submission = MongoDBDAO
//				.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
//						Submission.class, DBQuery.is("code", submissionCode));
//		if (submission == null) {
//			printfln(submissionCode + " n'existe pas dans la base");
//			return;
//		}
//		
//		
//		// installation du state avec etat N sans passer par le workflow car transition 
//		// FE-SUB vers N non autorisee :
//		final String N	 = "N";
//		String user = submission.state.user;
//		
//		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
//		State nextState = new State(N, user);
//		submission.state = nextState;
//		submissionAPI.dao_saveObject(submission);
//		submissionWorkflowsHelper.updateSubmissionChildObject(submission, ctxVal);
//
//	}
//
//}
