package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.instance.AbstractContainerUsed;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import workflows.container.ContentHelper;
/**
 * Script propagation d'une propriété experiment (ContainerIn ou ContainerOut) passé à un niveau content
 * Script crée pour le ticket NGL-2958
 * 
 * ATTENTION script uniquement pour des experiences de type OneToOne
 * ATTENTION les tags secondaires ne sont pas pris en compte
 * 
 * Arguments : 
 * typeCodes : typeCode des expériences possèdant la propriété
 * newPropertyCode : code de la propriété à propager
 * typeContainer : niveau de la propriété ContainerIn (valeur=IN) ou ContainerOut (valeur=OUT)
 * @author ejacoby
 *
 */
public class UpdateExperimentPropertiesContent extends ScriptWithArgs<UpdateExperimentPropertiesContent.Args>{

	final String user = "ngl-support";

	public static class Args {
		public String[] typeCodes;
		public String newPropertyCode;
		public String typeContainer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Args args) throws Exception {

		ContextValidation validation = ContextValidation.createUpdateContext(user);

		validation.putObject("RecapContainer", new ArrayList<String>());
		validation.putObject("RecapExperiment", new ArrayList<String>());
		validation.putObject("RecapProcess", new ArrayList<String>());
		validation.putObject("RecapReadSet", new ArrayList<String>());
		validation.putObject("RecapNoUpdate", new ArrayList<String>());
		//Add Header Recap
		((List<String>)validation.getObject("RecapContainer")).add("Code experiment,code container,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapExperiment")).add("Code experiment,code experiment updated,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapProcess")).add("Code experiment,code process,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapReadSet")).add("Code experiment,code readSet,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapNoUpdate")).add("Code experiment,code no update");
		//Recherche des experiences selon typeCode
		for(int i=0; i<args.typeCodes.length; i++) {
			Logger.debug("START update typeCode "+args.typeCodes[i]);
			List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", args.typeCodes[i])).toList();
			Logger.debug("Nb experiment to update "+experiments.size());
			int nbExp=experiments.size();
			int nb=1;
			for(Experiment experiment : experiments) {
				Logger.debug("update experiment "+experiment.code+" "+nb+"/"+nbExp);
				//Recuperer la propriete a mettre a jour
				experiment.atomicTransfertMethods.forEach(atm -> {
					//Uniquement pour les OneToOne
					if(atm instanceof OneToOneContainer) {
						PropertyValue propertyToAdd = null;
						if(args.typeContainer.equals("IN")) {
							propertyToAdd = atm.inputContainerUseds.iterator().next().experimentProperties.get(args.newPropertyCode);
						}else if(args.typeContainer.equals("OUT") && atm.outputContainerUseds!=null) {
							propertyToAdd = atm.outputContainerUseds.iterator().next().experimentProperties.get(args.newPropertyCode);
						}
						if(propertyToAdd!=null) {
							final PropertyValue propertyValue =propertyToAdd;
							if (ExperimentCategory.CODE.qualitycontrol.toString().equals(experiment.categoryCode)) {
								atm.inputContainerUseds
								.stream()
								.forEach(icu -> updateContainerContentPropertiesInCascading(experiment.code, validation, icu, args.newPropertyCode, propertyValue));
							} else if (atm.outputContainerUseds != null) {
								atm.outputContainerUseds
								.stream()
								.forEach(ocu -> updateContainerContentPropertiesInCascading(experiment.code, validation, ocu, args.newPropertyCode, propertyValue));					
							}
						}else {
							Logger.debug("CANNOT update experiment no property found "+experiment.code);
							((List<String>)validation.getObject("RecapNoUpdate")).add(experiment.code+",no property found");
						}
					}else {
						Logger.error("CANNOT update experiment "+experiment.code+" not OneToOne");
						((List<String>)validation.getObject("RecapNoUpdate")).add(experiment.code+",not OneToOne");
					}
				});		
				nb++;
			}
		}
		ProcessPropertiesContentHelper.createExcelFileRecap(validation);
	}

	private void updateContainerContentPropertiesInCascading(String codeExp, ContextValidation validation, AbstractContainerUsed acu, String newPropertyCode, PropertyValue newPropertyValue) {
		Logger.debug("Get container must be udpated");
		List<Container> containerMustBeUpdated = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.or(DBQuery.is("code", acu.code), DBQuery.regex("treeOfLife.paths", Pattern.compile(","+acu.code+"$|,"+acu.code+","))))
				.toList();

		Set<String> containerCodes = containerMustBeUpdated.stream().map(c -> c.code).collect(Collectors.toSet());

		acu.contents.forEach(ocuContent -> {
			Logger.debug("Update content "+ocuContent.sampleCode);
			List<Sample> allSamples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,  
					DBQuery.or(DBQuery.is("code", ocuContent.sampleCode), DBQuery.regex("life.path", Pattern.compile(","+ocuContent.sampleCode+"$|,"+ocuContent.sampleCode+","))))
					.toList();

			Set<String> projectCodes = allSamples.stream().map(s -> s.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
			Set<String> sampleCodes = allSamples.stream().map(s -> s.code).collect(Collectors.toSet());
			Set<String> tags = getTagAssignFromContainerLife(containerCodes, ocuContent, projectCodes, sampleCodes);

			updateContentProperties(codeExp, projectCodes, sampleCodes, containerCodes, tags, newPropertyCode, newPropertyValue, validation);

		});			
	}

	private Set<String> getTagAssignFromContainerLife(Set<String> containerCodes,
			Content ocuContent, 
			Set<String> projectCodes,  
			Set<String> sampleCodes) {
		Set<String> tags = null;
		if (ocuContent.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)){
			tags = new TreeSet<>();
			tags.add(ocuContent.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
		} else {
			DBQuery.Query query = DBQuery.in("code", containerCodes)
					.size("contents", 1) // only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
							.in("projectCode",  projectCodes)
							.exists("properties.tag"));

			MongoDBResult<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC);
			if (containersWithTag.size() > 0) {
				final Set<String> tmpTags = new TreeSet<>(); 
				containersWithTag.cursor.forEach(container -> 
				tmpTags.add(container.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()));
				tags = tmpTags;
			} else {
				// tags is null at this location
				//				tags = null;
			}
		}
		return tags;
	}

	@SuppressWarnings("unchecked")
	private void updateContentProperties(String codeExp, Set<String> projectCodes, 
			Set<String> sampleCodes, 
			Set<String> containerCodes,
			Set<String> tags, 
			String newPropertyCode,
			PropertyValue newPropertyValue,
			ContextValidation validation) {
		Logger.debug("Start update container");
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.in("code", containerCodes))
		.cursor
		.forEach(container -> {
			container.traceInformation.setTraceInformation(validation.getUser());
			container.contents
			.stream()
			.filter(content -> ((!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode))
					|| (null != tags  && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) 
					&&  tags.contains(content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))))
			.forEach(content -> {
				Query findContentQuery = ContentHelper.getContentQuery(container, content);
				((List<String>)validation.getObject("RecapContainer")).add(codeExp+","+container.code+","+newPropertyCode+","+newPropertyValue.getValue().toString());
				content.properties.put(newPropertyCode, newPropertyValue);
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, findContentQuery, DBUpdate.set("contents.$", content));
			});			
		});
		Logger.debug("Start update experiment");
		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.or(DBQuery.in("inputContainerCodes", containerCodes),
						DBQuery.in("outputContainerCodes", containerCodes)))
		.cursor
		.forEach(experiment -> {
			experiment.traceInformation.setTraceInformation(validation.getUser());
			experiment.atomicTransfertMethods
			.forEach(atm -> {
				atm.inputContainerUseds
				.stream()
				.filter(icu -> containerCodes.contains(icu.code))
				.map(icu -> icu.contents)
				.flatMap(List::stream)
				.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
				.forEach(content -> {
					content.properties.put(newPropertyCode, newPropertyValue);
				});
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.filter(ocu -> containerCodes.contains(ocu.code))							
					.map(ocu -> ocu.contents)
					.flatMap(List::stream)
					.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
					.forEach(content -> {
						content.properties.put(newPropertyCode, newPropertyValue);
					});
				}
			});		
			((List<String>)validation.getObject("RecapExperiment")).add(codeExp+","+experiment.code+","+newPropertyCode+","+newPropertyValue.getValue().toString());
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", experiment.code), 
					DBUpdate.set("atomicTransfertMethods", experiment.atomicTransfertMethods).set("traceInformation", experiment.traceInformation));	
		});	
		Logger.debug("Start update process");
		//update processes with new exp property values
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME,Process.class, 
				DBQuery.in("sampleOnInputContainer.containerCode", containerCodes).in("sampleOnInputContainer.sampleCode", sampleCodes).in("sampleOnInputContainer.projectCode", projectCodes))
		.cursor
		.forEach(process -> {
			if(!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				process.traceInformation.setTraceInformation(validation.getUser());
				process.sampleOnInputContainer.lastUpdateDate = new Date();
				process.sampleOnInputContainer.properties.put(newPropertyCode, newPropertyValue);
				((List<String>)validation.getObject("RecapProcess")).add(codeExp+","+process.code+","+newPropertyCode+","+newPropertyValue.getValue().toString());
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code), 
						DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer).set("traceInformation", process.traceInformation));
			}
		});

		Logger.debug("Start update readSet");
		//update readsets with new exp property values
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", containerCodes).in("sampleCode", sampleCodes).in("projectCode", projectCodes))
		.cursor
		.forEach(readset -> {
			if(!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)
					|| (null != tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
					&&  tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))){
				readset.traceInformation.setTraceInformation(validation.getUser());
				readset.sampleOnContainer.lastUpdateDate = new Date();
				readset.sampleOnContainer.properties.put(newPropertyCode, newPropertyValue);
				((List<String>)validation.getObject("RecapReadSet")).add(codeExp+","+readset.code+","+newPropertyCode+","+newPropertyValue.getValue().toString());
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), 
						DBUpdate.set("sampleOnContainer", readset.sampleOnContainer).set("traceInformation", readset.traceInformation));
			}
		});	
	}
}
