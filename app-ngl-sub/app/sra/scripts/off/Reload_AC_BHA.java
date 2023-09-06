package sra.scripts.off;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import mail.MailServiceException;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import validation.ContextValidation;


public class Reload_AC_BHA extends ScriptNoArgs {
	
	private final SubmissionNewAPI   submissionNewAPI;
	@Inject
	public Reload_AC_BHA(SubmissionNewAPI   submissionNewAPI) {
		super();
//		this.fileAcServices = fileAcServices;
//		this.submissionServices = submissionServices;
		this.submissionNewAPI   = submissionNewAPI;
	}
	
	@Override
	public void execute() throws IOException, SraException, MailServiceException {
		reloadAC_BHA();
	}
	
	public void debug_data_BHA (Submission submission) {
		String stateCode = "IP-SUB";
		
		printfln("***************submission avec code=%s", submission.code);

		submission.state.code = stateCode;
		printfln("***************sauvegarde de la soumission %s", submission.code);
		// sauver la soumission avec le status IP-SUB:
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);

		String sampleCodeForExp = null;
		for (String sampleCode :  submission.sampleCodes) {	
//			Sample sample = 
					MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampleCode);
			sampleCodeForExp = sampleCode;
		}
		
		for (String experimentCode :  submission.experimentCodes) {	
			Experiment experiment = MongoDBDAO
				.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, 
					models.sra.submit.sra.instance.Experiment.class, experimentCode);
			// correction specifique a la soumission (corriger reference au sample)

			experiment.sampleCode = sampleCodeForExp; 
			printfln("***************sauvegarde de l'experiment %s avec correction pour le sampleCode ", experiment.code);
			MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);			
		}
	}
	
	public void reloadAC_BHA() throws IOException, SraException, MailServiceException {
		List<String> submissionCodes = new ArrayList<>();
		submissionCodes.add("GSC_BHA_32FE4ABOO");
		
		for (String submissionCode: submissionCodes) {
			Submission submission = MongoDBDAO
					.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							models.sra.submit.sra.instance.Submission.class, submissionCode);
			debug_data_BHA(submission);
			
//			File fileEbi = new File("/env/cns/home/sgas/debug_BHA",  "listAC_" + submission.code + ".txt");
			String user = "ngsrg";
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
			this.submissionNewAPI.loadEbiResponseAC(ctxVal, submission);
		}	
	}
	
}

