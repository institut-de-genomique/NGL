package sra.services.crons;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import com.mongodb.MongoException;

import controllers.sra.api.StudyAPI;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import mail.MailServiceException;
import mail.MailServices;
import models.sra.submit.sra.instance.EmailAlert;
import models.sra.submit.sra.instance.Study;
import models.utils.dao.DAOException;
import play.Logger;
import rules.services.RulesException;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import validation.ContextValidation;
import sra.services.lock.LockAPI;
import sra.services.lock.ActiveLock;

//Lancement via sra.services.crons.sheduler.ProdSubmissionCronScheduler 
//ou via sra.scripts.crons.ScriptAlerteEmailPublicStudy

/**
 * Check l'ensemble des study et envoie un mail d'alerte si le study est proche de sa date de release (1 mois) et 
 * qu'il n'y a pas encore eu de mail d'alerte.
 * @author sgas
 *
 */
public class AlerteEmailPublicStudy implements Runnable {

	private final StudyAPI       	  	   studyAPI;
	private final NGLSUBConfig             config;
	private final LockAPI                  lockAPI; 

	@Inject
	public AlerteEmailPublicStudy(NGLSUBConfig config, StudyAPI studyAPI, LockAPI lockAPI) {
		this.studyAPI = studyAPI;
		this.config   = config;
		this.lockAPI  = lockAPI;

	}

	public boolean sendMailAndUpdateStudy(Study study)  {
		//Logger.debug("Dans sendMailAndUpdateStudy, studyCode = " + study.code);
		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
		if(study.traceInformation == null || study.traceInformation.createUser == null || StringUtils.isBlank(study.traceInformation.createUser)) {
			throw new SraException("study " + study.code + " sans traceInformation.createUser");
		}
		if(study.externalId == null && StringUtils.isBlank(study.externalId)) {
			throw new SraException("study " + study.code + " sans externalId");
		}
		if(study.accession == null && StringUtils.isBlank(study.accession)) {
			throw new SraException("study " + study.code + " sans accession");
		}

		String expediteur = "cnsnglapps";
		String subject = config.getReleaseStudyEmailSubject() + " " + study.code;
		Date releaseDate = study.releaseDate;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String strReleaseDate = sdf.format(releaseDate);
		String message = "<pre> \n";
		//message += "isProd = '" + play.Play.application().isProd()+ "'\n";
		message += "Message automatique de NGL \n\n";
		message += "Le study " + study.code + " va devenir public avec ses experiments, analyses, ou samples liés\n\n";
		message += String.format("%-20s|%-30s|%s\n","RELEASE_DATE", "STUDY_ID", "STUDY_TITLE");  
		message += String.format("%-20s|%-30s|%s\n", strReleaseDate, study.externalId + " (" + study.accession + ") ", study.title);
		message += "</pre> \n";
		MailServices mailService = new MailServices();
		Set<String> destinataires = new HashSet<>();
		String str_destinataires = config.getReleaseStudyEmailTo();
		destinataires.addAll(Arrays.asList(str_destinataires.split(",")));    
		//destinataires.add("sgas@genoscope.cns.fr");
		if(study.traceInformation != null 
				&& study.traceInformation.createUser != null 
				&& StringUtils.isNotBlank(study.traceInformation.createUser)) {
			String destinataire = study.traceInformation.createUser;
			destinataire = destinataire + "@genoscope.cns.fr";
			//          ne pas utiliser la variable play.Play.application().isProd() qui indique PROD meme si on est sur serveur isoprod !
			//			if (play.Play.application().isProd()) {
			//				if (! destinataires.contains(destinataire)) {
			//					destinataires.add(destinataire);
			//				}
			//			}
			if(config.isPROD()) {
				if (! destinataires.contains(destinataire)) {
					destinataires.add(destinataire);
				}
			}
		}
		try {
			mailService.sendMail(expediteur, destinataires, subject, new String(message.getBytes(), "iso-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			throw new SraException("sendMailAndUpdateStudy:: Probleme lors de l'encodage du message du mail d'alerte pour la release du study "+ study.accession);
		} catch (MailServiceException e1) {
			throw new SraException("sendMailAndUpdateStudy:: Probleme lors de l'envoie du mail d'alerte pour la release du study "+ study.accession);
		}

		if(study.emailAlerts==null) {
			study.emailAlerts = new ArrayList<EmailAlert>(); 
		}

		EmailAlert emailAlert = new EmailAlert();
		emailAlert.date = new Date();
		emailAlert.subject = subject;
		emailAlert.destinataires =  new ArrayList<String>();
		emailAlert.destinataires.addAll(destinataires);
		if(emailAlert.destinataires.size() > 0) {
			study.emailAlerts.add(emailAlert);
		}
		return true;
	}

	private static boolean mustSendMail (Date dateSendMail, List<EmailAlert> emailAlerts) {
		for (EmailAlert emailAlert : emailAlerts) {
			if(emailAlert.date != null && emailAlert.date.after(dateSendMail)) {
				return false;
			}
		}
		return true;
	}


	public void run() {
		// poser un lock pour ne pas avoir 2 executions simultanées de la classe
		// Permet d'utiliser le filtre dans logger.xml qui separe les loggers de crons des autres loggers. 
		try (ActiveLock l = lockAPI.aquireLock(AlerteEmailPublicStudy.class)){
			// selectionner study privés ayant AC et pour lesquels, il n'y a pas deja eu d'alerte mail pour la release :
			List <Study> studyList = studyAPI.dao_find(DBQuery.is("_type", "Study").is("consolidatedPublicStatus", false).exists("accession")).toList();		
			int count_study = 0;
			Date courantDate = new Date();
			Logger.debug("debut du traitement AlerteEmailPublicStudy");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(courantDate);
			calendar.add(Calendar.MONTH, -1);
			Date dateSendMail = calendar.getTime();

			for (Study study : studyList) {
				count_study++;
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				String strReleaseDate = sdf.format(study.releaseDate);
				Logger.debug("studyCode = " + study.code + " et releaseDate = "+ strReleaseDate);
				calendar.setTime(study.releaseDate);
				calendar.add(Calendar.MONTH, -1);
				Date  dateSeuil = calendar.getTime();
				String str_dateSeuil = sdf.format(dateSeuil);
				String str_courantDate = sdf.format(courantDate);
				Logger.debug("dateSeuil=" + str_dateSeuil + "  et courantDate=" + str_courantDate + " et releaseDate=" + strReleaseDate);

				if (mustSendMail(dateSendMail, study.emailAlerts)) {
					if( (courantDate.after(dateSeuil)    || courantDate.equals(dateSeuil)) && 
							(courantDate.before(study.releaseDate) || courantDate.equals(study.releaseDate))) {
						//Logger.debug("appel de sendMailAndUpdateStudy pour study " +  study.code);
						if(sendMailAndUpdateStudy(study)) {
							// sauvegarde dans la base de l'emailAlerts du study:
							Logger.debug("sauvegarde en base du study " + study.code);
							studyAPI.dao_update(DBQuery.is("code", study.code),
									DBUpdate.set("emailAlerts", study.emailAlerts));
						} else {
							throw new SraException("Traitement en echec du study "+ study.code);
						}
					}	
				}
			}	
			Logger.debug("fin du traitement AlerteEmailPublicStudy pour les " + count_study + " study");		
		} catch(Exception e) {
			throw new RuntimeException(e);
		} 
	}
}
