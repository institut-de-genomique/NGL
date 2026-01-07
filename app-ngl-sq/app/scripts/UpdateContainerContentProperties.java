package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
/**
 * Script mise à jour d'une propriété content sur container
 * Ce script va mettre à jour la propriété uniquement sur la descendance du container 
 * Permet de gérer entre autre les mises à jour ATLASea et ERGA avec stratégie code individu
 * Pour un même sample plusieurs tubes avec des propriétés différentes qui ont chacun leur arbre de vie
 * Exemple propriété : tissus disséqué
 * Exemple support : SUPSQ-5714
 * 
 * Format fichier
 * codeContainerOrigine code propriété valeur propriété
 * 
 * Seuls les containers d'origine seront mis à jour à savoir ceux qui n'ont pas d'ascendance
 */
public class UpdateContainerContentProperties extends ScriptWithExcelBody {

	private static final String user = "ngl-admin";

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		getLogger().debug("Start update content properties");
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapContainer", new ArrayList<String>());
		cv.putObject("RecapExperiment", new ArrayList<String>());
		cv.putObject("RecapProcess", new ArrayList<String>());
		cv.putObject("RecapReadSet", new ArrayList<String>());
		cv.putObject("RecapError", new ArrayList<String>());

		((List<String>)cv.getObject("RecapContainer")).add("Code originContainer,code container,codeProperty,valueProperty");
		((List<String>)cv.getObject("RecapExperiment")).add("Code originContainer,code experiment,codeProperty,valueProperty");
		((List<String>)cv.getObject("RecapProcess")).add("Code originContainer,code process udpated,codeProperty,valueProperty");
		((List<String>)cv.getObject("RecapReadSet")).add("Code originContainer,code readSet, statut soumission, codeProperty,valueProperty");
		((List<String>)cv.getObject("RecapError")).add("Type erreur,Erreur");


		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header

			String originContainerCode = row.getCell(0).getStringCellValue();
			String propertyCode = row.getCell(1).getStringCellValue();
			//Gestion suppression
			Object propertyValue = null;

			if(row.getCell(2)!=null) {
				if(row.getCell(2).getCellType()==Cell.CELL_TYPE_NUMERIC)
					propertyValue = row.getCell(2).getNumericCellValue();
				else
					propertyValue = row.getCell(2).getStringCellValue();

			}

