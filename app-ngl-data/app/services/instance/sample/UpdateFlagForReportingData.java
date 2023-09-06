package services.instance.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.mongodb.MongoException;
import com.typesafe.config.ConfigFactory;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import net.sourceforge.htmlunit.corejs.javascript.tools.shell.Global;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import validation.ContextValidation;

/**
 * Script qui permet de détecter les samples à mettre à jour dans le CRON reporting data.
 * Conditions possibles pour qu'un sample soit à mettre à jour :
 * - Un process avec ce code sample associé (champ "sampleCodes") a été modifié ou créé dans les dernières 24h.
 * - Un readset avec ce code sample associé (champ "sampleCode") a été modifié ou créé dans les dernières 24h.
 * - Une expérience avec ce code sample associé (champ "sampleCodes") a été modifié ou créé dans les dernières 24h.
 * 
 * Ce script est intégré sous forme de CRON dans NGL.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 * 
 * @see ImportDataCNS pour la définition des CRON au CNS.
 * @see ImportDataCNG pour la définition des CRON au CNG.
 */
public class UpdateFlagForReportingData extends AbstractImportData {

	private MailServices mailServices = new MailServices();

	private static final int START_HOUR = 20;

	private static final int START_MINUTES = 0;

	private static final int START_SECONDES = 0;

	private static final long DAY = 24 * 60 * 60 * 1000;

	private static final int SEUIL_PJ = 100;

	private static final String EXPEDITEUR_KEY = "reporting.email.from";

	private static final String DESTINATAIRES_KEY = "reporting.email.to";

	private static final String ENV_KEY = "ngl.env";

	public Map<String, Long> MAP_DURATION = new TreeMap<String, Long>();

	public Set<String> ASC_SAMPLES = new TreeSet<String>();

	public Set<String> SAMPLES = new TreeSet<String>();

	private Date CUSTOM_END_DATE;

	private Date END_DATE;

	private Date CUSTOM_START_DATE;

	private int NB_TRAITE = 100;
	
	/**
	 * Date de début pour l'intervalle de recherche des samples.
	 * Par défaut, AUJOURD'HUI - 24H. 
	 * Peut être re-définie si on donne des dates différentes dans le script.
	 */
	private Date START_DATE; 

	private NGLConfig NGL_CONFIG;

	@Inject
	public UpdateFlagForReportingData(NGLApplication app, NGLConfig config) {
		super("UpdateFlagForReportingData", app);
		this.NGL_CONFIG = config;
	}

	/**
	 * Méthode permettant d'écraser le comportement par défaut de la recherche (par défaut, on cherche sur les dernières 24 heures).
	 * Ici, on peut spécifier la liste de samples qu'on veut.
	 * 
	 * @param contextError Contexte de validation du CRON.
	 * @param sampleCodeList La liste des samples à mettre à jour.
	 */
	public void runImport(ContextValidation contextError, List<String> sampleCodeList) {
		logger.info("Start UpdateFlagForReportingData");

		END_DATE = new Date();
		START_DATE = new Date(END_DATE.getTime() - DAY);

		final Date START_CRON_DATE = new Date();
		final long start = System.currentTimeMillis();

		updateFoundSamplesWithToLaunchDate();

		final long end = System.currentTimeMillis();
		final Date END_CRON_DATE = new Date();

		handleRequestDuration(Global.class, start, end);

		TreeSet<String> processCodeList = new TreeSet<>();
		TreeSet<String> experimentCodeList = new TreeSet<>();
		TreeSet<String> readsetCodeList = new TreeSet<>();

		// Comme on a déjà une liste de samples, on peut pas avoir de liste de process / experiment / readset.

		buildAndSendMail(processCodeList, experimentCodeList, readsetCodeList, START_CRON_DATE, END_CRON_DATE);

		logger.info("End UpdateFlagForReportingData");
	}

