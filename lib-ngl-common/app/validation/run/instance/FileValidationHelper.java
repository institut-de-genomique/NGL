package validation.run.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class FileValidationHelper extends CommonValidationHelper {
	
	// --------------------------------------------------------------
	// renamed and arguments reordered
	
//	public static void validationFiles(List<File> files, ContextValidation contextValidation) {
//		if (files!= null && files.size() > 0) {
//			int index = 0;
//			List<String> lstFullName = new ArrayList<>();
//			for (File file : files) {
//				contextValidation.addKeyToRootKeyName("files[" + index + "]");
//				if (!lstFullName.contains(file.fullname)) {
//					file.validate(contextValidation);
//					lstFullName.add(file.fullname);
//				} else { 
//					contextValidation.addError("fullname", ValidationConstants.ERROR_NOTUNIQUE_MSG, file.fullname); 
//				}
//				contextValidation.removeKeyFromRootKeyName("files[" + index + "]");
//				index++;
//			}
//		}
//	}
	
//	/**
//	 * Validate a collection of non null files (name uniqueness in collection and 
//	 * individual validation).
//	 * @param files             files to validate
//	 * @param contextValidation validation context
//	 * @deprecated use {@link #validateFiles(ContextValidation, List)}
//	 */
//	@Deprecated
//	public static void validationFiles(List<File> files, ContextValidation contextValidation) {
//		FileValidationHelper.validateFiles(contextValidation, files);
//	}
//	
//	/**
//	 * Validate a collection of non null files (name uniqueness in collection and 
//	 * individual validation) (context parameter "objectClass", ("readSet" or "analysis")).
//	 * @param contextValidation validation context
//	 * @param files             files to validate
//	 */
//	@Deprecated
//	public static void validateFiles(ContextValidation contextValidation, List<File> files) {
//		if (CollectionUtils.isEmpty(files))
//			return;
//		int index = 0;
//		Set<String> lstFullName = new HashSet<>();
//		for (File file : files) {
//			contextValidation.addKeyToRootKeyName("files[" + index + "]");
//			if (!lstFullName.contains(file.fullname)) {
//				file.validate(contextValidation);
//				lstFullName.add(file.fullname);
//			} else { 
//				contextValidation.addError("fullname", ValidationConstants.ERROR_NOTUNIQUE_MSG, file.fullname); 
//			}
//			contextValidation.removeKeyFromRootKeyName("files[" + index + "]");
//			index++;
//		}
//	}
	
//	// Alternate implementation using a generic method.
//	public static void validateFiles_(ContextValidation contextValidation, List<File> files) {
////		validateUniqueness(contextValidation, files, "files", "fullname", f -> f.fullname, (c,f) -> f.validate(c));
//		validateFiles(contextValidation, files, (c,f) -> f.validate(c));
//	}
	
	private static void validateFiles(ContextValidation contextValidation, List<File> files, BiConsumer<ContextValidation,File> validation) {
		validateUniqueness(contextValidation, files, "files", "fullname", f -> f.fullname, validation);
	}
	
	public static void validateFiles(ContextValidation contextValidation, Analysis analysis, List<File> files) {
		validateFiles(contextValidation, files, (c,f) -> f.validate(c, analysis));
	}
	
	public static void validateFiles(ContextValidation contextValidation, ReadSet readSet, List<File> files) {
		validateFiles(contextValidation, files, (c,f) -> f.validate(c, readSet));
	}
	
//	// ------------------------------------------------------------------------
//	
//	/**
//	 * Get ReadSet instance from context (context parameter "readSet").
//	 * @param contextValidation validation context
//	 * @return                  ReadSet instance
//	 */
//	private static ReadSet getReadSetFromContext(ContextValidation contextValidation) {
////		return getObjectFromContext("readSet", ReadSet.class, contextValidation);
//		return contextValidation.getTypedObject("readSet");
//	}
//		
//	/**
//	 * Get Analysis instance from context (context parameter "analysis").
//	 * @param contextValidation validation context
//	 * @return                  Analysis instance
//	 */
//	private static Analysis getAnalysisFromContext(ContextValidation contextValidation) {
////		return getObjectFromContext("analysis", Analysis.class, contextValidation);
//		return contextValidation.getTypedObject("analysis");
//	}
	
//	// ------------------------------------------------------------------------
//	// arguments reordered
//	
//	/**
//	 * Validate a file by name (context parameter "objectClass", ReadSet and Analysis
//	 * contextual objects). If there is no "objectClass" in the context, nothing is done.
//	 * The "objectClass" object must match a key that is dependent on the object class
//	 * (if the object class is ReadSet, a "readSet" object must be provided in the context).   
//	 * @param contextValidation validation context
//	 * @param fullname          file name
//	 * @deprecated use {@link #validateFileFullName(ContextValidation, String)}
//	 */
//	@Deprecated
//	public static void validateFileFullName(String fullname, ContextValidation contextValidation) {
//		FileValidationHelper.validateFileFullName(contextValidation, fullname);
//	}
//
//	/**
//	 * Validate a file by name (context parameter "objectClass", ReadSet and Analysis
//	 * contextual objects). If there is no "objectClass" in the context, nothing is done.
//	 * The "objectClass" object must match a key that is dependent on the object class
//	 * (if the object class is ReadSet, a "readSet" object must be provided in the context).   
//	 * @param contextValidation validation context
//	 * @param fullname          file name
//	 */
//	@Deprecated
//	public static void validateFileFullName(ContextValidation contextValidation, String fullname) {
////		Class<?> objectClass =  getObjectFromContext("objectClass", Class.class, contextValidation);
//		Class<?> objectClass =  contextValidation.getTypedObject("objectClass");
//		if (ReadSet.class.isAssignableFrom(objectClass)) {
//			validateReadSetFileFullName(contextValidation, fullname);
//		} else if(Analysis.class.equals(objectClass)) {
//			validateAnalysisFileFullName(contextValidation, fullname);
//		}
//	}
	
	// ------------------------------------------------------------------------

	// LOGIC: This probably does not test the actual instance as the actual enclosing instance
	// is not in the database as we are probably either creating or updating it otherwise we have
	// saved or updated the instance before validating it. 
//	private static void validateReadSetFileFullName(ContextValidation contextValidation, String fullname) {
//		ReadSet readSet = getReadSetFromContext(contextValidation);
////		if (ValidationHelper.validateNotEmpty(contextValidation, fullname, "fullname")) {
////			// Validate unique file.code if not already exists
////			if (contextValidation.isCreationMode() && MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
////					DBQuery.and(DBQuery.is("code", readSet.code), DBQuery.is("files.fullname", fullname)))) {
////				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTUNIQUE_MSG, fullname);
////			} else if (contextValidation.isUpdateMode() && !MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
////					DBQuery.and(DBQuery.is("code", readSet.code), DBQuery.is("files.fullname", fullname)))) {
////				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTEXISTS_MSG, fullname);
////			}
////		}
//		validateFileFullName(contextValidation, fullname, readSet);
//	}
	
	public static void validateFileFullName(ContextValidation contextValidation, String fullname, ReadSet readSet) {
		if (ValidationHelper.validateNotEmpty(contextValidation, fullname, "fullname")) {
			// Validate unique file.code if not already exists
			if (contextValidation.isCreationMode() && MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("code", readSet.code), DBQuery.is("files.fullname", fullname)))) {
				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTUNIQUE_MSG, fullname);
			} else if (contextValidation.isUpdateMode() && !MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("code", readSet.code), DBQuery.is("files.fullname", fullname)))) {
				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTEXISTS_MSG, fullname);
			}
		}
	}

	// LOGIC: This probably does not test the actual instance as the actual enclosing instance
	// is not in the database as we are probably either creating or updating it otherwise we have
	// saved or updated the instance before validating it. 
