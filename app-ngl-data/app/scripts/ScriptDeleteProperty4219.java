package scripts;

import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;

public class ScriptDeleteProperty4219 extends ScriptWithArgs <ScriptDeleteProperty4219.Args> {

	public static class Args {

	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.error("ScriptDeleteProperty4219 start");

		DBQuery.Query query = DBQuery.exists("contents.properties.containerDescription");

		List<Container> containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query).toList();
		
		for (int i = 0; i < containersList.size(); i++) {
			for (int j = 0; j < containersList.get(i).contents.size(); j++) {
				Logger.error("Mise à jour du container '" + containersList.get(i).code + ".");

				PropertyValue propValue = containersList.get(i).contents.get(j).properties.get("containerDescription");

				containersList.get(i).contents.get(j).properties.remove("containerDescription");
				containersList.get(i).contents.get(j).properties.put("initialContainerDescription", propValue);

				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containersList.get(i));
			}
		}

		query = DBQuery.exists("sampleOnInputContainer.properties.containerDescription");

		List<Process> processList = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, query).toList();
		
		for (int i = 0; i < processList.size(); i++) {
			Logger.error("Mise à jour du process '" + processList.get(i).code + ".");

			PropertyValue propValue = processList.get(i).sampleOnInputContainer.properties.get("containerDescription");

			processList.get(i).sampleOnInputContainer.properties.remove("containerDescription");
			processList.get(i).sampleOnInputContainer.properties.put("initialContainerDescription", propValue);

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, processList.get(i));
		}

		query = DBQuery.exists("sampleOnContainer.properties.containerDescription");

		List<ReadSet> rsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, query).toList();
		
		for (int i = 0; i < rsList.size(); i++) {
			Logger.error("Mise à jour du readset '" + rsList.get(i).code + ".");

			PropertyValue propValue = rsList.get(i).sampleOnContainer.properties.get("containerDescription");

			rsList.get(i).sampleOnContainer.properties.remove("containerDescription");
			rsList.get(i).sampleOnContainer.properties.put("initialContainerDescription", propValue);

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, rsList.get(i));
		}

		query = DBQuery.exists("atomicTransfertMethods.inputContainerUseds.contents.properties.containerDescription");

		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();
		
		for (int i = 0; i < expList.size(); i++) {
			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.size(); l++) {
						if (expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.containsKey("containerDescription")) {
							Logger.error("Mise à jour de l'atm/icu/contents '" + expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).sampleCode + ".");

							PropertyValue propValue = expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.get("containerDescription");

							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.remove("containerDescription");
							expList.get(i).atomicTransfertMethods.get(j).inputContainerUseds.get(k).contents.get(l).properties.put("initialContainerDescription", propValue);

							MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
						}
					}
				}
			}
		}

		query = DBQuery.exists("atomicTransfertMethods.outputContainerUseds.contents.properties.containerDescription");

		expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();
		
		for (int i = 0; i < expList.size(); i++) {
			for (int j = 0; j < expList.get(i).atomicTransfertMethods.size(); j++) {
				for (int k = 0; k < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.size(); k++) {
					for (int l = 0; l < expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.size(); l++) {
						if (expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.containsKey("containerDescription")) {
							Logger.error("Mise à jour de l'atm/ocu/contents '" + expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).sampleCode + ".");

							PropertyValue propValue = expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.get("containerDescription");

							expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.remove("containerDescription");
							expList.get(i).atomicTransfertMethods.get(j).outputContainerUseds.get(k).contents.get(l).properties.put("initialContainerDescription", propValue);

							MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, expList.get(i));
						}
					}
				}
			}
		}

		Logger.error("ScriptDeleteProperty4219 end");
	}

}
