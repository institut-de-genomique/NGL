package sra.scripts;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Experiment;



/*
 * Script pour verifier laboReadset.submissionDate, laboReadset.submissionUser et laboReadset.submissionState.code à partir des experiments.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.VerifReadSetStateAndDateFromExperiment

 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class VerifReadSetStateAndDateFromExperiment extends ScriptNoArgs {

	private final ExperimentAPI experimentAPI;
	private final ReadSetsAPI readSetsAPI;



	@Inject
	public VerifReadSetStateAndDateFromExperiment (
			ReadSetsAPI   readSetsAPI,
			ExperimentAPI experimentAPI) {
		this.experimentAPI = experimentAPI;
		this.readSetsAPI   = readSetsAPI;

	}




	@Override
	public void execute() throws Exception {
		//writeEbiSubmissionDate(new File(args.outputFile));
		int cp_readset = 0;
		int cp_exp = 0;
		List<String> listSubCodes = new ArrayList<String>();
		listSubCodes.add("CNS_BYQ_AWF_24RF4HJFI");


		Iterable<Experiment> iterable = experimentAPI.dao_all();

		for (Experiment experiment: iterable) {
			if(experiment.typePlatform.equalsIgnoreCase("ls454")) {
				continue;
			}
			cp_exp++;
			if (StringUtils.isNotBlank(experiment.readSetCode)) {
				//tenir compte des soumissions qui ne repondent pas au modele avec plusieurs readsets pour un experiment de soumission :
				String[] readsetCodes;
				readsetCodes = experiment.readSetCode.split("\\|");
				for (String readsetCode : readsetCodes) {
					cp_readset++;
					println("readsetCode=" + readsetCode);
					ReadSet readset = readSetsAPI.get(readsetCode);
					if(readset == null) {
						println("code = " + experiment.code + " sans readset " + readset + " dans la base \n" );
						continue;
					}
					if(experiment.firstSubmissionDate != null) {
						if(readset.submissionDate == null) {
							println("Probleme de date nulle pour le readset " + readset.code + " alors que experiment.firstSubmissionDate = " + experiment.firstSubmissionDate);
							continue;
						} else {
							if(!experiment.firstSubmissionDate.toString().equals(readset.submissionDate.toString())) {
								println("Probleme de date pour le readset " + readset.code + " => date = " + experiment.firstSubmissionDate + " et readset.date = "+ readset.submissionDate);
							} else {
								//println("ok pour date pour l'experiment" + experiment.code);
							}
						}
					}
					if( ! experiment.state.code.equals(readset.submissionState.code)) {
						println("readset " + readset.code +" avec submissionState.code different de l'experiment = " + readset.submissionState.code);
					} else {
						//println("ok pour submissionStateCode pour le readset" + readset.code);
					}

					if(readset.submissionUser == null) {
						println("Probleme de user null pour le readset " + readset.code);
					}
					if( ! readset.submissionUser.equals(experiment.traceInformation.createUser)) {
						println("readset " + readset.code +" avec submissionUser different de celui indique dans l'experiment = " + readset.submissionUser);
					}
				}
			}
		}

		println("Nombre de readsets traites = " + cp_readset + " Nombre d'experiments traites= " + cp_exp);
	}

}





