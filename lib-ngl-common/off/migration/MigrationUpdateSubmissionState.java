package controllers.migration;		

import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import play.mvc.Result;

/**
 * Update SampleOnContainer on ReadSet
 * @author galbini
 *
 */
public class MigrationUpdateSubmissionState extends CommonController {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	public static Result migration(){
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		Logger.info("Migration submission state start");
		backupReadSet();
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notExists("submissionState"), keys).toList();
		
		Logger.debug("migre "+readSets.size()+" readSets");
		int nb=0;
		int size= readSets.size();
		for(ReadSet readSet : readSets){
			Logger.info(nb+"/"+size+"="+readSet.code);
			State state = new State("NONE","ngl-sub");
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
					DBQuery.is("code", readSet.code), DBUpdate.set("submissionState", state));
			nb++;
		}
		Logger.info("Migration submission state finish");
		return ok("Migration Finish");

	}
	
	private static void backupReadSet() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SOC_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("code"), keys).toList();						
		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
		
	}

	

}
