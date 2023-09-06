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

public class MigrationUpdateSoftwareTaxonomy extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration()
	{

		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		//keys.put("treatments.taxonomy", 1);
		
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.is("treatments.taxonomy.code","taxonomy").notExists("treatments.taxonomy.read1.software"),keys).toList();
		
		//Attention backup manuel car trop de donnees
		//backUpReadSet(readSets);

		Logger.debug("Nb readSets to update "+readSets.size());
		int nb=0;
		for(ReadSet readSet : readSets){
			Logger.debug("ReadSet "+nb);
			readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			Treatment treatmentTaxonomy = readSet.treatments.get("taxonomy");
			treatmentTaxonomy.results.get("read1").put("software", new PropertySingleValue("megablast_megan"));
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readSet.code),  DBUpdate.set("treatments.taxonomy", treatmentTaxonomy));
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
