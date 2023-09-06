package sra.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;


/*
 * Aucune erreur en PROD. Ne pas relancer.
 * Script a lancer pour mettre a jour la base pour soumission.submissionDate à partir des donnees de l'EBI.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.UpdateExperimentTypePlatform
 
 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class UpdateExperimentTypePlatform extends ScriptNoArgs {
	
	private final ExperimentAPI experimentAPI;

	
	

	@Inject
	public UpdateExperimentTypePlatform (ExperimentAPI experimentAPI) {
		this.experimentAPI = experimentAPI;

	}
	



	@Override
	public void execute() throws Exception {
		//writeEbiSubmissionDate(new File(args.outputFile));
		File fileExpTypePlatform_ok = new File("C:\\Users\\sgas\\debug\\expTypePlatform_ok_.txt");

		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(fileExpTypePlatform_ok))) {

			int cp = 0;
			List<String> listSubCodes = new ArrayList<String>();
			listSubCodes.add("CNS_BYQ_AWF_24RF4HJFI");

			Iterable<Experiment> iterable = experimentAPI.dao_all();
			
			for (Experiment experiment: iterable) {
				if (StringUtils.isNotBlank(experiment.typePlatform)) {
					experiment.typePlatform = experiment.typePlatform.toLowerCase();
					experimentAPI.dao_saveObject(experiment);
					cp++;
				}	
				output_buffer.write(experiment.code);
			}
			println("Nombre d'experiment traitées = " + cp);
		}
	}
			
}
	
	


