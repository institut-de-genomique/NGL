package sra.scripts;


import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;
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
//	private final NGLApplication    app;
    private final XmlServices       xmlServices;

    @Inject
	public WriteMetadata(SubmissionAPI     submissionAPI,
					 XmlServices       xmlServices,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
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
		Submission submission = submissionAPI.dao_getObject(args.submissionCode);
		xmlServices.writeAllXml(submission, args.resultDirectory);
	}

}
