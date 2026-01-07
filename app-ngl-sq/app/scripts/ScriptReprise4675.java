package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import io.jsonwebtoken.lang.Collections;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import play.Logger;

public class ScriptReprise4675 extends ScriptWithArgs<Object> {
	
	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise 4675");

		// CAS 1

		List<Experiment> expListCas1 = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
			DBQuery	.is("typeCode", "hmw-dna-extraction")
					.notExists("atomicTransfertMethods.inputContainerUseds.experimentProperties.hmwDnaExtractInputQuantity"))
		.toList();

		for (int i = 0; i < expListCas1.size(); i++) {
			Experiment exp = expListCas1.get(i);
			Logger.error(exp.code);

			for (int j = 0; j < exp.atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < exp.atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					PropertySingleValue propertyDef = new PropertySingleValue(1, "ng");
					Logger.error("Dans l'expérience '" + exp.code + "', mise à jour de l'inputContainerUsed '" + exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + "' avec comme ajout la propriété 'hmwDnaExtractInputQuantity' et comme valeur '" + propertyDef.value + "'.");
					
					if (Collections.isEmpty(exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties)) {
						exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties = new HashMap<>();
					}

					exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.put("hmwDnaExtractInputQuantity", propertyDef);
				}
			}

			if (exp.comments == null) {
				exp.comments = new ArrayList<>();
			}

			exp.comments.add(new Comment("reprise de passif cf. NGL-4675", "ngl-support"));

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}

		// CAS 2

		/** List<Experiment> expListCas2 = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
			DBQuery	.is("typeCode", "grinding")
					.exists("atomicTransfertMethods.inputContainerUseds.experimentProperties.biologicalMaterialInputQty"))
		.toList();

		for (int i = 0; i < expListCas2.size(); i++) {
			Experiment exp = expListCas2.get(i);
			Logger.error(exp.code);

			for (int j = 0; j < exp.atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < exp.atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					Logger.error("Dans l'expérience '" + exp.code + "', mise à jour de l'inputContainerUsed '" + exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).code + "' avec comme ajout la propriété 'grindingBioMatInputQuantity' et suppression de la propriété 'biologicalMaterialInputQty'.");
					
					if (Collections.isEmpty(exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties)) {
						exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties = new HashMap<>();
					}

					exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.putIfAbsent("grindingBioMatInputQuantity", exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.get("biologicalMaterialInputQty"));
					exp.atomicTransfertMethods.get(j).inputContainerUseds.get(k).experimentProperties.remove("biologicalMaterialInputQty");
				}
			} 

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		} */

		Logger.error("Fin reprise 4675");
	}
}