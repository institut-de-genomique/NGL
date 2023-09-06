package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateSortRibo extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration()
	{

		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		//keys.put("treatments.taxonomy", 1);
		
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.is("treatments.sortingRibo.code","sortingRibo").notExists("treatments.sortingRibo.default.software"),keys).toList();
		
		//Attention backup manuel car trop de donnees
		//backUpReadSet(readSets);

		Logger.debug("Nb readSets to update "+readSets.size());
		int nb=0;
		for(ReadSet readSet : readSets){
			Logger.debug("ReadSet "+nb);
			readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			Treatment treatmentSortRibo = readSet.treatments.get("sortingRibo");
			if(!treatmentSortRibo.results.containsKey("default")){
				treatmentSortRibo.results.put("default",  new HashMap<String, PropertyValue>());
			}
			treatmentSortRibo.results.get("default").put("software", new PropertySingleValue("sortmerna1.0"));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),  DBUpdate.set("treatments.sortingRibo", treatmentSortRibo));
			nb++;
		}
		
		return ok("Migration finished");

	}

	private static void backUpReadSet(List<ReadSet> readSets)
	{
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_NGL962_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");

		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");

	}


}
