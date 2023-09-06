package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.instance.AbstractContainerUsed;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import workflows.container.ContentHelper;

public class RemovePropertyContentCascade extends ScriptWithExcelBody {

	private final ExperimentsAPI expAPI;

	@Inject
	public RemovePropertyContentCascade(ExperimentsAPI expAPI) {
		this.expAPI   = expAPI;
	}

	/**
	 * Script a supprimer lorsque la suppression des proprietes de protocole sera prise en compte dans disquette rouge
	 * !!! Attention script qui supprime la propriété en cascade selon code quelque soit son niveau ou type
	 * !!! Pas de verification de description 
	 * ATTENTION ne prend pas en compte les tags secondaires!!!
	 * 
	 * 
	 * Format of excel file supported:                                          <br>
            1st sheet "racine": experiments which require cascading properties <br>
            For each sheet:                                                  <br>
            1st line: header of columns (the name used are free)             <br>

            | Experiment | property to delete | 							<br>
                                                                             <br>            

            1st column: code of experiment                                   <br>
            2nd column: property to delete         							<br>
                                                                             <br>
            Make sure to remove any empty line in the file.
	 */
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {

		List<String> failedCodes = new ArrayList<>();
		getLogger().info("Update experiments with cascading properties");
		failedCodes = updateExperiment(workbook.getSheet("racine"));

		if(failedCodes.size() != 0) {
			getLogger().warn("Some experiments have not been updated");
			getLogger().warn(failedCodes.toString());
		} else {
			getLogger().info("All experiments have been updated");
		}
	}

