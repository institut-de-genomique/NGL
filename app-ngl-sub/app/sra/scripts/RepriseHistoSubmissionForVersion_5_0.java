package sra.scripts;


import java.util.Date;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import play.libs.Json;
import validation.ContextValidation;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.RepriseHistoSubmissionForVersion_5_0
 * @author sgas
 *
 */
public class RepriseHistoSubmissionForVersion_5_0 extends Script<RepriseHistoSubmissionForVersion_5_0.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoSubmissionForVersion_5_0.class);

	private final SubmissionAPI     submissionAPI;
	private final NGLApplication    app;

	@Inject
	public RepriseHistoSubmissionForVersion_5_0(SubmissionAPI     submissionAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	public void repriseHistoSubmission() {
		Iterable<Submission> list_submission =  submissionAPI.dao_all();
		println("Validations des submissions");	
		int cp = 0;
		int cpError = 0;
		for (Submission submission : list_submission) {
			cp++;
			String user = "william";
			ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
			contextValidation.putObject("copy", true);
			submission.traceInformation.modifyDate = new Date();
			submission.traceInformation.modifyUser = user;
			
			switch (submission.type) {
				case RELEASE   : {submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData; break;}
				case UPDATE    : {submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData; break;}
				case CREATION  : {
					if(StringUtils.isNotBlank(submission.analysisCode)) {
						submission.typeRawDataSubmitted = TypeRawDataSubmitted.bionanoRawData;
					} else if (submission.experimentCodes != null && submission.experimentCodes.size()>0) {
						submission.typeRawDataSubmitted = TypeRawDataSubmitted.defaultRawData;
					} else {
						submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
					}
					break;
				}	
				default:
					// Do nothing
			}
		
			submission.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				cpError++;
				contextValidation.displayErrors(logger, "debug");
				println("Error pour " + submission.code);
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));	
			} else {
				submissionAPI.dao_saveObject(submission);				
			}
		}	
		println("Fin de la reprise histo des submissions " + cpError + " error / "  + cp);				
	}
	

	
	@Override
	public void execute(MyParam args) throws Exception {
		repriseHistoSubmission();
		
	}

}