//	private static void validateAnalysisFileFullName(ContextValidation contextValidation, String fullname) {
//		Analysis analysis = getAnalysisFromContext(contextValidation);
////		if (ValidationHelper.validateNotEmpty(contextValidation, fullname, "fullname")) {
////			//Validate unique file.code if not already exists
////			if (contextValidation.isCreationMode() && MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
////					DBQuery.and(DBQuery.is("code", analysis.code), DBQuery.is("files.fullname", fullname)))) {
////				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTUNIQUE_MSG, fullname);
////			} else if (contextValidation.isUpdateMode() && !MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
////					DBQuery.and(DBQuery.is("code", analysis.code), DBQuery.is("files.fullname", fullname)))) {
////				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTEXISTS_MSG, fullname);
////			}
////		}
//		validateFileFullName(contextValidation, fullname, analysis);
//	}

	public static void validateFileFullName(ContextValidation contextValidation, String fullname, Analysis analysis) {
		if (ValidationHelper.validateNotEmpty(contextValidation, fullname, "fullname")) {
			// Validate unique file.code if not already exists
			if (contextValidation.isCreationMode() && MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
					DBQuery.and(DBQuery.is("code", analysis.code), DBQuery.is("files.fullname", fullname)))) {
				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTUNIQUE_MSG, fullname);
			} else if (contextValidation.isUpdateMode() && !MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
					DBQuery.and(DBQuery.is("code", analysis.code), DBQuery.is("files.fullname", fullname)))) {
				contextValidation.addError("fullname",ValidationConstants.ERROR_NOTEXISTS_MSG, fullname);
			}
		}
	}

