package services.io.reception;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.reception.instance.AbstractFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;

/**
 * Class to map a line of Excel or CVS file to a DBObject : sample, support, container, etc.
 * 
 * @author galbini
 *
 * @param <T> DBOject subclass to map 
 */
public abstract class Mapping<T extends DBObject> {

	private static final play.Logger.ALogger logger = play.Logger.of(Mapping.class);
	
	public enum Keys {
		sample,
		support,
		container
	}
	
	// Seems that it's supposed to be 3 maps (samples, supports, containers) and not 1.
	// Each map indexes its element by code (string).
	protected Map<String, Map<String, DBObject>>                objects;
	protected Map<String, ? extends AbstractFieldConfiguration> configuration;
	protected Action                                            action;
	protected ContextValidation                                 contextValidation;
	protected String                                            collectionName;
	protected Class<T>                                          type;
	private   Keys                                              key;
	
	protected Mapping(Map<String, Map<String, DBObject>> objects, 
			          Map<String, ? extends AbstractFieldConfiguration> configuration, 
			          Action action,
			          String collectionName, 
			          Class<T> type, 
			          Mapping.Keys key, 
			          ContextValidation contextValidation) {
		this.objects           = objects;
		this.configuration     = configuration;
		this.action            = action;
		this.contextValidation = contextValidation;
		this.collectionName    = collectionName;
		this.type              = type;
		this.key               = key;
	}
	
	/*
	 * convert a file line in DBObject
	 * @param rowMap
	 * @return
	 */
	public T convertToDBObject(Map<Integer, String> rowMap) throws Exception {
		T object = type.newInstance();
		if (Action.update.equals(action)) {
			object = get(object, rowMap, true);
		} else if(Action.save.equals(action)) {
			T objectInDB = get(object, rowMap, false);
			if (objectInDB != null) {
				contextValidation.addError("Error", "error.objectexist", type.getSimpleName(), objectInDB.code);
			} else if (object.code != null) {
				@SuppressWarnings("unchecked")
				T objectInObjects = (T)objects.get(key.toString()).get(object.code);
				if (objectInObjects != null) {
					object = objectInObjects;
				}
			}
		}
		if (object != null) {
			Field[] fields = type.getFields();
			for (Field field : fields) {
				populateField(field, object, rowMap);			
			}
			update(object);
		}
		return object;
	}
	
	/*
	 * Update the current object alone without any information from other object
	 * @param object
	 */
	protected abstract void update(T object);

	/*
	 * Add missing property from other objectType
	 * @param c
	 */
	public abstract void consolidate(T object);
	
	public void synchronizeMongoDB(DBObject c) {
		if (Action.save.equals(action)) {
			MongoDBDAO.save(collectionName, c);
		} else if (Action.update.equals(action)) {
			MongoDBDAO.update(collectionName, c);
		}		
	}
	
	public void rollbackInMongoDB(DBObject c) {
		if (Action.save.equals(action) && c._id == null) { 
			// Delete sample and support if already exist !!!!
			MongoDBDAO.deleteByCode(collectionName, c.getClass(), c.code);
		} else if (Action.update.equals(action)) {
			// replace by old version of the object
		}		
	}
	
	public void validate(DBObject c) {
		ContextValidation cv =
				Action.save.equals(action) ? ContextValidation.createCreationContext(contextValidation.getUser())
						                   : ContextValidation.createUpdateContext  (contextValidation.getUser());
		cv.setRootKeyName(contextValidation.getRootKeyName());
		cv.addKeyToRootKeyName(c.code);
		((IValidation)c).validate(cv);
		if (cv.hasErrors()) {
			contextValidation.addErrors(cv.getErrors());
		}
		cv.removeKeyFromRootKeyName(c.code);	
	}
	
//	public <A extends DBObject & IValidation>void validate(A c) {
//		ContextValidation cv =
//				Action.save.equals(action) ? ContextValidation.createCreationContext(contextValidation.getUser())
//						                   : ContextValidation.createUpdateContext  (contextValidation.getUser());
//		cv.setRootKeyName(contextValidation.getRootKeyName());
//		cv.addKeyToRootKeyName(c.code);
//		c.validate(cv);
//		if (cv.hasErrors()) {
//			contextValidation.addErrors(cv.getErrors());
//		}
//		cv.removeKeyFromRootKeyName(c.code);	
//	}

	protected void populateField(Field field, DBObject dbObject, Map<Integer, String> rowMap) {
		if (configuration.containsKey(field.getName())) {
			AbstractFieldConfiguration fieldConfiguration = configuration.get(field.getName());
			try {
				fieldConfiguration.populateField(field, dbObject, rowMap, contextValidation, action);
			} catch (Exception e) {
				logger.error("Error", e.getMessage(), e);
				contextValidation.addError("Error", e.getMessage());
				throw new RuntimeException(e);
			}
		}			
	}
	
	protected T get(T object, Map<Integer, String> rowMap, boolean errorIsNotFound) {
		try {
			AbstractFieldConfiguration codeConfig = configuration.get("code");
			if (codeConfig != null) {
				codeConfig.populateField(object.getClass().getField("code"), object, rowMap, contextValidation, action);
				if (object.code != null) {
					String code = object.code;
					object = MongoDBDAO.findByCode(collectionName, type, object.code);	
					if (errorIsNotFound && object == null) {
						contextValidation.addError("Error", "not found " + type.getSimpleName() + " for code " + code);
					}
				} else if (codeConfig.required) {
					contextValidation.addError("Error", "not found " + type.getSimpleName() + " code !!!");
				} else {
					object = null;
				}
			} else {
				object = null;
			}
		} catch (Exception e) {
			logger.error("Error", e.getMessage(), e);
			contextValidation.addError("Error", e.getMessage());
			throw new RuntimeException(e);
		}
		return object;
	}

	protected ContainerSupport getContainerSupport(String code) {
		if (!objects.containsKey("support")) 
			throw new RuntimeException("Support must be load from Excel file, check configuration");
		ContainerSupport cs = (ContainerSupport)objects.get("support").get(code);
		return cs;
	}
	
	protected Sample getSample(String code) {
		Sample sample = null;
		if (objects.containsKey("sample")) {
			sample = (Sample)objects.get("sample").get(code);			
		} else {
			objects.put("sample", new TreeMap<String, DBObject>());
		}
		if (sample == null) {
			sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, code);
			objects.get("sample").put(code, sample);
		}
		return sample;
	}
	
}
