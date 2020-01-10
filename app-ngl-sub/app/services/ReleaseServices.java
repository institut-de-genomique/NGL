package services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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



//import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.util.SraException;
import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;

public class ReleaseServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(ReleaseServices.class);


	// final static SubmissionWorkflows submissionWorkflows = Spring.get BeanOfType(SubmissionWorkflows.class);
	private final SubmissionWorkflows submissionWorkflows;
	private final NGLConfig           config;
	private final SubmissionAPI       submissionAPI;
	private final StudyAPI            studyAPI;
	
	@Inject
	public ReleaseServices(NGLConfig config, 
						   SubmissionWorkflows submissionWorkflows,
						   SubmissionAPI submissionAPI,
						   StudyAPI studyAPI) {
		this.submissionWorkflows = submissionWorkflows;
		this.config              = config;
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
	}

	public Submission traitementRetourRelease(ContextValidation ctxVal, String submissionCode, File retourEbiRelease) throws IOException, SraException, MailServiceException {
		if (StringUtils.isBlank(submissionCode) || (retourEbiRelease == null)) {
			throw new SraException("traitementRelease :: parametres d'entree à null" );
		}
//		System.out.println("submissionCode=" + submissionCode);
		logger.debug("submissionCode = {}", submissionCode);
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);

		if (submission == null) 
			throw new SraException("soumission " + submissionCode + " impossible à recuperer dans base");
//		Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, submission.studyCode);
		Study study = studyAPI.dao_getObject(submission.studyCode);
		if (! retourEbiRelease.exists())
			throw new SraException("Fichier resultat de l'ebi pour la release absent des disques : " + retourEbiRelease.getAbsolutePath());
		if (!submission.release)
			throw new SraException("soumission " + submission.code + " ne correspond pas a une soumission pour release");

//		BufferedReader inputBuffer = null;
//		try {
//			inputBuffer = new BufferedReader(new FileReader(retourEbiRelease));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		// BufferedReader inputBuffer = null;
//		try (BufferedReader inputBuffer = new BufferedReader(new FileReader(retourEbiRelease))) {
//			// inputBuffer = new BufferedReader(new FileReader(retourEbiRelease));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} 
		
		// Get global parameters for email => utiliser Play.application().configuration().getString plutot que
		// ConfigFactory.load().getString pour recuperer les parametres pour avoir les surcharges de AbstractTest si 
		// test unitaires 
		MailServices mailService = new MailServices();
//		String expediteur = ConfigFactory.load().getString("releaseReporting.email.from"); 
		// String expediteur = Play.application().configuration().getString("releaseReporting.email.from");
		String expediteur = config.getReleaseReportingEmailFrom();
//		System.out.println("expediteur=" + expediteur);
		logger.debug("expediteur=" + expediteur);
		// String dest = Play.application().configuration().getString("releaseReporting.email.to");
		String dest = config.getReleaseReportingEmailTo();
//		System.out.println("destinataires = " + dest);
		logger.debug("destinataires = " + dest);
		// String subjectSuccess = Play.application().configuration().getString("releaseReporting.email.subject.success");
		String subjectSuccess = config.getReleaseReportingEmailSubjectSuccess();
		//l.debug("subjectSuccess = "+Play.application().configuration().getString("releaseReporting.email.subject.success"));
		// String subjectError = Play.application().configuration().getString("releaseReporting.email.subject.error");
		String subjectError = config.getReleaseReportingEmailSubjectError();
		Set<String> destinataires = new HashSet<>();
		
		destinataires.addAll(Arrays.asList(dest.split(",")));    		    

		String  message        = null;
		String  errorStatus    = "FE-SUB-R";
		String  okStatus       = "F-SUB";
		Boolean ebiSuccess     = false;	
		String  studyAccession = null;
		String  infos          = null;
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
			
			final Document document= builder.parse(retourEbiRelease);
			//Affiche du prologue
			/*System.out.println("*************PROLOGUE************");
			System.out.println("version : " + document.getXmlVersion());
			System.out.println("encodage : " + document.getXmlEncoding());      
			System.out.println("standalone : " + document.getXmlStandalone());
            */
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			/*System.out.println("\n*************RACINE************");
			System.out.println(racine.getNodeName());
			System.out.println("success = " + racine.getAttribute("success"));
			*/
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			
//			System.out.println("Nombre de racine noeud = "+ nbRacineNoeuds);
			logger.debug("Nombre de racine noeud = "+ nbRacineNoeuds);
			
			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
				ebiSuccess = true;
			} else {
				ebiSuccess = false;
				message = "Absence de la ligne RECEIPT ... pour  " + submissionCode + " dans fichier "+ retourEbiRelease.getPath();
			}
			for (int i = 0; i<nbRacineNoeuds; i++) {
				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					/*System.out.println("\n*************Elt************");
					System.out.println("localName="+elt.getLocalName());
					System.out.println("nodeName="+ elt.getNodeName());
					System.out.println("nodeValue="+elt.getNodeValue());
					System.out.println("nodeType="+elt.getNodeType());
					System.out.println("textContent="+elt.getTextContent());	
					*/
					if (elt.getNodeName().equals("MESSAGES")){
						if (elt.getElementsByTagName("INFO").item(0) != null){
							infos = elt.getElementsByTagName("INFO").item(0).getTextContent();
							//System.out.println("infos="+ infos);
							String pattern = "study accession \"([^\"]+)\" is set to public status";
							java.util.regex.Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(infos);
							if ( m.find() ) { 
								studyAccession = m.group(1);
//								System.out.println("studyAccession="+ studyAccession);
								logger.debug("studyAccession="+ studyAccession);
							}
						}
					}
				}			
			}  // end for  
