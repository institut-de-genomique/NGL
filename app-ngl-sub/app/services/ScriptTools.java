package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;

public class ScriptTools {
	private static final play.Logger.ALogger logger = play.Logger.of(ScriptTools.class);

	private final ExperimentAPI  experimentAPI;
	private final SubmissionAPI  submissionAPI;

	@Inject
	private ScriptTools(ExperimentAPI     experimentAPI,
						SubmissionAPI     submissionAPI
			) {
		this.experimentAPI  = experimentAPI;
		this.submissionAPI  = submissionAPI;
		
	}
	
	public  Map<String, String> getListRawData (String submissionCode) throws IOException, SraException {
		Map<String, String> mapRawData = new HashMap<>();
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		if (submission == null) {
			logger.debug("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA Dans base, soumission inexistante pour ", submissionCode);
			return mapRawData;
		} else {
			logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  Recuperation dans la base de la soumission" , submissionCode);
		}
		// submission de type release qui ne contient pas de fichiers de données brutes
		if (submission.type == Submission.Type.RELEASE) {
			return mapRawData;
		}
		if (submission.experimentCodes.isEmpty()) {
			logger.debug("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA Aucun experimentCodes dans " , submission.code );
			return mapRawData;
		} else {
			logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX La soumission contient des experimentCodes");
		}

		for (String experimentCode : submission.experimentCodes) {
			Experiment experiment = experimentAPI.get(experimentCode);	
			logger.debug(experimentCode);
			Run run = experiment.run;
			for(RawData rawData : run.listRawData) {
				String relatifName = rawData.relatifName;
				logger.debug("relatifName = " + relatifName );
				if(!relatifName.endsWith("gz")) {
					relatifName = relatifName.concat(".gz");
					//logger.debug("ajout extention gz => new relatifName" + relatifName);
				}
				String directory = rawData.directory;
				String cns_directory = rawData.directory;
				if (directory.startsWith("/ccc/")) {
					int index = rawData.directory.indexOf("/rawdata/");
					String lotseq_dir = directory.substring(index + 9);
					cns_directory="/env/cns/proj/" + lotseq_dir;
					logger.debug("directory ccc transformé en cns_directory " + cns_directory);
				}
				//mapRawData.put(rawData.relatifName, rawData.directory + File.separator + rawData.relatifName);
				mapRawData.put(relatifName, cns_directory + "/" + relatifName);
				
				
			}
		}
		return mapRawData;
	}
	
	
	public void writeMap(Map<String, String> map, File outputFile) throws IOException, SraException { 
		System.out.println("Creation du fichier " + outputFile);
	
		StringBuilder sb = new StringBuilder();
		for(String k: map.keySet()) {
			String value = map.get(k);
			//sb.append(k + "  " + value + "\n");
			sb.append("ln -s " + value + "  " + k + "\n");
			
		}
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			output_buffer.write(sb.toString());
		}
	}
	
}
	