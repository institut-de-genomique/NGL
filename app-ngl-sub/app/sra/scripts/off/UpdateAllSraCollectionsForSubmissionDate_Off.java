package sra.scripts.off;
//package sra.scripts;
//
//import java.util.Calendar;
//import java.util.Date;
//
//import javax.inject.Inject;
//import org.apache.commons.lang3.StringUtils;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//
//import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
//import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
//import models.laboratory.run.instance.ReadSet;
//import models.sra.submit.common.instance.Readset;
//import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Study;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Experiment;
//import services.EbiAPI;
//
//
///*
// * Met a jour les collections submission, study, sample, experiment pour date premiere soumission et date release
// * à partir de la date de soumission de la collection submission
// * Exemple de lancement :
// * http://localhost:9000/sra/scripts/run/sra.scripts.UpdateAllSraCollectionsForSubmissionDate
//
// * @author sgas
// *
// */
//public class UpdateAllSraCollectionsForSubmissionDate extends ScriptNoArgs {
//	
//	private final SubmissionAPI  		submissionAPI;
//	private final StudyAPI       		studyAPI;
//	private final SampleAPI      		sampleAPI;
//	private final ExperimentAPI  		experimentAPI;
//	private final ReadSetsDAO    		laboReadSetDAO;
//	private final ReadsetAPI    		readsetAPI;
//
//	private final String 				ngsrgUser   		= "ngsrg";    
//
//
//	
//	
//
//	@Inject
//	public UpdateAllSraCollectionsForSubmissionDate (
//					SubmissionAPI submissionAPI,
//					StudyAPI              studyAPI,
//					AbstractStudyAPI      abstractStudyAPI,
//				    SampleAPI             sampleAPI,
//				    AbstractSampleAPI     abstractSampleAPI,
//				    ExperimentAPI         experimentAPI,
//				    ReadSetsDAO           laboReadSetDAO,
//				    ReadsetAPI            readsetAPI,
//				    EbiAPI                ebiAPI) {
//		this.submissionAPI  = submissionAPI;
//		this.studyAPI       = studyAPI;
//		this.sampleAPI      = sampleAPI;
//		this.experimentAPI  = experimentAPI;
//		this.laboReadSetDAO = laboReadSetDAO;
//		this.readsetAPI = readsetAPI;
//
//	}
//	
//
//
//	public void updateDataForSubmissionDate() {
//	Calendar calendar = Calendar.getInstance();
//	java.util.Date dateJour = calendar.getTime();
//	int count_submissionSave = 0;
//	int count_studySave = 0;
//	int count_sampleSave = 0;
//	int count_experimentReadsetSave = 0;
//	for (Submission submission : submissionAPI.dao_all()) {
//		//if (submission.ok != null && cp < 20 ) {
//			//println("mise à jour pour les objets lié a la soumission " + submission.code + " nos=" + cp);
//			
//			// mise a jour submission pour traceInformation 
//			if(StringUtils.isNotBlank(submission.adminComment)) {
//				if (submission.submissionDate != null) {
//					submission.traceInformation.creationDate = submission.submissionDate;
//				}
//				submission.traceInformation.createUser = ngsrgUser;
//				submission.traceInformation.modifyDate = null;
//				submission.state.historical = null;
//			} else {
//				if(StringUtils.isNotBlank(submission.traceInformation.createUser) 
//						&& submission.traceInformation.createUser.equals("william")) {
//					submission.traceInformation.createUser= ngsrgUser;
//				}
//				if(StringUtils.isNotBlank(submission.traceInformation.modifyUser) 
//						&& submission.traceInformation.modifyUser.equals("william")) {
//					submission.traceInformation.modifyUser= ngsrgUser;
//				}
//			}
//			
//			submission.creationUser = null;
//			submission.creationDate = null;
//
//			submissionAPI.dao_saveObject(submission);
//			count_submissionSave++;
//			// Mise a jour study lié à submission
//			if (StringUtils.isNotBlank(submission.studyCode)) {
//				count_studySave = count_studySave + updateStudyForSubmissionDate(submission.studyCode, submission.submissionDate, dateJour);
//			}
//			
//			for (String sampleCode: submission.sampleCodes) {
//				count_sampleSave = count_sampleSave + updateSampleForSubmissionDate(sampleCode, submission.submissionDate);
//			}
//				
//			for (String experimentCode: submission.experimentCodes) {
//				count_experimentReadsetSave = count_experimentReadsetSave + updateExperimentAndReadsetForSubmissionDate(experimentCode, submission.submissionDate);
//			}
//		}
//	
//		println ("Nombre de soumission mise jour = " + count_submissionSave); 
//		println ("Nombre de study mis jour = " + count_studySave); 
//		println ("Nombre de sample mis jour = " + count_sampleSave); 
//		println ("Nombre d experiment et readset mis jour = " + count_experimentReadsetSave); 
//	}
//	
//	
//	public int updateStudyForSubmissionDate(String studyCode, Date submissionDate, Date dateJour) {
//		Study study = studyAPI.get(studyCode);
//		if (study == null) {
//			return 0;
//		}
//		if (submissionDate != null) {
//			study.firstSubmissionDate = submissionDate;
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTime(submissionDate);
//			calendar.add(Calendar.YEAR, 2);
//			java.util.Date releaseDateCalculee = calendar.getTime(); 
//			if( study.releaseDate == null) {
//				study.releaseDate  = releaseDateCalculee;
//			} else {
//				// si donnée deja publique, ne pas ecraser avec releaseDateCalculée
//				if(study.releaseDate.before(dateJour)) {
//					//println("studyAc="+ study.accession +", firstSubmissionDate="+ study.firstSubmissionDate + ", study.releaseDate=" + study.releaseDate + ", releaseCalcul=" + calendar.getTime());
//				}
//				else {
//					study.releaseDate  = releaseDateCalculee;	
//				}
//			}
//		}
//		if(StringUtils.isNotBlank(study.adminComment)) {
//			if (submissionDate != null) {
//				study.traceInformation.creationDate = submissionDate;
//			}
//			study.traceInformation.createUser = ngsrgUser;
//			study.traceInformation.modifyDate = null;
//			study.traceInformation.modifyDate = null;
//			study.state.historical = null;
//		} else {
//			if(StringUtils.isNotBlank(study.traceInformation.createUser) 
//					&& study.traceInformation.createUser.equals("william")) {
//				study.traceInformation.createUser= ngsrgUser;
//			}
//			if(StringUtils.isNotBlank(study.traceInformation.modifyUser) 
//					&& study.traceInformation.modifyUser.equals("william")) {
//				study.traceInformation.modifyUser= ngsrgUser;
//			}
//		}
//		studyAPI.dao_saveObject(study);
//		return 1;
//	}
//	
//	
//	public int updateSampleForSubmissionDate(String sampleCode, Date submissionDate) {
//
//		Sample sample = sampleAPI.get(sampleCode);
//		if (sample == null) {
//			return 0;
//		}
//		if (submissionDate != null) {
//			sample.firstSubmissionDate = submissionDate;
//		}
//		if (StringUtils.isNotBlank(sample.adminComment)) {
//			if (submissionDate != null) {
//				sample.traceInformation.creationDate = submissionDate;
//			}
//			sample.traceInformation.createUser = ngsrgUser;
//			sample.traceInformation.modifyDate = null;
//			sample.traceInformation.modifyDate = null;
//			sample.state.historical = null;
//		} else {
//			if(StringUtils.isNotBlank(sample.traceInformation.createUser) 
//					&& sample.traceInformation.createUser.equals("william")) {
//				sample.traceInformation.createUser= ngsrgUser;
//			}
//			if(StringUtils.isNotBlank(sample.traceInformation.modifyUser) 
//					&& sample.traceInformation.modifyUser.equals("william")) {
//				sample.traceInformation.modifyUser= ngsrgUser;
//			}
//		}
//		sampleAPI.dao_saveObject(sample);
//		return 1;
//	}
//
//	
//	public int updateExperimentAndReadsetForSubmissionDate(String experimentCode, Date submissionDate) {
//		Experiment experiment = experimentAPI.get(experimentCode);
//		if (experiment == null) {
//			return 0;
//		}
//		if (submissionDate != null ) {
//			experiment.firstSubmissionDate = submissionDate;
//		}
//		if (StringUtils.isNotBlank(experiment.adminComment)) {
//			if (submissionDate != null ) {
//				experiment.traceInformation.creationDate = submissionDate;
//			}
//			experiment.traceInformation.createUser = ngsrgUser;
//			experiment.traceInformation.modifyDate = null;
//			experiment.traceInformation.modifyDate = null;
//			experiment.state.historical = null;
//		} else {
//			if(StringUtils.isNotBlank(experiment.traceInformation.createUser) 
//					&& experiment.traceInformation.createUser.equals("william")) {
//				experiment.traceInformation.createUser= ngsrgUser;
//			}
//			if(StringUtils.isNotBlank(experiment.traceInformation.modifyUser) 
//					&& experiment.traceInformation.modifyUser.equals("william")) {
//				experiment.traceInformation.modifyUser= ngsrgUser;
//			}
//		}
//		experimentAPI.dao_saveObject(experiment);
//
//		// Pour les 454, le readsetCode n'existe pas forcement dans la collection, ne pas declencher erreur
//		// mettre a jour la collection ngl-bi.ReadSetIllumina pour submissionUser si besoin
//		ReadSet laboreadset = laboReadSetDAO.findByCode(experiment.readSetCode);
//
//		if (laboreadset != null) {
//			if (submissionDate != null) {
//				laboreadset.submissionState.date = submissionDate;
//				laboreadset.submissionDate = submissionDate;
//			}
//			if (StringUtils.isNotBlank(experiment.adminComment)) {
//				laboreadset.submissionState.historical = null;
//			}
//			if (StringUtils.isNotBlank(laboreadset.submissionState.user) 
//					&& laboreadset.submissionState.user.equals("william")) {
//				laboreadset.submissionState.user = ngsrgUser;
//			}
//			if(submissionDate != null ) {
//				laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
//						DBUpdate.set("submissionState", laboreadset.submissionState)
//						.set("submissionDate", submissionDate));
//			} else {
//				laboReadSetDAO.update(DBQuery.is("code", laboreadset.code),					
//						DBUpdate.set("submissionState", laboreadset.submissionState));
//			}
//		}	
//
//		// mettre à jour si besoin la collection sra.readset :
//		if( ! readsetAPI.isObjectExist(experiment.readSetCode)) {
//			// creation du readset (collection readset de ngl-sub)
//			Readset readset = new Readset();
//			readset.code = experiment.readSetCode;
//			readset.experimentCode = experiment.code;
//			readset.type = experiment.typePlatform.toLowerCase();
//			readset.runCode = experiment.run.code;
//			readsetAPI.dao_saveObject(readset);
//		}
//		return 1;
//	}
//
//	
//	@Override
//	public void execute() throws Exception {
//		updateDataForSubmissionDate();
//		println("fin du traitement");
//	}
//
//}
