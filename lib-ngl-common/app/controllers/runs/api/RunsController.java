package controllers.runs.api;


import org.mongojack.DBQuery;

import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class RunsController extends CommonController {

    protected static Run getRun(String code) {
    	Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, code);
		return run;
    }

    protected static Run getRun(String code, String...keys) {
		Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", code), getIncludeKeys(keys));
		return run;
    }
    
    protected static TraceInformation getUpdateTraceInformation(Run run) {
		TraceInformation ti = run.traceInformation;
		ti.setTraceInformation(getCurrentUser());
		return ti;
	}
	
    protected static Run getRun(String code, Integer laneNumber) {
		Run run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,
			Run.class, DBQuery.and(DBQuery.is("code", code), DBQuery.is("lanes.number", laneNumber)));
		return run;
    }
    
}