	private List<String> updateExperiment(XSSFSheet sheet) {
		List<String> failedCodes = new ArrayList<>();
		final String user = "ngl-admin";

		sheet.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header

			String expCode      = row.getCell(0).getStringCellValue();
			String propertyToDeleted = row.getCell(1).getStringCellValue();

			try {
				// Update the experiment with new protocol after checking the current protocol into experiment
				Experiment exp = this.expAPI.get(expCode);
				try {
					Logger.debug("Start update from "+expCode);
					updateWithRemoveProperty(exp, user,propertyToDeleted);
				} catch (APIException e) {
					getLogger().error("(" + expCode + ") update failed: " + e.getMessage());
					getLogger().debug(e.getMessage(), e);
				}
				getLogger().info("experiment " + expCode + " updated");
			} catch (Exception e) {
				getLogger().error("Experiment does not exist " + expCode);
				getLogger().debug(e.getMessage(), e);
				failedCodes.add(expCode);
			}
		});
		return failedCodes;
	}

	private Experiment updateWithRemoveProperty(Experiment exp, String currentUser, String deletedPropertyCode) throws APIException, APIValidationException {

		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		if (exp.traceInformation != null) {
			exp.traceInformation.modificationStamp(ctxVal, currentUser);
		} else {
			getLogger().error("traceInformation is null !!");
		}

		//Update first experiment
		updateFirstExperiment(exp, deletedPropertyCode, ctxVal);
		
		//Remove property
		exp.atomicTransfertMethods.forEach(atm -> {
			if(ExperimentCategory.CODE.qualitycontrol.toString().equals(exp.categoryCode)){
				atm.inputContainerUseds
				.stream()
				.forEach(icu -> updateContainerContentPropertiesInCascading(ctxVal, icu, deletedPropertyCode));
			}else if(atm.outputContainerUseds != null){
				atm.outputContainerUseds
				.stream()
				.forEach(ocu -> updateContainerContentPropertiesInCascading(ctxVal, ocu, deletedPropertyCode));					
			}

		});	

		exp.validate(ctxVal, null);	

		if (!ctxVal.hasErrors()) {	
			expAPI.update(exp, currentUser);
			return expAPI.get(exp.code);
		} else {
			throw new APIValidationException("Invalid Experiment object", ctxVal.getErrors());
		}
	}

	private void updateContainerContentPropertiesInCascading(ContextValidation validation, AbstractContainerUsed acu, String deletedPropertyCode) {
		List<Container> containerMustBeUpdated = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.or(DBQuery.is("code", acu.code), DBQuery.regex("treeOfLife.paths", Pattern.compile(","+acu.code+"$|,"+acu.code+","))))
				.toList();
		Set<String> containerCodes = containerMustBeUpdated.stream().map(c -> c.code).collect(Collectors.toSet());
		Logger.debug("Start update for ");
		containerCodes.forEach(c->Logger.debug("Container "+c));
		acu.contents.forEach(ocuContent -> {
			List<Sample> allSamples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,  
					DBQuery.or(DBQuery.is("code", ocuContent.sampleCode), DBQuery.regex("life.path", Pattern.compile(","+ocuContent.sampleCode+"$|,"+ocuContent.sampleCode+","))))
					.toList();

			Set<String> projectCodes = allSamples.stream().map(s -> s.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
			Set<String> sampleCodes = allSamples.stream().map(s -> s.code).collect(Collectors.toSet());
			Set<String> tags = getTagAssignFromContainerLife(containerCodes, ocuContent, projectCodes, sampleCodes);

			updateContentProperties(projectCodes, sampleCodes, containerCodes, tags, deletedPropertyCode, validation);
		});			
	}

	private Set<String> getTagAssignFromContainerLife(Set<String> containerCodes,
			Content ocuContent, 
			Set<String> projectCodes,  
			Set<String> sampleCodes) {
		Set<String> tags = null;
		DBQuery.Query query = DBQuery.in("code", containerCodes)
				.size("contents", 1) //only one content is very important because we targeting the lib container and not a pool after lib prep.
				.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
						.in("projectCode",  projectCodes)
						.exists("properties.tag"));

		MongoDBResult<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC);
		if (containersWithTag.size() > 0) {
			final Set<String> tmpTags = new TreeSet<>(); 
			containersWithTag.cursor.forEach(container -> {
				tmpTags.add(container.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			});
			tags = tmpTags;
		} else {
			// tags is null at this location
			//tags = null;
		}
		return tags;
	}

	private void updateContentProperties(Set<String> projectCodes, 
			Set<String> sampleCodes, 
			Set<String> containerCodes,
			Set<String> tags, 
			String deletedPropertyCode,
			ContextValidation validation) {
		
		sampleCodes.forEach(s->Logger.debug("Sample "+s));
		projectCodes.forEach(p->Logger.debug("Project "+p));
//		tags.stream().forEach(t->Logger.debug("Tag : "+t));
		if (tags != null)
			tags.forEach(t->Logger.debug("Tag : "+t));
		
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  DBQuery.in("code", containerCodes))
		.cursor
		.forEach(container -> {
			container.traceInformation.setTraceInformation(validation.getUser());
			container.contents.stream()
			.filter(content -> ((!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode))
					|| (null != tags  && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) 
					&&  tags.contains(content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))))
			.forEach(content -> {
				Logger.debug("Update container "+container.code);
				Query findContentQuery = ContentHelper.getContentQuery(container, content);
				content.properties = removeProperty(content.properties, deletedPropertyCode);		
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, findContentQuery, DBUpdate.set("contents.$", content));
			});			
		});

		
		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or(DBQuery.in("inputContainerCodes", containerCodes), DBQuery.in("outputContainerCodes", containerCodes)))
		.cursor.forEach(experiment -> {
			Logger.debug("Update experiment "+experiment.code);
			experiment.traceInformation.setTraceInformation(validation.getUser());
			experiment.atomicTransfertMethods.forEach(atm ->{
				atm.inputContainerUseds
				.stream()
				.filter(icu -> containerCodes.contains(icu.code))
				.map(icu -> icu.contents)
				.flatMap(List::stream)
				.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
				.forEach(content -> {
					content.properties = removeProperty(content.properties, deletedPropertyCode);							
				});
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.filter(ocu -> containerCodes.contains(ocu.code))							
					.map(ocu -> ocu.contents)
					.flatMap(List::stream)
					.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
					.forEach(content -> {
						content.properties = removeProperty(content.properties, deletedPropertyCode);							
					});
				}
			});				
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", experiment.code), 
					DBUpdate.set("atomicTransfertMethods", experiment.atomicTransfertMethods).set("traceInformation", experiment.traceInformation));	
		});	
		

		//update processes with new exp property values
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME,Process.class, 
				DBQuery.in("sampleOnInputContainer.containerCode", containerCodes).in("sampleOnInputContainer.sampleCode", sampleCodes).in("sampleOnInputContainer.projectCode", projectCodes))
		.cursor
		.forEach(process -> {
			
			if(!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				Logger.debug("Update process "+process.code);
				process.traceInformation.setTraceInformation(validation.getUser());
				process.sampleOnInputContainer.lastUpdateDate = new Date();
				process.sampleOnInputContainer.properties = removeProperty(process.sampleOnInputContainer.properties, deletedPropertyCode);	
				//MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code), 
						DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer).set("traceInformation", process.traceInformation));
			}
		});

		//update readsets with new exp property values
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", containerCodes).in("sampleCode", sampleCodes).in("projectCode", projectCodes))
		.cursor
		.forEach(readset -> {
			if(!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				Logger.debug("Update ReadSet "+readset.code);
				readset.traceInformation.setTraceInformation(validation.getUser());
				readset.sampleOnContainer.lastUpdateDate = new Date();
				readset.sampleOnContainer.properties = removeProperty(readset.sampleOnContainer.properties, deletedPropertyCode);	
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), 
						DBUpdate.set("sampleOnContainer", readset.sampleOnContainer).set("traceInformation", readset.traceInformation));
			}
		});	
	}

	private Map<String, PropertyValue> removeProperty(Map<String, PropertyValue> properties, 
			String deletedPropertyCode) {
		//delete remove properties
		properties.remove(deletedPropertyCode);
		return properties;
	}


	private void updateFirstExperiment(Experiment experiment, String deletedPropertyCode, ContextValidation validation)
	{
			Logger.debug("Update experiment "+experiment.code);
			experiment.traceInformation.setTraceInformation(validation.getUser());
			experiment.atomicTransfertMethods.forEach(atm ->{
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.map(ocu -> ocu.contents)
					.flatMap(List::stream)
					.forEach(content -> {
						content.properties = removeProperty(content.properties, deletedPropertyCode);							
					});
				}
			});				
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", experiment.code), 
					DBUpdate.set("atomicTransfertMethods", experiment.atomicTransfertMethods).set("traceInformation", experiment.traceInformation));	
		}
}
