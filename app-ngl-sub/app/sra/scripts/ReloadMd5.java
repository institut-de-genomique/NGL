package sra.scripts;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ReloadMd5?submissionCode=codeSoumission}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class ReloadMd5 extends Script<ReloadMd5.MyParams> {
	//private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5.class);
	private final SubmissionAPI    submissionAPI; 
	private final ExperimentAPI    experimentAPI; 
	private final ReadSetsAPI      readSetsAPI;

	@Inject
	public ReloadMd5(SubmissionAPI    submissionAPI,
					 ExperimentAPI    experimentAPI,
					 ReadSetsAPI      readSetsAPI) {
		this.submissionAPI = submissionAPI;
		this.experimentAPI = experimentAPI;
		this.readSetsAPI   = readSetsAPI;
	}
	
	
	// Structure de controle et stockage des arguments de l'url.
	public static class MyParams {
		//public List<String> codeSoumissions;	
		public String codeSoumissions;	
	}
	

	public boolean loadMd5InSubmission (String submissionCode) {
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		boolean success = true;
		List<Experiment> listExperiments = new ArrayList<>();
		for(String expCode: submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_getObject(expCode);
			if (experiment == null) {
				throw new RuntimeException("Pas d'experiment dans la base pour "+ expCode);
			}
			println("experimentCode = " + expCode);
			ReadSet readSet = readSetsAPI.dao_getObject(experiment.readSetCode);	
			if (readSet == null) {
				throw new RuntimeException("Pas de readSet dans la base pour "+ experiment.readSetCode);
			}			
			println("readSetCode = " + readSet.code);
			List <models.laboratory.run.instance.File> list_files =  readSet.files;
			int countRawData = experiment.run.listRawData.size();
			for (RawData rawData : experiment.run.listRawData) {
				println("rawData.relatifName = " + rawData.relatifName);
				for (models.laboratory.run.instance.File runFile :  list_files) {
					String relatifName = runFile.fullname;	
					println("runFile.relatifName = " + relatifName);
					if (runFile.properties != null && runFile.properties.containsKey("md5")) {			
						if (rawData.relatifName.equals(relatifName)) {
							rawData.md5 =  (String) runFile.properties.get("md5").value;
							countRawData--;
							println("Pour le rawData " + rawData.relatifName + ", md5="+ rawData.md5);
						}
					} else {
						// rien le countRawData ne sera pas à zero au final
					}
				}
			}
			if (countRawData==0) {
				println("Tous les rawData de l'experiment " + expCode + " ont ete rechargés pour leur md5");
			} else {
				println("Probleme : Tous les rawData de l'experiment " + expCode +" n'ont pas ete rechargés pour leur md5");
				success = false;
			}
			listExperiments.add(experiment);
		}
		if (success) {
			for (Experiment experiment : listExperiments) {
				println("Sauvegarde en base de l'experiment " + experiment.code);
				experimentAPI.dao_saveObject(experiment);
			}
		}
		return success;	
	}
	
	
	@Override
	public void execute(MyParams args) throws Exception {
		loadMd5InSubmission(args.codeSoumissions);
	}

}
