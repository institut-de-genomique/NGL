package sra.scripts;


import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import sra.services.ScriptTools;


/*
* Script pour corriger les codes des experiments et runs des soumissions BMR et BEZ pour lesquelles il y a deja eu une soumission à l'EBI
* puis deletion qui doivent etre soumis sous un nouveau nom.

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.GetListRawDataWithCollabFileName
* @author sgas
*
*/
public class GetListRawDataWithCollabFileName extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	// private final SubmissionAPI     submissionAPI;
	// private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;
	private final ScriptTools       scriptTools;
	@Inject
	public GetListRawDataWithCollabFileName(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 ScriptTools       scriptTools,
					 NGLApplication    app) {

		// this.submissionAPI     = submissionAPI;
		// this.experimentAPI     = experimentAPI;
		this.scriptTools       = scriptTools;
//		this.app               = app;
	}



	@Override
	public void execute() throws Exception {
		String submissionCode = "GSC_CIY_9AUB1TFA0";
		File fileAspera = new File("C:\\Users\\sgas.IBFJ-EVRY\\liste_aspera_1.txt");
		File fileCmdLinksForCollabFileName =  new File("C:\\Users\\sgas.IBFJ-EVRY\\cmd_links_forCollabFileName.txt");
		Map <String, String> mapRawData = scriptTools.getListRawDataWithCollabFileName(submissionCode);
		scriptTools.writeMapForListAsperaCaseCollabFileName(mapRawData, fileAspera);
		scriptTools.creeLinksForCollabFileName(mapRawData, fileCmdLinksForCollabFileName);
		printfln("ok");
	}



}
