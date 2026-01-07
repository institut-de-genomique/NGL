package sra.scripts;

import java.io.UnsupportedEncodingException;
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

import controllers.sra.api.StudyAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import mail.MailServiceException;
import mail.MailServices;
import models.sra.submit.sra.instance.EmailAlert;
import models.sra.submit.sra.instance.Study;
import services.sra.NGLSUBConfig;
import services.sra.SraException;


/*
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.AlerteEmailPublicStudy
 * @author sgas
 *
 */
public class SimpleAlerteEmailPublicStudy extends ScriptNoArgs {
	
	private final StudyAPI       		 studyAPI;
	private final NGLSUBConfig             config;
	
	@Inject
	public SimpleAlerteEmailPublicStudy (StudyAPI              studyAPI,
								         NGLSUBConfig                config) {
		this.studyAPI            = studyAPI;
		this.config              = config;
	}
	
	
	public boolean sendMailAndUpdateStudy(Study study)  {
		
		//println("Dans sendMailAndUpdateStudy, studyCode = " + study.code);
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
		message += "Message automatique produit par NGL \n\n";
		message += "Le study " + study.code + " va devenir public avec ses experiments, analyses, ou samples liés\n\n";
		message += String.format("%-20s|%-30s|%s\n","RELEASE_DATE", "STUDY_ID", "STUDY_TITLE");  
		message += String.format("%-20s|%-30s|%s\n", strReleaseDate, study.externalId + " (" + study.accession + ") ", study.title);
		message += "</pre> \n";
		MailServices mailService = new MailServices();
		Set<String> destinataires = new HashSet<>();
		String str_destinataires = config.getReleaseStudyEmailTo();
		destinataires.addAll(Arrays.asList(str_destinataires.split(",")));    

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
		// ici on veut pouvoir envoyer un mail pour tous les study proches de la date de release sans prevenir l'utilisateur
		//study.emailAlerts.add(emailAlert); // laisser en commentaire
		return true;
	}

	
	
	@Override
	public void execute() throws Exception {
		// selectionner study privés ayant AC et pour lesquels, il n'y a pas deja eu d'alerte mail pour la release :
		List <Study> studyList = studyAPI.dao_find(DBQuery.is("_type", "Study").is("consolidatedPublicStatus", false).exists("accession").notExists("emailAlerts")).toList();
		int count_study = 0;
		Date courantDate = new Date();
		
		println("date du jour = " + courantDate);
		for (Study study : studyList) {
			count_study++;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String strReleaseDate = sdf.format(study.releaseDate);
			println("studyCode = " + study.code + " et releaseDate = "+ strReleaseDate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(study.releaseDate);
			calendar.add(Calendar.MONTH, -1);
			Date  dateSeuil = calendar.getTime();
			String str_dateSeuil = sdf.format(dateSeuil);
			String str_courantDate = sdf.format(courantDate);
			println("dateSeuil=" + str_dateSeuil + "  et courantDate=" + str_courantDate);
			if( (courantDate.after(dateSeuil)    || courantDate.equals(dateSeuil)) && 
				(courantDate.before(study.releaseDate) || courantDate.equals(study.releaseDate))) {
				println("appel de sendMailAndUpdateStudy pour study " +  study.code);
				if(sendMailAndUpdateStudy(study)) {
					// sauvegarde dans la base de l'emailAlerts du study:
					println("sauvegarde en base du study " + study.code);
					// Ne pas sauver emailAlerts ici, seulement dans NGL-DATA
					//studyAPI.dao_update(DBQuery.is("code", study.code),
										//DBUpdate.set("emailAlerts", study.emailAlerts));
				} else {
					throw new SraException("Traitement en echec du study "+ study.code);
				}
			}			
		}	
		println("fin traitement des " + count_study + " study");
		
	}

}
