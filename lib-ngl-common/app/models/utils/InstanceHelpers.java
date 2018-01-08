package models.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.parameter.index.Index;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import play.Logger;
import play.mvc.Http;
import validation.ContextValidation;
import validation.IValidation;

import com.mongodb.BasicDBObject;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;

public class InstanceHelpers {

	@SuppressWarnings("unchecked")
	public static Map<String, PropertyValue> getLazyMapPropertyValue() {
		return MapUtils.lazyMap(new HashMap<String, PropertyValue>(), new Transformer() {
			public PropertyValue transform(Object mapKey) {
				// todo comment je sais quel est le type on doit mettre
				return new PropertySingleValue();
			}
		});
	}

	@Deprecated
	public static String getUser() {
		String user;
		try {
			user = Http.Context.current().session().get("CAS_FILTER_USER");
			if (user == null) {
				user = "ngl";
			}
		} catch (RuntimeException e) {
			user = "ngl";
		}
		return user;

	}

	public static List<Comment> addComment(String comment, List<Comment> comments, String user) {
		if (comments == null) {
			comments = new ArrayList<Comment>();
		}

		Comment newComment = new Comment(comment, user);
		
		comments.add(newComment);
		return comments;
	}

	
	public static List<Comment> updateComments(List<Comment> comments, ContextValidation contextValidation){
		if(comments != null && comments.size() > 0){
			comments = comments.parallelStream()
							.filter(c -> StringUtils.isNotBlank(c.comment))
							.map(c -> {
								c.comment = c.comment.trim();
								if(null == c.createUser){
									c.createUser = contextValidation.getUser();
									c.creationDate = new Date();
									c.code = CodeHelper.getInstance().generateExperimentCommentCode(c);
								}
								return c;
							}).collect(Collectors.toList());
			if(comments.size() > 0)return comments;
		}
		return new ArrayList<Comment>(0);					
	}
	
	
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

	public static TraceInformation updateTraceInformation(TraceInformation traceInformation, State nextState) {
		traceInformation.modifyDate = nextState.date;
		traceInformation.modifyUser = nextState.user;
		return traceInformation;
	}

	public static TraceInformation getUpdateTraceInformation(TraceInformation traceInformation, String user) {
		TraceInformation ti = null;
		if (traceInformation == null) {
			ti = new TraceInformation();
		} else {
			ti = traceInformation;
		}
		ti.setTraceInformation(user);
		return ti;
	}

