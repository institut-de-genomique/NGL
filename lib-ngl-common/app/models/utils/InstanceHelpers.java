package models.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.parameter.index.Index;
import models.laboratory.processes.instance.Process;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;
import models.sra.submit.sra.instance.Readset;
import validation.ContextValidation;
import validation.IValidation;
import workflows.container.ContentHelper;

public class InstanceHelpers {

	public static final play.Logger.ALogger logger = play.Logger.of(InstanceHelpers.class);


	/**
	 * Construct a new map with lazily ({@link MapUtils#lazyMap}) initialized
	 * property values ({@link PropertySingleValue}). 
	 * @return lazy value map
	 */
	public static Map<String, PropertyValue> getLazyMapPropertyValue() {
		// GA: comment je sais quel est le type on doit mettre
		return MapUtils.lazyMap(new HashMap<>(), mapKey -> new PropertySingleValue());
	}

	/**
	 * Current user from HTTP context.
	 * @return current user login
	 */
	public static String getUser() {
		return fr.cea.ig.authentication.Authentication.getUser();
	}

	/**
	 * Add a comment to the provided possibly empty list (null) of comments,
	 * this updates the provided list if it is not null.
	 * @param comments comment list to update
	 * @param comment  text of comment to add
	 * @param user     comment creator user identifier
	 * @return         updated or created list
	 */
	public static List<Comment> addComment(List<Comment> comments, String comment, String user) {
		Comment newComment = new Comment(comment, user);
		if (comments == null) 
			return Arrays.asList(newComment);
		comments.add(newComment);
		return comments;
	}


	/**
	 * Cleans a comment list by removing the empty comments and assigning the
	 * comments that have no creator to the validation context user. 
	 * @param contextValidation validation context
	 * @param comments          list of comments to clean
	 * @return                  cleaned comment list
	 */
	public static List<Comment> updateComments(ContextValidation contextValidation, List<Comment> comments) {
		if (comments == null)
			return new ArrayList<>(0);
		// this run a parallel stream while the most expensive call is
		// generateExperimentCommentCode which is synchronized.
		return comments.parallelStream()
				.filter(c -> StringUtils.isNotBlank(c.comment))
				.map(c -> {
					c.comment = c.comment.trim();
					if (c.createUser == null) {
						c.createUser   = contextValidation.getUser();
						c.creationDate = new Date();
						c.code         = CodeHelper.getInstance().generateExperimentCommentCode(c);
					}
					return c;
				}).collect(Collectors.toList());
	}

	/**
	 * This is almost {@link TraceInformation#setTraceInformation(String)}, this may behave
	 * a bit differently in fringe cases but should be compatible enough to be replaced.
	 * @param traceInformation trace information to update
	 * @param user             user
	 * @deprecated use {@link TraceInformation#setTraceInformation(String)}
	 */
	@Deprecated

	public static void updateTraceInformation(TraceInformation traceInformation, String user) {
		if (traceInformation.createUser == null) {
			traceInformation.createUser = user;
		} else {
			traceInformation.modifyUser = user;
		}
		if (traceInformation.creationDate == null) {
			traceInformation.creationDate = new Date();
		} else {
			traceInformation.modifyDate = new Date();
		}
	}


	/**
	 * Update or create a trace information.
	 * @param traceInformation optional trace information to update
	 * @param user             user
	 * @return                 updated object if one was provided otherwise a created object
	 * @deprecated use {@link TraceInformation#updateOrCreateTraceInformation(TraceInformation, String)}
	 */
	@Deprecated
	public static TraceInformation getUpdateTraceInformation(TraceInformation traceInformation, String user) {
		return TraceInformation.updateOrCreateTraceInformation(traceInformation, user);
	}

	// -------------------------------------------------------------------------------------------

