package validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import play.Logger.ALogger;
import play.data.validation.ValidationError;

/**
 * Validation context for objects that support validation (implementing IValidation).
 */
class ValidationContext implements ContextValidation {

	/**
	 * User running the validation.
	 */
	private String user = null;

	/**
	 * Validation context mode.
	 */
	private Mode mode = Mode.NOT_DEFINED;
		
	//
	private String rootKeyName = "";
	
	//
	private Map<String,List<ValidationError>> errors;
	
	//
	private Map<String,Object> contextObjects;

//	/**
//	 * Constructs a validation context using the provided user name.
//	 * @param user user name
//	 */
//	public ContextValidation(String user) {
//		errors         = new TreeMap<>();
//		contextObjects = new TreeMap<>();
//		this.user      = user;
//	}
//	public ContextValidation(String user) {
//		this(user, new TreeMap<>());
//	}

	/*
	 * Constructs a validation context using the provided user name and initial errors.
	 * @param user
	 * @param errors
	 */
//	public ContextValidation(String user, Map<String,List<ValidationError>> errors) {
//		this.errors    = new TreeMap<>(errors);
//		contextObjects = new TreeMap<>();
//		this.user      = user;
//	}
//	public ContextValidation(String user, Map<String,List<ValidationError>> errors) {
//		this(Mode.NOT_DEFINED, user, new TreeMap<>(errors), new TreeMap<>());
//	}
	
//	// TODO: provide a proper initialization
//	@SuppressWarnings("deprecation")
//	public ContextValidation(String user, Form<?> form) {
//		this(user,form.errors());
//	}
//	// TODO: provide a proper initialization
//	@SuppressWarnings("deprecation")
//	public ContextValidation(String user, Form<?> form) {
//		this(Mode.NOT_DEFINED, user, new TreeMap<>(form.errors()), new TreeMap<>());
//	}
	
//	public ContextValidation(String user, Map<String,List<ValidationError>> errors, Map<String,Object> contextObjects) {
//		this.user           = user;
//		this.errors         = errors;
//		this.contextObjects = contextObjects;
//	}
	