//		} catch (final ParserConfigurationException e) {
//			e.printStackTrace();
//		} catch (final SAXException e) {
//			e.printStackTrace();
//		} catch (final IOException e) {
//			e.printStackTrace();
//		} 
		} catch (final ParserConfigurationException| SAXException | IOException e) {
			e.printStackTrace();
		} 
		if (submissionCode.equals(submission.code)) {
//			System.out.println("ok submissionCode = submission.code");
			logger.debug("ok submissionCode = submission.code");
		}
		if (studyAccession != null && StringUtils.isNotBlank(studyAccession)) { // Duplicate test to avoid warning
			if (studyAccession.equals(study.accession)) {
//				System.out.println("studyAccession :'"+ studyAccession + "' ==  study.accession :'" +  study.accession +"'");
				logger.debug("studyAccession :'"+ studyAccession + "' ==  study.accession :'" +  study.accession +"'");
				ebiSuccess = true;
				message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "</br>"; 
			} else {
				ebiSuccess = false;
//				System.out.println("studyAccession :'"+ studyAccession + "' !=  study.accession :'" +  study.accession +"' pour study.code = '"+ study.code +"'");
				logger.debug("studyAccession :'"+ studyAccession + "' !=  study.accession :'" +  study.accession +"' pour study.code = '"+ study.code +"'");
				message = "La soumission ."+ submission.code + " indique un study à releaser "+ submission.studyCode + " different du studyAccession indiqué dans " + retourEbiRelease.getName();
			}
		} else {
//			System.out.println("Pas de recuperation du studyAccession");
			logger.debug("Pas de recuperation du studyAccession");
			message = "La soumission ."+ submission.code + " a un retour incorrect " + retourEbiRelease.getName();
		}
		String destinataire = submission.creationUser;
//		System.out.println("destinataire="+destinataire);
		logger.debug("destinataire="+destinataire);
		// ne pas envoyer de mail à ngsrg:
		if (destinataire.equals("ngsrg")) {
			destinataire = "";
		}
		if (StringUtils.isNotBlank(destinataire)) {
			if (!destinataire.endsWith("@genoscope.cns.fr")) {
				destinataire = destinataire + "@genoscope.cns.fr";
			}
			destinataires.add(destinataire);
		}
		
		// Mise à jour dans la base de la soumission du status et de la date de release :
		ctxVal.setUpdateMode();
		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();		
		Date release_date  = calendar.getTime();
		
		if (! ebiSuccess ) {
			// mettre status à jour
			State errorState = new State(errorStatus, user);
			submissionWorkflows.setState(ctxVal, submission, errorState);
			mailService.sendMail(expediteur, destinataires, subjectError, new String(message.getBytes(), "iso-8859-1"));
		} else {
			State okState = new State(okStatus, user);
			submissionWorkflows.setState(ctxVal, submission, okState);
			message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "</br>"; 
			mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
//			MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
//				DBQuery.is("code", submissionCode),
//				DBUpdate.set("submissionDate", date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));	
			submissionAPI.dao_update(DBQuery.is("code", submissionCode), 
									 DBUpdate.set("submissionDate", date).
									 set("traceInformation.modifyUser", user).
									 set("traceInformation.modifyDate", date));

//			MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//				DBQuery.is("accession", studyAccession),
//				DBUpdate.set("releaseDate", release_date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));
			studyAPI.dao_update(DBQuery.is("accession", studyAccession), DBUpdate.set("releaseDate", release_date).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", date));
		}
		// Recuperer l'objet soumission mis à jour pour le status :
//		submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		submission = submissionAPI.dao_getObject(submissionCode);
		return submission;
	}
	
}
