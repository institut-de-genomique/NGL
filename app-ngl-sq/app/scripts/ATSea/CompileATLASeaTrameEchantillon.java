package scripts.ATSea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import play.Logger;
import scripts.ATSea.model.BatchData;
import scripts.ATSea.model.ConfigurationData;
import scripts.ATSea.model.TaxonData;
import validation.ContextValidation;

/**
 * Script permettant compiler les trames d'échantillonnage lors de la mission.
 * 
 * Prend en entrée un répertoire où se trouve toutes les trames du jour de la collecte
 * Prend en entrée un fichier de configuration ? 
 * Sortie fichier compilé de toutes les trames avec competion des données non saisies
 * 
 * 
 * @author ejacoby
 *
 */

public class CompileATLASeaTrameEchantillon extends ScriptWithArgsAndExcelBody<CompileATLASeaTrameEchantillon.Args> {

	private static final String user = "ngl-support";
	//Nom du repertoire des fichiers de configuragtion
	private static final String configDir = "Configuration";
	//Configuration : Nom du fichier de template trame de sortie à remplir
	private static final String templateTrameFileName = "ATSea_trame_echantillonnage-V4_8_Dinard_V3.xlsx";
	//Configuration : Nom du fichier de configuration
	private static final String configFileName = "ConfigColonnes.xlsx";
	
	//Répertoire des trames de saisie de collecte
	private static final String inputTrameDirName = "InputTrame";
	//Repertoire de la compilation des trames de saisie de collecte
	private static final String outputTrameDirName = "OutputTrame";
	
	
	public static class Args {
		public String inputTrameDir;
		public String collectDate;
	}



	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {

		//Fichier de log
		//TODO mettre les intitules des onglets en paramètre
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("Erreur", new ArrayList<String>());
		cv.putObject("ErreurACorriger", new ArrayList<String>());
		cv.putObject("RecapFile", new ArrayList<String>());
		
		((List<String>)cv.getObject("RecapFile")).add("Fichier lu, Nb lignes, Nb lot, Nb individus, Nb containers");
		((List<String>)cv.getObject("ErreurACorriger")).add("Fichier, Numéro ligne,Type, Code, error message");
		((List<String>)cv.getObject("Erreur")).add("Fichier, Numéro ligne, Type, Code, error message");
		
		
		File templateFinalTrame = new File(args.inputTrameDir+File.separator+configDir+File.separator+templateTrameFileName);

		File inputConfigurationFile = new File(args.inputTrameDir+File.separator+configDir+File.separator+configFileName);
		//Lecture du fichier de configuration
		ConfigurationData configData = new ConfigurationData();
		configData.build(inputConfigurationFile);
		
		//Lecture du fichier selection des epsèces
		Map<String, TaxonData> mapTaxonData = TrameServices.readSpeciesSelectFile(workbook);
		//Lecture l'onglet listeValeurs trame échantillonnage
		Map<String, String> mapTaxonomistAffiliation = TrameServices.getMapAffiliationTaxonomist(templateFinalTrame);

		//1. Rechercher les fichiers dans le répertoire
		File inputDirectory = new File(args.inputTrameDir+File.separator+inputTrameDirName+File.separator+args.collectDate);
		Map<String, BatchData> mapBatchData = new HashMap<String, BatchData>();
		if(inputDirectory.exists() && inputDirectory.canRead()) {
			for(File inputFile : inputDirectory.listFiles()) {
				if(!inputFile.getName().startsWith("~") && inputFile.getName().endsWith("xlsx")) {
					Logger.debug("Get file "+inputFile.getName());
					//Read file
					FileInputStream fis = new FileInputStream(inputFile);
					XSSFWorkbook workbookFileInput = new XSSFWorkbook (fis);
					TrameServices.convertTrameToMapBatch(inputFile.getName(), mapBatchData, workbookFileInput, configData, cv);
				}else {
					Logger.error("Ce fichier n'est pas pris en compte pour la compilation "+inputFile.getName());
					((List<String>)cv.getObject("Erreur")).add(inputFile.getName()+",,Fichier,"+inputFile.getName()+",Ce fichier n'est pas pris en compte pour la compilation");
				}
			}
		}else {
			Logger.error("Le répertoire n'existe pas ou n'a pas les droits de lecture");
		}

		//Validation mapBatchData
		//TODO Unicité code barre, unicite code individu (dans compilation finale aussi ?)
		//TODO vérifier unicité sac
		
		FileInputStream fis = new FileInputStream(templateFinalTrame);
		XSSFWorkbook workbookTrame = TrameServices.fillWorkbookTrame(templateFinalTrame, mapBatchData, configData, mapTaxonomistAffiliation, mapTaxonData, cv);
		
		//mettre la date en parametre (date de collecte)
		File dirOuputTrame = new File(args.inputTrameDir+File.separator+inputTrameDirName+File.separator+args.collectDate+File.separator+outputTrameDirName);
		if(!dirOuputTrame.exists()) {
			dirOuputTrame.mkdir();
		}
		
		String preFileName = templateTrameFileName.substring(0, templateTrameFileName.lastIndexOf("."));
		String fileNameCollectDate = preFileName+"_"+args.collectDate+".xlsx";
		String fileOutputName = dirOuputTrame+File.separator+fileNameCollectDate;

		try (OutputStream fileOut = new FileOutputStream(fileOutputName)) {
			workbookTrame.write(fileOut);
			fileOut.close();
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}

		fis.close();

		//Create excel file to recap in execution directory
		String fileNameRecap = dirOuputTrame+File.separator+"Recap_"+fileNameCollectDate.replace("xlsx", "xls");
		TrameServices.createExcelFileRecap(cv, fileNameRecap);

		
	}





	


}
