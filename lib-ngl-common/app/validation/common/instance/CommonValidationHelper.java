package validation.common.instance;

import static fr.cea.ig.play.IGGlobals.configuration;
import static validation.utils.ValidationHelper.validateNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.ObjectType.CODE;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.project.instance.Project;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.sample.instance.Sample;
import models.laboratory.valuation.instance.ValuationCriteria;
import models.utils.InstanceConstants;
import ngl.refactoring.MiniDAO;
import rules.services.RulesServices6;
import validation.ContextValidation;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;
import models.administration.authorisation.User;

/**
 * Common validation helper methods. We use optional something as 'either something or null'
 * and required something as 'something not null' as this is supposed to be commonly understood.
 * <p>
 * The reorganized code has two sets of methods: the old methods that are deprecated and whose implementation
 * calls the new version and the renamed versions. 
 * 
 * <p>
 * Factoring and renaming: 
 * 
 * <p>
 * Renaming strategy
 * <ul>
 *   <li>validateExistT : validateTOptional</li>
 *   <li>validateRequiredT : validateTRequired</li>
 * </ul>
 * This replaces exist with optional which is a better fit and provides a better 
 * alphabetical sort in the outline. InstanceCode is renamed to CodeForeign (foreign key) or
 * CodePrimary (primary key).
 * 
 * <p>
 * Argument reordering: validation context is always the first parameter.
 * 
 * <p>
 * Invert implementation logic from exists/optional as the core method to required as the core method
 * <pre>
 *     validateExistT(ContextValidation ctx, T t) { if (t == null) return null else doRequiredValidation; }
 *     validateRequiredT(ContextValidation ctx, T t) { if (t == null) error else validateExist(ctx,t); } 
 * </pre>
 * to
 * <pre>
 *     validateExistT(ContextValidation ctx, T t) { if (t == null) return null else validateRequired(ctx,t); ... }
 *     validateRequiredT(ContextValidation ctx, T t) { doRequiredValidation; }
 * </pre>  
 * .
 * 
 * <p>
 * The two methods validateDescriptionCode and validateInstanceCode only differ by the collection definition
 * which is a DAO in one case and a MongoDB collection name and a type in the other.
 * Those are validation of codes that act as foreign keys, the kept naming is probably
 * validateInstanceCode(Optional|Required).
 * 
 * @author vrd
 *
 */
public class CommonValidationHelper {
	
	private static final play.Logger.ALogger logger = play.Logger.of(CommonValidationHelper.class);
	
	private static final String droolsRulesName                        = "validations";

	/**
	 * Generic code that refers to some instance.
	 */
	public static final String FIELD_CODE = "code";
	
	/**
	 * Generic type code linked to the context (for example: experiment type code).
	 */
	public static final String FIELD_TYPE_CODE = "typeCode";
	
//	@Deprecated
	public static final String FIELD_STATE_CODE                        = "stateCode";
//	@Deprecated
	public static final String FIELD_PREVIOUS_STATE_CODE               = "previousStateCode";
	public static final String FIELD_EXPERIMENT                        = "experiment";
	
	/**
	 * Instrument used ({@link InstrumentUsed}). 
	 */
	public static final String FIELD_INST_USED = "instrumentUsed";
	
	public static final String FIELD_OBJECT_TYPE_CODE                  = "objectTypeCode";
	
	/**
	 * Import type code (String value).
	 */
	public static final String FIELD_IMPORT_TYPE_CODE = "importTypeCode";
	
	@Deprecated
	public static final String FIELD_PROCESS_CREATION_CONTEXT          = "processCreationContext"; // value : COMMON, SPECIFIC
	@Deprecated
	public static final String VALUE_PROCESS_CREATION_CONTEXT_COMMON   = "COMMON";
	@Deprecated
	public static final String VALUE_PROCESS_CREATION_CONTEXT_SPECIFIC = "SPECIFIC";
	
//	@Deprecated
	public static final String OBJECT_IN_DB = "objectInDB";
	
	/**
	 * Validate that the (non null) code does not exist in a given MongoDB collection.
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context 
	 * @param code              code to test existence of
	 * @param type              type of object referenced by the code
	 * @param collectionName    MongoDB collection name
	 * @return                  true if the code does not exist in the collection, false otherwise
	 */
	// RENAME validateCodeDoesNotExist
	public static <T extends DBObject> boolean validateUniqueInstanceCode(ContextValidation contextValidation,
			                                                              String code, 
			                                                              Class<T> type, 
			                                                              String collectionName) {
		if (code == null)
			throw new IllegalArgumentException("code is null");
		if (MongoDBDAO.checkObjectExistByCode(collectionName, type, code)) {
			logger.debug("validateUniqueInstanceCode {} not unique in {}", code, collectionName);
			contextValidation.addError(FIELD_CODE,	ValidationConstants.ERROR_CODE_NOTUNIQUE_MSG, code);
			return false;
		}
		return true;
	}
	
	/** 
	 * Validate that the field value is unique in a given MongoDB collection.
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param key               field name
	 * @param keyValue          field value
	 * @param type              type of collection objects
	 * @param collectionName    MongoDB collection name
	 * @return                  true if the named field with the given value does not exist in the collection, false otherwise 
	 */
	// RENAME validateFieldValueDoesNotExist
	public static <T extends DBObject> boolean validateUniqueFieldValue(ContextValidation contextValidation,
			                                                            String key, 
			                                                            String keyValue, 
			                                                            Class<T> type, 
			                                                            String collectionName) {
		if (key == null)
			throw new IllegalArgumentException("key is null");
		if (keyValue == null)
			throw new IllegalArgumentException("keyValue is null");	
		if (MongoDBDAO.checkObjectExist(collectionName, type, key, keyValue)) {
			contextValidation.addError(key, ValidationConstants.ERROR_NOTUNIQUE_MSG, keyValue);
			return false;
		}
		return true;		
	}
	
	// ----------------------------------------------------------------------------
	// renamed and argument reordered
	
	/**
	 * Validate that a code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code to validate
	 * @param key               error key
	 * @param find              DAO
	 * @deprecated use {@link #validateCodeForeignRequired(ContextValidation, MiniDAO, String, String)}
	 */
	@Deprecated
//	public static <T, U extends MiniDAO<T>> void validateRequiredDescriptionCode(ContextValidation contextValidation, String code, String key, U find) {
	public static <T> void validateRequiredDescriptionCode(ContextValidation contextValidation, String code, String key, MiniDAO<T> find) {
		validateCodeForeignRequired(contextValidation, find, code, key);
	}
	
