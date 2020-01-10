package services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.State;

import models.sra.submit.common.instance.Submission;
import models.sra.submit.util.SraException;

import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;

public class FileAcServices  {
	
	private static final play.Logger.ALogger logger = play.Logger.of(FileAcServices.class);

	// final static SubmissionWorkflows submissionWorkflows = Spring.get BeanOfType(SubmissionWorkflows.class);

	private final NGLConfig           config;
	private final SubmissionWorkflows submissionWorkflows;
	private SubmissionAPI   submissionAPI;
	private final StudyAPI  studyAPI;
	private final SampleAPI sampleAPI;
	private ExperimentAPI   experimentAPI;


	@Inject
	public FileAcServices(NGLConfig           config, 
						  SubmissionWorkflows submissionWorkflows,
						  SubmissionAPI       submissionAPI,
						  StudyAPI            studyAPI,
						  SampleAPI           sampleAPI, 
						  ExperimentAPI       experimentAPI) {
		this.config              = config;
		this.submissionWorkflows = submissionWorkflows;
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
		this.sampleAPI           = sampleAPI;
		this.experimentAPI       = experimentAPI;


	}
	

	
	public Submission traitementFileAC(ContextValidation ctxVal, String submissionCode, File ebiFileAc) throws IOException, SraException, MailServiceException {
		if (StringUtils.isBlank(submissionCode) || (ebiFileAc == null)) {
			throw new SraException("traitementFileAC :: parametres d'entree à null" );
		}
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		if (submission == null) {
			throw new SraException(" soumission " + submissionCode + " impossible à recuperer dans base");
		}
		if (! ebiFileAc.exists()) {
			throw new SraException("Fichier des AC de l'Ebi non present sur disque : "+ ebiFileAc.getAbsolutePath());
		}

//		BufferedReader inputBuffer = null;
//		try {
//			inputBuffer = new BufferedReader(new FileReader(ebiFileAc));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
		// Get global parameters for email => utiliser Play.application().configuration().getString plutot que
		// ConfigFactory.load().getString pour recuperer les parametres pour avoir les surcharges de AbstractTest si 
		// test unitaires 
		MailServices mailService = new MailServices();
//		String expediteur = ConfigFactory.load().getString("accessionReporting.email.from"); 
		// String expediteur = Play.application().configuration().getString("accessionReporting.email.from");
		String expediteur = config.getReleaseReportingEmailFrom();
		// String dest = Play.application().configuration().getString("accessionReporting.email.to");
		String dest = config.getReleaseReportingEmailTo();
		System.out.println("destinataires = "+ dest);
		// String subjectSuccess = Play.application().configuration().getString("accessionReporting.email.subject.success");
		String subjectSuccess = config.getReleaseReportingEmailSubjectSuccess();
		//logger.debug("subjectSuccess = "+Play.application().configuration().getString("accessionReporting.email.subject.success"));
		// String subjectError = Play.application().configuration().getString("accessionReporting.email.subject.error");
		String subjectError = config.getReleaseReportingEmailSubjectError(); 
		Set<String> destinataires = new HashSet<>();
		
		destinataires.addAll(Arrays.asList(dest.split(",")));    		    

//		String sujet = null;

		Map<String, String> mapSamples      = new HashMap<>(); // String, String>(); 
		Map<String, String> mapExtIdSamples = new HashMap<>(); // String, String>(); 
		Map<String, String> mapExperiments  = new HashMap<>(); // <String, String>(); 
		Map<String, String> mapRuns         = new HashMap<>(); // String, String>(); 
		String submissionAc      = null;
		String studyAc           = null;
		String studyExtId        = null;
		String message           = null;
		String ebiSubmissionCode = null;
		String ebiStudyCode      = null;
		String errorStatus       = "FE-SUB";
		String okStatus          = "F-SUB";
		Boolean ebiSuccess       = false;	
		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
		String user;
		if (StringUtils.isNotBlank(submission.creationUser)) {
			user = submission.creationUser;
		} else {
			user = ctxVal.getUser();
		}

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			/*
			 * Etape 2 : création d'un parseur
			 */
			final DocumentBuilder builder = factory.newDocumentBuilder();
			/*
			 * Etape 3 : création d'un Document
			 */
			
			final Document document= builder.parse(ebiFileAc);
			//Affiche du prologue
			System.out.println("*************PROLOGUE************");
			System.out.println("version : " + document.getXmlVersion());
			System.out.println("encodage : " + document.getXmlEncoding());      
			System.out.println("standalone : " + document.getXmlStandalone());
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			System.out.println("\n*************RACINE************");
			System.out.println(racine.getNodeName());
			System.out.println("success = " + racine.getAttribute("success"));
			//System.out.println(((Node) racine).getNodeName());
			//System.out.println("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
			/*
			 * Etape 5 : récupération des samples
			 */
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
				ebiSuccess = true;
			}
			for (int i = 0; i<nbRacineNoeuds; i++) {
				
				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					System.out.println("\n*************Elt************");
					
					String alias = elt.getAttribute("alias");
					String accession = elt.getAttribute("accession");
					
					System.out.println("alias : " + alias);
					System.out.println("accession : " + accession);
					
					if (elt.getTagName().equalsIgnoreCase("SUBMISSION")) {
						ebiSubmissionCode = elt.getAttribute("alias");	
						submissionAc = elt.getAttribute("accession");
					} else if(elt.getTagName().equalsIgnoreCase("STUDY")) {
						ebiStudyCode = elt.getAttribute("alias");	
						studyAc = elt.getAttribute("accession");
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						studyExtId = eltExtId.getAttribute("accession");
						System.out.println("study_ext_id: " + studyExtId);
				    } else if(elt.getTagName().equalsIgnoreCase("SAMPLE")) {
				    	mapSamples.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
				    	final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						//String sampleExtId = eltExtId.getAttribute("accession");
						mapExtIdSamples.put(elt.getAttribute("alias"), eltExtId.getAttribute("accession"));	
					} else if(elt.getTagName().equalsIgnoreCase("EXPERIMENT")) {
				    	mapExperiments.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
					} else if(elt.getTagName().equalsIgnoreCase("RUN")) {
						mapRuns.put(elt.getAttribute("alias"), elt.getAttribute("accession"));
					} else {
						// Ignore other nodes
					}
				}
			}  // end for  
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		} 
		
		if (! ebiSuccess ) {
			logger.debug("pattern 'success=true' absent du fichier  {}", ebiFileAc);
			// mettre status à jour
			State errorState = new State(errorStatus, user);
			submissionWorkflows.setState(ctxVal, submission, errorState);
			message = "Absence de la ligne RECEIPT ... pour  " + submissionCode + " dans fichier "+ ebiFileAc.getPath();
			mailService.sendMail(expediteur, destinataires, subjectError, new String(message.getBytes(), "iso-8859-1"));
			return submission;
		}
		
		// Mise à jour des objets :
		Boolean error = false;
