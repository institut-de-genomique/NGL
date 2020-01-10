package sra.scripts;


import javax.inject.Inject;
//import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;


/*
* List les rawData d'une soumission

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.ListRawData?submissionCode=XXX
* @author sgas
*
*/
public class ListRawData extends Script<ListRawData.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public ListRawData(SubmissionAPI     submissionAPI,
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
				String directory = rawData.directory;
				String relatifName = rawData.relatifName;
				printfln("name=%s", directory + "/" + relatifName);
				
//				if(rawData.directory.startsWith("/ccc/")){
//					int index = rawData.directory.indexOf("/rawdata/");
//					String lotseq_dir = rawData.directory.substring(index + 9);
//					String cns_directory="/env/cns/proj/" + lotseq_dir;
//					printfln("name=%s", cns_directory + "/" + relatifName);
//				}
			}
		}
	}

}
