package scripts.ATSea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import play.Logger;
import scripts.ATSea.model.BatchData;
import scripts.ATSea.model.ConfigurationData;
import scripts.ATSea.model.ContainerData;
import scripts.ATSea.model.IndividualData;
import scripts.ATSea.model.TaxonData;
import validation.ContextValidation;

/**
 * Script permettant de calculer le poids de chaque espèce collecté.
 * 
 * Prend en entrée le fichier de trame
 * 
 * Sortie fichier recap avec le poids total, nb tubes, nb individus... par espèce
 * 
 * 
 * @author ejacoby
 *
 */

public class CompileWeightATLASeaTrameEchantillon extends ScriptWithArgsAndExcelBody<CompileWeightATLASeaTrameEchantillon.Args> {

	private static final String user = "ngl-support";

	private static final String speciesFileName = "ATS_TrameSuiviEspece_V1_1_Dinard.xlsx";

	public static class Args {
		public String configDir;
	}



	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("Erreur", new ArrayList<String>());
		cv.putObject("ErreurACorriger", new ArrayList<String>());
		cv.putObject("RecapFile", new ArrayList<String>());

		((List<String>)cv.getObject("RecapFile")).add("Fichier lu, Nb lignes, Nb lot, Nb individus, Nb containers");
		((List<String>)cv.getObject("ErreurACorriger")).add("Fichier, Numéro ligne,Type, Code, error message");
		((List<String>)cv.getObject("Erreur")).add("Fichier, Numéro ligne, Type, Code, error message");
		//Read config file
		File inputConfigurationFile = new File(args.configDir+File.separator+"ConfigColonnes.xlsx");
		ConfigurationData configData = new ConfigurationData();
		configData.build(inputConfigurationFile);

		//Lecture du fichier selection des epsèces
		File inputSpeciesSelection = new File(args.configDir+File.separator+File.separator+speciesFileName);
		FileInputStream fis = new FileInputStream(inputSpeciesSelection);
		XSSFWorkbook workbookInputSpecies = new XSSFWorkbook(fis);
		Map<String, TaxonData> mapTaxonData = TrameServices.readSpeciesSelectFile(workbookInputSpecies);

		//lecture du fichier et conversion en map batch DATA
		Map<String, BatchData> mapBatchData = new HashMap<String, BatchData>();
		TrameServices.convertCompileTrameToMapBatch(mapBatchData, workbook,configData, cv);

		//TODO ajouter le phylum dans recap
		Map<String, TaxonData> weightDataBySpecies = new HashMap<String, TaxonData>();
		for(BatchData batchData : mapBatchData.values()) {
			TaxonData taxonData = new TaxonData(batchData.taxon, 0, 0, 1, 0, 0);
			if(weightDataBySpecies.containsKey(batchData.taxon)) {
				taxonData=weightDataBySpecies.get(batchData.taxon);
				taxonData.nbCollect+=1;
			}

			taxonData.nbIndividuals+=batchData.individualsData.size();
			for(IndividualData ind: batchData.individualsData) {
				taxonData.nbContainers+=ind.containersData.size();
				for(ContainerData cont : ind.containersData) {
					if(cont.weight!=null)
						taxonData.totalWeight+=cont.weight;
				}
			}
			if(taxonData.nbContainers!=0)
				taxonData.meanWeight=taxonData.totalWeight/taxonData.nbContainers;
			taxonData.listCollectDate.addAll(batchData.listCollectDate);
			weightDataBySpecies.put(batchData.taxon, taxonData);
		}


		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("CalculPourRecollecte");

		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue(createHelper.createRichTextString("Phylum"));
		row.createCell(1).setCellValue(createHelper.createRichTextString("Taxon"));
		row.createCell(2).setCellValue(createHelper.createRichTextString("Nb individus"));
		row.createCell(3).setCellValue(createHelper.createRichTextString("Nb containers"));
		row.createCell(4).setCellValue(createHelper.createRichTextString("Poids total"));
		row.createCell(5).setCellValue(createHelper.createRichTextString("Poids moyen"));
		row.createCell(6).setCellValue(createHelper.createRichTextString("Nb collecte"));
		row.createCell(7).setCellValue(createHelper.createRichTextString("Dates collectes"));

		//Write header
		int nbLine=1;
		for(String taxon : weightDataBySpecies.keySet()){
			//	Logger.debug("recap: "+recap);
			row = sheet.createRow(nbLine);
			TaxonData taxonData = weightDataBySpecies.get(taxon);
			String phylum = null;
			if(mapTaxonData.containsKey(taxonData.taxonName))
				phylum=mapTaxonData.get(taxonData.taxonName).getPhylum();
			row.createCell(0).setCellValue(createHelper.createRichTextString(phylum));
			row.createCell(1).setCellValue(createHelper.createRichTextString(taxonData.taxonName));
			row.createCell(2).setCellValue(taxonData.nbIndividuals);
			row.createCell(3).setCellValue(taxonData.nbContainers);
			row.createCell(4).setCellValue(taxonData.totalWeight);
			row.createCell(5).setCellValue(taxonData.meanWeight);
			row.createCell(6).setCellValue(taxonData.nbCollect);
			TreeSet<String> listCollectDateSorted = new TreeSet<String>(taxonData.listCollectDate);
			row.createCell(7).setCellValue(listCollectDateSorted.toString());
			nbLine++;
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream(args.configDir+File.separator+"OutputWeight.xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}


		//TODO ecrire dans un fichier de sortie
		for(String taxon : weightDataBySpecies.keySet()) {
			TaxonData taxonData = weightDataBySpecies.get(taxon);
			Logger.debug("Taxon  "+taxon+" nbInd="+taxonData.nbIndividuals+" nbCont="+taxonData.nbContainers+" totWeight="+taxonData.totalWeight+" meanWeigh="+taxonData.meanWeight+" nbCollect "+taxonData.nbCollect);
		}

		//Create excel file to recap in execution directory
		String fileNameRecap = args.configDir+File.separator+"Recap_Weight.xls";
		TrameServices.createExcelFileRecap(cv, fileNameRecap);


	}








}
