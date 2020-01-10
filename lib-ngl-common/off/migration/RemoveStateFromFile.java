package controllers.migration;

import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.mvc.Result;
import controllers.CommonController;

import fr.cea.ig.MongoDBDAO;

public class RemoveStateFromFile  extends CommonController {

	private static final String READSET_ILLUMINA_BCK = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK";

	public static Result migration(){
		
		JacksonDBCollection<ReadSet, String> readSetsCollBck = MongoDBDAO.getCollection(READSET_ILLUMINA_BCK, ReadSet.class);
		if(readSetsCollBck.count() == 0){
			Logger.info("Migration readset start");
			
			// joker "$" don't work, so replace it with 1 to 8 !
			 MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("files"), 
						DBUpdate.unset("files.1.state").unset("files.2.state").unset("files.3.state").unset("files.4.state")
						.unset("files.5.state").unset("files.6.state").unset("files.7.state").unset("files.8.state"));
				 			
		}else{
			Logger.info("Migration readset already execute !");
		}
			
		Logger.info("Migration finish");
		return ok("Migration Finish");
	}
	
}
