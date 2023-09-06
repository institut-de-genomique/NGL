package workflows.process;

import static validation.common.instance.CommonValidationHelper.OBJECT_IN_DB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.instance.SampleHelper;
import play.Logger;
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
			Logger.info("Update content process properties with process properties for process "+process.code);
			
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
				.filter(content -> (
		    	        process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && (
		    	        (!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
		    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))	
		    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
		    	        || (null != tags && !content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
		    	        )))
				.forEach(content -> {
					content.processProperties = process.properties;
					// content.processComments = process.comments; NGL-2913: comments mis à jour par méthode dédiée updateContentProcessCommentsAttribut
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, ContentHelper.getContentQuery(container, content), DBUpdate.set("contents.$", content));
				});

			});
		} 
	}
	
	/* FDS NGL-2913
	 * propagate process comments
	 *  if process state=N  propagate to initial container only
	 *  if process state=IP propagate to "In work" outputContainers
	 *  if process state=F  no propagation
	 */
	public void updateContentProcessCommentsAttribute(ContextValidation validation, Process process) {
		// NON si le commentaire de processus a été supprimé il faut aussi répercuter la supression !!!
		//if(process.comments != null && process.comments.size() > 0){
			
			List<String> containerCodesToModify = new ArrayList<>();
			
			// si le process est à l'état NEW ne modifier que le container initial
			//NGL-2816 si processus à IP sans container de sortie
			if ("N".equals(process.state.code) || ("IP".equals(process.state.code) && process.outputContainerCodes==null)) {
				containerCodesToModify.add(process.inputContainerCode);
			}
			// si le process est l'état In Progress modifier les containers en cours de travail (s'il a des containers enfants)
			else if ("IP".equals(process.state.code) && process.outputContainerCodes != null) {
				
				List<String> lastContainerCodes = getProcessLastOutputContainerCodes(validation, process);
				containerCodesToModify.addAll(lastContainerCodes);
			} 
			// laisser liste vide pour un process terminé (state.code=F)
			
			//Validation Julie; la mise à jour des contents appropriés doit se baser sur les tags...
			//find tag inside inputContainer and all outputContainer if input does not have a tag
			Set<String> tags = getTagAssignFromProcessContainers(process);
			if (containerCodesToModify.size() > 0) {
				logger.debug("modify contents.procesComments for containers:"+containerCodesToModify);
				//System.out.println("FDS DEBUG modify contents.processComments for containers:"+containerCodesToModify);
				MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  DBQuery.in("code", containerCodesToModify))
				.cursor.forEach(container -> {
					//System.out.println("FDS DEBUG update processComments for container "+container.code);
					container.traceInformation.setTraceInformation(validation.getUser());
					container.contents.stream()
					// validation Julie=> ne mettre a jour que le content approprié=> ajouter filtres 
					.filter(content -> (
			    	        process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && (
			    	        (!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
			    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))	
			    	        || (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
			    	        || (null != tags && !content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
			    	        )))
					.forEach(content -> {
						//System.out.println("FDS DEBUG update only contents  for "+ content.sampleCode + " and "+ content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value);
						content.processComments = process.comments;	
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, ContentHelper.getContentQuery(container, content), DBUpdate.set("contents.$", content));
					});
				});
			}
		//}
	}
	
	/* FDS NGL-2913 
	 * get "In work" (ie last created) outputContainers codes
	 */
	public List<String> getProcessLastOutputContainerCodes(ContextValidation validation, Process process) {
		List<String> lastContainerCodes= new ArrayList<>();
		
		// il n'y qu'un seul cas ou un process a plusieurs derniers containers en cour de travail: creation de flowcell
		// Dans les autres cas quand une expérience crée plusieurs containers de sortie, les process sont dédoublés/clonés, 
		// =>chacun n'a qu'un seul dernier container...
		
		// .sort(  Sort.DESC).limit(1) permet de trouver le container le plus récent
		  List <Container> lastContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", process.outputContainerCodes))
				.sort("traceInformation.creationDate",Sort.DESC).limit(1).toList();
		
		//obligé de passé par une liste de containers... donc faut utiliser un iterator() meme si la liste ne contient qu'un élément
		Container lastContainer=lastContainers.iterator().next();
		
		//System.out.println("last container code:"+ lastContainer.code);
		//System.out.println("last container categoryCode:"+ lastContainer.categoryCode);
		
		if ( !"lane".equals(lastContainer.categoryCode) ) {
			// container autre que lane => le traiter directement
			lastContainerCodes.add(lastContainer.code);
		} else {
			// container lane => trouver tous les autres containers lane 
			// ET créées dans un intervalle de temps réduit (car il peut y avoir d'autres flowcells/lanes créés précédemment 
			// dans le même process [cas d'errreurs])

			Date laneDate=lastContainer.traceInformation.creationDate;
			// intervalle de création: 2 secondes avant
			Date limitDate=DateUtils.addSeconds(laneDate, -2);	
			
			//System.out.println("last container creation timestamp="+ laneDate);
			//System.out.println("> limitDate "+limitDate);
			
			List <Container> lastLanes = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
					DBQuery.in("code", process.outputContainerCodes).is("categoryCode","lane" )
					.greaterThan("traceInformation.creationDate", limitDate)).toList();
			for( Container lane: lastLanes) {
				//System.out.println("last lane code:"+ lane.code+ " date:"+lane.traceInformation.creationDate);
				lastContainerCodes.add(lane.code);
			}
		}
		
		return lastContainerCodes;
	}

	/*
	 * Find the tag assign during process or existing at the beginning of process
	 * 
	 * @param process
	 * @return
	 */
	public Set<String> getTagAssignFromProcessContainersUpdateReporting(Process process) {
		//NGL-2712 adaptation pour prise en charge des tags secondaire 
		Set<String> tags = null;
		//1. Si container initial du processus avec tag I sans tag II alors filtre ReadSet uniquement sur tag I (concatenation "_tagI")
		if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)){
			tags = new HashSet<>();
			tags.add("_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());			
			//2. Si container initial du processus avec tag I et tag II alors filtre ReadSet sur tag I et tag II (concatenation "tagII_tagI")
		}else if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)){
			tags = new HashSet<>();
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			//3.Si container initiale du processus sans tag I et avec tag II alors recherche du tag I sur les containers enfants (car tag I est obligatoire avec un tag II)
		}else if(!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && process.outputContainerCodes != null && process.outputContainerCodes.size() > 0){
			//3.1 Recherche containers enfants avec un seul contenu + sampleCode processus + projectCode processus + tag I et tag II
			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
					.size("contents", 1)  
					.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
							.in("projectCode",  process.projectCodes)
							.is("properties.secondaryTag.value", process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString())
							.exists("properties.tag"));
			List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
			if(containersWithDoubleTag.size() > 0){
				tags = new HashSet<>();
				tags.add(containersWithDoubleTag.iterator().next().contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+containersWithDoubleTag.iterator().next().contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}else{
				//3.2 Recherche container enfant avec plusieurs contenus et qui contient un contenu sampleCode processus + projectCode processus + tag II de l'étape 3 et tag I (concatenation "tagII_tagI")
				tags = getDoubleTagForContainerMultipleContent(process.outputContainerCodes, process.sampleCodes, process.projectCodes, process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString());
			}
			//4. Si container initiale sans tag I et sans tagII
		}else if(process.outputContainerCodes != null && process.outputContainerCodes.size() > 0){
			//4.1 Recherche container enfant avec un seul contenu + sampleCode processus + projectCode processus + tag I et tag II concatenation "tagII_tagI"
			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
					.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
							.in("projectCode",  process.projectCodes)
							.exists("properties.tag").exists("properties.secondaryTag"));
			List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
			if(containersWithDoubleTag.size() > 0){
				tags = new HashSet<>();
				tags.add(containersWithDoubleTag.iterator().next().contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+containersWithDoubleTag.iterator().next().contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}else{
				//4.2 Recherche container enfant avec un seul contenu + sampleCode processus + projectCode processus + tag I et sans tag II concatenation "_tagI"
				query = DBQuery.in("code",process.outputContainerCodes)
						.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
						.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
								.in("projectCode",  process.projectCodes)
								.exists("properties.tag").notExists("properties.secondaryTag"));
				List<Container> containersWithSimpleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
				if(containersWithSimpleTag.size() > 0){
					tags = new HashSet<>();
					tags.add("_"+containersWithSimpleTag.iterator().next().contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
				}else{
					//4.3 Recherche container enfant avec un seul contenu + sampleCode processus + projectCode processus + sans tag I et avec tag II 
					query = DBQuery.in("code",process.outputContainerCodes)
							.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
							.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
									.in("projectCode",  process.projectCodes)
									.notExists("properties.tag").exists("properties.secondaryTag"));
					List<Container> containersWithSecondaryTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
					if(containersWithSecondaryTag.size() > 0){
						//Get first container
						Container containerWithOnlySecondaryTag = containersWithSecondaryTag.iterator().next();
						//4.3 puis recherche container avec plusieurs contenus qui contient un contenu sampleCode processus + projectCode processus + tagII et tag I concatenation "tagII_tagI"
						tags = getDoubleTagForContainerMultipleContent(process.outputContainerCodes, process.sampleCodes, process.projectCodes, containerWithOnlySecondaryTag.contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString());
					}
				}
			}
		}
		return tags;
	}
	/**
	 * Search value of primary tag from a secondaryTag in a set of containerCodes 
	 * @param containerCodes
	 * @param sampleCodes
	 * @param projectCodes
	 * @param secondaryTagValue
	 * @return concatenate value of secondaryTag and primaryTag
	 */
	private Set<String> getDoubleTagForContainerMultipleContent(Set<String> containerCodes, Set<String> sampleCodes, Set<String> projectCodes, String secondaryTagValue)
	{
		Set<String> tags = null;
		DBQuery.Query query = DBQuery.in("code",containerCodes)
				.elemMatch("contents", DBQuery.in("sampleCode", sampleCodes)
						.in("projectCode",  projectCodes)
						.is("properties.secondaryTag.value", secondaryTagValue)
						.exists("properties.tag"));
		List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
		if(containersWithDoubleTag.size() > 0){
			tags = new HashSet<>();
			//Get first container
			Container container = containersWithDoubleTag.iterator().next();
			for(Content content : container.contents){
				if(sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) && content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString().equals(secondaryTagValue)){
					tags.add(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
				}
			}
		}
		return tags;
	}

	/*
	 * Find the tag assign during process or existing at the beginning of process
	 * @param process
	 * @return
	 */
	public Set<String> getTagAssignFromProcessContainers(Process process) {
		Set<String> tags = new HashSet<String>();
		//New algo NGL-2816
		//1. input container tag I + tag II STOP search
		if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && 
				process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).getValue().toString()+"_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).getValue().toString());
			//2.input container tag I + no tag II STOP search 
		}else if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && 
				!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)) {
			tags.add("_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).getValue().toString());
		}else if(process.outputContainerCodes!=null && process.outputContainerCodes.size()>0){
			//search in container treeOfLife 
			//3. Search container  one content only tagI => STOP search
			DBQuery.Query query = DBQuery.in("code",process.outputContainerCodes)
					.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
					.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
							.in("projectCode",  process.projectCodes)
							.exists("properties.tag").notExists("properties.secondaryTag"));
			List<Container> containersWithTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
			if(containersWithTag.size() > 0){
				//STOP SEARCH
				//Get first container
				Container containerWithTag = containersWithTag.iterator().next();
				tags.add("_"+containerWithTag.contents.get(0).properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
			}else {
				//4.a Recherche tag II
				//4.a.1 Content courant tagII
				String secondaryTag = null;
				if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) &&
						!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) {
					secondaryTag=process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
					tags.add(secondaryTag+"_");
				}else {
					//4.a.2 Container enfant un seul content tagII
					query = DBQuery.in("code",process.outputContainerCodes)
							.size("contents", 1)  //only one content is very important because we targeting the lib container and not a pool after lib prep.
							.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
									.in("projectCode",  process.projectCodes)
									.notExists("properties.tag").exists("properties.secondaryTag"));
					List<Container> containersWithSecondTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
					if(containersWithSecondTag.size()>0) {
						Container containerWithSecondTag = containersWithSecondTag.iterator().next();
						secondaryTag=containerWithSecondTag.contents.get(0).properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString();
						tags.add(secondaryTag+"_");
					}
				}
				//4.b Recherche container enfant plusieurs content avec tagI identique et tagII du 4.a pour avoir combinaison tagII_tagI
				if(secondaryTag!=null) {
					query = DBQuery.in("code",process.outputContainerCodes)
							.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
									.in("projectCode",  process.projectCodes)
									.is("properties.secondaryTag.value", secondaryTag)
									.exists("properties.tag"));
					List<Container> containersWithDoubleTag = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query).sort("traceInformation.creationDate",Sort.ASC).limit(1).toList();
					if(containersWithDoubleTag.size() > 0){
						//Check same all tagI 
						for(Container container : containersWithDoubleTag) {
							if(checkSameTagInContainer(container)) {
								for(Content content : container.contents){
									if(process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString().equals(secondaryTag)){
										tags.add(secondaryTag+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString());
									}
								}
								break;
							}
						}
						
					}
				}
			}
		}else if(process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) &&
				!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) {
			tags.add(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_");
		} 
		return tags;
	}
	
	private boolean checkSameTagInContainer(Container container)
	{
		Set<String> tags = container.contents.stream()
		.filter(content->content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
		.map(content->content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString())
		.collect(Collectors.toSet());
		
		if(tags.size()==1)
			return true;
		else
			return false;
	}

	public void updateContentPropertiesWithContentProcessProperties(ContextValidation validation, Process process) {
		//update output container with new process property values
		Set<String> propertyCodes = getProcessesPropertyDefinitionCodes(process, Level.CODE.Content);

		Set<String> outputContainerCodes = process.outputContainerCodes;
		if(null != outputContainerCodes && outputContainerCodes.size() > 0 
				&& process.properties != null && process.properties.size() > 0 && propertyCodes.size() > 0){
			Logger.info("Update content process properties with process properties content for process "+process.code);
			
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

				InstanceHelpers._updateContentProperties(projectCodes, sampleCodes, outputContainerCodes, tags, updatedProperties,
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
