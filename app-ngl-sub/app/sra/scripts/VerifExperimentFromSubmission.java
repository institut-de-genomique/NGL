package sra.scripts;

import java.io.BufferedWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;


/*
 * Script a lancer pour verifier experiment.firstSubmissionDate et experiment.state.code à partir des soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.VerifExperimentForEbiDate
 
 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class VerifExperimentFromSubmission extends ScriptNoArgs {
	
	private final SubmissionAPI submissionAPI;
	private final ExperimentAPI experimentAPI;
	
	

	@Inject
	public VerifExperimentFromSubmission (SubmissionAPI submissionAPI,
				    				   ExperimentAPI experimentAPI) {
		this.submissionAPI = submissionAPI;
		this.experimentAPI = experimentAPI;

	}
	



	@Override
	public void execute() throws Exception {
		//writeEbiSubmissionDate(new File(args.outputFile));
		int cp = 0;
		List<String> listSubCodes = new ArrayList<String>();
		listSubCodes.add("CNS_BYQ_AWF_24RF4HJFI");

		File fileSubmissionExperiment_ok = new File("C:\\Users\\sgas\\debug\\submissionExperiment_ok_.txt");

		Iterable<Submission> iterable = submissionAPI.dao_all();
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(fileSubmissionExperiment_ok))) {
			
			for (Submission submission: iterable) {
				if (StringUtils.isNotBlank(submission.accession) && submission.type.equals(Submission.Type.CREATION)) {
					//println("submission.accession=" + submission.accession + "submission.date");
					output_buffer.write("code = " + submission.code + ", date="+ submission.submissionDate +  "\n" );
					cp++;

					if(! submission.state.code.equals("SUB-F")) {
						println("Probleme de state pour la soumission" + submission.code + " => state = " + submission.state.code);	
					}
					for (String expCode : submission.experimentCodes) {
						Experiment experiment = experimentAPI.get(expCode);
						if (experiment == null) {
							println("experiment " + expCode + " de la soumission " + submission.code + " n'existe pas dans la base");
						}
						if(! submission.submissionDate.toString().equals(experiment.firstSubmissionDate.toString())) {
							println("Probleme de date pour l'experiment" + experiment.code + " => date = " + experiment.firstSubmissionDate + " et submission date = "+ submission.submissionDate);
						} else {
							println("ok pour date pour l'experiment" + experiment.code);

						}
						if(!experiment.state.code.equals("SUB-F")) {
							println("Experiment " + experiment.code +" avec state.code " + experiment.state.code);
						} else {
							println("ok pour stateCode pour l'experiment" + experiment.code);
						}
					}
					
				}
				
				
			}
			println("Nombre de soumission traitées = " + cp);
		}
			
	}
	
	


}
