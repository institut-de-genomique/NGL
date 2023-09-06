package sra.scripts.off;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import mail.MailServiceException;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import sra.api.submission.SubmissionNewAPI;
import validation.ContextValidation;

public class Reload_AC_BRI extends ScriptNoArgs {
	
	private final SubmissionNewAPI   submissionNewAPI;
	
	@Inject
	public Reload_AC_BRI(SubmissionNewAPI   submissionNewAPI) {
		super();
//		this.fileAcServices = fileAcServices;
//		this.submissionServices = submissionServices;
		this.submissionNewAPI   = submissionNewAPI;
	}
	
	@Override
	public void execute() throws IOException, SraException, MailServiceException {
		reloadAC_BAS();
	}
	
	public void reloadAC_BAS() throws IOException, SraException, MailServiceException {
		List<String> submissionCodes = new ArrayList<>();
		submissionCodes.add("GSC_BRI_34OB1147O");
		
		for (String submissionCode: submissionCodes) {
			Submission submission = MongoDBDAO
					.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
							models.sra.submit.sra.instance.Submission.class, submissionCode);
			
//			File fileEbi = new File("/env/cns/home/sgas/debug_BRI",  "listAC_" + submission.code + ".txt");
			String user = "ngsrg";
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
			this.submissionNewAPI.loadEbiResponseAC(ctxVal, submission);
		}	
	}
		
}

