package sra.scripts;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.w3c.dom.Element;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import services.sra.SraException;
import services.sra.SraValidationException;
import sra.api.SubmissionNewAPI;
import sra.api.SubmissionNewAPITools;
import sra.services.DatabaseConsistencyTools;
import sra.services.EbiFileResponseServices;
import sra.services.EbiRetour;
import validation.ContextValidation;

//http://localhost:9000/sra/scripts/run/sra.scripts.ReloadEbiReport?submissionCode=GSC_CBU_A1GH3L6P5&pathEbiReport=C:\Users\sgas.IBFJ-EVRY\ebi_release_STUDY_931F3Z7OL.txt

public class ReloadEbiReport extends Script<ReloadEbiReport.MyParam>{
	

	private final SubmissionNewAPI          submissionNewAPI;
	private static final play.Logger.ALogger logger = play.Logger.of(ReloadEbiReport.class);

	
	@Inject
	public ReloadEbiReport (SubmissionNewAPI         submissionNewAPI) {
		this.submissionNewAPI  = submissionNewAPI;
	}
		
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String pathEbiReport;
		public String submissionCode;
	}
			
	@Override
	public void execute(MyParam args) throws Exception {
		String submissionCode = args.submissionCode;
		Submission submission = MongoDBDAO
				.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
						Submission.class, DBQuery.is("code", submissionCode));
		if (submission == null) {
			printfln("'%s' n'existe pas dans la base", submissionCode);
			return;
		}
		String user = "sgas";

		ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
		boolean sendMail = false;
		boolean confidential = true;
		
		if(submissionCode.contains("atlasea")) {
			confidential = false;
		}
		
		if(submission.type.equals(Submission.Type.CREATION)) {
			submission.state.code = "SUB-SMD-F";
			submissionNewAPI.loadRespEbiForCreation(contextValidation, submission, args.pathEbiReport, sendMail, confidential);
		} else if(submission.type.equals(Submission.Type.UPDATE)) {
			submission.state.code = "SUBU-SMD-F";
			submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission, args.pathEbiReport, sendMail);
		} else if (submission.type.equals(Submission.Type.RELEASE)) {
			submission.state.code = "SUBR-SMD-F";
			submissionNewAPI.loadRespEbiForRelease(contextValidation, submission, args.pathEbiReport, sendMail);
		} else {
			printfln("'%s' est une soumission de type '%s' pour laquelle le script ne s'applique pas", submissionCode, submission.type);
		}
		
		println("ok pour le rechargement du rapport de soumission " + args.pathEbiReport);
	}


}
