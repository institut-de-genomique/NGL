package scripts;

import org.mongojack.DBQuery;

import java.util.*;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;

import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

import play.Logger;

public class ScriptReprise4407 extends ScriptNoArgs {

	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptReprise4407");

		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
			 DBQuery.is("typeCode", "fragmentation")
					.is("instrument.code", "megaruptor3-1"))
		.toList();

		expList.forEach(exp -> {
			if (exp.instrumentProperties.get("speed") != null) {
				Logger.error(exp.code + " : new value of program : " + exp.instrumentProperties.get("speed").value);

				exp.instrumentProperties.get("program").value = String.valueOf(exp.instrumentProperties.get("speed").value);
				exp.instrumentProperties.remove("speed");
				
				MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			} else {
				Logger.error(exp.code + " : has no speed property");
			}
			
		});

		Logger.error("End ScriptReprise4407");
	}
}