//		sujet = "Probleme parsing fichier des AC : ";
		message = "Pour la soumission " + submissionCode + ", le fichier des AC "+ ebiFileAc.getPath() + "</br>";
		String destinataire = submission.creationUser;
		if (StringUtils.isNotBlank(destinataire)) {
			if (!destinataire.endsWith("@genoscope.cns.fr")) {
				destinataire = destinataire + "@genoscope.cns.fr";
			}
			destinataires.add(destinataire);
		}
		if (ebiSubmissionCode == null || StringUtils.isBlank(ebiSubmissionCode)) { // Duplicate test to avoid warning
			//System.out.println("Pas de Recuperation de ebiSubmissionCode");
		    message += "- ne contient pas ebiSubmissionCode \n";
			error = true;
		} else {
			if (! ebiSubmissionCode.equals(submissionCode)) {
				//System.out.println("ebiSubmissionCode != submissionCode");
				message += "- contient un ebiSubmissionCode ("  + ebiSubmissionCode + ") different du submissionCode passé en parametre "+ submissionCode + "</br>"; 
				error = true;
			}
			// Verifier que le nombre d'ac recuperés dans le fichier est bien celui attendu pour l'objet submission:
			if (StringUtils.isNotBlank(submission.studyCode)) {
				if (StringUtils.isBlank(studyAc)) {
					//System.out.println("studyAc attendu non trouvé pour " + submission.studyCode);
					message += "- ne contient pas de valeur pour le studyCode " + submission.studyCode+"</br>";
				}
			}
			if (submission.sampleCodes != null) {
				for (int i = 0; i < submission.sampleCodes.size() ; i++) {
					if (!mapSamples.containsKey(submission.sampleCodes.get(i))){
						//System.out.println("sampleAc attendu non trouvé pour " + submission.sampleCodes.get(i));
						message += "- ne contient pas d'AC pour le sampleCode " + submission.sampleCodes.get(i)+"</br>";
						error = true;
					}	
				}
			}
			if (submission.experimentCodes != null){
				for (int i = 0; i < submission.experimentCodes.size() ; i++) {
					if (!mapExperiments.containsKey(submission.experimentCodes.get(i))){
						//System.out.println("experimentAc attendu non trouvé pour " + submission.experimentCodes.get(i));
						message += "- ne contient pas d'AC pour l'experimentCode " + submission.experimentCodes.get(i) + "</br>";
						error = true;
					}	
				}
			}
			if (submission.runCodes != null){
				for (int i = 0; i < submission.runCodes.size() ; i++) {
					//System.out.println("runCode========="+ submission.runCodes.get(i));
					if (!mapRuns.containsKey(submission.runCodes.get(i))){
						//System.out.println("runAc attendu non trouvé pour " + submission.runCodes.get(i));
						message += "- ne contient pas d'AC pour le runCode " + submission.runCodes.get(i) + "</br>";
						error = true;
					}	
				}
			}
		}
		if (error) {
		    mailService.sendMail(expediteur, destinataires, subjectError, new String(message.getBytes(), "iso-8859-1"));
			State errorState = new State(okStatus, user);
			submissionWorkflows.setState(ctxVal, submission, errorState);
			return submission;
		} else {
			//System.out.println("OK");
		}
		
		// Mise à jour dans la base de la soumission et de ses objets pour les AC :
		ctxVal.setUpdateMode();

		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();		
		calendar.add(Calendar.YEAR, 2);
		Date release_date  = calendar.getTime();
		message = "Liste des AC attribues pour la soumission "  + submissionCode + " en mode confidentiel jusqu'au : " + release_date +" </br></br>";
