package sra.scripts.off;


import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import services.ScriptTools;


/*
* Script pour corriger les codes des experiments et runs des soumissions BMR et BEZ pour lesquelles il y a deja eu une soumission à l'EBI
* puis deletion qui doivent etre soumis sous un nouveau nom.

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.Debug_BMR_BEZ
* http://localhost:9000/sra/scripts/run/sra.scripts.GetListRawData_GSC_BRM_54TJ575NB
* @author sgas
*
*/
public class GetListRawData_GSC_BRM_54TJ575NB extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;
	private final ScriptTools       scriptTools;
	@Inject
	public GetListRawData_GSC_BRM_54TJ575NB(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 ScriptTools       scriptTools,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
		this.scriptTools       = scriptTools;
//		this.app               = app;
	}



	@Override
	public void execute() throws Exception {
		String submissionCode = "GSC_BRM_54TJ575NB";
		File fileOut = new File("C:\\Users\\sgas\\listRawData_GSC_BRM_54TJ575NB.txt");
		
		Map <String, String> mapRawData = scriptTools.getListRawData(submissionCode);
		scriptTools.writeMap(mapRawData, fileOut);
		printfln("ok");
	}



}
