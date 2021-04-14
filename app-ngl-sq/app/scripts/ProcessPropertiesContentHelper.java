package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import workflows.container.ContentHelper;

/**
 * classe utilitaire qui reprend les services de cascade d'une propriété process de niveau content
 * ATTENTION les tags secondaires ne sont pas pris en compte dans les services
 * TODO à mettre jour avec le nouveau code tag secondaire
 * @author ejacoby
 *
 */
public class ProcessPropertiesContentHelper {


	public static Set<String> getTagAssignFromProcessContainers(Process process) {
		Set<String> tags = null;
		if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)){
			tags = new TreeSet<>();
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());			
		}else if(process.outputContainerCodes != null && process.outputContainerCodes.size() > 0){

			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
					.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
							.in("projectCode",  process.projectCodes)
							.exists("properties.tag"));

			MongoDBResult<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC);
			if(containersWithTag.size() > 0){
				tags = new TreeSet<>();
				tags.add(containersWithTag.cursor.next().contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}
		}
		return tags;
	}

	public static void createExcelFileRecap(ContextValidation cv)
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


	public static void updateProcessContentPropertiesWithCascade(Set<String> containerCodes, Set<String> sampleCodes, Set<String> projectCodes, Set<String> tags,String newPropertyCode, String newPropertyValue, String codeUpdated, ContextValidation validation) {
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.in("code", containerCodes))
		.cursor
		.forEach(container -> {
			container.traceInformation.setTraceInformation(validation.getUser());
			container.contents
			.stream()
			.filter(content -> ((!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode))
					|| (null != tags  && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) 
					&&  tags.contains(content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))))
			.forEach(content -> {
				Query findContentQuery = ContentHelper.getContentQuery(container, content);
				((List<String>)validation.getObject("RecapContainer")).add(codeUpdated+","+container.code+","+newPropertyCode+","+newPropertyValue);
				content.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, findContentQuery, DBUpdate.set("contents.$", content));
			});			
		});

		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or(DBQuery.in("inputContainerCodes", containerCodes),
						DBQuery.in("outputContainerCodes", containerCodes)))
		.cursor
		.forEach(experiment -> {
			experiment.traceInformation.setTraceInformation(validation.getUser());
			experiment.atomicTransfertMethods
			.forEach(atm -> {
				atm.inputContainerUseds
				.stream()
				.filter(icu -> containerCodes.contains(icu.code))
				.map(icu -> icu.contents)
				.flatMap(List::stream)
				.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
				.forEach(content -> {
					content.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				});
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.filter(ocu -> containerCodes.contains(ocu.code))							
					.map(ocu -> ocu.contents)
					.flatMap(List::stream)
					.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
					.forEach(content -> {
						content.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
					});
				}
			});				
			((List<String>)validation.getObject("RecapExperiment")).add(codeUpdated+","+experiment.code+","+newPropertyCode+","+newPropertyValue);

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", experiment.code), 
					DBUpdate.set("atomicTransfertMethods", experiment.atomicTransfertMethods).set("traceInformation", experiment.traceInformation));	
		});	

		//update processes with new exp property values
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME,Process.class, 
				DBQuery.in("sampleOnInputContainer.containerCode", containerCodes).in("sampleOnInputContainer.sampleCode", sampleCodes).in("sampleOnInputContainer.projectCode", projectCodes))
		.cursor
		.forEach(p -> {
			if(!p.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && p.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(p.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				p.traceInformation.setTraceInformation(validation.getUser());
				p.sampleOnInputContainer.lastUpdateDate = new Date();
				((List<String>)validation.getObject("RecapProcess")).add(codeUpdated+","+p.code+","+newPropertyCode+","+newPropertyValue);
				p.sampleOnInputContainer.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				//MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", p.code), 
						DBUpdate.set("sampleOnInputContainer", p.sampleOnInputContainer).set("traceInformation", p.traceInformation));
			}
		});

		//update readsets with new exp property values
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", containerCodes).in("sampleCode", sampleCodes).in("projectCode", projectCodes))
		.cursor
		.forEach(readset -> {
			if(!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				readset.traceInformation.setTraceInformation(validation.getUser());
				readset.sampleOnContainer.lastUpdateDate = new Date();
				((List<String>)validation.getObject("RecapReadSet")).add(codeUpdated+","+readset.code+","+newPropertyCode+","+newPropertyValue);
				readset.sampleOnContainer.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), 
						DBUpdate.set("sampleOnContainer", readset.sampleOnContainer).set("traceInformation", readset.traceInformation));
			}
		});	
	}
}
