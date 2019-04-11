package sra.scripts;


import javax.inject.Inject;
//import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;


/*
* Script pour corriger les rawData d'une soumission 
* - en remplacant extention fastq.gz par fastq
* - en enlevant md5 
* - en mettant gzip à true
* - en enlevant .gz de relatif_name
* - on remet la soumission à IW-SUB
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.Debug_BDA?code=code_soumission_1&codes=code_soumission_2
* @author sgas
*
*/
public class Debug_BDA extends Script<Debug_BDA.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public Debug_BDA(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}


	@Override
	public void execute(MyParam args) throws Exception {

		Submission submission = submissionAPI.dao_getObject(args.submissionCode);
		for (String expCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_getObject(expCode);
			for (RawData rawData : experiment.run.listRawData) {
				String relatifName = rawData.relatifName;
				String newRelatifName = relatifName.replaceAll("\\.gz", "");
				printfln("relatifName=%s et newRelatifName=%s", relatifName, newRelatifName);
				rawData.relatifName=newRelatifName;
				rawData.gzipForSubmission=true;
				rawData.md5="";
				rawData.extention="fastq";
			}
			experimentAPI.dao_saveObject(experiment);
		}
	}

}
