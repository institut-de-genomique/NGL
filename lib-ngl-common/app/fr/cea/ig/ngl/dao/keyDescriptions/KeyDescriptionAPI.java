package fr.cea.ig.ngl.dao.keyDescriptions;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.authentication.keys.KeyUtils;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.administration.authorisation.instance.KeyDescription;
import models.laboratory.common.instance.TraceInformation;
import validation.ContextValidation;
import validation.key.instance.KeyDescriptionValidationHelper;

public class KeyDescriptionAPI extends GenericAPI<KeyDescriptionDAO, KeyDescription> {
	
	private final static List<String> authorizedUpdateFields = Collections.unmodifiableList(Arrays.asList("name", "user.permissions"));
	private final static List<String> defaultKeys = Collections.unmodifiableList(Arrays.asList());
	
	private static final String INVALID_KEY_OBJECT = "Invalid Key object";

	SecureRandom random;

	@Inject
	public KeyDescriptionAPI(KeyDescriptionDAO dao) {
		super(dao);
		this.random = new SecureRandom();
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	protected List<String> defaultKeys() {
		return defaultKeys;
	}
	
	private Query searchByCode(String code) {
		return DBQuery.is("code", new String(code));
	}
	
	private boolean isExistingKeyForCode(String code) {
		Query findKeyQuery = searchByCode(code);
		return dao.checkObjectExist(findKeyQuery);
	}
	
	public KeyDescription getByUserName(String userName) {
		return dao.findOne(DBQuery.is("user.name", userName));
	}
	
	private void traceModification(KeyDescription input, String currentUser) {
		if(input.traceInformation != null) input.traceInformation.modificationStamp(currentUser);
	}
	
	/* KEY GENERATION */
	
	private String generateCode() {
		String code;
		do {
			code = KeyUtils.randomAlphaNumericString(random, KeyUtils.CODE_LENGTH);
		} while (isExistingKeyForCode(code));	
		return code;
	}

	private String generateKey() {
		return KeyUtils.randomAlphaNumericString(random, KeyUtils.KEY_LENGTH);
	}
	
	private String createApiKeyForCode(String code) {
		String key = this.generateKey();
		return code + '.' + key;
	}
	
	/* ERROR HANDLERS */
	
	private void throwKeyDescriptionDoesntExist(String code) throws APIException {
		String simpleName = dao.getElementClass().getSimpleName();
		String message = simpleName + " with code " + code + " does not exist";
		throw new APIException(message);
	}

	private void handleValidationErrors(ContextValidation ctxVal, String errorMessage) throws APIValidationException {
		if(ctxVal.hasErrors()) {
        	throw new APIValidationException(errorMessage, ctxVal.getErrors());
        }
	}
	
	/* CREATE */
	
	private void handleNullKeyIdOnCreate(KeyDescription input) throws APISemanticException {
		if (input._id != null) {
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		}
	}
	
	private void handleCreateValidation(KeyDescription input, String currentUser) throws APIValidationException {
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        input.validate(ctxVal);
		handleCreateValidationErrors(ctxVal);
	}
	
	private void handleCreateValidationErrors(ContextValidation ctxVal) throws APIValidationException {
		this.handleValidationErrors(ctxVal, INVALID_INPUT_ERROR_MSG);
	}

	@Override
	public KeyDescription create(KeyDescription input, String currentUser) throws APIValidationException, APIException {
		handleNullKeyIdOnCreate(input);
		input.code = this.generateCode();
		input.traceInformation = new TraceInformation(currentUser);
		handleCreateValidation(input, currentUser);
        return this.dao.save(input);
	}
	
	/* RENEW */
	
	private KeyDescription getExistingKey(String code) throws APIException {
		KeyDescription keyDescription = get(code);
		if (keyDescription == null) throwKeyDescriptionDoesntExist(code);
		return keyDescription;
	}
	
	private void handleKeyInitialisation(KeyDescription keyDescription, String currentUser) throws APIException {
		if(keyDescription.hash != null) throw new APIException("Key Description is already initialized (hash present)!");
		if(keyDescription.owners == null || keyDescription.owners.isEmpty()) throw new APIException("Key Description cannot be initialized without Owners!");
		if(!keyDescription.owners.contains(currentUser)) throw new APIException("'" + String.valueOf(currentUser) + "' is not a valid Owner for key " + keyDescription.code);
	}
	
	private String getHashForKey(String apiKey) throws APIException {
		try {
			byte[] hash = KeyUtils.EncodeUtils.hashKey(apiKey);
			return new String(hash);
		} catch (final NoSuchAlgorithmException e) {
			throw new APIException("Unsupported hashing method: " + KeyUtils.EncodeUtils.HASH_ALGORITHM, e);
		}
	}
	
	private void handleRenewValidation(KeyDescription keyDescription, String currentUser) throws APIValidationException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		ctxVal.putObject(KeyDescriptionValidationHelper.KEY_RENEW_CONTEXT, true);
		keyDescription.validate(ctxVal);
		handleUpdateValidationErrors(ctxVal);
	}
	
	public String renew(KeyDescription keyDescription, String currentUser) throws APIValidationException, APIException {
		final String newApiKey = createApiKeyForCode(keyDescription.code);
		keyDescription.hash = getHashForKey(newApiKey);
		traceModification(keyDescription, currentUser);
		this.handleRenewValidation(keyDescription, currentUser);
		this.dao.updateObject(keyDescription);
		return newApiKey;
	}
	
	public String init(String code, String currentUser) throws APIValidationException, APIException {
		KeyDescription key = getExistingKey(code);
		handleKeyInitialisation(key, currentUser);
		return renew(key, currentUser);
	}
	
	public String renew(String code, String currentUser) throws APIValidationException, APIException {
		KeyDescription key = getExistingKey(code);
		return renew(key, currentUser);
	}
	
	/* UPDATE */
	
	private void handleKeyDescriptionDoesntExistOnUpdate(String code) throws APIException {
		if (!isObjectExist(code)) {
			throwKeyDescriptionDoesntExist(code);
		}
	}
	
	private void handleUpdateValidation(KeyDescription input, String currentUser) throws APIValidationException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        input.validate(ctxVal);
        handleUpdateValidationErrors(ctxVal);
	}
	
	private void handleUpdateValidation(KeyDescription input, String currentUser, List<String> fields) throws APIValidationException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		checkAuthorizedUpdateFields(ctxVal, fields);
        checkIfFieldsAreDefined(ctxVal, fields, input);
        input.validate(ctxVal);
        handleUpdateValidationErrors(ctxVal);
	}
	
	private void handleUpdateValidationErrors(ContextValidation ctxVal) throws APIValidationException {
		this.handleValidationErrors(ctxVal, INVALID_KEY_OBJECT);
	}

	@Override
	public KeyDescription update(KeyDescription input, String currentUser) throws APIException, APIValidationException {
		handleKeyDescriptionDoesntExistOnUpdate(input.code);
		traceModification(input, currentUser);
		handleUpdateValidation(input, currentUser);
        dao.updateObject(input);
        return get(input.code);
	}

	@Override
	public KeyDescription update(KeyDescription input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		handleKeyDescriptionDoesntExistOnUpdate(input.code);
		traceModification(input, currentUser);
		handleUpdateValidation(input, currentUser, fields);
		Query findKeyQuery = searchByCode(input.code);
		Builder fieldsBuilder = dao.getBuilder(input, fields).set("traceInformation", input.traceInformation);
        dao.updateObject(findKeyQuery, fieldsBuilder);
        return get(input.code);
	}

}
