package validation.utils;

import java.util.Collection;
import java.util.List;

import fr.cea.ig.DBObject;
import ngl.refactoring.MiniDAO;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

/**
 * Helper to validate MongoDB Object Used before insert or update a MongoDB
 * object.
 * 
 * @deprecated use {@link CommonValidationHelper}
 * 
 * @author galbini
 * 
 */
@Deprecated
public class BusinessValidationHelper {
	
//	private static final String FIELD_CODE = "code";
	
//	/*
//	 * Validate if code is unique in MongoDB collection
//	 * Unique code is validate if key "_id" not in map contextObjects or if value of key "_id" is null else no code validation
//	 * @param contextValidation
//	 * @param code
//	 * @param type
//	 * @return collectionName
//	 */
//	public static <T extends DBObject> boolean validateUniqueInstanceCode(ContextValidation contextValidation, 
//			                                                              String code, 
//			                                                              Class<T> type, 
//			                                                              String collectionName) {
//		if (code == null)
//			throw new IllegalArgumentException("code is null");	
//		if (MongoDBDAO.checkObjectExistByCode(collectionName, type, code)) {
//			contextValidation.addErrors(FIELD_CODE,	ValidationConstants.ERROR_CODE_NOTUNIQUE_MSG, code);
//			return false;
//		}
//		return true;
//	}
	
	
//	/*
//	 * Validate if field value is unique in MongoDB collection
//	 * @param errors
//	 * @param key : field name
//	 * @param keyValue : field value
//	 * @param type : type DBObject
//	 * @param collectionName : Mongo collection name
//	 * @param returnObject
//	 * @return boolean
//	 */	
//	public static <T extends DBObject> boolean validateUniqueFieldValue(ContextValidation contextValidation,
//			                                                            String key, 
//			                                                            String keyValue, 
//			                                                            Class<T> type, 
//			                                                            String collectionName) {
//		if (key == null)
//			throw new IllegalArgumentException("key is null");
//		if (keyValue == null)
//			throw new IllegalArgumentException("keyValue is null");
//		if (MongoDBDAO.checkObjectExist(collectionName, type, key, keyValue)) {
//			contextValidation.addErrors(key, ValidationConstants.ERROR_NOTUNIQUE_MSG, keyValue);
//			return false;
//		}
//		return true;
//	}
	
//	/*
//	 * 
//	 * @param contextValidation
//	 * @param code
//	 * @param key
//	 * @param find
//	 */
////	public static <T> void validateRequiredDescriptionCode(ContextValidation contextValidation, String code, String key,
////			Finder<T,? extends AbstractDAO<T>> find) {
////		 validateRequiredDescriptionCode(contextValidation, code, key, find,false);
////	}
//	public static <T, U extends AbstractDAO<T>> void validateRequiredDescriptionCode(ContextValidation contextValidation, 
//			                                                                         String code, 
//			                                                                         String key,
//			                                                                         U find) {
//		 validateRequiredDescriptionCode(contextValidation, code, key, find,false);
//	}
	
	/**
	 * Validate that a code references an object using a DAO. 
	 * @param <T>               element type
	 * @param <U>               DAO type
	 * @param contextValidation validation context
	 * @param code              code to validate
	 * @param key               error key
	 * @param find              DAO
	 * @deprecated use {@link CommonValidationHelper#validateCodeForeignRequired(ContextValidation, MiniDAO, String, String) }
	 */
	@Deprecated 
	public static <T, U extends MiniDAO<T>> void validateRequiredDescriptionCode(ContextValidation contextValidation, 
			                                                                         String code, 
			                                                                         String key,
			                                                                         U find) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, find, code, key);
	}

	
