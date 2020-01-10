package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationExperimentFlowcellChemistry extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	/*
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(){

		//backupContainerCollection();
		//backupReadSetCollection();

		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.exists("instrumentProperties.flowcellChemistry").notEquals("instrumentProperties.flowcellChemistry.value", "R9-spot-on"));


		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			Logger.debug("Code experiment "+exp.code);

			if(!((String)exp.instrumentProperties.get("flowcellChemistry").value).startsWith("R")){
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class
						,DBQuery.is("code",exp.code)
						,DBUpdate.set("instrumentProperties.flowcellChemistry.value", "R"+exp.instrumentProperties.get("flowcellChemistry").getValue()));
			}
		}

		return ok();
	}


}
