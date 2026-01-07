package sra.scripts;

import java.io.File;

import javax.inject.Inject;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.sra.instance.Submission;
import services.sra.EbiAPI;
import validation.ContextValidation;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestEbiCurl}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */

public class TestEbiCurl extends ScriptNoArgs {
	@SuppressWarnings("unused")
	private static final play.Logger.ALogger logger = play.Logger.of(TestEbiCurl.class);
	private final EbiAPI        ebiAPI;
	private final SubmissionAPI submissionAPI;
	
	
	@Inject
	public TestEbiCurl(EbiAPI        ebiAPI,
			    SubmissionAPI submissionAPI) {
		this.ebiAPI = ebiAPI;
		this.submissionAPI = submissionAPI;
	}


	@SuppressWarnings("unused")
	@Override
	public void execute() throws Exception {
		String submissionFileName = "C:\\Users\\sgas.IBFJ-EVRY\\Downloads\\xbVenVerr\\submissionUmbrella.xml";
		String projectFileName = "C:\\Users\\sgas.IBFJ-EVRY\\Downloads\\xbVenVerr\\umbrella.xml";
		//String result = ebiAPI.ebiCurlJava(submissionFileName, projectFileName);
		//String result = ebiAPI.ebiCurl(submissionFileName, projectFileName);
		//println(result);
		Submission submission = submissionAPI.get("submissionAtlasea_xbVenVerr_umbrella");
		//println("xmlSubmission='" + submission.xmlSubmission + "', xmlProjects='" + submission.xmlProjects + "'");
		//println("submissionFileName='" + submissionFileName + "', projectFileName='" + projectFileName +"'");
		ContextValidation ctx = ContextValidation.createUndefinedContext("sgas");
		ebiAPI.devWebin9Curl(submission, ctx);
		println("Fin traitement. Ecriture du fichier resultat dans " + submission.submissionDirectory + "/" + submission.ebiResult);
	}


		
}