//	// --------------------------------------------------------------
//	// arguments reordered
//	
//	/**
//	 * Validate file properties using a context object that may define extra properties (context
//	 * parameter none, "objectClass", ("readSet" or "analysis")). 
//	 * @param properties        properties 
//	 * @param contextValidation validation context
//	 * @deprecated use {@link #validateFileProperties(ContextValidation, Map)}
//	 */
//	public static void validateFileProperties_(Map<String, PropertyValue> properties, ContextValidation contextValidation) {
//		FileValidationHelper.validateFileProperties(contextValidation, properties);
//	}
//	
//	/**
//	 * Validate file properties using a context object that may define extra properties (context
//	 * parameter none, "objectClass", ("readSet" or "analysis")). 
//	 * @param contextValidation validation context
//	 * @param properties        properties 
//	 */
//	@Deprecated
//	public static void validateFileProperties(ContextValidation contextValidation, Map<String, PropertyValue> properties) {
////		Class<?> objectClass =  getObjectFromContext("objectClass", Class.class, contextValidation);
//		Class<?> objectClass = contextValidation.getTypedObject("objectClass");
//		if (ReadSet.class.isAssignableFrom(objectClass)) { 
//			validateReadSetFileProperties(contextValidation, properties);
//		} else if (Analysis.class.equals(objectClass)) {
//			validateAnalysisFileProperties(contextValidation, properties);
//		}
//	}

	// -------------------------------------------------------
	// cleaned (removed extraneous try) and arguments reordered 
	
//	private static void validateReadSetFileProperties(Map<String, PropertyValue> properties, ContextValidation contextValidation) {
//		ReadSet readSet = getReadSetFromContext(contextValidation);
//		try {
//			ReadSetType readSetType = ReadSetType.find.get().findByCode(readSet.typeCode);
//			if (readSetType != null) {
//				contextValidation.addKeyToRootKeyName("properties");
//				ValidationHelper.validateProperties(contextValidation, properties, readSetType.getPropertyDefinitionByLevel(Level.CODE.File), true);
//				contextValidation.removeKeyFromRootKeyName("properties");
//			}
//		} catch (DAOException e) {
//			throw new RuntimeException(e);
//		}		
//	}
	
