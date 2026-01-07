package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ScriptRepriseTrameATS extends ScriptWithExcelBody{

	/**
	 * Script reprise de données des anciennes trames ATLASea/ERGA avec nouvelle verion V3
	 * NGL-4264
	 * 
	 * Format fichier en entrée excel
	 * Onglet 1 : correspondance sample permet d'identifier les propriétés à ajouter au sample dans onglet 2
	 * codePropriété NuméroColonne
	 * Onglet 2 : infos sample (3 lignes de header) première colonne code sample
	 * Onglet 3 : correspondance container permet d'identifier le propriétés à ajouter au container dans onglet 4
	 * codePropriete NuméroColonne
	 * Onglet 4 : infos container (3 lignes de header) première colonne code container
	 * 
	 * Si propriété de niveau sample et content alors executer cascade via ScriptUpdateSampleProperties dans environnement test ou vérifier en prod que la cascade passe
	 * Si propriété de niveau content alors executer la cascade par le script UpdateContainerContentProperties dans environnement test pour identifier pb de pool
	 * 
	 */

	private static final String user = "ngl-admin";

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapErrorSample", new ArrayList<String>());
		cv.putObject("RecapErrorContainer", new ArrayList<String>());
		cv.putObject("RepriseContent", new ArrayList<String>());
		((List<String>)cv.getObject("RecapErrorSample")).add("Code sample,error message");
		((List<String>)cv.getObject("RecapErrorContainer")).add("Code container,error message");
		((List<String>)cv.getObject("RepriseContent")).add("Code container origine,code propriete, valeur property");

		//Sheet 1 Get sample property to add 
		Map<String, Integer> mapSampleProperty = new HashMap<String, Integer>();
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			mapSampleProperty.put(row.getCell(0).getStringCellValue(), (int) row.getCell(1).getNumericCellValue());
		});

		List<String> propertiesDate = Arrays.asList(new String[]{"collectDate"});
		List<String> propertiesHour = Arrays.asList(new String[]{"packingHour", "collectHour"});
		List<String> propertiesBoolean = Arrays.asList(new String[]{"isTaxoVoucher", "isOtherEukSpecProbability","isAxenic"});
		List<String> propertiesNumeric = Arrays.asList(new String[]{"WoRMSCode","eukCellDensity","collectedVolume","eukNbCells","bacterialDensity"});
		//Sheet 2 Parse sample information and add property
		workbook.getSheetAt(1).rowIterator().forEachRemaining(row -> {
			// skip header
			if(row.getRowNum() > 2) {
				ContextValidation cvSample = ContextValidation.createUpdateContext(user);
				//Get sampleCode
				String sampleCode = row.getCell(0).getStringCellValue();
				Logger.debug("Update sample "+sampleCode);
				//Get sample
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

				for(String property: mapSampleProperty.keySet()) {
					Logger.debug("Get property : "+property);
					//Gestion des cas spécifique
					if(property.equals("individualBatch")) {
						//specific propery lien regroupement
						sample.properties.put(property, new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getStringCellValue()));
						sample.properties.put("groupIDNumber", new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getStringCellValue()));
					}else if(propertiesDate.contains(property)) {
						sample.properties.put(property, new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getDateCellValue()));
					}else if(propertiesHour.contains(property)) {
						Date date = row.getCell(mapSampleProperty.get(property)).getDateCellValue();
						Logger.debug(date.toString());
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
						String valueDate = sdf.format(date);
						Logger.debug(valueDate);
						sample.properties.put(property, new PropertySingleValue(valueDate));
					}else if(propertiesBoolean.contains(property)) {
						if(row.getCell(mapSampleProperty.get(property)).getStringCellValue().toLowerCase().equals("oui")) {
							sample.properties.put(property, new PropertySingleValue(Boolean.TRUE));
						}else if(row.getCell(mapSampleProperty.get(property)).getStringCellValue().toLowerCase().equals("non")){
							sample.properties.put(property, new PropertySingleValue(Boolean.FALSE));
						}else {
							sample.properties.put(property, new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getStringCellValue()));
						}
					}else if(propertiesNumeric.contains(property)) {
						sample.properties.put(property, new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getNumericCellValue()));
					}else{					
						//Ajouter la propriété dans sample properties
						sample.properties.put(property, new PropertySingleValue(row.getCell(mapSampleProperty.get(property)).getStringCellValue()));
					}

					//TODO attention comment gerer si valeur autre que String
					//TODO lifeCycle à voir pour les autres trames
					//TODO date de collecte à voir pour d'autres trames pour ERGA prendre valeur dans fichier

				}
				//add traceInformation update
				sample.traceInformation.modifyDate=new Date();
				sample.traceInformation.modifyUser=cv.getUser();

				//changer type import 
				sample.importTypeCode="reference-genome-biological-sample-reception";

				//validate sample
				sample.validate(cv);
				if(cvSample.hasErrors()) {
					((List<String>)cv.getObject("RecapErrorSample")).add(sample.code+","+cvSample.getErrors());
				}else {
					//update sample in DB
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}

			}
		});
		//Sheet 3 Get container property to add
		Map<String, Integer> mapContainerProperty = new HashMap<String, Integer>();
		workbook.getSheetAt(2).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			mapContainerProperty.put(row.getCell(0).getStringCellValue(), (int) row.getCell(1).getNumericCellValue());
		});
		//TODO faire une liste des propriété de niveau content
		List<String> propertiesContent = Arrays.asList(new String[]{"initialContainerDescription","isAxenic"});
		//Sheet 4 Parse container information and add property
		workbook.getSheetAt(3).rowIterator().forEachRemaining(row -> {

			if(row.getRowNum() > 2) {
				ContextValidation cvContainer = ContextValidation.createUpdateContext(user);
				String containerCode = row.getCell(0).getStringCellValue();
				Logger.debug("Update container "+containerCode);
				//Get container
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
				for(String property: mapContainerProperty.keySet()) {
					Logger.debug("Get property "+property);
					PropertySingleValue ps = null;
					if(propertiesHour.contains(property)) {
						if(row.getCell(mapContainerProperty.get(property))!=null) {
							Date date = row.getCell(mapContainerProperty.get(property)).getDateCellValue();
							SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
							String valueDate = sdf.format(date);
							ps = new PropertySingleValue(valueDate);
						}
					}else if(propertiesBoolean.contains(property)) {
						if(row.getCell(mapContainerProperty.get(property)).getStringCellValue().toLowerCase().equals("oui")) {
							ps=new PropertySingleValue(Boolean.TRUE);
						}else if(row.getCell(mapContainerProperty.get(property)).getStringCellValue().toLowerCase().equals("non")){
							ps=new PropertySingleValue(Boolean.FALSE);
						}else {
							ps=new PropertySingleValue(row.getCell(mapContainerProperty.get(property)).getStringCellValue());
						}
					}else if(propertiesNumeric.contains(property)) {
						ps= new PropertySingleValue(row.getCell(mapContainerProperty.get(property)).getNumericCellValue());
					}else {
						ps=new PropertySingleValue(row.getCell(mapContainerProperty.get(property)).getStringCellValue());
					}

					if(propertiesContent.contains(property)) {
						container.contents.get(0).properties.put(property, ps);
						//TODO generate fichier to update
						((List<String>)cv.getObject("RepriseContent")).add(container.code+","+property+","+ps.getValue());
					}else {
						container.properties.put(property, ps);
					}
				}
				//add traceInformation
				container.traceInformation.modifyDate=new Date();
				container.traceInformation.modifyUser=cv.getUser();

				//Modifier import type code 
				container.importTypeCode="reference-genome-biological-sample-reception";

				//validate container
				container.validate(cv, container.importTypeCode, container.state.code);
				if(cvContainer.hasErrors()) {
					((List<String>)cv.getObject("RecapErrorContainer")).add(container.code+","+cvContainer.getErrors());
				}else {
					//update container in DB
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
				}
			}

			//TODO gerer cas où propriété de niveau content. Faire fichier recap pour lancement script update container content
			//TODO gerer cas où valeur n'est pas string
		});
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);

	}

	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	private void createExcelFileRecap(ContextValidation cv)	{

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
		try (OutputStream fileOut = new FileOutputStream("ScriptRepriseTrameATS_out.xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}

}
