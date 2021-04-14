package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import services.Tools;


/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir d'un fichier md5.txt
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.ReloadFileMd5?submissionCode=codeSoumission&pathMd5File=md5.txt}
 * <br>path
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class ReloadFileMd5 extends Script<ReloadFileMd5.MyParams> {
	//private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5.class);
	private final SubmissionAPI    submissionAPI; 
	private final ExperimentAPI    experimentAPI; 
	private final ReadSetsAPI      readSetsAPI;

	@Inject
	public ReloadFileMd5(SubmissionAPI    submissionAPI,
					 ExperimentAPI    experimentAPI,
					 ReadSetsAPI      readSetsAPI
					 ) {
		this.submissionAPI = submissionAPI;
		this.experimentAPI = experimentAPI;
		this.readSetsAPI   = readSetsAPI;
	}
	
	
	// Structure de controle et stockage des arguments de l'url.
	public static class MyParams {
		//public List<String> codeSoumissions;	
		public String codeSoumissions;	
		public String pathMd5File;
	}
	

	public boolean loadMd5InSubmission (String submissionCode, String pathMd5File) {
		println("pathMd5File = " + pathMd5File);
		
		File md5File = new File(pathMd5File);
		
		Map<String, String> mapMd5 = Tools.loadMd5File(md5File);
//		
//		for (Entry entry : mapMd5.entrySet()) {
//			println(entry.getKey() + " " +entry.getValue());
//		}
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		boolean success = true;
		List<Experiment> listExperiments = new ArrayList<>();
		for(String expCode: submission.experimentCodes) {
			Experiment experiment = experimentAPI.dao_getObject(expCode);
			if (experiment == null) {
				throw new RuntimeException("Pas d'experiment dans la base pour "+ expCode);
			}
			if(experiment.run == null) {
				throw new RuntimeException("Pas de run dans l'experiment "+ expCode);
			}
			
			for(RawData rawData : experiment.run.listRawData) {
				rawData.gzipForSubmission = false;
				rawData.extention="fastq.gz";
				rawData.location = "CNS";
				
				if (rawData.directory.startsWith("/ccc/")) {
					int index = rawData.directory.indexOf("/rawdata/");
					String lotseq_dir = rawData.directory.substring(index + 9);
					String cns_directory = "/env/cns/proj/" + lotseq_dir;
					rawData.directory = cns_directory;
				}
				if (!rawData.relatifName.endsWith(".gz")) {
					rawData.relatifName = rawData.relatifName.concat(".gz");
				}
				if(mapMd5.containsKey(rawData.relatifName)){
					rawData.md5= mapMd5.get(rawData.relatifName);
				} else {
					println("Pas de md5 pour rawData.relatifName :" + rawData.relatifName +" pour experiment " + experiment.code);
					success = false;
				}
				listExperiments.add(experiment);
			}
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
		loadMd5InSubmission(args.codeSoumissions, args.pathMd5File);
	}

}
