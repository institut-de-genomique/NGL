package sra.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;


import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import ngl.refactoring.state.SRAExperimentStateNames;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAc_new?codes=code_soumission_1&codes=code_soumission_2
 * @author sgas
 *
 */
public class BilanAc_new extends ScriptWithArgs<BilanAc_new.MyParam>{
	
	private final ExperimentAPI experimentAPI;
	private final ReadSetsAPI   laboReadSetAPI;

	

	@Inject
	public BilanAc_new (ExperimentAPI experimentAPI,
				    	ReadSetsAPI    laboReadSetAPI) {
		this.experimentAPI  = experimentAPI;
		this.laboReadSetAPI = laboReadSetAPI;
	}

	
	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String[] studyAC;
//		public String[] sampleAC;
//		public String[] projectCode;
	}
	

	@Override
	public void execute(MyParam args) throws Exception {	
		
//		if(args.projectCode.length==0 && args.studyAC.length==0 && args.sampleAC.length==0) {
//			println ("Aucun argument");
//			return;
//		}
				
		List<Query> queries = new ArrayList<>();
		queries.add(DBQuery.is("state.code", SRAExperimentStateNames.SUB_F));
		Query query = null;
		if(args.studyAC.length > 0 ) {
			println("Jai des studyAC");
			List<String> listStudyAC =  new ArrayList<String>(Arrays.asList(args.studyAC));
			for(String ac : listStudyAC) {
				println("studyAC = %s", ac);
			}
			queries.add(DBQuery.in("studyAccession", listStudyAC));
			println("j'ai mis à jour ma query");
		}
//		if(args.sampleAC.length > 0 ) {
//			List<String> listSampleAC = new ArrayList<String>(Arrays.asList(args.sampleAC));
//			queries.add(DBQuery.in("sampleAccession", listSampleAC));
//			println("j'ai mis à jour ma query");
//
//		}		
//		if(args.projectCode.length > 0 ) {
//			List<String> listprojectCode =  new ArrayList<String>(Arrays.asList(args.projectCode));
//			queries.add(DBQuery.in("projectCode", listprojectCode));
//			println("j'ai mis à jour ma query");
//
//		}		
			
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
			println("query ok = %s", query.toString());
		}
		List <Experiment> experimentList = experimentAPI.dao_findAsList(query);
		println("recuperation de %d readset ", experimentList.size());
		int cp_rawData = 0;
		int cp_exp = 0;
		for (Experiment experiment : experimentList) {
			cp_exp++;
			for(RawData rawData : experiment.run.listRawData) {
				cp_rawData++;
				if ("LS454".equals(experiment.typePlatform)) {
					println("fichier=%s, expCode=%s, expAccession=%s, studyAC=%s; sampleAC=%s, readsetCode=%s, techno=%s", 
						rawData.relatifName, experiment.code, experiment.accession, experiment.studyAccession, experiment.sampleAccession, experiment.readSetCode, experiment.typePlatform);
				} else {
					if ((StringUtils.isNotBlank(experiment.readSetCode) && (!experiment.readSetCode.contains("|")))) {
						ReadSet readSet = laboReadSetAPI.get(experiment.readSetCode);
						String materiel = "";
						String refCollab = "";
						if (StringUtils.isNotBlank(readSet.sampleCode)) {
							materiel = readSet.sampleCode;
						}
						if (StringUtils.isNotBlank(readSet.sampleOnContainer.referenceCollab)) {
							refCollab = readSet.sampleOnContainer.referenceCollab;
						}
						println("fichier=%s, expCode=%s, expAccession=%s, studyAC=%s; sampleAC=%s, readsetCode=%s, techno=%s, materiel=%s, refCollab=%s",
								rawData.relatifName, experiment.code, experiment.accession, experiment.studyAccession, 
								experiment.sampleAccession, experiment.readSetCode, experiment.typePlatform, materiel, refCollab);
					} else {
						println("fichier=%s, expCode=%s, expAccession=%s, studyAC=%s; sampleAC=%s, readsetCode=%s, techno=%s", 
								rawData.relatifName, experiment.code, experiment.accession, experiment.studyAccession, experiment.sampleAccession, experiment.readSetCode, experiment.typePlatform);
					}
				}
			}	
			println("Nombre d'experiment soumis=%s et nombre de fichiers=%s",cp_exp, cp_rawData);
		}
	}

}
