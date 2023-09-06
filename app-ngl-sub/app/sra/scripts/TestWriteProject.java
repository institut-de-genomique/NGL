package sra.scripts;

import java.io.File;


import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;
import services.XmlServices;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.TestWriteProject}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class TestWriteProject extends ScriptNoArgs {
	private final XmlServices   xmlServices;
	private final SubmissionAPI submissionAPI;
//	private final ExperimentAPI     exp
	@Inject
	public TestWriteProject(ExperimentAPI      experimentAPI,
				XmlServices        xmlServices,
				ProjectAPI         projectAPI,
				SubmissionAPI      submissionAPI
				) {
		this.xmlServices   = xmlServices;
		this.submissionAPI = submissionAPI;
		
	}
	

	@Override
	public void execute() throws Exception {
//		Submission submission = new Submission();
//		submission.type = Type.UPDATE;
//		submission.ebiProjectCode = "STUDY_AYQ_58JI2QAH9";
		String submissionCode = "GSC_AYQ_5C391T8LT";
		Submission submission = submissionAPI.get(submissionCode);
		// File outputFile = new File("\\C:\\Users\\sgas\\umbrella.xml");
		File outputFile2 = new File("\\C:\\Users\\sgas\\submission.xml");

		//xmlServices.writeProjectXml(submission, outputFile);
		xmlServices.writeSubmissionUpdateXml(submission, outputFile2);
		println("fin du test");
	}
		
		
}