	/**
	 * Validate that a code references an object using a DAO (CTX_OK). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code to validate
	 * @param key               error key
	 */
//	public static <T, U extends MiniDAO<T>> void validateCodeForeignRequired(ContextValidation contextValidation, String code, String key, U find) {
	public static <T> void validateCodeForeignRequired(ContextValidation contextValidation, MiniDAO<T> find, String code, String key) {
		validateCodeForeignRequired(contextValidation, find, code, key, false);
	}

	// ----------------------------------------------------------------------------
	// renamed and argument reordered
	
	/**
	 * Validate that a code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code to validate
	 * @param key               error key
	 * @param find              DAO
	 * @param returnObject      should a found instance be returned ?
	 * @return                  the instance referenced by the code is found and returnObject is true, null otherwise
	 * @deprecated use {@link #validateCodeForeignRequired(ContextValidation, MiniDAO, String, String, boolean)}
	 */
	@Deprecated 
	public static <T> T validateRequiredDescriptionCode(ContextValidation contextValidation, 
			                                            String code, 
			                                            String key, 
			                                            MiniDAO<T> find, 
			                                            boolean returnObject) {
		return validateCodeForeignRequired(contextValidation, find, code, key, returnObject);
	}
	
	/**
	 * Validate that a code references an object using a DAO (CTX_OK). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code to validate
	 * @param key               error key
	 * @param returnObject      should a found instance be returned ?
	 * @return                  the instance referenced by the code is found and returnObject is true, null otherwise
	 */
	public static <T> T validateCodeForeignRequired(ContextValidation contextValidation, 
			                                        MiniDAO<T> find, 
			                                        String code, 
			                                        String key, 
			                                        boolean returnObject) {
		if (validateNotEmpty(contextValidation, code, key))
			return validateCodeForeignOptional(contextValidation, find, code, key, returnObject);
		return null;		
	}
	
	//---------------------------------------------------------------------------------
	// added
	
	/**
	 * @param <T>               type of object to validate
	 * Validate that a code references an object using a DAO. 
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code to validate
	 * @param returnObject      should a found instance be returned ?
	 * @return                  the instance referenced by the code is found and returnObject is true, null otherwise
	 */
	public static <T> T validateCodeForeignRequired(ContextValidation contextValidation, 
                                                    MiniDAO<T> find, 
                                                    String code, 
                                                    boolean returnObject) {
		return validateCodeForeignRequired(contextValidation, find, code, FIELD_CODE, returnObject);
	}

	// ----------------------------------------------------------------------------
	// renamed and argument reordered
	
