package controllers.migration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import play.Logger;
import play.mvc.Result;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class AddEstimatedPercentPerLaneToProcess extends CommonController{

	private static final Date date = new Date();
	private static final DateFormat df = new SimpleDateFormat("ddMMyy'_'hhmm");
	private static final String today = df.format(date);
	private static final String PROCESS__COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME+"_BCK"+"_"+today;
	
	/*
	public static Result migration() {

		List<Process> processesCollBck = MongoDBDAO.find(PROCESS__COLL_NAME_BCK, Process.class).toList();
		if(processesCollBck.size() == 0){

			Logger.info(">>>>>>>>>>> Migration PercentageContents in Containers starts");
			
	
		}
	}
	
	*/
	
	
	
	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" start");
		MongoDBDAO.save(PROCESS__COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class).toList());
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" end");
	}
	
}
