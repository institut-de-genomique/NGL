package sra.scripts;

import javax.inject.Inject;
import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Submission;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;
import workflows.sra.submission.SubmissionWorkflowsHelper;

public class UpdateSubmissionStateInWorkflow extends Script<UpdateSubmissionStateInWorkflow.MyParam>{
	

	private final SubmissionWorkflows submissionWorkflows;

	@Inject
	public UpdateSubmissionStateInWorkflow (SubmissionAPI submissionAPI, 
								  SubmissionWorkflowsHelper submissionWorkflowsHelper,
								  SubmissionWorkflows submissionWorkflows ){

		this.submissionWorkflows= submissionWorkflows;

	}
	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String state;
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
		//String submissionCode = "GSC_APX_BXT_38AF1N87U";
		//String submissionCode = "GSC_APX_BXT_38AG1BOO0";
		
		// si http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionStateInWorkflow?state=IW-SUB&code=toto&code=titi
	
	
		for (String submissionCode : args.codes) {
			printfln(" submissionCode : %s", submissionCode);
	
			Submission submission = MongoDBDAO
					.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							Submission.class, DBQuery.is("code", submissionCode));
			if (submission == null) {
				printfln(" %s n'existe pas dans la base", submissionCode);
				return;
			}
		
			// installation du state en passant par le workflow car transition autorisée sinon declenche erreur
			// FE-SUB vers N non autorisee :
			final String stateValue	 = args.state;             //"IW-SUB";
			String user = submission.state.user;
//			ContextValidation contextValidation = new ContextValidation(user);
//			contextValidation.setUpdateMode();
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
			State state = new State(stateValue, user);
			submissionWorkflows.setState(contextValidation, submission, state);
		}

	}


}
