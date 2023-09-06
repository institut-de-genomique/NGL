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

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.sra.submit.util.VariableSRA;
import ngl.refactoring.state.SRASubmissionStateNames;
import validation.ContextValidation;

public class ReleaseServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(ReleaseServices.class);


	private final NGLConfig           config;
	private final StudyDAO            studyDAO;
	private final SubmissionDAO       submissionDAO;
	private final SraCodeHelper       sraCodeHelper;
	
	@Inject
	public ReleaseServices(NGLConfig     config, 
						   SubmissionDAO submissionDAO,
						   StudyDAO      studyDAO,
						   SraCodeHelper sraCodeHelper) {
		this.config              = config;
		this.submissionDAO       = submissionDAO;
		this.studyDAO            = studyDAO;
		this.sraCodeHelper       = sraCodeHelper;
	}
	
	
	/**
	 * Parse le fichier retour de l'EBI recu en reponse de l'envoie des metadonnées pour une release de study.
	 * Met a jour dans la base le study pour la date de release et la soumission pour la date de soumission.
	 * Ne met pas à jour les objets pour l'etat.
	 * @param  ctxVal                 context de validation
	 * @param  submission             objet soumission
	 * @param  retourEbiRelease       fichier retour de l'ebi en reponse d'une soumission pour release d'un study
	 * @throws IOException            error
	 * @throws SraException           error
	 * @throws MailServiceException   error
	 */
	public void traitementRetourRelease(ContextValidation ctxVal, Submission submission, File retourEbiRelease) throws IOException, SraException, MailServiceException {
		if (submission == null) {
			throw new SraException("parametre soumission à null ??");
		}
		String submissionCode = submission.code;
		if (StringUtils.isBlank(submissionCode) || (retourEbiRelease == null)) {
			throw new SraException("traitementRelease :: parametres d'entree à null" );
		}
		//logger.debug("submissionCode=" + submissionCode);
		//logger.debug("submissionCode = {}", submissionCode);
		//Submission submission = submissionDAO.getObject(submissionCode);

		
		Study study = studyDAO.getObject(submission.studyCode);
		if (! retourEbiRelease.exists())
			throw new SraException("Fichier resultat de l'ebi pour la release absent des disques : " + retourEbiRelease.getAbsolutePath());
//		if (!submission.release)
		if (submission.type != Submission.Type.RELEASE) {
			throw new SraException("soumission " + submission.code + " ne correspond pas a une soumission pour release");
		}
		// Get global parameters for email => utiliser Play.application().configuration().getString plutot que
		// ConfigFactory.load().getString pour recuperer les parametres pour avoir les surcharges de AbstractTest  
		// si tests unitaires 
		MailServices mailService = new MailServices();
//		String expediteur = ConfigFactory.load().getString("releaseReporting.email.from"); 
		// String expediteur = Play.application().configuration().getString("releaseReporting.email.from");
		String expediteur = config.getReleaseReportingEmailFrom();
//		logger.debug("expediteur=" + expediteur);
		// String dest = Play.application().configuration().getString("releaseReporting.email.to");
		String dest = config.getReleaseReportingEmailTo();
		Set<String> destinataires = new HashSet<>();
		destinataires.addAll(Arrays.asList(dest.split(",")));    		    
		String destinataire = submission.traceInformation.createUser;		
		if (StringUtils.isNotBlank(destinataire)) {
			if (! destinataire.endsWith("@genoscope.cns.fr")) {
				destinataire = destinataire + "@genoscope.cns.fr";
			}
			destinataires.add(destinataire);
		}

		//logger.debug("destinataires = " + dest);
		// String subjectSuccess = Play.application().configuration().getString("releaseReporting.email.subject.success");
		String subjectSuccess = config.getReleaseReportingEmailSubjectSuccess();
		//logger.debug("subjectSuccess = "+Play.application().configuration().getString("releaseReporting.email.subject.success"));
		// String subjectError = Play.application().configuration().getString("releaseReporting.email.subject.error");
		String subjectError = config.getReleaseReportingEmailSubjectError();


		String  message        = null;
		Boolean ebiSuccess     = false;	
		String  studyAccession = null;
		String  infos          = null;
		// On ne prend pas ctxVal.getUser(), car methode lancé par birds,pas très informatif
		String user = null;
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
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
//			logger.debug("*************PROLOGUE************");
//			logger.debug("version : " + document.getXmlVersion());
//			logger.debug("encodage : " + document.getXmlEncoding());      
//			logger.debug("standalone : " + document.getXmlStandalone());
            
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
//			logger.debug("\n*************RACINE************");
//			logger.debug(racine.getNodeName());
//			logger.debug("success = " + racine.getAttribute("success"));
			
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			
//			System.out.println("Nombre de racine noeud = "+ nbRacineNoeuds);
			//logger.debug("Nombre de racine noeud = "+ nbRacineNoeuds);
			
			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
				ebiSuccess = true;
			} else {
				ebiSuccess = false;
				message = "Absence de la ligne RECEIPT ... pour  " + submissionCode + " dans fichier "+ retourEbiRelease.getPath();
			}
			for (int i = 0; i<nbRacineNoeuds; i++) {
				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elt = (Element) racineNoeuds.item(i);
//					//Affichage d'un elt :
//					logger.debug("\n*************Elt************");
//					logger.debug("localName="+elt.getLocalName());
//					logger.debug("nodeName="+ elt.getNodeName());
//					logger.debug("nodeValue="+elt.getNodeValue());
//					logger.debug("nodeType="+elt.getNodeType());
//					logger.debug("textContent="+elt.getTextContent());	
				
					if (elt.getNodeName().equals("MESSAGES")){
						if (elt.getElementsByTagName("INFO").item(0) != null){
							infos = elt.getElementsByTagName("INFO").item(0).getTextContent();
							//System.out.println("infos="+ infos);
							String pattern = "study accession \"([^\"]+)\" is set to public status";
							java.util.regex.Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(infos);
							if ( m.find() ) { 
								studyAccession = m.group(1);
								//logger.debug("studyAccession="+ studyAccession);
							}
						}
					}
				}			
			}  // end for  
		} catch (final ParserConfigurationException| SAXException | IOException e) {
			e.printStackTrace();
		} 

		if (studyAccession != null && StringUtils.isNotBlank(studyAccession)) { // Duplicate test to avoid warning
			if (studyAccession.equals(study.accession)) {
				//logger.debug("studyAccession :'"+ studyAccession + "' ==  study.accession :'" +  study.accession +"'");
				ebiSuccess = true;
				message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "</br>"; 
			} else {
				ebiSuccess = false;
				//logger.debug("studyAccession :'"+ studyAccession + "' !=  study.accession :'" +  study.accession +"' pour study.code = '"+ study.code +"'");
				message = "La soumission ."+ submission.code + " indique un study à releaser "+ submission.studyCode + " different du studyAccession indiqué dans " + retourEbiRelease.getName();
			}
		} else {
			//logger.debug("Pas de recuperation du studyAccession");
			ebiSuccess = false;
			message = "La soumission ."+ submission.code + " a un retour incorrect " + retourEbiRelease.getName();
		}

		
		// Mise à jour dans la base de la date d'envoie de la soumission et de la date de release du study
		ctxVal.setUpdateMode();
		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();		
		Date release_date  = calendar.getTime();
		
		if (! ebiSuccess ) {
			mailService.sendMail(expediteur, destinataires, subjectError, new String(message.getBytes(), "iso-8859-1"));
			throw new SraException(message);
		} else {
			message = "Objets lies au studyAccession = " + studyAccession + " mis dans le domaine public via la soumission "+ submissionCode + "</br>"; 
			mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
			submissionDAO.update(DBQuery.is("code", submissionCode), 
									 DBUpdate.set("submissionDate", date).
									 set("traceInformation.modifyUser", user).
									 set("traceInformation.modifyDate", date));
			
			studyDAO.update(DBQuery.is("accession", studyAccession), 
							DBUpdate.set("releaseDate", release_date)
							        .set("traceInformation.modifyUser", user)
									.set("traceInformation.modifyDate", date));
		}
		// recharger dans l'objet soumission les valeurs mises à jour dans la base:
		submission = submissionDAO.getObject(submissionCode);
	}

	/**
	 * Construit la soumission correspondant à la release du study dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etat
	 * {@link ngl.refactoring.state.States#SUBR_N}.
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  contextValidation  contexte de validation
	 * @param  studyCode          code du study
	 * @param  user               nom de l'utilisateur
	 * @return submissionCode     code de la soumission nouvellement crée et sauvée dans la base.
	 * @throws SraException       error
	 */

	public String createSubmissionForRelease(ContextValidation contextValidation, 
			                                 String studyCode, 
											 String user) throws SraException {
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		Date courantDate = new java.util.Date();
		contextValidation.setCreationMode();		
		Study study = studyDAO.findByCode(studyCode);

		if (study == null) {
			String message = "Le studyCode " + studyCode 
					+ " n'existe pas dans base";
			throw new SraException("createSubmissionFromStudy", message);
		} 
		
		SraCodeHelper.assertObjectStateCode(study, SRASubmissionStateNames.SUB_F, contextValidation);
		if (study.traceInformation.createUser == null) {
			String message = "Le study " + studyCode 
					+ " n'est pas renseigné pour le champs createUser";	
			//throw new SraException("createSubmissionFromStudy", message);
			contextValidation.addError("createSubmissionFromStudy", message);
		}
		if (user == null) {
			String message = "l'utilisateur n'est pas authentifié";
			//throw new SraException("createSubmissionFromStudy", message);
			contextValidation.addError("createSubmissionFromStudy", message);
		}
		// suprression du controle utilisateur
//		if (! study.traceInformation.createUser.equals(user)) {		
//			String message = "l'utilisateur " + user + 
//					" n'est pas autorisé à rendre publique le study " + study.code 
//					+ " crée par " + study.traceInformation.createUser;
//			throw new SraException("createSubmissionFromStudy", message);
//		}
		
//		if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", studyCode),
//				   									   DBQuery.is("type", Submission.Type.RELEASE)))) {
			
			if (submissionDAO.checkObjectExist(DBQuery.and(DBQuery.is("studyCode", studyCode),
					   DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)))) {
//			String mess = "";
//			for (Submission subm : submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", studyCode),
//													   DBQuery.is("release", true)))) {
//				mess += subm.code + " "; 
//			}

			Iterable<Submission> it = submissionDAO.find(DBQuery.and(DBQuery.is("studyCode", studyCode),
						 DBQuery.notIn("state.code", SRASubmissionStateNames.SUB_F)));
 			String mess = Iterables.map(it, s->s.code)
 					               .surround("(", ", ", ")")
 					               .asString();
 			String message = "Il existe deja des soumissions en cours " + mess 
 					+ " impliquant le study " + studyCode;
			throw new SraException("createSubmissionFromStudy", message);
		}
		submission = new Submission(user, study.projectCodes);
//		submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
		submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
		submission.traceInformation.creationDate = courantDate;

		// mettre à jour l'objet submission pour le cas d'une release de study :
//		submission.release = true;
		submission.type = Submission.Type.RELEASE;
		submission.refStudyCodes.add(study.code);
		submission.studyCode = study.code;		
		submission.state = new State(SRASubmissionStateNames.SUBR_N, contextValidation.getUser());
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code + "_release"; 	
		submission.ebiResult = "ebi_release_" + study.code + ".txt";
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// valider et sauver submission
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		Submission dbSubmission = submissionDAO.save(submission);
		// Recuperer object study pour mettre historique des state et traceInformation à jour:
		study.updateStateAndTrace(submission.state);
		studyDAO.update(DBQuery.is("code", study.code), 
				 DBUpdate.set("state", study.state)
						 .set("traceInformation", study.traceInformation));
		//logger.debug("sauvegarde dans la base de la soumission" + submission.code);
		return dbSubmission.code;
	}
		
}
