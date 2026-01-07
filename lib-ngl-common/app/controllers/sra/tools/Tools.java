package controllers.sra.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
//import javax.inject.Inject;
//import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;

public class Tools {

	// passer par MongoDBDAO plutot que par abstractSampleAPI dans cette classe statique.
	//private  final static AbstractSampleAPI abstractSampleAPI  = AbstractSampleAPI.get();  // pour eviter injection problematique ici
	//private  final static AbstractStudyAPI  abstractStudyAPI   = AbstractStudyAPI.get();
	//private final static SubmissionAPI      submissionAPI      = SubmissionAPI.get();
	
	private static final play.Logger.ALogger logger = play.Logger.of(Tools.class);
	
	/**
	 * Methode qui va chercher l'abstSample indiqué par le sampleIdentifier. Si l'abstSample n'est pas null, 
	 * alors, l'accession et l'externalId de l'abstSample sont ajoutés à la liste des sampleIdentifiers.
	 * @param sampleIdentifier identifiant du sample
	 * @param sampleIdentifiers liste des sampleIdentifiers
	 */
	public static void completeListSampleIdentifiers(String sampleIdentifier, List<String> sampleIdentifiers) {
		if(sampleIdentifiers == null) {
			sampleIdentifiers = new ArrayList<String>();
		}
		AbstractSample abstSample = null;
		logger.debug("dans completeListSampleIdentifiers, sampleIdentifier='{}'", sampleIdentifier);
		if (sampleIdentifier.startsWith("SAM")) {
			logger.debug("sampleIdentifier de type SAM");
			 //if(abstractSampleAPI.dao_checkObjectExist("externalId", sampleIdentifier)) {
			 if( MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
					                           AbstractSample.class,
					                           DBQuery.is("externalId", sampleIdentifier))) {                       
			    logger.debug("sampleIdentifier de type SAM existe bien dans base");
				//abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("externalId", sampleIdentifier));
				abstSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
						                        AbstractSample.class,  
						                        DBQuery.is("externalId", sampleIdentifier));
				if(abstSample != null) {
					logger.debug("sampleIdentifier de type SAM existe bien dans base et bien recupere");
				}
			 }
		 } else {// cas ERS ou SRS
			 //if(abstractSampleAPI.dao_checkObjectExist("accession", sampleIdentifier)) {
			 if(  MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
                     						   AbstractSample.class,
                     						   DBQuery.is("accession", sampleIdentifier))) {  
				 //abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("accession", sampleIdentifier)); 
				 abstSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
	                        					 AbstractSample.class,  
	                        					 DBQuery.is("accession", sampleIdentifier));
			 }
		 }
		if(abstSample != null) {
			//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX sampleCode =" + abstSample.code);
			if( StringUtils.isNotBlank(abstSample.accession) && ! sampleIdentifiers.contains(abstSample.accession)) {
				//logger.debug("ajout de sampleIdentifier='{}' dans la liste des sampleIdentifiers", abstSample.accession);
				sampleIdentifiers.add(abstSample.accession);
			}
			if( StringUtils.isNotBlank(abstSample.externalId) && ! sampleIdentifiers.contains(abstSample.externalId)) {
				//logger.debug("ajout de sampleIdentifier='{}' dans la liste des sampleIdentifiers", abstSample.externalId);
				sampleIdentifiers.add(abstSample.externalId);
			}
		}
	}
	/**
	 * Methode qui va chercher l'abstStudy indiqué par le studyIdentifier. Si l'abstStudy n'est pas null, 
	 * alors, l'accession et l'externalId de l'abstStudy sont ajoutés à la liste des studyIdentifiers.
	 * @param studyIdentifier studyIdentifier
	 * @param studyIdentifiers liste des studyIdentifiers
	 */
	public static void completeListStudyIdentifiers(String studyIdentifier, List<String> studyIdentifiers) {
		if(studyIdentifiers == null) {
			studyIdentifiers = new ArrayList<String>();
		}
		AbstractStudy abstStudy = null;
		//logger.debug("studyIdentifier='{}'", studyIdentifier);
		if (studyIdentifier.startsWith("PRJ")) {
			 //if(abstractStudyAPI.dao_checkObjectExist("externalId", studyIdentifier)) {
			 if( MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, 
                     						 AbstractStudy.class,
                     						 DBQuery.is("externalId", studyIdentifier))) {                       
				 logger.debug("sampleIdentifier de type SAM existe bien dans base");
				 //abstStudy = abstractStudyAPI.dao_findOne(DBQuery.in("externalId", studyIdentifier));
				 abstStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
						 	                    AbstractStudy.class, 
						 	                    DBQuery.in("externalId", studyIdentifier));
			 }
		 } else {// cas ERP 
			 //if(abstractStudyAPI.dao_checkObjectExist("accession", studyIdentifier)) {
			 if( MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, 
                     						 AbstractStudy.class,
                     						 DBQuery.is("accession", studyIdentifier))) {                       
				 //abstStudy = abstractStudyAPI.dao_findOne(DBQuery.in("accession", studyIdentifier)); 
				 abstStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME, 
						                        AbstractStudy.class,
						                        DBQuery.in("accession", studyIdentifier)); 
			 }
		 }
		if(abstStudy != null) {
			//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX studyCode =" + abstStudy.code);
			if( StringUtils.isNotBlank(abstStudy.accession) && ! studyIdentifiers.contains(abstStudy.accession)) {
				//logger.debug("ajout de studyIdentifier='{}' dans la liste des studyIdentifiers", abstStudy.accession);
				studyIdentifiers.add(abstStudy.accession);
			}
			if( StringUtils.isNotBlank(abstStudy.externalId) && ! studyIdentifiers.contains(abstStudy.externalId)) {
				//logger.debug("ajout de studyIdentifier='{}' dans la liste des studyIdentifiers", abstStudy.accession);
				studyIdentifiers.add(abstStudy.externalId);
			}
		}
	}
	
	/**
	 * Methode qui va chercher les abstSample indiqués par le sampleIdentifierRegex et qui ajoute dans la liste des sampleIdentifiers
	 * les identifiants des abstSample s'ils ne sont pas nuls 
	 * @param sampleIdentifierRegex sampleIdentifierRegex
	 * @param sampleIdentifiers sampleIdentifiers
	 */
	public static void completeListSampleIdentifiersFromIdentifierRegex(String sampleIdentifierRegex, List<String>sampleIdentifiers) {
		if(sampleIdentifiers == null) {
			sampleIdentifiers = new ArrayList<String>();
		}
		List<AbstractSample> abstSamples = new ArrayList<AbstractSample>();
		logger.debug("sampleIdentifierRegex='{}'", sampleIdentifierRegex );
		if(sampleIdentifierRegex.startsWith("SAM")) {
			logger.debug("sampleIdentifierRegex='{}' de type SAM", sampleIdentifierRegex );
			//abstSamples = abstractSampleAPI.dao_find(DBQuery.regex("externalId", Pattern.compile(sampleIdentifierRegex))).toList();
			abstSamples = MongoDBDAO.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
										  AbstractSample.class,
										  DBQuery.regex("externalId", Pattern.compile(sampleIdentifierRegex))).toList();
		} else {
			logger.debug("sampleIdentifierRegex='{}' de type ERS ou SRS", sampleIdentifierRegex );
			//abstSamples = abstractSampleAPI.dao_find(DBQuery.regex("accession", Pattern.compile(sampleIdentifierRegex))).toList();
			abstSamples = MongoDBDAO.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, 
					  					  AbstractSample.class,
					  					  DBQuery.regex("accession", Pattern.compile(sampleIdentifierRegex))).toList();
		}
		logger.debug("Nombre de samples trouves = '{}'", abstSamples.size());
		for (AbstractSample abstSample : abstSamples) {
			logger.debug("sample trouve avec code = '{}' et accession='{}' et externalId='{}' ", abstSample.code, abstSample.accession, abstSample.externalId);
			if(StringUtils.isNotBlank(abstSample.accession) && ! sampleIdentifiers.contains(abstSample.accession)) {
				sampleIdentifiers.add(abstSample.accession);
			}
			if(StringUtils.isNotBlank(abstSample.externalId) && ! sampleIdentifiers.contains(abstSample.externalId)) {
				sampleIdentifiers.add(abstSample.externalId);
			}	
		}
	}
	
	/**
	 * Methode qui va chercher les abstSample indiqués par le sampleIdentifierRegex et qui ajoute dans la liste des sampleIdentifiers
	 * les identifiants des abstSample s'ils ne sont pas nuls 
	 * @param studyIdentifierRegex studyIdentifierRegex
	 * @param studyIdentifiers studyIdentifiers
	 */
	public static void completeListStudyIdentifiersFromIdentifierRegex(String studyIdentifierRegex, List<String>studyIdentifiers) {
		if(studyIdentifiers == null) {
			studyIdentifiers = new ArrayList<String>();
		}
		List<AbstractStudy> abstStudies = new ArrayList<AbstractStudy>();
		logger.debug("studyIdentifierRegex='{}'", studyIdentifierRegex );
		if(studyIdentifierRegex.startsWith("PRJ")) {
			logger.debug("studyIdentifierRegex='{}' de type PRJ", studyIdentifierRegex );
			//abstStudies = abstractStudyAPI.dao_find(DBQuery.regex("externalId", Pattern.compile(studyIdentifierRegex))).toList();
			abstStudies = MongoDBDAO.find(InstanceConstants.SRA_STUDY_COLL_NAME, 
					 					  AbstractStudy.class,
					 					  DBQuery.regex("externalId", Pattern.compile(studyIdentifierRegex))).toList();

		} else {
			logger.debug("studyIdentifierRegex='{}' de type ERP", studyIdentifierRegex );
			//abstStudies = abstractStudyAPI.dao_find(DBQuery.regex("accession", Pattern.compile(studyIdentifierRegex))).toList();
			abstStudies =  MongoDBDAO.find(InstanceConstants.SRA_STUDY_COLL_NAME, 
					  					   AbstractStudy.class,
					  					   DBQuery.regex("accession", Pattern.compile(studyIdentifierRegex))).toList();
		}
		logger.debug("Nombre de studies trouves = '{}'", abstStudies.size());
		for (AbstractStudy abstStudy : abstStudies) {
			logger.debug("abstStudy trouve avec code = '{}' et accession='{}' et externalId='{}' ", abstStudy.code, abstStudy.accession, abstStudy.externalId);
			if(StringUtils.isNotBlank(abstStudy.accession) && ! studyIdentifiers.contains(abstStudy.accession)) {
				studyIdentifiers.add(abstStudy.accession);
			}
			if(StringUtils.isNotBlank(abstStudy.externalId) && ! studyIdentifiers.contains(abstStudy.externalId)) {
				studyIdentifiers.add(abstStudy.externalId);
			}
			
		}
	}
	
	/**
	 * Methode qui va chercher les sampleCodes dans la liste des soumission indiquée.
	 * Ajoute ces sampleCodes dans la liste des sampleCodes passée en parametre
	 * @param submissionCodes  liste des submissionCodes dans lesquelles chercher les sampleCodes
	 * @param sampleCodes     liste des sampleCodes à completer 
	 */
	public static void completeListSampleCodesFromSubmissionCodes(List<String> submissionCodes, List<String>sampleCodes) {
		if(sampleCodes == null) {
			sampleCodes = new ArrayList<String>();
		}
		for (String submissionCode : submissionCodes) {
			//if(! submissionAPI.isObjectExist(submissionCode)) {
			 if( ! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
						 					 Submission.class,
						 					 DBQuery.is("code", submissionCode))) {  
				 continue;
			}
			//Submission submission = submissionAPI.get(submissionCode);
			Submission submission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class,  
													   DBQuery.is("code", submissionCode));
			if(submission.sampleCodes != null) {
				for (String sampleCode : submission.sampleCodes) {
					if (StringUtils.isNotBlank(sampleCode) && ! sampleCodes.contains(sampleCode)) {
						sampleCodes.add(sampleCode);
					}
				}
			}
		}
	}
	
	/**
	 * Methode qui va chercher les abstSample dans le champs sampleCodes des soumissions indiquée par la regex et qui ajoute dans la liste des sampleCodes
	 * les sampleCodes des abstSample s'ils ne sont pas nuls 
	 * @param submissionCodeRegex regex des codes de soumission
	 * @param sampleCodes      liste des sampleCodes
	 */
	public static void completeListSampleCodesFromSubmissionRegex(String submissionCodeRegex, List<String>sampleCodes) {
		//List<Submission> submissions = submissionAPI.dao_find(DBQuery.regex("code", Pattern.compile(SubmissionCodeRegex))).toList();		
		List<Submission> submissions = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class, 
													   DBQuery.regex("code", Pattern.compile(submissionCodeRegex))).toList();	 
		for (Submission submission : submissions) {
			if(submission.sampleCodes != null) {
				for (String sampleCode : submission.sampleCodes) {
					if (StringUtils.isNotBlank(sampleCode) && ! sampleCodes.contains(sampleCode)) {
						sampleCodes.add(sampleCode);
					}
				}
			}
		}
	}
	
	/**
	 * Methode qui va chercher les experimentCodes dans la liste des soumission indiquée.
	 * Ajoute ces experiment dans la liste des experimentCodes passée en parametre
	 * @param submissionCodes      liste des submissionCodes dans lesquelles chercher les experimentCodes
	 * @param experimentCodes     liste des experimentCodes à completer 
	 */
	public static void completeListExperimentCodesFromSubmissionCodes(List<String> submissionCodes, List<String>experimentCodes) {
		if(experimentCodes == null) {
			experimentCodes = new ArrayList<String>();
		}
		for (String submissionCode : submissionCodes) {
			//if(! submissionAPI.isObjectExist(submissionCode)) {
			 if( ! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
						 					 Submission.class,
						 					 DBQuery.is("code", submissionCode))) {  
				 continue;
			}
			//Submission submission = submissionAPI.get(submissionCode);
			Submission submission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class,  
													   DBQuery.is("code", submissionCode));
			if(submission.experimentCodes != null) {
				for (String experimentCode : submission.experimentCodes) {
					if (StringUtils.isNotBlank(experimentCode) && ! experimentCodes.contains(experimentCode)) {
						experimentCodes.add(experimentCode);
					}
				}
			}
		}
	}
	
	/**
	 * Methode qui va chercher les experimentCodes dans les soumissions indiquées par la regex 
	 * et qui ajoute ces experiment dans la liste des experimentCodes passée en parametre
	 * @param SubmissionCodeRegex   regex des codes de soumission
	 * @param experimentsCodes      liste des experimentCodes à completer 
	 */
	public static void completeListExperimentCodesFromSubmissionRegex(String SubmissionCodeRegex, List<String>experimentsCodes) {
		//List<Submission> submissions = submissionAPI.dao_find(DBQuery.regex("code", Pattern.compile(SubmissionCodeRegex))).toList();		
		List<Submission> submissions = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class, 
													   DBQuery.regex("code", Pattern.compile(SubmissionCodeRegex))).toList();	 
		for (Submission submission : submissions) {
			if(submission.experimentCodes != null) {
				for (String expCode : submission.experimentCodes) {
					if (StringUtils.isNotBlank(expCode) && ! experimentsCodes.contains(expCode)) {
						experimentsCodes.add(expCode);
					}
				}
			}
		}
	}
	
	/**
	 * Methode qui va chercher les analysisCode dans la liste des soumission indiquée.
	 * Ajoute ces analyses dans la liste des analysisCodes passée en parametre
	 * @param submissionCodes      liste des submissionCodes dans lesquelles chercher les analysisCode
	 * @param analysisCodes        liste des analysisCodes à completer 
	 */
	public static void completeListAnalysisCodesFromSubmissionCodes(List<String> submissionCodes, List<String>analysisCodes) {
		if(analysisCodes == null) {
			analysisCodes = new ArrayList<String>();
		}
		for (String submissionCode : submissionCodes) {
			//if(! submissionAPI.isObjectExist(submissionCode)) {
			 if( ! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
						 					 Submission.class,
						 					 DBQuery.is("code", submissionCode))) {  
				 continue;
			}
			//Submission submission = submissionAPI.get(submissionCode);
			Submission submission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class,  
													   DBQuery.is("code", submissionCode));
			if(submission.analysisCode != null && StringUtils.isNotBlank(submission.analysisCode) && ! analysisCodes.contains(submission.analysisCode)) {
				analysisCodes.add(submission.analysisCode);
			}
		}
	}
	
	/**
	 * Methode qui va chercher les analysisCodes dans les soumissions indiquées par la regex 
	 * et qui ajoute ces analysis dans la liste des analysisCodes passée en parametre
	 * @param SubmissionCodeRegex   regex des codes de soumission
	 * @param analysisCodes         liste des analysisCodes à completer 
	 */
	public static void completeListAnalysisCodesFromSubmissionRegex(String SubmissionCodeRegex, List<String>analysisCodes) {
		//List<Submission> submissions = submissionAPI.dao_find(DBQuery.regex("code", Pattern.compile(SubmissionCodeRegex))).toList();		
		List<Submission> submissions = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, 
													   Submission.class, 
													   DBQuery.regex("code", Pattern.compile(SubmissionCodeRegex))).toList();	 
		for (Submission submission : submissions) {
			if(submission.analysisCode != null && StringUtils.isNotBlank(submission.analysisCode) && ! analysisCodes.contains(submission.analysisCode)) {
				analysisCodes.add(submission.analysisCode);
			}
		}
	}
		
	

}
