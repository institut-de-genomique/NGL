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

public class ScriptRepriseGridionFlongle extends ScriptWithArgs<Object> {
	
	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début reprise gridion flongle");

		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("instrument.typeCode", "gridION")).toList();

		for (int i = 0; i < expList.size(); i++) {
			Experiment exp = expList.get(i);

			if (exp.code.equals("NANOPORE-DEPOT-20210428_082738AIH")) {
				exp.instrumentProperties.put("flongleAdapter", new PropertySingleValue("FA-01336"));
			} else if (exp.code.equals("NANOPORE-DEPOT-20210429_110550EDF")) {
				exp.instrumentProperties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
			} else if (exp.code.equals("NANOPORE-DEPOT-20210519_150301EEE")) {
				exp.instrumentProperties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
			} else {
				exp.instrumentProperties.put("flongleAdapter", new PropertySingleValue("noFlongle"));
			}

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);

			// Mise à jour des output container

			for (int j = 0; j < exp.outputContainerCodes.toArray().length; j++) {
				Container c = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", exp.outputContainerCodes.toArray()[j]));
				
				for (int k = 0; k < c.contents.size(); k++) {
					if (c.code.equals("AGQ257_A")) {
						c.contents.get(k).properties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
					} else if (c.code.equals("AGQ111_A")) {
						c.contents.get(k).properties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
					} else if (c.code.equals("AGQ150_A")) {
						c.contents.get(k).properties.put("flongleAdapter", new PropertySingleValue("FA-01336"));
					} else {
						c.contents.get(k).properties.put("flongleAdapter", new PropertySingleValue("noFlongle"));
					}
				}

				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, c);

				// Mise à jour des readsets

				List<ReadSet> rsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.containerCode", exp.outputContainerCodes.toArray()[j])).toList();

				for (int l = 0; l < rsList.size(); l++) {
					ReadSet rs = rsList.get(l);

					if (rs.code.equals("CYD_AAAA_ONT_1_AGQ257_A")) {
						rs.sampleOnContainer.properties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
					} else if (rs.code.equals("CYA_AAAA_ONT_1_AGQ111_A")) {
						rs.sampleOnContainer.properties.put("flongleAdapter", new PropertySingleValue("FA-00334"));
					} else if (rs.code.equals("CYA_AAAA_ONT_1_AGQ150_A")) {
						rs.sampleOnContainer.properties.put("flongleAdapter", new PropertySingleValue("FA-01336"));
					} else {
						rs.sampleOnContainer.properties.put("flongleAdapter", new PropertySingleValue("noFlongle"));
					}

					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, rs);
				}
			}
		}

		Logger.error("Fin reprise gridion flongle");
	}
}