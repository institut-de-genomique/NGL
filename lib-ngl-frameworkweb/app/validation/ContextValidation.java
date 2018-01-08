package validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.validation.ValidationError;
import play.i18n.Messages;

public class ContextValidation {

	
	private enum Mode {
		CREATION, UPDATE, DELETE, NOT_DEFINED;
	}

	private String user = null;

	private Mode mode = Mode.NOT_DEFINED;
	
	
	private String rootKeyName = "";
	public Map<String,List<ValidationError>> errors;
	private Map<String,Object> contextObjects;

	public ContextValidation(String user){
		errors= new TreeMap<String, List<ValidationError>>();
		contextObjects= new TreeMap<String, Object>();
		this.user = user;
	}

	public ContextValidation(String user, Map<String,List<ValidationError>> errors){
		this.errors = errors;
		contextObjects= new TreeMap<String, Object>();
		this.user = user;
	}

	
	public String getUser() {
		return user;
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public Object getObject(String key){
		if(contextObjects.containsKey(key)) {
			return contextObjects.get(key);
		} else {
			return null;
		}
	}

	public Map<String,Object> getContextObjects() {
		return this.contextObjects;
	}


	public void setContextObjects(Map<String,Object> contextObjects) {
        this.contextObjects = new TreeMap<String,Object>(contextObjects);
        
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	public void putObject(String key, Object value){
		contextObjects.put(key, value);
	}
	
	/**
	 *
	 * @param key
	 * @param value
	 */
	public void removeObject(String key){
		contextObjects.remove(key);
	}


	/**
	 * add an error message
	 * @param key : property key
	 * @param message : message key
	 * @param arguments : message args
	 */
	public void addErrors(String property, String message, Object...arguments) {
		String key = getKey(property);
		if (!errors.containsKey(key)) {
			errors.put(key, new ArrayList<ValidationError>());
		}
		errors.get(key).add(new ValidationError(key, message,  java.util.Arrays.asList(arguments)));			
	}

	public void addErrors(Map<String,List<ValidationError>> errors){
		this.errors.putAll(errors);
	}
	
	/**
	 *
	 * @param rootKeyName
	 * @param property
	 * @return
	 */
	private String getKey(String property) {
		return (StringUtils.isBlank(rootKeyName))?property: rootKeyName+"."+property;
	}


	/**
	 *
	 * @return
	 */
	public String getRootKeyName() {
		return this.rootKeyName;
	}

	/**
	 *
	 * @param rootKeyName
	 */
	public void setRootKeyName(String rootKeyName) {
		this.rootKeyName = rootKeyName;
	}

	/**
	 *
	 * @param key
	 */
	public void addKeyToRootKeyName(String key) {
		if (StringUtils.isBlank(this.rootKeyName)) {
			this.rootKeyName = key;
		}
		else {
			this.rootKeyName += "." + key;
		}
	}

	/**
	 *
	 * @param key
	 */
	public void removeKeyFromRootKeyName(String key) {
		if(StringUtils.isNotBlank(this.rootKeyName) && this.rootKeyName.equals(key)){
			this.rootKeyName = null;
		}else if(StringUtils.isNotBlank(this.rootKeyName) && this.rootKeyName.endsWith(key)){
			this.rootKeyName = this.rootKeyName.substring(0, this.rootKeyName.length()-key.length()-1);
		}
	}

	public boolean hasErrors() {
        return !errors.isEmpty();
    }
	
	public void setCreationMode(){
		this.mode = Mode.CREATION;
	}
	
	public void setUpdateMode(){
		this.mode = Mode.UPDATE;
	}
	
	public void setDeleteMode(){
		this.mode = Mode.DELETE;
	}
	
	private boolean isMode(Mode mode){
		return mode.equals(this.mode);
	}
	
	public boolean isUpdateMode(){
		return isMode(Mode.UPDATE);
	}
	
	public boolean isCreationMode(){
		return isMode(Mode.CREATION);
	}
	
	public boolean isDeleteMode(){
		return isMode(Mode.DELETE);
	}
	
	public boolean isNotDefined(){
		return isMode(Mode.NOT_DEFINED);
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	public Mode getMode() {
		return this.mode;
	}
	
	
	/***
	 *
	 */
	public void clear(){
		errors.clear();
		rootKeyName=null;
		contextObjects.clear();
		mode=Mode.NOT_DEFINED;
	}

	
	public void displayErrors(ALogger logger) {
		Iterator entries = this.errors.entrySet().iterator();
		while (entries.hasNext()) {
			Entry thisEntry = (Entry) entries.next();
			String key = (String) thisEntry.getKey();
			List<ValidationError> value = (List<ValidationError>) thisEntry.getValue();	  

			for(ValidationError validationError:value){
				logger.error( key+ " : "+Messages.get(validationError.message(),validationError.arguments()));
			}
		}
		
	}

}
