package scripts;

import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import play.Logger;

public class ScriptReprise4704 extends ScriptWithArgs<Object> {
	
	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise 4704");

		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
			DBQuery.is("typeCode", "abc"))
		.toList();

		for (int i = 0; i < expList.size(); i++) {
			Logger.error(expList.get(i).code);

			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.size(); k++) {
					expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).experimentProperties.putIfAbsent("middleConcentration", expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).experimentProperties.get("concentration1"));
					expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).experimentProperties.remove("concentration1");
				
					MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
				}
			}
		}

		Logger.error("Fin reprise 4704");
	}
}