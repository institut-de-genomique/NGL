package controllers.admin.supports.api;		

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.mvc.Result;

/**
 * Update SampleOnContainer on ReadSet
 * 
 * @author galbini
 *
 */
public class MigrationUpdateSampleOnContainer extends DocumentController<ReadSet> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(MigrationUpdateSampleOnContainer.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	//	@Inject
//	public MigrationUpdateSampleOnContainer(NGLContext ctx) {
//		super(ctx, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
//	}
	
	@Inject
	public MigrationUpdateSampleOnContainer(NGLApplication app) {
		super(app, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
	}
	
	public Result migration(String code, Boolean onlyNull) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		logger.info("Migration sample on container start");
		try {
			backupReadSet(code, onlyNull);
		} catch (Exception e) {
			// e.printStackTrace();
			return forbidden(e.getMessage());
		}
		List<ReadSet> readSets = null;
		if (!"all".equals(code)) {
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code",code), keys).toList();						
		} else if (onlyNull.booleanValue()) {
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notExists("sampleOnContainer"), keys).toList();						
		} else {
			String message = "code: " + code + " is not authorized"; 
			logger.error(message);
			//readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("code"), keys).toList();
			return internalServerError("migration failed : " + message);
		}
		logger.debug("migre " + readSets.size() + " readSets");
//		if (readSets != null) {
			for(ReadSet readSet : readSets){
				migreReadSet(readSet);				
			}
//		}
		logger.info("Migration sample on container finish");
		return ok("Migration Finish");
	}

	private /*static*/ void migreReadSet(ReadSet readSet) {
		SampleOnContainer sampleOnContainer = InstanceHelpers.getSampleOnContainer(readSet);
		if (sampleOnContainer != null) {
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, 
					DBQuery.is("code", readSet.code), DBUpdate.set("sampleOnContainer", sampleOnContainer));
		} else{
			logger.error("sampleOnContainer null for "+readSet.code);
		}
	}
	
	private /*static*/ void backupReadSet(String code, Boolean onlyNull) throws Exception {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SOC_"+sdf.format(new java.util.Date());
		logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		List<ReadSet> readSets = null;
		if (!"all".equals(code)) {
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code",code), keys).toList();						
		} else if (onlyNull.booleanValue()) {
			readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notExists("sampleOnContainer"), keys).toList();
		} else {
			// DO NOT BACKUP the entire collection!
			// readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("code"), keys).toList();
			//Logger.error("code: " + code + " is not authorized");
			logger.warn("backup of complete " + InstanceConstants.READSET_ILLUMINA_COLL_NAME + " is forbidden!");
			throw new Exception("backup of complete " + InstanceConstants.READSET_ILLUMINA_COLL_NAME + " is forbidden!");
		}
		if (readSets != null) {
			MongoDBDAO.save(backupName, readSets);
			logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
		}
	}

}
