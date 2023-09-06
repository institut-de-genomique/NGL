package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgsAndBody;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;
import play.mvc.Http.MultipartFormData.FilePart;

/**
 * Script permettant de supprimer des ressources (readset, process, expérience) et de mettre à jour le champ 'processesToLaunchDate'
 * sur les samples 'associés' aux ressources supprimées.
 * 
 * Ce script est à utiliser dans un contexte de reporting nocturne.
 * 
 * @author Jordi CHARPENTIER jcharpen@genoscope.cns.fr 
 */
public class DeleteRessourceAndUpdateLaunchDate extends ScriptWithArgsAndBody<DeleteRessourceAndUpdateLaunchDate.Args> {

    private ExperimentsAPI expAPI;

	private ProcessesAPI procAPI;

	private ReadSetsAPI rsAPI;

	private static final String SHEET_READSETS = "Readsets";

	private static final String SHEET_PROCESSUS = "Processus";

	private static final String SHEET_EXPERIMENT = "Expériences";

	private boolean PROCESS_EMPTY = false;
	
	private boolean EXPERIMENT_EMPTY = false;

	private boolean READSET_EMPTY = false;

	private int COUNT_PROCESS = 0;

	private int COUNT_EXPERIMENT = 0;

	private int COUNT_READSET = 0;

	private static final String EXPEDITEUR_KEY = "error.mail.from";

	private static final String DESTINATAIRES_KEY = "error.mail.to";

	private static final String ENV_KEY = "ngl.env";

	private static final List<String> EXP_CODES_LIST = new ArrayList<>();

	private static final List<String> PROC_CODES_LIST = new ArrayList<>();

	private static final List<String> RS_CODES_LIST = new ArrayList<>();

	private MailServices mailServices = new MailServices();

	private NGLConfig NGL_CONFIG;

	public static class Args {

		/**
		 * Booléen permettant de savoir si on est en mode simulation (ou non).
		 * Si on est dans ce mode, rien n'est modifié en base, c'est utile pour le test.
		 */
		public boolean simulation = false;
	}

	@Inject
	public DeleteRessourceAndUpdateLaunchDate(ExperimentsAPI expAPI, ProcessesAPI procAPI, ReadSetsAPI rsAPI, NGLConfig nglConfig) {
		this.expAPI = expAPI;
		this.procAPI = procAPI;
		this.rsAPI = rsAPI;

		this.NGL_CONFIG = nglConfig;
	}

	@Override
	public void execute(Args args, RequestBody body) throws Exception { 		
		println("Début du script");

		if (args.simulation) {
			println("Mode simulation actif. Rien ne sera supprimé ni mis à jour.");
		} else {
			println("Mode simulation inactif. Tout sera supprimé et mis à jour.");
		}

		MultipartFormData<File> multiFormData = body.asMultipartFormData();

		if (multiFormData != null) {
			FilePart<File> filePart = multiFormData.getFile("xlsx");

			if (filePart != null) {
				File fxlsx = (File) filePart.getFile();
				FileInputStream fis = new FileInputStream(fxlsx);

				XSSFWorkbook workbook = new XSSFWorkbook(fis);
				XSSFSheet sheetProcess = workbook.getSheet(SHEET_PROCESSUS);

				if (sheetProcess != null) {			
					sheetProcess.rowIterator().forEachRemaining(row -> {
						handleProcessLine(row, args);
					});

					if (this.COUNT_PROCESS <= 1) {
						handleEmptyProcess();
					}
				} else {
					println("L'onglet '" + SHEET_PROCESSUS + "' n'existe pas dans le fichier Excel.");
				}

				XSSFSheet sheetExp = workbook.getSheet(SHEET_EXPERIMENT);

				if (sheetExp != null) {
					workbook.getSheet(SHEET_EXPERIMENT).rowIterator().forEachRemaining(row -> {
						handleExperimentLine(row, args);
					});

					if (this.COUNT_EXPERIMENT <= 1) {
						handleEmptyExperiment();
					}
				} else {
					println("L'onglet '" + SHEET_EXPERIMENT + "' n'existe pas dans le fichier Excel.");
				}

				XSSFSheet sheetRs = workbook.getSheet(SHEET_READSETS);

				if (sheetRs != null) {
					workbook.getSheet(SHEET_READSETS).rowIterator().forEachRemaining(row -> {	
						handleReadsetLine(row, args);
					});

					if (this.COUNT_READSET <= 1) {
						handleEmptyReadset();
					}
				} else {
					println("L'onglet '" + SHEET_READSETS + "' n'existe pas dans le fichier Excel.");
				}

				if (this.EXPERIMENT_EMPTY && this.PROCESS_EMPTY && this.READSET_EMPTY) {
					println("Les 3 onglets sont vides.");
				}

				if (!args.simulation) {
					if (needReportingMail()) {
						buildAndSendMail();
					}
				}
			} else {
				println("Le fichier Excel attendu en entrée n'est pas là.");
			}
		} else {
			println("Le fichier Excel attendu en entrée n'est pas là.");
		}

		println("Fin du script");
	}

