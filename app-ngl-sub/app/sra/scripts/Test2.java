package sra.scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test2}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Test2 extends ScriptNoArgs {

	private final SubmissionAPI     submissionAPI;
	
	@Inject
	public Test2(SubmissionAPI submissionAPI
				) {
		this.submissionAPI = submissionAPI;

		
	}


	@Override
	public void execute() throws Exception {
		String submissionCode="CNS_BCM_09_03_2016";
		Submission submission = submissionAPI.get(submissionCode);
		for (String expCode : submission.experimentCodes) {
			printfln(expCode.substring(4));
		}
		printfln("fin ecriture des " + submission.experimentCodes.size() + " readsets");
	}
		
		
}