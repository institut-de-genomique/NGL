package sra.scripts.off;


import javax.inject.Inject;
//import org.mongojack.DBQuery;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;


/*
* Script pour ajouter studyCode et sampleCode dans exp lies à la soumission BBK
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.Debug_BBK?code=code_soumission_1&codes=code_soumission_2
* @author sgas
*
*/
public class Debug_BBK extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);
	private final AbstractStudyAPI   abstractStudyAPI;
	private final AbstractSampleAPI  abstractSampleAPI;
	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public Debug_BBK(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 AbstractStudyAPI      abstractStudyAPI,
					 AbstractSampleAPI     abstractSampleAPI,
					 NGLApplication    app) {
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}


	@Override
	public void execute() throws Exception {
		String submissionCode = "GSC_BBK_15_01_2020";
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		for (String expCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_getObject(expCode);
			if (StringUtils.isNotBlank(experiment.accession) && 
			(StringUtils.isBlank(experiment.studyCode) || StringUtils.isBlank(experiment.sampleCode) )) {
				AbstractStudy absStudy  = abstractStudyAPI.dao_findOne(DBQuery.is("accession", experiment.studyAccession));
				experiment.studyCode = absStudy.code;
				AbstractSample absSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", experiment.sampleAccession));
				experiment.sampleCode = absSample.code;
				println("sauvegarde de l'experiment " + experiment.code);
				experimentAPI.dao_saveObject(experiment);
			}
		}
	}



}
