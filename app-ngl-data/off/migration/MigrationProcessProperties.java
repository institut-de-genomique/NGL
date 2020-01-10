package controllers.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;

public class MigrationProcessProperties extends CommonController {
	
	private static final String PROCESS_COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME+"201702XX_BCK";
	private static final String TAG_PROPERTY_NAME = "tag";
	
	static ALogger logger=Logger.of("MigrationProcessProperties");

	public static Result migration(){

		Logger.info("Start point of Migration Process");

		JacksonDBCollection<Process, String> containersCollBck = MongoDBDAO.getCollection(PROCESS_COLL_NAME_BCK, Process.class);
		if(containersCollBck.count() == 0){
		
			Logger.info("Migration Process start");
			
			//backupContainerCollection();
			
			MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("state.code", Arrays.asList("IP","N"))
					/*.is("code", "BYU_AABL_TAG-PCR-AND-DNA-LIBRARY_22385HMN7")*/)
					.sort("code",Sort.DESC).getCursor().forEach(p -> migrationProcess(p));
			
			Logger.info("Migration process end");

		}else{
			Logger.info("Migration Process already execute !");
		}
		Logger.info("Migration Process finish");
		return ok("Migration Process Finish");
	}

	

	private static void migrationProcess(Process process) {
		//1 Get Tag
		String tag = getTagAssignFromProcessContainers(process);
		process.outputContainerCodes = new TreeSet<String>();
		
		List<String> containerSupportCodes = new ArrayList<String>();
		containerSupportCodes.add(process.inputContainerSupportCode);
		if(null != process.outputContainerSupportCodes){
			containerSupportCodes.addAll(process.outputContainerSupportCodes);
		}
		
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.in("support.code", containerSupportCodes).elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes).in("projectCode", process.projectCodes)))
		.sort("code",Sort.ASC).cursor.forEach(container -> {
			
			//Logger.error("cont code "+container.code);
			
			List<Content> contentFound = container.contents.stream()
				.filter(content -> contentFiltering(content, process, tag))
				.collect(Collectors.toList());
			
			if(contentFound.size() == 1){
				Integer nbContentChange = contentFound.stream()
						.filter(content -> contentFiltering(content, process, tag))
						.mapToInt(content -> {
							if(!"UA".equals(container.state.code) 
									&& !"IS".equals(container.state.code)
									&& !"IW-P".equals(container.state.code)
									&& !"F".equals(container.state.code)){
								int state = 0;
								if(process.properties != null && process.properties.size() > 0){
									content.processProperties = process.properties;	
									state = 1;
								}
								if(process.comments != null && process.comments.size() > 0){
									content.processComments = process.comments;
									state = 1;
								}
								
								return state;	
							}	
							return 0;
						}).sum();
					/*
					if(!container.support.code.equals(process.inputContainerSupportCode)){
						
						
						if(container.treeOfLife == null){
							process.outputContainerCodes.add(container.code);
						}else if(container.treeOfLife.paths.stream().anyMatch(path -> path.contains(process.inputContainerCode))){
							process.outputContainerCodes.add(container.code);
						}	
						
						
											
					}
					*/
					if(nbContentChange > 0){
						Logger.debug("update container "+container.code+" "+container.state.code);
						container.traceInformation.setTraceInformation("ngl");
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);	
					}
			}else if(contentFound.size() > 1){
				Logger.error("found several contents "+container.code);
			}else if(contentFound.size() == 0){
				//Logger.error("found 0 contents "+container.code);
			}
			
			
		});
		/*
		if((process.outputContainerCodes != null && process.outputContainerSupportCodes != null) 
				&& (process.outputContainerCodes.size() == process.outputContainerSupportCodes.size() 
				|| (process.outputContainerCodes.size() > process.outputContainerSupportCodes.size()  
						&& ("illumina-run".equals(process.typeCode) || "bionano-chip-process".equals(process.typeCode) 
								|| "dna-illumina-indexed-library-process".equals(process.typeCode)
								|| "bionano-nlrs-process".equals(process.typeCode) || "norm-fc-depot-illumina".equals(process.typeCode) 
								|| "prepfc-depot".equals(process.typeCode) || "prepfcordered-depot".equals(process.typeCode)
								|| "x5-wg-pcr-free".equals(process.typeCode))))){
			
			process.traceInformation.setTraceInformation("ngl");
			MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME,process);
		}else if(process.outputContainerCodes != null && process.outputContainerSupportCodes != null){
			Logger.debug("process size cont !="+process.code+" / "+process.state.code+" "+process.outputContainerCodes.size()+" != "+process.outputContainerSupportCodes.size()+" : "+process.outputContainerCodes);
		}
		*/
	}



	private static boolean contentFiltering(Content content, Process process, String tag) {
		return (process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && !content.properties.containsKey(TAG_PROPERTY_NAME))
			|| (null != tag && process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.containsKey(TAG_PROPERTY_NAME) 
					&&  tag.equals(content.properties.get(TAG_PROPERTY_NAME).value));
	}


	private static String getTagAssignFromProcessContainers(Process process) {
		
		if(process.sampleOnInputContainer.properties.containsKey(TAG_PROPERTY_NAME)){
			return process.sampleOnInputContainer.properties.get(TAG_PROPERTY_NAME).value.toString().trim();
		}else if(process.outputContainerSupportCodes != null){
			
			DBQuery.Query query = DBQuery.in("support.code",process.outputContainerSupportCodes)
						.size("contents", 1)
						.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
													.in("projectCode",  process.projectCodes)
													.exists("properties.tag"));
			
			Set<String> tags = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query)
					.toList()
					.stream()
					.filter(c -> filterWithPaths(c, process))
					.map(c -> c.contents)
					.flatMap(List::stream)
					.map(c -> c.properties.get(TAG_PROPERTY_NAME).value.toString())
					.collect(Collectors.toSet());
			
			
			if(tags.size() == 1){
				return tags.iterator().next().trim();
			}else if(tags.size() > 1){
				Logger.warn("Found lot of tags for process "+process.code);
				return null;
			} else{
				return null;
			}
		}else{
			return null;
		}
	}
	private static boolean filterWithPaths(Container c, Process process) {
		if(c.treeOfLife == null){
			return true;
		}else{
			return c.treeOfLife.paths.stream().anyMatch(path -> path.contains(process.inputContainerCode));			
		}		
	}



	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" start");
		MongoDBDAO.save(PROCESS_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class).toList());
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" end");
	}

}
