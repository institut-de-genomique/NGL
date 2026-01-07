package sra.scripts.crons;

import javax.inject.Inject;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import services.sra.EbiAPI;
import sra.services.crons.AlerteEmailPublicStudy;

/*
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.crons.ScriptAlerteEmailPublicStudy
 * @author sgas
 *
 */
public class ScriptAlerteEmailPublicStudy extends ScriptNoArgs {
	private final AlerteEmailPublicStudy alerteEmailPublicStudy;
	
	@Inject
	public ScriptAlerteEmailPublicStudy(AlerteEmailPublicStudy        alerteEmailPublicStudy) {
		this.alerteEmailPublicStudy = alerteEmailPublicStudy;
	}

	@Override
	public void execute() throws Exception {
		printfln("\n", "Lancement de ScriptAlerteEmailPublicStudy");
		alerteEmailPublicStudy.run();
		printfln("\n", "End ScriptAlerteEmailPublicStudy");
	}
	
}