//	/*
//	 * Validate i a description code is not null and exist in description DB
//	 * @param errors
//	 * @param code
//	 * @param key
//	 * @param find
//	 * @param returnObject
//	 * @return object de T or null if returnObject is false
//	 */
//	public static <T, U extends AbstractDAO<T>> T validateRequiredDescriptionCode(ContextValidation contextValidation, 
//			                                                                      String code, 
//			                                                                      String key,
//			                                                                      U find, 
//			                                                                      boolean returnObject) {
//		T o = null;
//		if (validateNotEmpty(contextValidation, code, key)) {
//			o = validateExistDescriptionCode(contextValidation, code, key, find, returnObject);
//		}
//		return o;		
//	}

	
	/**
	 * Validate that a code references an object using a DAO. 
	 * @param <T>               element type
	 * @param <U>               DAO type
	 * @param contextValidation validation context
	 * @param code              code to validate
	 * @param key               error key
	 * @param find              DAO
	 * @param returnObject      should a found instance be returned ?
	 * @return                  the instance referenced by the code is found and returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateCodeForeignRequired(ContextValidation, MiniDAO, String, String, boolean)}
	 */
	@Deprecated
	public static <T, U extends MiniDAO<T>> T validateRequiredDescriptionCode(ContextValidation contextValidation, 
			                                                                      String code, 
			                                                                      String key,
			                                                                      U find, 
			                                                                      boolean returnObject) {
		return CommonValidationHelper.validateCodeForeignRequired(contextValidation, find, code, key, returnObject);
	}


//	/*
//	 * Validate if a code in a description table exist
//	 * @param errors
//	 * @param code
//	 * @param key
//	 * @param find
//	 * @param returnObject
//	 * @return void
//	 */
//	public static <T, U extends AbstractDAO<T>> void validateExistDescriptionCode(ContextValidation contextValidation, 
//			                                                                      String code, 
//			                                                                      String key,
//			                                                                      U find) {
//		 validateExistDescriptionCode(contextValidation, code, key, find, false);
//	}
	
	/*
	 * Validate if a code in a description table exist
	 * @param errors
	 * @param code
	 * @param key
	 * @param find
	 * @param returnObject
	 * @return object de T or null if returnObject is false
	 */
//	public static <T,U extends AbstractDAO<T>> T validateExistDescriptionCode(ValidationContext contextValidation, 
//			                                                                  String code, 
//			                                                                  String key,
//			                                                                  U find, 
//			                                                                  boolean returnObject) {
//		T o = null;
//		try {
//			if (code != "" && null != code && returnObject) {
//				o = find.findByCode(code);
//				if (o == null) {
//					contextValidation.addErrors(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//				}
//			} else if (code != "" && null != code && !find.isCodeExist(code)) {
//				contextValidation.addErrors(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//			}
//		} catch (DAOException e) {
//			throw new RuntimeException(e);
//		}
//		return o;
//	}
	
//	/**
//	 * Validate an optional code.
//	 * @param contextValidation validation context
//	 * @param code              code to validate
//	 * @param key               key for errors in the validation context
//	 * @param find              DAO to use to test existence 
//	 * @param returnObject      if found for the provided code, must the object be returned
//	 * @return                  object for the code if the object exists and returnObject is true, null otherwise
//	 */
//	public static <T,U extends AbstractDAO<T>> T validateExistDescriptionCode(ContextValidation contextValidation, 
//                                                                              String code, 
//                                                                              String key,
//                                                                              U find, 
//                                                                              boolean returnObject) {
//		if (StringUtils.isBlank(code))
//			return null;
//		if (returnObject) {
//			T o = find.findByCode(code);
//			if (o == null)
//				contextValidation.addErrors(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//			return o;
//		} else {
//			if (!find.isCodeExist(code))
//				contextValidation.addErrors(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//			return null;
//		}
//	}
	
	/**
	 * Validate that an optional code references an object using a DAO. 
	 * @param <T>               element type
	 * @param <U>               DAO type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param find              DAO
	 * @param returnObject      should a found instance be returned ? 
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateCodeForeignOptional(ContextValidation, MiniDAO, String, String, boolean)}
	 */
	@Deprecated
	public static <T,U extends MiniDAO<T>> T validateExistDescriptionCode(ContextValidation contextValidation, 
						                                                      String code, 
						                                                      String key,
						                                                      U find, 
						                                                      boolean returnObject) {
		return CommonValidationHelper.validateCodeForeignOptional(contextValidation, find, code, key, returnObject);
	}
	