//	/**
//	 * Validate file properties within a ReadSet instance (context parameter "readSet").
//	 * @param contextValidation validation context
//	 * @param properties        properties
//	 */
//	private static void validateReadSetFileProperties(ContextValidation contextValidation, Map<String, PropertyValue> properties) {
//		ReadSet     readSet     = getReadSetFromContext(contextValidation);
////		ReadSetType readSetType = ReadSetType.find.get().findByCode(readSet.typeCode);
////		if (readSetType != null) {
////			contextValidation.addKeyToRootKeyName("properties");
////			ValidationHelper.validateProperties(contextValidation, properties, readSetType.getPropertyDefinitionByLevel(Level.CODE.File), true);
////			contextValidation.removeKeyFromRootKeyName("properties");
////		}
//		validateFileProperties(contextValidation, properties, readSet);
//	}
	
	/**
	 * Validate file properties in a read set context.
	 * @param contextValidation validation context
	 * @param properties        properties
	 * @param readSet           readSet
	 */
	public static void validateFileProperties(ContextValidation contextValidation, Map<String, PropertyValue> properties, ReadSet readSet) {
		ReadSetType readSetType = ReadSetType.find.get().findByCode(readSet.typeCode);
		if (readSetType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, readSetType.getPropertyDefinitionByLevel(Level.CODE.File), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}
	}
	
//	private static void validateAnalysisFileProperties(Map<String, PropertyValue> properties, ContextValidation contextValidation) {
//		Analysis analysis = getAnalysisFromContext(contextValidation);
//		try {
//			AnalysisType analysisType = AnalysisType.find.get().findByCode(analysis.typeCode);
//			if (null != analysisType) {
//				contextValidation.addKeyToRootKeyName("properties");
//				ValidationHelper.validateProperties(contextValidation, properties, analysisType.getPropertyDefinitionByLevel(Level.CODE.File), true);
//				contextValidation.removeKeyFromRootKeyName("properties");
//			}
//		} catch (DAOException e) {
//			throw new RuntimeException(e);
//		}		
//	}

//	/**
//	 * Validate file properties within an Analysis instance (context parameter "analysis").
//	 * @param contextValidation validation context
//	 * @param properties        files properties
//	 */
//	private static void validateAnalysisFileProperties(ContextValidation contextValidation, Map<String, PropertyValue> properties) {
//		Analysis     analysis     = getAnalysisFromContext(contextValidation);
////		AnalysisType analysisType = AnalysisType.find.get().findByCode(analysis.typeCode);
////		if (analysisType != null) {
////			contextValidation.addKeyToRootKeyName("properties");
////			ValidationHelper.validateProperties(contextValidation, properties, analysisType.getPropertyDefinitionByLevel(Level.CODE.File), true);
////			contextValidation.removeKeyFromRootKeyName("properties");
////		}
//		validateFileProperties(contextValidation, properties, analysis);
//	}
	
	/**
	 * Validate file properties in an analysis context.
	 * @param contextValidation validation context
	 * @param properties        file properties
	 * @param analysis          analysis
	 */
	public static void validateFileProperties(ContextValidation contextValidation, Map<String, PropertyValue> properties, Analysis analysis) {
		AnalysisType analysisType = AnalysisType.find.get().findByCode(analysis.typeCode);
		if (analysisType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, analysisType.getPropertyDefinitionByLevel(Level.CODE.File), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}
	}

	// --------------------------------------------------------------
	// Collection validation using some uniqueness criteria.
	
	/**
	 * Map with automatic value creation when accessing values ({@link #get(Object)}).
	 * 
	 * @author vrd
	 *
	 * @param <K> key type
	 * @param <V> value type
	 */
	static class LazyMap<K,V> extends HashMap<K,V> {
		
		/**
		 * Serialization id.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Missing value creator.
		 */
		private final Function<K,V> initializer;
		
		/**
		 * Construct a map with the specified missing value creator.
		 * @param initializer missing value creator
		 */
		public LazyMap(Function<K,V> initializer) {
			this.initializer = initializer;
		}
		
		/**
		 * This creates a value for the requested key if there is none. This make
		 * {@code get(key) == null} not equivalent to {@code containsKey(key)} (which
		 * should not have been used in the first place). 
		 */
		@Override
		public V get(Object o) {
			@SuppressWarnings("unchecked")
			K k = (K)o;
			if (!containsKey(k)) {
				V v = initializer.apply(k);
				put(k, v);
				return v;
			}
			return super.get(k);
		}
		
	}
	
	// ------------------------------------------------------------------------------------------------
	// AJ: the original implementation is validateFiles. The proposed implementation handles nulls
	// as context errors instead of NullPointerException, generate uniqueness errors for all
	// the conflicting elements (not just the ones after the first) and validates all the files
	// is validateUniqueness. The implementation with a behavior closer to the original is validateUniqueness_.
	// Everything is untested.
	
	/**
	 * Validate uniqueness of collection element along a given projection.
	 * @param <A>           element type
	 * @param <B>           key type
	 * @param ctx           validation context
	 * @param as            collection of elements to validate
	 * @param collectionKey error key for the collection
	 * @param attributeKey  error key for the projection attribute
	 * @param projection    uniqueness value projection
	 * @param validation    element validation procedure
	 */
	public static <A,B> void validateUniqueness(ContextValidation               ctx, 
			                                    Collection<A>                   as, 
			                                    String                          collectionKey, 
			                                    String                          attributeKey, 
			                                    Function<A,B>                   projection, 
			                                    BiConsumer<ContextValidation,A> validation) {
		if (CollectionUtils.isEmpty(as))
			return;
		Map<B,List<A>> uniquenessIndex = new LazyMap<>(k -> new ArrayList<>());
		for (A a : as)
			if (a != null)
				uniquenessIndex.get(projection.apply(a)).add(a);
		for (ImmutablePair<Integer,A> p : Iterables.zip(Iterables.range(0), as)) {
			A a = p.right;
			String key = collectionKey + "[" + p.left + "]";
			if (ValidationHelper.validateNotEmpty(ctx, a, key)) { 
				ctx.addKeyToRootKeyName(key);
				validation.accept(ctx, a);
				B b = projection.apply(a);
				if (uniquenessIndex.get(b).size() > 1) 
					ctx.addError(attributeKey, ValidationConstants.ERROR_NOTUNIQUE_MSG, b); 
				ctx.removeKeyFromRootKeyName(key);
			}
		}
	}
	
