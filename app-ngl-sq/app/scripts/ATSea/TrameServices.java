package scripts.ATSea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import play.Logger;
import scripts.ATSea.model.ATSeaConstant;
import scripts.ATSea.model.BatchData;
import scripts.ATSea.model.ConfigurationData;
import scripts.ATSea.model.ContainerData;
import scripts.ATSea.model.IndividualData;
import scripts.ATSea.model.TaxonData;
import validation.ContextValidation;

public class TrameServices {



	public static Map<String, TaxonData> readSpeciesSelectFile(XSSFWorkbook workbook)
	{
		Map<String, TaxonData> mapTaxonData = new HashMap<String, TaxonData>();
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String taxonName = ReadExcelServices.getStringCellValue(row, 3);
			Logger.debug("Taxon name "+taxonName);
			if(taxonName!=null) {
				//TODO faire un mapping des col fichier selection espece
				Integer aphiaIDWorms = null;
				if(row.getCell(4).getCellType()==Cell.CELL_TYPE_NUMERIC) {
					aphiaIDWorms = (int) row.getCell(4).getNumericCellValue();
				}else {
					String aphiaIDWormsStringValue = row.getCell(4).getStringCellValue();
					if(StringUtils.isNotBlank(aphiaIDWormsStringValue)) {
						aphiaIDWorms = Integer.parseInt(StringUtils.deleteWhitespace(row.getCell(4).getStringCellValue()));
					}
				}


				String goatStatus = row.getCell(5).getStringCellValue();
				TaxonData taxonData = new TaxonData(taxonName, aphiaIDWorms, goatStatus);
				String phylum = row.getCell(0).getStringCellValue();
				taxonData.setPhylum(phylum);
				if(!mapTaxonData.containsKey(taxonName)) {
					mapTaxonData.put(taxonName, taxonData);
				}else {
					Logger.error("Doublon taxon dans fichier selection espèce "+taxonName);
					//TODO compile des erreurs
				}
			}
		});
		return mapTaxonData;
	}

	public static Map<String, String> getMapAffiliationTaxonomist(File inputFinalTrame) throws IOException
	{
		Map<String, String> mapTaxonomistAffiliation = new HashMap<String, String>();
		FileInputStream fisTF = new FileInputStream(inputFinalTrame);
		XSSFWorkbook workbookTrameValue = new XSSFWorkbook (fisTF);
		workbookTrameValue.getSheetAt(1).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String taxonomistName = ReadExcelServices.getStringCellValue(row, 23);
			String taxonomistAffiliation = ReadExcelServices.getStringCellValue(row, 24);
			if(taxonomistName!=null && taxonomistAffiliation!=null) {
				mapTaxonomistAffiliation.put(taxonomistName, taxonomistAffiliation);
			}

		});
		fisTF.close();
		return mapTaxonomistAffiliation;
	}
	public static void convertTrameToMapBatch(String fileName, Map<String, BatchData> mapBatchData, XSSFWorkbook workbookFileInput, 
			ConfigurationData configData, ContextValidation cv)
	{
		//Context validation infos
		int nbLignes=0;
		int nbLot=0;
		int nbIndividus=0;
		int nbContainers=0;

		//Verifier que la ligne n'est pas vide 
		String codeIndividuMNHN=null;
		String codeIndividuATS=null;
		BatchData batchData = new BatchData();
		IndividualData individualData = new IndividualData();
		//Lecture premier onglet trameInfo
		Iterator<Row> iteratorRow = workbookFileInput.getSheetAt(0).rowIterator();
		while(iteratorRow.hasNext()) {
			Row row = iteratorRow.next();
			//Skip header
			Logger.debug("Nb line "+row.getRowNum());
			if(row.getRowNum() != 0) {
				if(!ReadExcelServices.checkIfRowIsEmpty(row)) {
					boolean newLot=false;
					//Lecture nouveau lot réinitialisation stationCode et taxon ou changement station et taxon
					String currentStationCode = ReadExcelServices.getStringCellValue(row, configData.mappingColSamplingFile.get(ATSeaConstant.stationNumber));
					String currentTaxon = ReadExcelServices.getStringCellValue(row, configData.mappingColSamplingFile.get(ATSeaConstant.taxonName));
					if((batchData.stationCode==null && batchData.taxon==null) || 
							
							(StringUtils.isNotBlank(batchData.stationCode) && StringUtils.isNotBlank(batchData.taxon) && StringUtils.isNotBlank(currentStationCode) && StringUtils.isNotBlank(currentTaxon) && 
							(!batchData.stationCode.equals(currentStationCode) || !batchData.taxon.equals(currentTaxon)))) {
						if(batchData.stationCode!=null && batchData.taxon!=null) {
							//Validation du batch
							batchData.validate(cv, fileName);
							//Ajouter le batch à la map
							mapBatchData.put(batchData.numBatchATS, batchData);
						}
						//Ligne vide passage à l'espèce suivante
						codeIndividuMNHN=null;
						codeIndividuATS=null;
						batchData = new BatchData();
						newLot=true;

					}

					if(newLot) {
						Logger.debug("Lecture nouveau lot");
						nbLot++;
						//Valider données obligatoire première ligne du lot 
						if(validateMandatoryProperties(row, configData)) {
							//Récupération des codes individus
							codeIndividuMNHN=row.getCell(configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberMNHN)).getStringCellValue();
							codeIndividuATS=row.getCell(configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberATS)).getStringCellValue();
							Logger.debug("Lecture premier code individu "+codeIndividuMNHN+ " "+codeIndividuATS);
							//Lecture container
							ContainerData containerData = new ContainerData();
							containerData.build(row, configData.mappingColSamplingFile, cv, fileName);
							Logger.debug("ContainerData "+containerData);
							//Lecture individu
							individualData = new IndividualData();
							individualData.build(row, configData.mappingColSamplingFile, null);
							individualData.containersData.add(containerData);
							Logger.debug("Individual Data "+individualData);
							//lecture première ligne du lot
							batchData.build(row, configData.mappingColSamplingFile);
							batchData.setFileName(fileName);
							batchData.individualsData.add(individualData);
							Logger.debug("Batch Data "+batchData);
							nbContainers++;
							nbIndividus++;
						}else {
							//TODO message erreur explicite sur les données obligatoires
							((List<String>)cv.getObject("ErreurACorriger")).add(fileName+","+(nbLignes+1)+",Line,"+batchData.taxon+",données obligatoires manquantes fichier");
						}

					}else {
						//Passage à la ligne suivante du lot
						//Récupération code individu MNHN pour savoir si on passe à un nouvel individu
						String codeIndividuMNHNLine = ReadExcelServices.getStringCellValue(row, configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberMNHN));
						String codeIndividuATSLine = ReadExcelServices.getStringCellValue(row, configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberATS));
						if((StringUtils.isNotBlank(codeIndividuMNHNLine) && !codeIndividuMNHN.equals(codeIndividuMNHNLine)) || (StringUtils.isNotBlank(codeIndividuATSLine) && !codeIndividuATS.equals(codeIndividuATSLine))) {
							//vérifier que les deux codes ont changés
							if(StringUtils.isNotBlank(codeIndividuMNHNLine) && StringUtils.isNotBlank(codeIndividuATSLine) && !codeIndividuMNHN.equals(codeIndividuMNHNLine) && !codeIndividuATS.equals(codeIndividuATSLine)) {
								//Nouveau code individu
								//Je rajoute le dernier individu à mon batch
								Logger.debug("Nouvel individu "+codeIndividuMNHNLine+" "+codeIndividuATSLine);
								codeIndividuMNHN=codeIndividuMNHNLine;
								codeIndividuATS=codeIndividuATSLine;
								//Lecture container
								ContainerData containerData = new ContainerData();
								containerData.build(row, configData.mappingColSamplingFile, cv, fileName);
								Logger.debug("ContainerData "+containerData);
								//Lecture individu
								IndividualData previousIndividu = individualData;
								individualData = new IndividualData();
								individualData.build(row, configData.mappingColSamplingFile, previousIndividu);
								individualData.containersData.add(containerData);
								Logger.debug("Individual Data "+individualData);
								batchData.individualsData.add(individualData);
								Logger.debug("Batch Data "+batchData);
								nbContainers++;
								nbIndividus++;
							}else {
								((List<String>)cv.getObject("ErreurACorriger")).add(fileName+","+(nbLignes+1)+",Line,"+batchData.taxon+",Incohérence sur les codes individus MNHN et ATLASea");
							}
						}else if(StringUtils.isBlank(codeIndividuMNHNLine) && StringUtils.isBlank(codeIndividuATSLine)){
							Logger.debug("Nouveau container ");
							//Même individu
							ContainerData containerData = new ContainerData();
							containerData.build(row, configData.mappingColSamplingFile, cv, fileName);
							//vérifier que le container n'est pas vide fin de fichier
							if(StringUtils.isNotBlank(containerData.numContainer)) {
								Logger.debug("ContainerData "+containerData);
								//ajout container à l'individu courant
								individualData.containersData.add(containerData);
								Logger.debug("Individual Data "+individualData);
								//batchData.individualsData.add(individualData);
								Logger.debug("Batch Data "+batchData);
								nbContainers++;
							}else {
								Logger.debug("Container vide");
							}
						}else {
							//message erreur un des deux codes est null
							((List<String>)cv.getObject("ErreurACorriger")).add(fileName+","+(nbLignes+1)+",Line,"+batchData.taxon+",code individu non renseigné");
						}

					}


				}else {

					//Ajouter le batch à la map
					//mapBatchData.put(batchData.numBatchATS, batchData);
					//Ligne vide passage à l'espèce suivante
					//codeIndividuMNHN=null;
					//codeIndividuATS=null;
					//batchData = new BatchData();
				}
			}
			nbLignes++;
		}
		//ajouter le dernier batch à la map à la fin du fichier
		mapBatchData.put(batchData.numBatchATS, batchData);
		((List<String>)cv.getObject("RecapFile")).add(fileName+","+nbLignes+","+nbLot+","+nbIndividus+","+nbContainers);
	}



	public static XSSFWorkbook fillWorkbookTrame(File inputFinalTrame, Map<String, BatchData> mapBatchData, ConfigurationData configData,
			Map<String, String> mapTaxonomistAffiliation, Map<String, TaxonData> mapTaxonData, ContextValidation cv) throws IOException
	{
		FileInputStream fis = new FileInputStream(inputFinalTrame);
		XSSFWorkbook workbookTrame = new XSSFWorkbook (fis);
		//écrire dans le fichier de sortie excel
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		int nbRow=1;
		for(BatchData batch : mapBatchData.values()) {
			for(IndividualData ind : batch.individualsData) {
				for(ContainerData cont : ind.containersData) {
					Row row = workbookTrame.getSheetAt(0).getRow(nbRow);
					//add batch value
					Logger.debug("batch "+batch.taxon);
					Logger.debug("Nb row "+nbRow);
					setCell(ATSeaConstant.stationNumber, row, batch.stationCode, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.taxonName, row, batch.taxon, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.taxonomistNames, row, batch.taxonomistName, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.taxonomicConfidence, row, batch.taxonomicConfidence, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.isTaxoVoucher, row, batch.isVoucherTaxo, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.individualBatch, row, batch.numBatchATS, configData.mappingColSamplingFile);
					//add individual value
					setCell(ATSeaConstant.lifeCyclePhase, row, ind.lifeCyclePhase, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.sex, row, ind.sex, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.isOtherEukSpecProbability, row, ind.isOtherEukProbability, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.individualViability, row, ind.viability, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.sampleDescription, row, ind.sampleDescription, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.prepSampleDescription, row, ind.prepSampleDescription, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.individualType, row, ind.individualType, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.individualNumberMNHN, row, ind.numIndividuMNHN, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.individualNumberATS, row, ind.numIndividuATS, configData.mappingColSamplingFile);
					//add container value
					setCell(ATSeaConstant.containerBarcode, row, cont.numContainer, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.containerCategory, row, cont.categoryContainer, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.weight, row, cont.weight, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.dissectTissue, row, cont.dissectedTissue, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.packingDate, row, cont.packingDate, configData.mappingColSamplingFile);
					Logger.debug("container "+cont.numContainer);
					Logger.debug("container "+cont.packingHour);
					if(cont.packingHour!=null) {
						String packingHour = sdfHour.format(cont.packingHour);
						setCell(ATSeaConstant.packingHour, row, packingHour, configData.mappingColSamplingFile);
					}
					setCell(ATSeaConstant.packingName, row, cont.packingName, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.destination, row, cont.containerDestination, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.conservationMode, row, cont.storageMethod, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.tempStorage, row, cont.temperatureStorage, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.descContainer, row, cont.containerDescription, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.bagNb, row, cont.bagNb, configData.mappingColSamplingFile);
					setCell(ATSeaConstant.dryShipperNb, row, cont.dryShipperNb, configData.mappingColSamplingFile);
					//add default value
					setCell(ATSeaConstant.missionCode, row, configData.mappingColDefaultValue.get(ATSeaConstant.missionCode), configData.mappingColCompleteData);
					setCell(ATSeaConstant.coordinatorName, row, configData.mappingColDefaultValue.get(ATSeaConstant.coordinatorName), configData.mappingColCompleteData);
					setCell(ATSeaConstant.coordinatorAffiliation, row, configData.mappingColDefaultValue.get(ATSeaConstant.coordinatorAffiliation), configData.mappingColCompleteData);
					setCell(ATSeaConstant.barcodingCenter, row, configData.mappingColDefaultValue.get(ATSeaConstant.barcodingCenter), configData.mappingColCompleteData);
					//add value from selection file
					//Get taxonData
					if(mapTaxonData.containsKey(batch.taxon)) {
						TaxonData taxonData = mapTaxonData.get(batch.taxon);
						setCell(ATSeaConstant.WoRMSCode, row, taxonData.aphiaIDWorms, configData.mappingColCompleteData);
						setCell(ATSeaConstant.goatStatus, row, taxonData.goatStatus, configData.mappingColCompleteData);
					}else {
						Logger.error("No taxon in selection file "+batch.taxon);
						//TODO Compile error message
						((List<String>)cv.getObject("Erreur")).add(batch.fileName+",,Taxon,"+batch.taxon+",Ce taxon n'est pas trouvé dans le fichier de suivi, vérifier le nom");
					}
					//add correspondance affiliation taxonomiste in trame complete
					if(mapTaxonomistAffiliation.containsKey(batch.taxonomistName)) {
						setCell(ATSeaConstant.taxonomistAffiliation, row, mapTaxonomistAffiliation.get(batch.taxonomistName), configData.mappingColCompleteData);
					}else {
						Logger.error("No affiliation for taxonomist "+batch.taxonomistName);
						((List<String>)cv.getObject("Erreur")).add(batch.fileName+",,Taxon,"+batch.taxonomistName+",Pas d'affiliation pour ce taxonomiste");
					}
					nbRow++;
				}
			}
		}
		return workbookTrame;

	}

	public static boolean validateMandatoryProperties(Row row, ConfigurationData configData) {
		for(String property : configData.mandatoryProperties) {
			if(row.getCell(configData.mappingColSamplingFile.get(property))==null) {
				Logger.debug("Manque données obligatoire "+property);
				return false;
			}
		}
		return true;
	}

	public static void setCell(String code, Row row, Object value, Map<String, Integer> mapCodeProperty) {
		int number =  mapCodeProperty.get(code);
		Cell cell = row.getCell(number);
		if(cell==null) {
			cell = row.createCell(number);
		}
		if(value instanceof Date) {
			Date date = (Date)value;
			cell.setCellValue(date);
		}
		else if (value instanceof Double)
			cell.setCellValue((Double)value);
		else if(value instanceof String)
			cell.setCellValue((String)value);
		else if(value instanceof Integer)
			cell.setCellValue((Integer)value);
		else if(value instanceof Long)
			cell.setCellValue((Long)value);
		else if(value instanceof Boolean)
			cell.setCellValue((Boolean)value);
	}

	public static void convertCompileTrameToMapBatch(Map<String, BatchData> mapBatchData, XSSFWorkbook workbookFileInput, 
			ConfigurationData configData, ContextValidation cv)
	{
		//Verifier que la ligne n'est pas vide 
		BatchData batchData = new BatchData();
		IndividualData individualData = new IndividualData();
		//Lecture premier onglet trameInfo
		Iterator<Row> iteratorRow = workbookFileInput.getSheetAt(0).rowIterator();
		while(iteratorRow.hasNext()) {
			Row row = iteratorRow.next();
			//Skip header
			Logger.debug("Nb line "+row.getRowNum());
			if(row.getRowNum() != 0) {

				if(!ReadExcelServices.checkIfRowIsEmpty(row)) {
					//Verifie si même batch ou nouveau batch
					String numBatchATSRow = row.getCell(configData.mappingColSamplingFile.get(ATSeaConstant.individualBatch)).getStringCellValue();
					if(batchData.numBatchATS==null || (batchData.numBatchATS!=null && !numBatchATSRow.equals(batchData.numBatchATS))){
						//Ajout du lot avant de passer au lot suivant
						if(batchData.numBatchATS!=null) {
							mapBatchData.put(batchData.numBatchATS, batchData); 
						}
						//Nouveau lot
						batchData = new BatchData();
						//Lecture container
						ContainerData containerData = new ContainerData();
						containerData.build(row, configData.mappingColSamplingFile, cv, "Fichier entrée");
						Logger.debug("ContainerData "+containerData);
						//Lecture individu
						individualData = new IndividualData();
						individualData.build(row, configData.mappingColSamplingFile, null);
						individualData.containersData.add(containerData);
						Logger.debug("Individual Data "+individualData);
						//lecture première ligne du lot
						batchData.build(row, configData.mappingColSamplingFile);
						batchData.individualsData.add(individualData);
						SimpleDateFormat sdfExcel = new SimpleDateFormat("dd/MM/yyyy");
						batchData.listCollectDate.add(sdfExcel.format(containerData.packingDate));
						Logger.debug("Batch Data "+batchData);
					}else{
						//lot courant
						String codeIndividuMNHNRow=row.getCell(configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberMNHN)).getStringCellValue();
						String codeIndividuATSRow=row.getCell(configData.mappingColSamplingFile.get(ATSeaConstant.individualNumberATS)).getStringCellValue();
						if(codeIndividuMNHNRow.equals(individualData.numIndividuMNHN) && codeIndividuATSRow.equals(individualData.numIndividuATS)) {
							//individu courant
							ContainerData containerData = new ContainerData();
							containerData.build(row, configData.mappingColSamplingFile, cv, "fichier entrée");
							//vérifier que le container n'est pas vide fin de fichier
							if(StringUtils.isNotBlank(containerData.numContainer)) {
								Logger.debug("ContainerData "+containerData);
								//ajout container à l'individu courant
								individualData.containersData.add(containerData);
								Logger.debug("Individual Data "+individualData);
								//batchData.individualsData.add(individualData);
								Logger.debug("Batch Data "+batchData);
							}else {
								Logger.debug("Container vide");
							}
						}else {
							//nouvel individu
							//Lecture container
							ContainerData containerData = new ContainerData();
							containerData.build(row, configData.mappingColSamplingFile, cv, "fichier entrée");
							Logger.debug("ContainerData "+containerData);
							//Lecture individu
							IndividualData previousIndividu = individualData;
							individualData = new IndividualData();
							individualData.build(row, configData.mappingColSamplingFile, previousIndividu);
							individualData.containersData.add(containerData);
							Logger.debug("Individual Data "+individualData);
							batchData.individualsData.add(individualData);
							Logger.debug("Batch Data "+batchData);
						}
					}
				}
			}

		}
		//ajouter le dernier batch à la map à la fin du fichier
		mapBatchData.put(batchData.numBatchATS, batchData); 
	}

	
	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	public static void createExcelFileRecap(ContextValidation cv, String fileName)	{

		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//	Logger.debug("recap: "+recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream(fileName)) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}
}
