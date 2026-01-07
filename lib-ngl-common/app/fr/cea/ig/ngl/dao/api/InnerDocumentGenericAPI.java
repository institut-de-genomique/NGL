package fr.cea.ig.ngl.dao.api;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.springframework.beans.BeansException;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import play.Logger;
import validation.ContextValidation;
import validation.IValidation;

public abstract class InnerDocumentGenericAPI<T, U extends GenericMongoDAO<V>, V extends DBObject & IValidation> {
	
	protected static final String METHOD_NOT_ALLOWED_MESSAGE = "method not allowed for this inner-resource";
	
	protected final U dao;
	
	@Inject
	public InnerDocumentGenericAPI(U dao) {
		this.dao = dao;
	}
	
	private void checkAuthorizedUpdateFields(ContextValidation ctxVal, List<String> fields) {
		Set<String> authorizedUpdateFields = authorizedUpdateFields();
		fields.stream()
		.filter(field -> !authorizedUpdateFields.contains(field))
		.forEach(field -> ctxVal.addError("fields", "error.valuenotauthorized", field)); 
	}
	
	private boolean isUndefinedField(String field, T input) throws BeansException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return FieldAccessor.from(field, input).get(input) == null;
	}

	private void checkIfFieldsAreDefined(ContextValidation ctxVal, List<String> fields, T input) {
		try {
			for(String field : fields) {
				if(isUndefinedField(field, input)) ctxVal.addError(field, "error.notdefined");
			};
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	private T getInnerResourceWithFields(V objectInDB, T input, List<String> fields) throws APIException {
		T innerResource = this.getInnerObject(objectInDB);
		try {
			for(String field : fields) {
				FieldAccessor fieldAccessor = FieldAccessor.from(field, input);
				Object newValue = fieldAccessor.get(input);
				fieldAccessor.set(innerResource, newValue);
			}
			return innerResource;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new APIException("Invalid field access", e);
		}
	}
	
	protected ContextValidation createUpdateContext(String currentUser, V objectInDB) {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		setContextValidationLevel(ctxVal, objectInDB);
		return ctxVal;
	}
	
	protected abstract Set<String> authorizedUpdateFields();
	
	protected abstract void setContextValidationLevel(ContextValidation ctxVal, V objectInDB);
	
	protected abstract void saveInnerResource(ContextValidation ctxVal, V objectInDB, T input, String currentUser);
	
	protected abstract void deleteInnerResource(V objectInDB, String currentUser);
	
	public abstract T getInnerObject(V objectInDB);
	
	public abstract void setInnerObject(V objectInDB, T input);
	
	public abstract String innerObjectName();
	
	public boolean isInnerObject(V objectInDB) {
		return getInnerObject(objectInDB) != null;
	}
	
	public T save(V objectInDB, T input, String currentUser) throws APIException {
		if(isInnerObject(objectInDB)) throw new APIException(innerObjectName() + " already exists!");
		ContextValidation ctxVal = createUpdateContext(currentUser, objectInDB);
		setInnerObject(objectInDB, input);
		objectInDB.validate(ctxVal);
		if (ctxVal.hasErrors())  throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		saveInnerResource(ctxVal, objectInDB, input, currentUser);
		V savedObject = dao.getObject(objectInDB.code);
		return getInnerObject(savedObject);
	}
	
	public T update(V objectInDB, T input, String currentUser) throws APIException {
		if(!isInnerObject(objectInDB)) throw new APIException("No " + innerObjectName() + " to update!");
		ContextValidation ctxVal = createUpdateContext(currentUser, objectInDB);
		setInnerObject(objectInDB, input);
		objectInDB.validate(ctxVal);
		if (ctxVal.hasErrors())  throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		saveInnerResource(ctxVal, objectInDB, input, currentUser);
		V savedObject = dao.getObject(objectInDB.code);
		return getInnerObject(savedObject);
	}
	
	public T update(V objectInDB, T input, String currentUser, List<String> fields) throws APIException {
		if(!isInnerObject(objectInDB)) throw new APIException("No " + innerObjectName() + " to update!");
		ContextValidation ctxVal = createUpdateContext(currentUser, objectInDB);
		checkAuthorizedUpdateFields(ctxVal, fields);
		checkIfFieldsAreDefined(ctxVal, fields, input);
		if (ctxVal.hasErrors())  throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		T innerResource = getInnerResourceWithFields(objectInDB, input, fields);
		setInnerObject(objectInDB, innerResource);
		objectInDB.validate(ctxVal);
		if (ctxVal.hasErrors())  throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		saveInnerResource(ctxVal, objectInDB, innerResource, currentUser);
		V savedObject = dao.getObject(objectInDB.code);
		return getInnerObject(savedObject);
	}
	
	public void delete(V objectInDB, String currentUser) throws APIException {
		if(!isInnerObject(objectInDB)) throw new APIException("No " + innerObjectName() + " to delete!");
		deleteInnerResource(objectInDB, currentUser);
	}
	
	static abstract class FieldAccessor {
		
		public final Field field;
		
		protected FieldAccessor(Field field) {
			this.field = field;
		}
		
		public abstract <T> Object getObectToAccessFrom(T input) throws IllegalArgumentException, IllegalAccessException;
		
		public <T> Object get(T input) throws IllegalArgumentException, IllegalAccessException {
			Object objectToAccess = getObectToAccessFrom(input);
			return field.get(objectToAccess);
		}
		
		public <T,V> void set(T input, V value) throws IllegalArgumentException, IllegalAccessException {
			Object objectToAccess = getObectToAccessFrom(input);
			field.set(objectToAccess, value);
		}
		
		private static <T> FieldAccessor simpleFieldAccessor(String fieldPath, T input) throws NoSuchFieldException, SecurityException {
			Class<?> clazz = input.getClass();
			Field field = clazz.getField(fieldPath);
			return new SimpleFieldAccessor(field);
		}
		
		private static <T> FieldAccessor subObjectFieldAccessor(Matcher matcher, T input) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			String parentFieldPath = matcher.group(1);
	        String fieldPath = matcher.group(2);
	        Class<?> clazz = input.getClass();
	        Field parentField = clazz.getField(parentFieldPath);
	        Object parent = parentField.get(input);
	        Class<?> parentClass = parent.getClass();
	        Field field = parentClass.getField(fieldPath);
	        return new SubFieldAccessor(parentField, field);
		}
		
		public static <T> FieldAccessor from(String fieldPath, T input) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			Matcher matcher = GenericMongoDAO.FIELD_PATTERN.matcher(fieldPath);
			return matcher.matches() ? subObjectFieldAccessor(matcher, input) : simpleFieldAccessor(fieldPath, input);
		}
		
		static final class SimpleFieldAccessor extends FieldAccessor {

			private SimpleFieldAccessor(Field field) {
				super(field);
			}

			@Override
			public <T> Object getObectToAccessFrom(T input) {
				return input;
			}
			
		}
		
		static final class SubFieldAccessor extends FieldAccessor {
			
			private final Field parentField;

			private SubFieldAccessor(Field parentField, Field field) {
				super(field);
				this.parentField = parentField;
			}

			@Override
			public <T> Object getObectToAccessFrom(T input) throws IllegalArgumentException, IllegalAccessException {
				return parentField.get(input);
			}
			
		}
		
	}

}
