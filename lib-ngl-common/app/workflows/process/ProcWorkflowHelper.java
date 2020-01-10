package workflows.process;

import static validation.common.instance.CommonValidationHelper.OBJECT_IN_DB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.instance.SampleHelper;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import workflows.container.ContWorkflows;
import workflows.container.ContentHelper;


@Singleton
public class ProcWorkflowHelper {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ProcWorkflowHelper.class);

	public final ContWorkflows contWorkflows;
	public final ContentHelper contentHelper;
	
	@Inject
	public ProcWorkflowHelper(ContWorkflows contWorkflows, ContentHelper contentHelper) {
		this.contWorkflows = contWorkflows;
		this.contentHelper = contentHelper;
	}
	
	public void updateInputContainerToStartProcess(ContextValidation contextValidation, Process process) {
		ProcessType processType = ProcessType.find.get().findByCode(process.typeCode);
		String voidExpTypeCode = processType.voidExperimentType.code;
		
		DBQuery.Query query = getInputContainerQuery(process);
		
		Builder builder = DBUpdate.addToSet("processCodes", process.code)
				.addToSet("processTypeCodes", process.typeCode);
				
		if(process.properties != null && process.properties.size() > 0){
			builder.set("contents.$.processProperties", process.properties);
		}
		
		if(process.comments != null && process.comments.size() > 0){
			builder.set("contents.$.processComments", process.comments);
		}
		
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
				query,
				builder);	
		
		
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
				DBQuery.is("code",process.inputContainerCode).or(DBQuery.notExists("fromTransformationTypeCodes"),DBQuery.size("fromTransformationTypeCodes", 0)),
				DBUpdate.addToSet("fromTransformationTypeCodes", voidExpTypeCode));
		
		
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,process.inputContainerCode);
		State nextState = new State();
