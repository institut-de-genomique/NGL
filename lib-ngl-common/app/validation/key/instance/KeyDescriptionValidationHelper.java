package validation.key.instance;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import models.administration.authorisation.instance.KeyDescription;
import models.administration.authorisation.instance.KeyUser;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class KeyDescriptionValidationHelper {
	
	public static final play.Logger.ALogger logger = play.Logger.of(KeyDescriptionValidationHelper.class);
	
	private static final String HASH 				= "hash";
	private static final String USER 				= "user";
	private static final String USER_NAME 			= "user.name";
	private static final String USER_PERMISSIONS	= "user.permissions";
	private static final String OWNERS				= "owners";
	
	private static final String ERROR_HASH_NOT_NULL_MSG  		= "error.hash.not.null";
	private static final String ERROR_NAME_NOT_UNIQUE_MSG  		= "error.name.not.unique";
	private static final String ERROR_RENEW_EMPTY_HASH_MSG		= "error.renew.empty.hash";
	private static final String ERROR_UPDATE_ERASE_HASH_MSG		= "error.update.erase.hash";
	private static final String ERROR_UPDATE_REPLACE_HASH_MSG	= "error.update.replace.hash";
	
	public static final String KEY_RENEW_CONTEXT = "renew";
	
	private static boolean isNull(Object obj) {
		return obj == null;
	}
	
	private static boolean isNotNull(Object obj) {
		return !isNull(obj);
	}
	
	private static void handleKeyHashAtCreation(ContextValidation contextValidation, String hash) {
		if(!isNull(hash)) {
			// hash should be initiated empty
			contextValidation.addError(HASH, ERROR_HASH_NOT_NULL_MSG, String.valueOf(hash));
		}
	}
	
	private static void handleKeyHashAtRenew(ContextValidation contextValidation, String hash) {
		if(isNull(hash)) {
			// Cannot register an empty hash on renew
			contextValidation.addError(HASH, ERROR_RENEW_EMPTY_HASH_MSG);
		}
	}
	
	private static boolean isRenewContext(ContextValidation contextValidation) {
		return contextValidation.containsKey(KEY_RENEW_CONTEXT) && 
				contextValidation.<Boolean>getTypedObject(KEY_RENEW_CONTEXT);
	}
	
	private static boolean isErasingExistingHash(String hash, KeyDescription keyDescription) {
		return isNull(hash) && isNotNull(keyDescription.hash);
	}
	
	private static boolean isReplacingExistingHash(String hash, KeyDescription keyDescription) {
		return isNotNull(hash) && !hash.equals(keyDescription.hash);
	}
	
	private static void validateKeyHashUpdate(ContextValidation contextValidation, String hash, KeyDescription keyDescription) {
		if(isErasingExistingHash(hash, keyDescription)) {
			// Cannot replace existing hash by null
			contextValidation.addError(HASH, ERROR_UPDATE_ERASE_HASH_MSG);
		}
		if(isReplacingExistingHash(hash, keyDescription)) {
			// Cannot replace existing hash on update
			contextValidation.addError(HASH, ERROR_UPDATE_REPLACE_HASH_MSG);
		}
	}
	
	private static void handleKeyHashAtClassicUpdate(ContextValidation contextValidation, String code, String hash) {
		KeyDescription keyDescription = MongoDBDAO.findByCode(InstanceConstants.API_KEY_COLL_NAME, KeyDescription.class, code);
		if(isNotNull(keyDescription)) {
			validateKeyHashUpdate(contextValidation, hash, keyDescription);
		}
	}
	
	private static void handleKeyHashAtUpdate(ContextValidation contextValidation, String code, String hash) {
		if(isRenewContext(contextValidation)) {
			handleKeyHashAtRenew(contextValidation, hash);
		} else {
			handleKeyHashAtClassicUpdate(contextValidation, code, hash);
		}
	}
	
	private static boolean isUserNotEmpty(ContextValidation contextValidation, KeyUser user) {
		ValidationHelper.validateNotEmpty(contextValidation, user, USER);
		return isNotNull(user);
	}
	
	private static boolean shouldValidateUserName(ContextValidation contextValidation, String userName) {
		return StringUtils.isNotBlank(userName) && contextValidation.isCreationMode();
	}
	
	private static boolean userNameAlreadyExists(String userName) {
		Query isUserName = DBQuery.is(USER_NAME, userName);
		return MongoDBDAO.checkObjectExist(InstanceConstants.API_KEY_COLL_NAME, KeyDescription.class, isUserName);
	}
	
	private static void validateUniqueUserName(ContextValidation contextValidation, String userName) {
		if(shouldValidateUserName(contextValidation, userName) && userNameAlreadyExists(userName)) {
			contextValidation.addError(USER, ERROR_NAME_NOT_UNIQUE_MSG, String.valueOf(userName));
		}
	}
	
	public static void validateKeyHash(ContextValidation contextValidation, String code, String hash) {
		if(contextValidation.isCreationMode()) {
			handleKeyHashAtCreation(contextValidation, hash);
		} else if(contextValidation.isUpdateMode()) {
			handleKeyHashAtUpdate(contextValidation, code, hash);
		}
	}

	public static void validateKeyUser(ContextValidation contextValidation, KeyUser user) {
		if(isUserNotEmpty(contextValidation, user)) {
			ValidationHelper.validateNotEmpty(contextValidation, user.name, USER_NAME);
			ValidationHelper.validateNotEmpty(contextValidation, user.permissions, USER_PERMISSIONS);
			validateUniqueUserName(contextValidation, user.name);
		}
	}
	
	public static void validateKeyOwners(ContextValidation contextValidation, Set<String> owners) {
		if(contextValidation.isCreationMode()) {
			ValidationHelper.validateNotEmpty(contextValidation, owners, OWNERS);
		}
	}

}