	/**
	 * Validate that an optional code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code to validate
	 * @param key               error key
	 * @param find              DAO
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String, String)}
	 */
	@Deprecated
	public static <T> void validateExistDescriptionCode(ContextValidation contextValidation, String code, String key, MiniDAO<T> find) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, find, code, key);
	}

	/**
	 * Validate that an optional code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code to validate
	 * @param key               error key
	 */
	public static <T> void validateCodeForeignOptional(ContextValidation contextValidation, MiniDAO<T> find, String code, String key) {
		 validateCodeForeignOptional(contextValidation, find, code, key, false);
	}
	
	// ---------------------------------------------------------------------------------
	// added
	
	/**
	 * Validate that an optional code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code to validate
	 */	
	public static <T> void validateCodeForeignOptional(ContextValidation contextValidation, MiniDAO<T> find, String code) {
		 validateCodeForeignOptional(contextValidation, find, code, FIELD_CODE, false);
	}

	// ----------------------------------------------------------------------------
	// renamed and argument reordered
	
	/**
	 * Validate that an optional code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param find              DAO
	 * @param returnObject      should a found instance be returned ? 
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String, String, boolean)}
	 */
	@Deprecated
	public static <T> T validateExistDescriptionCode(ContextValidation contextValidation, 
			                                         String code, 
			                                         String key,
			                                         MiniDAO<T> find, 
			                                         boolean returnObject) {
		return validateCodeForeignOptional(contextValidation, find, code, key, returnObject);
	}

	/**
	 * Validate that an optional code references an object using a DAO (foreign key) (CTX_OK). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code
	 * @param key               error key
	 * @param returnObject      should a found instance be returned ? 
	 * @return                  found instance if returnObject is true, null otherwise
	 */
	public static <T> T validateCodeForeignOptional(ContextValidation contextValidation, 
                                                    MiniDAO<T> find, 
                                                    String code, 
                                                    String key,
                                                    boolean returnObject) {
		if (StringUtils.isBlank(code))
			return null;
		if (returnObject) {
			T o = find.findByCode(code);
			if (o == null) {
				logger.debug("validateExistDescriptionCode (ret) - {} does not exist ({})", code, find);
				contextValidation.addError(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
			}
			return o;
		} else {
			if (!find.isCodeExist(code)) {
				logger.debug("validateExistDescriptionCode - {} does not exist ({})", code, find);
				contextValidation.addError(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
			}
			return null;
		}
	}
	
	// ----------------------------------------------------------------
	// added
	
	/**
	 * Validate that an optional code references an object using a DAO (foreign key). 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param find              DAO
	 * @param code              code
	 * @param returnObject      should a found instance be returned ? 
	 * @return                  found instance if returnObject is true, null otherwise
	 */
	public static <T> T validateCodeForeignOptional(ContextValidation contextValidation, 
                                                    MiniDAO<T> find,  
                                                    String code,
                                                    boolean returnObject) {
		return validateCodeForeignOptional(contextValidation, find, code, FIELD_CODE, returnObject);
	}
	
	// ----------------------------------------------------------------------------
	// deprecated
	
	/**
	 * Validate that a code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateCodeForeignRequired(ContextValidation, MiniDAO, String, String)}
	 */
	@Deprecated
	public static <T extends DBObject> void validateRequiredInstanceCode(String code, 
			                                                             String key, 
			                                                             Class<T> type, 
			                                                             String collectionName, 
			                                                             ContextValidation contextValidation) {
		if (validateNotEmpty(contextValidation, code, key))
			validateExistInstanceCode(contextValidation, code, key, type,collectionName);
	}

	// ----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate that a code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param contextValidation validation context
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link #validateCodeForeignRequired(ContextValidation, MiniDAO, String, String, boolean)}  
	 */	
	@Deprecated
	public static <T extends DBObject> T validateRequiredInstanceCode(String code, 
			                                                          String key, 
			                                                          Class<T> type, 
			                                                          String collectionName, 
			                                                          ContextValidation contextValidation, 
			                                                          boolean returnObject) {
		if (validateNotEmpty(contextValidation, code, key))
			return validateExistInstanceCode(contextValidation, code, key, type,collectionName, returnObject);
		else
			return null;
	}

	/**
	 * Validate a required list of optional code using a collection name and a type.
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param codes             list of optional codes to validate
	 * @param key               error key
	 * @param type              collection objects type 
	 * @param collectionName    MongoDB collection name
	 * @param returnObject      should the list of found instances be returned ?
	 * @return                  found instance list if returnObject is true, null otherwise
	 */
	// RENAME: find a proper name
	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ContextValidation  contextValidation,
			                                                                 Collection<String> codes,
			                                                                 String             key, 
			                                                                 Class<T>           type, 
			                                                                 String             collectionName, 
			                                                                 boolean            returnObject) {
		if (validateNotEmpty(contextValidation, codes, key))
			return validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
		else
			return null;
	}
	
	/**
	 * Validate an optional list of optional code using a collection name and a type.
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param codes             list of optional codes to validate
	 * @param key               error key
	 * @param type              collection objects type 
	 * @param collectionName    MongoDB collection name
	 * @param returnObject      should the list of found instances be returned ?
	 * @return                  found instance list if returnObject is true, null otherwise
	 */
	// RENAME: find a proper name
	public static <T extends DBObject> List<T> validateExistInstanceCodes(ContextValidation  contextValidation,
																		  Collection<String> codes, 
																		  String             key, 
																		  Class<T>           type, 
																		  String             collectionName, 
																		  boolean            returnObject) {
		List<T> l = null;
		if (! CollectionUtils.isEmpty(codes)) {
			if (returnObject)
				l = new ArrayList<>();
			for (String code: codes) {
				T o = validateExistInstanceCode(contextValidation, code, key, type, collectionName, returnObject) ;
				if (l != null)
					l.add(o);
			}			
		}
		return l;
	}

	/**
	 * Validate that a code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String, String)}
	 */		
	@Deprecated
	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
			                                                          String code,
			                                                          String key, 
			                                                          Class<T> type, 
			                                                          String collectionName) {
		validateExistInstanceCode(contextValidation, code, key, type, collectionName, false);
	}
	
	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String, String, boolean)} 
	 */	
	@Deprecated
	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
			                                                       String code, 
			                                                       String key, 
			                                                       Class<T> type, 
			                                                       String collectionName, 
			                                                       boolean returnObject) {		
//		if (code == null)
//			throw new IllegalArgumentException("code is null");
		if (code == null)
			return null;
		if (key == null)
			throw new IllegalArgumentException("key is null");
		if (returnObject) {
			T o =  MongoDBDAO.findByCode(collectionName, type, code);
			if (o == null)
				contextValidation.addError(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
			return o;
		} else {
			if (!MongoDBDAO.checkObjectExistByCode(collectionName, type, code)) 
				contextValidation.addError(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
			return null;
		}
	}	
	
	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param code              code
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String)}
	 */	
	@Deprecated
	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation, 
			                                                          String code, 
			                                                          Class<T> type, 
			                                                          String collectionName) {
		validateExistInstanceCode(contextValidation, code, type, collectionName, false);
	}
	
	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               type of object to validate
	 * @param contextValidation validation context
	 * @param code              code
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link #validateCodeForeignOptional(ContextValidation, MiniDAO, String, String)}
	 */
	@Deprecated
	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
			                                                       String code, 
			                                                       Class<T> type, 
			                                                       String collectionName, 
			                                                       boolean returnObject) {
		return validateExistInstanceCode(contextValidation, code, FIELD_CODE, type, collectionName, returnObject);
	}
	
	//------------------------------------------------------------------------------
	// Reorder argument and rename
		
	/**
	 * Validate the id field value of a DBObject (primary key). It must be null when validating creation
	 * and not null in other cases.   
	 * @param dbObject          object to validate id of
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateIdPrimary(ContextValidation, DBObject)}
	 */
	@Deprecated
	public static void validateId_(DBObject dbObject, ContextValidation contextValidation) {
		validateIdPrimary(contextValidation, dbObject);
	}

	/**
	 * Validate the id field value of a DBObject (primary key). It must be null when validating creation
	 * and not null in other cases.   
	 * @param contextValidation validation context
	 * @param dbObject          object to validate id of
	 */
	public static void validateIdPrimary(ContextValidation contextValidation, DBObject dbObject) {
		if (contextValidation.isUpdateMode()) {
    		ValidationHelper.validateNotEmpty(contextValidation, dbObject._id, "_id");
    	} else if (contextValidation.isCreationMode()) {
    		if (dbObject._id != null) // Should probably be (!isEmpty(...)) to be consistent
    			contextValidation.addError("_id", ValidationConstants.ERROR_ID_NOTNULL_MSG);
    	}
	}
	
	//------------------------------------------------------------------------------
	// rename and argument reordering
	
	/**
	 * Validate the code of a DBObject (like a primary key). The code must not exist in the collection when validating creation
	 * and must exist in other cases.
	 * @param contextValidation validation context
	 * @param dbObject          object to validate code of
	 * @param collectionName    name of collection to test
	 * @deprecated use {{@link #validateCodePrimary(ContextValidation, DBObject, String)}
	 */
	@Deprecated
	public static void validateCode(DBObject dbObject, String collectionName, ContextValidation contextValidation) {
		validateCodePrimary(contextValidation, dbObject, collectionName);
	}

	/**
	 * Validate the code of a DBObject (like a primary key). The code must not exist in the collection when validating creation
	 * and must exist in other cases.
	 * @param contextValidation validation context
	 * @param dbObject          object to validate code of
	 * @param collectionName    name of collection to test
	 */
	public static void validateCodePrimary(ContextValidation contextValidation, DBObject dbObject, String collectionName) {
		if (ValidationHelper.validateNotEmpty(contextValidation, dbObject.code, "code")) {
		    if (contextValidation.isCreationMode()) {
				validateUniqueInstanceCode(contextValidation, dbObject.code, dbObject.getClass(), collectionName);		
			} else if (contextValidation.isUpdateMode()) {
				validateExistInstanceCode(contextValidation, dbObject.code, dbObject.getClass(), collectionName);
			} else {
				logger.error("validation code not correctly managed for collection '{}'", collectionName);
			}
		}	
	}

	/**
	 * Méthode permettant de valider qu'un champ (chaine de caractères) a bien une taille en dessous de la taille donnée.
	 * 
	 * @param contextValidation Le contexte de validation de l'objet.
	 * @param field Le champ à valider.
	 * @param fieldValue La valeur du champ à valider.
	 * @param size La taille que le champ ne doit pas dépasser.
	 */
	public static void validateFieldMaxSize(ContextValidation contextValidation, String field, String fieldValue, int size) {
		if (StringUtils.isNotEmpty(fieldValue)) {
			if (fieldValue.length() > size) {
				contextValidation.addError(field, ValidationConstants.ERROR_BADSIZEFIELD, field, fieldValue, size);
			}
		}
	}
	
