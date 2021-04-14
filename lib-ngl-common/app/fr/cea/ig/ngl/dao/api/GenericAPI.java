package fr.cea.ig.ngl.dao.api;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jongo.Aggregate;
import org.jongo.MongoCursor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.mongodb.BasicDBObject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.mongo.MongoStreamer;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import validation.ContextValidation;

public abstract class GenericAPI<O extends GenericMongoDAO<T>, T extends DBObject> {

	private static final play.Logger.ALogger logger = play.Logger.of(GenericAPI.class);
	
	public static final String CREATE_SEMANTIC_ERROR_MSG = "create method does not update existing objects";
	public static final String INVALID_INPUT_ERROR_MSG = "invalid input";
	public static final String INVALID_STATE_ERROR_MSG = "Invalid state modification";
	
	protected final O dao; 

	@Inject
	public GenericAPI(O dao) {
		this.dao = dao;
	}

	/**
	 * @return the list of field of DBObject which could be updated
	 */
	protected abstract List<String> authorizedUpdateFields();

	/**
	 * @return Default keys of DBObject
	 */
	protected abstract List<String> defaultKeys();

	public abstract T create(T input, String currentUser) throws APIValidationException, APIException;

	/**
	 * Update a complete object
	 * @param input 					Object to update
	 * @param currentUser 				current user
	 * @return 							updated object
	 * @throws APIException if the code doesn't correspond to an object 
	 * @throws APIValidationException validation failure
	 */
	public abstract T update(T input, String currentUser) throws APIException, APIValidationException;

	/**
	 * Define only fields to update (not the entire object). <br>
	 * Get the list of editable fields using {@link #authorizedUpdateFields()}.
	 * @param input 			Object to update
	 * @param currentUser 		current user
	 * @param fields 			fields
	 * @return 					updated object
	 * @throws APIException if the code doesn't correspond to an object 
	 * @throws APIValidationException validation failure
	 */
	public abstract T update(T input, String currentUser, List<String> fields) throws APIException, APIValidationException;

	public void delete(String code) throws APIException {
	    if (dao.checkObjectExistByCode(code)) {
	        dao.deleteObject(code);
	    } else {
	        throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
	    }
	}

	/**
	 * Find an object by code in db.
	 * @param code code of object to find
	 * @return     object found in persistent storage
	 */
	public T get(String code) {
		return dao.findByCode(code);
	}

	/**
	 * Get an object by specifying keys.
	 * @param code code of object to find
	 * @param keys restriction keys
	 * @return     object in persistent storage
	 */
	public T getObject(String code, BasicDBObject keys) {
		return dao.getObject(code, keys);
	}

	/**
	 * Checks if an object exists in DB.
	 * @param code code of object to find
	 * @return     true if the object exists in DB, false otherwise
	 */
	public boolean isObjectExist(String code) {
		return dao.isObjectExist(code);
	}

	public Source<ByteString, ?> list(ListFormWrapper<T> wrapper) throws APIException {
		return wrapper.transform().apply(listObjects(wrapper));
	}
	
	public Iterable<T> listObjects(ListFormWrapper<T> wrapper) throws APIException {
		if (wrapper.isReportingMode()) {
			return findByQuery(wrapper.reportingQuery());
		} else if(wrapper.isAggregateMode()) {
			return findByAggregate(wrapper.reportingQuery());
		} else if(wrapper.isMongoJackMode()) {
			return dao.mongoDBFinder(wrapper.getQuery(), wrapper.orderBy(), Sort.ASC, wrapper.getKeys(defaultKeys())).cursor;
		} else {
			throw new APIException("Unsupported query mode");
		}
	}
	
	public MongoCursor<T> findByQuery(String reportingQuery) {
		return dao.findByQuery(reportingQuery);
	}

	public Aggregate.ResultsIterator<T> findByAggregate(String reportingQuery) {
		return dao.findByAggregate(reportingQuery);
	}

	/* ---- method often used in reporting context ---- */
	public Integer count(String reportingQuery) {
		return findByQuery(reportingQuery).count();
	}

	public Source<ByteString, ?> stream(String reportingQuery) {
		return MongoStreamer.streamUDT(findByQuery(reportingQuery));
	}

	/* ------------------------------------------------ */

	/**
	 * @return BasicDBObject which corresponds to the list of default keys from {@link GenericAPI#defaultKeys()}
	 */
	protected BasicDBObject defaultDBKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.putAll(defaultKeys().stream().collect(Collectors.toMap(k -> k, k -> 1)));
		// same as:
		// for(String k : defaultKeys()) 
		//	   keys.put(k, 1);
		return keys;		
	}

	public void checkAuthorizedUpdateFields(ContextValidation ctxVal, List<String> fields) {
		for (String field : fields) 
			if (!authorizedUpdateFields().contains(field)) 
			    ctxVal.addError("fields", "error.valuenotauthorized", field);   
	}

	// This only works for 'field' or 'field.otherfield'. This could have used a split ('.')
	// and checked the path.
	protected void checkIfFieldsAreDefined(ContextValidation ctxVal, List<String> fields, T s) {
	    try {
            for (String field: fields) {
                Matcher matcher = GenericMongoDAO.FIELD_PATTERN.matcher(field);
                if(matcher.matches()){
                    String nPrefix = matcher.group(1);
                    String nField = matcher.group(2);
                    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(s);
                    Object o = wrapper.getPropertyValue(nPrefix);
                    if (wrapper.getPropertyType(nPrefix).getField(nField).get(o) == null) {
                        ctxVal.addError(field, "error.notdefined");
                    }
                } else {
                    if (s.getClass().getField(field).get(s) == null && !field.equals("valuation")) {
                        ctxVal.addError(field, "error.notdefined");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

}
