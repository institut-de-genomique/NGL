package services.instance.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.*;
import com.mongodb.*;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.*;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.*;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.*;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import net.sourceforge.htmlunit.corejs.javascript.tools.shell.Global;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import validation.ContextValidation;
import workflows.process.ProcWorkflowHelper;

/**
 * Script qui permt de calculer le champ "processes" d'un ensemble de samples.
 * 
 * @see ImportDataCNS pour la définition des CRON au CNS.
 * @see ImportDataCNG pour la définition des CRON au CNG.
 */
public class UpdateReportingData extends AbstractImportData {

	/**
	 * Classe qui permet de définir une durée d'exécution pour un sample donné.
	 * Contient : date début de traitement du sample, date de fin de traitement du sample et durée de traitemnt du sample.
	 * Cette classe est utilisée dans le reporting à la fin du CRON.
	 * 
	 * @see buildMailBody()
	 */
	static public class DureeSample {

		public Date startDate;
		
		public Date endDate;

		public long duration;

		public DureeSample(Date startDate, Date endDate, long duration) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.duration = duration;
		}
	}

	private static final String EXPEDITEUR_KEY = "reporting.email.from";

	private static final String DESTINATAIRES_KEY = "reporting.email.to";

	private static final String ENV_KEY = "ngl.env";

	private final Map<String, List<String>> transformationCodesByProcessTypeCode = new HashMap<>();

	private final Map<String, Integer> nbExpPositionInProcessType = new HashMap<>();

	private Date LAUNCH_DATE;

	private MailServices mailServices = new MailServices();

	private static final int SEUIL_PJ = 100;

	private static final int DURATION_DAY = 86400000; 

	public Map<String, DureeSample> MAP_SAMPLE_DURATION = new TreeMap<String, DureeSample>();

	public Map<String, Long> MAP_DURATION = new TreeMap<String, Long>();

	public List<Sample> SAMPLE_LIST = new ArrayList<Sample>();

	private ProcWorkflowHelper procWorkflowHelper;	

	private NGLConfig NGL_CONFIG;

	private boolean warningMailSent = false;

	@Inject
	public UpdateReportingData(NGLApplication app, NGLConfig config) {
		super("UpdateReportingData", app);
		
		this.LAUNCH_DATE = new Date();
		this.procWorkflowHelper = app.injector().instanceOf(ProcWorkflowHelper.class);
		this.NGL_CONFIG = config;
	}

	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		logger.info("Start reporting synchro");

		this.LAUNCH_DATE = new Date();

		final long start = System.currentTimeMillis();
		
		warningMailSent = false;

		handleClearCollections();

		int NB_TOTAL = getSamplesToUpdateSize();

		handleSampleListUpdate(contextError);

		long end = System.currentTimeMillis();
		Date END_CRON_DATE = new Date();
		
		handleRequestDuration(Global.class, start, end);

		buildAndSendMail(END_CRON_DATE, NB_TOTAL);

		logger.info("End reporting synchro");
	}

	/**
	 * Méthode permettant de set un service de mail pour la classe.
	 * 
	 * @param mailServices Le service de mail à utiliser pour le CRON.
	 */
	public void setMailServices(MailServices mailServices) {
		this.mailServices = mailServices;
	}

	/**
	 * Méthode permettant de vider le champ "processesToLaunchDate" de toute la liste de samples.
	 */
	public void setProcessLaunchDateToNull() {
		SAMPLE_LIST.forEach(sample -> {
			clearProcessesToLaunchDate(sample.code);
		});
	}

	/**
	 * Méthode permettant de gérer la mise à jour de la liste de samples à mettre à jour.
	 * 
	 * @param contextError Le contexte de validation du CRON.
	 */
	private void handleSampleListUpdate(ContextValidation contextError) {
		while (getSamplesToUpdateSize() > 0) { 
			getSamplesToUpdate();

			setProcessLaunchDateToNull();

			SAMPLE_LIST.forEach(sample -> {
				try {
					Date startDate = new Date();

					handleWarningInProgressMail(startDate);

					updateProcesses(sample);

					Date endDate = new Date();

					handleSampleDuration(sample.code, startDate, endDate);

					if (sample.processes != null && sample.processes.size() > 0) {
						updateSample(sample, false);
					} else {
						updateSample(sample, true);
					}
				} catch(Throwable e) {
					e.printStackTrace();
					handleSampleError(sample, e, contextError);
					
					setProcessLaunchDateToNow(sample.code);
				}
			});

			System.gc();
		}
	}

	/**
	 * handleWarningInProgressMail()
	 * 
	 * @param startDate
	 */
	private void handleWarningInProgressMail(Date startDate) {
		long durationFromStart = startDate.getTime() - LAUNCH_DATE.getTime();

		if (durationFromStart > DURATION_DAY && !warningMailSent) {
			final String EXPEDITEUR = getEmailExpediteur();
			final HashSet<String> DESTINATAIRES = getEmailDestinataires();
			final String ENVIRONMENT = getEnvironment();
			final String INSTITUTE = getInstitute();

			String warningMessage = "<b>La dur&eacute;e d'ex&eacute;cution du CRON 'UpdateReportingData' vient de d&eacute;passer 24h.</b><br />";
			String warningSubject = "[NGL-DATA] [" + ENVIRONMENT + "] [" + INSTITUTE + "] UpdateReportingData - Alerte - Durée d'exécution en cours supérieure à 24h";

			try {
				mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, warningSubject, warningMessage);
				warningMailSent = true;
			} catch (MailServiceException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * tmpDirectory()
	 * 
	 * @return
	 */
	private static final String tmpDirectory() {
		return ConfigFactory.load().getString("mail.tmp.directory");
	}

	/**
	 * Méthode permettant de remettre une date dans le champ 'processesToLaunchDate'.
	 * Cette méthode est appelée quand une erreur survient lors du traitement d'un sample. Ca permet de le remettre dans le reporting du lendemain.
	 * 
	 * @param sampleCode Le code du sample où on veut remettre une 'processesToLaunchDate'.
	 */
	private void setProcessLaunchDateToNow(String sampleCode) {
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode), DBUpdate.set("processesToLaunchDate", new Date()));
	}

	/**
	 * Méthode permettant de gérer la mise en jour en base d'un sample.
	 * 
	 * @param sample Le sample à mettre à jour en base.
	 * @param unset Booléen permettant de savoir si on doit mettre à jour les champs "processes" et "processesStatistics" ou non.
	 */
	private void updateSample(Sample sample, boolean unset) {
		Date currentDate = new Date();

		DBUpdate.Builder dbUpdate = DBUpdate.set("processesUpdatedDate", LAUNCH_DATE)
											.set("sampleProcessesFinishUpdateDate", currentDate);

		if (unset) {
			dbUpdate = dbUpdate.unset("processes")
							   .unset("processesStatistics");
		} else {
			dbUpdate = dbUpdate.set("processes", sample.processes)
							   .set("processesStatistics", sample.processesStatistics);
		}

		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), dbUpdate);
	}

	/**
	 * Méthode permettant de lancer le vidage des variables avant chaque ré-exécution du CRON.
	 */
	private void handleClearCollections() {
		clearSampleList();

		clearMapDuration();

		clearMapSampleDuration();
	}

	/**
	 * Méthode permettant de vider la liste des samples modifiés par le CRON.
	 * 
	 * @see SAMPLE_LIST
	 */
	public void clearSampleList() {
		SAMPLE_LIST.clear();
	}

	/**
	 * Méthode permettant de vider la map contenant les durées d'exécution du CRON.
	 * 
	 * @see MAP_DURATION
	 */
	public void clearMapDuration() {
		MAP_DURATION.clear();
	}

	/**
	 * Méthode permettant de vider la map contenant les durées d'exécution de chaque sample modifiés.
	 * 
	 * @see MAP_SAMPLE_DURATION
	 */
	public void clearMapSampleDuration() {
		MAP_SAMPLE_DURATION.clear();
	}

	/**
	 * Méthode permettant de récupérer en base le nombre de samples à mettre à jour.
	 * Pour qu'un sample soit à mettre à jour, il faut qu'il ai un champ "processesToLaunchDate" non null.
	 * 
	 * @return Le nombre de samples à mettre à jour.
	 */
	private int getSamplesToUpdateSize() {
		return MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
			DBQuery.exists("processesToLaunchDate")
				   .notEquals("processesToLaunchDate", null)
				   .notIn("projectCodes", "CEA")
		)
		.count();
	}

	/**
	 * Méthode permettant de récupérer en base la liste des samples à mettre à jour.
	 * Pour qu'un sample soit à mettre à jour, il faut qu'il ai un champ "processesToLaunchDate" non null.
	 */
	private void getSamplesToUpdate() {
		SAMPLE_LIST.clear();

		List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
			DBQuery.exists("processesToLaunchDate")
				   .notEquals("processesToLaunchDate", null)
				   .notIn("projectCodes", "CEA")
		)
		.sort("processesToLaunchDate", Sort.ASC)
		.limit(100)
		.toList();

		SAMPLE_LIST.addAll(sampleList); 
	}

	/**
	 * Méthode permettant de gérer une erreur de mise à jour d'un sample :
	 * - On log l'erreur.
	 * 
	 * @param sample Le sample où l'erreur a eu lieu pendant la mise à jour.
	 * @param e L'erreur survenue sur le sample en paramètres.
	 */
	private void handleSampleError(Sample sample, Throwable e) {
		logger.error("Erreur sur le sample '" + sample.code + "' : " + e.getMessage());
	}

	/**
	 * Méthode permettant de gérer une erreur de mise à jour d'un sample :
	 * - On log l'erreur.
	 * - On ajoute l'erreur à un contexte de validation donné en argument.
	 * 
	 * @param sample Le sample où l'erreur a eu lieu pendant la mise à jour.
	 * @param e L'erreur survenue sur le sample en paramètres.
	 * @param contextValidation Le contexte de validation du CRON (les erreurs sont affichées à la fin).
	 */
	private void handleSampleError(Sample sample, Throwable e, ContextValidation contextValidation) {
		logger.error("Erreur sur le sample '" + sample.code + "' : " + e.getMessage());
		
		if (e.getMessage() != null) {
			contextValidation.addError(sample.code, e.getMessage());
		} else {
			contextValidation.addError(sample.code, "Erreur inconnue");
		}
	}

	/**
	 * Méthode permettant de gérer la durée d'une requête : 
	 * - Log de la durée.
	 * - Enregistrement de la durée pour reporting.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param start Timestamp de début de la requête.
	 * @param end Timestamp de fin de la requête.
	 * 
	 * @see registerRequestDuration(clazz, durationObjSec)
	 */
	private <T> void handleRequestDuration(Class<T> clazz, long start, long end) {
		long durationObj = end - start;
		long durationObjSec = (durationObj / 1000);

		logRequestDuration(clazz.getName(), durationObjSec);

		registerRequestDuration(clazz.getSimpleName(), durationObjSec);
	}

	/**
	 * Méthode permettant de gérer la durée de mise à jour d'un sample : 
	 * - Log de la durée.
	 * - Enregistrement de la durée pour reporting.
	 * 
	 * @param sampleCode Le code du sample mis à jour.
	 * @param start Date de début de mise à jour du sample.
	 * @param end Date de fin de mise à jour du sample.
	 */
	private <T> void handleSampleDuration(String sampleCode, Date start, Date end) {
		long durationObj = end.getTime() - start.getTime();
		long durationObjSec = (durationObj / 1000);

		logRequestDuration(sampleCode, durationObjSec);

		registerSampleDuration(sampleCode, start, end, durationObjSec);
	}

	/**
	 * Méthode permettant de logger la durée d'une requête.
	 * 
	 * @param name Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param duration La durée d'exécution de la requête.
	 */
	private void logRequestDuration(String name, long duration) {
		logger.debug("Durée pour : '" + name + "' : " + duration + " sec");
	}

	/**
	 * Méthode permettant d'enregistrer la durée d'exécution d'une requête pour ensuite faire le reporting.
	 * 
	 * @param className Une chaîne de caractères représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param duration La durée d'exécution de la requête.
	 * 
	 * @see MAP_DURATION
	 */
	private <T> void registerRequestDuration(String className, long duration) {
		MAP_DURATION.put(className, duration);
	}

	/**
	 * Méthode permettant d'enregistrer la durée de mise à jour d'un sample pour ensuite faire le reporting.
	 * 
	 * @param sampleCode Le code sample mis à jour.
	 * @param startDate La date de début de traitement du sample.
	 * @param endDate La date de fin de traitement du sample.
	 * @param duration La durée de la mise à jour.
	 * 
	 * @see MAP_SAMPLE_DURATION
	 */
	private void registerSampleDuration(String sampleCode, Date startDate, Date endDate, long duration) {
		DureeSample dureeSample = new DureeSample(startDate, endDate, duration);

		MAP_SAMPLE_DURATION.put(sampleCode, dureeSample);
	}

	/**
	 * Méthode permettant de construire le contenu du mail de reporting.
	 * 
	 * @param END_CRON_DATE La date de fin d'exécution du CRON.
	 * @param NB_TOTAL Le nombre total de samples à prendre en charge dans le CRON.
	 * 
	 * @return Une chaîne de caractères représentant le mail de reporting qui sera envoyé.
	 * 
	 * @throws UnsupportedEncodingException Si l'encodage spécifié pour le contenu du mail n'est pas bon.
	 */
	public String buildMailBody(Date END_CRON_DATE, int NB_TOTAL) throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();
		
		Long dureeGlobale = MAP_DURATION.get("Global");
		Double dureeGlobaleHeure = Double.valueOf(dureeGlobale != null ? (dureeGlobale / 3600.0) : 0);
		DecimalFormat decForm = new DecimalFormat("##.##");

		message.append("<b>Durée globale : " + (dureeGlobale != null ? dureeGlobale : 0) + " secondes / " + decForm.format(dureeGlobaleHeure) + " heures.</b><br /><br />");

		message.append("Date de lancement du CRON : " + LAUNCH_DATE + ".<br /><br />");

		message.append("Nombre de samples à prendre en charge : " + NB_TOTAL + ".<br /><br />");

		message.append("Date de fin du CRON : " + END_CRON_DATE + ".<br /><br />");

		message.append("Durée pour chaque sample (sample code, date de début de traitement, date de fin de traitement, durée de traitement en secondes) : <br />");

		MAP_SAMPLE_DURATION.entrySet().stream().limit(SEUIL_PJ).forEach(((Map.Entry<String, DureeSample> entry) -> {
			message.append(entry.getKey() + "," + entry.getValue().startDate + "," + entry.getValue().endDate + "," + entry.getValue().duration + "<br />");
		})); 

		if (MAP_SAMPLE_DURATION.size() > SEUIL_PJ) {
			message.append("La liste complète des samples est en pièce-jointe du mail.<br /><br />");
		}

		return new String(message.toString().getBytes(), "iso-8859-1");
	}

	/**
	 * Méthode permettant de récupérer l'expéditeur du mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant l'expéditeur du mail de reporting.
	 */
	public String getEmailExpediteur() {
		return ConfigFactory.load().getString(EXPEDITEUR_KEY); 
	}

	/**
	 * Méthode permettant de récupérer les destinataires qui vont recevoir le mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant le / les destinataires du mail de reporting.
	 */
	public HashSet<String> getEmailDestinataires() {
		final String DESTINATAIRES = ConfigFactory.load().getString(DESTINATAIRES_KEY);   
		return new HashSet<String>(Arrays.asList(DESTINATAIRES.split(",")));
	}

	/**
	 * Méthode permettant de récupérer l'environnement pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'environnement dans lequel le CRON est exécuté.
	 */
	public String getEnvironment() {
		return ConfigFactory.load().getString(ENV_KEY);
	}

	/**
	 * Méthode permettant de récupérer l'institut pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'institut pour lequel le CRON est exécuté.
	 */
	public String getInstitute() {
		return NGL_CONFIG.getInstitute();
	}

	/**
	 * Méthode permettant de construire le mail de reporting et de l'envoyer.
	 * 
	 * @param END_CRON_DATE La date de fin d'exécution du CRON.
	 * @param NB_TOTAL Le nombre total d'échantillons traités.
	 * 
	 * @see buildMailBody()
	 * @see MAIL_SERVICES.sendMail(EXPEDITEUR, destinataires, SUBJECT, message)
	 */
	public void buildAndSendMail(Date END_CRON_DATE, int NB_TOTAL) {
		final String EXPEDITEUR = getEmailExpediteur();
		final HashSet<String> DESTINATAIRES = getEmailDestinataires();
		final String ENVIRONMENT = getEnvironment();
		final String INSTITUTE = getInstitute();

		try {
			String message = buildMailBody(END_CRON_DATE, NB_TOTAL);
			String subject = "[NGL-DATA] [" + ENVIRONMENT + "] [" + INSTITUTE + "] Exécution de UpdateReportingData terminée";

			if (needAttachment()) {		
				Date currentDate = new Date();

				String attachment = buildMailAttachment();
				String fileName = "rapport-reporting-nocturne_UpdateReportingData_" + currentDate.getTime() + ".txt";

				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpDirectory() + fileName));
				writer.write(attachment);
				writer.close();

				File file = new File(tmpDirectory() + fileName);
				int ALMOST_ONE_MB = 900000;

				if (file.length() > ALMOST_ONE_MB) {
					message += "Le fichier de log est trop volumineux. Il est disponible &agrave; cette adresse : " + file.getAbsolutePath();

					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, subject, message);
				} else {
					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, subject, message, file);
				}	
			} else {
				mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, subject, message);
			}
		} catch (IOException | MailServiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode permettant de construire la pièce-jointe du mail de warning du reporting.
	 * 
	 * @return Une chaîne de caractères représentant la pièce-jointe du mail de warning du reporting.
	 */
	public String buildWarningMailBody() throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();

		long dureeGlobale = MAP_DURATION.get("Global");
		long dureeGlobaleHeure = dureeGlobale / 3600;

		message.append("<b>La durée d'exécution du CRON 'UpdateReportingData' a été supérieure à 24h.</b><br />");
		message.append("<b>Durée globale : " + dureeGlobale + " secondes / " + dureeGlobaleHeure + " heures.</b>");

		return new String(message.toString().getBytes(), "iso-8859-1");
	}

	/**
	 * Méthode permettant de construire la pièce-jointe du mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant la pièce-jointe du mail de reporting.
	 */
	public String buildMailAttachment() throws UnsupportedEncodingException {
		StringBuilder attachment = new StringBuilder();

		attachment.append("Durée pour chaque sample (sample code, date de début de traitement, date de fin de traitement, durée de traitement en secondes) : \n");

		MAP_SAMPLE_DURATION.entrySet().stream().forEach(((Map.Entry<String, DureeSample> entry) -> {
			attachment.append(entry.getKey() + "," + entry.getValue().startDate + "," + entry.getValue().endDate + "," + entry.getValue().duration + "\n");
		})); 

		return new String(attachment.toString().getBytes(), "utf-8");
	}

	/**
	 * Méthode permettant de savoir si on doit ajouter une pièce-jointe au mail de reporting.
	 * La règle est la suivante : si on a plus de samples que le seuil (aujourd'hui à 100), alors on doit ajouter une pièce-jointe.
	 * 
	 * @return true si on a besoin d'ajouter une pièce-jointe au mail de reporting. false sinon.
	 * 
	 * @see SEUIL_PJ
	 */
	public boolean needAttachment() {
		return MAP_SAMPLE_DURATION.size() > SEUIL_PJ;
	}

	/**
	 * Méthode permettant de supprimer le champ "processesToLaunchDate".
	 * Elle est appelée lorsque le CRON commence le traitement sur le sample.
	 */
	public void clearProcessesToLaunchDate(String sampleCode) {
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode), DBUpdate.unset("processesToLaunchDate"));
	}
	
	/**
	 * Méthode permettant de lancer le calcul du champ "processes" d'un sample.
	 * 
	 * @param sample Le sample à mettre à jour.
	 */
	public void updateProcesses(Sample sample) {
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", sample.code))
				.toList();
		
		sample.processes = processes.parallelStream()
					.map(process -> convertToSampleProcess(sample, process))
					.collect(Collectors.toList());

		handleReadsetBeforeMarch2015(sample);

		computeStatistics(sample);
	}

	/**
	 * Méthode permettant de chercher si un sample date d'avant SQ (30 Mars 2015) et si on doit "simuler" un readset.
	 * 
	 * @param sample Le sample où on vérifie sa date de création.
	 * 
	 * @see getReadSetBeforeNGLSQ(sample)
	 */
	private void handleReadsetBeforeMarch2015(Sample sample) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			Date march2015 = sdf.parse("2015/03/30");

			if (sample.traceInformation.creationDate.before(march2015)) {
				SampleProcess spWithoutProcess = getReadSetBeforeNGLSQ(sample);
	
				if (spWithoutProcess != null) {
					sample.processes.add(spWithoutProcess);
				}
			}
		} catch (ParseException e) {
			handleSampleError(sample, e);
		}
	}

	/**
	 * Méthode permettant de faire des statistiques sur le champ "processes" d'un sample.
	 * 
	 * @param sample Le sample à mettre à jour.
	 */
	public void computeStatistics(Sample sample) {
		if (sample.processes != null) {
			sample.processesStatistics = new SampleProcessesStatistics();	
			sample.processesStatistics.processTypeCodes = sample.processes.stream().filter(p -> p.typeCode != null).collect(Collectors.groupingBy(p -> p.typeCode, Collectors.counting()));
			sample.processesStatistics.processCategoryCodes = sample.processes.stream().filter(p -> p.categoryCode != null).collect(Collectors.groupingBy(p -> p.categoryCode, Collectors.counting()));
			sample.processesStatistics.readSetTypeCodes = sample.processes.stream().filter(p -> p.readsets != null).map(p -> p.readsets).flatMap(List::stream).collect(Collectors.groupingBy(r -> r.typeCode, Collectors.counting()));
		}
	}

	/**
	 * Méthode permettant de convertir un sample et un process en un objet "SampleProcess".
	 * 
	 * @param sample Le sample à convertir.
	 * @param process Le process à convertir.
	 * 
	 * @return Un objet "SampleProcess" représentant le sample et le process en argument de la méthode.
	 */
	public SampleProcess convertToSampleProcess(Sample sample, Process process) {
		SampleProcess sampleProcess = new SampleProcess();
		sampleProcess.code= process.code;
		sampleProcess.typeCode= process.typeCode;
		sampleProcess.categoryCode= process.categoryCode;
		sampleProcess.state= process.state;
		sampleProcess.state.historical=null;
		sampleProcess.traceInformation= process.traceInformation;
		sampleProcess.sampleOnInputContainer = process.sampleOnInputContainer;
		
		if (process.properties != null && process.properties.size() > 0) {
			sampleProcess.properties = process.properties;			
		}

		sampleProcess.currentExperimentTypeCode = process.currentExperimentTypeCode;

		if (process.experimentCodes != null && process.experimentCodes.size() > 0) {
			List<SampleExperiment> experiments  = transformExperimentsToSampleExperiments(process);

			if (experiments != null && experiments.size() > 0) {
				sampleProcess.experiments = experiments;
			}
		}

		if (process.outputContainerCodes != null && process.outputContainerCodes.size() > 0) {
			List<SampleReadSet> readsets = getSampleReadSets(sample, process);

			if (readsets != null && readsets.size() > 0) {
				sampleProcess.readsets = readsets;
			}
		}

		List<String> transformationCodes = getTransformationCodesForProcessTypeCode(process);
		Integer nbExp = 0;

		if (process.experimentCodes != null && process.experimentCodes.size() > 0 && transformationCodes != null && transformationCodes.size() > 0) {
			nbExp = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes)
						.in("typeCode", transformationCodes)
						.in("state.code", Arrays.asList("IP","F"))).count();
			
			try {
				sampleProcess.progressInPercent = (new BigDecimal((nbExp.floatValue() / Integer.valueOf(nbExpPositionInProcessType.get(process.typeCode)).floatValue())*100.00)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			} catch (Exception e) {
				handleSampleError(sample, e);
			}
		} else {
			sampleProcess.progressInPercent = null;			
		}

		return sampleProcess;
	}
			
	/**
	 * Méthode permettant de récupérer une liste de codes transformation (type d'expériences) à partir d'un process en entrée.
	 * 
	 * @param process Le process où on va chercher les codes transformation.
	 * 
	 * @return Une liste de chaîne de caractères avec les codes transformation du process en entrée.
	 */
	private List<String> getTransformationCodesForProcessTypeCode(Process process) {
		List<String> transformationCodes;

		if (transformationCodesByProcessTypeCode.containsKey(process.typeCode)) {
			transformationCodes = transformationCodesByProcessTypeCode.get(process.typeCode);
		} else {
			transformationCodes = ExperimentType.find.get().findByProcessTypeCode(process.typeCode,true).stream().map(e -> e.code).collect(Collectors.toList());
			transformationCodesByProcessTypeCode.put(process.typeCode, transformationCodes);
			
			Integer nbPos = ExperimentType.find.get().countDistinctExperimentPositionInProcessType(process.typeCode);
			nbExpPositionInProcessType.put(process.typeCode, nbPos);
		}
		
		return transformationCodes;
	}

	/**
	 * Méthode permettant de transformer (niveau objet) à partir d'un process en entrée une liste d'expériences en objet "SampleExperiment". 
	 * Les expériences sont récupérées via le champ "experimentCodes" du process donné en entrée.
	 * 
	 * @param process Le process où on va récupérer les expériences à transformer.
	 * 
	 * @return Une liste d'objets "SampleExperiment" correspondant aux expériences du process en entrée.
	 * 
	 * @see convertToSampleExperiments() pour la conversion.
	 */
	private List<SampleExperiment> transformExperimentsToSampleExperiments(Process process) {		
		List<SampleExperiment> sampleExperiments = new ArrayList<>();

		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes))
			.cursor.forEach(experiment -> {
				sampleExperiments.addAll(convertToSampleExperiments(process, experiment));
			}
		);

		return sampleExperiments;
	}
	
	/**
	 * Méthode utilisée pour créer une liste d'objets "SampleExperiment" à partir d'un process donné et d'une expérience.
	 * 
	 * @param process Le process de l'expérience en entrée.
	 * @param experiment L'expérience à convertir.
	 * 
	 * @return Une liste d'objets "SampleExperiment".
	 * 
	 * @see transformExperimentsToSampleExperiments() pour le contexte d'appel de la méthode.
	 */
	public List<SampleExperiment> convertToSampleExperiments(Process process, Experiment experiment) {
		List<SampleExperiment> sampleExperiments = new ArrayList<>();
		Set<String> containerCodes = new TreeSet<>();
		containerCodes.add(process.inputContainerCode);
		
		if (process.outputContainerCodes != null) {
			containerCodes.addAll(process.outputContainerCodes);
		}

		experiment.atomicTransfertMethods.parallelStream().forEach(atm -> {
			if (OneToVoidContainer.class.isInstance(atm)) {
				atm.inputContainerUseds.forEach(icu -> {
					if (containerCodes.contains(icu.code)) {
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}					
				});
			} else {
				atm.inputContainerUseds.forEach(icu -> {
					if (atm.outputContainerUseds != null) {
						atm.outputContainerUseds.forEach(ocu -> {
							if (ocu.code != null && containerCodes.containsAll(Arrays.asList(icu.code, ocu.code))) {
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, ocu);
								sampleExperiments.add(sampleExperiment);
							} else if(containerCodes.contains(icu.code)) {
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
								sampleExperiments.add(sampleExperiment);
							}
						});
					} else if(containerCodes.contains(icu.code)) {
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}
				});	
			}
		});
		
		return sampleExperiments;
	}

	/**
	 * Méthode permettant d'extraire un ensemble de propriété ("instrumentProperties" et "experimentProperties")
	 * à partir d'une expérience, d'un input container used et d'un output container used. 
	 * Les propriétés extraites sont ajoutées dans une map et sont ajoutées uniquement si elles ne sont pas de type "file" ou "img".
	 * 
	 * @param experiment L'expérience où on veut extraire les propriétés.
	 * @param icu L'input container used où on veut extraire les propriétés.
	 * @param ocu L'output container used où on veut extraire les propriétés.
	 * 
	 * @return Une map contenant l'ensemble des propriétés extraites.
	 * 
	 * @see filterProperties() pour l'extraction des propriétés.
	 */
	private Map<String, PropertyValue> computeExperimentProperties(Experiment experiment, InputContainerUsed icu, OutputContainerUsed ocu) {
		Map<String, PropertyValue> finalProperties = new HashMap<>();

		if (experiment.experimentProperties != null) finalProperties.putAll(filterProperties(experiment.experimentProperties));
		if (experiment.instrumentProperties != null) finalProperties.putAll(filterProperties(experiment.instrumentProperties));
		if (icu.experimentProperties        != null) finalProperties.putAll(filterProperties(icu.experimentProperties));
		if (icu.instrumentProperties        != null) finalProperties.putAll(filterProperties(icu.instrumentProperties));
		if (ocu != null) {
			if (ocu.experimentProperties != null) finalProperties.putAll(filterProperties(ocu.experimentProperties));
			if (ocu.instrumentProperties != null) finalProperties.putAll(filterProperties(ocu.instrumentProperties));
		}

		return finalProperties;
	}

	/**
	 * Méthode permettant de filtrer un ensemble de propriétés (sous forme de map).
	 * 
	 * @param properties Une map contenant les propriétés à extraire.
	 * 
	 * @return Une map de propriétés qui correspondent aux filtres de la méthode. Principalement : pas de type "img" et pas de type "file".
	 * 
	 * @see computeExperimentProperties() pour l'usage de la méthode.
	 */
	public Map<String, PropertyValue> filterProperties(Map<String, PropertyValue> properties) {
		return properties.entrySet().parallelStream()
				.filter(entry -> entry.getValue() != null && !entry.getValue()._type.equals(PropertyValue.imgType) && !entry.getValue()._type.equals(PropertyValue.fileType))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}
	
	/**
	 * Méthode permettant de transformer à partir d'un sample et d'un process une liste d'objets "SampleReadSet".
	 * 
	 * @param sample Le sample à transformer.
	 * @param process Le process à transformer.
	 * 
	 * @return Une liste d'objets "SampleReadSet" représentant le sample et le process en entrée.
	 */
	private List<SampleReadSet> getSampleReadSets(Sample sample, Process process) {
		List<SampleReadSet> sampleReadSets = new ArrayList<>();
		Set<String> tags = procWorkflowHelper.getTagAssignFromProcessContainersUpdateReporting(process);
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", process.outputContainerCodes).in("sampleCode", process.sampleCodes).in("projectCode", process.projectCodes),
				keys)
		.cursor
		.forEach(readset -> {
			if(readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&& readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) 
					&& null!=tags 
					&& tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString())){
				sampleReadSets.add(convertToSampleReadSet(readset));
			} else if(readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&& !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) 
					&& null != tags
					&& tags.contains("_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString())){
				sampleReadSets.add(convertToSampleReadSet(readset));
			} else if(!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&& !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)){
				sampleReadSets.add(convertToSampleReadSet(readset));
			}
		});

		return sampleReadSets;
	}

	/**
	 * Méthode permettant de convertir un readset en un objet "SampleReadset".
	 * 
	 * @param readset Le readset à convertir.
	 * 
	 * @return Un objet "SampleReadset" représentant le readset donné en argument de la méthode.
	 */
	public SampleReadSet convertToSampleReadSet(ReadSet readset) {
		SampleReadSet sampleReadSet = new SampleReadSet();
		sampleReadSet.code = readset.code;
		sampleReadSet.typeCode = readset.typeCode;
		sampleReadSet.state = readset.state;
		sampleReadSet.state.historical = null;
		sampleReadSet.runCode = readset.runCode;
		sampleReadSet.runTypeCode = readset.runTypeCode;
		sampleReadSet.runSequencingStartDate = readset.runSequencingStartDate;
		
		sampleReadSet.productionValuation = readset.productionValuation;   
		sampleReadSet.bioinformaticValuation = readset.bioinformaticValuation; 
		sampleReadSet.sampleOnContainer = readset.sampleOnContainer; 

		BasicDBObject keys = new BasicDBObject();
		
		if(readset.typeCode.equals("rsnanopore")) {
			keys.put("code", 1);
			keys.put("treatments.readQuality.default.1DForward", 1);
			keys.put("treatments.readQuality.code", 1);
			keys.put("treatments.readQuality.typeCode", 1);
			keys.put("treatments.readQuality.categoryCode", 1);
			keys.put("treatments.ngsrg.default.1DForward", 1);
			keys.put("treatments.ngsrg.code", 1);
			keys.put("treatments.ngsrg.typeCode", 1);
			keys.put("treatments.ngsrg.categoryCode", 1);
		}else {
			keys.put("code", 1);
			keys.put("treatments.ngsrg", 1);
		}
		
		ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readset.code, keys);
		sampleReadSet.treatments = rs.treatments;

		return sampleReadSet;
	}

	/**
	 * Méthode permettant de chercher les readsets d'un sample donné.
	 * Cette méthode s'exécute uniquement pour les samples d'avant SQ, donc ayant une traceInformation avant le 30 Mars 2015
	 * 
	 * @param sample Le sample cherché.
	 * 
	 * @return Un objet "SampleProcess" comprenant les readsets du sampel donné en argument
	 * avec des process bouchonnés car non existants dans SQ.
	 */
	public SampleProcess getReadSetBeforeNGLSQ(Sample sample) {
		List<SampleReadSet> list = new ArrayList<>();

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			Date march2015 = sdf.parse("2015/03/30");

			MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,
						ReadSet.class,
						DBQuery .lessThan("runSequencingStartDate", march2015)
								.is("sampleCode", sample.code)).toList().forEach(readset -> {
				list.add(convertToSampleReadSet(readset));		
			}); 
		} catch (ParseException e) {
			handleSampleError(sample, e);
		}
					
		SampleProcess sampleProcess = null;

		if (CollectionUtils.isNotEmpty(list)) {
			sampleProcess = new SampleProcess();
			sampleProcess.typeCode = "Old LIMS";
			sampleProcess.readsets = list;
		}
			
		return sampleProcess;
	}
}