//	public static void validateCode_(DBObject dbObject, String collectionName, ValidationContext contextValidation) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, dbObject.code, "code")) {
//		    switch (contextValidation.getMode()) {
//		    case CREATION:
//				validateUniqueInstanceCode(contextValidation, dbObject.code, dbObject.getClass(), collectionName);
//				break;
//		    case UPDATE:
//				validateExistInstanceCode(contextValidation, dbObject.code, dbObject.getClass(), collectionName);
//				break;
//			default:
//				logger.error("Problem : validation code not correctly managed for collection '{}'", collectionName);
//			}
//		}	
//	}

	// ----------------------------------------------------------------------------
	// Argument reordering
	
	/**
	 * Validate a trace information.
	 * @param contextValidation validation context
	 * @param traceInformation  trace information to validate
	 * @deprecated use validateTraceInformationRequired(ContextValidation,TraceInformation)
	 */
	@Deprecated
	public static void validateTraceInformation(TraceInformation traceInformation, ContextValidation contextValidation) {
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}

	/**
	 * Validate a trace information.
	 * @param contextValidation validation context
	 * @param traceInformation  trace information to validate
	 */
	public static void validateTraceInformationRequired(ContextValidation contextValidation, TraceInformation traceInformation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, traceInformation, "traceInformation")) {
			//contextValidation.addKeyToRootKeyName("traceInformation");
			traceInformation.validate(contextValidation.appendPath("traceInformation"));
			//contextValidation.removeKeyFromRootKeyName("traceInformation");
		}		
	}
	
//	/**
//	 * Validate a trace information (CTX_OK).
//	 * @param contextValidation validation context
//	 * @param traceInformation  trace information to validate
//	 */
//	public static void validateTraceInformationRequired(ContextValidation contextValidation, TraceInformation traceInformation) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, traceInformation, "traceInformation"))
//			traceInformation.validate(contextValidation.appendPath("traceInformation"));
//	}

	// ----------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a state code (context parameter {@link CommonValidationHelper#FIELD_TYPE_CODE} 
	 * or {@link CommonValidationHelper#FIELD_OBJECT_TYPE_CODE} or none).
	 * @param stateCode         state code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateStateCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateStateCode(String stateCode, ContextValidation contextValidation) {
		CommonValidationHelper.validateStateCodeRequired(contextValidation, stateCode);
	}

	/**
	 * Validate a state code (context parameter {@link CommonValidationHelper#FIELD_TYPE_CODE} 
	 * or {@link CommonValidationHelper#FIELD_OBJECT_TYPE_CODE} or none).
	 * @param contextValidation validation context
	 * @param stateCode         state code
	 */
	@Deprecated
	public static void validateStateCodeRequired(ContextValidation contextValidation, String stateCode) {
//		if (contextValidation.getContextObjects().containsKey(FIELD_TYPE_CODE)) {
		if (contextValidation.containsKey(FIELD_TYPE_CODE)) {
//			String typeCode = getObjectFromContext(FIELD_TYPE_CODE, String.class, contextValidation);
			String typeCode = contextValidation.getTypedObject(FIELD_TYPE_CODE);
			validateStateCodeRequired(contextValidation, typeCode, stateCode);
//		} else if (contextValidation.getContextObjects().containsKey(FIELD_OBJECT_TYPE_CODE)) {
		} else if (contextValidation.containsKey(FIELD_OBJECT_TYPE_CODE)) {
//			ObjectType.CODE objectTypeCode = getObjectFromContext(FIELD_OBJECT_TYPE_CODE, ObjectType.CODE.class, contextValidation);
			ObjectType.CODE objectTypeCode = contextValidation.getTypedObject(FIELD_OBJECT_TYPE_CODE);
			validateStateCodeRequired(contextValidation, objectTypeCode, stateCode);
		} else {
			validateCodeForeignRequired(contextValidation, models.laboratory.common.description.State.miniFind.get(), stateCode, "state.code");
		}
	}

	// ----------------------------------------------------------------------------
	// Argument reordering
	
	/**
	 * Validate state object.
	 * @param contextValidation validation context
	 * @param typeCode          type code
	 * @param state             state
	 * @deprecated use validateStateRequired(ContextValidation,String,State)
	 */
	@Deprecated
	public static void validateState(String typeCode, State state, ContextValidation contextValidation) {
		validateStateRequired(contextValidation, typeCode, state);
	}

	/**
	 * Validate state object.
	 * @param contextValidation validation context
	 * @param typeCode          type code
	 * @param state             state
	 */
	public static void validateStateRequired(ContextValidation contextValidation, String typeCode, State state) {
		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state")) {
//			contextValidation.putObject(FIELD_TYPE_CODE, typeCode);
			contextValidation.addKeyToRootKeyName("state");
			state.validate(contextValidation, typeCode);
			contextValidation.removeKeyFromRootKeyName("state");
//			contextValidation.removeObject(FIELD_TYPE_CODE);
		}		
	}

//	/**
//	 * Validate state object (CTX_OK).
//	 * @param contextValidation validation context
//	 * @param typeCode          type code
//	 * @param state             state
//	 */
//	public static void validateStateRequired(ContextValidation contextValidation, String typeCode, State state) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state"))
//			state.validate(contextValidation.appendPath("state"), typeCode);
//	}
	
	// ----------------------------------------------------------------------------
	// Argument reordering	
	
	/**
	 * Validate state object.
	 * @param contextValidation validation context
	 * @param objectTypeCode    type code
	 * @param state             state
	 * @deprecated use validateStateRequired(ObjectType.CODE,String,State)
	 */
	@Deprecated
	public static void validateState(CODE objectTypeCode, State state, ContextValidation contextValidation) {
		validateStateRequired(contextValidation, objectTypeCode, state);
	}

	/**
	 * Validate state object.
	 * @param contextValidation validation context
	 * @param objectTypeCode    type code
	 * @param state             state
	 */
	public static void validateStateRequired(ContextValidation contextValidation, CODE objectTypeCode, State state) {
		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state")) {
//			contextValidation.putObject(FIELD_OBJECT_TYPE_CODE, objectTypeCode);
//			contextValidation.addKeyToRootKeyName("state");
//			state.validate(contextValidation.appendPath("state").withObject(FIELD_OBJECT_TYPE_CODE, objectTypeCode));
			state.validate(contextValidation.appendPath("state"), objectTypeCode);
//			contextValidation.removeKeyFromRootKeyName("state");
//			contextValidation.removeObject(FIELD_OBJECT_TYPE_CODE);
		}		
	}
	
