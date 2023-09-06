package scripts;

import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;

/**
 * Script permettant, à partir de ressources données en entrée (readset, process, expérience) de mettre à jour le champ 'processesToLaunchDate'
 * sur les samples 'associés' aux ressources données au script.
 * 
 * Ce script est à utiliser dans un contexte de reporting nocturne.
 * 
 * @author Jordi CHARPENTIER jcharpen@genoscope.cns.fr 
 */
// SCRIPT A EVITER - Nécessite de donner les codes des objets qu'on s'apprête à supprimer et de lancer le script AVANT de les supprimer.
@Deprecated
public class UpdateSampleLaunchDateFromRessource extends ScriptWithExcelBody {

	private static final String SHEET_READSETS = "Readsets";

	private static final String SHEET_PROCESSUS = "Processus";

	private static final String SHEET_EXPERIMENT = "Expériences";

	@Inject
	public UpdateSampleLaunchDateFromRessource() {

	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception { 		
		println("Début du script");

		XSSFSheet sheetProcess = workbook.getSheet(SHEET_PROCESSUS);

		if (sheetProcess != null) {
			sheetProcess.rowIterator().forEachRemaining(row -> {	
				if (row.getRowNum() == 0) {
					return; 
				}

				if (row != null && row.getCell(0) != null) { 
					String objCode = row.getCell(0).getStringCellValue().trim();
					
					Logger.info("Traitement du process '" + objCode + "'.");
					Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, objCode);

					handleProcess(process, objCode);
				} 
			});
		} else {
			println("L'onglet '" + SHEET_PROCESSUS + "' n'existe pas dans le fichier Excel.");
		}

		XSSFSheet sheetExp = workbook.getSheet(SHEET_EXPERIMENT);
		
		if (sheetExp != null) {
			sheetExp.rowIterator().forEachRemaining(row -> {
				if (row.getRowNum() == 0) {
					return; 
				}

				if (row != null && row.getCell(0) != null) { 
					String objCode = row.getCell(0).getStringCellValue().trim();
					Logger.info("Traitement de l'expérience '" + objCode + "'.");

					Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, objCode);
							
					handleExperiment(experiment, objCode);	
				} 
			});
		} else {
			println("L'onglet '" + SHEET_EXPERIMENT + "' n'existe pas dans le fichier Excel.");
		}

		XSSFSheet sheetRs = workbook.getSheet(SHEET_READSETS);

		if (sheetRs != null) {
			sheetRs.rowIterator().forEachRemaining(row -> {	
				if (row.getRowNum() == 0) {
					return; 
				}

				if (row != null && row.getCell(0) != null) { 
					String objCode = row.getCell(0).getStringCellValue().trim();
					Logger.info("Traitement du readset '" + objCode + "'.");

					ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, objCode);
							
					handleReadset(readset, objCode);	
				} 
			});
		} else {
			println("L'onglet '" + SHEET_READSETS + "' n'existe pas dans le fichier Excel.");
		}

		println("Fin du script");
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
	 */
	private void handleProcess(Process process, String processCode) {
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
				sample.setProcessesToLaunchDate(new Date());

				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			});
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
	 */
	private void handleReadset(ReadSet readset, String readsetCode) {
		if (readset != null) {
			List<String> res = new ArrayList<>();

			res.add(readset.sampleCode);

			List<String> sampleCodeFromLife = handleSampleLife(readset.sampleCode);
			
			if (sampleCodeFromLife != null) {
				res.addAll(sampleCodeFromLife);
			}

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", res)).toList();

			sampleList.forEach(s -> {
				s.setProcessesToLaunchDate(new Date());

				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, s);
			});
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
	 */
	private void handleExperiment(Experiment experiment, String experimentCode) {
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
				sample.setProcessesToLaunchDate(new Date());

				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			});
		} else {
			println("Le code de l'expérience donnée n'existe pas (code : '" + experimentCode + "').");
		}
	}
}