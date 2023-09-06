package sra.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Experiment;


/*
 *
 * Script a lancer pour mettre a jour la base pour readset.submissionDate, readset.submissionUser et readset.submissionState.code 
 * à partir des experiments apres avoir verifier experiments à jour (VerifExperimentForEbiDate)
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.UpdateReadSetStateAndDateFromExperiment

 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class UpdateReadSetStateAndDateFromExperiment extends ScriptNoArgs {

	private final ExperimentAPI experimentAPI;
	private final ReadSetsAPI readSetsAPI;



	@Inject
	public UpdateReadSetStateAndDateFromExperiment (
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

		File fileSubmissionExperiment_ok = new File("C:\\Users\\sgas\\debug\\readset_ok_.txt");

		Iterable<Experiment> iterable = experimentAPI.dao_all();

		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(fileSubmissionExperiment_ok))) {

			for (Experiment experiment: iterable) {
				if(experiment.typePlatform.equalsIgnoreCase("ls454")) {
					continue;
				}
				cp_exp++;
				if (StringUtils.isNotBlank(experiment.readSetCode)) {
					//tenir compte des soumissions qui ne repondent pas au modele avec plusieurs readsets pour un experiment de soumission :
					String[] readsetCodes = null;
					readsetCodes = experiment.readSetCode.split("\\|");
					for (String readsetCode : readsetCodes) {
						cp_readset++;
						println("readsetCode = "+ readsetCode);

						ReadSet readset = readSetsAPI.get(readsetCode);
						if(readset == null) {
							output_buffer.write("code = " + experiment.code + " sans readset " + readset + " dans la base \n" );
							continue;
						}
						if(experiment.firstSubmissionDate != null) {
							if(readset.submissionDate == null) {
								readset.submissionDate = experiment.firstSubmissionDate;
								println("Probleme de date nulle pour le readset " + readset.code + " alors que experiment.firstSubmissionDate = " + experiment.firstSubmissionDate);
							} else {
								if(!experiment.firstSubmissionDate.toString().equals(readset.submissionDate.toString())) {
									readset.submissionDate = experiment.firstSubmissionDate;
									println("Probleme de date pour le readset " + readset.code + " => date = " + experiment.firstSubmissionDate + " et readset.date = "+ readset.submissionDate);
								} else {
									//println("ok pour date pour l'experiment" + experiment.code);
								}
							}
						}
						
						if( ! experiment.state.code.equals(readset.submissionState.code)) {
							readset.submissionState.code = experiment.state.code;
							println("readset " + readset.code +" avec submissionState.code different de l'experiment = " + readset.submissionState.code);
						} else {
							//println("ok pour submissionStateCode pour le readset" + readset.code);
						}
						//NGL3832 :
						if(experiment.state.code.startsWith("SUBU")||experiment.state.code.startsWith("SUBR")) {
							readset.submissionState.code = "SUB-F";
						}
						if(readset.submissionUser == null) {
							println("Probleme de user null pour le readset " + readset.code);
							readset.submissionUser = experiment.traceInformation.createUser;
						}
						if( ! readset.submissionUser.equals(experiment.traceInformation.createUser)) {
							readset.submissionUser = experiment.traceInformation.createUser;
							println("readset " + readset.code +" avec submissionUser different de celui indique dans l'experiment = " + readset.submissionUser);
						}
						readSetsAPI.dao_update(DBQuery.is("code", readset.code), 
								DBUpdate.set("submissionState.code", readset.submissionState.code)
								.set("submissionDate", readset.submissionDate)
								.set("submissionUser", readset.submissionUser)
								.set("traceInformation.modifyUser", "sgas")
								.set("traceInformation.modifyDate", new java.util.Date()));
					}
				}
			}
		}
		println("Nombre de readsets traites = " + cp_readset + " Nombre d'experiments traites= " + cp_exp);
	}

}