//	/**
//	 * Validate state object (CTX_OK).
//	 * @param contextValidation validation context
//	 * @param objectTypeCode    type code
//	 * @param state             state
//	 */
//	public static void validateStateRequired(ContextValidation contextValidation, CODE objectTypeCode, State state) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state")) 
//			state.validate(contextValidation.appendPath("state"), objectTypeCode);
//	}
	
	// ----------------------------------------------------------------------------
	// renamed and argument reordered
	
//	private static void validateStateCode(String typeCode, String stateCode, ContextValidation contextValidation) {
//		try {
//			if (validateNotEmpty(contextValidation, stateCode, "code")) {
//				if (!models.laboratory.common.description.State.find.get().isCodeExistForTypeCode(stateCode, typeCode)) {
//					logger.debug("state code '{}' does not exist for type code '{}'", stateCode, typeCode);
//					contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, stateCode);
//				}
//			}
//		} catch(DAOException e) {
//			throw new RuntimeException(e);
//		}
//	}
	public static void validateStateCodeRequired(ContextValidation contextValidation, String typeCode, String stateCode) {
		if (validateNotEmpty(contextValidation, stateCode, "code")) {
			if (!models.laboratory.common.description.State.find.get().isCodeExistForTypeCode(stateCode, typeCode)) {
				logger.debug("state code '{}' does not exist for type code '{}'", stateCode, typeCode);
				contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, stateCode);
			}
		}
	}

	// ----------------------------------------------------------------------------
	// rename and argument reordered
	
//	protected static void validateStateCode(ObjectType.CODE objectType, String stateCode, ContextValidation contextValidation) {
//		try {
//			if (validateNotEmpty(contextValidation, stateCode, "code")) {
//				if (!models.laboratory.common.description.State.find.get().isCodeExistForObjectTypeCode(stateCode, objectType)) {
//					contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, stateCode);
//				}
//			}
//		} catch(DAOException e) {
//			throw new RuntimeException(e);
//		}	
//	}
	public static void validateStateCodeRequired(ContextValidation contextValidation, ObjectType.CODE objectType, String stateCode) {
		if (validateNotEmpty(contextValidation, stateCode, "code")) {
			if (!models.laboratory.common.description.State.find.get().isCodeExistForObjectTypeCode(stateCode, objectType)) {
				contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, stateCode);
			}
		}
	}

	// ----------------------------------------------------------------------------
	// Arguments reordered
	
	/**
	 * Validate resolution codes (context parameter {@link #FIELD_TYPE_CODE} or {@link #FIELD_OBJECT_TYPE_CODE}).
	 * @param resoCodes         resolution codes
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateResolutionCodes(ContextValidation, Set)}
	 */
	@Deprecated
	public static void validateResolutionCodes(Set<String> resoCodes, ContextValidation contextValidation) {
		validateResolutionCodes(contextValidation, resoCodes);
	}

	/**
	 * Validate resolution codes (context parameter {@link #FIELD_TYPE_CODE} or {@link #FIELD_OBJECT_TYPE_CODE}).
	 * @param contextValidation validation context
	 * @param resoCodes         resolution codes
	 */
	@Deprecated
	public static void validateResolutionCodes(ContextValidation contextValidation, Set<String> resoCodes) {
		if (contextValidation.containsKey(FIELD_TYPE_CODE)) {
			String typeCode = contextValidation.getTypedObject(FIELD_TYPE_CODE);
			validateResolutionCodes(contextValidation, typeCode, resoCodes);
		} else if (contextValidation.containsKey(FIELD_OBJECT_TYPE_CODE)){
			ObjectType.CODE objectTypeCode = contextValidation.getTypedObject(FIELD_OBJECT_TYPE_CODE);
			validateResolutionCodes(contextValidation, objectTypeCode, resoCodes);
		} 				
	}
	
	// ----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate an optional collection of required resolution codes that must have the 
	 * given type code.
	 * @param typeCode          type code
	 * @param resoCodes         resolution codes
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateResolutionCodes(ContextValidation, String, Set)}
	 */
	@Deprecated
	public static void validateResolutionCodes_(String typeCode, Set<String> resoCodes, ContextValidation contextValidation) {
		CommonValidationHelper.validateResolutionCodes(contextValidation, typeCode, resoCodes);
	}

	/**
	 * Validate an optional collection of required resolution codes that must have the 
	 * given type code.
	 * @param contextValidation validation context
	 * @param typeCode          type code
	 * @param resoCodes         resolution codes
	 */
	public static void validateResolutionCodes(ContextValidation contextValidation, String typeCode, Set<String> resoCodes) {
		if (resoCodes == null) 
			return;
		int i = 0;
		for (String resoCode : resoCodes) {
			List<String> typeCodes = new ArrayList<>();
			typeCodes.add(typeCode);
			if (! MongoDBDAO.checkObjectExist(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, DBQuery.and(DBQuery.is("resolutions.code", resoCode), DBQuery.in("typeCodes", typeCodes)))) {
				contextValidation.addError("resolutionCodes["+i+"]", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, resoCode);
			}
			i++;
		}
	}
	
