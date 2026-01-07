package sra.scripts.crons;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import sra.services.crons.SubmissionAtlaseaProjectsToEbi;

/*
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.crons.ScriptSubmissionAtlaseaProjectsToEbi
 * @author sgas
 *
 */
public class ScriptSubmissionAtlaseaProjectsToEbi extends ScriptNoArgs{

	private final SubmissionAtlaseaProjectsToEbi submissionAtlaseaProjectsToEbi;
	
	@Inject
	ScriptSubmissionAtlaseaProjectsToEbi (SubmissionAtlaseaProjectsToEbi submissionAtlaseaProjectsToEbi) {
		this.submissionAtlaseaProjectsToEbi = submissionAtlaseaProjectsToEbi;
	}

	@Override
	public void execute() throws Exception {
		printfln("\n", "Lancement de ScriptSubmissionAtlaseaProjectsToEbi");
		submissionAtlaseaProjectsToEbi.run();
		printfln("\n", "End ScriptSubmissionAtlaseaProjectsToEbi");
	}
	
}
