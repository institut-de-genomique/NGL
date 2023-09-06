package controllers.migration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;


public class AddSequencingProgramTypeToRun  extends CommonController {

	private static final String RUN_ILLUMINA_COLL_NAME_BCK = InstanceConstants.RUN_ILLUMINA_COLL_NAME+"_BCK";

	public static Result migration() {
		
		List<Run> runsCollBck = MongoDBDAO.find(RUN_ILLUMINA_COLL_NAME_BCK, Run.class).toList();
		if(runsCollBck.size() == 0){
			
			Logger.info(">>>>>>>>>>> Migration Run starts");

			backupRunCollection();

			migreSequencingProgramType();
			
			Logger.info(">>>>>>>>>>> Migration Run end");
		} else {
			Logger.info(">>>>>>>>>>> Migration Run already execute !");
		}
		
		return ok(">>>>>>>>>>> Migration Run finish");
	}


	private static void migreSequencingProgramType() {

		//set hashMap hm
		HashMap<String, PropertyValue> hm = new HashMap<String, PropertyValue>();
		
		List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("categoryCode", Arrays.asList("flowcell-1", "flowcell-2", "flowcell-8"))).toList();
		for (ContainerSupport containerSupport : containerSupports) {
			//Logger.debug("ContainerSupport "+containerSupport.code);
			if(containerSupport.properties!=null && containerSupport.properties.containsKey("sequencingProgramType")){
				hm.put(containerSupport.code, containerSupport.properties.get("sequencingProgramType"));
			}
		}

		//use hm to retrieve sequencingProgramType with the containerSupportCode and affect it to runs
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class).toList();
		
		Logger.debug("Migre "+runs.size()+" RUNS");		
		
		String categoryCode = "";

		for (Run run : runs) {
			
			try {
				categoryCode = models.laboratory.run.description.RunType.find.findByCode(run.typeCode).category.code;
			
				if (categoryCode.equals("illumina")) { 
					
					run.properties.put("sequencingProgramType", hm.get(run.containerSupportCode));
					
					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, ContainerSupport.class, DBQuery.is("code", run.code),   
							DBUpdate.set("properties", run.properties));
					//global update of the object to have the _type (json subtype) like in the import 
					//MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
				}
			}
			catch(DAOException e) {
				Logger.error("DAOException type error !");
			}
		}

		
	}


	private static void backupRunCollection() {
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" start");
		MongoDBDAO.save(RUN_ILLUMINA_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class).toList());
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" end");
	}
	
	
}
