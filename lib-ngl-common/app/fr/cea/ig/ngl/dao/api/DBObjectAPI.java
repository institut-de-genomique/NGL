package fr.cea.ig.ngl.dao.api;

import java.util.List;

import org.jongo.Aggregate;
import org.jongo.Aggregate.ResultsIterator;
import org.jongo.MongoCursor;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import fr.cea.ig.DBObject;
import fr.cea.ig.mongo.MongoStreamer;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.ITracingAccess;
import validation.ContextValidation;
import validation.IValidation;

public interface DBObjectAPI<T extends DBObject> {
	
	public abstract T create(T input, String currentUser) throws APIValidationException, APIException;

	/**
	 * Update a complete object.
	 * @param input 					Object to update
	 * @param currentUser 				current user
	 * @return 							updated object
	 * @throws APIException if the code doesn't correspond to an object 
	 * @throws APIValidationException validation failure
	 */
	public abstract T update(T input, String currentUser) throws APIException, APIValidationException;

	/**
	 * Define only fields to update (not the entire object).
	 * @param input 			Object to update
	 * @param currentUser 		current user
	 * @param fields 			fields
	 * @return 					updated object
	 * @throws APIException if the code doesn't correspond to an object 
	 * @throws APIValidationException validation failure
	 */
	public abstract T update(T input, String currentUser, List<String> fields) throws APIException, APIValidationException;

	/**
	 * Delete an object with the given code.
	 * @param code          object code
	 * @throws APIException error
	 */
	public void delete(String code) throws APIException;

	/**
	 * Find an object by code in DB.
	 * @param code code of object to find
	 * @return     object found in persistent storage
	 */
	public T get(String code);

	/**
	 * Get an object by specifying keys.
	 * @param code code of object to find
	 * @param keys restriction keys
	 * @return     object in persistent storage
	 */
	public T getObject(String code, BasicDBObject keys);

	/**
	 * Checks if an object exists in DB.
	 * @param code code of object to find
	 * @return     true if the object exists in DB, false otherwise
	 */
	public boolean isObjectExist(String code);

	public Source<ByteString, ?> list(ListFormWrapper<T> wrapper) throws APIException;
//	{
//		return wrapper.transform().apply(listObjects(wrapper));
//	}
	
	public Iterable<T> listObjects(ListFormWrapper<T> wrapper) throws APIException;
//	{
//		if (wrapper.isReportingMode()) {
//			return findByQuery(wrapper.reportingQuery());
//		} else if(wrapper.isAggregateMode()) {
//			return findByAggregate(wrapper.reportingQuery());
//		} else if(wrapper.isMongoJackMode()) {
//			return dao.mongoDBFinder(wrapper.getQuery(), "code", Sort.ASC, wrapper.getKeys(defaultKeys())).cursor; //.getCursor();
//		} else {
//			throw new APIException("Unsupported query mode");
//		}
//	}
	
	public MongoCursor<T> findByQuery(String reportingQuery);
//	{
//		return dao.findByQuery(reportingQuery);
//	}

	public Aggregate.ResultsIterator<T> findByAggregate(String reportingQuery);
//	{
//		return dao.findByAggregate(reportingQuery);
//	}

	/* ---- method often used in reporting context ---- */
	public Integer count(String reportingQuery);
//	{
//		return findByQuery(reportingQuery).count();
//	}

//	public Source<ByteString, ?> stream(String reportingQuery);
//	{
//		return MongoStreamer.streamUDT(findByQuery(reportingQuery));
//	}
	
	default Source<ByteString, ?> stream(String reportingQuery) {
		return MongoStreamer.streamUDT(findByQuery(reportingQuery));
	}
	
	/* ------------------------------------------------ */

//	/**
//	 * @return BasicDBObject which corresponds to the list of default keys from {@link GenericAPI#defaultKeys()}
//	 */
//	protected BasicDBObject defaultDBKeys() {
//		BasicDBObject keys = new BasicDBObject();
//		keys.putAll(defaultKeys().stream().collect(Collectors.toMap(k -> k, k -> 1)));
//		// same as
//		// for(String k : defaultKeys()) {
//		//	keys.put(k, 1);
//		// }
//		return keys;		
//	}
//
//	public void checkAuthorizedUpdateFields(ContextValidation ctxVal, List<String> fields) {
//		for (String field: fields) {
//			if (!authorizedUpdateFields().contains(field)) {
//			    ctxVal.addError("fields", "error.valuenotauthorized", field);   
//			}
//		}
//	}
//
//	protected void checkIfFieldsAreDefined(ContextValidation ctxVal, List<String> fields, T s) {
//	    try {
//            for (String field: fields) {
//                Matcher matcher = GenericMongoDAO.FIELD_PATTERN.matcher(field);
//                if(matcher.matches()){
//                    String nPrefix = matcher.group(1);
//                    String nField = matcher.group(2);
//                    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(s);
//                    Object o = wrapper.getPropertyValue(nPrefix);
//                    if (wrapper.getPropertyType(nPrefix).getField(nField).get(o) == null) {
//                        ctxVal.addError(field, "error.notdefined");
//                    }
//                } else {
//                    if (s.getClass().getField(field).get(s) == null) {
//                        ctxVal.addError(field, "error.notdefined");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//	}

}

