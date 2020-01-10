package controllers.migration.cns;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationInputExperimentPropertiesTreeOfLifeUpgrade extends MigrationExperimentProperties{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	/*
	 * Propagation d'une propriété d'experiment au niveau content (containers et readSets)
	 * @param experimentTypeCode
	 * @param keyProperty
	 * @return
	 */
	public static Result migration(String experimentTypeCode, String newKeyProperty){

		//Get list experiment
		List<Experiment> experiments = getListExperiments(DBQuery.is("typeCode", experimentTypeCode).is("code", "RNA-ILLUMINA-INDEXED-LIBRARY-20160909_135531GEJ"));

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
				final String tag = (String) output.contents.iterator().next().properties.get("tag").getValue();
				Logger.debug("Sample code "+sampleCode+" Tag "+tag);

				PropertyValue propValue = input.experimentProperties.get(newKeyProperty);

				//Get outputContainer
				Logger.debug("Get outputContainerCode "+output.code);
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, output.code);
				
				if(container!=null){
					//add property to container
					Logger.debug("Size content to update "+container.contents.size());
					container.contents.stream().filter(content->content.sampleCode.equals(sampleCode) && (tag==null || ((String)content.properties.get("tag").getValue()).equals(tag))).forEach(c->{
						if(!c.properties.containsKey(newKeyProperty)){
							Logger.debug("Missing Update container "+container.code+" for content "+c.sampleCode+" with tag "+tag);
							//c.properties.put(newKeyProperty, propValue);
						}
					});
					updateContainer(container.code, newKeyProperty, propValue, false);

					//Get list of all Container in process
					List<Container> containerOuts = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.regex("treeOfLife.paths", Pattern.compile(","+container.code))).toList();
					String tagContainer = tag;
					for(Container containerOut : containerOuts){
						if(tagContainer==null){
							for(Content c:containerOut.contents){
								if(c.sampleCode.equals(sampleCode) && c.properties.containsKey("tag")){
									tagContainer=c.properties.get("tag").value.toString();
								}
							}
						}
						Logger.debug("Update container code "+containerOut.code+" size content "+containerOut.contents.size()+" with tag "+tagContainer);
						updateContainer(containerOut.code, sampleCode, tagContainer, newKeyProperty, propValue, false);
					}
				}
			});
		}
		return ok();
	}
	
	

}
