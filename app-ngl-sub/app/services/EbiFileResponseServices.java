package services;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.AnalysisAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import mail.MailServiceException;
import mail.MailServices;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import validation.ContextValidation;
//import workflows.sra.submission.SubmissionWorkflows;

public class EbiFileResponseServices  {

	private static final play.Logger.ALogger logger = play.Logger.of(EbiFileResponseServices.class);

	private final NGLConfig           config;
	private final SubmissionAPI       submissionAPI;
	private final StudyAPI            studyAPI;
	private final ProjectAPI          projectAPI;
	private final AbstractStudyAPI    abstractStudyAPI;
	private final AnalysisAPI         analysisAPI;

	private final SampleAPI           sampleAPI;
	private final AbstractSampleAPI   abstractSampleAPI;

	private final ExperimentAPI       experimentAPI;
	private final ReadSetsAPI         laboReadsetsAPI;
	private final ReadsetAPI   	      readsetAPI;
	private final SimpleDateFormat    dateFormat     = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	@Inject
	public EbiFileResponseServices(
			NGLConfig           config, 
			SubmissionAPI       submissionAPI,
			StudyAPI            studyAPI,
			AnalysisAPI         analysisAPI,
			ProjectAPI          projectAPI,
			AbstractStudyAPI    abstractStudyAPI,
			SampleAPI           sampleAPI,
			AbstractSampleAPI   abstractSampleAPI,
			ExperimentAPI       experimentAPI,
			ReadSetsAPI   	    laboReadsetsAPI,
			ReadsetAPI   	    readsetAPI
			) {
		this.config              = config;
		this.submissionAPI       = submissionAPI;
		this.studyAPI            = studyAPI;
		this.analysisAPI         = analysisAPI;
		this.projectAPI          = projectAPI;
		this.abstractStudyAPI    = abstractStudyAPI;
		this.sampleAPI           = sampleAPI;
		this.abstractSampleAPI   = abstractSampleAPI;
		this.experimentAPI       = experimentAPI;
		this.laboReadsetsAPI     = laboReadsetsAPI;
		this.readsetAPI          = readsetAPI;
	}


	/**
	 * Parse le fichier de l'EBI recu en retour de l'envoie des metadonnées lors d'une soumission de type creation
	 * de données ou d'une soumission de type update de données.
	 * Met à jour les differents objets de la soumission pour les AC, les dates (creation, release),
	 * mais pas pour le status ou le TraceInformation 
	 * @param ctxVal                  context de validation
	 * @param submission              objet soumission
	 * @param ebiFileResp             fichier des AC
	 * @throws SraException           error
	 */
	


	//@SuppressWarnings("null")
	public void loadEbiResp(ContextValidation ctxVal, Submission submission, File ebiFileResp) throws SraException {
			//logger.debug("coucou   ok           dans loadEbiResp, ebiRileResp=" +ebiFileResp); 
		if (submission == null) {
			//logger.debug("loadEbiResp :: parametres d'entree submission à null ??" );
			throw new SraException("EbiFileResponseServices.loadEbiResp :: parametres d'entree submission à null ??" );
		}
	
		
		//logger.debug("EbiFileResponseServices.loadEbiResp : ok on attaque parsing");
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
			subjectError = config.getUpdateReportingEmailSubjectError();
			subjectSuccess = config.getUpdateReportingEmailSubjectSuccess();
			dest = config.getUpdateReportingEmailTo();
			expediteur = config.getUpdateReportingEmailFrom();
			break;
		case RELEASE:
			throw new SraException("Impossible d'utiliser la methode parseRetourEbi " 
					+ "pour la soumission de type RELEASE "+ submission.code);
		default:
			throw new SraException("Type de soumission non gerée :" + submission.type 
					+ " pour la soumission " + submission.code);
		}
		