// DAO based API implementation (default implementation).
abstract class DAO_X_API<T extends DBObject> implements DBObjectAPI<T> {

	private static final play.Logger.ALogger logger = play.Logger.of(DAO_X_API.class);
	
	public static final String CREATE_SEMANTIC_ERROR_MSG = "create method does not update existing objects";
	public static final String INVALID_INPUT_ERROR_MSG = "invalid input";
	public static final String INVALID_STATE_ERROR_MSG = "Invalid state modification";

	GenericMongoDAO<T> dao;
	
	public DAO_X_API(GenericMongoDAO<T> dao) {
		this.dao = dao;
	}
	
	@Override
	public T create(T input, String currentUser) throws APIValidationException, APIException {
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
		if (input.code == null)
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		if (dao.isObjectExist(input.code))
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		finalizeConstruction(ctxVal, input);
		if (input instanceof ITracingAccess) {
//		input.traceInformation = new TraceInformation();
//		input.traceInformation.creationStamp(ctxVal, currentUser);
			((ITracingAccess) input).getTraceInformation().creationStamp(ctxVal, currentUser);
		}
//			input.setTraceCreationStamp(ctxVal, currentUser);
//		if (input.state == null)
//			input.state = new State();
//		input.state.code = ContainerStateNames.N;
//		input.state.user = currentUser;
//		input.state.date = new Date();
		
//		input.validate(ctxVal);
		if (input instanceof IValidation)
			((IValidation)input).validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		return dao.saveObject(input);
	}

	public abstract void finalizeConstruction(ContextValidation c, T t);
	
	@Override
	public T update(T input, String currentUser) throws APIException, APIValidationException {
		if (!isObjectExist(input.code))
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " does not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		if (input instanceof ITracingAccess)
			((ITracingAccess)input).setTraceModificationStamp(ctxVal, currentUser);
		if (input instanceof IValidation)
			((IValidation)input).validate(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		dao.updateObject(input);
		return input;
	}

	abstract void checkAuthorizedUpdateFields(ContextValidation c, List<String> fields);
	abstract void checkIfFieldsAreDefined(ContextValidation c, List<String> fields, T t);
	
	@Override
	public T update(T input, String currentUser, List<String> fields) throws APIException, APIValidationException {
        T runInDB = get(input.code);
        if (runInDB == null)
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist");
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);     
        checkAuthorizedUpdateFields(ctxVal, fields);
        checkIfFieldsAreDefined(ctxVal, fields, input);
        if (ctxVal.hasErrors()) 
        	throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
//        TraceInformation ti = runInDB.traceInformation;
//        ti.modificationStamp(ctxVal, currentUser);
        if (runInDB instanceof ITracingAccess)
        	((ITracingAccess)runInDB).setTraceModificationStamp(ctxVal, currentUser);
        dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ((ITracingAccess)runInDB).getTraceInformation()));
        return get(input.code);
	}

	abstract void finalizeDeletion(String code);
	
	@Override
	public void delete(String code) throws APIException {
        if (!isObjectExist(code))
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
    	dao.deleteObject(code);
        logger.debug("delete readset(s) linked to the Run " + code);
        // readSetDao.deleteObject(DBQuery.is("runCode", code));
        finalizeDeletion(code);
	}

	@Override
	public T get(String code) {
		return dao.findByCode(code);
	}

	@Override
	public T getObject(String code, BasicDBObject keys) {
		return dao.findByCode(code, keys);
	}

	@Override
	public boolean isObjectExist(String code) {
		return dao.isCodeExist(code);
	}

	@Override
	public Source<ByteString, ?> list(ListFormWrapper<T> wrapper) throws APIException {
		throw new RuntimeException();
	}

	@Override
	public Iterable<T> listObjects(ListFormWrapper<T> wrapper) throws APIException {
		throw new RuntimeException();
	}

	@Override
	public MongoCursor<T> findByQuery(String reportingQuery) {
		return dao.findByQuery(reportingQuery);
	}

	@Override
	public ResultsIterator<T> findByAggregate(String reportingQuery) {
		return dao.findByAggregate(reportingQuery);
	}

	@Override
	public Integer count(String reportingQuery) {
		return findByQuery(reportingQuery).count();
	}

}

