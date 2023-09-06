package controllers.migration.cng;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.WriteResult;
import org.springframework.stereotype.Repository;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update contents and projectCodes in the Container 
 * (add missing attribute projectCode in the content and set list of projectCodes for the container with the projectCode of each content)
 * Update list of projectCodes in the Container Support
 *
 * 
 * @author dnoisett
 * 26/11/2014
 */

@Repository
@Deprecated
public class MigrationProjectCodes extends CommonController {
		
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private static String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_PC_"+sdf.format(new java.util.Date());		
	private static String backupName2 = InstanceConstants.CONTAINER_SUPPORT_COLL_NAME+"_BCK_PC_"+sdf.format(new java.util.Date());	

	
	
	public static Result migration() {		
		int[] intResultsArray = new int[2]; 
		int[] intResultsArray2 = new int[2];
		int nb;
		
		backUpCollections();
		
		Logger.info("Migration contents of containers starts : add projectCode attribute");
		
		intResultsArray = migrateContainer("lane","prepa-flowcell-cng");		
		intResultsArray2 = migrateContainer("tube","lib-normalization");
		
		nb = intResultsArray[0] + intResultsArray2[0];
		Logger.info("Migration contents of containers Finish : " + nb + " contents and projectCodes of containers updated !");
		
		nb = intResultsArray[1] + intResultsArray2[1];
		Logger.info("Migration projectCodes of support of containers Finish : " + nb + " container supports updated !");
		
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
	
	
	
	private static int[] migrateContainer(String categoryCode,String experimentTypeCode) {
		int[] intResultsArray = new int[] {0,0};
		
		//find collection up to date
		ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
		List<Container> newContainers = null;
		try {
			newContainers = limsServices.findAllContainer(contextError, categoryCode,experimentTypeCode);
		} catch (DAOException e) {
			Logger.debug("ERROR in findAllContainer():" + e.getMessage());
		}
		
		//find current collection
		List<Container> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
		
		HashMap<String, Set<String>> hm = new HashMap<String, Set<String>>();

		for (Container oldContainer : oldContainers) {

			for (Container newContainer : newContainers) {
				
				if (oldContainer.code.equals(newContainer.code)) {	
				 
					//set contents to the new ones
					WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", oldContainer.code),   
							DBUpdate.set("contents", newContainer.contents).set("projectCodes", newContainer.projectCodes));
					
					intResultsArray[0] = intResultsArray[0] + 1; 
					
					break;
				}
			}
			
			//update hashMap
			if (!hm.containsKey(oldContainer.support.code)) {
				hm.put(oldContainer.support.code, oldContainer.projectCodes);
			}
			else {
				Set<String> updatedProjectCodes = hm.get(oldContainer.support.code);
				updatedProjectCodes.addAll(oldContainer.projectCodes);
				hm.put(oldContainer.support.code, updatedProjectCodes); 				
			}
			
		}
		
		//iteration over the hashMap
		for (Map.Entry<String, Set<String>> entry : hm.entrySet()) {
			String supportCode = entry.getKey();
			Set<String> projectCodes = entry.getValue();
			
			WriteResult r2 = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", supportCode),   
					DBUpdate.set("projectCodes", projectCodes));
			
			intResultsArray[1] = intResultsArray[1] + 1; 			
		}
		
		return intResultsArray;

	}
	
	

}