	/**
	 * needReportingMail()
	 * 
	 * @return
	 */
	private boolean needReportingMail() {
		return !EXP_CODES_LIST.isEmpty() || !PROC_CODES_LIST.isEmpty() || !RS_CODES_LIST.isEmpty();
	}

	/**
	 * Méthode permettant de récupérer l'expéditeur du mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant l'expéditeur du mail de reporting.
	 */
	private String getEmailExpediteur() {
		return ConfigFactory.load().getString(EXPEDITEUR_KEY); 
	}

	/**
	 * Méthode permettant de récupérer les destinataires qui vont recevoir le mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant le / les destinataires du mail de reporting.
	 */
	private HashSet<String> getEmailDestinataires() {
		final String DESTINATAIRES = ConfigFactory.load().getString(DESTINATAIRES_KEY);   
		return new HashSet<String>(Arrays.asList(DESTINATAIRES.split(",")));
	}

	/**
	 * Méthode permettant de récupérer l'environnement pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'environnement dans lequel le CRON est exécuté.
	 */
	private String getEnvironment() {
		return ConfigFactory.load().getString(ENV_KEY);
	}

	/**
	 * Méthode permettant de récupérer l'institut pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'institut pour lequel le CRON est exécuté.
	 */
	private String getInstitute() {
		return NGL_CONFIG.getInstitute();
	}

	/**
	 * buildAndSendMail()
	 */
	private void buildAndSendMail() {
		final String EXPEDITEUR = getEmailExpediteur();
		final HashSet<String> DESTINATAIRES = getEmailDestinataires();
		final String ENVIRONMENT = getEnvironment();
		final String INSTITUTE = getInstitute();

		try {
			String message = buildMailBody();
			String subject = "[NGL-SQ] [" + ENVIRONMENT + "] [" + INSTITUTE + "] Logs script DeleteRessourceAndUpdateLaunchDate";

			mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, subject, message);
		} catch (UnsupportedEncodingException | MailServiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * buildMailBody()
	 * 
	 * @return
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private String buildMailBody() throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();

		if (!EXP_CODES_LIST.isEmpty()) {
			message.append("Liste des exp&eacute;riences supprim&eacute;es :<br />");

			EXP_CODES_LIST.stream().forEach(expCode -> {
				message.append(expCode);
				message.append("<br />");
			});

			message.append("<br />");
		}

		if (!RS_CODES_LIST.isEmpty()) {
			message.append("Liste des readsets supprim&eacute;s :<br />");

			RS_CODES_LIST.stream().forEach(rsCode -> {
				message.append(rsCode);
				message.append("<br />");
			});

			message.append("<br />");
		}

		if (!PROC_CODES_LIST.isEmpty()) {
			message.append("Liste des processus supprim&eacute;s :<br />");

			PROC_CODES_LIST.stream().forEach(procCode -> {
				message.append(procCode);
				message.append("<br />");
			});

			message.append("<br />");
		}

		return new String(message.toString().getBytes(), "iso-8859-1");
	}

