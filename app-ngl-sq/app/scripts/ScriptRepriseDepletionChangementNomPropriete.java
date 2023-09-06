package scripts;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import play.Logger;

public class ScriptRepriseDepletionChangementNomPropriete extends ScriptWithArgs<Object> {

	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise");

		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "rrna-depletion")).toList().forEach(exp -> {
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if (icu.experimentProperties != null) {						
						icu.experimentProperties.put("rRNAdepletInputQuantity", icu.experimentProperties.get("inputQuantity"));
						icu.experimentProperties.remove("inputQuantity");
		
						Logger.error("Mise à jour de l'icu '" + icu.code + "' avec l'expérience '" + exp.code + "'.");
		
						MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
					} else {
						Logger.error("'icu.experimentProperties' est 'null'.");
					}
				});
			});
		});

		Logger.error("Fin reprise");
	}
}