//	public static void validateResolutionCodes_(ContextValidation contextValidation, String typeCode, Set<String> resoCodes) {
//		Iterables.index(0, resoCodes)
//		         .unzipEach((i, resoCode) -> {
//		        	 if (! MongoDBDAO.checkObjectExist(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, 
//		        			                           DBQuery.and(DBQuery.is("resolutions.code", resoCode), 
//		        			                        		       DBQuery.in("typeCodes",        Arrays.asList(typeCode))))) 
//		        		 contextValidation.addError("resolutionCodes[" + i + "]", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, resoCode);
//		         });
//	}
	
	// ----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate an optional collection of required resolution codes.
	 * @param contextValidation validation context
	 * @param objectTypeCode    object type code
	 * @param resoCodes         codes to validate
	 * @deprecated use {@link #validateResolutionCodes(ContextValidation, ObjectType.CODE, Set)}
	 */
	@Deprecated
	public static void validateResolutionCodes(CODE objectTypeCode, Set<String> resoCodes, ContextValidation contextValidation) {
		CommonValidationHelper.validateResolutionCodes(contextValidation, objectTypeCode, resoCodes);
	}

	/**
	 * Validate an optional collection of required resolution codes.
	 * @param contextValidation validation context
	 * @param objectTypeCode    object type code
	 * @param resoCodes         codes to validate
	 */
	public static void validateResolutionCodes(ContextValidation contextValidation, ObjectType.CODE objectTypeCode, Set<String> resoCodes) {
		if (resoCodes == null) 
			return;
		int i = 0;
		for (String resoCode : resoCodes) {
			if (! MongoDBDAO.checkObjectExist(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, DBQuery.and(DBQuery.is("resolutions.code", resoCode), DBQuery.is("objectTypeCode", objectTypeCode.toString())))) {
				contextValidation.addError("resolutionCodes["+i+"]", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, resoCode);
			}
			i++;
		}
	}
	
	// -------------------------------------------------------------------
	// arguments reordered
		
	/**
	 * Validate a criteria code. 
	 * @param criteriaCode      criteria code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateCriteriaCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateCriteriaCode(String criteriaCode, ContextValidation contextValidation) {
		CommonValidationHelper.validateCriteriaCode(contextValidation, criteriaCode);
	}

	/**
	 * Validate a criteria code (context parameter {@link #FIELD_TYPE_CODE}). 
	 * @param contextValidation validation context
	 * @param criteriaCode      criteria code
	 */
	@Deprecated
	public static void validateCriteriaCode(ContextValidation contextValidation, String criteriaCode) {
//		String typeCode = getObjectFromContext(FIELD_TYPE_CODE, String.class, contextValidation);
		String typeCode = contextValidation.getTypedObject(FIELD_TYPE_CODE);
		validateCriteriaCode(contextValidation, typeCode, criteriaCode);		
	}

	// -------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate a criteria code. 
	 * @param typeCode          type code
	 * @param criteriaCode      criteria code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateCriteriaCode(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateCriteriaCode_(String typeCode, String criteriaCode, ContextValidation contextValidation) {
		CommonValidationHelper.validateCriteriaCode(contextValidation, typeCode, criteriaCode);
	}

	/**
	 * Validate a criteria code. 
	 * @param contextValidation validation context
	 * @param typeCode          type code
	 * @param criteriaCode      criteria code
	 */
	public static void validateCriteriaCode(ContextValidation contextValidation, String typeCode, String criteriaCode) {
		if (criteriaCode != null) {
			Query q = DBQuery.and(DBQuery.is("code", criteriaCode), DBQuery.in("typeCodes", typeCode));
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, q)) {
				contextValidation.addError("criteriaCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, criteriaCode);
			}
		}		
	}
	
	// ----------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate valuation.
	 * @param typeCode          type code
	 * @param valuation         valuation
	 * @param contextValidation validation context
	 * @deprecated use validateValuationRequired(ContextValidation,String,Valuation)
	 */
	@Deprecated
	public static void validateValuation(String typeCode, Valuation valuation, ContextValidation contextValidation) {
		validateValuationRequired(contextValidation, typeCode, valuation);
	}

	/**
	 * Validate valuation.
	 * @param contextValidation validation context
	 * @param typeCode          type code
	 * @param valuation         valuation
	 */
	public static void validateValuationRequired(ContextValidation contextValidation, String typeCode, Valuation valuation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, valuation, "valuation")) {
//			contextValidation.putObject(FIELD_TYPE_CODE, typeCode);
			contextValidation.addKeyToRootKeyName("valuation");
			valuation.validate(contextValidation, typeCode);
			contextValidation.removeKeyFromRootKeyName("valuation");
//			contextValidation.removeObject(FIELD_TYPE_CODE);
		}		
	}
	
//	/**
//	 * Validate valuation (CTX_OK).
//	 * @param contextValidation validation context
//	 * @param typeCode          type code
//	 * @param valuation         valuation
//	 */
//	public static void validateValuationRequired(ContextValidation contextValidation, String typeCode, Valuation valuation) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, valuation, "valuation"))
//			valuation.validate(contextValidation.appendPath("valuation"), typeCode);
//	}

	// ----------------------------------------------------------------------------

//	public static void validateValuation_(String typeCode, Valuation valuation, ContextValidation contextValidation) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, valuation, "valuation"))
//			valuation.validate(contextValidation.appendPath("valuation").withObject(FIELD_TYPE_CODE, typeCode));
//	}
	
////	@SuppressWarnings("unchecked")
//	protected static <T> T getObjectFromContext(String key, Class<T> clazz, ContextValidation contextValidation) {
////		@SuppressWarnings("unchecked") 
////		T o = (T) contextValidation.getObject(key);
//		if (o == null)
//			throw new IllegalArgumentException(key + " from contextValidation is null");
//		return o;
//	}

//	protected static <T> T getObjectFromContext(String key, Class<T> clazz, ContextValidation contextValidation) {
//		Object o = contextValidation.getObject(key);
//		if (o == null)
//			throw new IllegalArgumentException(key + " from contextValidation is null");
//		T t = clazz.cast(o);
//		if (t == null)
//			throw new IllegalArgumentException(key + " from contextValidation is not of type " + clazz.getName());
//		return t;
//	}

//	public static void validateProjectCodes(List<String> projectCodes, ValidationContext contextValidation) {
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, projectCodes, "projectCodes",Project.class,InstanceConstants.PROJECT_COLL_NAME,false);
//	}
	
	// ----------------------------------------------------------------------------
	// Argument reordering
	
	/**
	 * Validate a required list of optional project codes.
	 * @param projectCodes      project codes
	 * @param contextValidation validation context
	 * @deprecated use {@literal validateProjectCodes(ContextValidation,Collection<String>)}
	 */
	@Deprecated
	public static void validateProjectCodes(Collection<String> projectCodes, ContextValidation contextValidation) {
		validateProjectCodes(contextValidation, projectCodes);
	}

	/**
	 * Validate a required list of optional project codes.
	 * @param contextValidation validation context
	 * @param projectCodes      project codes
	 */
	public static void validateProjectCodes(ContextValidation contextValidation, Collection<String> projectCodes) {
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, projectCodes, "projectCodes",Project.class,InstanceConstants.PROJECT_COLL_NAME,false);
		validateRequiredInstanceCodes(contextValidation, projectCodes, "projectCodes", Project.class, InstanceConstants.PROJECT_COLL_NAME, false);
	}
	
