package sra.scripts;

import java.io.File;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.common.instance.Submission;
import services.XmlServices;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Test extends ScriptNoArgs {
	private final SubmissionAPI submissionAPI;
	private final XmlServices xmlServices;
	@Inject
	public Test(ExperimentAPI      experimentAPI,
				SubmissionAPI      submissionAPI,
				XmlServices        xmlServices
				) {
		this.submissionAPI = submissionAPI;
		this.xmlServices = xmlServices;
		
	}
	
	@Override
	public void execute() throws Exception {
		String submissionCode = "GSC_BHW_BNP_44BH5KE4Z";
		Submission submission = submissionAPI.get(submissionCode);
		println("submissionCode = " + submission.code);
		xmlServices.writeStudyXml(submission,new File("/env/cns/home/sgas/study.xml"));

		
	}


}
