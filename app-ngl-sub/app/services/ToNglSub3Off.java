package services;

/** import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.UserRefCollabType;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.Logger;

import ngl.refactoring.state.SRASubmissionStateNames;
import java.util.Map.Entry;

import java.util.Iterator;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.util.SraException; */


// obsolete car au passage à NGL-SUB-3 disparition de plusieurs champs de l'objet submission.
//public class ToNglSub3Off {
//	private final SubmissionAPI     submissionAPI;
//	private final ConfigurationAPI  configurationAPI;
//	private final AbstractStudyAPI  abstractStudyAPI;
//	private final AbstractSampleAPI abstractSampleAPI;
//	private final SampleAPI         sampleAPI;
//	private final ExperimentAPI     experimentAPI;
//	private final ExperimentDAO     experimentDAO;
//	private final ReadSetsAPI       readsetsAPI;
//	private final String undifinedValueInRepriseHistorique = "undifinedValueRepriseHistorique";
//	private static final play.Logger.ALogger logger = play.Logger.of(ToNglSub3Off.class);
//
//	@Inject
//	public ToNglSub3Off(
//			         SubmissionAPI     submissionAPI,
//					 ConfigurationAPI  configurationAPI,
//					 AbstractStudyAPI  abstractStudyAPI,
////					 StudyAPI          studyAPI,
//					 AbstractSampleAPI abstractSampleAPI,
//					 SampleAPI         sampleAPI,
//					 ExperimentAPI     experimentAPI,
//					 ExperimentDAO     experimentDAO,
//					 ReadSetsAPI       readsetsAPI
////					 NGLApplication    app
//					 ) {
//
//		this.submissionAPI     = submissionAPI;
//		this.configurationAPI  = configurationAPI;
//		this.abstractStudyAPI  = abstractStudyAPI;
//		this.abstractSampleAPI = abstractSampleAPI;
//		this.sampleAPI         = sampleAPI;
//		this.experimentAPI     = experimentAPI;
//		this.experimentDAO     = experimentDAO;
//		this.readsetsAPI       = readsetsAPI;
//	}
//
//
//	public void executeForConfiguration() throws Exception {
//		
//		// changer les etats de la soumission  mettre à jour le champs type et supprimer le champs release:
//		for (Configuration configuration : configurationAPI.dao_all()) {	
//			switch (configuration.state.code) {
//				case "N" : {
//					configuration.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "NONE" : {
//					configuration.state.code = SRASubmissionStateNames.NONE;
//					break;
//				}
//				case "SUB-N" : {
//					configuration.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				default :{
////					printfln("Status non prevu : " + configuration.state.code + " pour la configuration " + configuration.code);
////					continue;
//					throw new SraException("Status non prevu : " + configuration.state.code + " pour la configuration " + configuration.code);
//				}
//			}
//			switch (configuration.strategyStudy) {
//				case strategy_internal_study :  {
//					configuration.strategyStudy = Configuration.StrategyStudy.STRATEGY_CODE_STUDY;
//					break;
//				}
//				case strategy_external_study : {
//					configuration.strategyStudy = Configuration.StrategyStudy.STRATEGY_AC_STUDY;
//					break;
//				}
//				case STRATEGY_AC_STUDY : case STRATEGY_CODE_STUDY : {
//					break;
//				}
//				default :{
//					throw new SraException("Status de strategyStudy non attendu " + configuration.strategyStudy);
//				}
//			}
//			switch (configuration.strategySample) {
//			
//			case STRATEGY_AC_SAMPLE: case STRATEGY_CODE_SAMPLE_BANK : case STRATEGY_CODE_SAMPLE_REFCOLLAB: case STRATEGY_CODE_SAMPLE_TAXON: {
//				break;
//			}
//			case strategy_external_sample : {
//				configuration.strategySample = Configuration.StrategySample.STRATEGY_AC_SAMPLE;
//				break;
//			}
//			case strategy_sample_bank:{
//				configuration.strategySample = Configuration.StrategySample.STRATEGY_CODE_SAMPLE_BANK;
//				break;
//			}
//			case strategy_sample_clone: {
//				configuration.strategySample = Configuration.StrategySample.STRATEGY_CODE_SAMPLE_REFCOLLAB;
//				break;
//			}
//			case strategy_sample_taxon: {
//				configuration.strategySample = Configuration.StrategySample.STRATEGY_CODE_SAMPLE_TAXON;
//				break;
//			}
//			default:
//				throw new SraException("Status de strategySample non attendu pour la config " + configuration.strategySample);
//			} 
//			configurationAPI.dao_saveObject(configuration);
//		}
//		Logger.error("ok Traitement des configurations");
//	} 
//	
//	
//	
//	public void executeForSubmission() throws Exception {
//		for (Submission submission : submissionAPI.dao_all()) {			
//			logger.debug("   - submissionCode = " + submission.code);
//			switch (submission.state.code) {
//				case "F-SUB" : {
//					submission.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}			
//				case "V-SUB" : {
//					submission.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}		
//				case "N" : {
//					submission.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "FE-SUB" : {
//					submission.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}	
//
//				case "IP-SUB" : {
//					submission.state.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}	
//				case "SUB-F" : {
//					break;
//				}	
//				case "SUB-N" : {
//					break;
//				}
//				case "SUB-V" : {
//					break;
//				}
//				default :{
////					printfln("Status non prevu : " + submission.state.code + " pour la soumission " + submission.code);
////					continue;
//					throw new SraException("Status non prevu : " + submission.state.code + " pour la soumission " + submission.code);
//				}
//			}
//			if(submission.release != null && submission.release.equals(true)) {
//				submission.type = Submission.Type.RELEASE;
//			} else {
//				submission.type = Submission.Type.CREATION;
//			}
//			
//			submission.release=null;
//			
//			// Mise a jour de l'objet submission pour mapUserRefCollab :
//			
//			if (submission.mapUserClone != null && submission.mapUserClone.size() > 0) {
//				for (Iterator<Entry<String, UserRefCollabType>> iterator = submission.mapUserClone.entrySet().iterator(); iterator.hasNext();) {
//					Entry<String, UserRefCollabType> entry = iterator.next();
//					//System.out.println("cle du userRefCollab = '" + entry.getKey() + "'");
//					//System.out.println("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
//					//System.out.println("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
//				}
//				submission.mapUserRefCollab = new HashMap<String, UserRefCollabType>();
//				submission.mapUserRefCollab.putAll(submission.mapUserClone);
//			} else {
//				//submission.mapUserRefCollab = null;
//			}
//			submission.mapUserClone = null;
//			
//			
//			
//			submissionAPI.dao_saveObject(submission);
//		}	
//		logger.error("ok pour les soumissions");
//	}
//	
//	
//	public void executeForStudy() throws Exception {
//
//		for (AbstractStudy abstractStudy : abstractStudyAPI.dao_all()) {
//			
//			logger.debug("   - abstractStudyCode = %s", abstractStudy.code);
//			
//			switch (abstractStudy.state.code) {
//				case "F-SUB" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case "V-SUB" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "N" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "FE-SUB" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "NONE" : {
//					abstractStudy.state.code = SRASubmissionStateNames.NONE;
//					break;
//				}
//				case "IP-SUB" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}		
//				case "SUB-F" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}	
//				case "SUB-V" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}				
//				case "SUB-N" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}	
//				case "SUB-FE" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "SUB-SRD-IW" : {
//					abstractStudy.state.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}	
//				default :{
//					logger.debug("Status non prevu : " + abstractStudy.state.code + " pour la soumission " + abstractStudy.code);
//					throw new SraException("Status non prevu : " + abstractStudy.state.code + " pour le study " + abstractStudy.code);
//				}
//			} 
//			abstractStudyAPI.dao_saveObject(abstractStudy);
//		} //end for study
//		logger.error("ok pour les studies");
//	} // end executeForStudy
//	
//	
//	public void executeForSample() throws Exception {
//		
//		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {
//			//printfln("   - abstractSampleCode = %s", abstractSample.code);
//			switch (abstractSample.state.code) {
//				case ("F-SUB") : {
//					//printfln("Remplacement de F-SUB par SUB-F pour %s", abstractSample.code);
//					abstractSample.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case ("N") : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case ("V-SUB") : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "FE-SUB" : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "IP-SUB" : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}
//				case ("SUB-F") : {
//					//printfln("Remplacement de F-SUB par SUB-F pour %s", abstractSample.code);
//					abstractSample.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case ("SUB-N") : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case ("SUB-V") : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "SUB-FE" : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "SUB-SRD-IW;" : {
//					abstractSample.state.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}
//				default :{
//					throw new SraException("Status non prevu : " + abstractSample.state.code + " pour le sample " + abstractSample.code);
//				}
//			}
//			
//			if(abstractSample instanceof Sample) {
//				Sample sample = sampleAPI.dao_getObject(abstractSample.code);
//				sample.state = abstractSample.state;
//				//sample.refCollab = sample.clone;
//				sample.clone = null;
//				if(StringUtils.isBlank(sample.attributes)) {
//					sample.attributes = null;
//				}
////				if(StringUtils.isBlank(sample.refCollab)) {
////					sample.refCollab = null;
////				}
//				sampleAPI.dao_saveObject(sample);
//			} else {
//				abstractSampleAPI.dao_saveObject(abstractSample);
//			}
//		}	// end for sample
//		logger.error("ok pour les samples");
//	} // end executeForSample
//
//
//	public void executeForExperiment() throws Exception {
//		
////bad		for (Experiment experiment : experimentAPI.dao_all_batch(16000)) {	
//
//// ok		List <Experiment> experimentList = MongoDBDAO.find(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class).toList();
//// ok		for (Experiment experiment : experimentList) {	
//		
//		List <String> experimentCodes = new ArrayList<String>();
//		for (Experiment experiment : experimentAPI.dao_all()) {	
//			experimentCodes.add(experiment.code);
//		}
//		
//		for (String experimentCode : experimentCodes) {
//			Experiment experiment = experimentAPI.get(experimentCode);
//			//printfln("   - experimentCode = %s", experiment.code);
//			if (experiment.state.code == null) {
//				Submission submission = submissionAPI.dao_findOne(DBQuery.in("experimentCodes", experiment.code));
//				if (submission != null) {
//					//printfln("submissionStateCode = %s", submission.state.code);
//					experiment.state = submission.state;
//				} 
//			}
//			
//			if (StringUtils.isBlank(experiment.state.code)) {
//				logger.error("Impossible de completer state pour %s", experiment.code);
//				continue;
//			}
//			if(StringUtil.isBlank(experiment.studyAccession)) {
//				AbstractStudy abstStudy = abstractStudyAPI.dao_getObject(experiment.studyCode);
//				if (abstStudy != null && StringUtil.isNotBlank(abstStudy.accession)) {
//					experiment.studyAccession = abstStudy.accession;
//				}
//			}
//			if(StringUtil.isBlank(experiment.sampleAccession)) {
//				AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
//				if (abstSample != null && StringUtil.isNotBlank(abstSample.accession)) {
//					experiment.sampleAccession = abstSample.accession;
//				}
//			}
//			switch (experiment.state.code) {
//				case "F-SUB" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case "V-SUB" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "N" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "FE-SUB" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "IP-SUB" : {
//					experiment.state.code = "SUB_SRD_IW";
//					break;
//				}
//				case "SUB-F" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case "SUB-V" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "SUB-N" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "SUB-FE" : {
//					experiment.state.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "SUB-SRD-IW" : {
//					experiment.state.code = "SUB_SRD_IW";
//					break;
//				}
//				case "SUB_SRD_IW" : {
//					experiment.state.code = "SUB_SRD_IW";
//					break;
//				}
//				default :{
//					//throw new SraException("Status non prevu : " + experiment.state.code + " pour l'experiment " + experiment.code);
//					continue;
//				}
//			} // end switch
//			
//			// Ajout pour remplir les champs locations et directory si besoin
//			for (RawData rawData : experiment.run.listRawData) {
//				if( rawData.directory == null) {
//					rawData.directory = undifinedValueInRepriseHistorique;
//				}
//				if( rawData.location == null) {
//					rawData.location = undifinedValueInRepriseHistorique;
//				}
//			
//				if (StringUtils.isNotBlank(rawData.submittedMd5)) {
//					if (rawData.submittedMd5.equals(rawData.md5)) {
//						rawData.submittedMd5 = null;
//					} else {
//						logger.debug("Probleme submittedMd5 pour " + rawData.relatifName);
//					}
//				} else {
//					rawData.submittedMd5 = null;
//				}
//				if (StringUtils.isBlank(rawData.collabFileName )) {
//					rawData.collabFileName = rawData.relatifName;
//					if (rawData.collabFileName.endsWith("fastq")) {
//						rawData.collabFileName = rawData.collabFileName.concat(".gz");
//					}
//				}
//			}
//			experimentAPI.dao_saveObject(experiment);
//			//logger.error("ok pour experiment " + experiment.code);
//			String readsetCode = experiment.readSetCode;
//			ReadSet readSets = null;
//			if (StringUtils.isNotBlank(readsetCode)){
//				//printfln("   - readsetCode = %s", readsetCode);
//				if (readsetsAPI.isObjectExist(readsetCode)) {
//					readSets = readsetsAPI.get(readsetCode);
//				} 
//			}
//			
//			if(readSets == null) { // cas des 454
//				//printfln("Pas de readset dans collection Illumina pour %s", readsetCode);
//				continue;
//			}
//			//printfln("   - illuminaCode = %s", readSets.code);
//
//			switch (readSets.submissionState.code) {	
//				case "F-SUB" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}
//				case "V-SUB" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "N" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}
//				case "FE-SUB" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "NONE" : {
//					readSets.submissionState.code = SRASubmissionStateNames.NONE;
//					break;
//				}
//				case "IP-SUB" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}
//				case "SUB-SRD-IW" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}				
//				case "SUB_SRD_IW" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_SRD_IW;
//					break;
//				}					
//				case "SUB-N" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}	
//				case "SUB-SMD-IW" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_SMD_IW;
//					break;
//				}
//				case "SUB-SMD-IP" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_SMD_IP;
//					break;
//				}
//				case "SUB_N" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_N;
//					break;
//				}	
//				case "SUB-V" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_V;
//					break;
//				}
//				case "SUB-FE" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_FE;
//					break;
//				}
//				case "SUB-F" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}	
//				case "SUB_F" : {
//					readSets.submissionState.code = SRASubmissionStateNames.SUB_F;
//					break;
//				}	
//				default :{
////					printfln("Status non prevu : " + readSets.submissionState.code + " pour le readset " + readSets.code);
////					continue;
//					//throw new SraException("Status non prevu : " + readSets.submissionState.code + " pour le readset " + readSets.code);
//					continue;
//				}
//			}	// end switch
//			
//			readsetsAPI.dao_update(DBQuery.is("code", readSets.code), 
//					DBUpdate.set("submissionState", readSets.submissionState));
//			//logger.error("ok pour update readset " + readSets.code);
//			} // end for experiment
//			logger.error("ok pour experiments et readsets");
//		} // end executeForExperiment
//		
//		
//	} //end class

		

		
		
		
	