			Logger.debug("Origin container "+originContainerCode+" propertyCode "+propertyCode+" propertyValue "+propertyValue);
			//Get Container from database
			Container originContainer = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, originContainerCode);
			//Check container is origin
			if(originContainer.treeOfLife == null || 
					(originContainer.treeOfLife!=null && originContainer.treeOfLife.paths==null) || 
					(originContainer.treeOfLife.paths!=null && originContainer.treeOfLife.paths.size()==0)) {
				Logger.debug("Update container origine");
				//Update origin container content et get sampleCode and sampleAliquoteCode 
				//check same code container et sampleAliquoteCode
				if(originContainer.contents.get(0).properties.containsKey("sampleAliquoteCode")) {
					if(originContainer.code.equals(originContainer.contents.get(0).properties.get("sampleAliquoteCode").getValue())) {
						boolean isValid = updateContainer(originContainer, originContainer, propertyCode, propertyValue, cv);

						//Si container valide on continue sinon on arrête 
						if(isValid) {
							Object propertyValueToUpdate = propertyValue;
							//Get all descendant containers in DB
							Logger.debug("Get containers...");
							Set<String> descendantContainerCodes = new HashSet<String>();
							MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.regex("treeOfLife.paths", Pattern.compile("^,"+originContainerCode)))
							.cursor
							.forEach(container->{
								Logger.debug("Update container "+container.code);
								if(container.contents.stream().filter(content->content.properties.containsKey("sampleAliquoteCode")).collect(Collectors.toList()).size()>0) {
									//For each container update content with sampleCode et sampleAliquoteCode
									updateContainer(container, originContainer, propertyCode, propertyValueToUpdate, cv);
								}else {
									((List<String>)cv.getObject("RecapError")).add("ContainerCode"+","+"Erreur no sampleAliquotecode"+originContainerCode+" in "+container.code);
								}
								descendantContainerCodes.add(container.code);
							});

							descendantContainerCodes.add(originContainerCode);

							Logger.debug("Get experiments...");
							//Get experiment with container list
							MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
									DBQuery.or(DBQuery.in("inputContainerCodes", descendantContainerCodes), DBQuery.in("outputContainerCodes", descendantContainerCodes)))
							.toList().stream().distinct().forEach(experiment->{
								Logger.debug("Update experiment "+experiment.code);
								//For each experiment update content with sampleCode and sampleAliquoteCode
								updateExperiment(experiment, originContainer, propertyCode, propertyValueToUpdate, cv);

							});

							Logger.debug("Get processes...");
							//Get Processes with container list or sampleAliquoteCode
							MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.properties.sampleAliquoteCode.value", originContainerCode))
							.cursor
							.forEach(process->{
								Logger.debug("Update process "+process.code);
								//For each process update sampleOnInputContainer properties identified with sampleAliquoteCode
								updateProcess(process, propertyCode, propertyValueToUpdate, originContainer, cv);
							});

							Logger.debug("Get readsets...");
							//Get ReadSet with sampleAliquoteCode
							MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.in("sampleOnContainer.containerCode", descendantContainerCodes), DBQuery.is("sampleOnContainer.properties.sampleAliquoteCode.value", originContainerCode)))
							.cursor
							.forEach(readset->{
								Logger.debug("Update readset "+readset.code);
								//For each Readset update sampleOnContainer properties identified with sampleAliquoteCode
								updateReadSet(readset, propertyCode, propertyValueToUpdate, originContainer, cv);
							});
						}

					}else {
						Logger.debug("Erreur sampleAliquoteCode in containerOrigin"+originContainerCode);
						cv.addError("ContainerCode", "Erreur sampleAliquotecode"+originContainerCode);
						((List<String>)cv.getObject("RecapError")).add("ContainerCode"+","+"Erreur sampleAliquotecode"+originContainerCode);
					}
				}else {
					Logger.debug("No sampleAliquoteCode in containerOrigin"+originContainerCode);
					cv.addError("ContainerCode", "Erreur no sampleAliquotecode"+originContainerCode);
					((List<String>)cv.getObject("RecapError")).add("ContainerCode"+","+"Erreur no sampleAliquotecode"+originContainerCode);
				}
			}else {
				Logger.debug("No originContainer for"+originContainerCode);
				cv.addError("ContainerCode", "No originContainer for"+originContainerCode);
				((List<String>)cv.getObject("RecapError")).add("ContainerCode"+","+"No originContainer for"+originContainerCode);
			}

		});

		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);

		Logger.info("End of update ");
	}

	private boolean checkSampleAliquoteExistInContainer(Container containerToUpdate, ContextValidation cv)
	{
		if(containerToUpdate.contents.stream().filter(content->content.properties.containsKey("sampleAliquoteCode")).collect(Collectors.toList()).size()>0)
			return true;
		else
			return false;
	}

	private boolean updateContainer(Container containerToUpdate, Container originContainer, String propertyCode, Object propertyValue, ContextValidation cv)
	{
		Boolean isValid=true;
		ContextValidation cvUpdateContainer = ContextValidation.createUpdateContext(user);
		//Update origin container content et get sampleCode and sampleAliquoteCode (check same code container)
		containerToUpdate.contents.stream().filter(content-> (content.properties.containsKey("sampleAliquoteCode") && content.properties.get("sampleAliquoteCode").getValue().equals(originContainer.code)))
		.forEach(content->{
			if(propertyValue==null || (propertyValue!=null && propertyValue.equals("")))
				content.properties.remove(propertyCode);
			else
				content.properties.put(propertyCode, new PropertySingleValue(propertyValue));
		});
		//Add traceInformation
		containerToUpdate.traceInformation.modifyDate=new Date();
		containerToUpdate.traceInformation.modifyUser=cvUpdateContainer.getUser();
		containerToUpdate.validate(cvUpdateContainer, containerToUpdate.importTypeCode, containerToUpdate.state.code);
		if(!cvUpdateContainer.hasErrors()) {
			((List<String>)cv.getObject("RecapContainer")).add(originContainer.code+","+containerToUpdate.code+","+propertyCode+","+propertyValue);
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containerToUpdate);
		}else {
			isValid=false;
			cv.addError("ContainerValidation", "Container not valid "+containerToUpdate.code+ " "+cvUpdateContainer.getErrors());
			((List<String>)cv.getObject("RecapError")).add("ContainerValidation"+","+"Container not valid "+containerToUpdate.code+ " "+cvUpdateContainer.getErrors());
		}
		return isValid;
	}

	private void updateExperiment(Experiment experimentToUpdate, Container originContainer, String propertyCode, Object propertyValue, ContextValidation cv)
	{
		ContextValidation cvUpdateExp = ContextValidation.createUpdateContext(user);
		//Update inputContainerUsed
		List<Content> ICUcontents =  experimentToUpdate.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds)
				.flatMap(List::stream)
				.map(icu -> icu.contents)
				.flatMap(List::stream)
				.filter(content->(content.properties.containsKey("sampleAliquoteCode") && content.properties.get("sampleAliquoteCode").getValue().equals(originContainer.code))).collect(Collectors.toList());

		ICUcontents.forEach(content -> {
			if(propertyValue==null || (propertyValue!=null && propertyValue.equals("")))
				content.properties.remove(propertyCode);
			else
				content.properties.put(propertyCode, new PropertySingleValue(propertyValue));
		});

		//Update outputContainerUsed
		List<Content> OCUcontents = new ArrayList<>();
		
		experimentToUpdate.atomicTransfertMethods
				.forEach(atm -> {
					if (atm.outputContainerUseds != null) {
						atm.outputContainerUseds.forEach(ocu -> {
							if (ocu != null && ocu.contents != null) {
								OCUcontents.addAll(ocu.contents);
							}
						});
					}
				});

		List<Content> OCUcontents2 = new ArrayList<>();

		for (int i = 0; i < OCUcontents.size(); i++) {
			if (OCUcontents.get(i).properties.containsKey("sampleAliquoteCode") && OCUcontents.get(i).properties.get("sampleAliquoteCode").getValue().equals(originContainer.code)) {
				OCUcontents2.add(OCUcontents.get(i));
			}
		}
			
		OCUcontents2.forEach(content -> {
			if(propertyValue==null || (propertyValue!=null && propertyValue.equals("")))
				content.properties.remove(propertyCode);
			else
				content.properties.put(propertyCode, new PropertySingleValue(propertyValue));
		});

		//Validate experiment
		//Add traceInformation
		experimentToUpdate.traceInformation.modifyDate=new Date();
		experimentToUpdate.traceInformation.modifyUser=cvUpdateExp.getUser();
		// experimentToUpdate.validate(cvUpdateExp, experimentToUpdate.state.code); // Désactivé pour le NGL-4697.
		if(!cvUpdateExp.hasErrors()) {
			((List<String>)cv.getObject("RecapExperiment")).add(originContainer.code+","+experimentToUpdate.code+","+propertyCode+","+propertyValue);
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, experimentToUpdate);
		}else {
			cv.addError("Experiment Validation", "Experiment not valid "+experimentToUpdate.code+ " "+cvUpdateExp.getErrors());
			((List<String>)cv.getObject("RecapError")).add("Experiment Validation"+","+"Experiment not valid "+experimentToUpdate.code+ " "+cvUpdateExp.getErrors());
		}

		if(ICUcontents.size()==0 && OCUcontents2.size()==0) {
			cv.addError("Experiment Warning", "Experiment with no content with sampleAliquoteCode "+experimentToUpdate.code);
			((List<String>)cv.getObject("RecapError")).add("Experiment Warning"+","+"Experiment with no content with sampleAliquoteCode "+experimentToUpdate.code);
		}

	}

	private void updateProcess(Process processToUpdate, String propertyCode, Object propertyValue, Container originContainer, ContextValidation cv)
	{
		ContextValidation cvUpdateProcess = ContextValidation.createUpdateContext(user);
		if(propertyValue==null || (propertyValue!=null && propertyValue.equals("")))
			processToUpdate.sampleOnInputContainer.properties.remove(propertyCode);
		else
			processToUpdate.sampleOnInputContainer.properties.put(propertyCode, new PropertySingleValue(propertyValue));
		processToUpdate.traceInformation.modifyDate=new Date();
		processToUpdate.traceInformation.modifyUser=cvUpdateProcess.getUser();
		processToUpdate.validateUpdate(cvUpdateProcess);
		if(!cvUpdateProcess.hasErrors()){
			((List<String>)cv.getObject("RecapProcess")).add(originContainer.code+","+processToUpdate.code+","+propertyCode+","+propertyValue);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, processToUpdate);
		}else {
			cv.addError("ProcessValidation", "Process not valid "+processToUpdate.code+ " "+cvUpdateProcess.getErrors());
			((List<String>)cv.getObject("RecapError")).add("ProcessValidation"+","+"Process not valid "+processToUpdate.code+ " "+cvUpdateProcess.getErrors());
		}
	}

	private void updateReadSet(ReadSet readsetToUpdate, String propertyCode, Object propertyValue, Container originContainer, ContextValidation cv)
	{
		//For each Readset update sampleOnContainer properties identified with sampleAliquoteCode
		ContextValidation cvUpdateReadset = ContextValidation.createUpdateContext(user);
		if(propertyValue==null || (propertyValue!=null && propertyValue.equals("")))
			readsetToUpdate.sampleOnContainer.properties.remove(propertyCode);
		else
			readsetToUpdate.sampleOnContainer.properties.put(propertyCode, new PropertySingleValue(propertyValue));
		readsetToUpdate.traceInformation.modifyDate = new Date();
		readsetToUpdate.traceInformation.modifyUser = cvUpdateReadset.getUser();
		readsetToUpdate.validate(cvUpdateReadset);
		if(!cvUpdateReadset.hasErrors()) {
			((List<String>)cv.getObject("RecapReadSet")).add(originContainer.code+","+readsetToUpdate.code+","+readsetToUpdate.submissionState.code+","+propertyCode+","+propertyValue);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readsetToUpdate);
		}else {
			cv.addError("Readset Validation", "Readset not valid "+readsetToUpdate.code+ " "+cvUpdateReadset.getErrors());
			((List<String>)cv.getObject("RecapError")).add("Readset Validation"+","+"Readset not valid "+readsetToUpdate.code+ " "+cvUpdateReadset.getErrors());
		}

		if(readsetToUpdate.submissionState.code.equals("SUB-F"))
			((List<String>)cv.getObject("RecapError")).add("Readset Warning"+","+"Readset with finished submission ");
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
		try (OutputStream fileOut = new FileOutputStream("UpdateContainerContentProperties_out.xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}
}
