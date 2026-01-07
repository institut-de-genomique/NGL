package sra.scripts;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.w3c.dom.Element;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.State;
import models.laboratory.run.instance.Analysis;
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

//http://localhost:9000/sra/scripts/run/sra.scripts.ReloadEbiReportForAtlasea?pathReportsAtlasea=C:\Users\sgas.IBFJ-EVRY\reportAtlasea
public class ReloadEbiReportForAtlasea extends Script<ReloadEbiReportForAtlasea.MyParam>{
	

	private final SubmissionNewAPI          submissionNewAPI;
	private final AtlaseaDAO                atlaseaDAO;
	private static final play.Logger.ALogger logger = play.Logger.of(ReloadEbiReportForAtlasea.class);

	
	@Inject
	public ReloadEbiReportForAtlasea (SubmissionNewAPI         submissionNewAPI,
			                          AtlaseaDAO               atlaseaDAO) {
		this.submissionNewAPI  = submissionNewAPI;
		this.atlaseaDAO        = atlaseaDAO;
	}
		
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String pathReportsAtlasea;
	}
			
	@Override
	public void execute(MyParam args) throws Exception {
		List<Submission> listSubmissions = MongoDBDAO.find( InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
				DBQuery.regex("code", Pattern.compile("atlasea"))).toList();
		int cp = 0;
		// pour raccourcir tests : 
		//listSubmissions = Iterables.take(listSubmissions, 2).toList();
		for(Submission submission : listSubmissions) {

			File ebiReport = new File(args.pathReportsAtlasea, submission.ebiResult);
			if (! ebiReport.exists()) {
				println("rapport non trouve sur disques pour " + submission.code);
				continue;
			} 
			String tolid = submission.code;
			tolid = tolid.replace("submission_atlasea_", "");
			tolid = tolid.replace("_studyPrimaryAssembly", "");
			tolid = tolid.replace("_studyRawData", "");
			tolid =  tolid.replace("_umbrella", "");
			tolid =  tolid.replace("_updateUmbrella", "");
			
			if( ! atlaseaDAO.checkObjectExist(DBQuery.is("tolId", tolid))) {
				println("tolid " + tolid +" non present dans table Atlasea ");
				continue;
			}
			cp++;
			//println("chemin du rapport = " + ebiReport.getAbsolutePath());

			String user = "sgas";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user);
			boolean sendMail = false;
			boolean confidential = true;
			if(submission.code.contains("atlasea")) {
				//logger.debug("submission " + submission.code + " est bien une soumission atlasea");
				confidential = false;
			}
			if(submission.type.equals(Submission.Type.CREATION)) {
				submission.state.code = "SUB-SMD-F";
				submissionNewAPI.loadRespEbiForCreation(contextValidation, submission, ebiReport.getAbsolutePath(), sendMail, confidential);
			} else if(submission.type.equals(Submission.Type.UPDATE)) {
				submission.state.code = "SUBU-SMD-F";
				submissionNewAPI.loadRespEbiForUpdate(contextValidation, submission, ebiReport.getAbsolutePath(), sendMail);
			} else if (submission.type.equals(Submission.Type.RELEASE)) {
				submission.state.code = "SUBR-SMD-F";
				submissionNewAPI.loadRespEbiForRelease(contextValidation, submission, ebiReport.getAbsolutePath(), sendMail);
			} else {
				printfln("'%s' est une soumission de type '%s' pour laquelle le script ne s'applique pas", submission.code, submission.type);
			}
			println("ok pour le rechargement du rapport de soumission " + args.pathReportsAtlasea);
		}
		println("ok pour le rechargement des " + cp + " rapports de soumission ");

	}


}
