package scripts.ATSea;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import akka.protobuf.TextFormat.ParseException;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import play.Logger;
import scripts.ATSea.model.ATSeaConstant;
import scripts.ATSea.model.CardopsData;
import validation.ContextValidation;

/**
 * Script permettant de récupérer les données de station MNHN Cardops pour les ajouter a la trame echantillonnage
 * Prend en entrée un fichier excel avec 
 * onglet 1 Info Trame
 * onglet 2 trame echantillonnage à compléter
 * onglet 3 Liste de valeurs
 * onglet 4 Data cardops
 * @author ejacoby
 *
 */

public class AddStationMetaDataToSamplingFile extends ScriptWithExcelBody {

	private static final String user = "ngl-support";


	@Override
	public void execute(XSSFWorkbook workbook) throws Exception, ParseException {
		getLogger().debug("Start add station metadata to samplingFile");

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("TrameInfo", new ArrayList<String>());

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		//Lecture Onglet Cardops
		//Construction Map Data Cardops
		Map<String, CardopsData> mapCardopsData = new HashMap<String, CardopsData>();
		Logger.debug(workbook.getSheetAt(0).getSheetName());
		Logger.debug(workbook.getSheetAt(1).getSheetName());
		Logger.debug(workbook.getSheetAt(2).getSheetName());
		Logger.debug(workbook.getSheetAt(3).getSheetName());
		workbook.getSheetAt(3).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String stationNumber = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.stationNumber)).getStringCellValue();
			String dateString = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.collectDate)).getStringCellValue();
			Date collectDate = null;
			try {
				collectDate = sdf.parse(dateString);
			} catch (java.text.ParseException e) {
				cv.addError("Cardops", "Erreur date format "+dateString);
			}
			String collectLocalisation = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.collectLocalisation)).getStringCellValue();
			Double latitudeStart = Double.valueOf(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.latitudeStart)).getStringCellValue());
			Double longitudeStart = Double.valueOf(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.longitudeStart)).getStringCellValue());
			Double latitudeEnd = Double.valueOf(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.latitudeEnd)).getStringCellValue());
			Double longitudeEnd = Double.valueOf(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.longitudeEnd)).getStringCellValue());
			Double depth=(double) 0;
			if(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.depth))!=null) {
				String depthString = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.depth)).getStringCellValue();
				if(depthString!=null && !depthString.isEmpty()) {
					depth = Double.valueOf(row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.depth)).getStringCellValue().replace(",","."));
				}
			}
			String collectMethod = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.collectMethod)).getStringCellValue();
			String collectName = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.collectName)).getStringCellValue();
			String habitat = row.getCell(ATSeaConstant.mapColDataCardops.get(ATSeaConstant.habitat)).getStringCellValue();

			CardopsData cdata = new CardopsData(stationNumber, collectDate, collectLocalisation, latitudeStart, longitudeStart, latitudeEnd, longitudeEnd, depth, collectMethod, collectName, habitat);
			if(!mapCardopsData.containsKey(stationNumber)) {
				mapCardopsData.put(stationNumber, cdata);
			}else {
				cv.addError("Cardops", "Station number already exist in cardops data "+stationNumber);
			}
		});

		//Lecture Onglet TrameInfo
		//Ajout infos station
		workbook.getSheetAt(1).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header

			String stationNumber = row.getCell(ATSeaConstant.mapColDataSamplingFile.get(ATSeaConstant.stationNumber)).getStringCellValue().replaceAll(" ", "");
			CardopsData cod = mapCardopsData.get(stationNumber);

			Logger.debug("Row "+row.getRowNum());
			Logger.debug("Station number "+stationNumber);
			if(cod!=null) {
			Logger.debug("cod "+cod);
			Logger.debug(cod.collectDate+" station Number "+stationNumber);
			setCell(ATSeaConstant.collectDate, row, cod.collectDate);
			//Convert date to hour
			SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm:ss");
			String hour = sdfHour.format(cod.collectDate);
			setCell(ATSeaConstant.collectHour, row, hour);
			setCell(ATSeaConstant.collectLocalisation, row, cod.collectLocalisation);
			setCell(ATSeaConstant.latitudeStart, row, cod.latitudeStart);
			setCell(ATSeaConstant.longitudeStart, row, cod.longitudeStart);
			setCell(ATSeaConstant.latitudeEnd, row, cod.latitudeEnd);
			setCell(ATSeaConstant.longitudeEnd, row, cod.longitudeEnd);
			setCell(ATSeaConstant.depth, row, cod.depth);
			setCell(ATSeaConstant.collectMethod, row, cod.collectMethod);
			setCell(ATSeaConstant.collectName, row, cod.collectName);
			setCell(ATSeaConstant.habitat, row, cod.habitat);
			}else {
				Logger.error("No station "+stationNumber);
			}

		});

		//Write outputFile
		try (OutputStream fileOut = new FileOutputStream("SampleFileWithStation.xlsx")) {
			workbook.write(fileOut);
			fileOut.close();
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		//createExcelFileRecap(cv);

		Logger.info("End add station metadata to samplingFile");
	}



	private void setCell(String code, Row row, Object value) {
		
		SimpleDateFormat sdfExcel = new SimpleDateFormat("dd/MM/yyyy");
		
		int number =  ATSeaConstant.mapColDataSamplingFile.get(code);
		Cell cell = row.getCell(number);
		if(cell==null) {
			cell = row.createCell(number);
		}
		if(value instanceof Date) {
			Date date = (Date)value;
			cell.setCellValue(sdfExcel.format(date));
		}
		else if (value instanceof Double)
			cell.setCellValue((Double)value);
		else if(value instanceof String)
			cell.setCellValue((String)value);
	}


	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	private void createExcelFileRecap(ContextValidation cv)	{
		List<String> recaps = cv.getTypedObject("Trame");
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Trame");
		int nbLine=0;
		for(String recap : recaps){
			Logger.debug(recap);
			Row row = sheet.createRow(nbLine);
			String[] tabRecap = recap.split(",");
			for(int i=0;i<tabRecap.length;i++){
				row.createCell(i).setCellValue(
						createHelper.createRichTextString(tabRecap[i]));
			}
			nbLine++;
		}
		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("SampleFileWithStation.xlsx")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}

}
