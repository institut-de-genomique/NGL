package services;

import fr.cea.ig.MongoDBDAO;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

public class DbUtil {
	
	// Check des differents codes dans les collections de la base via mongoDb

/*
	// Verifie que le codeReadSet existe dans la collection Experiment
	public static Boolean checkCodeReadSetExistInExperimentCollection(String codeReadSet){
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "readSetCode", codeReadSet);
	}

	// Verifie que le codeRun existe dans la collection Experiment
	public static Boolean checkCodeRunExistInExperimentCollection(String codeRun){
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "runCode", codeRun);
	}	
	
	// Verifie que le codeExperiment existe bien dans la collection Experiment
	public static Boolean checkCodeExperimentExistInExperimentCollection(String codeExperiment) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", codeExperiment);
	}
	
	
	
	// Verifie que le codeSample existe bien dans la collection Sample
	public static Boolean checkCodeSampleExistInSampleCollection(String codeSample) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, "code", codeSample);
	}	
	
	// Verifie que le codeStudy existe bien dans la collection Study
	public static Boolean checkCodeStudyExistInStudyCollection(String codeStudy) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, "code", codeStudy);
	}
	// Verifie que le codeSubmission existe bien dans la collection Submission
	public static Boolean checkCodeSubmissionExistInSubmissionCollection(String codeSubmission) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "code", codeSubmission);
	}
	// Verifie que le codeStudy existe bien dans la collection Submission
	public static Boolean checkCodeProjectExistInSubmissionCollection(String codeProject) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "projectCode", codeProject);
	}	

*/
	
	// methode permettant de sauver dans la base les differents types d'objets
	// attention declenchent une erreur si on tente de sauver des objets avec meme code
	public void save(Study study) throws SraException {
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, "code", study.code)){
			throw new SraException("le study "+ study.code + " existe deja dans la collection Study de la base");
		} 
		MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
	}	
	
	public void save(Sample sample) throws SraException {
		//if (services.SraDbServices.checkCodeSampleExistInSampleCollection(sample.code)) {
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, "code", sample.code)) {			
			throw new SraException("le sample "+ sample.code + " existe deja dans la collection Sample de la base");
		} 
		MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample);
	}	
	
	public void save(Submission submission) throws SraException {
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "code", submission.code)) {
			throw new SraException("le submissionCode "+ submission.code + " existe deja dans la collection Experiment de la base");
		}
		MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);
	}	
	
	public static String checkExperimentExist(Experiment experiment) throws SraException {
		String mess = "";
		if	(MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", experiment.code)) {
			//throw new SraException("l'experiment "+ experiment.code + " existe deja dans la collection Experiment de la base");
			mess += "l'experiment "+ experiment.code + " existe deja dans la collection Experiment de la base";
		}
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "runCode", experiment.run.code)){
			//throw new SraException("le run "+ experiment.run.code + " existe deja dans la collection Experiment de la base");
			mess += "le run "+ experiment.run.code + " existe deja dans la collection Experiment de la base";
		}
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "readSetCode", experiment.readSetCode)) {
			//throw new SraException("le readSetcode "+ experiment.run.code + " existe deja dans la collection Experiment de la base");
			mess += "le readSetcode "+ experiment.run.code + " existe deja dans la collection Experiment de la base";
		}	
		//retourne l'ensemble des erreurs dans une string ou null
		if (mess.equals("")){
			return null;
		}
		return mess;
	}
	
	public void save(Experiment experiment) throws SraException{	
		checkExperimentExist(experiment);
		MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
	}		
		
	
/*	public void cleanDataBase(String submissionCode) {
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		if (submission==null){
			return;
		}
		// On verifie que la donnée n'est pas connu de l'EBI avant de detruire
		if (submission.accession == null || submission.accession.equals("")) {
			if ( submission.studyCode != null) {
				// verifier que study n'est pas utilisé par autre objet submission avant destruction
				List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("studyCode", submission.studyCode)).toList();
				if (submissionList.size() > 1) {
					for (Submission sub: submissionList) {
						System.out.println(submission.studyCode + " utilise par objet Submission " + sub.code);
					}
				} else {
					System.out.println("deletion dans base pour " + submission.studyCode);
					MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, submission.studyCode);
				}
			}
			if (! submission.sampleCodes.isEmpty()) {
				for (String sampleCode : submission.sampleCodes){
					// verifier que sample n'est pas utilisé par autre objet submission avant destruction
					List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("sampleCodes", sampleCode)).toList();
					if (submissionList.size() > 1) {
						for (Submission sub: submissionList) {
							System.out.println(sampleCode + " utilise par objet Submission " + sub.code);
						}
					} else {
						System.out.println("deletion dans base pour "+sampleCode);
						MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, sampleCode);		
					}
				}
			}		
			if (! submission.experimentCodes.isEmpty()) {
				for (String experimentCode : submission.experimentCodes) {
					// verifier que l'experiment n'est pas utilisé par autre objet submission avant destruction
					List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("experimentCodes", experimentCode)).toList();
					if (submissionList.size() > 1) {
						for (Submission sub: submissionList) {
							System.out.println(experimentCode + " utilise par objet Submission " + sub.code);
						}
					} else {
						System.out.println("deletion dans base pour "+experimentCode);
						MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
					}
				}
			}			
			MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		}
	}

*/
	
}