package controllers.migration.cng;

import java.text.SimpleDateFormat;
import java.util.List;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import org.springframework.stereotype.Repository;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update concentration value and unit for tube
 * Replace old migration "UpdateConcentrationUnit"
 * @author dnoisett
 * 21/11/2014
 */

@Repository
public class UpdateConcentration extends CommonController {
		
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	private static String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_C_"+sdf.format(new java.util.Date());	

	
	
	public static Result migration() {
		
		int n=0;
		
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(backupName, Container.class);
		if (containersCollBck.count() == 0) {
	
			backUpContainer();
			
			Logger.info("Migration unit of concentration starts");
		
			//find collection up to date
			ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
			List<Container> newContainers = null;
			try {
				newContainers = limsServices.findAllContainer(contextError, "tube","lib-normalization");
			} catch (DAOException e) {
				Logger.debug("ERROR in findAllContainer():" + e.getMessage());
			}
			
			//find current collection
			List<Container> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("categoryCode", "tube")).toList();

			for (Container oldContainer : oldContainers) {
				for (Container newContainer : newContainers) {
					if (newContainer.code.equals(oldContainer.code)) {

						//update concentration unit and value
						WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", oldContainer.code),   
								DBUpdate.set("concentration.unit","nM").set("concentration.value", newContainer.concentration.value));
						
						n++;
						
						break;
					}
				}
				
			}
			
			//delete concentration < 2nM
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.lessThan("concentration.value", 2));
						
		} else {
			Logger.info("Migration of concentration already executed !");
		}
		
		Logger.info("Migration of concentration Finish : " + n + " containers updated !");
		return ok("End");
	}

	private static void backUpContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" start");		
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" end");	
	}
	
}