	/**
	 * Méthode permettant d'écraser le comportement par défaut de la recherche (par défaut, on cherche sur les dernières 24 heures).
	 * Ici, on peut spécifier l'intervalle qu'on veut.
	 * 
	 * @param contextError Contexte de validation du CRON.
	 * @param startDate Date de début de la recherche de process / experiment / readset récemment modifiés.
	 * @param endDate Date de fin de la recherche de process / experiment / readset récemment modifiés.
	 * 
	 * @throws SQLException
	 * @throws DAOException
	 * @throws MongoException
	 * @throws RulesException
	 */
	public void runImport(ContextValidation contextError, Date startDate, Date endDate) throws SQLException, DAOException, MongoException, RulesException {
		CUSTOM_START_DATE = startDate;
		CUSTOM_END_DATE = endDate;

		runImport(contextError);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void runImport(ContextValidation contextError) throws SQLException, DAOException, MongoException, RulesException {
		logger.info("Start UpdateFlagForReportingData");

		if (CUSTOM_END_DATE != null && CUSTOM_START_DATE != null) {
			END_DATE = CUSTOM_END_DATE;
			START_DATE = CUSTOM_START_DATE;

			CUSTOM_END_DATE = null;
			CUSTOM_START_DATE = null;
		} else {
			END_DATE = new Date();
			END_DATE.setHours(START_HOUR);
			END_DATE.setMinutes(START_MINUTES);
			END_DATE.setSeconds(START_SECONDES);

			START_DATE = new Date(END_DATE.getTime() - DAY);
			START_DATE.setHours(START_HOUR);
			START_DATE.setMinutes(START_MINUTES);
			START_DATE.setSeconds(START_SECONDES);
		}

		final Date START_CRON_DATE = new Date();
		final long start = System.currentTimeMillis();

		handleClearCollections();

		final TreeSet<String> processCodeList = new TreeSet<String>();

		int size = getRecentObjectsRequestSize(Process.class, InstanceConstants.PROCESS_COLL_NAME);
		int skip = 0;

		logger.info("Récupération des process");

		while (skip < size) {
			getRecentObjects(Process.class, InstanceConstants.PROCESS_COLL_NAME, skip).forEach(process -> {
				processCodeList.add(process.code);

				process.sampleCodes.forEach(sampleCode -> {
					addSamplesToReportingLaunchList(sampleCode);
				});
			});

			skip += NB_TRAITE;
			System.gc();
		}

		final TreeSet<String> experimentCodeList = new TreeSet<String>();

		size = getRecentObjectsRequestSize(Experiment.class, InstanceConstants.EXPERIMENT_COLL_NAME);
		skip = 0;

		logger.info("Récupération des expériences");

		while (skip < size) {
			getRecentObjects(Experiment.class, InstanceConstants.EXPERIMENT_COLL_NAME, skip).forEach(exp -> {
				experimentCodeList.add(exp.code);

				exp.sampleCodes.forEach(sampleCode -> {
					addSamplesToReportingLaunchList(sampleCode);
				});
			});

			skip += NB_TRAITE;
			System.gc();
		}

		final TreeSet<String> readsetCodeList = new TreeSet<String>();

		size = getRecentObjectsRequestSize(ReadSet.class, InstanceConstants.READSET_ILLUMINA_COLL_NAME);
		skip = 0;

		logger.info("Récupération des readsets");

		while (skip < size) {
			getRecentObjects(ReadSet.class, InstanceConstants.READSET_ILLUMINA_COLL_NAME, skip).forEach(rs -> {
				readsetCodeList.add(rs.code);
				
				addSamplesToReportingLaunchList(rs.sampleCode);
			}); 

			skip += NB_TRAITE;
			System.gc();
		}

		logger.info("Mise à jour des samples avec une launchDate");

		updateFoundSamplesWithToLaunchDate();

		final long end = System.currentTimeMillis();
		final Date END_CRON_DATE = new Date();

		handleRequestDuration(Global.class, start, end); 

		buildAndSendMail(processCodeList, experimentCodeList, readsetCodeList, START_CRON_DATE, END_CRON_DATE); 

		logger.info("End UpdateFlagForReportingData");
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
	 * Méthode permettant de set un service de mail pour la classe.
	 * 
	 * @param mailServices Le service de mail à utiliser pour le CRON.
	 */
	public void setMailServices(MailServices mailServices) {
		this.mailServices = mailServices;
	}

	/**
	 * Méthode permettant de gérer le cas de sample qui ont une ascendance.
	 * On cherche donc dans le 'life.path'.
	 * 
	 * @param sample Le sample où on va chercher l'ascendance.
	 * 
	 * @return
	 */
	private Set<String> handleParentSample(Sample sample) {
		Set<String> parentSamplesList = new TreeSet<>();

		if (sample.life != null) {
			List<String> sampleCodeFromChild = Arrays.asList(sample.life.path.split(","));
			sampleCodeFromChild = sampleCodeFromChild.stream().filter(sc -> sc.length() > 0).collect(Collectors.toList());

			sampleCodeFromChild.forEach(scFChild -> {
				parentSamplesList.add(scFChild);
			});
		}

		return parentSamplesList;
	}

	/**
	 * Méthode permettant de lancer le vidage des variables avant chaque ré-exécution du CRON.
	 */
	private void handleClearCollections() {
		clearMapDuration();

		clearSampleCodes();
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
	 * Méthode permettant de vider la map contenant les samples (+ ascendants) récemment modifiés.
	 * 
	 * @see SAMPLES
	 * @see ASC_SAMPLES
	 */
	public void clearSampleCodes() {
		SAMPLES.clear();
		ASC_SAMPLES.clear();
	}

	/**
	 * Méthode permettant de mettre à jour les samples identifiés comme récemment modifiés.
	 * On met come valeur au champ "processesToLaunchDate" la date actuelle.
	 * Pour trouver les samples récemment modifiés, on itère sur la map "SAMPLES".
	 * 
	 * @see SAMPLES
	 */
	private void updateFoundSamplesWithToLaunchDate() {
		long start = System.currentTimeMillis();

		SAMPLES.forEach(sampleCode -> {
			Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			sample.setProcessesToLaunchDate(new Date());

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, 
				Sample.class, 
				DBQuery.is("code", sample.code),
				DBUpdate.set("processesToLaunchDate", sample.getProcessesToLaunchDate())); 

			Set<String> parentSampleList = handleParentSample(sample);

			parentSampleList.forEach(parentSampleCode -> {
				Sample parentSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, parentSampleCode);
				parentSample.setProcessesToLaunchDate(new Date());

				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, 
					Sample.class, 
					DBQuery.is("code", parentSample.code),
					DBUpdate.set("processesToLaunchDate", parentSample.getProcessesToLaunchDate())); 
			});

			ASC_SAMPLES.addAll(parentSampleList);
		});

		long end = System.currentTimeMillis();

		handleRequestDuration(Duration.class, start, end);
	}

