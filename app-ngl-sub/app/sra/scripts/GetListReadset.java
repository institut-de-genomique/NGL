package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.sra.submit.sra.instance.Submission;
import sra.services.Tools;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.GetListReadset?code=submissionCode&pathFileOut=pathFileOut}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class GetListReadset extends ScriptWithArgs<GetListReadset.MyParam> {
	//public class GetListRefCollabToAc extends ScriptNoArgs {
	private final SubmissionAPI submissionAPI;

	@Inject
	public GetListReadset(SubmissionAPI      submissionAPI
				) {
		this.submissionAPI = submissionAPI;	
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
		//String submissionCode = "GSC_APX_BXT_38AF1N87U";
		Submission submission = submissionAPI.get(submissionCode);
		if(submission==null) {
			println("Pas de soumission dans la base pour le code '"+ args.code + "'");
			return;
		}
		if(submission.experimentCodes == null || submission.experimentCodes.size()==0) {
			println("Pas de readsets pour la soumission '" + args.code + "'");
			return;
		}
		List<String> readsetCodes = new ArrayList<String>();
		for (String code : submission.experimentCodes) {
			readsetCodes.add(code.replaceFirst("exp_", ""));
		}
		File outputFile = new File (args.pathFileOut);
		Tools.writeReadSet(readsetCodes, outputFile);
		println("Ecriture du fichier " + outputFile.getPath() );
	}
	
}


