package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import play.Logger;
import controllers.CommonController;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

public class AbstractMigration extends CommonController {

	protected static final String today =( new SimpleDateFormat("ddMMyy'_'hhmm")).format(new Date());

	
	protected static <T extends DBObject> void backupOneCollection(String collectionName,Class<T> classType) {
		Logger.info("\tCopie "+collectionName+" start");
		MongoDBDAO.save(collectionName+"_BCK_"+today,MongoDBDAO.find(collectionName, classType).toList());
		Logger.info("\tCopie "+collectionName+" end");
		
		
	}
	
	
	protected static <T extends DBObject> void backupOneCollection(String collectionName,Class<T> classType,BasicDBObject keys) {
		Logger.info("\tCopie "+collectionName+" start");
		MongoDBDAO.save(collectionName+"_BCK_"+today,MongoDBDAO.find(collectionName, classType,DBQuery.exists("code"),keys).toList());
		Logger.info("\tCopie "+collectionName+" end");
		
		
	}

}