//	public static void validateProjectCodes(Set<String> projectCodes, ValidationContext contextValidation) {
//		List<String> listProject = null;
//		if (CollectionUtils.isNotEmpty(projectCodes)) {
//			listProject = new ArrayList<>();
//			listProject.addAll(projectCodes);
//		}
//		validateProjectCodes(listProject,contextValidation);
//	}
	
	// ----------------------------------------------------------------------------
	// Argument reordering
	
	/**
	 * Validate a project code.
	 * @param contextValidation validation context
	 * @param projectCode       project code
	 * @deprecated use {@link #validateProjectCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProjectCode(String projectCode, ContextValidation contextValidation) {
		validateProjectCodeRequired(contextValidation, projectCode);
	}
	
	/**
	 * Validate a project code (CTX_OK).
	 * @param contextValidation validation context
	 * @param projectCode       project code
	 */
	public static void validateProjectCodeRequired(ContextValidation contextValidation, String projectCode) {
		validateCodeForeignRequired(contextValidation, Project.find.get(), projectCode, "projectCode");
	}

	// ----------------------------------------------------------------------------

//	public static void validateSampleCodes(List<String> sampleCodes, ValidationContext contextValidation) {
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, sampleCodes,"sampleCodes",Sample.class,InstanceConstants.SAMPLE_COLL_NAME,false);
//	}
//	
//	public static void validateSampleCodes(Set<String> sampleCodes,ValidationContext contextValidation){
//		List<String> listSample = null;
//		if (CollectionUtils.isNotEmpty(sampleCodes)) {
//			listSample = new ArrayList<>();
//			listSample.addAll(sampleCodes);
//		}
//		validateSampleCodes(listSample,contextValidation);
//	}
	
	// Arguments reordered
	
	/**
	 * Validate a required list of optional sample codes.
	 * @param sampleCodes       sample codes to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCodes(ContextValidation, Collection)}
	 */
	@Deprecated
	public static void validateSampleCodes(Collection<String> sampleCodes, ContextValidation contextValidation) {
		CommonValidationHelper.validateSampleCodes(contextValidation, sampleCodes);
	}

	/**
	 * Validate a required list of optional sample codes.
	 * @param contextValidation validation context
	 * @param sampleCodes       sample codes to validate
	 */
	public static void validateSampleCodes(ContextValidation contextValidation, Collection<String> sampleCodes) {
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, sampleCodes,"sampleCodes",Sample.class,InstanceConstants.SAMPLE_COLL_NAME,false);
		validateRequiredInstanceCodes(contextValidation, sampleCodes,"sampleCodes",Sample.class,InstanceConstants.SAMPLE_COLL_NAME, false);
	}	
	
	// ----------------------------------------------------------------------------
	// rename and argument reorder
	
	/**
	 * Validate a sample code.
	 * @param sampleCode        sample code
	 * @param projectCode       project code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCodeRequired(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateSampleCode(String sampleCode, String projectCode, ContextValidation contextValidation) {
		CommonValidationHelper.validateSampleCodeRequired(contextValidation, sampleCode, projectCode);
	}

	/**
	 * Validate a sample code.
	 * @param contextValidation validation context
	 * @param sampleCode        sample code
	 * @param projectCode       project code
	 */
	public static void validateSampleCodeRequired(ContextValidation contextValidation, String sampleCode, String projectCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, sampleCode, "sampleCode")) {
			Sample sample =  MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,  sampleCode);
			if (sample == null) {
				contextValidation.addError("sampleCode", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, sampleCode);
			} else {
				if ((sample.projectCodes == null) || (!sample.projectCodes.contains(projectCode))) { 
					contextValidation.addError("projectCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, projectCode);
				}
			}
		}
	}
	
	// -----------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate a container code.
	 * @param contextValidation validation context
	 * @param containerCode     container code
	 * @param propertyName      property name (error path)
	 * @deprecated {@link #validateContainerCodeRequired(ContextValidation, String, String)} 
	 */
	@Deprecated
	public static void validateContainerCode(String containerCode, ContextValidation contextValidation, String propertyName) {
		validateContainerCodeRequired(contextValidation, containerCode, propertyName);
	}

	/**
	 * Validate a container code.
	 * @param contextValidation validation context
	 * @param containerCode     container code
	 * @param propertyName      property name (error path)
	 */
	public static void validateContainerCodeRequired(ContextValidation contextValidation, String containerCode, String propertyName) {
//		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME);
//		validateRequiredInstanceCode(containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, contextValidation);
		validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName);
	}
	
	// -----------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate a container support code (foreign key).
	 * @param containerSupportCode container support code
	 * @param contextValidation    validation context
	 * @param propertyName         error key
	 * @deprecated use {@link #validateContainerSupportCodeRequired(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateContainerSupportCode(String containerSupportCode, ContextValidation contextValidation, String propertyName) {
		validateContainerSupportCodeRequired(contextValidation, containerSupportCode, propertyName);
	}
		
	/**
	 * Validate a container support code (foreign key) (CTX_OK).
	 * @param contextValidation    validation context
	 * @param containerSupportCode container support code
	 * @param propertyName         error key
	 */
	public static void validateContainerSupportCodeRequired(ContextValidation contextValidation, String containerSupportCode, String propertyName) {
		validateCodeForeignRequired(contextValidation, ContainerSupport.find.get(), containerSupportCode, propertyName);		
	}
	
	// -----------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate rules with the provided object list as facts.
	 * @param objects           "facts"
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRulesWithList(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateRules(List<Object> objects, ContextValidation contextValidation) {
		CommonValidationHelper.validateRulesWithList(contextValidation, objects);
	}

//	/**
//	 * Validate rules with the provided objects as facts.
//	 * @param contextValidation validation context
//	 * @param objects           "facts"
//	 * @deprecated use {@link #validateRules_(ContextValidation, Object...)}
//	 */
//	@Deprecated
	/**
	 * Validate rules with the provided object list as facts (rules key from configuration, rules name "validations").
	 * @param contextValidation validation context
	 * @param objects           "facts"
	 */
//	public static void validateRulesWithList(ContextValidation contextValidation, List<Object> objects) {
//		ArrayList<Object> facts = new ArrayList<>();
//		facts.addAll(objects);
////		ValidationContext validationRules = new ContextValidation(contextValidation.getUser());
//		ContextValidation validationRules = ContextValidation.createUndefinedContext(contextValidation.getUser());
//		facts.add(validationRules);
////		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), nameRules, facts);
//		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(configuration().getString("rules.key"), droolsRulesName, facts);
//		for (Object obj : factsAfterRules) {
//			if (ContextValidation.class.isInstance(obj)) {
//				// logger.debug("validateRules/errors " + (((ContextValidation) obj).errors.size()));
//				contextValidation.getErrors().putAll(((ContextValidation) obj).getErrors());
//			}
//		}
//	}
	public static void validateRulesWithList(ContextValidation contextValidation, List<Object> objects) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.addAll(objects);
		facts.add(ContextValidation.createUndefinedContext(contextValidation.getUser()));
		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(configuration().getString("rules.key"),
				                                                                              droolsRulesName, 
				                                                                              facts);
		for (Object obj : factsAfterRules) 
			if (ContextValidation.class.isInstance(obj)) 
				contextValidation.getErrors().putAll(((ContextValidation) obj).getErrors());
	}
	
	/**
	 * Validate rules with the provided objects as facts.
	 * @param ctx     validation context
	 * @param objects "facts"
	 */