//	/*
//	 * 
//	 * @param contextValidation
//	 * @param code
//	 * @param key
//	 * @param type
//	 * @param collectionName
//	 */
//	public static <T extends DBObject> void validateRequiredInstanceCode(ContextValidation contextValidation,
//			                                                             String code, 
//			                                                             String key, 
//			                                                             Class<T> type, 
//			                                                             String collectionName) {
//		if (validateNotEmpty(contextValidation, code, key)) {
//			validateExistInstanceCode(contextValidation, code, key, type,collectionName);
//		}
//	}
	
	/**
	 * Validate that a code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, String, Class, String)}
	 */
	@Deprecated
	public static <T extends DBObject> void validateRequiredInstanceCode(ContextValidation contextValidation,
			                                                             String code, 
			                                                             String key, 
			                                                             Class<T> type, 
			                                                             String collectionName) {
		CommonValidationHelper.validateExistInstanceCode(contextValidation, code, key, type,collectionName);
	}

//	/*
//	 * Validate if code is not null and exist
//	 * @param errors
//	 * @param code
//	 * @param key
//	 * @param type
//	 * @param collectionName
//	 * @param returnObject
//	 * @return
//	 */
//	public static <T extends DBObject> T validateRequiredInstanceCode(ContextValidation contextValidation,
//			                                                          String code, 
//			                                                          String key, 
//			                                                          Class<T> type, 
//			                                                          String collectionName, 
//			                                                          boolean returnObject) {
//		T o = null;
//		if (validateNotEmpty(contextValidation, code, key)) {
//			o = validateExistInstanceCode(contextValidation, code, key, type,collectionName, returnObject);
//		}
//		return o;	
//	}
	
	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, String, Class, String, boolean)}
	 */	
	@Deprecated
	public static <T extends DBObject> T validateRequiredInstanceCode(ContextValidation contextValidation,
			                                                          String code, 
			                                                          String key, 
			                                                          Class<T> type, 
			                                                          String collectionName, 
			                                                          boolean returnObject) {
		return CommonValidationHelper.validateExistInstanceCode(contextValidation, code, key, type,collectionName, returnObject);
	}


	/*
	 * Validate if list is not null and code exist
	 * @param errors
	 * @param sampleCode
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
//	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ValidationContext contextValidation,
//			List<String> codes, String key, Class<T> type, String collectionName, boolean returnObject) {
//
//		List<T> l = null;
//		
//		if (validateNotEmpty(contextValidation, codes, key)) {
//			l = validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
//		}
//		return l;
//		
//	}
//	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ValidationContext contextValidation,
//			                                                                 Collection<String> codes, 
//			                                                                 String key, 
//			                                                                 Class<T> type, 
//			                                                                 String collectionName, 
//			                                                                 boolean returnObject) {
//
//		List<T> l = null;
//		
//		if (validateNotEmpty(contextValidation, codes, key)) {
//			l = validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
//		}
//		return l;
//		
//	}
//	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ContextValidation contextValidation,
//                                                                             Collection<String> codes, 
//                                                                             String key, 
//                                                                             Class<T> type, 
//                                                                             String collectionName, 
//                                                                             boolean returnObject) {
//		if (validateNotEmpty(contextValidation, codes, key))
//			return validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
//		else
//			return null;
//	}
	
	/**
	 * Validate a required list of optional code using a collection name and a type.
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param codes             list of optional codes to validate
	 * @param key               error key
	 * @param type              collection objects type 
	 * @param collectionName    MongoDB collection name
	 * @param returnObject      should the list of found instances be returned ?
	 * @return                  found instance list if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateRequiredInstanceCodes(ContextValidation, Collection, String, Class, String, boolean)}
	 */
	@Deprecated
	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ContextValidation contextValidation,
                                                                             Collection<String> codes, 
                                                                             String key, 
                                                                             Class<T> type, 
                                                                             String collectionName, 
                                                                             boolean returnObject) {
		return CommonValidationHelper.validateRequiredInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
	}
	
	/*
	 * Validate a code of a MongoDB Collection
	 * @param errors
	 * @param sampleCode
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
//	public static <T extends DBObject> List<T> validateExistInstanceCodes(ContextValidation contextValidation,
//			                                                              List<String> codes, 
//			                                                              String key, 
//			                                                              Class<T> type, 
//			                                                              String collectionName, 
//			                                                              boolean returnObject) {
//		List<T> l = null;
//		if (codes != null && codes.size() > 0) {
//			l = returnObject ? new ArrayList<T>() : null;
//			for (String code: codes) {
//				T o = validateExistInstanceCode(contextValidation, code, key, type, collectionName, returnObject);
//				if (returnObject)
//					l.add(o);
//			}			
//		}
//		return l;
//	}
//	public static <T extends DBObject> List<T> validateExistInstanceCodes(ValidationContext contextValidation,
//			                                                              List<String> codes, 
//			                                                              String key, 
//			                                                              Class<T> type, 
//			                                                              String collectionName, 
//			                                                              boolean returnObject) {
//		List<T> l = null;
//		if (codes != null && codes.size() > 0) {
//			if (returnObject)
//				l = new ArrayList<>();
//			for (String code : codes) {
//				T o = validateExistInstanceCode(contextValidation, code, key, type, collectionName, returnObject);
//				if (l != null)
//					l.add(o);
//			}			
//		}
//		return l;
//	}
//	public static <T extends DBObject> List<T> validateExistInstanceCodes(ContextValidation contextValidation,
//			                                                              Collection<String> codes, 
//			                                                              String key, 
//			                                                              Class<T> type, 
//			                                                              String collectionName, 
//			                                                              boolean returnObject) {
//		List<T> l = null;
//		if (codes != null && codes.size() > 0) {
//			if (returnObject)
//				l = new ArrayList<>();
//			for (String code : codes) {
//				T o = validateExistInstanceCode(contextValidation, code, key, type, collectionName, returnObject);
//				if (l != null)
//					l.add(o);
//			}			
//		}
//		return l;
//	}
	
	/**
	 * Validate a optional list of optional code using a collection name and a type.
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param codes             list of optional codes to validate
	 * @param key               error key
	 * @param type              collection objects type 
	 * @param collectionName    MongoDB collection name
	 * @param returnObject      should the list of found instances be returned ?
	 * @return                  found instance list if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCodes(ContextValidation, Collection, String, Class, String, boolean)}
	 */
	@Deprecated
	public static <T extends DBObject> List<T> validateExistInstanceCodes(ContextValidation contextValidation,
                                                                          Collection<String> codes, 
                                                                          String key, 
                                                                          Class<T> type, 
                                                                          String collectionName, 
                                                                          boolean returnObject) {
		return CommonValidationHelper.validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
	}

