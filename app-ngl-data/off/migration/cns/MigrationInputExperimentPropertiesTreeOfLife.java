package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationInputExperimentPropertiesTreeOfLife extends MigrationExperimentProperties{

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

		Logger.debug("Size experiments "+experiments.size());
		//Get list experiment with no experiment properties
		for(Experiment exp : experiments){
			checkATMExperiment(exp);
			checkInputExperimentProperties(exp, keyProperty);
			checkOneContentForATM(exp);
		}

		//Get all inputQuantity to change to libraryInputQuantity
		for(Experiment exp : experiments){
			Logger.debug("Code experiment "+exp.code);
			Logger.debug("Nb ATM "+exp.atomicTransfertMethods.size());
			exp.atomicTransfertMethods.stream().filter(atm->atm.getClass().getName().equals(OneToOneContainer.class.getName())).forEach(atm->{
				//Get inputContainer
				InputContainerUsed input = atm.inputContainerUseds.iterator().next();
				OutputContainerUsed output = atm.outputContainerUseds.iterator().next();
				
				//Get sampleCode and tag
				String sampleCode = output.contents.iterator().next().sampleCode;
				String tag = (String) output.contents.iterator().next().properties.get("tag").getValue();
				Logger.debug("Sample code "+sampleCode+" Tag "+tag);
				if(keyProperty!=null){
					input.experimentProperties.put(newKeyProperty, input.experimentProperties.get(keyProperty));
					input.experimentProperties.remove(keyProperty);
				}
				
				PropertyValue propValue = input.experimentProperties.get(newKeyProperty);
				//add property to contents properties to inputContainerUsed
				//updateContainerContents(input, newKeyProperty, propValue);
				
				//updateContainer(input.code, newKeyProperty, propValue, false);
				
				updateOutputContainerTreeOfLife(output, sampleCode, tag, newKeyProperty, propValue, false);
				
				
			});

			//Update experiment in database
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		}

		Logger.debug("migration MigrationInputExperimentPropertiesTreeOfLife "+experimentTypeCode+" keyProperty "+ keyProperty +" keyProperty "+ newKeyProperty+" done!");

		return ok();
	}

}
