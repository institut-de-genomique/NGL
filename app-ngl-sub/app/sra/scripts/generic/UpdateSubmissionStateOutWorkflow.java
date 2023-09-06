package sra.scripts.generic;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import sra.api.submission.SubmissionNewAPITools;
// Exemple de lancement :
//	 code_soumission_1&codes=code_soumission_2
//	  @author sgas
	 
	 
public class UpdateSubmissionStateOutWorkflow extends Script<UpdateSubmissionStateOutWorkflow.MyParam>{
	

	private final SubmissionAPI         submissionAPI;
	private final SubmissionNewAPITools submissionNewAPITools;

	
	@Inject
	public UpdateSubmissionStateOutWorkflow (SubmissionAPI         submissionAPI, 
											 SubmissionNewAPI      submissionNewAPI,
											 SubmissionNewAPITools submissionNewAPITools) {
		this.submissionAPI         = submissionAPI;
		this.submissionNewAPITools = submissionNewAPITools;

	}
		
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		//public String state;
		//public int code;
		public String state;
		public String[] codes;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
		//String submissionCode = "GSC_APX_BXT_38AF1N87U";
		//String submissionCode = "GSC_APX_BXT_38AG1BOO0";
		
		// si http://localhost:9000/sra/scripts/run/sra.scripts.UpdateSubmissionStateOutWorkflow?state=IW-SUB&codes=toto&codes=titi
	
	
		for (String submissionCode : args.codes) {
			printfln(" submissionCode : '%s'", submissionCode);
	
			Submission submission = MongoDBDAO
					.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							Submission.class, DBQuery.is("code", submissionCode));
			if (submission == null) {
				printfln("'%s' n'existe pas dans la base", submissionCode);
				return;
			}
		
			// installation du state avec etat N sans passer par le workflow car transition 
			// FE-SUB vers N non autorisee :
			String user = submission.state.user;
			
			State nextState = new State(args.state, user);
			submission.state.code = nextState.code;
			submissionAPI.dao_saveObject(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);			
			printfln("Soumission '%s' mise dans l'etat '%s' ", submission.code, args.state);

		}

	}


}
