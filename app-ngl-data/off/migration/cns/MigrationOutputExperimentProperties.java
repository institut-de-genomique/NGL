package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;


import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationOutputExperimentProperties extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	/**
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(String experimentTypeCode, String keyProperty, String newKeyProperty){

		//backupContainerCollection();
		//backupReadSetCollection();

		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode));
		//List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode).notExists("atomicTransfertMethods.outputContainerUseds.contents.properties.qcFlowcell"));
		//Get list experiment with no experiment properties
		//Logger.debug("Size experiments "+experiments.size());
		for(Experiment exp : experiments){
			checkATMExperiment(exp);
			checkOutputExperimentProperties(exp, newKeyProperty);
		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			//Logger.debug("Code experiment "+exp.code);
			//Logger.debug("Classe "+OneToOneContainer.class.getName());
			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				//Logger.debug("ATM "+atm.getClass());
				atm.outputContainerUseds.stream().filter(output->output.experimentProperties!=null).forEach(output->{
					//Logger.debug("Update property");
					//get container
					//Logger.debug("Get container "+output.code);
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, output.code);
					if(container!=null){
						//Logger.debug("size content "+container.contents.size());
						Content content = container.contents.iterator().next();
						if(!content.properties.containsKey("qcFlowcell")){
							Logger.debug("Update experiment "+exp.code);
							if(keyProperty!=null){
								output.experimentProperties.put(newKeyProperty, output.experimentProperties.get(keyProperty));
								output.experimentProperties.remove(keyProperty);
							}
							PropertyValue propValue = output.experimentProperties.get(newKeyProperty);
							//add property to contents properties to inputContainerUsed
							updateContainerContents(output, newKeyProperty, propValue);
							updateContainer(output.code, newKeyProperty, propValue, false);
							
							List<String> containerCodes = new ArrayList<String>();
							getListContainerCode(output.locationOnContainerSupport.code, containerCodes);
							for(String codeContainer : containerCodes){
								Logger.debug("Update container code "+codeContainer);
								updateContainer(codeContainer, newKeyProperty, propValue, false);
							}
						}
					}else{
						Logger.debug("No container "+output.code);
						//Update only outputContainer
						if(keyProperty!=null){
							output.experimentProperties.put(newKeyProperty, output.experimentProperties.get(keyProperty));
							output.experimentProperties.remove(keyProperty);
						}
						PropertyValue propValue = output.experimentProperties.get(newKeyProperty);
						//add property to contents properties to inputContainerUsed
						updateContainerContents(output, newKeyProperty, propValue);
					}
					
				});
			});

			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}



		return ok();
	}

}