//		nextState.code  = contWorkflows.getContainerStateFromExperimentCategory(processType.firstExperimentType.category.code);
		nextState.code  = ExperimentCategory.getContainerStateFromExperimentCategory(processType.firstExperimentType.category.code);
		nextState.user  = contextValidation.getUser();
		
		contextValidation.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "workflow");
		contextValidation.putObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, Boolean.TRUE);
		contWorkflows.setState(contextValidation, container, nextState, "workflow", true);
	}

	
	public void updateContentProcessPropertiesAttribute(ContextValidation validation, Process process) {
		if(process.properties != null && process.properties.size() > 0){
			
			//1 find tag inside inputContainer and all outputContainer if input does not have a tag
			Set<String> tags = getTagAssignFromProcessContainers(process);
			//2 find container with processProperties and tag if needed
			//DBQuery.Query query = getChildContainerQueryForProcessProperties(process, tag);			
			//3 update processProperties
			/*
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					query,DBUpdate.set("contents.$.processProperties", process.properties));
			*/
			// GA: Problem when property disappeared after pool fusion, we had a new property
			List<String> containerCodes = new ArrayList<>();
			containerCodes.add(process.inputContainerCode);
			if (process.outputContainerCodes != null) {
				containerCodes.addAll(process.outputContainerCodes);
			}
			
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  DBQuery.in("code", containerCodes).elemMatch("contents",DBQuery.exists("processProperties")))
			.cursor.forEach(container -> {
				container.traceInformation.setTraceInformation(validation.getUser());
				container.contents.stream()
					.filter(content -> ((process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && !content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
							|| (null != tags && process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) 
									&&  tags.contains(content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value))))
					.forEach(content -> {
						content.processProperties = process.properties;
						content.processComments = process.comments;	
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, ContentHelper.getContentQuery(container, content), DBUpdate.set("contents.$", content));
					});
					
			});
			
		}
	}
	
	/*
	 * Find the tag assign during process or existing at the beginning of process
	 * @param process
	 * @return
	 */
	public Set<String> getTagAssignFromProcessContainers(Process process) {
		Set<String> tags = null;
		if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)){
			tags = new TreeSet<>();
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());			
		}else if(process.outputContainerCodes != null && process.outputContainerCodes.size() > 0){
			
			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
						.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
						.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
													.in("projectCode",  process.projectCodes)
													.exists("properties.tag"));
			
			MongoDBResult<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC);
			if(containersWithTag.size() > 0){
				tags = new TreeSet<>();
				tags.add(containersWithTag.cursor.next().contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}
		}
		return tags;
	}


	public void updateContentPropertiesWithContentProcessProperties(ContextValidation validation, Process process) {
			//update output container with new process property values
		Set<String> propertyCodes = getProcessesPropertyDefinitionCodes(process, Level.CODE.Content);
		
		Set<String> outputContainerCodes = process.outputContainerCodes;
		if(null != outputContainerCodes && outputContainerCodes.size() > 0 
				&& process.properties != null && process.properties.size() > 0 && propertyCodes.size() > 0){
			Process oldProcess = (Process) validation.getObject(OBJECT_IN_DB);
			Map<String, Pair<PropertyValue,PropertyValue>> updatedProperties = InstanceHelpers.getUpdatedPropertiesForSomePropertyCodes(propertyCodes, oldProcess.properties, process.properties);
			Set<String> deletedPropertyCodes = InstanceHelpers.getDeletedPropertiesForSomePropertyCodes(propertyCodes, oldProcess.properties, process.properties);
			logger.debug("updatedProperties " + updatedProperties);
			logger.debug("deletedPropertyCodes " + deletedPropertyCodes);
			
			if(updatedProperties.size() > 0 || deletedPropertyCodes.size() > 0){
				//1 find tag inside inputContainer and all outputContainer if input does not have a tag
				Set<String> sampleCodes = process.sampleCodes;
				Set<String> projectCodes = process.projectCodes;
				Set<String> tags = getTagAssignFromProcessContainers(process);
				
				logger.debug("UpdateProperties for tag "+tags);
				
				//2 update content properties
				
				InstanceHelpers.updateContentProperties(projectCodes, sampleCodes, outputContainerCodes, tags, updatedProperties,
						deletedPropertyCodes, validation);
						
			}
		}
	}

	private Set<String> getProcessesPropertyDefinitionCodes(Process process, Level.CODE level) {		
		ProcessType processType = ProcessType.find.get().findByCode(process.typeCode);
		return processType.getPropertyDefinitionByLevel(level)
				.stream()
				.map(pd -> pd.code)
				.collect(Collectors.toSet());		
	}
		
	/*
	 * Query to retrieve container and content (using tag if exist)
	 * @param process
	 * @return
	 */
	private DBQuery.Query getInputContainerQuery(Process process) {
		DBQuery.Query query = DBQuery.is("code",process.inputContainerCode);
		if (process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) {
			query.elemMatch("contents", DBQuery.is("sampleCode", process.sampleOnInputContainer.sampleCode)
												.is("projectCode",  process.sampleOnInputContainer.projectCode)
												.is("properties.tag.value", process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value));
			
		} else {
			query.elemMatch("contents", DBQuery.is("sampleCode", process.sampleOnInputContainer.sampleCode).is("projectCode",  process.sampleOnInputContainer.projectCode));
		}
		return query;
	}

	public void updateSampleOnContainer(ContextValidation validation, Process process) {
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,process.inputContainerCode);
		if(container.contents.size() == 1 && (process.sampleOnInputContainer == null || "IW-C".equals(process.state.code))){
			process.sampleCodes = SampleHelper.getSampleParent(container.contents.get(0).sampleCode);
			process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
	
			process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(container.contents.get(0), container);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code)
					,DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer)
								.set("projectCodes", process.projectCodes)
								.set("sampleCodes", process.sampleCodes));
		} else if(container.contents.size() > 1 && process.sampleOnInputContainer == null){
			logger.error("container is a pool, sampleOnInputContainer cannot be updated : "+process.code);
		}
	}

	public void setIWCConfiguration(ContextValidation validation, Process process) {
		String sampleCode = process.code.split("_"+process.typeCode.toUpperCase()+"_")[0];
		if(sampleCode != null){
			process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(sampleCode);	
			process.inputContainerCode = null;
			process.inputContainerSupportCode = null;
			process.sampleCodes.clear();
			process.sampleCodes.add(process.sampleOnInputContainer.sampleCode);
			process.projectCodes.clear();
			process.projectCodes = SampleHelper.getProjectParent(process.sampleCodes);
			//WARNING no set to null because in this case we don't want to keep the N state. if you set null the history contains previous state @see Workflow.updateHistoricalNextState
			process.state.historical = new HashSet<>(0); 
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code)
					,DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer)
								.unset("inputContainerCode")
								.unset("inputContainerSupportCode")
								.unset("state.historical")
								.set("sampleCodes", process.sampleCodes)
								.set("projectCodes", process.projectCodes));
		}else{
			logger.error("cannot retrieve sample code for process "+process.code);
		}
	}

}
