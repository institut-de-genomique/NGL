package sra.scripts;

import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

import javax.inject.Inject;

//import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.Script;
//import fr.cea.ig.lfw.controllers.scripts.ScriptNoArgs;
//import mail.MailServiceException;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.util.SraException;
//import models.utils.InstanceConstants;
import services.FileAcServices;
import services.SubmissionServices;
//import sra.scripts.UpdateSubmissionStateInWorkflow.MyParam;
import validation.ContextValidation;

/*
 * Script à utiliser pour recharger le fichier des AC recus en retrour d'une soumission.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ReloadAC?submissionCode=codeSoumission&fileAC=pathFichierAc}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Reload_AC extends Script<Reload_AC.MyParam>{ 
	
	
	private FileAcServices fileAcServices;
//	private SubmissionServices submissionServices;
//	private ContextValidation contextValidation; 
	
	@Inject
	public Reload_AC(FileAcServices fileAcServices, SubmissionServices submissionServices) {
		super();
		this.fileAcServices = fileAcServices;
	}
	
	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
		public String fileAC;
		
	}
	

	@Override
	public void execute(MyParam args) throws Exception {
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, args.submissionCode);

		//			File fileEbi = new File("/env/cns/home/sgas/debug/",  "listAC_" + submission.code + ".txt");
		File fileEbi = new File(args.fileAC);
		String user = "william";
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
//		submission = this.fileAcServices.traitementFileAC(ctxVal, args.submissionCode, fileEbi); 
		this.fileAcServices.traitementFileAC(ctxVal, args.submissionCode, fileEbi); 
		
	}	

		
}