	/**
	 * handleEmptyProcess()
	 */
	private void handleEmptyProcess() {
		println("Onglet '" + SHEET_PROCESSUS + "' vide.");
		println("Format de l'onglet : ");
		println("Ligne 1 : Header 'Code'");
		println("Sur chaque ligne : 1 code process");

		setEmptyProcess(true);
	}

	/**
	 * handleEmptyExperiment()
	 */
	private void handleEmptyExperiment() {
		println("Onglet '" + SHEET_EXPERIMENT + "' vide.");
		println("Format de l'onglet : ");
		println("Ligne 1 : Header 'Code'");
		println("Sur chaque ligne : 1 code expérience");

		setEmptyExperiment(true);
	}

	/**
	 * handleEmptyReadset()
	 */
	private void handleEmptyReadset() {
		println("Onglet '" + SHEET_READSETS + "' vide.");
		println("Format de l'onglet : ");
		println("Ligne 1 : Header 'Code'");
		println("Sur chaque ligne : 1 code readset");

		setEmptyReadset(true);
	}

	/**
	 * Méthode permettant de gérer une ligne de fichier Excel pour l'onglet 'Readset'.
	 * 
	 * @param row La ligne du fichier Excel actuellement traitée.
	 * @param args Les arguments du script. 
	 */
	private void handleReadsetLine(Row row, Args args) {
		countLineReadset();

		if (row.getRowNum() == 0) {
			return; 
		}

		if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().length() > 0) { 
			String objCode = row.getCell(0).getStringCellValue().trim();

			ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, objCode);
						