//	/*
//	 * 
//	 * @param contextValidation
//	 * @param code
//	 * @param key
//	 * @param type
//	 * @param collectionName
//	 */
//	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
//			                                                          String code, 
//			                                                          String key, 
//			                                                          Class<T> type, 
//			                                                          String collectionName) {
//		validateExistInstanceCode(contextValidation, code, key, type, collectionName, false);
//	}
	
	/**
	 * Validate that a code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, String, Class, String)}
	 */		
	@Deprecated
	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
			                                                          String code, 
			                                                          String key, 
			                                                          Class<T> type, 
			                                                          String collectionName) {
		CommonValidationHelper.validateExistInstanceCode(contextValidation, code, key, type, collectionName);
	}
	
//	/*
//	 * Validate a code of a MongoDB Collection, code is not required and can be null
//	 * @param errors
//	 * @param code
//	 * @param key
//	 * @param type
//	 * @param collectionName
//	 * @param returnObject
//	 * @return
//	 */
//	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
//			                                                       String code, 
//			                                                       String key, 
//			                                                       Class<T> type, 
//			                                                       String collectionName, 
//			                                                       boolean returnObject) {
////		if(code!=null) {
////			if(null != key) {
////				T o = null;
////				if(returnObject){
////					o =  MongoDBDAO.findByCode(collectionName, type, code);
////					if(o == null){
////						contextValidation.addErrors(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
////					}
////				}else if(!MongoDBDAO.checkObjectExistByCode(collectionName, type, code)){
////					contextValidation.addErrors( key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
////				}
////				return o;
////			}else {
////				throw new IllegalArgumentException("key is null for "+code);
////			}
////		}
////		return null;
//		if (code == null)
//			return null;
//		if (key == null)
//			throw new IllegalArgumentException("key is null for " + code);
//		if (returnObject) {
//			T o =  MongoDBDAO.findByCode(collectionName, type, code);
//			if (o == null) 
//				contextValidation.addError(key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//			return o;
//		} else {
//			if(!MongoDBDAO.checkObjectExistByCode(collectionName, type, code))
//				contextValidation.addError( key, ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code);
//			return null;
//		}
//	}	

	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param key               error key
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, String, Class, String, boolean)}
	 */
	@Deprecated
	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
			                                                       String code, 
			                                                       String key, 
			                                                       Class<T> type, 
			                                                       String collectionName, 
			                                                       boolean returnObject) {
		return CommonValidationHelper.validateExistInstanceCode(contextValidation, code, type, collectionName, returnObject);
	}
	