//		String retourChariot = "</br>";
		message += "submissionCode = " + submissionCode + ",   AC = "+ submissionAc + "</br>";  
		submission.accession=submissionAc;
//		
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
//				DBQuery.is("code", submissionCode).notExists("accession"),
//				DBUpdate.set("accession", submissionAc).set("submissionDate", date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));	
		submissionAPI.dao_update(DBQuery.is("code", submissionCode).notExists("accession"),
								 DBUpdate.set("accession", submissionAc)
								 .set("submissionDate", date)
								 .set("traceInformation.modifyUser", user)
								 .set("traceInformation.modifyDate", date));	

		if (StringUtils.isNotBlank(ebiStudyCode)) {	
			message += "studyCode = " + ebiStudyCode + ",   AC = "+ studyAc + ", bioProjectId = " + studyExtId + "</br>";
			logger.debug("Mise à jour du study {} avec AC={} et externalId={}", ebiStudyCode, studyAc, studyExtId);
//			MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//					DBQuery.is("code", ebiStudyCode).notExists("accession"),
//					DBUpdate.set("accession", studyAc).set("externalId", studyExtId).set("firstSubmissionDate", date).set("releaseDate", release_date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));
			studyAPI.dao_update(DBQuery.is("code", ebiStudyCode).notExists("accession"),
								DBUpdate.set("accession", studyAc)
								.set("externalId", studyExtId)
								.set("firstSubmissionDate", date)
								.set("releaseDate", release_date)
								.set("traceInformation.modifyUser", user)
								.set("traceInformation.modifyDate", date));
			
		}

		for(Entry<String, String> entry : mapSamples.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			String ext_id_ac = mapExtIdSamples.get(code);
			message += "sampleCode = " + code + ",   AC = "+ ac + " , externalId = " + ext_id_ac +"</br>";  
			logger.debug("Mise à jour du sample {} avec AC={} et externalId={}", code, ac, ext_id_ac);

//			MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class,
//					DBQuery.is("code", code).notExists("accession"),
//					DBUpdate.set("accession", ac).set("externalId", ext_id_ac).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date)); 		
			sampleAPI.dao_update(DBQuery.is("code", code).notExists("accession"),
								 DBUpdate.set("accession", ac)
								 .set("externalId", ext_id_ac)
								 .set("traceInformation.modifyUser", user)
								 .set("traceInformation.modifyDate", date)); 		
			
		}
		for(Entry<String, String> entry : mapExperiments.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			message += "experimentCode = " + code + ",   AC = "+ ac + "</br>";  
			logger.debug("Mise à jour de l'exp {} avec AC={}", code, ac);
//			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,
//					DBQuery.is("code", code).notExists("accession"),
//					DBUpdate.set("accession", ac).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date)); 	
			experimentAPI.dao_update(DBQuery.is("code", code).notExists("accession"),
								     DBUpdate.set("accession", ac)
								     .set("traceInformation.modifyUser", user)
								     .set("traceInformation.modifyDate", date)); 	
		}
		for(Entry<String, String> entry : mapRuns.entrySet()) {
			String code = entry.getKey();
			String ac = entry.getValue();
			message += "runCode = " + code + ",   AC = "+ ac  + "</br>";  
			logger.debug("Mise à jour du run {} avec AC={}", code, ac);
			
//			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class,
//					DBQuery.is("run.code", code).notExists("run.accession"),
//					DBUpdate.set("run.accession", ac)); 			
			experimentAPI.dao_update(DBQuery.is("run.code", code).notExists("run.accession"),
									 DBUpdate.set("run.accession", ac)); 			
		}
		logger.debug("submission.studyCode = {}", submission.studyCode);

		State state = new State(okStatus, user);
		submissionWorkflows.setState(ctxVal, submission, state);
		logger.debug("expediteur = {}", expediteur);
		logger.debug("destinataires = {}", destinataires);
		logger.debug("subjectSuccess = {}", subjectSuccess);
		mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
		// Recuperer l'objet submission mis à jour pour le status :
//		submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		submission = submissionAPI.dao_getObject(submissionCode);
		return submission;
	}

}
