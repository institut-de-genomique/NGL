package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationExperimentInstrumentProperties extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	/**
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(String experimentTypeCode, String keyProperty, boolean addToRun){

		//backupContainerCollection();
		//backupReadSetCollection();

		Logger.debug("Get experiments "+experimentTypeCode+" / "+keyProperty+" / "+addToRun);
		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode).exists("instrumentProperties."+keyProperty));
		Logger.debug("Size experiments "+experiments.size());

		//Get list experiment with no experiment properties
		for(Experiment exp : experiments){
			checkATMExperiment(exp);
		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			Logger.debug("Code experiment "+exp.code);
			Logger.debug("Classe "+OneToOneContainer.class.getName());
			PropertyValue propValue = exp.instrumentProperties.get(keyProperty);
			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				Logger.debug("ATM "+atm.getClass());
				atm.inputContainerUseds.stream().forEach(input->{
					//Get property
					Logger.debug("Update property for container out "+input.code);

					//updateContainerContents(input, keyProperty, propValue);
					
					//Get container from input
					//updateContainer(input.code, keyProperty, propValue, addToRun);

					updateOutputContainer(atm, keyProperty, propValue, addToRun);

				});
			});

			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}

		return ok();
	}


}
