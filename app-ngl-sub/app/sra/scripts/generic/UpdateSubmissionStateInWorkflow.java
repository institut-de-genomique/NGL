package sra.scripts.generic;

import javax.inject.Inject;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Submission;
import services.Tools;
import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;
//import workflows.sra.submission.SubmissionWorkflowsHelper;

/**
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * {@literal http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionStateInWorkflow?codes=GSC_BBA_42BJ137CW&state=SUB-SRD-IP}
 * 
 * @author sgas
 *
 */
public class UpdateSubmissionStateInWorkflow extends Script<UpdateSubmissionStateInWorkflow.MyParam>{
	

	private final SubmissionWorkflows submissionWorkflows;
	private final SubmissionAPI submissionAPI;


	@Inject
	public UpdateSubmissionStateInWorkflow (SubmissionAPI submissionAPI, 
								  SubmissionWorkflows submissionWorkflows ){

		this.submissionWorkflows = submissionWorkflows;
		this.submissionAPI       = submissionAPI;

	}
	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String state;
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
		println(getClass().toString());
		// si http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionStateInWorkflow?state=IW-SUB&code=toto&code=titi
	
	
		for (String submissionCode_ : args.codes) {
			String submissionCode = Tools.clean(submissionCode_);
			printfln(" submissionCode : %s", submissionCode);
	
//			Submission submission = MongoDBDAO
//					.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
//							Submission.class, DBQuery.is("code", submissionCode));
			Submission submission = submissionAPI.dao_getObject(submissionCode);
			if (submission == null) {
				printfln("La soumission %s n'existe pas dans la base", submissionCode);
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
			printfln("Soumission '%s' mise dans l'etat '%s' ", submission.code, args.state);
		}
	}



}
