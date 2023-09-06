package controllers.migration.cng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import models.LimsCNGDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.WriteResult;

import controllers.CommonController;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import fr.cea.ig.MongoDBDAO;

/**
 * Update projectCodes from readSets
 * @author dnoisett
 * 03-12-2014
 * Refactoring 10-12-2014
 */
public class MigrationProjectCodesFromReadSets  extends CommonController {
	
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private static String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_PC_"+sdf.format(new java.util.Date());		
	private static String backupName2 = InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+"_BCK_PC_"+sdf.format(new java.util.Date());
	
	
	
	public static Result migration() {		
		int[] intResultsArray = new int[2]; 
		
		backUpCollections();
		
		Logger.info("Migration contents of containers starts : add projectCode attribute");
		
		intResultsArray = migrateContainer("lane");		
		
		Logger.info("Migration contents of containers Finish : " + intResultsArray[0] + " contents and projectCodes of containers updated !");
		Logger.info("Migration contents of container supports Finish : " + intResultsArray[1] + " projectCodes of container supports updated !");
			
		return ok("End");
	}
	
	
	
	private static void backUpCollections() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" end");	
		
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+" to "+backupName2+" start");		
		MongoDBDAO.save(backupName2, MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+" to "+backupName2+" end");	
	}
	
	
	
	private static int[] migrateContainer(String type) {		
		int[] intResultsArray = new int[] {0,0};
		String errorMsg = "", oldErrorMsg = "", errorMsg2 = "", oldErrorMsg2 = "";
		boolean bFindReadSet, bError4_4;

			
		//find container supports
		List<ContainerSupport> oldSupportContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, 
				DBQuery.regex("categoryCode", Pattern.compile("flowcell"))).toList();

	
		for (ContainerSupport oldSupportContainer : oldSupportContainers) {
			
			bFindReadSet = false;
			bError4_4 = false;
			HashMap<String, String> hmSamplesAndProjectsInReadSets = new HashMap<String, String>();
			HashMap<String, HashSet<String>> hmLaneNumbersAndSamplesInReadSets = new HashMap<String, HashSet<String>>();
			
			//find run for this container support
			List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,  
					DBQuery.and(DBQuery.is("containerSupportCode", oldSupportContainer.code), DBQuery.notEquals("state.code", "FE-S"))).toList();
			if (runs == null || runs.size() == 0) {
				errorMsg = "ERROR 1 : No run found for container support " + oldSupportContainer.code;
				//I don't log this message to not polluate the others.
				//Logger.error(errorMsg);
			}
			else if (runs.size() > 1) {
				errorMsg = "ERROR 2 : Multiple runs found container support " + oldSupportContainer.code;
				if (!errorMsg.equals(oldErrorMsg)) {
					Logger.error(errorMsg);
				}
			}
			else {
				//find readSets associated with this run
				List<ReadSet> rds = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", runs.get(0).code)).toList();
				if (rds == null || rds.size() == 0) {
					errorMsg = "ERROR 3 : No readSet found for run " + runs.get(0).code + " (run.state.code = " + runs.get(0).state.code + ")";
					if (!errorMsg.equals(oldErrorMsg)) {
						Logger.error(errorMsg);
					}
				}
				else {
					bFindReadSet = true;
					HashSet<String> tmpSampleCodes = new HashSet<String>();
					
					//find sampleCodes and projectCodes
					for (ReadSet rd : rds) {
						hmSamplesAndProjectsInReadSets.put(rd.sampleCode, rd.projectCode); 
						
						tmpSampleCodes.add(rd.sampleCode); 
						
						hmLaneNumbersAndSamplesInReadSets.put(rd.laneNumber.toString(), tmpSampleCodes);
					}
					
					//new : check number of readsets
					if (oldSupportContainer.categoryCode.equals("flowcell-1") &&  hmLaneNumbersAndSamplesInReadSets.size() != 1) {
						errorMsg = "ERROR 4.1 : Nb lanes found : "+ hmLaneNumbersAndSamplesInReadSets.size() + ", expected: 1 (container support: "+ oldSupportContainer.code + ", type: " + oldSupportContainer.categoryCode + ")";
					}
					else if (oldSupportContainer.categoryCode.equals("flowcell-2") &&  hmLaneNumbersAndSamplesInReadSets.size() != 2) {
						errorMsg = "ERROR 4.2 : Nb lanes found : "+ hmLaneNumbersAndSamplesInReadSets.size() + ", expected: 2 (container support: "+ oldSupportContainer.code + ", type: " + oldSupportContainer.categoryCode + ")";
					}
					else if (oldSupportContainer.categoryCode.equals("flowcell-4") &&  hmLaneNumbersAndSamplesInReadSets.size() != 4) {
						errorMsg = "ERROR 4.3 : Nb lanes found : "+ hmLaneNumbersAndSamplesInReadSets.size() + ", expected: 4 (container support: "+ oldSupportContainer.code + ", type: " + oldSupportContainer.categoryCode + ")";
					}
					else if (oldSupportContainer.categoryCode.equals("flowcell-8") &&  hmLaneNumbersAndSamplesInReadSets.size() != 8) {
						bError4_4 = true;
						errorMsg = "ERROR 4.4 : Nb lanes found : "+ hmLaneNumbersAndSamplesInReadSets.size() + ", expected: 8 (container support: "+ oldSupportContainer.code + ", type: " + oldSupportContainer.categoryCode + ")";
					}
					if (!errorMsg.equals(oldErrorMsg)) {
						Logger.error(errorMsg);
					}
					
					
					
				}
			}
			
			
			if (bFindReadSet) {
				
				//update container support
				List<String> listSampleCodesInSupport = new ArrayList<String>(hmSamplesAndProjectsInReadSets.keySet()); 			
				HashSet<String> listProjectCodesInSupport = new HashSet<String>(hmSamplesAndProjectsInReadSets.values()); 
				
				WriteResult<ContainerSupport, String> r;
				if (!bError4_4) {
					r = (WriteResult<ContainerSupport, String>) MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, 
						DBQuery.is("code", oldSupportContainer.code),   
						DBUpdate.set("sampleCodes", listSampleCodesInSupport).set("projectCodes", listProjectCodesInSupport));
				}
				else {
					//bug identified, we update the categoryCode too
					r = (WriteResult<ContainerSupport, String>) MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, 
							DBQuery.is("code", oldSupportContainer.code),   
							DBUpdate.set("sampleCodes", listSampleCodesInSupport).set("projectCodes", listProjectCodesInSupport).set("categoryCode", "flowcell-2"));
				}
				
				intResultsArray[0]++; 
				
				
				//find containers associated with this container support
				List<Container> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", oldSupportContainer.code)).toList();
				
				ArrayList<String> sampleCodesInContainers = new ArrayList<String>();				
				ArrayList<String> projectCodesInContainers = new ArrayList<String>();
				
				//iterate over the containers
				for (Container oldContainer : oldContainers) {
					HashMap<String, String> hmSampleAndProjectInContainer = new HashMap<String, String>();
					
					for (Content content : oldContainer.contents) {
						
						if (hmLaneNumbersAndSamplesInReadSets.containsKey(oldContainer.support.line)) {
							if (hmSamplesAndProjectsInReadSets.containsKey(content.sampleCode)) {
								content.projectCode = hmSamplesAndProjectsInReadSets.get(content.sampleCode);
								hmSampleAndProjectInContainer.put(content.sampleCode, content.projectCode); 
							}
							else {
								//error missing sample code in container support
								errorMsg2 = "ERROR 6 : Missing sample code " + content.sampleCode + " in container support " + oldSupportContainer.code + " OR wrong sample code in a content of container " + oldContainer.code; 
								if (!errorMsg2.equals(oldErrorMsg2)) {
									Logger.error(errorMsg2);
								}
								oldErrorMsg2 = errorMsg2; 
							}
						}
						else {
							//error
							errorMsg2 ="ERROR 7 : No lane with number " + oldContainer.support.line + " found in the readSets collection";
							if (!errorMsg2.equals(oldErrorMsg2)) {
								Logger.error(errorMsg2);
							}
							oldErrorMsg2 = errorMsg2; 
						} 
						
						//for further control *
						if (!sampleCodesInContainers.contains(content.sampleCode)) {
							sampleCodesInContainers.add(content.sampleCode);
						}
						if (!projectCodesInContainers.contains(content.projectCode)) {
							projectCodesInContainers.add(content.projectCode);
						}

					}
					
					//update each container
					List<String> listSampleCodesInContainer = new ArrayList<String>(hmSampleAndProjectInContainer.keySet()); 
					HashSet<String> listProjectCodesInContainer = new HashSet<String>(hmSampleAndProjectInContainer.values());
					

					WriteResult<Container, String> r2 = (WriteResult<Container, String>) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.is("code", oldContainer.code),   
							DBUpdate.set("contents", oldContainer.contents).set("sampleCodes", listSampleCodesInContainer).set("projectCodes", listProjectCodesInContainer));					
					intResultsArray[1]++; 
				
				}
				
				//* 
				// control sampleCodes in the support
				boolean bFind = false;
				for (String sampleCode : listSampleCodesInSupport) {
					for (String sampleCode2 : sampleCodesInContainers) {
						if (sampleCode.equals(sampleCode2)) {
							bFind = true;
							break;
						}
					}
					if (!bFind) {
						errorMsg = "ERROR 9 : Sample code " + sampleCode + " in support " + oldSupportContainer.code + " not in all his containers";
						if (!errorMsg.equals(oldErrorMsg)) {
							Logger.error(errorMsg);
						}
					}
				}

					
				
				//control projectCodes in the support
				bFind = false;
				for (String projectCode : listProjectCodesInSupport) {
					for (String projectCode2 : projectCodesInContainers) {
						if (projectCode.equals(projectCode2)) {
							bFind = true;
							break;
						}
					}
					if (!bFind) {
						errorMsg = "ERROR 10 : Project code " + projectCode + " in support " + oldSupportContainer.code + " not in all his containers";
						if (!errorMsg.equals(oldErrorMsg)) {
							Logger.error(errorMsg);
						}
					}
				}

				
				//reverse control for sampleCodes
				bFind = false;
				for (String sCode : sampleCodesInContainers) {
					for (String sCode2 : listSampleCodesInSupport) {
						if (sCode.equals(sCode2)) {
							bFind = true;
							break;
						}
					}
					if (!bFind) {
						errorMsg = "ERROR 11 : Sample code " + sCode + " in a container  not in the list of sample codes of his support (" + oldSupportContainer + ")";
						if (!errorMsg.equals(oldErrorMsg)) {
							Logger.error(errorMsg);
						}
					}
				}
				
				//reverse control for projectCodes
				bFind = false;
				for (String pCode : projectCodesInContainers) {
					for (String pCode2 : projectCodesInContainers) {
						if (pCode.equals(pCode2)) {
							bFind = true;
							break;
						}
					}
					if (!bFind) {
						errorMsg = "ERROR 12 : Project code " + pCode + " in a container not in the list of project codes of his support (" + oldSupportContainer + ")";
						if (!errorMsg.equals(oldErrorMsg)) {
							Logger.error(errorMsg);
						}
					}
				}
					

			} // end of bFindReadSet test
			
			//just for not repeated the same error msg
			oldErrorMsg = errorMsg;

		} //end of iteration over the collection of container supports
		
		return intResultsArray;	
	}

}
				
