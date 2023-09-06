package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateSoftwareMapping extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration()
	{

		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.is("treatments.mapping.code","mapping").notExists("treatments.mapping.pairs.softwareVersion"),keys).toList();
		
		//Attention backup manuel car trop de donnees

		Logger.debug("Nb readSets to update "+readSets.size());
		int nb=0;
		for(ReadSet readSet : readSets){
			Logger.debug("ReadSet "+nb);
			readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			Treatment treatmentMapping = readSet.treatments.get("mapping");
			treatmentMapping.results.get("pairs").put("softwareVersion", new PropertySingleValue("bwa_aln"));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),  DBUpdate.set("treatments.mapping", treatmentMapping));
			nb++;
		}
		
		return ok("Migration finished");

	}

	
}