//	public static void validateFiles(ContextValidation contextValidation, List<File> files) {
//	if (CollectionUtils.isEmpty(files))
//		return;
//	int index = 0;
//	Set<String> lstFullName = new HashSet<>();
//	for (File file : files) {
//		contextValidation.addKeyToRootKeyName("files[" + index + "]");
//		if (!lstFullName.contains(file.fullname)) {
//			file.validate(contextValidation);
//			lstFullName.add(file.fullname);
//		} else { 
//			contextValidation.addError("fullname", ValidationConstants.ERROR_NOTUNIQUE_MSG, file.fullname); 
//		}
//		contextValidation.removeKeyFromRootKeyName("files[" + index + "]");
//		index++;
//	}
//}
	
	// Close to above commented implementation.
	/**
	 * Validate uniqueness of collection element along a given projection.
	 * @param <A>           element type
	 * @param <B>           key type
	 * @param ctx           validation context
	 * @param as            collection of elements to validate
	 * @param collectionKey error key for the collection
	 * @param attributeKey  error key for the projection attribute
	 * @param projection    uniqueness value projection
	 * @param validation    element validation procedure
	 */
	public static <A,B> void validateUniqueness_(ContextValidation               ctx, 
            	                                 Collection<A>                   as, 
            	                                 String                          collectionKey, 
            	                                 String                          attributeKey, 
            	                                 Function<A,B>                   projection, 
            	                                 BiConsumer<ContextValidation,A> validation) {
		if (CollectionUtils.isEmpty(as))
			return;
		Set<B> uniquenessIndex = new HashSet<>();
		for (ImmutablePair<Integer,A> p : Iterables.zip(Iterables.range(0), as)) {
			A element   = p.right;
			B projected = projection.apply(element);
			String key = collectionKey + "[" + p.left + "]";
			ctx.addKeyToRootKeyName(key);
			if (!uniquenessIndex.contains(projected)) {
				validation.accept(ctx, element);
				uniquenessIndex.add(projected);
			} else { 
				ctx.addError(attributeKey, ValidationConstants.ERROR_NOTUNIQUE_MSG, projected); 
			}
			ctx.removeKeyFromRootKeyName(key);
		}
	}

}
