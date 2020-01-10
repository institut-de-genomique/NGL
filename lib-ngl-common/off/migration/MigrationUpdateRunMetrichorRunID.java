package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateRunMetrichorRunID extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration()
	{


		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				DBQuery.is("treatments.minknowMetrichor.code","minknowMetrichor")).toList();
		backUpRun(runs);

		for(Run run : runs){
			Treatment treatmentMinknowMetrichor = run.treatments.get("minknowMetrichor");
			if(treatmentMinknowMetrichor.results.get("default").containsKey("metrichorRunID") && treatmentMinknowMetrichor.results.get("default").get("metrichorRunID").value instanceof String){
				Long metrichorRunId = Long.parseLong((String)treatmentMinknowMetrichor.results.get("default").get("metrichorRunID").value);
				Logger.info("metrichorRunID "+metrichorRunId);
				treatmentMinknowMetrichor.results.get("default").put("metrichorRunID", new PropertySingleValue(metrichorRunId));
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", run.code),  DBUpdate.set("treatments.minknowMetrichor", treatmentMinknowMetrichor));
			}
		}
		return ok("Migration finished");

	}

	private static void backUpRun(List<Run> runs)
	{
		String backupName = InstanceConstants.RUN_ILLUMINA_COLL_NAME+"_BCK_NGL879_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" start");

		MongoDBDAO.save(backupName, runs);
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" end");

	}


}
