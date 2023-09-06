package sra.scripts.off;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import models.sra.submit.sra.instance.Experiment;

import javax.inject.Inject;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import sra.scripts.utils.CSVParsing;

public class DebugExperiment_CMX extends ScriptNoArgs {
	private final ExperimentAPI    experimentAPI;
//	private final ExperimentDAO    experimentDAO;
//	private final NGLApplication   app;

	@Inject 
	public DebugExperiment_CMX(ExperimentAPI     experimentAPI) {
		this.experimentAPI    = experimentAPI;
//		this.experimentDAO    = experimentDAO;
//		this.app              = app;
	}
	

	
	

	@Override
	public void execute() throws Exception {
		List<Experiment> listExperiments = new ArrayList<>();
		CSVParser csvParser = CSVParsing.parse(new File("/env/cns/home/sgas/CMX_refCollab_ERS.csv"), ';');
		for (CSVRecord  record : csvParser) {
			String expTitle = record.get(0);
			String expCode = record.get(1);
			Experiment experiment = experimentAPI.get(expCode);
			if (experiment == null) {
				throw new RuntimeException("Pas d'experiment dans la base pour "+ expCode);
			}
			println("experimentCode = '" + expCode + "'");
			println("experimentTitle = '" + expTitle + "'");
			experiment.title = expTitle;
			listExperiments.add(experiment);
		}
//		for (Experiment  experiment : listExperiments) {
//			println("Sauvegarde en base de l'experiment " + experiment.code);
//			experimentAPI.dao_saveObject(experiment);
//		}

	}

}
