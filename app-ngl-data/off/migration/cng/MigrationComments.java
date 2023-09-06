package controllers.migration.cng;


import java.util.List;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.sample.instance.Sample;
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
 * Update contents in the Container (add missing contents, update properties)
 * This migration replaces MigrationTag (scope larger)
 * 
 * @author dnoisett
 * 04/04/2014
 */
@Repository
public class MigrationComments extends CommonController {
	
	private static final String SAMPLE_COLL_NAME_BCK = InstanceConstants.SAMPLE_COLL_NAME + "_MC_20141121_1";
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);

	
	
	public static Result migration() {
		
		int n=0;
		
		JacksonDBCollection<Sample, String> samplesCollBck = MongoDBDAO.getCollection(SAMPLE_COLL_NAME_BCK, Sample.class);
		if (samplesCollBck.count() == 0) {
	
			backUpSamples();
			
			Logger.info("Migration container starts");
		
			//find collection up to date
			ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
			List<Sample> newSamples = null;
			try {
				newSamples = limsServices.findAllSample(contextError);
			} catch (DAOException e) {
				Logger.debug("ERROR in findAllSamples():" + e.getMessage());
			}
			
			//find current collection
			List<Sample> oldSamples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList();

			//delete all contents
			for (Sample oldSample : oldSamples) {
				WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", oldSample.code),   
						DBUpdate.unset("comments"));
					
					
			}
			
			Logger.info("Remove old comments OK");

			//iteration over current collection
			for (Sample oldSample : oldSamples) {
				
				for (Sample newSample : newSamples) {
					
					if (oldSample.code.equals(newSample.code)) {	
						//oldSample.comments = newSample.comments;
					 
						WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", oldSample.code),   
								DBUpdate.set("comments", newSample.comments));
							
						
						
						n++;
						break;
					}
				}
				
			}	//end for containers
						
		} else {
			Logger.info("Migration containers already executed !");
		}
		
		Logger.info("Migration container (tag) Finish : " + n + " contents of containers updated !");
		return ok("Migration container (tag) Finish");
	}

	private static void backUpSamples() {
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" starts");
		MongoDBDAO.save(SAMPLE_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList());
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" ended");
	}
	

}