	ValidationContext(Mode mode, String user, Map<String,List<ValidationError>> errors, Map<String,Object> contextObjects) {
		this.mode           = mode;
		this.user           = user;
		this.setErrors(errors);
		this.contextObjects = contextObjects;
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getUser()
	 */
	@Override
	public String getUser() {
		return user;
	}

//	// Constructors replacements
//	public static ContextValidation createCreationContext(String user) {
//		return new ContextValidation(Mode.CREATION, user, new TreeMap<>(), new TreeMap<>());
//	}
//	@SuppressWarnings("deprecation")
//	public static ContextValidation createCreationContext(String user, Form<?> form) {
//		return new ContextValidation(Mode.CREATION, user, new TreeMap<>(form.errors()), new TreeMap<>());
//	}
//	public static ContextValidation createUpdateContext(String user) {
//		return new ContextValidation(Mode.UPDATE, user, new TreeMap<>(), new TreeMap<>());
//	}
//	@SuppressWarnings("deprecation")
//	public static ContextValidation createUpdateContext(String user, Form<?> form) {
//		return new ContextValidation(Mode.UPDATE, user, new TreeMap<>(form.errors()), new TreeMap<>());
//	}
//	public static ContextValidation createDeleteContext(String user) {
//		return new ContextValidation(Mode.DELETE, user, new TreeMap<>(), new TreeMap<>());
//	}
//	public static ContextValidation createUndefinedContext(String user) {
//		return new ContextValidation(Mode.NOT_DEFINED, user, new TreeMap<>(), new TreeMap<>());
//	}
//	// TODO: provide a proper initialization
//	@SuppressWarnings("deprecation")
//	public static ContextValidation createUndefinedContext(String user, Form<?> form) {
//		return new ContextValidation(Mode.NOT_DEFINED, user, new TreeMap<>(form.errors()), new TreeMap<>());
//	}

	// -------------------------------------------------------------
	// ---- Named objects
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String key) {
		return contextObjects.get(key);
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#putObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void putObject(String key, Object value) {
		contextObjects.put(key, value);
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#removeObject(java.lang.String)
	 */
	@Override
	public void removeObject(String key) {
		contextObjects.remove(key);
	}
		
	/* (non-Javadoc)
	 * @see validation.ValidationContext#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(String key) {
		return contextObjects.containsKey(key);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getTypedObject(java.lang.String)
	 */
	@Override
	public <T> T getTypedObject(String key) {
		@SuppressWarnings("unchecked")
		T t = (T)getObject(key);
		return t;
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getContextObjects()
	 */
	@Override
	public Map<String,Object> getContextObjects() {
		return contextObjects;
	}

//	public void setContextObjects(Map<String,Object> contextObjects) {
//		this.contextObjects = new TreeMap<>(contextObjects);
//	}
	/* (non-Javadoc)
	 * @see validation.ValidationContext#setContextObjects(java.util.Map)
	 */
	@Override
	public void setContextObjects(Map<String,Object> contextObjects) {
		this.contextObjects = new TreeMap<>(contextObjects);
	}

	// -------------------------------------------------------------
	// ---- Errors

	/* (non-Javadoc)
	 * @see validation.ValidationContext#addErrors(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public synchronized void addError(String property, String message, Object... arguments) {
//		String key = getKey(property);
//		if (!getErrors().containsKey(key))
//			getErrors().put(key, new ArrayList<ValidationError>());
//		getErrors().get(key).add(new ValidationError(key, message,  java.util.Arrays.asList(arguments)));
		addErrorKey(rootKeyName, property, message, arguments);
	}
	@Override
	public void addErrorKey(String key, String property, String message, Object... arguments) {
//		String key = getKey(property);
		key = getKey(key, property);
		if (!getErrors().containsKey(key))
			getErrors().put(key, new ArrayList<ValidationError>());
		getErrors().get(key).add(new ValidationError(key, message,  java.util.Arrays.asList(arguments)));			
	}
		
	/* (non-Javadoc)
	 * @see validation.ValidationContext#addErrors(java.util.Map)
	 */
	@Override
	public void addErrors(Map<String,List<ValidationError>> errors) {
		this.getErrors().putAll(errors);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getErrors()
	 */
	@Override
	public Map<String,List<ValidationError>> getErrors() {
		return errors;
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#hasErrors()
	 */
	@Override
	public boolean hasErrors() {
        return !getErrors().isEmpty();
    }
	
	@Override
	public int errorCount() { 
		return errors.size();
	}
	
	// -------------------------------------------------------------
	// ---- Key

//	private String getKey(String property) {
////		return (StringUtils.isBlank(rootKeyName))?property: rootKeyName+"."+property;
//		return getKey(rootKeyName, property);
//	}

	private String getKey(String key, String property) {
		return (StringUtils.isBlank(key))?property: key+"."+property;
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#getRootKeyName()
	 */
	@Override
	public String getRootKeyName() {
		return rootKeyName;
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#setRootKeyName(java.lang.String)
	 */
	@Override
	public void setRootKeyName(String rootKeyName) {
		this.rootKeyName = rootKeyName;
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#addKeyToRootKeyName(java.lang.String)
	 */
	@Override
	public void addKeyToRootKeyName(String key) {
		if (StringUtils.isBlank(rootKeyName)) {
			rootKeyName = key;
		} else {
			rootKeyName += "." + key;
		}
	}

	/* (non-Javadoc)
	 * @see validation.ValidationContext#removeKeyFromRootKeyName(java.lang.String)
	 */
	@Override
	public void removeKeyFromRootKeyName(String key) {
		if (StringUtils.isNotBlank(rootKeyName) && rootKeyName.equals(key)) {
			rootKeyName = null; // Constructor value is "" so "" would be the expected value
		} else if (StringUtils.isNotBlank(rootKeyName) && rootKeyName.endsWith(key)) {
			rootKeyName = rootKeyName.substring(0, rootKeyName.length()-key.length()-1);
		} else {
			// TODO: do not silently ignore errors
		}
	}

	// -------------------------------------------------------------
	// ---- Mode
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#setCreationMode()
	 */
	@Override
	public void setCreationMode() {
		setMode(Mode.CREATION);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#setUpdateMode()
	 */
	@Override
	public void setUpdateMode() {
		setMode(Mode.UPDATE);
	}
	
//	/* (non-Javadoc)
//	 * @see validation.ValidationContext#setDeleteMode()
//	 */
//	@Override
//	public void setDeleteMode() {
//		setMode(Mode.DELETE);
//	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#setMode(validation.ContextValidation.Mode)
	 */
	@Override
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	private boolean isMode(Mode mode) {
		return mode.equals(this.mode);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#isUpdateMode()
	 */
	@Override
	public boolean isUpdateMode() {
		return isMode(Mode.UPDATE);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#isCreationMode()
	 */
	@Override
	public boolean isCreationMode() {
		return isMode(Mode.CREATION);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#isDeleteMode()
	 */
	@Override
	public boolean isDeleteMode() {
		return isMode(Mode.DELETE);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#isNotDefined()
	 */
	@Override
	public boolean isNotDefined() {
		return isMode(Mode.NOT_DEFINED);
	}
	
	/* (non-Javadoc)
	 * @see validation.ValidationContext#getMode()
	 */
	@Override
	public Mode getMode() {
		return mode;
	}
		
	// -------------------------------------------------------------
	// ---- Utilities

//	public void clear() {
//		errors.clear();
//		rootKeyName = null;
//		contextObjects.clear();
//		mode = Mode.NOT_DEFINED;
//	}

//	public void displayErrors(ALogger logger) {
//		Iterator<Entry<String,List<ValidationError>>> entries = this.errors.entrySet().iterator();
//		while (entries.hasNext()) {
//			Entry<String,List<ValidationError>> thisEntry = entries.next();
//			String key = thisEntry.getKey();
//			List<ValidationError> value = thisEntry.getValue();	  
//			for(ValidationError validationError:value){
//				// logger.error(key + " : " + Messages.get(validationError.message(),validationError.arguments()));
//				logger.error(key + " : " + fr.cea.ig.play.IGGlobals.messages().at(validationError.message(),validationError.arguments()));
//			}
//		}
//	}
	/* (non-Javadoc)
	 * @see validation.ValidationContext#displayErrors(play.Logger.ALogger)
	 */
	@Override
	public void displayErrors(ALogger logger) {
		for (Map.Entry<String,List<ValidationError>> e : getErrors().entrySet())
			for (ValidationError validationError : e.getValue())
				logger.error("{} : {}", e.getKey(), fr.cea.ig.play.IGGlobals.messages().at(validationError.message(),validationError.arguments()));
	}

	public void setErrors(Map<String,List<ValidationError>> errors) {
		this.errors = errors;
	}

}