	/**
	 * Méthode permettant d'ajouter un sample à la liste des samples à mettre à jour.
	 * 
	 * @param sampleCode Le code sample à mettre à jour.
	 * 
	 * @see SAMPLES
	 */
	public <T> void addSamplesToReportingLaunchList(String sampleCode) {
		SAMPLES.add(sampleCode);
	}

	/**
	 * Méthode permettant de logger la durée d'une requête.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param duration La durée d'exécution de la requête.
	 */
	private <T> void logRequestDuration(Class<T> clazz, long duration) {
		logger.debug("Durée pour : '" + clazz.getSimpleName() + "' : " + duration + " sec");
	}

	/**
	 * Méthode permettant de logger le résultat d'une requête.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param listSize Le nombre de résultats de la requête.
	 */
	private <T> void logRequestResults(Class<T> clazz, long listSize) {
		logger.debug(clazz.getSimpleName() + " récemment modifiés ou récemment créés : " + listSize + " résultat(s)");
	}

	/**
	 * Méthode permettant d'enregistrer la durée d'exécution d'une requête pour ensuite faire le reporting.
	 * 
	 * @param className Une chaîne de caractères représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param duration La durée d'exécution de la requête.
	 * 
	 * @see MAP_DURATION
	 */
	public <T> void registerRequestDuration(String className, long duration) {
		MAP_DURATION.put(className, duration);
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
	 * @see logRequestDuration(clazz, durationObjSec)
	 * @see registerRequestDuration(clazz, durationObjSec)
	 */
	private <T> void handleRequestDuration(Class<T> clazz, long start, long end) {
		long durationObj = end - start;
		long durationObjSec = (durationObj / 1000);

		logRequestDuration(clazz, durationObjSec);

		registerRequestDuration(clazz.getSimpleName(), durationObjSec);
	}

	/**
	 * Méthode permettant de gérer la durée d'une requête : 
	 * - Log de la durée.
	 * - Log des résultats.
	 * - Enregistrement de la durée pour reporting.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param start Timestamp de début de la requête.
	 * @param end Timestamp de fin de la requête.
	 * @param listSize Le nombre de résultats de la requête.
	 * 
	 * @see handleRequestDuration(clazz, start, end)
	 * @see logRequestResults(clazz, listSize)
	 */
	private <T> void handleRequestDuration(Class<T> clazz, long start, long end, long listSize) {
		handleRequestDuration(clazz, start, end);
		
		logRequestResults(clazz, listSize);
	}

	/**
	 * Méthode permettant de récupérer un curseur Mongo représentant un objet récemment modifié.
	 * Parmis les objets possibles dans la recherche : Process, Experiment ou Readset.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param collectionName Le nom de la collection où la recherche est faite.
	 * @param skip
	 * 
	 * @return Un curseur Mongo contenant le résultat de la recherche pour l'entitée cherchée (Process, Experiment ou Readset).
	 */
	private <T extends DBObject> DBCursor<T> getRecentObjectsRequest(Class<T> clazz, String collectionName, int skip) {
		return MongoDBDAO.find(collectionName, clazz, 
			DBQuery.or(
				DBQuery.greaterThanEquals("traceInformation.modifyDate", START_DATE)
						.lessThanEquals("traceInformation.modifyDate", END_DATE), 
				DBQuery.greaterThanEquals("traceInformation.creationDate", START_DATE)
						.lessThanEquals("traceInformation.creationDate", END_DATE)
			) 
		)
		.limit(NB_TRAITE)
		.skip(skip)
		.cursor;
	}

	/**
	 * getRecentObjectsRequestSize()
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param collectionName Le nom de la collection où la recherche est faite.
	 * 
	 * @return
	 */
	private <T extends DBObject> int getRecentObjectsRequestSize(Class<T> clazz, String collectionName) {
		return MongoDBDAO.find(collectionName, clazz, 
			DBQuery.or(
				DBQuery.greaterThanEquals("traceInformation.modifyDate", START_DATE)
						.lessThanEquals("traceInformation.modifyDate", END_DATE), 
				DBQuery.greaterThanEquals("traceInformation.creationDate", START_DATE)
						.lessThanEquals("traceInformation.creationDate", END_DATE)
			) 
		)
		.count();
	}

	/**
	 * Méthode permettant de chercher des objets récemment modifiés.
	 * Parmis les objets possibles dans la recherche : Process, Experiment ou Readset.
	 * 
	 * @param clazz Une classe représentant l'entité cherchée (Process, Experiment ou Readset).
	 * @param collectionName Le nom de la collection où la recherche est faite.
	 * @param skip
	 * 
	 * @return Un curseur Mongo contenant le résultat de la recherche pour l'entitée cherchée (Process, Experiment ou Readset).
	 */
	private <T extends DBObject> List<T> getRecentObjects(Class<T> clazz, String collectionName, int skip) {
		long start = System.currentTimeMillis();

		DBCursor<T> cursor = getRecentObjectsRequest(clazz, collectionName, skip);

		List<T> res = cursor.toArray();

		long end = System.currentTimeMillis();
		
		handleRequestDuration(clazz, start, end, cursor.size());

		return res;
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
	 * Méthode permettant de récupérer les destinataires qui vont recevoir le mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant le / les destinataires du mail de reporting.
	 */
	public HashSet<String> getEmailDestinataires() {
		final String DESTINATAIRES = ConfigFactory.load().getString(DESTINATAIRES_KEY);   
		return new HashSet<String>(Arrays.asList(DESTINATAIRES.split(",")));
	}

	/**
	 * Méthode permettant de construire le mail de reporting et de l'envoyer.
	 * Les paramètres de la méthode sont déclarés en final car on ne veut surtout pas qu'ils soient altérés avant le reporting.
	 * 
	 * @param PROCESS_CODES_LIST La liste des process récemment modifiés ou créés.
	 * @param EXP_CODES_LIST La liste des expériences récemment modifiées ou créées.
	 * @param RS_CODES_LIST La liste des readsets récemment modifiés ou créés.
	 * @param START_CRON_DATE Date de lancement du CRON.
	 * @param END_CRON_DATE Date de fin d'exécution du CRON.
	 * 
	 * @see buildMailBody()
	 * @see MAIL_SERVICES.sendMail(EXPEDITEUR, destinataires, SUBJECT, message)
	 */
	public void buildAndSendMail(final TreeSet<String> PROCESS_CODES_LIST, final TreeSet<String> EXP_CODES_LIST, final TreeSet<String> RS_CODES_LIST, final Date START_CRON_DATE, final Date END_CRON_DATE) {	
		final String EXPEDITEUR = getEmailExpediteur();
		final HashSet<String> DESTINATAIRES = getEmailDestinataires();
		final String ENVIRONMENT = getEnvironment();
		final String INSTITUTE = getInstitute();
	    
		final String SUBJECT = "[NGL-DATA] [" + ENVIRONMENT + "] [" + INSTITUTE + "] Exécution de UpdateFlagForReportingData terminée";

		try {
			String message = buildMailBody(PROCESS_CODES_LIST, EXP_CODES_LIST, RS_CODES_LIST, START_CRON_DATE, END_CRON_DATE);
			
			if (needAttachment(PROCESS_CODES_LIST, EXP_CODES_LIST, RS_CODES_LIST)) {
				Date currentDate = new Date();
				
				String attachment = buildMailAttachment(PROCESS_CODES_LIST, EXP_CODES_LIST, RS_CODES_LIST);
				String fileName = "rapport-reporting-nocturne_UpdateFlagForReportingData_" + currentDate.getTime() + ".txt";

				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpDirectory() + fileName));
				writer.write(attachment);
				writer.close();

				File file = new File(tmpDirectory() + fileName);
				int ALMOST_ONE_MB = 900000;

				if (file.length() > ALMOST_ONE_MB) {
					message += "Le fichier de log est trop volumineux. Il est disponible &agrave; cette adresse : " + file.getAbsolutePath();

					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, SUBJECT, message);
				} else {
					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, SUBJECT, message, file);
				}	
			} else {
				mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, SUBJECT, message);
			}
		} catch (IOException | MailServiceException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * needAttachment()
	 * 
	 * @param PROCESS_CODES_LIST La liste des process récemment modifiés ou créés.
	 * @param EXP_CODES_LIST La liste des expériences récemment modifiées ou créées.
	 * @param RS_CODES_LIST La liste des readsets récemment modifiés ou créés.
	 * 
	 * @return
	 */
	public boolean needAttachment(TreeSet<String> PROCESS_CODES_LIST, TreeSet<String> EXP_CODES_LIST, TreeSet<String> RS_CODES_LIST) {
		return SAMPLES.size() > SEUIL_PJ || PROCESS_CODES_LIST.size() > SEUIL_PJ || EXP_CODES_LIST.size() > SEUIL_PJ || RS_CODES_LIST.size() > SEUIL_PJ;
	}

	/**
	 * buildMailAttachment()
	 * 
	 * @param PROCESS_CODES_LIST La liste des process récemment modifiés ou créés.
	 * @param EXP_CODES_LIST La liste des expériences récemment modifiées ou créées.
	 * @param RS_CODES_LIST La liste des readsets récemment modifiés ou créés.
	 * 
	 * @return
	 * 
	 * @throws UnsupportedEncodingException Si l'encodage spécifié pour le contenu du mail n'est pas bon.
	 */
	public String buildMailAttachment(TreeSet<String> PROCESS_CODES_LIST, TreeSet<String> EXP_CODES_LIST, TreeSet<String> RS_CODES_LIST) throws UnsupportedEncodingException {
		StringBuilder attachment = new StringBuilder();

		if (!SAMPLES.isEmpty()) {
			attachment.append("Liste des samples devant recevoir une date sur le champ 'processesToLaunchDate' (CEA exclu par un mécanisme ultérieur) :\n");

			SAMPLES.stream().forEach(sampleCode -> {
				attachment.append(sampleCode + "\n");
			});
		}

		attachment.append("\n");

		if (!ASC_SAMPLES.isEmpty()) {
			attachment.append("Liste des ascendants devant recevoir une date sur le champ 'processesToLaunchDate' (projet CEA exclu par un mécanisme ultérieur) :\n");

			ASC_SAMPLES.stream().forEach(sampleCode -> {
				attachment.append(sampleCode + "\n");
			});
		}

		attachment.append("\n");

		if (!PROCESS_CODES_LIST.isEmpty()) {
			attachment.append("Liste des codes process récemment modifiés ou créés :\n");

			PROCESS_CODES_LIST.forEach(processCode -> {
				attachment.append(processCode + "\n");
			});
		}

		attachment.append("\n");

		if (!EXP_CODES_LIST.isEmpty()) {
			attachment.append("Liste des codes expériences récemment modifiées ou créées :\n");

			EXP_CODES_LIST.forEach(expCode -> {
				attachment.append(expCode + "\n");
			});
		}

		attachment.append("\n");
		
		if (!RS_CODES_LIST.isEmpty()) {
			attachment.append("Liste des codes readsets récemment modifiés ou créés :\n");

			RS_CODES_LIST.forEach(readsetCode -> {
				attachment.append(readsetCode + "\n");
			});
		}

		return new String(attachment.toString().getBytes(), "utf-8");
	}

	/**
	 * Méthode permettant de construire le contenu du mail de reporting.
	 * 
	 * @param PROCESS_CODES_LIST La liste des process récemment modifiés ou créés.
	 * @param EXP_CODES_LIST La liste des expériences récemment modifiées ou créées.
	 * @param RS_CODES_LIST La liste des readsets récemment modifiés ou créés.
	 * @param START_CRON_DATE Date de lancement du CRON.
	 * @param END_CRON_DATE Date de fin d'exécution du CRON.
	 * 
	 * @return Une chaîne de caractères représentant le mail de reporting qui sera envoyé.
	 * 
	 * @throws UnsupportedEncodingException Si l'encodage spécifié pour le contenu du mail n'est pas bon.
	 */
	public String buildMailBody(final TreeSet<String> PROCESS_CODES_LIST, final TreeSet<String> EXP_CODES_LIST, final TreeSet<String> RS_CODES_LIST, final Date START_CRON_DATE, final Date END_CRON_DATE) throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();

		Long dureeGlobale = MAP_DURATION.get("Global");
		Double dureeGlobaleHeure = Double.valueOf(dureeGlobale != null ? (dureeGlobale / 3600.0) : 0);
		DecimalFormat decForm = new DecimalFormat("##.##");

		message.append("<b>Durée globale : " + (dureeGlobale != null ? dureeGlobale : 0)  + " secondes / " + decForm.format(dureeGlobaleHeure) + " heures.</b><br /><br />");

		message.append("Date de lancement du CRON : " + START_CRON_DATE + ".<br /><br />");

		message.append("Date de début de la recherche des objets récemment modifiés : " + START_DATE + ".<br />");
		message.append("Date de fin de la recherche des objets récemment modifiés  : " + END_DATE + ".<br /><br />");

		Set<String> nbTotalSample = new TreeSet<String>();
		nbTotalSample.addAll(SAMPLES);
		nbTotalSample.addAll(ASC_SAMPLES);

		message.append("Nombre de samples à mettre à jour : " + nbTotalSample.size() + ".<br />");
		
		message.append("Durée de mise à jour du champ 'processesToLaunchDate' : " + (MAP_DURATION.get("Duration") != null ? MAP_DURATION.get("Duration") : 0) + " secondes.<br /><br />");
		
		message.append("Date de fin du CRON : " + END_CRON_DATE + ".<br /><br />");

		if (!SAMPLES.isEmpty()) {
			message.append("Liste des samples devant recevoir une date sur le champ 'processesToLaunchDate' (CEA exclu par un mécanisme ultérieur) :<br />");

			SAMPLES.stream().limit(SEUIL_PJ).forEach(sampleCode -> {
				message.append(sampleCode + "<br />");
			});

			if (SAMPLES.size() > SEUIL_PJ) {
				message.append("La liste complète des samples est en pièce-jointe du mail.<br /><br />");
			} else {
				message.append("<br />");
			}
		}

		if (!ASC_SAMPLES.isEmpty()) {
			message.append("Liste des ascendants devant recevoir une date sur le champ 'processesToLaunchDate' (projet CEA exclu par un mécanisme ultérieur) :<br />");

			ASC_SAMPLES.stream().limit(SEUIL_PJ).forEach(sampleCode -> {
				message.append(sampleCode + "<br />");
			});

			if (ASC_SAMPLES.size() > SEUIL_PJ) {
				message.append("La liste complète des ascendants est en pièce-jointe du mail.<br /><br />");
			} else {
				message.append("<br />");
			}
		}

		message.append("Durée de recherche des process récemment créés ou modifiés : " + (MAP_DURATION.get("Process") != null ? MAP_DURATION.get("Process") : 0) + " secondes.<br /><br />");

		if (!PROCESS_CODES_LIST.isEmpty()) {
			message.append("Liste des codes process récemment modifiés ou créés :<br />");

			PROCESS_CODES_LIST.stream().limit(SEUIL_PJ).forEach(processCode -> {
				message.append(processCode + "<br />");
			});

			if (PROCESS_CODES_LIST.size() > SEUIL_PJ) {
				message.append("La liste complète des process est en pièce-jointe du mail.<br /><br />");
			} else {
				message.append("<br />");
			}
		}

		message.append("Durée de recherche des expériences récemment créées ou modifiées : " + (MAP_DURATION.get("Experiment") != null ? MAP_DURATION.get("Experiment") : 0) + " secondes.<br /><br />");

		if (!EXP_CODES_LIST.isEmpty()) {
			message.append("Liste des codes expériences récemment modifiées ou créées :<br />");

			EXP_CODES_LIST.stream().limit(SEUIL_PJ).forEach(expCode -> {
				message.append(expCode + "<br />");
			});

			if (EXP_CODES_LIST.size() > SEUIL_PJ) {
				message.append("La liste complète des expériences est en pièce-jointe du mail.<br /><br />");
			} else {
				message.append("<br />");
			}
		}

		message.append("Durée de recherche des readsets récemment créés ou modifiés : " + (MAP_DURATION.get("ReadSet") != null ? MAP_DURATION.get("ReadSet") : 0) + " secondes.<br /><br />");
		
		if (!RS_CODES_LIST.isEmpty()) {
			message.append("Liste des codes readsets récemment modifiés ou créés :<br />");

			RS_CODES_LIST.stream().limit(100).forEach(readsetCode -> {
				message.append(readsetCode + "<br />");
			});

			if (RS_CODES_LIST.size() > SEUIL_PJ) {
				message.append("La liste complète des readsets est en pièce-jointe du mail.<br /><br />");
			} 
		}

		return new String(message.toString().getBytes(), "iso-8859-1");
	}
}