//	public static void validateRulesWithObjects(ContextValidation ctx, Object... objects) {
//		ArrayList<Object> facts = new ArrayList<>();
//		facts.addAll(Arrays.asList(objects));
//		facts.add(ContextValidation.createUndefinedContext(ctx.getUser()));
//		List<Object> factsAfterRules = RulesServices6
//				.getInstance()
//				.callRulesWithGettingFacts(configuration().getString("rules.key"), 
//						                   droolsRulesName, 
//						                   facts);
//		for (Object obj : factsAfterRules)
//			if (obj instanceof ContextValidation)
//				ctx.getErrors().putAll(((ContextValidation) obj).getErrors());
//	}
	public static void validateRulesWithObjects(ContextValidation ctx, Object... objects) {
		validateRulesWithList(ctx, Arrays.asList(objects));
	}
	
	// -----------------------------------------------------------------------
	// argument reordered and generalized using Collection that is a super class
	// of List and Set.
	
	/*
	public static void validateRules(Object object,ContextValidation contextValidation){
		List<Object> list=new ArrayList<Object>();
		list.add(object);
		validateRules(list,contextValidation);
	}
	*/

//	public static void validateExperimentTypeCodes(List<String> experimentTypeCodes, ValidationContext contextValidation) {
//		if (experimentTypeCodes != null) 
//			for (String s : experimentTypeCodes) 
//				BusinessValidationHelper.validateExistDescriptionCode(contextValidation, s, "experimentTypeCodes", ExperimentType.find.get());
//	}

//	public static void validateExperimentTypeCodes(Set<String> experimentTypeCodes, ValidationContext contextValidation) {
//		List<String> arrayExperiments = null;
//		if (CollectionUtils.isNotEmpty(experimentTypeCodes)) {
//			arrayExperiments = new ArrayList<>();
//			arrayExperiments.addAll(experimentTypeCodes);
//		}
//		validateExperimentTypeCodes(arrayExperiments,contextValidation); 
//	}
	/**
	 * Validate a collection of experiment type codes.
	 * @param experimentTypeCodes experiment types to validate
	 * @param contextValidation   validation context
	 * @deprecated use {@link #validateExperimentTypeCodes(ContextValidation, Collection)}
	 */
	@Deprecated
	public static void validateExperimentTypeCodes(Collection<String> experimentTypeCodes, ContextValidation contextValidation) {
		CommonValidationHelper.validateExperimentTypeCodes(contextValidation, experimentTypeCodes);
	}

	/**
	 * Validate a collection of experiment type codes.
	 * @param contextValidation   validation context
	 * @param experimentTypeCodes experiment types to validate
	 */
	public static void validateExperimentTypeCodes(ContextValidation contextValidation, Collection<String> experimentTypeCodes) {
		if (experimentTypeCodes != null) 
			for (String s : experimentTypeCodes) 
//				BusinessValidationHelper.validateExistDescriptionCode(contextValidation, s, "experimentTypeCodes", ExperimentType.find.get());
//				validateExistDescriptionCode(contextValidation, s, "experimentTypeCodes", ExperimentType.miniFind.get());
				validateCodeForeignOptional(contextValidation, ExperimentType.miniFind.get(), s, "experimentTypeCodes");
	}
	
	// ----------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an experiment code.
	 * @param expCode           experiment code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentCodeOptional(ContextValidation, String)} 
	 */
	@Deprecated
	public static void validateExperimenCode(String expCode, ContextValidation contextValidation) {
		CommonValidationHelper.validateExperimentCodeOptional(contextValidation, expCode);
	}
	
	/**
	 * Validate an experiment code.
	 * @param contextValidation validation context
	 * @param expCode           experiment code
	 */
	public static void validateExperimentCodeOptional(ContextValidation contextValidation, String expCode) {
//		BusinessValidationHelper.validateExistInstanceCode(contextValidation, "experimentCode", expCode, Experiment.class, InstanceConstants.EXPERIMENT_COLL_NAME);
//		validateExistInstanceCode(contextValidation, "experimentCode", expCode, Experiment.class, InstanceConstants.EXPERIMENT_COLL_NAME);
		validateCodeForeignOptional(contextValidation, Experiment.find.get(), "experimentCode", expCode);
	}

    public static void validateComment(ContextValidation contextValidation, String comment) {
		if (comment == null || comment.length() ==0) {
			contextValidation.addError("comment", ValidationConstants.ERROR_REQUIRED_MSG);
		}
	}
	public static void validateName(ContextValidation contextValidation, String name) {
		if (name == null || name.length() ==0) {
			contextValidation.addError("name", ValidationConstants.ERROR_REQUIRED_MSG);
		}
	}

	public static void validateUser(ContextValidation contextValidation, String login) {
		if(login == null){
			contextValidation.addError("user", ValidationConstants.ERROR_REQUIRED_MSG);
		}else {
			User user = User.find.get().findByLogin(login);
			if(user == null){
				contextValidation.addError("user", ValidationConstants.ERROR_USER_NOT_EXIST);
			}
		}
	}

	public static void validateObjMaj(ContextValidation contextValidation, String objMAjJson) {
		if(!(objMAjJson.startsWith("{") || objMAjJson.endsWith("}"))){
			contextValidation.addError("objMAjJson", ValidationConstants.ERROR_BAD_FORMAT);
		}
	}
    
	public static void validateStringField(ContextValidation contextValidation, String fieldValue,String fieldName){
		if(fieldValue == null || fieldValue.length() == 0){
			contextValidation.addError(fieldName, ValidationConstants.ERROR_REQUIRED_MSG);
		}
	}

//	public static <T> T validateCodeForeignRequired(ContextValidation contextValidation, 
//			                                        Class<T> type, 
//			                                        String code, 
//			                                        String key, 
//			                                        boolean returnObject) {
//		if (validateNotEmpty(contextValidation, code, key))
//			return validateCodeForeignOptional(contextValidation, find, code, key, returnObject);
//		return null;		
//	}

	
}
