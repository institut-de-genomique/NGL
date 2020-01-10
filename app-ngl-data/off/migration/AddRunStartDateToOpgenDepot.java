package controllers.migration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

import org.apache.commons.collections.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;

import com.mongodb.MongoException;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class AddRunStartDateToOpgenDepot extends CommonController {

	private static final Date date = new Date();
	private static final DateFormat df = new SimpleDateFormat("ddMMyy'_'hhmm");
	private static final String today = df.format(date);
	private static final String EXPERIMENT_COLL_NAME_BCK = InstanceConstants.EXPERIMENT_COLL_NAME+"_BCK"+"_"+today;

	public static Result migration() {
		Logger.info(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot will start");
		List<Experiment> experimentsCollBck = MongoDBDAO.find(EXPERIMENT_COLL_NAME_BCK, Experiment.class).toList();		
		if(CollectionUtils.isEmpty(experimentsCollBck)){

			Logger.info(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot starts");
			backupExperimentCollection();
			AddRunStartDateProperties();
			Logger.info(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot end");

		}else{
			Logger.info(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot already execute !");
		}

		Logger.info(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot finish");
		return ok(">>>>>>>>>>> Migration Adding RunStartDate to OpgenDepot finish");
	}


	private static void AddRunStartDateProperties(){
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class).toList();
		Logger.debug("Migre "+experiments.size()+" EXPERIMENTS");

		PropertySingleValue pv = null;

		for(Experiment exp : experiments){			
			if((exp.typeCode).equals("opgen-depot")){

				if(exp.experimentProperties == null){
					exp.experimentProperties = new HashMap<String, PropertyValue>();
				}

				if(!exp.experimentProperties.containsKey("runStartDate")){
					pv = new PropertySingleValue(exp.traceInformation.creationDate);
					exp.experimentProperties.put("runStartDate", pv);					
					try{
						MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
								DBQuery.is("code",exp.code), DBUpdate.set("experimentProperties", exp.experimentProperties));
					}catch(MongoException e) {
						Logger.error("MongoException type error !");
					}	

				}

			}

		}	
	}

	private static void backupExperimentCollection() {
		Logger.info("\tCopie "+InstanceConstants.EXPERIMENT_COLL_NAME+" start");
		MongoDBDAO.save(EXPERIMENT_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class).toList());
		Logger.info("\tCopie "+InstanceConstants.EXPERIMENT_COLL_NAME+" end");
	}

}
