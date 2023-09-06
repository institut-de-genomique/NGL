package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.tree.ParentContainers;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Modification du code container initiale (equivaut au sampleAliquoteCode) pour ticket support BSI (NGL-2507)
 * La descendance est donnée dans le fichier en entrée ( pas de recherche de descendance)
 * Entrée fichier excel : un onglet Container  = Code Container  / Old Code Container Ini / New Code Container IniExperiment 
 * Code Container : container à mettre à jour
 * Old Code Container Ini : Ancien code container initiale, si le container à mettre à jour et le container initial alors Code Container=Old Code Container Ini
 * New Code Container Ini : Nouveau code container initiale
 * Onglet Experiment : liste des expérience à mettre à jour selon map code old container ini/new code ini = Code Experiment
 * Onglet Process : liste des process à mettre à jour selon map code old container ini/new code ini = Code Process
 * Onglet ReadSet : liste des readSet à mettre à jour selon map code old container ini/new code ini = Code ReadSet
 * @author ejacoby
 *
 */

public class ModifCodeContainerIniFromCodeList extends ScriptWithExcelBody{

	final String user = "ngl-admin";

	public static class Args {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapContainer", new ArrayList<String>());
		cv.putObject("RecapExperiment", new ArrayList<String>());
		cv.putObject("RecapProcess", new ArrayList<String>());
		cv.putObject("RecapReadSet", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapContainer")).add("Code container,collection,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapExperiment")).add("Code Experiment,collection,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapProcess")).add("Code Process,collection,propriete collection MAJ,valeur initiale,valeur finale");
		((List<String>)cv.getObject("RecapReadSet")).add("Code ReadSet,collection,propriete collection MAJ,valeur initiale,valeur finale");
		//((List<String>)cv.getObject("RecapContainerSupport")).add("Code sample,collection,propriete collection MAJ,code collection,valeur initiale,valeur finale");
		Map<String, String> mapContainer = new HashMap<String,String>();
		for(int i=0; i<workbook.getNumberOfSheets();i++){
			XSSFSheet sheet = workbook.getSheetAt(i);
			sheet.rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				if(sheet.getSheetName().equals("Container")){
					if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
						String codeContainer = row.getCell(0).getStringCellValue();
						String oldCodeContainerIni = row.getCell(1).getStringCellValue();
						String newCodeContainerIni = row.getCell(2).getStringCellValue();
						mapContainer.put(oldCodeContainerIni, newCodeContainerIni);
						if(!codeContainer.equals("")){
							updateContainer(codeContainer, oldCodeContainerIni, newCodeContainerIni, cv);
						}
					}
				}else if(sheet.getSheetName().equals("Experiment")){
					if(row!=null && row.getCell(0)!=null){
						String codeExperiment = row.getCell(0).getStringCellValue();
						updateExperiment(codeExperiment, mapContainer, cv);
					}
				}else if(sheet.getSheetName().equals("Process")){
					if(row!=null && row.getCell(0)!=null){
						String codeProcess = row.getCell(0).getStringCellValue();
						updateProcess(codeProcess, mapContainer, cv);
					}
				}else if(sheet.getSheetName().equals("ReadSet")){
					if(row!=null && row.getCell(0)!=null){
						String codeReadSet = row.getCell(0).getStringCellValue();
						updateReadSet(codeReadSet, mapContainer, cv);
					}
				}
			});
		}

		//Get error
		if(cv.hasErrors()){
			Logger.debug(cv.getErrors().toString());
		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
		Logger.debug("End ModifCodeContainerIniFromCodeList");
	}


	@SuppressWarnings("unchecked")
	private void updateContainer(String codeContainer, String oldCodeContainerIni, String newCodeContainerIni,  ContextValidation cv)
	{
		Logger.debug("Update Container"+new Date()+" "+codeContainer);
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME,Container.class, codeContainer);


		for(Content content : container.contents){
			if(content.properties.containsKey("sampleAliquoteCode") && content.properties.get("sampleAliquoteCode").getValue().equals(oldCodeContainerIni)){
				((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",contents.properties.sampleAliquoteCode,"+oldCodeContainerIni+","+newCodeContainerIni);
				content.properties.put("sampleAliquoteCode", new PropertySingleValue(newCodeContainerIni));
			}
		}
		if(container.treeOfLife!=null){
			if(container.treeOfLife.from!=null && container.treeOfLife.from.containers!=null){
				for(ParentContainers pc : container.treeOfLife.from.containers){
					if(pc.code.equals(oldCodeContainerIni)){
						pc.code=newCodeContainerIni;
						pc.supportCode=newCodeContainerIni;
						((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",treeOfLife.from.containers.code,"+oldCodeContainerIni+","+newCodeContainerIni);
					}
				}
			}
			if(container.treeOfLife.paths!=null){
				List<String> newPaths = new ArrayList<String>();
				for(String path:container.treeOfLife.paths){
					String newPath=null;
					if(path.contains(","+oldCodeContainerIni+",")){
						newPath=path.replace(","+oldCodeContainerIni+",",","+newCodeContainerIni+",");
						((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",treeOfLife.paths,"+oldCodeContainerIni+","+newCodeContainerIni);
					}else if(Pattern.compile(","+oldCodeContainerIni+"$").matcher(path).matches()){
						newPath=path.replace(","+oldCodeContainerIni, ","+newCodeContainerIni);
						((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",treeOfLife.paths,"+oldCodeContainerIni+","+newCodeContainerIni);
					}else{
						newPath=path;
					}
					newPaths.add(newPath);
				}
				container.treeOfLife.paths=newPaths;
			}
		}

		if(codeContainer.equals(oldCodeContainerIni)){
			container.code=newCodeContainerIni;
			container.support.code=newCodeContainerIni;
			((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",code,"+oldCodeContainerIni+","+newCodeContainerIni);
			((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_COLL_NAME+",support.code,"+oldCodeContainerIni+","+newCodeContainerIni);
			((List<String>)cv.getObject("RecapContainer")).add(codeContainer+","+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+",code,"+oldCodeContainerIni+","+newCodeContainerIni);
			MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,
					DBQuery.is("code", oldCodeContainerIni),
					DBUpdate.set("code", newCodeContainerIni));
		}

		Logger.debug("collection "+InstanceConstants.CONTAINER_COLL_NAME);
		Logger.debug("class "+Container.class);
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
				DBQuery.is("code", codeContainer),
				DBUpdate.set("code", container.code).set("support.code", container.support.code).set("contents", container.contents));
		if(container.treeOfLife!=null){
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code", container.code),
					DBUpdate.set("treeOfLife", container.treeOfLife));
		}



	}

	@SuppressWarnings("unchecked")
	private void updateExperiment(String codeExperiment, Map<String, String> mapContainer,  ContextValidation cv)
	{
		Logger.debug("Update Experiment"+new Date()+" "+codeExperiment);
		//Find experiment
		Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, codeExperiment);
		experiment.atomicTransfertMethods.forEach(atm->{
			atm.inputContainerUseds.forEach(icu->{
				if(mapContainer.containsKey(icu.code)){
					((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",icu.code,"+icu.code+","+mapContainer.get(icu.code));
					icu.code=mapContainer.get(icu.code);
				}
				if(mapContainer.containsKey(icu.locationOnContainerSupport.code)){
					((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",icu.locationOnContainerSupport.code,"+icu.locationOnContainerSupport.code+","+mapContainer.get(icu.locationOnContainerSupport.code));
					icu.locationOnContainerSupport.code=mapContainer.get(icu.locationOnContainerSupport.code);
				}

				icu.contents.stream().forEach(content->{
					//Get sampleAliquoteCode
					if(content.properties.containsKey("sampleAliquoteCode") && mapContainer.containsKey(content.properties.get("sampleAliquoteCode").getValue())){
						((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",icu.contents.sampleAliquoteCode,"+content.properties.get("sampleAliquoteCode").getValue()+","+mapContainer.get(content.properties.get("sampleAliquoteCode").getValue()));
						content.properties.put("sampleAliquoteCode", new PropertySingleValue(mapContainer.get(content.properties.get("sampleAliquoteCode").getValue())));
					}
				});
			});
			if(atm.outputContainerUseds!=null){
				atm.outputContainerUseds.forEach(ocu->{
					ocu.contents.stream().forEach(content->{
						if(content.properties.containsKey("sampleAliquoteCode") && mapContainer.containsKey(content.properties.get("sampleAliquoteCode").getValue())){
							((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",ocu.contents.sampleAliquoteCode,"+content.properties.get("sampleAliquoteCode").getValue()+","+mapContainer.get(content.properties.get("sampleAliquoteCode").getValue()));
							content.properties.put("sampleAliquoteCode", new PropertySingleValue(mapContainer.get(content.properties.get("sampleAliquoteCode").getValue())));
						}
					});
				});
			}
		});
		Set<String> newInputContainerCodes = new HashSet<String>();
		for(String inputCode : experiment.inputContainerCodes){
			if(mapContainer.containsKey(inputCode))
				newInputContainerCodes.add(mapContainer.get(inputCode));
			else
				newInputContainerCodes.add(inputCode);
		}
		((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",inputContainerCodes,"+experiment.inputContainerCodes+","+newInputContainerCodes);

		experiment.inputContainerCodes=newInputContainerCodes;
		Set<String> newInputContainerSupportCodes = new HashSet<String>();

		for(String inputSupportCode : experiment.inputContainerSupportCodes){
			if(mapContainer.containsKey(inputSupportCode))
				newInputContainerSupportCodes.add(mapContainer.get(inputSupportCode));
			else
				newInputContainerSupportCodes.add(inputSupportCode);
		}
		experiment.inputContainerSupportCodes=newInputContainerSupportCodes;
		
		((List<String>)cv.getObject("RecapExperiment")).add(codeExperiment+","+InstanceConstants.EXPERIMENT_COLL_NAME+",inputContainerSupportCodes,"+experiment.inputContainerSupportCodes+","+newInputContainerSupportCodes);

		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, experiment);
	}


	@SuppressWarnings("unchecked")
	private void updateProcess(String codeProcess, Map<String, String> mapContainer, ContextValidation cv)
	{
		Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, codeProcess);
		if(mapContainer.containsKey(process.inputContainerCode)){
			((List<String>)cv.getObject("RecapProcess")).add(codeProcess+","+InstanceConstants.PROCESS_COLL_NAME+",inputContainerCode,"+process.inputContainerCode+","+mapContainer.get(process.inputContainerCode));
			process.inputContainerCode=mapContainer.get(process.inputContainerCode);

		}
		if(mapContainer.containsKey(process.inputContainerSupportCode)){
			((List<String>)cv.getObject("RecapProcess")).add(codeProcess+","+InstanceConstants.PROCESS_COLL_NAME+",inputContainerSupportCode,"+process.inputContainerSupportCode+","+mapContainer.get(process.inputContainerSupportCode));
			process.inputContainerSupportCode=mapContainer.get(process.inputContainerSupportCode);
		}
		if(mapContainer.containsKey(process.sampleOnInputContainer.containerSupportCode)){
			((List<String>)cv.getObject("RecapProcess")).add(codeProcess+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.containerSupportCode,"+process.sampleOnInputContainer.containerSupportCode+","+mapContainer.get(process.sampleOnInputContainer.containerSupportCode));
			process.sampleOnInputContainer.containerSupportCode=mapContainer.get(process.sampleOnInputContainer.containerSupportCode);
		}
		if(mapContainer.containsKey(process.sampleOnInputContainer.containerCode)){
			((List<String>)cv.getObject("RecapProcess")).add(codeProcess+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.containerCode,"+process.sampleOnInputContainer.containerCode+","+mapContainer.get(process.sampleOnInputContainer.containerCode));
			process.sampleOnInputContainer.containerCode=mapContainer.get(process.sampleOnInputContainer.containerCode);
		}
		if(process.sampleOnInputContainer.properties.containsKey("sampleAliquoteCode") && mapContainer.containsKey(process.sampleOnInputContainer.properties.get("sampleAliquoteCode").getValue())){
			((List<String>)cv.getObject("RecapProcess")).add(codeProcess+","+InstanceConstants.PROCESS_COLL_NAME+",sampleOnInputContainer.properties.sampleAliquoteCode,"+process.sampleOnInputContainer.properties.get("sampleAliquoteCode").getValue()+","+mapContainer.get(process.sampleOnInputContainer.properties.get("sampleAliquoteCode").getValue()));
			process.sampleOnInputContainer.properties.put("sampleAliquoteCode", new PropertySingleValue(mapContainer.get(process.sampleOnInputContainer.properties.get("sampleAliquoteCode").getValue())));
		}
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME,process);
	}

	@SuppressWarnings("unchecked")
	private void updateReadSet(String codeReadSet, Map<String, String> mapContainer, ContextValidation cv)
	{
		ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
		if(rs.sampleOnContainer.properties.containsKey("sampleAliquoteCode") && mapContainer.containsKey(rs.sampleOnContainer.properties.get("sampleAliquoteCode").getValue())){
			((List<String>)cv.getObject("RecapReadSet")).add(codeReadSet+","+InstanceConstants.READSET_ILLUMINA_COLL_NAME+",sampleOnContainer.properties.sampleAliquoteCode,"+rs.sampleOnContainer.properties.get("sampleAliquoteCode").getValue()+","+mapContainer.get(rs.sampleOnContainer.properties.get("sampleAliquoteCode").getValue()));
			rs.sampleOnContainer.properties.put("sampleAliquoteCode", new PropertySingleValue(mapContainer.get(rs.sampleOnContainer.properties.get("sampleAliquoteCode").getValue())));
		}
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
				DBQuery.is("code", rs.code),
				DBUpdate.set("sampleOnContainer.properties", rs.sampleOnContainer.properties));

	}

	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//Logger.debug(recap);
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
		try (OutputStream fileOut = new FileOutputStream("Test.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}

	


}