	public static void copyPropertyValueFromPropertiesDefinition(List<PropertyDefinition> propertyDefinitions,
			Map<String, PropertyValue> propertiesInput, 
			Map<String, PropertyValue> propertiesOutPut) {
		// LOGIC: it is a meaningless creation and could as well return
		//        immediately without doing anything.
		if (propertiesOutPut == null) 
			propertiesOutPut = new HashMap<>();
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			PropertyValue propertyValue = propertiesInput.get(propertyDefinition.code);
			if (propertyValue != null) {
				propertiesOutPut.put(propertyDefinition.code, propertyValue);
			}
		}
	}

	public static Set<String> getDeletedPropertyDefinitionCode(List<PropertyDefinition> propertyDefinitions, Map<String, PropertyValue> propertiesInput) {
		return propertyDefinitions.stream()
				.filter(pd -> !propertiesInput.containsKey(pd.code))
				.map(pd -> pd.code)
				.collect(Collectors.toSet());			
	}

	//	public static DBObject save(String collectionName, IValidation obj, ContextValidation contextError, Boolean keepRootKeyName) {
	//		ContextValidation localContextError = new ContextValidation(contextError.getUser());
	//		localContextError.setMode(contextError.getMode());
	//		if (keepRootKeyName) {
	//			localContextError.addKeyToRootKeyName(contextError.getRootKeyName());
	//		}
	//		localContextError.setContextObjects(contextError.getContextObjects());
	//		if (obj != null) {
	//			obj.validate(localContextError);
	//		} else {
	//			throw new IllegalArgumentException("missing object to validate");
	//		}
	//
	//		if (localContextError.errors.size() == 0) {
	//			return MongoDBDAO.save(collectionName, (DBObject) obj);
	//		} else {
	//			contextError.errors.putAll(localContextError.errors);
	//			logger.info("error(s) on output :: " + contextError.errors.toString());
	//			return null;
	//		}
	//	}

	// -------------------------------------------------------------------------
	// arguments reordered

	/**
	 * Light generic API implementation (roughly equivalent to {@link GenericAPI#update(DBObject, String)}). 
	 * @param <T>              type of object to save
	 * @param collectionName   MongoDB collection name
	 * @param obj              object to save
	 * @param contextError     validation context
	 * @param keepRootKeyName  is the parameter validation context kept during validation ?
	 * @return                 object saved in the database
	 * @deprecated use {@link #save(ContextValidation, String, DBObject, boolean)}
	 */
	@Deprecated
	public static <T extends DBObject & IValidation> T save(String collectionName, T obj, ContextValidation contextError, boolean keepRootKeyName) {
		return InstanceHelpers.save(contextError, collectionName, obj, keepRootKeyName);
	}

	/**
	 * Light generic API implementation (roughly equivalent to {@link GenericAPI#update(DBObject, String)}). 
	 * @param <T>              type of object to save
	 * @param contextError     validation context
	 * @param collectionName   MongoDB collection name
	 * @param obj              object to save
	 * @param keepRootKeyName  is the parameter validation context kept during validation ?
	 * @return                 object saved in the database
	 */
	public static <T extends DBObject & IValidation> T save(ContextValidation contextError, String collectionName, T obj, boolean keepRootKeyName) {
		if (obj == null)
			throw new IllegalArgumentException("missing object to validate");
		ContextValidation localContextError = ContextValidation.createUndefinedContext(contextError.getUser());
		localContextError.setMode(contextError.getMode());
		if (keepRootKeyName) {
			localContextError.addKeyToRootKeyName(contextError.getRootKeyName());
		}
		localContextError.setContextObjects(contextError.getContextObjects());
		obj.validate(localContextError);
		if (localContextError.getErrors().size() == 0) {
			return MongoDBDAO.save(collectionName, obj);
		} else {
			contextError.getErrors().putAll(localContextError.getErrors());
			logger.info("error(s) on output :: " + contextError.getErrors().toString());
			return null;
		}
	}

	// -------------------------------------------------------------------------
	public static <T extends DBObject & IValidation> T save(String collectionName, T obj, ContextValidation contextError) {
		return save(contextError, collectionName, obj, false);
	}

	public static <T extends DBObject & IValidation> List<T> save(String collectionName, List<T> objects, ContextValidation contextErrors) {
		List<T> dbObjects = new ArrayList<>();
		for (T object : objects) {
			T result = save(collectionName, object, contextErrors);
			if (result != null)
				dbObjects.add(result);
		}
		return dbObjects;
	}

	public static <T extends DBObject& IValidation> List<T> save(String collectionName, List<T> objects, ContextValidation contextErrors, boolean keepRootKeyName) {
		List<T> dbObjects = new ArrayList<>();
		for (T object : objects) {
			T result = save(contextErrors, collectionName, object, keepRootKeyName);
			if (result != null) 
				dbObjects.add(result);
		}
		return dbObjects;
	}

	public static SampleOnContainer getSampleOnContainer(ReadSet readSet) {
		// 1 retrieve containerSupportCode from Run
		String containerSupportCode = getContainerSupportCode(readSet);
		Container container = getContainer(readSet, containerSupportCode);
		if (container != null) {
			Content content = getContent(container, readSet);
			if (content != null) {
				SampleOnContainer sampleContainer = convertToSampleOnContainer(readSet, containerSupportCode, container, content);
				return sampleContainer;
			}
		}
		return null;
	}

	public static SampleOnInputContainer getSampleOnInputContainer(Content content, Container container) {

		SampleOnInputContainer sampleOnInputContainer = new SampleOnInputContainer();
		sampleOnInputContainer.projectCode        = content.projectCode;
		sampleOnInputContainer.sampleCode         = content.sampleCode;
		sampleOnInputContainer.sampleCategoryCode = content.sampleCategoryCode;
		sampleOnInputContainer.sampleTypeCode     = content.sampleTypeCode;
		sampleOnInputContainer.percentage         = content.percentage;
		sampleOnInputContainer.properties         = content.properties;

		Sample sample = getSample(content.sampleCode);

		sampleOnInputContainer.referenceCollab    = sample.referenceCollab;
		sampleOnInputContainer.ncbiScientificName = sample.ncbiScientificName;
		sampleOnInputContainer.taxonCode          = sample.taxonCode;

		sampleOnInputContainer.containerConcentration = container.concentration;
		sampleOnInputContainer.containerCode          = container.code;
		sampleOnInputContainer.containerSupportCode   = container.support.code;
		sampleOnInputContainer.containerVolume        = container.volume;
		sampleOnInputContainer.containerQuantity      = container.quantity;
		sampleOnInputContainer.containerConcentration = container.concentration;

		sampleOnInputContainer.lastUpdateDate = new Date();
		return sampleOnInputContainer;
	}

	public static SampleOnInputContainer getSampleOnInputContainer(String sampleCode) {
		Sample sample = getSample(sampleCode);			

		SampleOnInputContainer sampleOnInputContainer = new SampleOnInputContainer();

		sampleOnInputContainer.sampleCode         = sample.code;
		sampleOnInputContainer.sampleCategoryCode = sample.categoryCode;
		sampleOnInputContainer.sampleTypeCode     = sample.typeCode;
		sampleOnInputContainer.properties         = sample.properties; //to authorize research on sample properties

		sampleOnInputContainer.referenceCollab = sample.referenceCollab;
		sampleOnInputContainer.ncbiScientificName = sample.ncbiScientificName;
		sampleOnInputContainer.taxonCode = sample.taxonCode;

		sampleOnInputContainer.lastUpdateDate = new Date();
		return sampleOnInputContainer;
	}

	private static SampleOnContainer convertToSampleOnContainer(ReadSet readSet, String containerSupportCode, Container container, Content content) {
		SampleOnContainer sc = new SampleOnContainer();
		sc.lastUpdateDate         = new Date();
		sc.containerSupportCode   = containerSupportCode;
		sc.containerCode          = container.code;
		sc.projectCode            = readSet.projectCode;
		sc.sampleCode             = readSet.sampleCode;
		sc.sampleTypeCode         = content.sampleTypeCode;
		sc.sampleCategoryCode     = content.sampleCategoryCode;
		sc.percentage             = content.percentage;
		sc.properties             = content.properties;
		sc.containerConcentration = container.concentration;

		Sample sample = getSample(content.sampleCode);
		sc.referenceCollab    = sample.referenceCollab;
		sc.ncbiScientificName = sample.ncbiScientificName;
		sc.taxonCode          = sample.taxonCode;

		return sc;
	}

	private static Content getContent(Container container, ReadSet readSet) {
		String tag = getTag(readSet);
		for (Content sampleUsed : container.contents) {
			try {
				if (null == tag && sampleUsed.sampleCode.equals(readSet.sampleCode)){
					return sampleUsed;
				}else if(null != tag && !tag.contains("-") && null != sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME)
						&& tag.equals(convertTagCodeToTagShortName((String)sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value)) && sampleUsed.sampleCode
						.equals(readSet.sampleCode)){
					return sampleUsed;
				}else if(null != tag && tag.contains("-") && sampleUsed.sampleCode.equals(readSet.sampleCode)){
					//Get tag from DB from shortName
					Index index=MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, DBQuery.in("typeCode", "index-illumina-sequencing","index-mgi-sequencing", "index-nanopore-sequencing","index-pacbio-sequencing").is("shortName", tag));
					if(index!=null){
						//Index existe shortName
						if(null != sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME)
								&& tag.equals(convertTagCodeToTagShortName((String)sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value)))
							return sampleUsed;
					}else{
						//Index n'existe pas il faut découper le shortName pour avoir les deux tag Iaire et IIaire
						//get primary tag and secondary tag
						String[] codeParts = tag.split("-", 2);
						String primaryTag = (codeParts.length == 2) ? codeParts[0] : null;
						String secondaryTag = (codeParts.length == 2) ? codeParts[1] : null;
						if(null != primaryTag && null != secondaryTag 
								&& null != sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME) && null != sampleUsed.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME)
								&& primaryTag.equals(convertTagCodeToTagShortName((String)sampleUsed.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value)) 
								&& secondaryTag.equals(convertTagCodeToTagShortName((String)sampleUsed.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value))
								&& sampleUsed.sampleCode.equals(readSet.sampleCode))
							return sampleUsed;
					}
				}
			} catch (Exception e) {
				logger.error("Problem with " + readSet.code + " / " + readSet.sampleCode + " : " + e.getMessage());
			}
		}
		logger.warn("Not found Content for " + readSet.code + " / " + readSet.sampleCode);
		return null;
	}

	private static Object convertTagCodeToTagShortName(String tagCode) {
		Index index = MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, 
				DBQuery.in("typeCode", "index-illumina-sequencing","index-mgi-sequencing", "index-nanopore-sequencing","index-pacbio-sequencing")
				.is("code", tagCode));
		if (index != null) {
			return index.shortName;
		} else {
			logger.error("Index not found for code : "+tagCode);
			return null;
		}		
	}

	private static String getTag(ReadSet readSet) {
		String[] codeParts = readSet.code.split("\\.", 2);
		return (codeParts.length == 2) ? codeParts[1] : null;
	}
	/**
	 * Get a sample by code from the database ({@link SamplesAPI#get(String)}).
	 * @param sampleCode code of sample to load from database
	 * @return           sample from database
	 */
	private static Sample getSample(String sampleCode) {
		return MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, 
				Sample.class,
				DBQuery.is("code", sampleCode));
	}

	/**
	 * Get the collaborator reference from a sample from the database. 
	 * @param sampleCode code of sample to get reference from
	 * @return           collaborator reference of the sample with the given code
	 */
	public static String getReferenceCollab(String sampleCode) {
		Sample sample = getSample(sampleCode);
		return sample.referenceCollab;
	}

	private static Container getContainer(ReadSet readSet, String containerSupportCode) {
		MongoDBResult<Container> cl = MongoDBDAO.find(
				InstanceConstants.CONTAINER_COLL_NAME,
				Container.class,
				DBQuery.and(DBQuery.is("support.code", containerSupportCode),
						DBQuery.is("support.line", readSet.laneNumber.toString()),
						DBQuery.in("sampleCodes", readSet.sampleCode)));

		if (cl.size() == 0) {
			logger.warn("Not found Container for " + readSet.code + " with : '" + containerSupportCode + ", "
					+ readSet.laneNumber.toString() + ", " + readSet.sampleCode + "'");
			return null;
		}
		return cl.toList().get(0); 
	}

	// Probably better to use the iterator nature of the result than to
	// create the result list.
	protected static Container getContainer_(ReadSet readSet, String containerSupportCode) {
		MongoDBResult<Container> cl = MongoDBDAO.find(
				InstanceConstants.CONTAINER_COLL_NAME,
				Container.class,
				DBQuery.and(DBQuery.is("support.code", containerSupportCode),
						DBQuery.is("support.line", readSet.laneNumber.toString()),
						DBQuery.in("sampleCodes", readSet.sampleCode)));

		if (!cl.cursor.hasNext()) {
			logger.warn("Not found Container for " + readSet.code + " with : '" + containerSupportCode + ", "
					+ readSet.laneNumber.toString() + ", " + readSet.sampleCode + "'");
			return null;
		}
		return cl.cursor.next();
	}

	private static String getContainerSupportCode(ReadSet readSet) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("containerSupportCode", 1);
		Run r = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				DBQuery.is("code", readSet.runCode), 
				keys);
		return r.containerSupportCode;
	}

	/*
	 * Used to update content properties 
	 * @param properties
	 * @param newProperties
	 * @param deletedPropertyCodes
	 * @return
	 */
	//	public static Map<String, PropertyValue> updateProperties(Map<String, PropertyValue> properties, 
	//			                                                  Map<String, PropertyValue> newProperties, 
	//			                                                  Set<String> deletedPropertyCodes) {
	//		properties.replaceAll((k,v) -> newProperties.containsKey(k) ? newProperties.get(k) : v);							
	//		newProperties.forEach((k,v) -> properties.putIfAbsent(k, v));
	//		deletedPropertyCodes.forEach(code -> properties.remove(code));
	//		return properties;
	//	}
	/**
	 * Update (in place) a property map by adding and overwriting using a new property map
	 * and removing keys defined by the deleted key set. New properties do not
	 * appear in the result properties if their key is in the set to delete.
	 * @param properties           properties to update
	 * @param newProperties        properties to add or overwrite
	 * @param deletedPropertyCodes property keys to delete
	 * @return                     updated properties
	 */
	public static Map<String, PropertyValue> updateProperties(Map<String, PropertyValue> properties, 
			Map<String, PropertyValue> newProperties, 
			Set<String> deletedPropertyCodes) {
		properties.putAll(newProperties);
		properties.keySet().removeAll(deletedPropertyCodes);
		return properties;
	}

	/*
	 * Update properties with using the oldValue to check if update is needed
	 * @param properties
	 * @param newProperties
	 * @param deletedPropertyCodes
	 * @return
	 */
	public static Map<String, PropertyValue> 
	updatePropertiesWithOldValueComparison(Map<String, PropertyValue> properties, 
			Map<String, Pair<PropertyValue, PropertyValue>> newProperties, 
			Set<String> deletedPropertyCodes) {
		// 1 replace if old value equals old value
		//properties.replaceAll((k,v) -> (newProperties.containsKey(k) && ((newProperties.get(k).getLeft() != null && newProperties.get(k).getLeft().equals(v)) || newProperties.get(k).getLeft() == null))?newProperties.get(k).getRight():v);							
		//NGL-2930 Compare only property value and not type because value propagation is not same type (Double, Integer)
		properties.replaceAll((k,v) -> 
		(newProperties.containsKey(k) && 
				((newProperties.get(k).getLeft() != null && newProperties.get(k).getLeft().getValue() instanceof Double && v.getValue() instanceof Integer && newProperties.get(k).getLeft().getValue().equals(Double.valueOf(((Integer)v.getValue()).intValue())))
						|| (newProperties.get(k).getLeft() != null && newProperties.get(k).getLeft().equals(v))
						|| newProperties.get(k).getLeft() == null))?newProperties.get(k).getRight():v);							
		// 2 add new properties
		newProperties.forEach((k,v)-> properties.putIfAbsent(k, newProperties.get(k).getRight()));
		// 3 delete remove properties
		deletedPropertyCodes.forEach(code -> properties.remove(code));
		return properties;
	}

	//NGL-2733 new service with check tag and secondaryTag to replace old service
	public static void _updateContentProperties(Set<String> projectCodes, 
			Set<String> sampleCodes, 
			Set<String> containerCodes,
			Set<String> tags, 
			Map<String, Pair<PropertyValue,PropertyValue>> updatedProperties, 
			Set<String> deletedPropertyCodes,
			ContextValidation validation)
	{

		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.in("code", containerCodes))
		.cursor
		.forEach(container -> {
			container.traceInformation.setTraceInformation(validation.getUser());
			container.contents.stream()
			.filter(content -> (
					sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) && (
							(!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
							|| (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))	
							|| (null != tags && content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
							|| (null != tags && !content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
							))).forEach(content -> {
								Query findContentQuery = ContentHelper.getContentQuery(container, content);
								content.properties = InstanceHelpers.updatePropertiesWithOldValueComparison(content.properties, updatedProperties, deletedPropertyCodes);		
								MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, findContentQuery, DBUpdate.set("contents.$", content));
							});			
		});

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
					content.properties = InstanceHelpers.updatePropertiesWithOldValueComparison(content.properties, updatedProperties, deletedPropertyCodes);							
				});
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.filter(ocu -> containerCodes.contains(ocu.code))							
					.map(ocu -> ocu.contents)
					.flatMap(List::stream)
					.filter(content -> sampleCodes.contains(content.sampleCode) && projectCodes.contains(content.projectCode) )
					.forEach(content -> {
						content.properties = InstanceHelpers.updatePropertiesWithOldValueComparison(content.properties, updatedProperties, deletedPropertyCodes);							
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
			if((!process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
					||(null!=tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+process.sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && !process.sampleOnInputContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && process.sampleOnInputContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(process.sampleOnInputContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
					){
				process.traceInformation.setTraceInformation(validation.getUser());
				process.sampleOnInputContainer.lastUpdateDate = new Date();
				process.sampleOnInputContainer.properties = InstanceHelpers.updatePropertiesWithOldValueComparison(process.sampleOnInputContainer.properties, updatedProperties, deletedPropertyCodes);	
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code), 
						DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer).set("traceInformation", process.traceInformation));
			}
		});

		//update readsets with new exp property values
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", containerCodes).in("sampleCode", sampleCodes).in("projectCode", projectCodes))
		.cursor
		.forEach(readset -> {
			if((!readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
					||(null!=tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains("_"+readset.sampleOnContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value.toString()))
					||(null!=tags && !readset.sampleOnContainer.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME) && readset.sampleOnContainer.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME) && tags.contains(readset.sampleOnContainer.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value.toString()+"_"))
					){
				readset.traceInformation.setTraceInformation(validation.getUser());
				readset.sampleOnContainer.lastUpdateDate = new Date();
				readset.sampleOnContainer.properties = InstanceHelpers.updatePropertiesWithOldValueComparison(readset.sampleOnContainer.properties, updatedProperties, deletedPropertyCodes);	
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), 
						DBUpdate.set("sampleOnContainer", readset.sampleOnContainer).set("traceInformation", readset.traceInformation));
				if(updatedProperties.containsKey("libProcessTypeCode")) {
					//Call rules to refresh libProcessTypeCodes Run
					//Get run to update
					Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, readset.runCode);
					//Call upate libProcessTypeCode from containerSupportCode
					//Code from rules Copy Properties from Support and Container to Run
					List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();

					Set<String> libProcessTypeCodes = new TreeSet<String>();
					for(Container container:containers){
						for(Content content:container.contents){
							if(content.properties.containsKey("libProcessTypeCode")){
								libProcessTypeCodes.add((String)(content.properties.get("libProcessTypeCode").value));
							}
						}
					}
					if(libProcessTypeCodes.size() > 0){
						run.properties.put("libProcessTypeCodes", new PropertyListValue(new ArrayList(libProcessTypeCodes)));
					}

					MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
							DBQuery.is("code", run.code),
							DBUpdate.set("properties", run.properties));

				}
			}
		});	



	}

	public static void updateContentProperties(Sample sample, 
			Map<String, PropertyValue> updatedProperties, 
			Set<String> deletedPropertyCodes,
			ContextValidation validation) {
		MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, 
				DBQuery.is("life.from.sampleCode", sample.code)
				.in("life.from.projectCode", sample.projectCodes))
		.cursor.forEach(updatedSample -> {
			updatedSample.traceInformation.setTraceInformation(validation.getUser());
			updatedSample.referenceCollab    = sample.referenceCollab;
			updatedSample.taxonCode          = sample.taxonCode;
			updatedSample.ncbiScientificName = sample.ncbiScientificName;
			updatedSample.ncbiLineage        = sample.ncbiLineage;
			updatedSample.properties         = InstanceHelpers.updateProperties(updatedSample.properties, updatedProperties, deletedPropertyCodes);
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,updatedSample);
		});

		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
				DBQuery.is("contents.sampleCode", sample.code)
				.in("contents.projectCode", sample.projectCodes))
		.cursor.forEach(container -> {
			container.traceInformation.setTraceInformation(validation.getUser());
			container.contents.stream()
			.filter(content -> sample.code.equals(content.sampleCode) && sample.projectCodes.contains(content.projectCode) )
			.forEach(content -> {
				Query findContentQuery = ContentHelper.getContentQuery(container, content);
				content.ncbiScientificName = sample.ncbiScientificName;
				content.taxonCode          = sample.taxonCode;
				content.referenceCollab    = sample.referenceCollab;
				content.properties = InstanceHelpers.updateProperties(content.properties, updatedProperties, deletedPropertyCodes);
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,findContentQuery, DBUpdate.set("contents.$", content));
			});				
		});

		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.in("sampleCodes", sample.code)
				.in("projectCodes", sample.projectCodes))
		.cursor.forEach(experiment -> {
			experiment.traceInformation.setTraceInformation(validation.getUser());
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds
				.stream()
				.filter(icu -> icu.contents != null) // for very old experiment before 01/12/2015
				//						.map(icu -> icu.contents)
				//						.flatMap(List::stream)
				.flatMap(icu -> icu.contents.stream())
				.filter(content -> sample.code.equals(content.sampleCode) && sample.projectCodes.contains(content.projectCode) )
				.forEach(content -> {
					content.ncbiScientificName = sample.ncbiScientificName;
					content.taxonCode          = sample.taxonCode;
					content.referenceCollab    = sample.referenceCollab;

					content.properties = InstanceHelpers.updateProperties(content.properties, updatedProperties, deletedPropertyCodes);							
				});
				if (atm.outputContainerUseds != null) {
					atm.outputContainerUseds
					.stream()
					.filter(ocu -> ocu.contents != null)
					//							.map(ocu -> ocu.contents)
					//							.flatMap(List::stream)
					.flatMap(ocu -> ocu.contents.stream())
					.filter(content -> sample.code.equals(content.sampleCode) && sample.projectCodes.contains(content.projectCode) )
					.forEach(content -> {
						content.ncbiScientificName = sample.ncbiScientificName;
						content.taxonCode          = sample.taxonCode;
						content.referenceCollab    = sample.referenceCollab;

						content.properties = InstanceHelpers.updateProperties(content.properties, updatedProperties, deletedPropertyCodes);							
					});
				}
			});				
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
					DBQuery.is("code", experiment.code), 
					DBUpdate.set("atomicTransfertMethods", experiment.atomicTransfertMethods)
					.set("traceInformation", experiment.traceInformation));	
		});

		// Processes update sampleOnInputContainer.properties		
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME,Process.class, 
				DBQuery.is("sampleOnInputContainer.sampleCode", sample.code)
				.in("sampleOnInputContainer.projectCode", sample.projectCodes))
		.cursor
		.forEach(process -> {
			process.traceInformation.setTraceInformation(validation.getUser());
			process.sampleOnInputContainer.lastUpdateDate     = new Date();
			process.sampleOnInputContainer.referenceCollab    = sample.referenceCollab;
			process.sampleOnInputContainer.taxonCode          = sample.taxonCode;
			process.sampleOnInputContainer.ncbiScientificName = sample.ncbiScientificName;
			process.sampleOnInputContainer.properties = InstanceHelpers.updateProperties(process.sampleOnInputContainer.properties, updatedProperties, deletedPropertyCodes);
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code), 
					DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer).set("traceInformation", process.traceInformation));		
		});

		// ReadSet update sampleOnContainer.properties		
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.is("sampleCode", sample.code)
				.in("projectCode", sample.projectCodes)) 
		.cursor
		.forEach(readset -> {
			readset.traceInformation.setTraceInformation(validation.getUser());
			readset.sampleOnContainer.lastUpdateDate     = new Date();
			readset.sampleOnContainer.referenceCollab    = sample.referenceCollab;
			readset.sampleOnContainer.taxonCode          = sample.taxonCode;
			readset.sampleOnContainer.ncbiScientificName = sample.ncbiScientificName;

			readset.sampleOnContainer.properties = InstanceHelpers.updateProperties(readset.sampleOnContainer.properties, updatedProperties, deletedPropertyCodes);
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, Readset.class, 
					DBQuery.is("code", readset.code), 
					DBUpdate.set("sampleOnContainer", readset.sampleOnContainer)
					.set("traceInformation",  readset.traceInformation));			
		});	
	}

	/*
	 * WARNING : NEED TO CALL AFTER VALIDATION BECAUSE SOME CONVERTION ARE EXECUTE DURING VALIDATION
	 * @param availablePropertyCodes
	 * @param oldProperties
	 * @param newProperties
	 * @return a pair with left element is the old propertyValue and right the new propertyValue
	 */
	public static Map<String, Pair<PropertyValue,PropertyValue>> 
	getUpdatedPropertiesForSomePropertyCodes(Set<String> propertyCodes, 
			Map<String, PropertyValue> oldProperties,
			Map<String, PropertyValue> newProperties) {
		return propertyCodes.stream()
				.filter(code -> newProperties.containsKey(code))
				.filter(code -> !newProperties.get(code).equals(oldProperties.get(code)))
				.collect(Collectors.toMap(code -> code, code -> Pair.of(oldProperties.get(code), newProperties.get(code))));		
	}

	/*
	 * WARNING : NEED TO CALL AFTER VALIDATION BECAUSE SOME CONVERTION ARE EXECUTE DURING VALIDATION
	 * @param availablePropertyCodes
	 * @param dbProperties
	 * @param newProperties
	 * @return
	 */
	public static Set<String> getDeletedPropertiesForSomePropertyCodes(Set<String> propertyCodes, 
			Map<String, PropertyValue> dbProperties,
			Map<String, PropertyValue> newProperties) {
		return propertyCodes.stream()
				.filter(code -> dbProperties.containsKey(code) && !newProperties.containsKey(code))
				.collect(Collectors.toSet());		
	}

}
