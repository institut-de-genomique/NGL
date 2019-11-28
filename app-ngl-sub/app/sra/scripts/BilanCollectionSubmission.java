package sra.scripts;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
//import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
//import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
import validation.ContextValidation;
import play.libs.Json;
import services.RepriseHistorique;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAc?codes=code_soumission_1&codes=code_soumission_2
 * @author sgas
 *
 */
public class BilanCollectionSubmission extends Script<BilanCollectionSubmission.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(BilanCollectionSubmission.class);

	private final SubmissionAPI     submissionAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final AbstractSampleAPI abstractSampleAPI;
	private final SampleAPI         sampleAPI;
	private final StudyAPI          studyAPI;
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;

	@Inject
	public BilanCollectionSubmission(SubmissionAPI     submissionAPI,
					 AbstractStudyAPI  abstractStudyAPI,
					 AbstractSampleAPI abstractSampleAPI,
					 SampleAPI         sampleAPI,
					 StudyAPI          studyAPI,
					 ExperimentAPI     experimentAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.sampleAPI         = sampleAPI;
		this.studyAPI          = studyAPI;
		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	@Override
	public void execute(MyParam args) throws Exception {
		int countExp = 0;
		int countExpNoSubmission = 0;
		int countSample = 0;
		int countSampleNoSubmission = 0;
		int countStudy = 0;
		int countStudyNoSubmission = 0;
		for (Experiment experiment : experimentAPI.dao_all()) {
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experiment.code)).toList();
			countExp++;
			if (submissionList.size() == 0) {
				printfln(experiment.code + " jamais associé à une submission");	
				countExpNoSubmission++;
			}			
		}
		for (AbstractSample absSample : abstractSampleAPI.dao_all()) {
			if(absSample instanceof ExternalSample) {
				continue;
			}
			countSample++;
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", absSample.code)).toList();
			if (submissionList.size() == 0) {
				printfln(absSample.code + " jamais associé à une submission");	
				countSampleNoSubmission++;
			}			
		}	
		for (AbstractStudy absStudy : abstractStudyAPI.dao_all()) {
			if(absStudy instanceof ExternalStudy) {
				continue;
			}
			countStudy++;
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode",  absStudy.code).is("release", false)).toList();
			if (submissionList.size() == 0) {
				printfln(absStudy.code + " jamais associe a une submissions\n");
				countStudyNoSubmission++;
			}
		}
		println("Nombre d'exp sans soumission " + countExpNoSubmission+ " (sur total de " + countExp + ")");
		println("Nombre de sample sans soumission " + countSampleNoSubmission+ " (sur total de " + countSample + ")");
		println("Nombre de study sans soumission " + countStudyNoSubmission+ " (sur total de " + countStudy + ")");
	
	}

}


