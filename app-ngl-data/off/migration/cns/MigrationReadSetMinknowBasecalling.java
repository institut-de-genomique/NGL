package controllers.migration.cns;

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
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
/**
 * NGL-1405 : Propriétés manquantes traitement minknowBasecalling du ReadSet
 * 
 * @author ejacoby
 *
 */
public class MigrationReadSetMinknowBasecalling extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration(){

		//Get run nanopore
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("categoryCode", "nanopore").exists("treatments.minknowBasecalling")).toList();
		for(Run run : runs){

			Treatment treatment = run.treatments.get("minknowBasecalling");
			//Get two missing values
			PropertySingleValue valueMinknowEvents = null;
			PropertySingleValue valueMinknowCompleteReads = null;

			if(treatment.results.get("default").containsKey("minknowEvents"))
				valueMinknowEvents=(PropertySingleValue) treatment.results.get("default").get("minknowEvents");
			if(treatment.results.get("default").containsKey("minknowCompleteReads"))
				valueMinknowCompleteReads=(PropertySingleValue) treatment.results.get("default").get("minknowCompleteReads");

			//Update all readSets
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).toList();
			for(ReadSet readSet : readSets){
				Logger.debug("Migrate metrichor for run "+run.code+" to readSet "+readSet.code);
				Treatment treatmentReadSet = readSet.treatments.get("minknowBasecalling");
				if(treatmentReadSet!=null){
					if(valueMinknowEvents!=null)
						treatmentReadSet.results.get("default").put("minknowEvents", valueMinknowEvents);
					if(valueMinknowCompleteReads!=null)
						treatmentReadSet.results.get("default").put("minknowCompleteReads", valueMinknowCompleteReads);

					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
							DBQuery.is("code", readSet.code),  
							DBUpdate.set("treatments.minknowBasecalling", treatmentReadSet));
				}

			}
		}
		return ok("MigrationReadSetMinknowBasecalling finished");
	}

	protected static void backupReadSetCollection() {
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_"+sdf.format(new java.util.Date());
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" start");
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("sampleOnContainer"), keys).toList());
	}

}