		// si cond de tests :
//		MailServices mailService = new MailServices();
//		Set<String> destinataires = new HashSet<>();
//	
//		// On ne prend pas ctxVal.getUser, car methode lancé par birds,pas très informatif
//		String user = ctxVal.getUser();
//		
//		String destinataire = user;
//		destinataire = destinataire + "@genoscope.cns.fr";
//		
//		destinataires.add(destinataire);
		// end condi de test
			
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
		}
		String destinataire = user;
		destinataire = destinataire + "@genoscope.cns.fr";
		if (! destinataires.contains(destinataire)) {
			destinataires.add(destinataire);
		}
		// end cond de prod
		
		EbiRetour ebiRetour = null;
		try {
			//logger.debug("parseRetourEbi");
			ebiRetour = parseRetourEbi(ebiFileResp, submission);
			//Logger.debug("ZZZZZZZZZZZZZ  dans EbiRetour, ebiFile=" + ebiRetour.getNameFileInput());


			
			//logger.debug("parseRetourEbi: ok");
			//logger.debug("assertEbiRetourValide");
			assertEbiRetourValide(ebiRetour, submission);
			//logger.debug("assertEbiRetourValide:ok");

		} catch (SraException e) {
			//logger.debug("loadEbiResp:: catch la SraException");
			//logger.debug("subjectError =" +subjectError);
			try {
				//logger.debug(new String(e.getMessage().getBytes(), "iso-8859-1"));
				mailService.sendMail(expediteur, destinataires, subjectError, new String(e.getMessage().getBytes(), "iso-8859-1"));
				throw new SraException(new String(e.getMessage().getBytes(), "iso-8859-1"));
			} catch (UnsupportedEncodingException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message d'erreur");
			} catch (MailServiceException e1) {
				throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail d'erreur");
			}
		}


		String message = "";
		if (submission.type.equals(Submission.Type.CREATION)) {
			// Mise à jour dans la base de la soumission et de ses objets pour les AC :
			message = updateDbForAC(submission, ebiRetour, user);
		} 
		if (submission.type.equals(Submission.Type.UPDATE)) {
			// Mise à jour dans la base de la soumission et de ses objets pour les traceInformations:
			message = updateDbForUpdate(submission, ebiRetour, user);
		} 
		try {
			mailService.sendMail(expediteur, destinataires, subjectSuccess, new String(message.getBytes(), "iso-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			throw new SraException("loadEbiResp:: Probleme lors de l'encodage du message de succes");
		} catch (MailServiceException e1) {
			throw new SraException("loadEbiResp:: Probleme lors de l'envoie du mail de success");
		}
		
	}
	
	// utilisée à la reception du rapport de soumission dans le cas d'une soumission de type CREATION
	private String updateDbForAC(Submission submission, EbiRetour ebiRetour, String user) {
		// calendar initialisé avec date du jour ou submissionDate du rapport de soumission
		Calendar calendar = Calendar.getInstance();
		Date dateNow = calendar.getTime();
		Date submissionDate = ebiRetour.getSubmissionDate();
		if (submissionDate !=null) {
			calendar.setTime(ebiRetour.getSubmissionDate());
		}	
		calendar.add(Calendar.YEAR, 2);
		Date release_date  = calendar.getTime();
		
		String message = "Liste des AC attribues pour la soumission "  + submission.code + " en mode confidentiel jusqu'au : " + release_date +" <br/><br/>\n";
		String header_umbrella = "Liste des AC attribues pour la soumission "  + submission.code + " en mode public <br/><br/>\n";
		//		String retourChariot = "\n";
		
		message += "submissionCode = " + submission.code + ",   AC = "+ ebiRetour.submissionAc + "</br>";  
		//logger.debug(message);
		submission.accession = ebiRetour.submissionAc;

		//logger.debug("Mise à jour de la soumission {} avec AC={} ", ebiRetour.submissionCode, ebiRetour.submissionAc);

		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
//							                             DBQuery.notExists("accession")),
				DBUpdate.set("accession", ebiRetour.submissionAc)
						.set("submissionDate", submissionDate)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", dateNow));	

		if (StringUtils.isNotBlank(submission.studyCode)) {	
			message += "studyCode = " + ebiRetour.studyCode + ",   AC = "+ ebiRetour.studyAc + ", bioProjectId = " + ebiRetour.studyExtId + "<br/>\n";
			//logger.debug("Mise à jour du study {} avec AC={} et externalId={}", ebiRetour.studyCode, ebiRetour.studyAc, ebiRetour.studyExtId);

			// si study avec champs accession existe mais vide, alors pas d'update mais pas de message d'erreur
			// d'ou verif avant de faire update
//			studyAPI.dao_update(DBQuery.is("code", ebiRetour.studyCode).notExists("accession"),
//					DBUpdate.set("accession", ebiRetour.studyAc)
//							.set("externalId", ebiRetour.studyExtId)
//							.set("firstSubmissionDate", submissionDate)
//							.set("releaseDate", release_date)
//							.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", dateNow));
			
			Study study = studyAPI.dao_getObject(submission.studyCode);
			if (study == null) {
				//logger.debug("Study " + submission.studyCode + " absent de la base");
				throw new SraException("Study " + submission.studyCode + " absent de la base");
			}
			if (StringUtils.isNotBlank(study.accession)) {
				//logger.debug("Dans la base le study " + submission.studyCode + " contient deja un AC");
				throw new SraException("Dans la base le study " + submission.studyCode + " contient deja un AC");
			}	
			studyAPI.dao_update(DBQuery.and(DBQuery.is("code", ebiRetour.studyCode)),
//                  DBQuery.notExists("accession")),
					DBUpdate.set("accession", ebiRetour.studyAc)
					.set("externalId", ebiRetour.studyExtId)
					.set("firstSubmissionDate", submissionDate)
					.set("releaseDate", release_date)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", dateNow));

		}
		if (StringUtils.isNotBlank(submission.analysisCode)) {
			message += "analysisCode = " + ebiRetour.analysisCode + ",   AC = "+ ebiRetour.analysisAc + "<br/>\n";
			//logger.debug("Mise à jour de l'analyse {} avec AC={} ", ebiRetour.analysisCode, ebiRetour.analysisAc);

			Analysis analysis = analysisAPI.dao_getObject(submission.analysisCode);
			if (analysis == null) {
				//logger.debug(" " + submission.analysisCode + " absent de la base");
				throw new SraException("analysis " + submission.analysisCode + " absent de la base");
			}
			if (StringUtils.isNotBlank(analysis.accession)) {
				//logger.debug("Dans la base, l'analyse " + submission.analysisCode + " contient deja un AC");
				throw new SraException("Dans la base, l'analyse " + submission.analysisCode + " contient deja un AC");
			}	
			analysisAPI.dao_update(DBQuery.and(DBQuery.is("code", ebiRetour.analysisCode)),
//                  DBQuery.notExists("accession")),
					DBUpdate.set("accession", ebiRetour.analysisAc)
					.set("firstSubmissionDate", submissionDate)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", dateNow));
			// updater les 
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
				}
			}
		}
		// On ne met rien pour project car on ne veut pas faire une soumission de création du project,
		// on veut que l'EBI cree automatiquement le project de type SEQUENCING à la creation du study
		// en revanche il faut traiter la creation d'un project UMBRELLA:
		
		if (StringUtils.isNotBlank(submission.umbrellaCode)) {
			message = header_umbrella;
			message += "umbrellaCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + "<br/>\n";
			//logger.debug("Mise à jour du project umbrella {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);

			Project umbrellaProject = projectAPI.get(submission.umbrellaCode);
			if (umbrellaProject == null) {
				//logger.debug(" " + submission.umbrellaCode + " absent de la base");
				throw new SraException("umbrellaProject " + submission.umbrellaCode + " absent de la base");
			}
			if (StringUtils.isNotBlank(umbrellaProject.accession)) {
				//logger.debug("Dans la base, le project " + submission.umbrellaCode + " contient deja un AC");
				throw new SraException("Dans la base, le project " + submission.umbrellaCode + " contient deja un AC");
			}	
			projectAPI.dao_update(DBQuery.and(DBQuery.is("code", ebiRetour.projectCode)),
//                  DBQuery.notExists("accession")),
					DBUpdate.set("accession", ebiRetour.projectAc)
					.set("firstSubmissionDate", submissionDate)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", dateNow));

		}
		for (String code : submission.sampleCodes) {
			String ac = ebiRetour.getAccessionSample(code);
			String extId = ebiRetour.getExtIdSample(code);
			message += "sampleCode = " + code + ",   AC = "+ ac + " , externalId = " + extId + "<br/>" + "\n";  
			//logger.debug("Mise à jour du sample {} avec AC={} et externalId={}", code, ac, extId);
			Sample sample = sampleAPI.dao_getObject(code);
			if (sample == null) {
				//logger.debug("Sample " + code + " absent de la base");
				throw new SraException("Sample " + code + " absent de la base");
			}
			if (StringUtils.isNotBlank(sample.accession)) {
				//logger.debug("Dans la base le sample " + code + " contient deja un AC");
				throw new SraException("Dans la base le sample " + code + " contient deja un AC");
			}
			sampleAPI.dao_update(DBQuery.is("code", code),
					DBUpdate.set("accession", ac)
							.set("externalId", extId)
							.set("firstSubmissionDate", submissionDate)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", dateNow)); 		

		}
		 
		for (String code : submission.experimentCodes) {
			String ac = ebiRetour.getAccessionExperiment(code);
			
			message += "experimentCode = " + code + ",   AC = "+ ac + "<br/>\n";  
			//logger.debug("Mise à jour de l'exp {} avec AC={} ", code, ac);
			//sgas
			Experiment experiment = experimentAPI.get(code);
			if (experiment == null) {
				//logger.debug("Experiment " + code + " absent de la base");
				throw new SraException("Experiment " + code + " absent de la base");
			}
			if (StringUtils.isNotBlank(experiment.accession)) {
				//logger.debug("Dans la base l' experiment " + code + " contient deja un AC");
				throw new SraException("Dans la base l' experiment " + code + " contient deja un AC");
			}
			// completer studyAc. 
			// Soit Study de type External et existe dans base à la création de la soumission, 
			// soit Study deja soumis par nous et existe dans base à la création de la soumission
			// soit Study dans la soumission courante et existe dans infosEbi:
			if (StringUtil.isNotBlank(ebiRetour.getCodeStudy()) && ebiRetour.getCodeStudy().equalsIgnoreCase(experiment.studyCode)) {
				experiment.studyAccession = ebiRetour.getAccessionStudy();
			} else {
				AbstractStudy absStudy = abstractStudyAPI.get(experiment.studyCode);
				//logger.debug("studyAccession recupere dans base => studyCode = "+ absStudy.code + " et studyAccession="+absStudy.accession);
				if(StringUtils.isNotBlank(absStudy.accession)) {
					experiment.studyAccession = absStudy.accession;
				} // else experiment.studyAccession reste avec sa valeur initiale de la forme PRJ
			}
			
			// completer sampleAc. 
			// Soit Sample de type External et existe dans base à la création de la soumission, 
			// soit Sample deja soumis par nous et existe dans base à la création de la soumission
			// soit Sample dans la soumission courante et existe dans infosEbi:
			if (StringUtil.isNotBlank(ebiRetour.getAccessionSample(experiment.sampleCode))) {
				experiment.sampleAccession = ebiRetour.getAccessionSample(experiment.sampleCode);
			} else {
				AbstractSample absSample = abstractSampleAPI.get(experiment.sampleCode);
				if(StringUtils.isNotBlank(absSample.accession)) {
					experiment.sampleAccession = absSample.accession;
				} 
			}
			//logger.debug("studyAccession = " + experiment.studyAccession);
			//logger.debug("sampleAccession = " + experiment.sampleAccession);

			experimentAPI.dao_update(DBQuery.is("code", code),
					DBUpdate.set("accession", ac)
							.set("sampleAccession", experiment.sampleAccession)
							.set("studyAccession", experiment.studyAccession)
							.set("firstSubmissionDate", submissionDate)
							.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", dateNow)); 

		
			// Creer le readset dans la collection sra_readset:
			Readset readset = new Readset();
			readset.code = experiment.readSetCode;
			readset.experimentCode = experiment.code;
			readset.type = experiment.typePlatform.toLowerCase();
			readset.runCode = experiment.run.code;
			if (! readsetAPI.dao_checkObjectExist("code", experiment.readSetCode)) {
				readsetAPI.dao_saveObject(readset);
			}
			
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
		for (String code : submission.runCodes) {
			String ac = ebiRetour.getAccessionRun(code);
			message += "runCode = " + code + ",   AC = "+ ac  + "<br>\n";  
			//logger.debug("Mise à jour du run {} avec AC={}", code, ac);
			experimentAPI.dao_update(DBQuery.is("run.code", code),
					DBUpdate.set("run.accession", ac)); 			
		}

//		// Recuperer l'objet submission mis à jour pour le status :
//		submission = submissionAPI.dao_getObject(submission.code);
//		updateSubmissionTypeCreationForDates(submission);
		submission = submissionAPI.get(submission.code);
		return message;
	}
	
	// utilisée à la reception du rapport de soumission dans le cas d'une soumission de type update
	private String updateDbForUpdate(Submission submission, EbiRetour ebiRetour, String user) {
		//Calendar calendar = Calendar.getInstance();
		//Date date  = calendar.getTime();
		Date date= ebiRetour.submissionDate;
		String message = "Liste des donnees mises à jour avec  la soumission "  + submission.code + "<br><br>\n";
		//logger.debug(message);

		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
				DBUpdate.set("submissionDate", date)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", date));	

		if (StringUtils.isNotBlank(submission.studyCode)) {	
			message += "studyCode = " + ebiRetour.studyCode + ",   AC = "+ ebiRetour.studyAc + ", bioProjectId = " + ebiRetour.studyExtId + "\n";
			//logger.debug("Mise à jour du study {} avec AC={} et externalId={}", ebiRetour.studyCode, ebiRetour.studyAc, ebiRetour.studyExtId);
			studyAPI.dao_update(DBQuery.is("code", ebiRetour.studyCode),
					DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date));
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

			sampleAPI.dao_update(DBQuery.is("code", code),
					DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date)); 		

		}
		for (String code : submission.experimentCodes) {
			String ac = ebiRetour.getAccessionExperiment(code);
			message += "experimentCode = " + code + ",   AC = "+ ac + "\n";  
			//logger.debug("Mise à jour de l'exp {} avec AC={}", code, ac);
			experimentAPI.dao_update(DBQuery.is("code", code),
					DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date)); 	
		}
		// recharger la soumission avec les valeurs AC, studyTrace mis à jour dans base :
		submission = submissionAPI.get(submission.code);
		return message;
	}

	// utilisée à la reception du rapport de soumission dans le cas d'une soumission de type update
	private String updateDbForUpdateDebug(Submission submission, EbiRetour ebiRetour, String user) {
		//Calendar calendar = Calendar.getInstance();
		//Date date  = calendar.getTime();
		Date date= ebiRetour.submissionDate;
		String rcMail = "<br>\n";
		String message = "Liste des donnees mises à jour avec  la soumission "  + submission.code + "<br>" + rcMail;
		//logger.debug(message);

//		submissionAPI.dao_update(DBQuery.and(DBQuery.is("code", submission.code)),
//				DBUpdate.set("submissionDate", date)
//						.set("traceInformation.modifyUser", user)
//						.set("traceInformation.modifyDate", date));	

		if (StringUtils.isNotBlank(submission.studyCode)) {	
			message += "studyCode = " + ebiRetour.studyCode + ",   AC = "+ ebiRetour.studyAc + ", bioProjectId = " + ebiRetour.studyExtId + rcMail;
			//logger.debug("Mise à jour du study {} avec AC={} et externalId={}", ebiRetour.studyCode, ebiRetour.studyAc, ebiRetour.studyExtId);
//			studyAPI.dao_update(DBQuery.is("code", ebiRetour.studyCode),
//					DBUpdate.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", date));
		}
		// si on a une soumission de type creation pour un bioproject umbrella :
		if (StringUtils.isNotBlank(submission.umbrellaCode)) {	
			message += "umbrellaProjectCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + rcMail;
			//logger.debug("Mise à jour du project {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);
//			projectAPI.dao_update(DBQuery.is("code", ebiRetour.projectCode),
//					DBUpdate.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", date));
		}
		// Si on avait une soumission de type creation pour un bioproject classique mais pas d'actualité :
//		if (StringUtils.isNotBlank(submission.ebiProjectCode)) {	
//			message += "projectCode = " + ebiRetour.projectCode + ",   AC = "+ ebiRetour.projectAc + rcMail;
//			logger.debug("Mise à jour du project {} avec AC={} ", ebiRetour.projectCode, ebiRetour.projectAc);
//			projectAPI.dao_update(DBQuery.is("code", ebiRetour.projectCode),
//					DBUpdate.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", date));
//		}
		for (String code : submission.sampleCodes) {
			String ac = ebiRetour.getAccessionSample(code);
			String extId = ebiRetour.getExtIdSample(code);
			message += "sampleCode = " + code + ",   AC = "+ ac + " , externalId = " + extId + " <br/>" + rcMail;  
			//logger.debug("Mise à jour du sample {} avec AC={} et externalId={}", code, ac, extId);

//			sampleAPI.dao_update(DBQuery.is("code", code),
//					DBUpdate.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", date)); 		

		}
		for (String code : submission.experimentCodes) {
			String ac = ebiRetour.getAccessionExperiment(code);
			message += "experimentCode = " + code + ",   AC = "+ ac + rcMail;  
			//logger.debug("Mise à jour de l'exp {} avec AC={}", code, ac);
//			experimentAPI.dao_update(DBQuery.is("code", code),
//					DBUpdate.set("traceInformation.modifyUser", user)
//							.set("traceInformation.modifyDate", date)); 	
		}
		// recharger la soumission avec les valeurs AC, studyTrace mis à jour dans base :
		submission = submissionAPI.get(submission.code);
		return message;
	}
	
	
//	
//	
//	public void updateSubmissionTypeCreationForDates(Submission submission) {
//		Calendar calendar = Calendar.getInstance();
//		Date date  = calendar.getTime();		
//		calendar.add(Calendar.YEAR, 2);
//		Date release_date  = calendar.getTime();
//		submissionAPI.dao_update(DBQuery.is("code", submission.code),
//				DBUpdate.set("submissionDate", date));		
//
//		if (submission.studyCode != null) {
//			studyAPI.dao_update(DBQuery.is("code", submission.studyCode),
//					DBUpdate.set("firstSubmissionDate", date).set("releaseDate", release_date));
//		}
//
//		//      Ne pas multiplier l'information, dates uniquement sur submission et study et surtout pas sur 
//		//		ReadSets.Illumina car sinon affecte model.
//	}
//	
//	


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
			
			if ( racine.getAttribute("success").equalsIgnoreCase ("true")) {
				ebiSuccess = true;
			}
			if ( racine.getAttribute("receiptDate") != null) {
				ebiRetour.stringSubmissionDate = racine.getAttribute("receiptDate");
				ebiRetour.submissionDate = dateFormat.parse(ebiRetour.stringSubmissionDate);
			}
		
			for (int i = 0; i<nbRacineNoeuds; i++) {

				if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {

					final Element elt = (Element) racineNoeuds.item(i);
					//Affichage d'un elt :
					//logger.debug("\n*************Elt************");

					String alias = elt.getAttribute("alias");
					String accession = elt.getAttribute("accession");

					//logger.debug("alias : " + alias);
					//logger.debug("accession : " + accession);

					if (elt.getTagName().equalsIgnoreCase("SUBMISSION")) {
						ebiRetour.submissionCode = elt.getAttribute("alias");	
						ebiRetour.submissionAc = elt.getAttribute("accession");
					} else if(elt.getTagName().equalsIgnoreCase("STUDY")) {
						ebiRetour.studyCode = elt.getAttribute("alias");	
						ebiRetour.studyAc = elt.getAttribute("accession");
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						if(eltExtId != null) {
							ebiRetour.studyExtId = eltExtId.getAttribute("accession");
							//logger.debug("study_ext_id: " + ebiRetour.studyExtId);
						}
					} else if(elt.getTagName().equalsIgnoreCase("ANALYSIS")) {
						ebiRetour.analysisCode = elt.getAttribute("alias");	
						ebiRetour.analysisAc = elt.getAttribute("accession");
					} else if(elt.getTagName().equalsIgnoreCase("PROJECT")) {
						ebiRetour.projectCode = elt.getAttribute("alias");	
						ebiRetour.projectAc = elt.getAttribute("accession");
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						if(eltExtId != null) {
							ebiRetour.projectExtId = eltExtId.getAttribute("accession");
							//logger.debug("project_ext_id: " + ebiRetour.projectExtId);
						}
					} else if(elt.getTagName().equalsIgnoreCase("SAMPLE")) {
						ebiRetour.mapSamples.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
						final Element eltExtId = (Element) elt.getElementsByTagName("EXT_ID").item(0);
						// declenche erreur non capturée String sampleExtId = eltExtId.getAttribute("accession");
						//logger.debug("sampleExtId='" +  sampleExtId);
						if(eltExtId != null) {
							//logger.debug("sample_ext_id:" + eltExtId.getAttribute("accession"));
							ebiRetour.mapExtIdSamples.put(elt.getAttribute("alias"), eltExtId.getAttribute("accession"));
						} else {
							//logger.debug("Pas d'externalId pour le sample '" +  elt.getAttribute("alias") + "'");
						}
					} else if(elt.getTagName().equalsIgnoreCase("EXPERIMENT")) {
						ebiRetour.mapExperiments.put(elt.getAttribute("alias"), elt.getAttribute("accession"));	
					} else if(elt.getTagName().equalsIgnoreCase("RUN")) {
						ebiRetour.mapRuns.put(elt.getAttribute("alias"), elt.getAttribute("accession"));
					} else {
						// Ignore other nodes
						//logger.debug("else");
					}
				}
			}  // end for  
			
			

		} catch (final ParserConfigurationException | SAXException | IOException | ParseException e) {
			//logger.debug(e.getMessage());
			//			e.printStackTrace();
			throw new SraException(e);
		}
		if (! ebiSuccess ) {		
			String message = "Absence de la ligne RECEIPT ... pour  " + submission.code
					+ " dans le fichier "+ ebiFile.getPath();
			throw new SraException(message);
		} 
		return ebiRetour;
	}

	
	//
	public void assertEbiRetourValide(EbiRetour ebiRetour, Submission submission) throws SraException {			
		Boolean error = false;
		//logger.debug("YYYYYYYYY      dans assertEbiRetourValide, ebiRetour = " + ebiRetour);
		//logger.debug("YYYYYYYYY      dans assertEbiRetourValide, ebiRetour.getNameFileInput()=" + ebiRetour.getNameFileInput());
		String message = "Pour la soumission " + submission.code + ", le fichier retour de l'EBI "+ ebiRetour.getNameFileInput() + "\n";

		if (StringUtils.isBlank(ebiRetour.submissionCode)) { 
			message += "- ne contient pas " + submission.code + "\n";
			error = true;
		} else {
			if (! ebiRetour.submissionCode.equals(submission.code)) {
				message += "- contient un ebiSubmissionCode ("  + ebiRetour.submissionCode + ") different du submissionCode passé en parametre "+ submission.code + "\n"; 
				error = true;
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
		}
		//logger.debug("error =" + error.booleanValue());
		if (error) {
			//logger.debug("assertEbiRetourValide:: Declenchement d'une SraException");
			throw new SraException(message);
		} 
	}


	
}
