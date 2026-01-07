package scripts.ATSea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import play.Logger;
import scripts.ATSea.model.ATSeaConstant;
import scripts.ATSea.model.ReceptionData;
import scripts.ATSea.model.TaxonInfo;

/**
 * Script permettant de convertir trame échantillonnage ATLASea en fichier reception
 * Prend en entrée trame echantillonage un fichier excel avec 
 *
 * onglet 1 trame echantillonnage
 * onglet 3 Liste de valeurs
 * @author ejacoby
 *
 */

public class ConvertSamplingFileToReceptionFile extends ScriptWithArgsAndExcelBody <ConvertSamplingFileToReceptionFile.Args> {

	private TaxonomyServices taxonomyServices;

	private static final String user = "ngl-support";

	public static class Args {
		public String receptionConfigInputPath;
	}

	@Inject
	public ConvertSamplingFileToReceptionFile( TaxonomyServices taxonomyServices) {
		this.taxonomyServices = taxonomyServices;
	}

	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {

		//TODO reste à definir colonne refCollab à voir avec Karine et Jana
		//TODO reste à définir colonne commentaire Container
		//TODO à adapter pour les cultures
		//TODO à adapter lors des saisies libres tissus disséqués
		//TODO si on attend un nombre enlever les espcaces en trop (par exemple wormsCode)
		//TODO adapter lorsque valeur attendue obligatoire et non oligatoire
		//TODO adapter script pour prendre en compte différent format date cellule excel
		//TODO prévoir un onglet pour la correspondance des colonnes plutot que dans des constantes
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		Map<String,TaxonInfo> mapTaxon = new HashMap<String, TaxonInfo>();
		//Lecture du fichier de trame 

		Map<String, ReceptionData> mapSamplingFileReceptionData = new HashedMap<String, ReceptionData>();

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String sampleOrigin = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.sampleOrigin)).getStringCellValue();
			String containerCategory = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.containerCategory)).getStringCellValue();
			String containerBarcode = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.containerBarcode)).getStringCellValue();
			String indCode = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.indCode)).getStringCellValue();
			Logger.debug("Container bar code "+containerBarcode);
			String taxonName = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.taxonName)).getStringCellValue();
			Integer taxonId = null;
			Long taxonSize = null;
			String nomOrgCollab = null;

			//Get projectCode et sampleCode second import
			//Code projet par défaut DKB
			String projectCode="DKB";
			String sampleCode=null;
			String meta="false";
			String refCollab=indCode;
			String sampleType="biological-sample";
			Date receptionDate=null;

			Integer WoRMSCode = (int) row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.WoRMSCode)).getNumericCellValue();
			String goatStatus = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.goatStatus)).getStringCellValue();
			String taxonomicConfidence = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.taxonomicConfidence)).getStringCellValue();
			String taxonomistNames = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.taxonomistNames)).getStringCellValue();
			String barcodingRef =  row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.barcodingRef)).getStringCellValue();
			String geographicLocation = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.geographicLocation)).getStringCellValue();;
			String geographicRegion = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.geographicRegion)).getStringCellValue();
			String individualType = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.individualType)).getStringCellValue();
			String sex = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.sex)).getStringCellValue();
			String ploidy = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.ploidy)).getStringCellValue();

			String isOtherEukSpecProbability = null;
			if(row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.isOtherEukSpecProbability))!=null) {
				String valueIsOtherEukSpecProbability = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.isOtherEukSpecProbability)).getStringCellValue();
				if(valueIsOtherEukSpecProbability.equals("oui"))
					isOtherEukSpecProbability="true";
				else if(valueIsOtherEukSpecProbability.equals("non"))
					isOtherEukSpecProbability="false";
			}
			String individualViability = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.individualViability)).getStringCellValue();
			String sampleDescription = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.sampleDescription)).getStringCellValue();
			String prepSampleDescription = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.prepSampleDescription)).getStringCellValue();
			String isTaxoVoucher = null;

			String valueIsTaxoVoucher = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.isTaxoVoucher)).getStringCellValue();
			if(valueIsTaxoVoucher.equals("oui"))
				isTaxoVoucher="true";
			else if(valueIsTaxoVoucher.equals("non"))
				isTaxoVoucher="false";

			String individualBatch = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.individualBatch)).getStringCellValue();


			Date packingHourDateValue = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.packingHour)).getDateCellValue();
			SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
			String packingHour = sdfHour.format(packingHourDateValue);
			String initialContainerDescription = null;
			if(row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.initialContainerDescription))!=null)
				initialContainerDescription = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.initialContainerDescription)).getStringCellValue();

			String lifeCyclePhase = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.lifeCyclePhase)).getStringCellValue();
			//TODO ajouter NOT APPLICABLE lorsque la valeur n'est pas définie
			String dissectTissue= row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.dissectTissue)).getStringCellValue();
			Integer weight = null;
			if(row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.weight))!=null)
				weight = (int) row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.weight)).getNumericCellValue();
			String conservationMode = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.conservationMode)).getStringCellValue();
			String tempStorage =row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.tempStorage)).getStringCellValue();

			Date collectDate = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.collectDate)).getDateCellValue();
			/*Date collectDate = null;
			try {
				collectDate = sdf.parse(dateString);
			} catch (java.text.ParseException e) {
				Logger.error("Erreur date collect");
			}*/
			Date collectHourDateValue = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.collectHour)).getDateCellValue();
			String collectHour = sdfHour.format(collectHourDateValue);
			String collectMethod = null;
			if(row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.collectMethod))!=null)
				collectMethod = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.collectMethod)).getStringCellValue();

			//Date collectDate = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.collectDate)).getDateCellValue();
			Date containerDate = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.containerDate)).getDateCellValue();
			String descContainer = null;
			if(row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.initialContainerDescription))!=null)
				descContainer = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.initialContainerDescription)).getStringCellValue();
			//TODO to define
			String commentContainer=null;

			//calculate storage and state for non Genoscope container
			String destination = row.getCell(ATSeaConstant.mapColDataSampleFileToReception.get(ATSeaConstant.destination)).getStringCellValue();
			String storage = null;
			String stateContainer = null;
			/*if(!destination.equals("Genoscope")) {
				storage="MNHN/"+destination;
				stateContainer="UA";
			}*/


			ReceptionData data = new ReceptionData(sampleOrigin, containerCategory, containerBarcode, projectCode,
					sampleCode, refCollab, sampleType, taxonId, WoRMSCode,
					goatStatus, nomOrgCollab, taxonName, taxonSize, indCode,
					lifeCyclePhase, dissectTissue, weight, conservationMode, tempStorage,
					collectDate, containerDate, descContainer, commentContainer, meta,
					stateContainer, storage, receptionDate, taxonomicConfidence,
					taxonomistNames, barcodingRef, geographicLocation, geographicRegion,
					individualType, sex, ploidy, isOtherEukSpecProbability,
					individualViability, sampleDescription, prepSampleDescription, isTaxoVoucher,
					individualBatch, packingHour, initialContainerDescription,collectHour, collectMethod, destination);
			//Premier import on prend que les lignes code barre = code unique individu
			//Second import on prend les lignes code barre different de code individu et recupere info sample
			mapSamplingFileReceptionData.put(containerBarcode, data);

		});

		//Conversion pour chaque ligne en fichier receptionConfig
		File xlsx = new File(args.receptionConfigInputPath);
		FileInputStream fis = new FileInputStream(xlsx);
		XSSFWorkbook workbookReception = new XSSFWorkbook (fis);
		Iterator<String> keysMap = mapSamplingFileReceptionData.keySet().iterator();

		int nbRow=1;
		while(keysMap.hasNext()) {
			Row row = workbookReception.getSheetAt(0).getRow(nbRow);
			String key = keysMap.next();
			ReceptionData receptionData = mapSamplingFileReceptionData.get(key);
			setCell(ATSeaConstant.sampleOrigin, row, receptionData.sampleOrigin);
			setCell(ATSeaConstant.containerCategory, row, receptionData.containerCategory);
			setCell(ATSeaConstant.containerBarcode, row, receptionData.containerBarcode);
			setCell(ATSeaConstant.projectCode, row, receptionData.projectCode);

			setCell(ATSeaConstant.sampleType, row, receptionData.sampleType);
			setCell(ATSeaConstant.taxonId, row, receptionData.taxonName);
			setCell(ATSeaConstant.WoRMSCode, row, receptionData.WoRMSCode);
			setCell(ATSeaConstant.goatStatus, row, receptionData.goatStatus);
			setCell(ATSeaConstant.nomOrgCollab, row, receptionData.nomOrgCollab);
			setCell(ATSeaConstant.taxonomicConfidence, row, receptionData.taxonomicConfidence);
			setCell(ATSeaConstant.taxonomistNames, row, receptionData.taxonomistNames);
			setCell(ATSeaConstant.barcodingRef, row, receptionData.barcodingRef);
			setCell(ATSeaConstant.taxonSize, row, receptionData.taxonSize);
			setCell(ATSeaConstant.geographicLocation, row, receptionData.geographicLocation);
			setCell(ATSeaConstant.geographicRegion, row, receptionData.geographicRegion);
			setCell(ATSeaConstant.collectDate, row, receptionData.collectDate);
			setCell(ATSeaConstant.collectHour, row, receptionData.collectHour);
			setCell(ATSeaConstant.collectMethod, row, receptionData.collectMethod);
			setCell(ATSeaConstant.individualType, row, receptionData.individualType);
			setCell(ATSeaConstant.lifeCyclePhase, row, receptionData.lifeCyclePhase);
			setCell(ATSeaConstant.sex, row, receptionData.sex);
			setCell(ATSeaConstant.ploidy, row, receptionData.ploidy);
			setCell(ATSeaConstant.isOtherEukSpecProbability, row, receptionData.isOtherEukSpecProbability);
			setCell(ATSeaConstant.individualViability, row, receptionData.individualViability);
			setCell(ATSeaConstant.sampleDescription, row, receptionData.sampleDescription);
			setCell(ATSeaConstant.isTaxoVoucher, row, receptionData.isTaxoVoucher);
			setCell(ATSeaConstant.individualBatch, row, receptionData.individualBatch);
			setCell(ATSeaConstant.indCode, row, receptionData.indCode);
			setCell(ATSeaConstant.dissectTissue, row, receptionData.dissectTissue);
			setCell(ATSeaConstant.weight, row, receptionData.weight);
			setCell(ATSeaConstant.destination, row, receptionData.destination);
			setCell(ATSeaConstant.conservationMode, row, receptionData.conservationMode);
			setCell(ATSeaConstant.tempStorage, row, receptionData.tempStorage);
			setCell(ATSeaConstant.containerDate, row, receptionData.containerDate);
			setCell(ATSeaConstant.packingHour, row, receptionData.packingHour);

			setCell(ATSeaConstant.initialContainerDescription, row, receptionData.initialContainerDescription);
			setCell(ATSeaConstant.meta, row, receptionData.meta);
			setCell(ATSeaConstant.refCollab, row, receptionData.refCollab);
			if(receptionData.stateContainer!=null)
				setCell(ATSeaConstant.stateContainer, row, receptionData.stateContainer);
			if(receptionData.storage!=null)
				setCell(ATSeaConstant.storageNGL, row, receptionData.storage);
			if(receptionData.receptionDate!=null)
				setCell(ATSeaConstant.receptionDate, row, receptionData.receptionDate);
			nbRow++;
		}
		//Write outputFile

		String fileOutputName = "ReceptionBiologiqueERGAATLASEA_v2.6.xlsx";

		try (OutputStream fileOut = new FileOutputStream(fileOutputName)) {
			workbookReception.write(fileOut);
			fileOut.close();
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
		Logger.debug("End of generated file");
	}

	private void convertInfoTaxo(String taxonName, Map<String,TaxonInfo> mapTaxon) throws InterruptedException, ExecutionException {
		if(!mapTaxon.containsKey(taxonName)) {
			//"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=Mus+musculus[ScientificName]"
			//Interroger GOAT
			//https://goat.genomehubs.org/api/v2/search?query=tax_tree%28Alsidium%20corallinum%29&result=taxon&taxonomy=ncbi&includeEstimates=true&summaryValues=count&size=25#tax_tree(Alsidium%20corallinum)
			//https://goat.genomehubs.org/api/v2/search?query=tax_tree%28Alsidium%20corallinum%29&result=taxon&taxonomy=ncbi&includeEstimates=true
			TaxonInfo taxonInfo = taxonomyServices.getTaxon(taxonName);
			mapTaxon.put(taxonName, taxonInfo);

		}
	}

	private void setCell(String code, Row row, Object value) {

		SimpleDateFormat sdfExcel = new SimpleDateFormat("dd/MM/yyyy");

		int number =  ATSeaConstant.mapColReceptionConfig.get(code);
		Cell cell = row.getCell(number);
		if(cell==null) {
			cell = row.createCell(number);
		}
		if(value instanceof Date) {
			Date date = (Date)value;
			//cell.setCellValue(sdfExcel.format(date));
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



}
