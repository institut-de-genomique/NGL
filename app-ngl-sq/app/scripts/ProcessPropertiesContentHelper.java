package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
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
		Set<String> tags = new HashSet<String>();
		//New algo NGL-2816
		//1. input container tag I + tag II STOP search
		if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && 
				process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).getValue().toString()+"_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).getValue().toString());
			//2.input container tag I + no tag II STOP search 
		}else if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && 
				!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			tags.add("_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).getValue().toString());
		}else{
			//search in container treeOfLife 
			//3. Search container  one content only tagI => STOP search
			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
					.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
							.in("projectCode",  process.projectCodes)
							.exists("properties.tag").notExists("properties.secondaryTag"));
			List<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
			if(containersWithTag.size() > 0){
				//STOP SEARCH
				//Get first container
				Container containerWithTag = containersWithTag.iterator().next();
				tags.add("_"+containerWithTag.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}else {
				//4.a Recherche tag II
				//4.a.1 Content courant tagII
				String secondaryTag = null;
				if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) &&
						!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) {
					secondaryTag=process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
					tags.add(secondaryTag+"_");
				}else {
					//4.a.2 Container enfant un seul content tagII
					query = DBQuery.in("code",process.outputContainerCodes)
							.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
							.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
									.in("projectCode",  process.projectCodes)
									.notExists("properties.tag").exists("properties.secondaryTag"));
					List<Container> containersWithSecondTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
					if(containersWithSecondTag.size()>0) {
						Container containerWithSecondTag = containersWithSecondTag.iterator().next();
						secondaryTag=containerWithSecondTag.contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
						tags.add(secondaryTag+"_");
					}
				}
				//4.b Recherche container enfant plusieurs content avec tagI identique et tagII du 4.a pour avoir combinaison tagII_tagI
				if(secondaryTag!=null) {
					query = DBQuery.in("code",process.outputContainerCodes)
							.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
									.in("projectCode",  process.projectCodes)
									.is("properties.secondaryTag.value", secondaryTag)
									.exists("properties.tag"));
					List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
					if(containersWithDoubleTag.size() > 0){
						//Check same all tagI 
						for(Container container : containersWithDoubleTag) {
							if(checkSameTagInContainer(container)) {
								for(Content content : container.contents){
									if(process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString().equals(secondaryTag)){
										tags.add(secondaryTag+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
									}
								}
								break;
							}
						}
						
					}
				}
			}
		}
		return tags;
	}

	@SuppressWarnings("unchecked")
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


	@SuppressWarnings("unchecked")
	public static void updateProcessContentPropertiesWithCascade(Set<String> containerCodes, Set<String> sampleCodes, Set<String> projectCodes, Set<String> tags,String newPropertyCode, String newPropertyValue, String codeUpdated, ContextValidation validation) {
		
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				        DBQuery.in("code", containerCodes))
			      .cursor
			      .forEach(container -> {
			    	  container.traceInformation.setTraceInformation(validation.getUser());
			    	  container.contents.stream()
			    	    .filter(content -> (
			    	        sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) && (
			    	        (!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
			    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))	
			    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
			    	        || (null != tags && !content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
			    	        ))).forEach(content -> {
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
		.forEach(process -> {
			if((!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
				||(null!=tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
				||(null!=tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
				||(null!=tags && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
			){
				process.traceInformation.setTraceInformation(validation.getUser());
				((List<String>)validation.getObject("RecapProcess")).add(codeUpdated+","+process.code+","+newPropertyCode+","+newPropertyValue);
				process.sampleOnInputContainer.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code), 
						DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer).set("traceInformation", process.traceInformation));
			}
		});
		
		//update readsets with new exp property values
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", containerCodes).in("sampleCode", sampleCodes).in("projectCode", projectCodes))
		.cursor
		.forEach(readset -> {
			if((!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
					||(null!=tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
				){
				readset.traceInformation.setTraceInformation(validation.getUser());
				readset.sampleOnContainer.lastUpdateDate = new Date();
				((List<String>)validation.getObject("RecapReadSet")).add(codeUpdated+","+readset.code+","+newPropertyCode+","+newPropertyValue);
				readset.sampleOnContainer.properties.put(newPropertyCode, new PropertySingleValue(newPropertyValue));
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), 
						DBUpdate.set("sampleOnContainer", readset.sampleOnContainer).set("traceInformation", readset.traceInformation));
				if(newPropertyCode.equals("libProcessTypeCode")) {
					//Call rules to refresh libProcessTypeCodes Run
					//Get run to update
					Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, readset.runCode);
					//Call upate libProcessTypeCode from containerSupportCode
					//Code from rules Copy Properties from Support and Container to Run
					List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();

					Set<String> libProcessTypeCodes = new TreeSet<String>();
					for(Container container:containers){
						for(Content content:container.contents){
							if(content.properties.containsKey("libProcessTypeCode")){
								libProcessTypeCodes.add((String)(content.properties.get("libProcessTypeCode").value));
							}
						}
					}
					if(libProcessTypeCodes.size() > 0){
						run.properties.put("libProcessTypeCodes", new PropertyListValue(new ArrayList<>(libProcessTypeCodes)));
					}

					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
							DBQuery.is("code", run.code),
							DBUpdate.set("properties", run.properties));

				}
			}
		});	
	}
	
	public static boolean checkSameTagInContainer(Container container)
	{
		Set<String> tags = container.contents.stream()
		.filter(content->content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
		.map(content->content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString())
		.collect(Collectors.toSet());
		
		if(tags.size()==1)
			return true;
		else
			return false;
	}
}
