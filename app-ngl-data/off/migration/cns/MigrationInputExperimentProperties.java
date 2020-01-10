package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationInputExperimentProperties extends MigrationExperimentProperties{

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
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode).notExists("atomicTransfertMethods.inputContainerUseds.contents.properties.loadingReport"));
		Logger.debug("Size of experiments "+experiments.size());
		
		//Get list experiment with no experiment properties
		for(Experiment exp : experiments){
			checkATMExperiment(exp);
			checkInputExperimentProperties(exp, newKeyProperty);
		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			Logger.debug("Code experiment "+exp.code);
			Logger.debug("Classe "+OneToOneContainer.class.getName());
			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				Logger.debug("ATM "+atm.getClass());
				atm.inputContainerUseds.stream().filter(input->input.experimentProperties!=null).forEach(input->{
					Logger.debug("Update property");
					if(keyProperty!=null){
						input.experimentProperties.put(newKeyProperty, input.experimentProperties.get(keyProperty));
						input.experimentProperties.remove(keyProperty);
					}
					PropertyValue propValue = input.experimentProperties.get(newKeyProperty);
					
					
					//add property to contents properties to inputContainerUsed
					//updateContainerContents(input, newKeyProperty, propValue);

					//Get container from input
					//updateContainer(input.code, newKeyProperty, propValue, false);

					updateOutputContainer(atm,newKeyProperty,propValue, false);
				});
			});

			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}



		return ok();
	}

}
