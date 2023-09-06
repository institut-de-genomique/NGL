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

public class MigrationExperimentPropertiesConcentration extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	/*
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(String experimentTypeCode, String newKeyProperty){

		//backupContainerCollection();
		//backupReadSetCollection();

		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode));
		//Get list experiment with no experiment properties
		for(Experiment exp : experiments){
			checkATMExperiment(exp);
			checkOutputExperimentProperties(exp, newKeyProperty);

		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			Logger.debug("Code experiment "+exp.code);
			Logger.debug("Classe "+OneToOneContainer.class.getName());
			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				Logger.debug("ATM "+atm.getClass());
				atm.outputContainerUseds.stream().filter(output->output.experimentProperties!=null && !output.experimentProperties.containsKey(newKeyProperty)).forEach(output->{
					//Get property
					Logger.debug("Update property for container out "+output.code);
					PropertyValue propValue = output.concentration;
					output.experimentProperties.put(newKeyProperty, propValue);

					updateContainerContents(output, newKeyProperty, propValue);
					
					updateContainer(output.code, newKeyProperty, propValue, false);

					List<String> containerCodes = new ArrayList<String>();
					getListContainerCode(output.locationOnContainerSupport.code, containerCodes);
					for(String codeContainer : containerCodes){
						Logger.debug("Update container code "+codeContainer);
						updateContainer(codeContainer, newKeyProperty, propValue, false);
					}

				});
			});

			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}

		return ok();
	}


}
