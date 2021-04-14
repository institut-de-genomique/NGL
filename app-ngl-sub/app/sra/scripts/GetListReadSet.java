package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import services.Tools;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.GetListReadSet?code=submissionCode&pathFileOut=pathFileOut}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class GetListReadSet extends ScriptWithArgs<GetListReadSet.MyParam> {
	private final SubmissionAPI submissionAPI;
	private final ExperimentAPI experimentAPI;

	@Inject
	public GetListReadSet(SubmissionAPI      submissionAPI,
						  ExperimentAPI      experimentAPI
				) {
		this.submissionAPI = submissionAPI;	
		this.experimentAPI = experimentAPI;
	}
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String code;
		public String pathFileOut;
//		public String[] sampleAC;
//		public String[] projectCode;
	}
	
	@Override
	public void execute(MyParam args) throws Exception {
		String submissionCode = args.code;
		println("submissionCode='"+ args.code+"'");
		Submission submission = submissionAPI.get(submissionCode);
		if(submission==null) {
			println("Pas de soumission dans la base pour le code '"+ args.code + "'");
			return;
		}
		if(submission.experimentCodes.isEmpty()) {
			println("Champs experimentCodes vide pour la soumission '"+ args.code + "'");
			return;
		}
		List<String> readsetCodes = new ArrayList<String>();
		for (String expCode : submission.experimentCodes) {
			Experiment exp = experimentAPI.get(expCode);
			if(!readsetCodes.contains(exp.readSetCode)) {
				readsetCodes.add(exp.readSetCode);
			}
		}
		File outputFile = new File (args.pathFileOut);
		Tools.writeReadSet(readsetCodes, outputFile);
		println("Ecriture du fichier " + outputFile.getPath() );
	}
	
}


