package sra.services;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.AnalysisAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.ReadsetAPI;
import controllers.sra.api.SampleAPI;
import controllers.sra.api.StudyAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authenticate;
import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import mail.MailServiceException;
import mail.MailServices;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import sra.services.EbiRetour;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;
//import workflows.sra.submission.SubmissionWorkflows;
//import fr.cea.ig.ngl.dao.api.sra.Tools;


// utilisation des DAO plutot que API car maj des objets pour releaseDate qui ne supportent pas validation si 
// anciens samples ou anciens experiments qui ne repondent pas aux nouvelles regles de validation.
// Ici juste ajout des AC et releaseDate donc pas de soucis si absence de validation.
public class EbiFileResponseServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(EbiFileResponseServices.class);

	private final NGLSUBConfig        config;
	private final SubmissionAPI       submissionAPI;
	private final StudyAPI            studyAPI;           
	private final StudyDAO            studyDAO;           

	private final ProjectAPI          projectAPI; 
	private final ProjectDAO          projectDAO; 
	private final AbstractStudyAPI    abstractStudyAPI;   
	private final AnalysisAPI         analysisAPI;   
	private final AnalysisDAO         analysisDAO;   

	private final SampleAPI           sampleAPI;         
	private final SampleDAO           sampleDAO;          // preferer update de sampleDAO (plutot que sampleAPI) sans validation car validation faite à la creation
    													  // et permet de recharger AC sur anciennes données plus forcement valides.
	private final AbstractSampleAPI   abstractSampleAPI;  

	private final ExperimentAPI       experimentAPI;  
	private final ExperimentDAO       experimentDAO;  

	private final ReadSetsAPI         laboReadsetsAPI;
	private final ReadsetAPI   	      readsetAPI;

	private final SubmissionNewAPITools submissionNewAPITools;

	@Inject
	public EbiFileResponseServices(
			NGLSUBConfig        config, 
			SubmissionAPI       submissionAPI,
			StudyAPI            studyAPI,
			StudyDAO            studyDAO,
			AnalysisAPI         analysisAPI,
			AnalysisDAO         analysisDAO,
			ProjectAPI          projectAPI,
			ProjectDAO          projectDAO,
			AbstractStudyAPI    abstractStudyAPI,
			SampleAPI           sampleAPI,
			SampleDAO           sampleDAO,
			AbstractSampleAPI   abstractSampleAPI,
			ExperimentAPI       experimentAPI,
			ExperimentDAO       experimentDAO,
			ReadSetsAPI   	    laboReadsetsAPI,
			ReadsetAPI   	    readsetAPI,
			SubmissionNewAPITools submissionNewAPITools
			) {
		this.config              = config;
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
		this.studyDAO            = studyDAO;
		this.analysisAPI         = analysisAPI;
		this.analysisDAO         = analysisDAO;
		this.projectAPI          = projectAPI;
		this.projectDAO          = projectDAO;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.sampleAPI           = sampleAPI;
		this.sampleDAO           = sampleDAO;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.experimentAPI       = experimentAPI;
		this.experimentDAO       = experimentDAO;
		this.laboReadsetsAPI     = laboReadsetsAPI;
		this.readsetAPI          = readsetAPI;
		this.submissionNewAPITools = submissionNewAPITools;
	}


	/**
	 * Parse le fichier de l'EBI recu en retour de l'envoie des metadonnées lors d'une soumission de type creation
	 * de données ou d'une soumission de type update de données.
	 * Met à jour les differents objets de la soumission pour les AC, les dates (creation, release),
	 * mais pas pour le status ou le TraceInformation 
	 * @param ctxVal                  contexte de validation
	 * @param submission              objet soumission
	 * @param ebiFileResp             fichier des AC
	 * @throws SraException           erreur
	 * @throws APIValidationException erreur
	 * @throws APIException           erreur
	 */
	//@SuppressWarnings("null")
	public void loadEbiRespForCreation(ContextValidation ctxVal, Submission submission, File ebiFileResp) throws SraException, APIValidationException, APIException {
		loadEbiRespForCreation(ctxVal, submission, ebiFileResp, true, true);
	}

	/**
	 * Parse le fichier de l'EBI recu en retour de l'envoie des metadonnées lors d'une soumission de type creation
	 * de données ou d'une soumission de type update de données.
	 * Met à jour les differents objets de la soumission pour les AC, les dates (creation, release),
	 * mais pas pour le status ou le TraceInformation 
	 * @param ctxVal                  context de validation
	 * @param submission              objet soumission
	 * @param ebiFileResp             fichier des AC
	 * @param sendMail                boolean indiquant s'il faut ou non envoyer un mail
	 * @param confidential            à false si les données sont soumises en public (cas Atlasea) sinon par defaut utiliser true
	 * @throws SraException           erreur
	 * @throws APIValidationException erreur
	 * @throws APIException           erreur
	 */
	//@SuppressWarnings("null")
	public void loadEbiRespForCreation(ContextValidation ctxVal, Submission submission, File ebiFileResp, boolean sendMail, boolean confidential) throws SraException, APIValidationException, APIException {
		//logger.debug("coucou   ok           dans loadEbiResp, ebiRileResp=" +ebiFileResp); 
		if (submission == null) {
			logger.debug("loadEbiResp :: parametres d'entree submission à null ??" );
			throw new SraException("EbiFileResponseServices.loadEbiRespForCreation :: parametres d'entree submission à null ??" );
		}


		//logger.debug("EbiFileResponseServices.loadEbiRespForCreation : ok on attaque parsing");
		String subjectError = null;
		String dest = null;
		String expediteur = null;
		String subjectSuccess = null;
		switch(submission.type) {
		case  CREATION :
			subjectError = config.getAccessionReportingEmailSubjectError(); 
			subjectSuccess = config.getAccessionReportingEmailSubjectSuccess();
			dest = config.getAccessionReportingEmailTo();
			expediteur = config.getAccessionReportingEmailFrom();
			break;
		case UPDATE :
			throw new SraException("Impossible d'utiliser la methode loadEbiRespForCreation " 
					+ "pour la soumission de type UPDATE "+ submission.code + "\nUtiliser la methode EbiFileResponseServices.loadRespEbiForUpdate\n");
		case RELEASE:
			throw new SraException("Impossible d'utiliser la methode loadEbiRespForCreation " 
					+ "pour la soumission de type RELEASE "+ submission.code + "\nUtiliser la methode submissionNewAPI.loadRespEbiForRelease\n");
		default:
			throw new SraException("Type de soumission non gerée :" + submission.type 
					+ " pour la soumission " + submission.code);
		}
		// si cond de prod
		MailServices mailService = new MailServices();
		Set<String> destinataires = new HashSet<>();
		destinataires.addAll(Arrays.asList(dest.split(",")));  

		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
		String user;
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
		} else {
			user = ctxVal.getUser();
			// user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data
		}
		String destinataire = user;
		if(StringUtils.isNotBlank(destinataire)) {
			destinataire = destinataire + "@genoscope.cns.fr";
			if (! destinataires.contains(destinataire)) {
				destinataires.add(destinataire);
			}
		}

		// end cond de prod

		EbiRetour ebiRetour = null;
		String message = "";
		try {
			ebiRetour = parseRetourEbi(ebiFileResp, submission);
			if(! ebiRetour.success) {
				message = "Absence de la ligne RECEIPT ... pour  " + submission.code + " dans le fichier "+ ebiRetour.getNameFileInput();
				throw new SraException(message);
			}
			assertEbiRetourValide(ebiRetour, submission);
			//logger.debug("XXXXXX Pas d'erreur dans parsing du rapport de soumission");
			message = updateDbForACAndReleaseDate(submission, ebiRetour, user, confidential);
		} catch (Exception e) {
			//logger.debug("loadEbiRespForCreation:: catch la SraException avec message = " + e.getMessage());
			//logger.debug("subjectError =" +subjectError);
			try {
				String errorMessage = "loadEbiRespForCreation: " + e.getMessage();
				logger.debug(errorMessage);
				if(sendMail) {
					mailService.sendMail(expediteur, destinataires, subjectError, new String(errorMessage.getBytes(), "iso-8859-1"));
				}
				throw new SraException(errorMessage, e);
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiRespForCreation:: Probleme lors de l'encodage du message d'erreur", e);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiRespForCreation:: Probleme lors de l'envoie du mail d'erreur", e);
			}
		}

		if (sendMail) {
			try {
				mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiRespForCreation:: Probleme lors de l'encodage du message de succes", e1);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiRespForCreation:: Probleme lors de l'envoie du mail de success", e1);
			}
		} else {
			logger.debug(message);
		}
	}

	
	
	
	/**
	 * Parse le fichier de l'EBI recu en retour de l'envoie des metadonnées lors d'une soumission de type creation
	 * de données ou d'une soumission de type update de données.
	 * Met à jour les differents objets de la soumission pour les AC, les dates (creation, release),
	 * mais pas pour le status ou le TraceInformation 
	 * @param ctxVal                  context de validation
	 * @param submission              objet soumission
	 * @param ebiFileResp             fichier des AC
	 * @param sendMail                boolean indiquant s'il faut ou non envoyer un mail
	 * @throws SraException           erreur
	 * @throws APIValidationException erreur
	 * @throws APIException           erreur
	 */
	//@SuppressWarnings("null")
	public void loadEbiRespForUpdate(ContextValidation ctxVal, Submission submission, File ebiFileResp, boolean sendMail) throws SraException, APIValidationException, APIException {
		//logger.debug("coucou   ok           dans loadEbiResp, ebiRileResp=" +ebiFileResp); 
		if (submission == null) {
			logger.debug("loadEbiRespForUpdate :: parametres d'entree submission à null ??" );
			throw new SraException("EbiFileResponseServices.loadEbiRespForUpdate :: parametres d'entree submission à null ??" );
		}

		//logger.debug("EbiFileResponseServices.loadEbiRespForUpdate : ok on attaque parsing");
		String subjectError = null;
		String dest = null;
		String expediteur = null;
		String subjectSuccess = null;
		switch(submission.type) {
		case UPDATE :
			subjectError = config.getUpdateReportingEmailSubjectError();
			subjectSuccess = config.getUpdateReportingEmailSubjectSuccess();
			dest = config.getUpdateReportingEmailTo();
			expediteur = config.getUpdateReportingEmailFrom();
			break;
		case  CREATION :
			throw new SraException("Impossible d'utiliser la methode loadEbiRespForUpdate " 
					+ "pour la soumission de type CREATION "+ submission.code + "\nUtiliser la methode submissionNewAPI.loadRespEbiForCreation\n");	
		case RELEASE:
			throw new SraException("Impossible d'utiliser la methode loadEbiRespForUpdate " 
					+ "pour la soumission de type RELEASE "+ submission.code + "\nUtiliser la methode submissionNewAPI.loadRespEbiForRelease\n");
		default:
			throw new SraException("Type de soumission non gerée :" + submission.type 
					+ " pour la soumission " + submission.code);
		}
		// si cond de prod
		MailServices mailService = new MailServices();
		Set<String> destinataires = new HashSet<>();
		destinataires.addAll(Arrays.asList(dest.split(",")));  

		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
		String user;
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
		} else {
			user = ctxVal.getUser();
			// user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data

		}
		String destinataire = user;
		if(StringUtils.isNotBlank(destinataire)) {
			destinataire = destinataire + "@genoscope.cns.fr";
			if (! destinataires.contains(destinataire)) {
				destinataires.add(destinataire);
			}
		}
		// end cond de prod

		
		EbiRetour ebiRetour = null;
		try {
			ebiRetour = parseRetourEbi(ebiFileResp, submission);
			assertEbiRetourValide(ebiRetour, submission);
			//logger.debug("le rapport de l'EBI est bien valide");
		} catch (SraException e) {
			//logger.debug("loadEbiResp:: catch la SraException");
			//logger.debug("subjectError =" +subjectError);
			String errorMessage = "loadEbiRespForUpdate: " + e.getMessage();
			logger.debug(errorMessage);
			try {
				//logger.debug(new String(e.getMessage().getBytes(), "iso-8859-1"));
				if(sendMail) {
					mailService.sendMail(expediteur, destinataires, subjectError, new String(errorMessage.getBytes(), "iso-8859-1"));
				}
				throw new SraException(errorMessage, e);
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message d'erreur", e1);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail d'erreur", e1);
			}
		}

		// Mise à jour dans la base de la soumission et de ses objets pour les traceInformations:
		String message = updateDbForUpdate(submission, ebiRetour, user);
		// Les differents objets ont ete mis à jour pour la date de release dans le service  UpdateServices lors de la creation de la soumission de type UPDATE
		
		if (sendMail) {
			try {
				mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message de succes", e1);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail de success", e1);
			}
		} else {
			logger.debug(message);
		}
	}

	/**
	 * Parse le fichier retour de l'EBI recu en reponse de l'envoie des metadonnées pour une release de study.
	 * Met a jour dans la base le study pour la date de release et la soumission pour la date de soumission.
	 * Ne met pas à jour les objets pour l'etat.
	 * Envoie un mail si demande
	 * @param  ctxVal                   context de validation
	 * @param  submission               objet soumission
	 * @param  ebiFileResp              fichier retour de l'ebi en reponse d'une soumission pour release d'un study
	 * @param  sendMail                 envoie mail si sendMail a true
	 * @throws SraException             error
	 * @throws APIException             error
	 * @throws APIValidationException   error
	 */
	public void loadEbiRespForRelease(ContextValidation ctxVal, Submission submission, File ebiFileResp, boolean sendMail) throws SraException, APIValidationException, APIException {
		if (submission == null) {
			logger.debug("loadEbiRespForUpdate :: parametres d'entree submission à null ??" );
			throw new SraException("EbiFileResponseServices.loadEbiRespForUpdate :: parametres d'entree submission à null ??" );
		}

		String subjectError = null;
		String dest = null;
		String expediteur = null;
		String subjectSuccess = null;
		switch(submission.type) {
			case RELEASE :
				// Get global parameters for email => utiliser Play.application().configuration().getString plutot que
				// ConfigFactory.load().getString pour recuperer les parametres pour avoir les surcharges de AbstractTest  
				// si tests unitaires 
				subjectError = config.getReleaseReportingEmailSubjectError();
				subjectSuccess = config.getReleaseReportingEmailSubjectSuccess();
				dest = config.getReleaseReportingEmailTo();
				expediteur = config.getReleaseReportingEmailFrom();
				break;
			case  CREATION :
				throw new SraException("Impossible d'utiliser la methode loadEbiRespForRelease " 
					+ "pour la soumission de type CREATION de " + submission.code + ". Utiliser la methode loadRespEbiForCreation\n");	
			case UPDATE:
				throw new SraException("Impossible d'utiliser la methode loadEbiRespForRelease " 
					+ "pour la soumission de type UPDATE de " + submission.code + ". Utiliser la methode loadRespEbiForUpdate\n");
			default:
				throw new SraException("Type de soumission non gerée :" + submission.type 
					+ " pour la soumission " + submission.code);
		}
		// si cond de prod
		
		
		MailServices mailService = new MailServices();
		Set<String> destinataires = new HashSet<>();
		destinataires.addAll(Arrays.asList(dest.split(",")));  

		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
		String user;
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
		} else {
			user = ctxVal.getUser();
			// user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data
		}
		String destinataire = user;
		if(StringUtils.isNotBlank(destinataire)) {
			destinataire = destinataire + "@genoscope.cns.fr";
			if (! destinataires.contains(destinataire)) {
				destinataires.add(destinataire);
			}
		}
		// end cond de prod

		EbiRetour ebiRetour = null;
		try {
			// chargement du rapport de soumission dans ebiRetour
			ebiRetour = parseRetourEbi(ebiFileResp, submission);
			// validation de ebiRetour 
			assertEbiRetourValide(ebiRetour, submission);
		} catch (SraException e) {
			String errorMessage = "loadEbiRespForRelease: " + e.getMessage();
			logger.debug(errorMessage);
			try {
				if(sendMail) {
					mailService.sendMail(expediteur, destinataires, subjectError, new String(errorMessage.getBytes(), "iso-8859-1"));
				}
				//throw new SraException(new String(e.getMessage().getBytes(), "iso-8859-1"));
				throw new SraException(errorMessage, e);
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message d'erreur", e1);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail d'erreur", e1);
			}
		}

		// Mise à jour dans la base de la soumission et de ses objets pour les traceInformations:
		String message = updateDbForRelease(submission, ebiRetour, user);
		// Les differents objets ont ete mis à jour pour la date de release dans le service  UpdateServices lors de la creation de la soumission de type UPDATE
		
		if (sendMail) {
			try {
				mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message de succes", e1);
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail de success", e1);
			}
		} else {
			logger.debug(message);
		}

	}


	public String updateDbForRelease(Submission submission, EbiRetour ebiRetour, String user) throws APIValidationException, APIException {
		
		Study study = null;
		Sample sample = null;
		// verifier coherence infos dans submission et dans base :
		if(StringUtils.isNotBlank(submission.studyCode)) {
			if(! studyAPI.isObjectExist(submission.studyCode)) {
				throw new SraException("soumission avec studyCode" + submission.studyCode + " qui n'existe pas dans la base");
			}
			study = studyAPI.get(submission.studyCode);
		} else if (submission.sampleCodes != null && StringUtils.isNotBlank(submission.sampleCodes.get(0))) {
			if(! sampleDAO.isObjectExist(submission.sampleCodes.get(0))) {
				throw new SraException("soumission avec sampleCode" + submission.sampleCodes.get(0) + " qui n'existe pas dans la base");
			}	
			sample = sampleDAO.getObject(submission.sampleCodes.get(0));
			//logger.debug("Release du sample " + sample.code);
		} else {
			throw new SraException("soumission " + submission.code + " sans study ou sample a releaser");
		}
		// verifier validité de ebiRetour :
		assertEbiRetourValide(ebiRetour, submission);
		
		String  message         = null;
		
		// Mise à jour dans la base de la date d'envoie de la soumission et de la date de release du study
		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();
		Date releaseDate = ebiRetour.getSubmissionDate();

		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
				DBUpdate.set("submissionDate", date)
				.set("traceInformation.modifyUser", user)
				.set("traceInformation.modifyDate", date));	
		
		if(study != null) {
			message = "Objets lies au studyAccession = " + study.accession + " mis dans le domaine public via la soumission " + submission.code + "</br>"; 

			// Mise à jour du study et sauvegarde en base:
			study.releaseDate                 = releaseDate;           // c'est la date qui figure dans le rapport de soumission de l'EBI 
			study.consolidatedPublicStatus    = true;   // sgas
			study.traceInformation.modifyDate = date;
			study.traceInformation.modifyUser = user;
			studyDAO.update(study);
			propagePublicReleaseDateInBaseForExpAnalysisSamples(study.code, study.accession, user, releaseDate);
		} else if(sample != null) {
			message = "Sample " + sample.accession + " mis dans le domaine public via la soumission " + submission.code + "</br>"; 
			sample.releaseDate                 = releaseDate;           // c'est la date qui figure dans le rapport de soumission de l'EBI 
			sample.consolidatedPublicStatus    = true;
			sample.traceInformation.modifyDate = date;
			sample.traceInformation.modifyUser = user;
			sampleDAO.update(sample);
		} else {
			// cas non prevu : Pas de sauvegarde en base 
			throw new SraException("Cas non prevu : Soumission pour release sans study ou sample ?");
		}
		return message;
	}
	
	
	// utilisée à la reception du rapport de soumission dans le cas d'une soumission de type CREATION
	public String updateDbForACAndReleaseDate(Submission submission, EbiRetour ebiRetour, String user, boolean confidential) throws APIValidationException, APIException {
		//logger.debug("Dans updateDbForACAndReleaseDate");
		HashMap<String, Sample>       hSampleToUpdateForReleaseDate = new HashMap<String, Sample>();

		// calendar initialisé avec date du jour ou submissionDate du rapport de soumission
		Calendar calendar = Calendar.getInstance();
		Date dateNow = calendar.getTime();
		Date submissionDate = ebiRetour.getSubmissionDate(); // date de soumission yyyy-MM-DD + 6 heures
		if (submissionDate != null) {
			calendar.setTime(ebiRetour.getSubmissionDate());
		} else {
			calendar.setTime(dateNow);
		}
		if(confidential) {
			// cas privé, on ajoute 2 ans pour date de confidentialite
			calendar.add(Calendar.YEAR, 2);
		} else {
			// cas public, on ajoute rien
		}
		Date release_date  = calendar.getTime();
		//logger.debug("release_date=" + release_date);
	
		String message = "Liste des AC attribues pour la soumission "  + submission.code + " en mode confidentiel jusqu'au : " + release_date +" <br/><br/>\n";
		String header_umbrella = "Liste des AC attribues pour la soumission "  + submission.code + " en mode public <br/><br/>\n";
		//		String retourChariot = "\n";

		message += "submissionCode = " + submission.code + ",   AC = "+ ebiRetour.submissionAc + "</br>";  
		//logger.debug(message);
		submission.accession = ebiRetour.submissionAc;

		//logger.debug("Mise à jour de la soumission {} avec AC={} ", ebiRetour.submissionCode, ebiRetour.submissionAc);

		// Renseigner la submission pour le numeros d'accession et sauver en base :
		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
				//							                             DBQuery.notExists("accession")),
				DBUpdate.set("accession", ebiRetour.submissionAc)
				.set("submissionDate", submissionDate)
				.set("traceInformation.modifyUser", user)
				.set("traceInformation.modifyDate", dateNow));	

		// On ne met rien pour project car on ne veut pas faire une soumission de création du project,
		// on veut que l'EBI cree automatiquement le project de type SEQUENCING à la creation du study
		// en revanche il faut traiter la creation d'un project UMBRELLA:
		if (StringUtils.isNotBlank(submission.umbrellaCode)) {
			message = header_umbrella;
			message += "umbrellaCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + "<br/>\n";
			//logger.debug("Mise à jour du project umbrella {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);
			updateDbforAcCaseUmbrella (ebiRetour, submission.umbrellaCode, user, submissionDate);
		}


		// important de sauver study avant analyses et experiments car intervient dans releaseDate des exp et analyses de la soumission et calcul fait en fct du study en base.
		if (StringUtils.isNotBlank(submission.studyCode)) {	
			message += "studyCode = " + ebiRetour.studyCode + ",   AC = "+ ebiRetour.studyAc + ", bioProjectId = " + ebiRetour.studyExtId + "<br/>\n";
			//logger.debug("ccccccccccccccccccccccc            Mise à jour du study {} avec AC={} et externalId={}", ebiRetour.studyCode, ebiRetour.studyAc, ebiRetour.studyExtId);
			updateDbForAcAndReleaseDateCaseStudy(ebiRetour, submission.studyCode, user, submissionDate, release_date);
		}

		// important de renseigner l'analyse de la soumission pour releaseDate après avoir sauver le study 
		if (StringUtils.isNotBlank(submission.analysisCode)) {
			message += "analysisCode = " + ebiRetour.analysisCode + ",   AC = "+ ebiRetour.analysisAc + "<br/>\n";
			// mise à jour dans base de l'analyse pour AC et releaseDate et pour readsets de l'analyse et determination du sample pour calclu de releaseDate
			updateDbForAcAndReleaseDateCaseAnalysis(ebiRetour, submission.analysisCode, user, submissionDate, release_date, hSampleToUpdateForReleaseDate);
		}

		// important de renseigner les experiments de la soumission pour releaseDate après avoir sauver le study 
		for (String expCode : submission.experimentCodes) {
			String ac = ebiRetour.getAccessionExperiment(expCode);
			message += "experimentCode = " + expCode + ",   AC = "+ ac + "<br/>\n";  
			//logger.debug("cccccccccccccc          Mise à jour de l'exp {} avec AC={} ", expCode, ac);
			updateDbForAcAndReleaseDateCaseExperiment(ebiRetour, expCode, user, submissionDate, release_date, hSampleToUpdateForReleaseDate);

		}
		// les runs sont contenus dans la collection Experiments donc simple update de l'exp mis a jour precedemment pour releaseDate
		for (String code : submission.runCodes) {
			String ac = ebiRetour.getAccessionRun(code);
			message += "runCode = " + code + ",   AC = "+ ac  + "<br>\n";  
			//logger.debug("ccccccccccccccc       Mise à jour du run {} avec AC={}", code, ac);
			//			experimentAPI.dao_update(DBQuery.is("run.code", code),
			//			DBUpdate.set("run.accession", ac)); 		
			Experiment experiment = experimentAPI.dao_findOne(DBQuery.is("run.code", code));
			experiment.run.accession = ac;			
			//experimentAPI.update(experiment, user);
			experimentDAO.update(experiment);
			// mettre a jour la collection readset pour runAccession :
			readsetAPI.dao_update(DBQuery.is("runCode", code), 
					DBUpdate.set("runAccession", ac));
		}

		// important de renseigner les samples de la soumission pour releaseDate après avoir renseignes et sauvés les analyses et experiments de la soumission
		for (String sampleCode : submission.sampleCodes) {
			String ac = ebiRetour.getAccessionSample(sampleCode);
			String extId = ebiRetour.getExtIdSample(sampleCode);
			message += "sampleCode = " + sampleCode + ",   AC = "+ ac + " , externalId = " + extId + "<br/>" + "\n";  
			//logger.debug("cccccccccccccccccccccc                        Mise à jour du sample {} avec AC={} et externalId={}", sampleCode, ac, extId);
			updateDbForAcAndReleaseDateCaseSample(ebiRetour, sampleCode, user, submissionDate, release_date);
		}

		// NGL-4373 : si update des experiments ou analyse pour releaseDate alors, il faut calculer les releaseDates des samples references par ces exp et analyses
		// Comme tous les exp et analyse de la soumission sont en base avec leurs releaseDate, et comme pas de dereferencement de sample par les analyses
		// ou les experiments, on appelle la methode calculateSampleReleaseDate avec des hash vides.
		HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
		// important de mettre context undefined plutot que update car validation des vieux samples
		// en mode undefined qui ne verifie pas presence attributs collectionDate et geographic 
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);		
		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			submissionNewAPITools.calculateSampleReleaseDate(setSample.getValue(), 
					hSampleReferenceByExpCodeExp, 
					hSampleDereferenceByExpCodeExp,
					hSampleReferenceByAnalysisCodeAnalysis,
					hSampleDereferenceByAnalysisCodeAnalysis,
					contextValidation);
			SraException.assertNoError(contextValidation);
			// on peut avoir des anciens samples à ce niveau qui ne repondent pas à la validation. Ici preferer DAO

			// attention a ne pas faire un update du sample complet car AC et externalId pas forcement dans setSample.getValue()
			// updater uniquement la releaseDate :
			sampleDAO.update(DBQuery.is("code", setSample.getValue().code), 
					DBUpdate.set("releaseDate", setSample.getValue().releaseDate)
					.set("consolidatedPublicStatus", setSample.getValue().consolidatedPublicStatus));

		}	

		submission = submissionAPI.get(submission.code);
		return message;
	}


	public void updateDbforAcCaseUmbrella (EbiRetour ebiRetour, String umbrellaCode, String user, Date submissionDate) throws APIValidationException, APIException {
		Project umbrellaProject = projectAPI.get(umbrellaCode);
		if (umbrellaProject == null) {
			//logger.debug(" " + submission.umbrellaCode + " absent de la base");
			throw new SraException("umbrellaProject " + umbrellaCode + " absent de la base");
		}
//		if (StringUtils.isNotBlank(umbrellaProject.accession)) {
//			//logger.debug("Dans la base, le project " + submission.umbrellaCode + " contient deja un AC");
//			throw new SraException("Dans la base, le project " + umbrellaCode + " contient deja un AC");
//		}
		// tous les projects umbrella sont déclarés en public. Pas de date de release sur l'objet
		umbrellaProject.accession = ebiRetour.projectAc;
		umbrellaProject.firstSubmissionDate = submissionDate;
		umbrellaProject.traceInformation.modifyUser = user;
		umbrellaProject.traceInformation.modifyDate = new Date();

		//projectAPI.update(umbrellaProject, user);

		projectDAO.update(umbrellaProject);

	}


	public void updateDbForAcAndReleaseDateCaseStudy(EbiRetour ebiRetour, String studyCode, String user, Date submissionDate, Date releaseDate) throws APIValidationException, APIException {
		if( ! studyAPI.isObjectExist(studyCode)) {
			throw new SraException("updateDbForAC ", " le study " + studyCode + " absent de la base");
		}
		Study study = studyAPI.dao_getObject(studyCode);
		//logger.debug("tititititititititititi   studyAc=" + ebiRetour.studyAc);
		//logger.debug("tititititititititititi   studyExtId=" + ebiRetour.studyExtId);

		study.accession = ebiRetour.studyAc;
		study.externalId =  ebiRetour.studyExtId;
		study.firstSubmissionDate = submissionDate;
		study.releaseDate = releaseDate;
		study.traceInformation.modifyUser = user;
		study.traceInformation.modifyDate = new Date();
		
		// indiquer les locus_tag comme connus à l'EBI
		if(study.locusTagPrefixs != null && study.locusTagPrefixs.size() > 0) {
			if ( study.ebiLocusTagPrefixs == null ) {
				study.ebiLocusTagPrefixs = new ArrayList<String>();
			}
			for (String locusTag : study.locusTagPrefixs) {
				if(!study.ebiLocusTagPrefixs.contains(locusTag)) {
					study.ebiLocusTagPrefixs.add(locusTag);
				}
			}
		}
		//logger.debug("XXXXXXXXXXXXXXXXXXXX  false, releaseDate="+releaseDate +" , date="+ new Date());

		if(submissionNewAPITools.isPublicAbstractStudy(study)) {
			study.consolidatedPublicStatus = true;
		} else {
			study.consolidatedPublicStatus = false;
		}
		if(study.ebiLocusTagPrefixs != null && study.ebiLocusTagPrefixs.size() > 0) {
			// mettre à jour readset de ngl-sub pour studyAccession et studyExternalId :
			MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
			DBQuery.is("studyCode", study.code),
				DBUpdate
				.set("state", study.state)
				.set("traceInformation.modifyUser", study.traceInformation.modifyUser)
				.set("traceInformation.creationDate", study.traceInformation.modifyDate)
				.set("ebiLocusTagPrefixs", study.ebiLocusTagPrefixs)
				.set("studyAccession", study.accession)
				.set("studyExternalId", study.externalId));
		} else {
			MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
			DBQuery.is("studyCode", study.code),
				DBUpdate
				.set("state", study.state)
				.set("traceInformation.modifyUser", study.traceInformation.modifyUser)
				.set("traceInformation.creationDate", study.traceInformation.modifyDate)
				.set("studyAccession", study.accession)
				.set("studyExternalId", study.externalId));			
		}
		//logger.debug("Pour study, consolidatedPublicStatus = " + study.consolidatedPublicStatus);
		// NGL-4373
		// si study dans soumission, alors seuls les experiments ou analyses de la soumission referencent ce study. Aucun exp ou analysis de la base ne referencent encore ce study.
		// On sauve le study avec sa date de release et on traitera la releaseDate des exp ou analyse dans le cas ou exp ou analyse present dans objet submission 
		//studyAPI.update(study, user);
		studyDAO.update(study);

	}		



	private void updateDbForAcAndReleaseDateCaseAnalysis(EbiRetour ebiRetour, String analysisCode, String user, Date submissionDate, Date releaseDate, HashMap<String, Sample> hSampleToUpdateForReleaseDate) throws APIValidationException, APIException {

		//logger.debug("Mise à jour de l'analyse {} avec AC={} ", ebiRetour.analysisCode, ebiRetour.analysisAc);
		if( ! analysisAPI.isObjectExist(analysisCode)) {
			throw new SraException("updateDbForAC ", " l'analyse " + analysisCode + " absent de la base");
		}
		Analysis analysis = analysisAPI.get(analysisCode);

//		if (StringUtils.isNotBlank(analysis.accession)) {
//			//logger.debug("Dans la base, l'analyse " + submission.analysisCode + " contient deja un AC");
//			throw new SraException("Dans la base, l'analyse " + analysisCode + " contient deja un AC");
//		}	
		analysis.accession = ebiRetour.analysisAc;
		analysis.firstSubmissionDate = submissionDate;
		analysis.releaseDate = releaseDate;
		analysis.traceInformation.modifyUser = user;
		analysis.traceInformation.creationDate = new Date();

		// Renseigner analysis.sampleAccession et analysis.studyAccession si besoin.
		// En janv 2024, inutile car creation analysis avec sampleAccession et studyAccession
		if(StringUtils.isNotBlank(ebiRetour.getAccessionStudy())) {
			analysis.studyIdentifier = ebiRetour.getAccessionStudy();
		} 
		if (! abstractStudyAPI.isObjectExist(analysis.studyCode)) {
			throw new SraException("l'analyse " + analysis.code + "reference le studyCode " + analysis.studyCode +" qui n'existe pas dans base");				
		}	
		AbstractStudy abstStudy = abstractStudyAPI.get(analysis.studyCode);
		if(StringUtils.isBlank(analysis.studyIdentifier)) {
			analysis.studyIdentifier = abstStudy.accession;
		}
		// calcul de la releaseDate de l'analyse
		if(submissionNewAPITools.isPublicAbstractStudy(abstStudy)) {
			analysis.releaseDate = submissionDate;
			analysis.consolidatedPublicStatus = true;
		} else {
			analysis.releaseDate = ((Study) abstStudy).releaseDate;
			analysis.consolidatedPublicStatus = false;
		}

		if(StringUtils.isBlank(analysis.sampleIdentifier) && StringUtils.isNotBlank(ebiRetour.getAccessionSample(analysis.sampleCode))) {
			analysis.sampleIdentifier = ebiRetour.getAccessionSample(analysis.sampleCode);
		} 
		if (! abstractSampleAPI.isObjectExist(analysis.sampleCode)) {
			throw new SraException("l'analyse " + analysis.code + "reference le sampleCode " + analysis.sampleCode +" qui n'existe pas dans base");				
		}	
		AbstractSample abstSample = abstractSampleAPI.get(analysis.sampleCode);
		if(StringUtils.isBlank(analysis.sampleIdentifier)) {
			analysis.sampleIdentifier = abstSample.accession;
		}
		// ajout du sample dans liste des samples pour calcul de releaseDate en fonction des données dans base
		if(submissionNewAPITools.isPublicAbstractSample(abstSample)) {
			// Le sample est deja public en base, il reste public. Rien à recalculer
		} else {
			hSampleToUpdateForReleaseDate.put(analysis.sampleCode, (Sample) abstSample);
		}

		// sauver l'analyse
		//analysisAPI.update(analysis, user);
		analysisDAO.update(analysis);

		// updater les readsets de l'analyse en base :
		for (RawData rawData : analysis.listRawData) {		
			if(StringUtils.isNotBlank(rawData.readsetCode)) {
				// Creer le readset dans la collection sra_readset:
				Readset readset = new Readset();
				readset.code = rawData.readsetCode;
				readset.analysisCode = analysis.code;
				readset.type = "bionano";
				if (! readsetAPI.dao_checkObjectExist("code", rawData.readsetCode)) {
					readsetAPI.dao_saveObject(readset);
				}
				// Updater le readset dans la collection ngl-bi:
				if (! laboReadsetsAPI.isObjectExist(readset.code)) {
					throw new SraException("readset " + readset.code+ " de type " + readset.type + " absent de la table ngl_bi.ReadSetIllumina");
				} 
				laboReadsetsAPI.dao_update(DBQuery.is("code", rawData.readsetCode), 
						DBUpdate.set("submissionState.code", SUB_F)
						.set("submissionState.date", submissionDate)
						.set("submissionState.user", user)
						.set("submissionDate", submissionDate)
						//.set("submissionUser", user) fait au moment du passage au status SUB-N avec updateReadSetAndSaveIndbForCreation et ne doit pas etre ecrase ici
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
				
				// updater readset dans ngl-sub  :
				if(! readsetAPI.dao_checkObjectExist("code", rawData.readsetCode)) {
					throw new SraException("le readset " + rawData.readsetCode + " n'existe pas dans la base de données");
				}
				//Readset rs = readsetAPI.get(rawData.readsetCode);
				readsetAPI.dao_update(DBQuery.is("code", readset.code), 
						DBUpdate.set("state", analysis.state)
						.set("traceInformation", analysis.traceInformation)
						.set("firstSubmissionDate", analysis.firstSubmissionDate)
						.set("releaseDate", analysis.releaseDate)
						.set("consolidatedPublicStatus", analysis.consolidatedPublicStatus)
						.set("analysisCode", analysis.code)
						.set("analysisAccession", analysis.accession)
						.set("studyCode", abstStudy.code)
						.set("studyAccession", abstStudy.accession)
						.set("studyExternalId", abstStudy.externalId)
						.set("sampleCode", abstSample.code)
						.set("sampleAccession", abstSample.accession)
						.set("sampleExternalId", abstSample.externalId));
				
			}
		}
	}

	public void updateDbForAcAndReleaseDateCaseExperiment(EbiRetour ebiRetour, String expCode, String user, Date submissionDate, Date releaseDate, HashMap<String, Sample> hSampleToUpdateForReleaseDate) throws APIValidationException, APIException {

		if( ! experimentAPI.isObjectExist(expCode)) {
			throw new SraException("experiment ", expCode + " absent de la base");
		}
		Experiment experiment = experimentAPI.get(expCode);
		experiment.accession = ebiRetour.getAccessionExperiment(experiment.code);
		experiment.firstSubmissionDate = submissionDate;
		experiment.traceInformation.modifyUser = user;
		experiment.traceInformation.modifyDate = new Date();	

		// recuperer le readset de la collection ngl-sub
		if(! readsetAPI.dao_checkObjectExist("code", experiment.readSetCode)) {
			throw new SraException("le readset " + experiment.readSetCode + " n'existe pas dans la base de données");
		}
		Readset readset = readsetAPI.get(experiment.readSetCode);
		//On veut permettre de pouvoir recharger le fichier des AC => retrait de la condition experiment sans AC
		//if (StringUtils.isNotBlank(experiment.accession)) {
			//logger.debug("Dans la base l' experiment " + code + " contient deja un AC");
			//throw new SraException("Dans la base l' experiment " + expCode + " contient deja un AC");
		//}
		// completer studyAc. 
		// Soit Study de type External et existe dans base à la création de la soumission, 
		// soit Study deja soumis par nous et existe dans base à la création de la soumission
		// soit Study dans la soumission courante et sauve dans base avec son numeros d'accession
		if(! abstractStudyAPI.isObjectExist(experiment.studyCode)) {
			logger.debug("updateDbForAcAndReleaseDateCaseExperiment ", " experiment "+ experiment.code + " avec le studyCode " + experiment.studyCode + " qui n'existe pas dans la base");
			throw new SraException("updateDbForAcAndReleaseDateCaseExperiment ", " experiment "+ experiment.code + " avec le studyCode " + experiment.studyCode + " qui n'existe pas dans la base");
		}
		AbstractStudy abstStudy = abstractStudyAPI.get(experiment.studyCode);
		// Ne pas modifier studyAccession de l'utilisateur si renseigné qui deviendra studyIdentifier
		// Chercher accession du study dans ebiRetour car study pas forcement sauvé avec ses AC si soumis dans soumission courante
        // si study dans autre soumission, alors study.accession existe en base
		if (StringUtils.isNotBlank(ebiRetour.getAccessionStudy())) {
			readset.studyAccession = ebiRetour.getAccessionStudy();
			if(StringUtils.isBlank(experiment.studyIdentifier)) {
				experiment.studyIdentifier = ebiRetour.getAccessionStudy();
			}
		}
		if(StringUtils.isBlank(experiment.studyIdentifier) && StringUtils.isNotBlank(abstStudy.accession)) {
			experiment.studyIdentifier = abstStudy.accession;
		}

		// NGL-4373 : releaseDate
		if(submissionNewAPITools.isPublicAbstractStudy(abstStudy)) {
			experiment.consolidatedPublicStatus = true;
			experiment.releaseDate = submissionDate;
		} else {
			experiment.releaseDate = ((Study) abstStudy).releaseDate;
			experiment.consolidatedPublicStatus = false;
		}

		// completer sampleAc. 
		// Soit Sample de type External et existe dans base à la création de la soumission, 
		// soit Sample deja soumis par nous et existe dans base à la création de la soumission
		// soit Sample dans la soumission courante et existe dans base mais sans AC.

		if(! abstractSampleAPI.isObjectExist(experiment.sampleCode)) {
			throw new SraException("updateDbForAcAndReleaseDateCaseExperiment ", " experiment "+ experiment.code + " avec sampleCode " + experiment.sampleCode + " n'existe pas dans la base");
		}
		AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
		// Ne pas modifier sampleAccession de l'utilisateur si renseigné qui deviendra sampleIdentifier
		// Chercher accession du sample dans ebiRetour car sample pas encore sauvé avec ses AC si soumis dans soumission courante
		if(StringUtils.isNotBlank(ebiRetour.getAccessionSample(experiment.sampleCode))) {
			readset.sampleAccession = ebiRetour.getAccessionSample(experiment.sampleCode);
			if(StringUtils.isBlank(experiment.sampleIdentifier)) {
				experiment.sampleIdentifier = ebiRetour.getAccessionSample(experiment.sampleCode);
			}
		}
		if(StringUtils.isNotBlank(ebiRetour.getExtIdSample(experiment.sampleCode))) {
			readset.sampleExternalId = ebiRetour.getExtIdSample(experiment.sampleCode);
			if(StringUtils.isBlank(experiment.sampleIdentifier)) {
				experiment.sampleIdentifier = ebiRetour.getExtIdSample(experiment.sampleCode);
			}
		}
		
		if (StringUtils.isBlank(experiment.sampleIdentifier) && StringUtils.isNotBlank(abstSample.accession)) {
			experiment.sampleIdentifier = abstSample.accession;
		} 
		// Ne pas declencher d'erreur car exp.sampleAccession sera rempli dans DatabaseConsistencyTools.completeDatabaseForExternalSampleAndStudy()
		//cas ou exp.sampleCode = externalSample_SAMEA112751371 et exp.sampleAccession=SAMEA112751371 et ou 
		// sample.accession = null ou l'AC du sample sera recuperé après la soumission
		

		//NGL-4373
		if(submissionNewAPITools.isPublicAbstractSample(abstSample)) {
			// rien à recalculer meme si rechargement du fichier
		} else {
			hSampleToUpdateForReleaseDate.put(experiment.sampleCode, (Sample) abstSample);
		}	

		//experimentAPI.update(experiment, user);
		experimentDAO.update(experiment);

		// updater le readset dans la collection sra_readset de ngl-sub :		
		readsetAPI.dao_update(DBQuery.is("code", readset.code), 
				DBUpdate.set("state", experiment.state)
				.set("traceInformation", experiment.traceInformation)
				.set("firstSubmissionDate", experiment.firstSubmissionDate)
				.set("releaseDate", experiment.releaseDate)
				.set("consolidatedPublicStatus", experiment.consolidatedPublicStatus)
				.set("experimentCode", experiment.code)
				.set("experimentAccession", experiment.accession)
				.set("runCode", experiment.run.code)
				//.set("runAccession", experiment.run.accession) sera mis au moment ou runAccession mis dans experiment
				.set("studyCode", abstStudy.code)
				.set("studyAccession", abstStudy.accession)
				.set("studyExternalId", abstStudy.externalId)
				.set("sampleCode", abstSample.code)
				.set("sampleAccession", abstSample.accession)
				.set("sampleExternalId", abstSample.externalId));

		// updater collection readset de ngl-bi.ReadSetIllumina si autre que 454 ,
		if ( ! readset.type.equalsIgnoreCase("ls454") ) {
			if (! laboReadsetsAPI.isObjectExist(readset.code)) {
				throw new SraException("readset " + readset.code+ " de type " + readset.type + " absent de la table ngl_bi.ReadSetIllumina");
			} 
			laboReadsetsAPI.dao_update(DBQuery.is("code", experiment.readSetCode), 
					DBUpdate.set("submissionState.code", SUB_F)
					.set("submissionState.date", submissionDate)
					.set("submissionState.user", user)
					.set("submissionDate", submissionDate)
					//.set("submissionUser", user) //fait au moment du passage au status SUB-N avec updateReadSetAndSaveIndbForCreation et ne doit pas etre ecrase ici
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));
		}

	}


	public void updateDbForAcAndReleaseDateCaseSample(EbiRetour ebiRetour, String sampleCode, String user, Date submissionDate, Date releaseDate) throws APIValidationException, APIException {
		
		if( ! sampleAPI.isObjectExist(sampleCode)) {
			throw new SraException("sample ", sampleCode + " absent de la base");
		}
		Sample sample = sampleAPI.dao_getObject(sampleCode);
//		if (StringUtils.isNotBlank(sample.accession)) {
//			//logger.debug("Dans la base le sample " + code + " contient deja un AC");
//			throw new SraException("Dans la base le sample " + sampleCode + " contient deja un AC");
//		}
		String ac = ebiRetour.getAccessionSample(sampleCode);
		String extId = ebiRetour.getExtIdSample(sampleCode);
		sample.accession = ac;
		sample.externalId = extId;
		sample.firstSubmissionDate = submissionDate;
		sample.traceInformation.modifyUser = user;
		sample.traceInformation.modifyDate = new Date();
		//logger.debug("yyyyyyyyyyyyyy         dans updateDbForAcAndReleaseDateCaseSample, sample.code='{}', sample.accession='{}', sample.externalId='{}'", sample.code, sample.accession, sample.externalId);
		// NGL-4373 : mise à jour du sample 
		// Comme tous les exp et analyse de la soumission sont en base avec leurs releaseDate, et comme pas de dereferencement de sample par les analyses
		// ou les experiments, on appelle la methode calculateSampleReleaseDate avec des hash vides.
		HashMap<String, HashMap<String,Experiment>> hSampleReferenceByExpCodeExp             = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Experiment>> hSampleDereferenceByExpCodeExp           = new HashMap<String,HashMap<String,Experiment>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleReferenceByAnalysisCodeAnalysis   = new HashMap<String,HashMap<String,Analysis>>();
		HashMap<String, HashMap<String,Analysis>>   hSampleDereferenceByAnalysisCodeAnalysis = new HashMap<String,HashMap<String,Analysis>>();
		// important de mettre context undefined plutot que update car validation des vieux samples
		// en mode undefined qui ne verifie pas presence attributs collectionDate et geographic 
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);	
		submissionNewAPITools.calculateSampleReleaseDate(sample, 
				hSampleReferenceByExpCodeExp, 
				hSampleDereferenceByExpCodeExp,
				hSampleReferenceByAnalysisCodeAnalysis,
				hSampleDereferenceByAnalysisCodeAnalysis,
				contextValidation);

		SraException.assertNoError(contextValidation);
		// on peut laisser sampleAPI plutot que sampleDAO car appelé uniquement sur des nouveaux samples qui passent la validation.
		// sauf si rechargement des AC sur anciens samples et inutile de faire validation à la reception des AC, la validation a ete faite a la creation des samples
		//sampleAPI.update(sample, user);
		//logger.debug("avant update dans base, sample '{}' avec AC '{}', externalId '{}'", sample.code, sample.accession, sample.externalId);
		sampleDAO.update(sample);
		//Sample sampledb = sampleDAO.findByCode(sample.code);
		//logger.debug("yyyyyyyyyyy     dans base, sample '{}' avec AC '{}', externalId '{}'", sampledb.code, sampledb.accession, sampledb.externalId);
		// mettre à jour readset de ngl-sub pour studyAccession et studyExternalId :
		MongoDBDAO.update(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, 
				DBQuery.is("sampleCode", sample.code),
				DBUpdate
				.set("state", sample.state)
				.set("traceInformation.modifyUser", sample.traceInformation.modifyUser)
				.set("traceInformation.creationDate",sample.traceInformation.modifyDate)
				.set("sampleAccession", sample.accession)
				.set("sampleExternalId", sample.externalId));
	}


	// utilisée à la reception du rapport de soumission dans le cas d'une soumission de type update
	public String updateDbForUpdate(Submission submission, EbiRetour ebiRetour, String user) throws APIValidationException, APIException {
		//Calendar calendar = Calendar.getInstance();
		//Date date  = calendar.getTime();
		Date date= ebiRetour.getSubmissionDate();
		String message = "Liste des donnees mises à jour avec  la soumission "  + submission.code + "<br><br>\n";
		//logger.debug(message);

		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
				DBUpdate.set("submissionDate", date)
				.set("traceInformation.modifyUser", user)
				.set("traceInformation.modifyDate", date));	

		if (StringUtils.isNotBlank(submission.studyCode)) {	
			if( ! studyAPI.isObjectExist(submission.studyCode)) {
				throw new SraException("updateDbForUpdate ", " le study " + submission.studyCode + " est absent de la base");
			}
			Study study = studyAPI.get(submission.studyCode); 
			// indiquer les locus_tag comme connus à l'EBI
			if(study.locusTagPrefixs != null && study.locusTagPrefixs.size() > 0) {
				if ( study.ebiLocusTagPrefixs == null ) {
					study.ebiLocusTagPrefixs = new ArrayList<String>();
				}
				for (String locusTag : study.locusTagPrefixs) {
					if(!study.ebiLocusTagPrefixs.contains(locusTag)) {
						study.ebiLocusTagPrefixs.add(locusTag);
					}
				}
			}
			study.traceInformation.modifyUser = user;
			study.traceInformation.modifyDate = date;			
			message += "studyCode = " + ebiRetour.studyCode + ",   AC = "+ ebiRetour.studyAc + ", bioProjectId = " + ebiRetour.studyExtId + "\n";
			//logger.debug("Mise à jour du study {} avec AC={} et externalId={}", ebiRetour.studyCode, ebiRetour.studyAc, ebiRetour.studyExtId);
			if(study.ebiLocusTagPrefixs != null && study.ebiLocusTagPrefixs.size() > 0) {
				studyAPI.dao_update(DBQuery.is("code",study.code),
					DBUpdate.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", date)
					.set("ebiLocusTagPrefixs", study.ebiLocusTagPrefixs));
			} else {
				studyAPI.dao_update(DBQuery.is("code",study.code),
					DBUpdate.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", date));			}
		}
		// si on a une soumission de type creation pour un bioproject umbrella :
		if (StringUtils.isNotBlank(submission.umbrellaCode)) {	
			message += "umbrellaProjectCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + "\n";
			//logger.debug("Mise à jour du project {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);
			projectAPI.dao_update(DBQuery.is("code", ebiRetour.projectCode),
					DBUpdate.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", date));	
		}
		// Si on avait une soumission de type creation pour un bioproject classique mais pas d'actualité :
		//		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {	
		//			message += "projectCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + "\n";
		//			logger.debug("Mise à jour du project {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);
		//			projectAPI.dao_update(DBQuery.is("code", ebiRetour.projectCode),
		//					DBUpdate.set("traceInformation.modifyUser", user)
		//							.set("traceInformation.modifyDate", date));
		//		}
		for (String code : submission.sampleCodes) {
			String ac = ebiRetour.getAccessionSample(code);
			String extId = ebiRetour.getExtIdSample(code);
			message += "sampleCode = " + code + ",   AC = "+ ac + " , externalId = " + extId + " <br/>" + " \n";  
			//logger.debug("Mise à jour du sample {} avec AC={} et externalId={}", code, ac, extId);
			//			sampleAPI.dao_update(DBQuery.is("code", code),
			//					DBUpdate.set("traceInformation.modifyUser", user)
			//							.set("traceInformation.modifyDate", date)); 	
			Sample sample = sampleAPI.get(code);
			sample.traceInformation.modifyUser = user;
			sample.traceInformation.modifyDate = date;
			//sampleAPI.update(sample, user);
			sampleDAO.update(sample);
		}
		for (String code : submission.experimentCodes) {
			String ac = ebiRetour.getAccessionExperiment(code);
			message += "experimentCode = " + code + ",   AC = "+ ac + "\n";  
			//logger.debug("Mise à jour de l'exp {} avec AC={}", code, ac);
			//			experimentAPI.dao_update(DBQuery.is("code", code),
			//					DBUpdate.set("traceInformation.modifyUser", user)
			//							.set("traceInformation.modifyDate", date)); 
			Experiment experiment = experimentAPI.get(code);
			experiment.traceInformation.modifyUser = user;
			experiment.traceInformation.modifyDate = date;
			//experimentDAO.update(experiment, user);
			experimentDAO.update(experiment);
		}
		// recharger la soumission avec les valeurs AC, studyTrace mis à jour dans base :
		submission = submissionAPI.get(submission.code);
		return message;
	}



	public EbiRetour parseRetourEbi(File ebiFile, Submission submission) throws SraException {
		EbiRetour ebiRetour = new EbiRetour(ebiFile);
		Boolean ebiSuccess  = false;
		//Calendar calendar = Calendar.getInstance();
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

			final Document document= builder.parse(ebiFile);
			//Affiche du prologue
			//logger.debug("*************PROLOGUE************");
			//logger.debug("version : " + document.getXmlVersion());
			//logger.debug("encodage : " + document.getXmlEncoding());      
			//logger.debug("standalone : " + document.getXmlStandalone());
			/*
			 * Etape 4 : récupération de l'Element racine
			 */
			final Element racine = document.getDocumentElement();
			//Affichage de l'élément racine
			//logger.debug("\n*************RACINE************");
			//logger.debug(racine.getNodeName());
			//logger.debug("success = " + racine.getAttribute("success"));
			//logger.debug(((Node) racine).getNodeName());
			//logger.debug("success = " + ((DocumentBuilderFactory) racine).getAttribute("success"));
			/*
			 * Etape 5 : récupération des samples
			 */
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();
			// NGL-5157
			if ( racine.getAttribute("receiptDate") != null && racine.getAttribute("success") != null && racine.getAttribute("success").equalsIgnoreCase("true")) {
				//				ebiRetour.stringSubmissionDate = racine.getAttribute("receiptDate");
				//				ebiRetour.stringSubmissionDate = ebiRetour.stringSubmissionDate .replace("T.*$","");
				//				//ebiRetour.submissionDate = dateFormat.parse(ebiRetour.stringSubmissionDate);
				//				Calendar calendar = Calendar.getInstance();
				//				calendar.setTime(sdf.parse(ebiRetour.stringSubmissionDate.replace("T.*$","")));
				//				calendar.add(Calendar.HOUR_OF_DAY, 6);
				//				ebiRetour.submissionDate = calendar.getTime();
				ebiSuccess = true;
				ebiRetour.stringSubmissionDate = racine.getAttribute("receiptDate");
				ebiRetour.stringSubmissionDate = ebiRetour.stringSubmissionDate.replace("T.*$",""); //conserver uniquement le jour de la forme yyyy-MM-dd (enlever heures, sec .. fuseau)
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(ebiRetour.stringSubmissionDate));
				// Pour eviter de changer date d'un jour si decalage avec EBI ete ou hivers :
				calendar.add(Calendar.HOUR_OF_DAY, 6);
				ebiRetour.submissionDate = calendar.getTime();	
			}
			
			for (int i = 0; i<nbRacineNoeuds; i++) {
				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					//logger.debug("\n*************Elt************");

					//String alias = elt.getAttribute("alias");
					//String accession = elt.getAttribute("accession");

					//logger.debug("alias : " + alias);
					//logger.debug("accession : " + accession);

					if (elt.getNodeName().equals("ACTIONS")) {
						//						logger.debug("          !!!!!!!!!!!!!!!!!!                 balises ACTIONS reconnue");
						//						logger.debug("          !!!!!!!!!!!!!!!!!!                 pour ACTIONS, nodeType = " + elt.getNodeType());
						//						logger.debug("          !!!!!!!!!!!!!!!!!!                 pour ACTIONS, nodeValue = " + elt.getNodeValue());
						//						logger.debug("          !!!!!!!!!!!!!!!!!!                 pour ACTIONS, getTextContent = " + elt.getTextContent());
						//						logger.debug("          !!!!!!!!!!!!!!!!!!                 pour ACTIONS, getUserData = " + elt.getUserData("ACTIONS"));

						if (StringUtils.isNotBlank(elt.getTextContent())) {
							String action = elt.getTextContent();
							//logger.debug("ACTION =  !!!!!!!!!!!!!!!!!!!!!!      " + action);
							switch(action) {
							case "ADD"     : ebiRetour.typeRetour = "CREATION";
							break;
							case "MODIFY"  : ebiRetour.typeRetour = "UPDATE";
							break;
							case "RELEASE" : ebiRetour.typeRetour = "RELEASE";
							break;
							default        : break;
							}
						}
					}
					switch(submission.type) {
						case RELEASE : 
							parseElementEbiForRelease(elt, ebiRetour);
							break;
						case CREATION : case UPDATE :
							parseElementEbiForCreationOrUpdate(elt, ebiRetour);
							break;
						default : 
							throw new SraException("Parsing du rapport de l'EBI pour soumission de type " + submission.type + " non implemente");
					}
				}
			}  // end for  

		} catch (final ParserConfigurationException | SAXException | IOException | ParseException e) {
			logger.debug(e.getMessage());
						e.printStackTrace();
			throw new SraException("parseRetourEbi::Erreur lors de la lecture du fichier " + ebiFile, e);
		}
		if ( ebiSuccess ) {
			ebiRetour.success = true;
		} else {
			//			ebiRetour.success = false;
			//			String message = "Absence de la ligne RECEIPT ... pour  " + submission.code
			//					+ " dans le fichier "+ ebiFile.getPath();
			//			throw new SraException(message);
		} 
		return ebiRetour;
	}


	
	public void assertEbiRetourValide(EbiRetour ebiRetour, Submission submission) throws SraException {		
		if( StringUtils.isBlank(ebiRetour.getNameFileInput()) ) {
			throw new SraException("Absence du nom complet du rapport de soumission dans ebiRetour pour la soumission " + submission.code);
		}
		if( StringUtils.isBlank(ebiRetour.typeRetour) ) {
			throw new SraException("Type de rapport de soumission non identifie dans le fichier " + ebiRetour.getNameFileInput() + " pour la soumission " + submission.code);
		}
		//logger.debug("dans assert ebiRetour.typeRetour='" + ebiRetour.typeRetour + "'");
		//logger.debug("dans assert ebiRetour.getAccessionStudy='" + ebiRetour.getAccessionStudy() + "'");
		if( ! ebiRetour.success) {
			String message = "Absence de la ligne 'RECEIPT' avec 'receiptDate' et 'success=true' ... pour  " + submission.code + " dans le fichier " + ebiRetour.getNameFileInput() + " pour la soumission " + submission.code;
			throw new SraException(message);
		}
		switch(submission.type) {
		case RELEASE :
			if ( ! ebiRetour.typeRetour.equals("RELEASE")) {
				throw new SraException("ebiReport=" + ebiRetour.getNameFileInput() + " de type " + ebiRetour.typeRetour + 
						" et soumission " + submission.code + " de type " + submission.type);	
			}
			// si au moins un des champs est renseigné, alors ok
			if ( StringUtils.isNotBlank(ebiRetour.getAccessionStudy()) || StringUtils.isNotBlank(ebiRetour.getExtIdStudy()) ||
					StringUtils.isNotBlank(ebiRetour.getAccessionSample("")) || StringUtils.isNotBlank(ebiRetour.getAccessionSample("")) ) {
				return;
			} else {
				//logger.debug("dans asser, dans else,  ebiRetour.typeRetour='" + ebiRetour.typeRetour + "'");
				//logger.debug("dans assert, dans else, ebiRetour.getAccessionStudy='" + ebiRetour.getAccessionStudy() + "'");
				throw new SraException("Absence d'un identifiant de study ou de sample dans le rapport de soumission " + ebiRetour.getNameFileInput() + " pour la soumission " + submission.code);				
			}
		case CREATION : 
			if ( ! ebiRetour.typeRetour.equals("CREATION")) {
				throw new SraException("ebiReport=" + ebiRetour.getNameFileInput() + " de type " + ebiRetour.typeRetour + 
						" et soumission " + submission.code + " de type " + submission.type);	
			}
			break;
		case UPDATE :
			if ( ! ebiRetour.typeRetour.equals("UPDATE")) {
				throw new SraException("ebiReport=" + ebiRetour.getNameFileInput() + " de type " + ebiRetour.typeRetour + 
						" et soumission " + submission.code + " de type " + submission.type);	
			}
			break;	
		}
		//logger.debug("dans assert, autre cas que release");

		// cas pour update ou creation :
		Boolean error = false;
		//logger.debug("YYYYYYYYY      dans assertEbiRetourValide, ebiRetour = " + ebiRetour);
		//logger.debug("YYYYYYYYY      dans assertEbiRetourValide, ebiRetour.getNameFileInput()=" + ebiRetour.getNameFileInput());
		String message = "Pour la soumission " + submission.code + ", le fichier retour de l'EBI "+ ebiRetour.getNameFileInput() + "\n";

		if (StringUtils.isBlank(ebiRetour.submissionCode)) { 
			message += "- ne contient pas " + submission.code + "\n";
			throw new SraException(message);
		} 
		if (! ebiRetour.submissionCode.equals(submission.code)) {
			message += "- contient un ebiSubmissionCode ("  + ebiRetour.submissionCode + ") different du submissionCode passé en parametre "+ submission.code + "\n"; 
			throw new SraException(message);
		}


		if(submission.type.equals(Submission.Type.CREATION) && StringUtils.isBlank(ebiRetour.submissionAc)) {
			message += "- ne contient pas de submissionAC\n";
			error = true;
		}
		// Verifier que le nombre d'ac recuperés dans le fichier est bien celui attendu pour l'objet submission:
		if (StringUtils.isNotBlank(submission.studyCode)) {
			//logger.debug("submission.studyCode = '" + submission.studyCode + "' et ebiRetour.studyCode = '" + ebiRetour.studyCode + "'");
			if (StringUtils.isBlank(ebiRetour.studyAc)) {
				message += "- ne contient pas d'AC pour le studyCode " + submission.studyCode+"\n";
				error = true;
			} 
			if (StringUtils.isBlank(ebiRetour.studyCode)) {
				message += "- ne contient pas de valeur pour le studyCode " + submission.studyCode+"\n";
				error = true;

			} else {
				if (! ebiRetour.studyCode.equals(submission.studyCode)) {
					logger .debug("- submission.studyCode="+ submission.studyCode 
							+ " different de ebiStudyCode=" + ebiRetour.studyCode + "\n");
					message += "- submission.studyCode="+ submission.studyCode 
							+ " different de ebiStudyCode=" + ebiRetour.studyCode + "\n";
					error = true;
				}
			}
		}
		if (submission.sampleCodes != null) {
			for (int i = 0; i < submission.sampleCodes.size() ; i++) {
				//				if (!mapSamples.containsKey(submission.sampleCodes.get(i))){
				if (StringUtils.isBlank(ebiRetour.getAccessionSample(submission.sampleCodes.get(i)))) {
					message += "- ne contient pas d'AC pour le sampleCode " + submission.sampleCodes.get(i)+"\n";
					error = true;	
				}
				if (StringUtils.isBlank(ebiRetour.getExtIdSample(submission.sampleCodes.get(i)))) {
					message += "- ne contient pas d'externalId pour le sampleCode '" + submission.sampleCodes.get(i)+"'\n";
					error = true;	
				}	
			}
		}
		if (submission.experimentCodes != null){
			for (int i = 0; i < submission.experimentCodes.size() ; i++) {
				//				if (!mapExperiments.containsKey(submission.experimentCodes.get(i))){
				if (StringUtils.isBlank(ebiRetour.getAccessionExperiment(submission.experimentCodes.get(i)))) {
					message += "- ne contient pas d'AC pour l'experimentCode " + submission.experimentCodes.get(i) + "\n";
					error = true;
				}	
			}
		}
		if (submission.runCodes != null){
			for (int i = 0; i < submission.runCodes.size() ; i++) {
				//				if (!mapRuns.containsKey(submission.runCodes.get(i))){
				if (StringUtils.isBlank(ebiRetour.getAccessionRun(submission.runCodes.get(i)))) {
					message += "- ne contient pas d'AC pour le runCode " + submission.runCodes.get(i) + "\n";
					error = true;
				}	
			}
		}

		//logger.debug("error =" + error.booleanValue());
		if (error) {
			//logger.debug("assertEbiRetourValide:: Declenchement d'une SraException");
			throw new SraException(message);
		} 
	}


	private void parseElementEbiForRelease(Element elt, EbiRetour ebiRetour) {
		//logger.debug("          !!!!!!!!!!!!!!!!!!                 pour MESSAGES, nodeType = " + elt.getNodeType());

		if (elt.getElementsByTagName("INFO").item(0) != null) {
			String infos = elt.getElementsByTagName("INFO").item(0).getTextContent();
			//logger.debug("infos="+ infos);
			String pattern = "study\\s+accession\\s+\"([^\"]+)\"\\s+is\\s+set\\s+to\\s+public\\s+status";
			java.util.regex.Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(infos);
			String patternSample = "sample\\s+accession\\s+\"([^\"]+)\"\\s+is\\s+set\\s+to\\s+public\\s+status";
			java.util.regex.Pattern ps = Pattern.compile(patternSample);
			Matcher ms = ps.matcher(infos);

			if ( m.find() ) { 
				String studyIdentifier = m.group(1);
				//logger.debug("Recuperation dans ebiReport du studyAccession=" + studyIdentifier);
				if(studyIdentifier.startsWith("ERP")) {
					ebiRetour.studyAc = studyIdentifier;
					//logger.debug("! !!!!!!!!!!!!!!!!!!   ebiRetour.studyAc="+ ebiRetour.studyAc);
					//logger.debug("! !!!!!!!!!!!!!!!!!!   ebiRetour.getAccessionStudy()="+ ebiRetour.getAccessionStudy());
				} else {
					ebiRetour.studyExtId = studyIdentifier;
					//logger.debug("!!!!!!!!!!!!!!!!!      ebiRetour.studyAc="+ ebiRetour.studyAc);
					//logger.debug("!!!!!!!!!!!!!!!!!      ebiRetour.getExtIdStudy()="+ ebiRetour.getExtIdStudy());

				}
			} else if ( ms.find() ) { 
				String sampleIdentifier = ms.group(1);
				//logger.debug("Recuperation dans ebiReport du sampleAccession=" + sampleIdentifier);
				if(sampleIdentifier.startsWith("ERS")) {
					ebiRetour.mapSamples.put("", sampleIdentifier);	
				} else {
					ebiRetour.mapExtIdSamples.put("", sampleIdentifier);	
				}				
			}
		}
		
	}	
		

	private void parseElementEbiForCreationOrUpdate(Element elt, EbiRetour ebiRetour1) {
		// parsing cas d'une creation ou d'un update d'objets :
		if (elt.getTagName().equalsIgnoreCase("SUBMISSION")) {
			ebiRetour1.submissionCode = elt.getAttribute("alias");	
			ebiRetour1.submissionAc = elt.getAttribute("accession");
		} else if(elt.getTagName().equalsIgnoreCase("STUDY")) {// si soumission du study.xml lors d'une soumission de type CREATION
			ebiRetour1.studyCode = elt.getAttribute("alias");	
			ebiRetour1.studyAc = elt.getAttribute("accession");
			final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
			if(eltExtId != null) {
				ebiRetour1.studyExtId = eltExtId.getAttribute("accession");
				//logger.debug("study_ext_id: " + ebiRetour.studyExtId);
			}
		} else if(elt.getTagName().equalsIgnoreCase("PROJECT")) {// si soumission du project.xml lors d'une soumission de type CREATION
			ebiRetour1.studyCode = elt.getAttribute("alias");	
			ebiRetour1.studyExtId = elt.getAttribute("accession");
			ebiRetour1.projectCode = elt.getAttribute("alias");	
			ebiRetour1.projectAc = elt.getAttribute("accession");
			final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
			if(eltExtId != null) {
				ebiRetour1.studyAc = eltExtId.getAttribute("accession");
				ebiRetour1.projectExtId = eltExtId.getAttribute("accession");
				//logger.debug("study_ext_id: " + ebiRetour.studyExtId);
			}
		} else if(elt.getTagName().equalsIgnoreCase("ANALYSIS")) {
			ebiRetour1.analysisCode = elt.getAttribute("alias");	
			ebiRetour1.analysisAc = elt.getAttribute("accession");
		} else if(elt.getTagName().equalsIgnoreCase("SAMPLE")) {
			ebiRetour1.mapSamples.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
			final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
			// declenche erreur non capturée String sampleExtId = eltExtId.getAttribute("accession");
			//logger.debug("sampleExtId='" +  sampleExtId);
			if(eltExtId != null) {
				//logger.debug("sample_ext_id:" + eltExtId.getAttribute("accession"));
				ebiRetour1.mapExtIdSamples.put(elt.getAttribute("alias"), eltExtId.getAttribute("accession"));
			} else {
				//logger.debug("Pas d'externalId pour le sample '" +  elt.getAttribute("alias") + "'");
			}
		} else if(elt.getTagName().equalsIgnoreCase("EXPERIMENT")) {
			ebiRetour1.mapExperiments.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
		} else if(elt.getTagName().equalsIgnoreCase("RUN")) {
			ebiRetour1.mapRuns.put(elt.getAttribute("alias"), elt.getAttribute("accession"));
		} else {
			// Ignore other nodes
			//logger.debug("else");
		}

	}

	

	
	// NGL-4373 : mise à jour pour releaseDate des exp, analyses et samples qui dependent de ce study après sauvegarde du study
	// Propage la date de release aux exp, analyses et samples associés au study dans la base sans controle car on met ici en public 
	// on ne risque pas de tenter de rendre prive un objet deja public
	private void propagePublicReleaseDateInBaseForExpAnalysisSamples (String studyCode, String studyAccession, String user, Date releaseDate) throws APIValidationException, APIException {
		List <Analysis> analysisList = new ArrayList<Analysis>();
		List <Experiment> expList    = new ArrayList<Experiment>();
		if (StringUtils.isBlank(studyCode)) {
			throw new SraException("studyCode null en entree");
		}

		analysisList = MongoDBDAO.find( InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, 
				DBQuery.or
				(DBQuery.in("studyAccession", studyAccession), 
						DBQuery.in("studyCode", studyCode))).toList();

		expList = MongoDBDAO.find( InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or
				(DBQuery.in("studyAccession", studyAccession), 
						DBQuery.in("studyCode", studyCode))).toList();

		HashMap<String, Sample>       hSampleToUpdateForReleaseDate = new HashMap<String, Sample>();


		for(Analysis analysis : analysisList) {
			if(StringUtils.isBlank(analysis.accession)) {
				// ignorer analysis non soumis pour releaseDate
				continue;
			}
			// Renseigner les analyses et sauver en base
			analysis.traceInformation.modifyUser = user;
			analysis.traceInformation.modifyDate = new Date();	
			analysis.releaseDate = releaseDate;
			analysis.consolidatedPublicStatus = true;
			//analysisAPI.update(analysis, user);
			analysisDAO.update(analysis);

			// Repertorier sample à modifier 
			AbstractSample abstSample = abstractSampleAPI.get(analysis.sampleCode);
			if(abstSample instanceof ExternalSample) {
				// rien à recalculer
			} else {
				if(StringUtil.isNotBlank(abstSample.accession) && ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
					hSampleToUpdateForReleaseDate.put(analysis.sampleCode, (Sample) abstSample);
				}
			}	
		}

		for(Experiment experiment : expList) {
			if(StringUtils.isBlank(experiment.accession)) {
				// ignorer experiment non soumis pour releaseDate
				continue;
			}
			// Renseigner les experiments et sauver en base
			experiment.traceInformation.modifyUser = user;
			experiment.traceInformation.modifyDate = new Date();	
			experiment.releaseDate = releaseDate;
			experiment.consolidatedPublicStatus = true;
			experimentDAO.update(experiment);
			
			// Repertorier sample à modifier 
			AbstractSample abstSample = abstractSampleAPI.get(experiment.sampleCode);
			if(abstSample instanceof ExternalSample) {
				// rien à recalculer
			} else {
				if(StringUtil.isNotBlank(abstSample.accession) && ! hSampleToUpdateForReleaseDate.containsKey(abstSample.code)) {
					hSampleToUpdateForReleaseDate.put(experiment.sampleCode, (Sample) abstSample);
				}
			}	
		}

		for (Entry<String, Sample> setSample : hSampleToUpdateForReleaseDate.entrySet()) {
			if(StringUtils.isBlank(setSample.getValue().accession)) {
				// on ne met pas de date de release sur un sample non soumis
				continue;
			}
			if(submissionNewAPITools.isPublicAbstractSample(setSample.getValue())) {
				if(! setSample.getValue().consolidatedPublicStatus) {			
					setSample.getValue().consolidatedPublicStatus = true;
					setSample.getValue().traceInformation.modifyUser = user;
					setSample.getValue().traceInformation.modifyDate = new Date();	
					sampleDAO.update(setSample.getValue());
				} else {
					// rien, le sample est deja public dans base avec bon consolidatedPublicStatus et releaseDate qui lui appartient.
				}
			} else {
				setSample.getValue().releaseDate = releaseDate;
				setSample.getValue().consolidatedPublicStatus = true;
				setSample.getValue().traceInformation.modifyUser = user;
				setSample.getValue().traceInformation.modifyDate = new Date();	
				sampleDAO.update(setSample.getValue());
			}
		}	
	}


}