			handleReadset(readset, objCode, args.simulation);	
		} else {
			if (row.getRowNum() == 1) {
				// La ligne 0 c'est les header, donc si la 1 est vide, on part du principe que le fichier est vide.

				handleEmptyReadset();
			}
		}
	}

	/**
	 * Méthode permettant de gérer une ligne de fichier Excel pour l'onglet 'Expériences'.
	 * 
	 * @param row La ligne du fichier Excel actuellement traitée.
	 * @param args Les arguments du script. 
	 */
	private void handleExperimentLine(Row row, Args args) {
		countLineExperiment();

		if (row.getRowNum() == 0) {
			return; 
		}

		if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().length() > 0) { 
			String objCode = row.getCell(0).getStringCellValue().trim();

			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, objCode);
							
			handleExperiment(experiment, objCode, args.simulation);	
		} else {
			if (row.getRowNum() == 1) {
				// La ligne 0 c'est les header, donc si la 1 est vide, on part du principe que le fichier est vide.

				handleEmptyExperiment();
			}
		}
	}

	/**
	 * Méthode permettant de gérer une ligne de fichier Excel pour l'onglet 'Processus'.
	 * 
	 * @param row La ligne du fichier Excel actuellement traitée.
	 * @param args Les arguments du script. 
	 */
	private void handleProcessLine(Row row, Args args) {
		countLineProcess();

		if (row.getRowNum() == 0 && row.getCell(0).getStringCellValue().equals("Code")) {
			return; 
		} else if (row.getRowNum() == 0 && !row.getCell(0).getStringCellValue().equals("Code")) {
			println("La ligne avec le texte '" + row.getCell(0).getStringCellValue() + "' est ignorée.");
		}

		if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().length() > 0) { 					
			String objCode = row.getCell(0).getStringCellValue().trim();
			
			Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, objCode);

			handleProcess(process, objCode, args.simulation);
		} else {
			if (row.getRowNum() == 1) {
				// La ligne 0 c'est les header, donc si la 1 est vide, on part du principe que le fichier est vide.

				handleEmptyProcess();
			}
		}
	}

	/**
	 * Méthode permettant d'incrémenter le compteur 'COUNT_PROCESS' qui permet d'identifier
	 * le nombre de process dans le fichier Excel en entrée.
	 */
	private void countLineProcess() {
		this.COUNT_PROCESS++;
	}

	/**
	 * Méthode permettant d'incrémenter le compteur 'COUNT_EXPERIMENT' qui permet d'identifier
	 * le nombre d'expériences dans le fichier Excel en entrée.
	 */
	private void countLineExperiment() {
		this.COUNT_EXPERIMENT++;
	}

	/**
	 * Méthode permettant d'incrémenter le compteur 'COUNT_READSET' qui permet d'identifier
	 * le nombre de readset dans le fichier Excel en entrée.
	 */
	private void countLineReadset() {
		this.COUNT_READSET++;
	}

	/**
	 * Méthode permettant de changer la valeur du booléen 'PROCESS_EMPTY'
	 * qui représente si l'onglet 'Processus' est vide (ou pas).
	 * 
	 * @param isEmptyProcess La nouvelle valeur du booléen.
	 */
	private void setEmptyProcess(boolean isEmptyProcess) {
		this.PROCESS_EMPTY = isEmptyProcess;
	}

	/**
	 * Méthode permettant de changer la valeur du booléen 'READSET_EMPTY'
	 * qui représente si l'onglet 'Readset' est vide (ou pas).
	 * 
	 * @param isEmptyReadset La nouvelle valeur du booléen.
	 */
	private void setEmptyReadset(boolean isEmptyReadset) {
		this.READSET_EMPTY = isEmptyReadset;
	}

	/**
	 * Méthode permettant de changer la valeur du booléen 'EXPERIMENT_EMPTY'
	 * qui représente si l'onglet 'Expériences' est vide (ou pas).
	 * 
	 * @param isEmptyExperiment La nouvelle valeur du booléen.
	 */
	private void setEmptyExperiment(boolean isEmptyExperiment) {
		this.EXPERIMENT_EMPTY = isEmptyExperiment;
	}

	/**
	 * Méthode permettant de gérer la vie d'un sample : s'il en a une, on récupère les codes samples et on les retourne 
	 * afin que ces samples soient pris en compte dans le reporting (ajout d'une date dans le champ 'processesToLaunchDate').
	 * 
	 * @param sampleCode Le code du sample à 'vérifier'.
	 * 
	 * @return Une liste de code samples correspondant à l'ascendance du sample, s'il en a une.
	 * 'null' s'il n'a pas d'ascendance.
	 */
	private List<String> handleSampleLife(String sampleCode) {
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

		if (sample.life != null) {
			List<String> sampleCodeFromChild = Arrays.asList(sample.life.path.split(","));
			sampleCodeFromChild = sampleCodeFromChild.stream().filter(sc -> sc.length() > 0).collect(Collectors.toList());

			return sampleCodeFromChild;
		}

		return null;
	}

	/**
	 * Méthode permettant de gérer le process entré dans le fichier Excel en entrée du script.
	 * 
	 * @param process Un objet 'Process' correspondant au process saisi dans le fichier Excel.
	 * Si le code du process donné n'existe pas, l'objet est 'null'.
	 * 
	 * @param processCode Le code du process saisi dans le fichier Excel.
	 * 
	 * @param simulation Booléen qui permet de savoir si on est en simulation ou pas. 
	 * Si on est en simulation, rien est fait en base.
	 */
	private void handleProcess(Process process, String processCode, boolean simulation) {
		if (process != null) {
			Iterator<String> iterator = process.sampleCodes.iterator();
			List<String> res = new ArrayList<>();

			while (iterator.hasNext()) {
				String sampleCode = iterator.next();
				res.add(sampleCode);

				List<String> sampleCodeFromLife = handleSampleLife(sampleCode);

				if (sampleCodeFromLife != null) {
					res.addAll(sampleCodeFromLife);
				}
			}

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", res)).toList();

			sampleList.forEach(sample -> {
				println("Champ 'processesToLaunchDate' mis à jour sur le sample '" + sample.code + "'.");

				sample.setProcessesToLaunchDate(new Date());

				if (!simulation) {
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}
			});

			if (!simulation) {
				boolean isDeleted = true;
				String message = "";

				try {
					this.procAPI.delete(process.code);
				} catch (APIException e) {
					message = e.getMessage();
					isDeleted = false;
				} 

				if (!isDeleted) {
					println("Le process '" + process.code + "' n'a pas été supprimé. Cause : " + message);
				} else {
					println("Le process '" + process.code + "' a été supprimé.");

					PROC_CODES_LIST.add(process.code);
				}
			}
		} else {
			println("Le code du process donné n'existe pas (code : '" + processCode + "').");
		}
	}

	/**
	 * Méthode permettant de gérer le readset entré dans le fichier Excel en entrée du script.
	 * 
	 * @param readset Un objet 'ReadSet' correspondant au readset saisi dans le fichier Excel.
	 * Si le code du readset donné n'existe pas, l'objet est 'null'.
	 * 
	 * @param readsetCode Le code du readset saisi dans le fichier Excel.
	 * 
	 * @param simulation Booléen qui permet de savoir si on est en simulation ou pas. 
	 * Si on est en simulation, rien est fait en base.
	 */
	private void handleReadset(ReadSet readset, String readsetCode, boolean simulation) {
		if (readset != null) {
			List<String> res = new ArrayList<>();

			res.add(readset.sampleCode);

			List<String> sampleCodeFromLife = handleSampleLife(readset.sampleCode);
			
			if (sampleCodeFromLife != null) {
				res.addAll(sampleCodeFromLife);
			}

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", res)).toList();

			sampleList.forEach(sample -> {
				println("Champ 'processesToLaunchDate' mis à jour sur le sample '" + sample.code + "'.");

				sample.setProcessesToLaunchDate(new Date());

				if (!simulation) {
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}
			});

			if (!simulation) {
				boolean isDeleted = true;
				String message = "";

				try {
					this.rsAPI.delete(readset.code);
				} catch (APIException e) {
					message = e.getMessage();
					isDeleted = false;
				} 

				if (!isDeleted) {
					println("Le readset '" + readset.code + "' n'a pas été supprimé. Cause : " + message);
				} else {
					println("Le readset '" + readset.code + "' a été supprimé.");

					RS_CODES_LIST.add(readset.code);
				}
			}
		} else {
			println("Le code du readset donné n'existe pas (code : '" + readsetCode + "').");
		}
	}

	/**
	 * Méthode permettant de gérer l'expérience entrée dans le fichier Excel en entrée du script.
	 * 
	 * @param experiment Un objet 'Experiment' correspondant à l'expérience saisie dans le fichier Excel.
	 * Si le code de l'expérience donné n'existe pas, l'objet est 'null'.
	 * 
	 * @param experimentCode Le code de l'expérience saisi dans le fichier Excel.
	 * 
	 * @param simulation Booléen qui permet de savoir si on est en simulation ou pas. 
	 * Si on est en simulation, rien est fait en base.
	 */
	private void handleExperiment(Experiment experiment, String experimentCode, boolean simulation) {
		if (experiment != null) {
			Iterator<String> iterator = experiment.sampleCodes.iterator();
			List<String> res = new ArrayList<>();

			while (iterator.hasNext()) {
				String sampleCode = iterator.next();
				res.add(sampleCode);

				List<String> sampleCodeFromLife = handleSampleLife(sampleCode);
				
				if (sampleCodeFromLife != null) {
					res.addAll(sampleCodeFromLife);
				}
			}

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", res)).toList();

			sampleList.forEach(sample -> {
				println("Champ 'processesToLaunchDate' mis à jour sur le sample '" + sample.code + "'.");

				sample.setProcessesToLaunchDate(new Date());

				if (!simulation) {
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}
			});

			if (!simulation) {
				boolean isDeleted = true;
				String message = "";

				try {
					this.expAPI.delete(experiment.code);
				} catch (APIException e) {
					message = e.getMessage();
					isDeleted = false;
				}

				if (!isDeleted) {
					println("L'expérience '" + experiment.code + "' n'a pas été supprimée. Cause : " + message);
				} else {
					println("L'expérience '" + experiment.code + "' a été supprimée.");

					EXP_CODES_LIST.add(experiment.code);
				}
			}
		} else {
			println("Le code de l'expérience donnée n'existe pas (code : '" + experimentCode + "').");
		}
	}
}