package scripts;

import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;

public class ScriptReprise2480 extends ScriptWithArgs<Object> {
	
	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise 2480");

		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
			DBQuery	.notExists("instrumentProperties.flongleAdapter.value")
					.or(DBQuery.is("instrument.typeCode", "gridION"),
						DBQuery.is("instrument.typeCode", "mk1b"),
						DBQuery.is("instrument.typeCode", "mk1c")))
		.toList();

		for (int i = 0; i < expList.size(); i++) {
			Experiment exp = expList.get(i);
			Logger.error(exp.code);

			exp.instrumentProperties.put("flongleAdapter", new PropertySingleValue("noFlongle"));

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);

			// Mise à jour des output container

			for (int j = 0; j < exp.outputContainerCodes.toArray().length; j++) {
				Container c = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", exp.outputContainerCodes.toArray()[j]));
				
				for (int k = 0; k < c.contents.size(); k++) {
					c.contents.get(k).properties.put("flongleAdapter", new PropertySingleValue("noFlongle"));
				}

				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, c);

				// Mise à jour des readsets

				List<ReadSet> rsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.containerCode", exp.outputContainerCodes.toArray()[j])).toList();

				for (int l = 0; l < rsList.size(); l++) {
					ReadSet rs = rsList.get(l);

					rs.sampleOnContainer.properties.put("flongleAdapter", new PropertySingleValue("noFlongle"));
					
					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, rs);
				}
			}
		}

		Logger.error("Fin reprise gridion flongle");
	}
}