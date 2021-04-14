package sra.scripts;

import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
//import fr.cea.ig.lfw.controllers.scripts.ScriptNoArgs;
//import mail.MailServiceException;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.util.SraException;
//import models.utils.InstanceConstants;
import services.EbiFileResponseServices;
//import sra.scripts.UpdateSubmissionStateInWorkflow.MyParam;
import validation.ContextValidation;

/*
 * 
 * Deprecated : Utiliser plutot 
 * ngl-sub.genoscope.cns.fr:/sra/scripts/run/sra.scripts.UpdateSubmissionStateInWorkflow?codes=monCode&state=SUB-SMD-F
 * Ce qui va declencher le rechargement du rapport de soumission mais aussi le rapatriement du project si study soumis
 * et sans changer le repertoire du rapport de soumission si on travaille sur sa VM
 * 
 * 
 * Script à utiliser pour recharger le fichier des AC recus en retour d'une soumission.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ReloadEbiSubmissionReport?submissionCode=codeSoumission&fileAC=pathFichierAc}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class ReloadEbiSubmissionReport extends Script<ReloadEbiSubmissionReport.MyParam>{ 
	
	
	private EbiFileResponseServices ebiFileResponseServices;
//	private SubmissionServices submissionServices;
//	private ContextValidation contextValidation; 
	private SubmissionAPI  submissionAPI;

	@Inject
	public ReloadEbiSubmissionReport(EbiFileResponseServices ebiFileResponseServices,
					 SubmissionAPI           submissionAPI) {
		super();
		this.ebiFileResponseServices = ebiFileResponseServices;
		this.submissionAPI           = submissionAPI;
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
		String user = "ngsrg";
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
//		submission = this.fileAcServices.traitementFileAC(ctxVal, args.submissionCode, fileEbi); 
		Submission submission = submissionAPI.get(args.submissionCode);
		this.ebiFileResponseServices.loadEbiResp(ctxVal, submission, fileEbi); 
		
	}	

		
}

