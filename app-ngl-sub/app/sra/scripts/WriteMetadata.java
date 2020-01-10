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
import services.XmlServices;


/*
* Ecrit les metadata d'une soumission

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.WriteMetadata?submissionCode=XXX&resultDirectory=/env/cns/home...
* @author sgas
*
*/
public class WriteMetadata extends Script<WriteMetadata.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;
    private final XmlServices       xmlServices;

    @Inject
	public WriteMetadata(SubmissionAPI     submissionAPI,
					 XmlServices       xmlServices,
					 ExperimentAPI     experimentAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
		this.xmlServices       = xmlServices;
	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
		public String resultDirectory;
	}


	@Override
	public void execute(MyParam args) throws Exception {
		//Submission submission = submissionAPI.dao_getObject(args.submissionCode);
		Submission submission = xmlServices.writeAllXml(args.submissionCode, args.resultDirectory);
	}

}
