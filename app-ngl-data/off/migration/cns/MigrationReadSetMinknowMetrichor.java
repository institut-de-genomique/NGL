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
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationReadSetMinknowMetrichor extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration(){

		//Get run nanopore
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("categoryCode", "nanopore").in("state.code", "F-V","IW-V","IP-V")).toList();
		for(Run run : runs){
			
			Treatment treatment = run.treatments.get("minknowMetrichor");
			Map<String, PropertyValue> newResultsValue = new HashMap<String, PropertyValue>();
			newResultsValue.put("minKnowVersion",treatment.results.get("default").get("minKnowVersion"));
			newResultsValue.put("metrichorVersion",treatment.results.get("default").get("metrichorVersion"));
			newResultsValue.put("metrichorWorkflowName",treatment.results.get("default").get("metrichorWorkflowName"));
			newResultsValue.put("metrichorWorkflowVersion",treatment.results.get("default").get("metrichorWorkflowVersion"));
			newResultsValue.put("metrichorRunID",treatment.results.get("default").get("metrichorRunID"));

			Treatment treatMetrichorForReadSet = treatment;
			treatMetrichorForReadSet.results.put("default",newResultsValue);

			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", run.code)).toList();
			for(ReadSet readSet : readSets){
				Logger.debug("Migrate metrichor for run "+run.code+" to readSet "+readSet.code);
				readSet.treatments.put("minknowMetrichor",treatMetrichorForReadSet);

				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,  
						DBQuery.is("code",readSet.code),
						DBUpdate.set("treatments", readSet.treatments));
			}
		}
		return ok("MigrationReadSetMinknowMetrichor finished");
	}

	protected static void backupReadSetCollection() {
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_"+sdf.format(new java.util.Date());
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" start");
		MongoDBDAO.save(backupName, MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("sampleOnContainer"), keys).toList());
	}

}
