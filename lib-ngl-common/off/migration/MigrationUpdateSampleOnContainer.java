package controllers.migration;		

import java.text.SimpleDateFormat;
import java.util.List;

import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update SampleOnContainer on ReadSet
 * @author galbini
 *
 */
public class MigrationUpdateSampleOnContainer extends CommonController {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	public static Result migration(String code, Boolean onlyNull){
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		Logger.info("Migration sample on container start");
		backupReadSet(code);
		List<ReadSet> readSets = null;
		if(!"all".equals(code)){
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code",code), keys).toList();						
		}else if(onlyNull.booleanValue()){
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notExists("sampleOnContainer"), keys).toList();						
		}else {
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("code"), keys).toList();
		}
		Logger.debug("migre "+readSets.size()+" readSets");
		for(ReadSet readSet : readSets){
			migreReadSet(readSet);				
		}
		Logger.info("Migration sample on container finish");
		return ok("Migration Finish");

	}

	

	private static void migreReadSet(ReadSet readSet) {
		SampleOnContainer sampleOnContainer = InstanceHelpers.getSampleOnContainer(readSet);
		if(null != sampleOnContainer){
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
					DBQuery.is("code", readSet.code), DBUpdate.set("sampleOnContainer", sampleOnContainer));
		}else{
			Logger.error("sampleOnContainer null for "+readSet.code);
		}
	}
	
	private static void backupReadSet(String code) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SOC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		List<ReadSet> readSets = null;
		if(!"all".equals(code)){
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code",code), keys).toList();						
		}else{
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("code"), keys).toList();						
		}
		
		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
		
	}

	

}