//	/*
//	 * Validate a code of a MongoDB Collection
//	 * @param errors
//	 * @param code
//	 * @param type
//	 * @param collectionName
//	 * @return
//	 */
//	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
//			                                                          String code, 
//			                                                          Class<T> type, 
//			                                                          String collectionName) {
//		validateExistInstanceCode(contextValidation, code, type, collectionName, false);
//	}

	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param code              code
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param contextValidation validation context
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, Class, String)}
	 */	
	@Deprecated
	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
			                                                          String code, 
			                                                          Class<T> type, 
			                                                          String collectionName) {
		CommonValidationHelper.validateExistInstanceCode(contextValidation, code, type, collectionName);
	}
	
//	/*
//	 * Validate a code of a MongoDB Collection
//	 * @param errors
//	 * @param code
//	 * @param type
//	 * @param collectionName
//	 * @param returnObject
//	 * @return
//	 */
//	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
//			                                                       String code, 
//			                                                       Class<T> type, 
//			                                                       String collectionName, 
//			                                                       boolean returnObject) {
//		return validateExistInstanceCode(contextValidation, code, FIELD_CODE, type, collectionName, returnObject);
//	}	

	/**
	 * Validate that an optional code references an object using a collection name and a type. 
	 * @param <T>               element type
	 * @param contextValidation validation context
	 * @param code              code
	 * @param type              collection objects type
	 * @param collectionName    collection name
	 * @param returnObject      should a found instance be returned ?
	 * @return                  found instance if returnObject is true, null otherwise
	 * @deprecated use {@link CommonValidationHelper#validateExistInstanceCode(ContextValidation, String, Class, String, boolean)}
	 */	
	@Deprecated
	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
			                                                       String code, 
			                                                       Class<T> type, 
			                                                       String collectionName, 
			                                                       boolean returnObject) {
		return CommonValidationHelper.validateExistInstanceCode(contextValidation, code, type, collectionName, returnObject);
	}	

}
