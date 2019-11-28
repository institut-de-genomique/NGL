package workflows.sra.submission;
//package workflows.sra.submission;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.FileAlreadyExistsException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//// import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import org.apache.commons.lang3.StringUtils;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//// import org.springframework.beans.factory.annotation.Autowired;
//// import org.springframework.stereotype.Service;
//
//import com.google.inject.Provider;
//
//import fr.cea.ig.MongoDBDAO;
//// import models.laboratory.common.instance.State;
//import models.laboratory.project.instance.Project;
//import models.laboratory.run.instance.ReadSet;
//import models.sra.submit.common.instance.AbstractStudy;
//import models.sra.submit.common.instance.ExternalStudy;
//import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Study;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.sra.instance.RawData;
//import models.sra.submit.util.SraException;
//import models.sra.submit.util.VariableSRA;
//import models.utils.InstanceConstants;
//import play.Logger;
//import validation.ContextValidation;
//// import workflows.readset.ReadSetWorkflows;
//import workflows.sra.experiment.ExperimentWorkflows;
//import workflows.sra.sample.SampleWorkflows;
//import workflows.sra.study.StudyWorkflows;
//
//
//
//
//
//// @Service
//@Singleton
//public class SubmissionWorkflowsHelper {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionWorkflowsHelper.class);
//
//	/*@Autowired
//	StudyWorkflows studyWorkflows;
//	@Autowired
//	SampleWorkflows sampleWorkflows;
//	@Autowired
//	ExperimentWorkflows experimentWorkflows;
//	@Autowired
//	ReadSetWorkflows readSetWorkflows;*/
//
//	private final Provider<StudyWorkflows>      studyWorkflows;
//	private final SampleWorkflows     sampleWorkflows;
//	private final ExperimentWorkflows experimentWorkflows;
//	// private final ReadSetWorkflows    readSetWorkflows;
//	
//	@Inject
//	public SubmissionWorkflowsHelper(Provider<StudyWorkflows>      studyWorkflows, 
//					                 SampleWorkflows     sampleWorkflows,
//			                         ExperimentWorkflows experimentWorkflows) {
//		this.studyWorkflows      = studyWorkflows;
//		this.sampleWorkflows     = sampleWorkflows;
//		this.experimentWorkflows = experimentWorkflows;
//		// this.readSetWorkflows    = readSetWorkflows;
//	}
//
//	public void updateSubmissionRelease(Submission submission) {	
//		Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, submission.studyCode);
//		Calendar calendar = Calendar.getInstance();
//		Date date  = calendar.getTime();		
//		Date release_date  = calendar.getTime();
//		Logger.debug("update submission date study "+study.code+" with date "+release_date);
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
//				DBQuery.is("code", submission.code),
//				DBUpdate.set("submissionDate", date).set("xmlSubmission", "submission.xml"));	
//
//		MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//				DBQuery.is("accession", study.accession),
//				DBUpdate.set("releaseDate", release_date));
//
//		//Update release date on experiment and ReadSet
//		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("studyCode", study.code)).toList();
//		for(Experiment experiment : experiments){
//			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
//					DBQuery.is("code", experiment.code),
//					DBUpdate.set("releaseDate", release_date));
//			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//					DBQuery.is("code", experiment.readSetCode),
//					DBUpdate.set("releaseDate", release_date));
//		}
//	}
//
//	public void updateSubmissionForDates(Submission submission) {
//		Calendar calendar = Calendar.getInstance();
//		Date date  = calendar.getTime();		
//		calendar.add(Calendar.YEAR, 2);
//		Date release_date  = calendar.getTime();
//		
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
//				DBQuery.is("code", submission.code),
//				DBUpdate.set("submissionDate", date));	
//		
//		if (submission.studyCode != null) {
//			MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//				DBQuery.is("code", submission.studyCode),
//				DBUpdate.set("firstSubmissionDate", date).set("releaseDate", release_date));
//		}
//		for(String sampleCode : submission.sampleCodes){
//			MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, 
//					DBQuery.is("code", sampleCode),
//					DBUpdate.set("firstSubmissionDate", date));
//		}
//		for(String experimentCode : submission.experimentCodes){
//			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
//			Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, experiment.studyCode);			
//			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
//					DBQuery.is("code", experimentCode),
//					DBUpdate.set("releaseDate", study.releaseDate).set("firstSubmissionDate", study.firstSubmissionDate));
//			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//					DBQuery.is("code", experiment.readSetCode),
//					DBUpdate.set("releaseDate", study.releaseDate).set("submissionDate", study.firstSubmissionDate));
//		}
//	}
//
//	public void createDirSubmission(Submission submission,ContextValidation validation){
//		// Determiner le repertoire de soumission:
//
//		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 
//		if (submission.release) {
//			submission.submissionDirectory = submission.submissionDirectory + "_release"; 
//		}
//		File dataRep = new File(submission.submissionDirectory);
//		Logger.debug("Creation du repertoire de soumission : " + submission.submissionDirectory);
//		Logger.of("SRA").info("Creation du repertoire de soumission" + submission.submissionDirectory);
//		if (dataRep.exists()){
//			validation.addErrors("submission", "error.directory.release.exist",dataRep,submission.code);
//		} else {
//			if(!dataRep.mkdirs()){	
//				validation.addErrors("submission", "error.directory.release.create",dataRep,submission.code);
//			}
//		}
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME,  Submission.class, 
//				DBQuery.is("code", submission.code),
//				DBUpdate.set("submissionDirectory", submission.submissionDirectory));
//	}
//
//	public void rollbackSubmission(Submission submission,ContextValidation validation){
//		// Si la soumission est connu de l'EBI on ne pourra pas l'enlever de la base :
//		if (StringUtils.isNotBlank(submission.accession)){
//			Logger.debug("objet submission avec AC : submissionCode = "+ submission.code + " et submissionAC = "+ submission.accession);
//			return;
//		} 
//		// Si la soumission concerne une release avec status "N-R" ou IW-SUB-R:
//		if (submission.release && (submission.state.code.equalsIgnoreCase("N-R")||(submission.state.code.equalsIgnoreCase("IW-SUB-R")))) {
//			// detruire la soumission :
//			MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submission.code);
//			// remettre le status du study avec un status F-SUB
//			// La date de release du study est modifié seulement si retour positif de l'EBI pour release donc si status F-SUB
//			MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class,
//					DBQuery.is("code", submission.studyCode),
//					DBUpdate.set("state.code", "F-SUB").set("traceInformation.modifyUser", validation.getUser()).set("traceInformation.modifyDate", new Date()));
//			return;
//		} 
//
//		if (! submission.experimentCodes.isEmpty()) {
//			for (String experimentCode : submission.experimentCodes) {
//				// verifier que l'experiment n'est pas utilisé par autre objet submission avant destruction
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
//				// mettre le status pour la soumission des readSet à NONE si possible: 
//				if (experiment != null){
//					// remettre les readSet dans la base avec submissionState à "NONE":
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
//							DBQuery.is("code", experiment.readSetCode),
//							DBUpdate.set("submissionState.code", "NONE").set("traceInformation.modifyUser", validation.getUser()).set("traceInformation.modifyDate", new Date()));
//					//System.out.println("submissionState.code remis à 'N' pour le readSet "+experiment.readSetCode);
//
//					List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("experimentCodes", experimentCode)).toList();
//					if (submissionList.size() > 1) {
//						for (Submission sub: submissionList) {
//							System.out.println(experimentCode + " utilise par objet Submission " + sub.code);
//						}
//					} else {
//						// todo : verifier qu'on ne detruit que des experiments en new ou uservalidate
//						if ("N".equals(experiment.state.code) ||"V-SUB".equals(experiment.state.code)){
//							MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
//						} else {
//							Logger.debug(experimentCode + " non delété dans base car status = " + experiment.state.code);
//						}
//					}
//				}
//			}
//		}
//
//		if (! submission.refSampleCodes.isEmpty()) {	
//			for (String sampleCode : submission.refSampleCodes){
//				// verifier que sample n'est pas utilisé par autre objet submission avant destruction
//				// normalement sample crees dans init de type external avec state=F-SUB ou sample avec state='N'
//				List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("refSampleCodes", sampleCode)).toList();
//				if (submissionList.size() > 1) {
//					for (Submission sub: submissionList) {
//						Logger.debug(sampleCode + " utilise par objet Submission " + sub.code);
//					}
//				} else {
//					MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, sampleCode);		
//					Logger.debug("deletion dans base pour sample "+sampleCode);
//				}
//			}
//		}
//
//		// verifier que la config à l'etat used n'est pas utilisé par une autre soumission avant de remettre son etat à 'N'
//		// update la configuration pour le statut en remettant le statut new si pas utilisé par ailleurs :
//		List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("configCode", submission.configCode)).toList();
//		if (submissionList.size() <= 1) {
//			MongoDBDAO.update(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, 
//					DBQuery.is("code", submission.configCode),
//					DBUpdate.set("state.code", "N").set("traceInformation.modifyUser", validation.getUser()).set("traceInformation.modifyDate", new Date()));	
//			Logger.debug("state.code remis à 'N' pour configuration "+submission.configCode);
//		}
//
//		// verifier que le study à l'etat userValidate n'est pas utilisé par une autre soumission avant de remettre son etat à 'N'
//		if (StringUtils.isNotBlank(submission.studyCode)){
//			List <Submission> submissionList2 = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("studyCode", submission.studyCode)).toList();
//			if (submissionList2.size() == 1) {
//				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//						DBQuery.is("code", submission.studyCode),
//						DBUpdate.set("state.code", "N").set("traceInformation.modifyUser", validation.getUser()).set("traceInformation.modifyDate", new Date()));	
//				Logger.debug("state.code remis à 'N' pour study "+submission.studyCode);
//			}	
//		}
//
//		if (! submission.refStudyCodes.isEmpty()) {	
//			// On ne peut detruire que des ExternalStudy crées et utilisés seulement par la soumission courante.
//			for (String studyCode : submission.refStudyCodes){
//				// verifier que study n'est pas utilisé par autre objet submission avant destruction
//				// normalement study crees dans init de type external avec state=F-SUB ou study avec state='N'
//				List <Submission> submissionList2 = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("refStudyCodes", studyCode)).toList();
//				if (submissionList2.size() > 1) {
//					for (Submission sub: submissionList2) {
//						Logger.debug(studyCode + " utilise par objet Submission " + sub.code);
//					}
//				} else {
//					AbstractStudy study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.AbstractStudy.class, studyCode);
//					if (study != null){
//						if ( ExternalStudy.class.isInstance(study)) {
//							// on ne veut enlever que les external_study cree par cette soumission, si internalStudy cree, on veut juste le remettre avec bon state.
//							//System.out.println("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
//							if("F-SUB".equalsIgnoreCase(study.state.code) ){
//								MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, studyCode);		
//								Logger.debug("deletion dans base pour study "+studyCode);
//							}
//						}
//					}
//				}
//			}
//		}
//
//		Logger.debug("deletion dans base pour submission "+submission.code);
//		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submission.code);
//	}
//
//	public void updateSubmissionChildObject(Submission submission, ContextValidation validation) {
//		Logger.debug("dans applySuccessPostStateRules submission=" + submission.code + " avec state.code='"+submission.state.code+"'");
//
//		if (StringUtils.isNotBlank(submission.studyCode)) {
//			Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, submission.studyCode);
//			// Recuperer object study pour mettre historique des state traceInformation à jour:
//			studyWorkflows.get().setState(validation, study, submission.state);
//			Logger.debug("mise à jour du study avec state.code=" + study.state.code);
//		}
//		//Normalement une soumission pour release doit concerner uniquement un study, donc pas de test pour status F-SUB-R		
//		if (submission.sampleCodes != null) {
//			for (int i = 0; i < submission.sampleCodes.size() ; i++) {
//				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, submission.sampleCodes.get(i));
//				sampleWorkflows.setState(validation, sample, submission.state);
//			}
//		}
//
//		if (submission.experimentCodes != null) {
//			for (int i = 0; i < submission.experimentCodes.size() ; i++) {
//
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, submission.experimentCodes.get(i));
//				experimentWorkflows.setState(validation, experiment, submission.state);
//
//				// Update objet readSet :
//				ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, experiment.readSetCode);
//				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
//						DBQuery.is("code", readset.code),
//						DBUpdate.set("submissionState.code", submission.state.code).set("traceInformation.modifyUser", validation.getUser()).set("traceInformation.modifyDate", new Date()));
//			}
//		}
//	}
//
//	public File createDirSubmission(Submission submission) throws SraException{
//		// Determiner le repertoire de soumission:
//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
//		Date courantDate = new java.util.Date();
//		String st_my_date = dateFormat.format(courantDate);
//
//		String syntProjectCode = submission.code;
//		/*
//		for (String projectCode: submission.projectCodes) {
//			if (StringUtils.isNotBlank(projectCode)) {
//				syntProjectCode += "_" + projectCode;
//			}
//		}
//		if (StringUtils.isNotBlank(syntProjectCode)){
//			syntProjectCode = syntProjectCode.replaceFirst("_", "");
//		}
//		*/			
//		//submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + syntProjectCode + File.separator + st_my_date;
//		//submission.submissionTmpDirectory = VariableSRA.submissionRootDirectory + File.separator + syntProjectCode + File.separator + "tmp_" + st_my_date;
//		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 
//		if (submission.release) {
//			submission.submissionDirectory = submission.submissionDirectory + "_release"; 
//		}
//		File dataRep = new File(submission.submissionDirectory);
//		System.out.println("Creation du repertoire de soumission : " + submission.submissionDirectory);
//		Logger.of("SRA").info("Creation du repertoire de soumission" + submission.submissionDirectory);
//		if (dataRep.exists()){
//			throw new SraException("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
//		} else {
//			if(!dataRep.mkdirs()){	
//				throw new SraException("Impossible de creer le repertoire " + dataRep + " ");
//			}
//		}
//		return (dataRep);
//	}	
//
//	public void activationPrimarySubmission(ContextValidation contextValidation, Submission submission) {
//		checkForActivatePrimarySubmission(contextValidation, submission);
//		if (!contextValidation.hasErrors()) {
//			activatePrimarySubmission(contextValidation, submission);
//		} 
//	}
//
//	public void activatePrimarySubmission(ContextValidation contextValidation, Submission submission) {
//		System.out.println("Dans SubmissionWorkflowsHelper.activatePrimarySubmission");
//		// creer repertoire de soumission sur disque et faire liens sur données brutes
//		try {
//			// creation repertoire de soumission :
//			File dataRep = createDirSubmission(submission);
//			// creation liens donnees brutes vers repertoire de soumission
//			for (String experimentCode: submission.experimentCodes) {
//				Experiment expElt =  MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
//
//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, expElt.readSetCode);
//				Project p = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
//				System.out.println("exp = "+ expElt.code);
//				contextValidation.addKeyToRootKeyName("experiment");
//				contextValidation.addKeyToRootKeyName("run");
//				contextValidation.addKeyToRootKeyName("rawData");
//
//				for (RawData rawData :expElt.run.listRawData) {
//					// Partie de code deportée dans activate : on met la variable location à CNS
//					// si les données sont physiquement au CNS meme si elles sont aussi au CCRT
//					// et on change le chemin pour remplacer /ccc/genostore/.../rawdata par /env/cns/proj/ 
//					String cns_directory = rawData.directory;
//					if(rawData.directory.startsWith("/ccc/genostore")){
//						int index = rawData.directory.indexOf("/rawdata/");
//						String lotseq_dir = rawData.directory.substring(index + 9);
//						cns_directory="/env/cns/proj/"+lotseq_dir;
//					}
//
//					File fileCible = new File(cns_directory + File.separator + rawData.relatifName);
//					// verification dans check que fileCible est bien soit au CNS soit au CCRT.
//					if(fileCible.exists()){
//						System.out.println("le fichier "+ fileCible +"existe bien");
//						rawData.location = "CNS";
//						rawData.directory = cns_directory;
//					} else {				
//						if ("CCRT".equalsIgnoreCase(readSet.location)) {
//							rawData.location = readSet.location;
///*
//					} else {
//						System.out.println("le fichier "+ fileCible +"n'existe pas au CNS");
//						if ("CNS".equalsIgnoreCase(readSet.location)) {
//							if (p.archive){
//								throw new SraException(rawData.relatifName + " n'existe pas sur les disques CNS, et Projet " + p.code + " avec archive=true, et readSet= " + readSet.code + " avec location = CNS");
//							} else {
//								throw new SraException(rawData.relatifName + " n'existe pas sur les disques CNS, et Projet " + p.code + " avec archive=false, et readSet " + readSet.code  + " localisée au CNS");
//							}
//						} else if ("CCRT".equalsIgnoreCase(readSet.location)) {
//							rawData.location = readSet.location;
//						} else {
//							throw new SraException(rawData.relatifName + " avec location inconnue => " + readSet.location);
//*/
//						}
//					}
//					if (rawData.extention.equalsIgnoreCase("fastq")) {
//						rawData.gzipForSubmission = true;
//					} else {
//						rawData.gzipForSubmission = false;
//						// verification dans check que md5 existe bien dans ce cas la.
///*
//						if (StringUtils.isBlank(rawData.md5)){
//							contextValidation.addErrors("md5", " valeur à null alors que donnée deja zippée pour "+ rawData.relatifName);
//						}
//*/
//					}
//					// On ne cree les liens dans repertoire de soumission vers rep des projets que si la 
//					// donnée est au CNS et si elle n'est pas à zipper
//					if ("CNS".equalsIgnoreCase(rawData.location) && ! rawData.gzipForSubmission) {
//						System.out.println("run = "+ expElt.run.code);
//						File fileLien = new File(submission.submissionDirectory + File.separator + rawData.relatifName);
//						if(fileLien.exists()){
//							fileLien.delete();
//						}
//						System.out.println("fileCible = " + fileCible);
//						System.out.println("fileLien = " + fileLien);
//
//						Path lien = Paths.get(fileLien.getPath());
//						Path cible = Paths.get(fileCible.getPath());
//						Files.createSymbolicLink(lien, cible);
//						System.out.println("Lien symbolique avec :  lien= "+lien+" et  cible="+cible);
//						//String cmd = "ln -s -f " + rawData.directory + File.separator + rawData.relatifName
//						//+ " " + submission.submissionDirectory + File.separator + rawData.relatifName;
//						//System.out.println("cmd = " + cmd);
//					} else {
//						System.out.println("Donnée "+ rawData.relatifName + " localisée au " + rawData.location);
//					}
//					contextValidation.removeKeyFromRootKeyName("rawData");
//					contextValidation.removeKeyFromRootKeyName("run");
//					contextValidation.removeKeyFromRootKeyName("experiment");
//				}
//
//				// sauver dans base la liste des rawData avec bonne location et bon directory:
//				MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME,  Experiment.class, 
//						DBQuery.is("code", experimentCode),
//						DBUpdate.set("run.listRawData", expElt.run.listRawData));
//			}
//
//
//		} catch (SraException e) {
//			contextValidation.addErrors("submission", e.getMessage());
//		} catch (SecurityException e) {
//			contextValidation.addErrors("Dans activatePrimarySubmission, pb SecurityException: ", e.getMessage());
//		} catch (UnsupportedOperationException e) {
//			contextValidation.addErrors(" Dans activatePrimarySubmission, pb UnsupportedOperationException: ", e.getMessage());
//		} catch (FileAlreadyExistsException e) {
//			contextValidation.addErrors(" Dans activatePrimarySubmission, pb FileAlreadyExistsException: ", e.getMessage());
//		} catch (IOException e) {
//			contextValidation.addErrors(" Dans activatePrimarySubmission, pb IOException: ", e.getMessage());		
//		}
//
//		if (! contextValidation.hasErrors()) {
//			// updater la soumission dans la base pour le repertoire de soumission (la date de soumission sera mise à la reception des AC)
//			MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME,  Submission.class, 
//					DBQuery.is("code", submission.code),
//					DBUpdate.set("submissionDirectory", submission.submissionDirectory));
//		} 
//
//
//	}
//
//	public void checkForActivatePrimarySubmission(ContextValidation contextValidation, Submission submission) {
//		System.out.println("Dans SubmissionWorkflowsHelper.checkForActivatePrimarySubmission");
//		// verifications liens donnees brutes vers repertoire de soumission
//		for (String experimentCode: submission.experimentCodes) {
//			Experiment expElt =  MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
//
//			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, expElt.readSetCode);
//			Project p = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
//
//			System.out.println("exp = "+ expElt.code);
//			contextValidation.addKeyToRootKeyName("experiment");
//			contextValidation.addKeyToRootKeyName("run");
//			contextValidation.addKeyToRootKeyName("rawData");
//
//			for (RawData rawData :expElt.run.listRawData) {
//				// Partie de code deportée dans activate : on met la variable location à CNS
//				// si les données sont physiquement au CNS meme si elles sont aussi au CCRT
//				// et on change le chemin pour remplacer /ccc/genostore/.../rawdata par /env/cns/proj/ 
//				String cns_directory = rawData.directory;
//				if(rawData.directory.startsWith("/ccc/genostore")){
//					int index = rawData.directory.indexOf("/rawdata/");
//					String lotseq_dir = rawData.directory.substring(index + 9);
//					cns_directory="/env/cns/proj/"+lotseq_dir;
//				}
//
//				File fileCible = new File(cns_directory + File.separator + rawData.relatifName);
//				if(fileCible.exists()){
//					System.out.println("le fichier "+ fileCible +"existe bien");
//					rawData.location = "CNS";
//					rawData.directory = cns_directory;
//				} else {
//					System.out.println("le fichier "+ fileCible +"n'existe pas au CNS");
//					if ("CNS".equalsIgnoreCase(readSet.location)) {
//						if (p.archive){
//							contextValidation.addErrors("rawData", "error.rawData.activate.CNS.archive", rawData.relatifName, p.code, readSet.code);
//						} else {
//							contextValidation.addErrors("rawData", "error.rawData.activate.CNS.notArchive", rawData.relatifName, p.code, readSet.code);
//						}
//					} else if ("CCRT".equalsIgnoreCase(readSet.location)) {
//						rawData.location = readSet.location;
//					} else {
//						contextValidation.addErrors("rawData", "error.rawData.activate.location", readSet.location, rawData.relatifName);
//					}
//				}
//				if (rawData.extention.equalsIgnoreCase("fastq")) {
//					rawData.gzipForSubmission = true;
//				} else {
//					rawData.gzipForSubmission = false;
//					if (StringUtils.isBlank(rawData.md5)){
//						contextValidation.addErrors("rawData", "error.rawData.activate.md5"+ rawData.relatifName);
//					}
//				}
//				// On ne cree les liens dans repertoire de soumission vers rep des projets que si la 
//				// donnée est au CNS et si elle n'est pas à zipper
//				if ("CNS".equalsIgnoreCase(rawData.location) && ! rawData.gzipForSubmission) {
//					System.out.println("run = "+ expElt.run.code);
//					File fileLien = new File(submission.submissionDirectory + File.separator + rawData.relatifName);
//					if(fileLien.exists()){
//						fileLien.delete();
//					}
//					System.out.println("fileCible = " + fileCible);
//					System.out.println("fileLien = " + fileLien);
//				} else {
//					System.out.println("Donnée "+ rawData.relatifName + " localisée au " + rawData.location);
//				}
//				contextValidation.removeKeyFromRootKeyName("rawData");
//				contextValidation.removeKeyFromRootKeyName("run");
//				contextValidation.removeKeyFromRootKeyName("experiment");
//			}
//
//		}		
//
//	}
//
//}