	public static void copyPropertyValueFromPropertiesDefinition(List<PropertyDefinition> propertyDefinitions,
			Map<String, PropertyValue> propertiesInput, Map<String, PropertyValue> propertiesOutPut) {
		
		if (propertiesOutPut == null) {
			propertiesOutPut = new HashMap<String, PropertyValue>();
		}
		
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			PropertyValue propertyValue = propertiesInput.get(propertyDefinition.code);
			if (propertyValue != null) {
				propertiesOutPut.put(propertyDefinition.code, propertyValue);
			}
		}

	}
	
	public static DBObject save(String collectionName, IValidation obj, ContextValidation contextError,
			Boolean keepRootKeyName) {
		ContextValidation localContextError = new ContextValidation(contextError.getUser());
		localContextError.setMode(contextError.getMode());
		Logger.debug("InstanceHelpers.save");
		if (keepRootKeyName) {
			Logger.debug("InstanceHelpers.save if keepRootKeyName " + keepRootKeyName);
			localContextError.addKeyToRootKeyName(contextError.getRootKeyName());
		} 
		localContextError.setContextObjects(contextError.getContextObjects());
		Logger.debug("InstanceHelpers.save localContextError.setContextObjects");

		if (obj != null) {
			Logger.debug("InstanceHelpers.save obj != null");
			obj.validate(localContextError);
			Logger.debug("InstanceHelpers.save obj != null after validate");
		} else {
			Logger.debug("InstanceHelpers.save else");
			throw new IllegalArgumentException("missing object to validate");
		}

		if (localContextError.errors.size() == 0) {
			return MongoDBDAO.save(collectionName, (DBObject) obj);
		} else {
			contextError.errors.putAll(localContextError.errors);
			Logger.info("error(s) on output :: " + contextError.errors.toString());
			return null;
		}  
	}

	public static DBObject save(String collectionName, IValidation obj, ContextValidation contextError) {
		return save(collectionName, obj, contextError, false);
	}

	public static <T extends DBObject> List<T> save(String collectionName, List<T> objects,
			ContextValidation contextErrors) {

		List<T> dbObjects = new ArrayList<T>();

		for (DBObject object : objects) {
			@SuppressWarnings("unchecked")
			T result = (T) InstanceHelpers.save(collectionName, (IValidation) object, contextErrors);
			if (result != null) {
				dbObjects.add(result);
			}
		}

		return (List<T>) dbObjects;
	}

	public static <T extends DBObject> List<T> save(String collectionName, List<T> objects,
			ContextValidation contextErrors, Boolean keepRootKeyName) {

		List<T> dbObjects = new ArrayList<T>();

		for (DBObject object : objects) {
			@SuppressWarnings("unchecked")
			T result = (T) InstanceHelpers.save(collectionName, (IValidation) object, contextErrors, keepRootKeyName);
			if (result != null) {
				dbObjects.add(result);
			}
		}

		return (List<T>) dbObjects;
	}

	public static SampleOnContainer getSampleOnContainer(ReadSet readSet) {
		// 1 retrieve containerSupportCode from Run
		String containerSupportCode = getContainerSupportCode(readSet);
		Container container = getContainer(readSet, containerSupportCode);
		if (null != container) {
			Content content = getContent(container, readSet);
			if (null != content) {
				SampleOnContainer sampleContainer = convertToSampleOnContainer(readSet, containerSupportCode,
						container, content);
				return sampleContainer;
			}
		}
		return null;
	}

	public static SampleOnInputContainer getSampleOnInputContainer(Content content,Container container) {
		
		Logger.debug("InstanceHelper getSampleOnInputContainer");

		SampleOnInputContainer sampleOnInputContainer = new SampleOnInputContainer();
		sampleOnInputContainer.projectCode = content.projectCode;
		sampleOnInputContainer.sampleCode = content.sampleCode;
		sampleOnInputContainer.sampleCategoryCode = content.sampleCategoryCode;
		sampleOnInputContainer.sampleTypeCode = content.sampleTypeCode;
		sampleOnInputContainer.percentage = content.percentage;
		sampleOnInputContainer.properties = content.properties;

		Sample sample = getSample(content.sampleCode);
		
		sampleOnInputContainer.referenceCollab = sample.referenceCollab;
		sampleOnInputContainer.ncbiScientificName = sample.ncbiScientificName;
		sampleOnInputContainer.taxonCode = sample.taxonCode;
		
		sampleOnInputContainer.containerConcentration = container.concentration;
		sampleOnInputContainer.containerCode = container.code;
		sampleOnInputContainer.containerSupportCode = container.support.code;
		sampleOnInputContainer.containerVolume = container.volume;
		sampleOnInputContainer.containerQuantity = container.quantity;
		sampleOnInputContainer.containerConcentration = container.concentration;

		sampleOnInputContainer.lastUpdateDate = new Date();
		return sampleOnInputContainer;
	}

	private static SampleOnContainer convertToSampleOnContainer(ReadSet readSet, String containerSupportCode,
			Container container, Content content) {
		SampleOnContainer sc = new SampleOnContainer();
		sc.lastUpdateDate = new Date();
		sc.containerSupportCode = containerSupportCode;
		sc.containerCode = container.code;
		sc.projectCode = readSet.projectCode;
		sc.sampleCode = readSet.sampleCode;
		sc.sampleTypeCode = content.sampleTypeCode;
		sc.sampleCategoryCode = content.sampleCategoryCode;
		sc.percentage = content.percentage;
		sc.properties = content.properties;
		sc.containerConcentration = container.concentration;
		
		Sample sample = getSample(content.sampleCode);
		sc.referenceCollab = sample.referenceCollab;
		sc.ncbiScientificName = sample.ncbiScientificName;
		sc.taxonCode = sample.taxonCode;
		
		return sc;
	}

	public static Sample convertToSample(ReadSet readSet)
	{
		SampleOnContainer sampleOnContainer = readSet.sampleOnContainer;
		Sample sample = new Sample();
		sample.code=sampleOnContainer.sampleCode;
		sample.name=sampleOnContainer.sampleCode;
		sample.typeCode=sampleOnContainer.sampleTypeCode;
		sample.categoryCode=sampleOnContainer.sampleCategoryCode;
		sample.properties=sampleOnContainer.properties;
		sample.referenceCollab=sampleOnContainer.referenceCollab;
		sample.projectCodes = new HashSet<>();
		sample.projectCodes.add(readSet.projectCode);
		sample.importTypeCode="external";
		InstanceHelpers.getUpdateTraceInformation(sample.traceInformation, "ngl-bi");
		return sample;
	}
	private static Content getContent(Container container, ReadSet readSet) {
		String tag = getTag(readSet);
		for (Content sampleUsed : container.contents) {
			try {
				if ((null == tag && sampleUsed.sampleCode.equals(readSet.sampleCode))
						|| (null != tag && null != sampleUsed.properties.get("tag")
								&& tag.equals(convertTagCodeToTagShortName((String)sampleUsed.properties.get("tag").value)) && sampleUsed.sampleCode
									.equals(readSet.sampleCode))) {
					return sampleUsed;
				}
			} catch (Exception e) {
				Logger.error("Problem with " + readSet.code + " / " + readSet.sampleCode + " : " + e.getMessage());
			}
		}
		Logger.warn("Not found Content for " + readSet.code + " / " + readSet.sampleCode);
		return null;
	}

	private static Object convertTagCodeToTagShortName(String tagCode) {
		Index index=MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, DBQuery.in("typeCode", "index-illumina-sequencing","index-nanopore-sequencing").is("code", tagCode));
		if(null != index){
			return index.shortName;
		}else{
			Logger.error("Index not found for code : "+tagCode);
			return null;
		}		
	}

	private static String getTag(ReadSet readSet) {
		String[] codeParts = readSet.code.split("\\.", 2);
		return (codeParts.length == 2) ? codeParts[1] : null;
	}
	
	
	private static Sample getSample(String sampleCode){
		return MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,
				DBQuery.is("code", sampleCode));
	}
	
	
	public static String getReferenceCollab(String sampleCode){
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
			Logger.warn("Not found Container for " + readSet.code + " with : '" + containerSupportCode + ", "
					+ readSet.laneNumber.toString() + ", " + readSet.sampleCode + "'");
			return null;
		}

		return cl.toList().get(0);
	}

	private static String getContainerSupportCode(ReadSet readSet) {
		BasicDBObject keys = new BasicDBObject();
		keys.put("containerSupportCode", 1);
		Run r = MongoDBDAO
				.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", readSet.runCode), keys);
		return r.containerSupportCode;
	}
	
	
}