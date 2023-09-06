package fr.cea.ig.ngl.dao;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jongo.Aggregate;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import models.utils.dao.DAOException;
import ngl.refactoring.MiniDAO;
import play.modules.jongo.MongoDBPlugin;

// AJ: transformer cette classe en classe abstraite si on décide d'utiliser l'héritage au lieu d'une association d'objet pour les DAO "concrêtes"
public /*abstract*/ class GenericMongoDAO<T extends DBObject> implements MiniDAO<T> {
	
	public static final Pattern FIELD_PATTERN = Pattern.compile("^(\\w+)\\.{1}(\\w+)$");
    private static final play.Logger.ALogger logger = play.Logger.of(GenericMongoDAO.class);
    
	private final String   collectionName;
	private final Class<T> elementClass;
	
	public GenericMongoDAO(final String collectionName, final Class<T> elementClass) {
		this.collectionName = collectionName;
		this.elementClass   = elementClass;
	}
	
	/**
	 * Throw exception if no instance is found.
	 * @param  q            Query
	 * @return              T the DBObject
	 * @throws DAOException if no instance is found
	 */
	public T findOne(Query q) throws DAOException {
		T t = MongoDBDAO.findOne(collectionName, elementClass, q);
		if (t == null) {
		    logger.debug("query: " + q.toString());
			throw new DAOException("no instance found ("+ collectionName + ")");
		}
		return t;
	}
	
	public Iterable<T> find(Query q) throws DAOException {
		return MongoDBDAO.find(collectionName, elementClass, q).cursor;
	}
	
	public List<T> findAsList(Query q) throws DAOException {
		return MongoDBDAO.find(collectionName, elementClass, q).toList();
	}
	
	public Iterable<T> all() throws DAOException {
		return MongoDBDAO.find(collectionName, elementClass).cursor;
	}
	
	public Iterable<T> all_batch(int cp) throws DAOException {
		return MongoDBDAO.find(collectionName, elementClass).cursor.batchSize(cp);
	}	
	@Override
	public T findByCode(String code) throws DAOException {
		return MongoDBDAO.findByCode(collectionName, elementClass, code);
	}
	
	public T findByCode(String code, BasicDBObject keys) throws DAOException {
    	return MongoDBDAO.findByCode(collectionName, elementClass, code, keys);
    }
	
	public T getObject(String code, BasicDBObject keys) throws DAOException {
    	//return MongoDBDAO.findByCode(collectionName, elementClass, code, keys);
		return findByCode(code, keys);
    }
	
	public T getObject(String code) throws DAOException {
		return this.findByCode(code);
	}
	
	public T getObject(Query query) throws DAOException {
    	return this.findOne(query);
    }
	
	public boolean checkObjectExistByCode(String code) throws DAOException {
		return MongoDBDAO.checkObjectExistByCode(collectionName, elementClass, code);
	}
	
	public boolean isObjectExist(String code) throws DAOException {
		return MongoDBDAO.checkObjectExistByCode(collectionName, elementClass, code);
	}
	
	@Override
	public boolean isCodeExist(String code) {
		return isObjectExist(code);
	}
	
	public boolean checkObjectExist(Query query) throws DAOException {
		return MongoDBDAO.checkObjectExist(collectionName, elementClass, query);
	}
	
	public boolean isObjectExist(Query query) throws DAOException {
		return MongoDBDAO.checkObjectExist(collectionName, elementClass, query);
	}
	
	public T save(T o) throws DAOException {
		return MongoDBDAO.save(collectionName, o);
	}

	public T saveObject(T o) throws DAOException {
		return MongoDBDAO.save(collectionName, o);
	}
	
	public void updateObject(T o) throws DAOException {
		MongoDBDAO.update(collectionName, o);
	}
	
	public void updateObject(Query query, Builder builder) throws DAOException {
		MongoDBDAO.update(collectionName, elementClass, query, builder);
	}
	
	public void deleteObject(String code) throws DAOException {
		MongoDBDAO.deleteByCode(collectionName,  elementClass, code);
	}
	
	/**
	 * A finder for mongoDB.
	 * @param query      Query
	 * @param orderBy    String
	 * @param orderSense how to sort the results
	 * @param limit      if it is set to -1 then results are unlimited  
	 * @return           a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinder(Query query, String orderBy, Sort orderSense, Integer limit) throws DAOException {
		MongoDBResult<T> results = MongoDBDAO.find(collectionName, elementClass, query).sort(orderBy, orderSense);
		if (limit != -1) {
			results.limit(limit);
		}
		return results;
	}
	
	/**
	 * A finder for mongoDB.
	 * @param query      Query
	 * @param orderBy    String
	 * @param orderSense how to sort the results
	 * @param limit      if it is set to -1 then results are unlimited  
	 * @param keys       map to restrict keys of object
	 * @return           a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinder(Query query, String orderBy, Sort orderSense, Integer limit, BasicDBObject keys) throws DAOException {
		MongoDBResult<T> results = MongoDBDAO.find(collectionName, elementClass, query, keys).sort(orderBy, orderSense);
		if (limit != -1) {
			results.limit(limit);
		}
		return results;
	}

	/**
	 * A finder for mongoDB without size limit of elements.
	 * @param query      Query
	 * @param orderBy    String
	 * @param orderSense how to sort the results
	 * @return           a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinder(Query query, String orderBy, Sort orderSense) throws DAOException {
		return mongoDBFinder(query, orderBy, orderSense, -1);
	}
	
	/**
	 * A finder for mongoDB without size limit of elements.
	 * @param query      Query
	 * @param orderBy    String
	 * @param orderSense how to sort the results
	 * @param keys       map to restrict keys of object
	 * @return           a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinder(Query query, String orderBy, Sort orderSense, BasicDBObject keys) throws DAOException {
		return mongoDBFinder(query, orderBy, orderSense, -1, keys);
	}
	
	/**
	 * A finder for mongoDB with pagination of results
	 * @param query Query
	 * @param orderBy String
	 * @param orderSense how to sort the results
	 * @param pageNumber Integer
	 * @param numberRecordsPerPage Integer
	 * @return a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinderWithPagination(Query query, String orderBy, Sort orderSense, 
														Integer pageNumber, Integer numberRecordsPerPage) throws DAOException {
		return mongoDBFinder(query, orderBy, orderSense).page(pageNumber, numberRecordsPerPage);
	}
	
	/**
	 * A finder for mongoDB with pagination of results.
	 * @param query                Query
	 * @param orderBy              String
	 * @param orderSense           how to sort the results
	 * @param pageNumber           Integer
	 * @param numberRecordsPerPage Integer
	 * @param keys                 map to restrict keys of object
	 * @return                     a MongoDBResult
	 * @throws DAOException exception
	 */
	public MongoDBResult<T> mongoDBFinderWithPagination(Query query, String orderBy, Sort orderSense, 
														Integer pageNumber, Integer numberRecordsPerPage, BasicDBObject keys) throws DAOException {
		return mongoDBFinder(query, orderBy, orderSense, keys).page(pageNumber, numberRecordsPerPage);
	}

	/**
	 * use to replace controllers.MongoCommonController.nativeMongoDBQuery(ListForm form).
	 * @param query String
	 * @return      mongo cursor
	 */
	public MongoCursor<T> findByQuery(String query) {
		MongoCollection collection = MongoDBPlugin.getCollection(collectionName);
		return collection.find(query).as(elementClass);
	}
	
	/**
     * use to replace controllers.MongoCommonController.nativeMongoDBQuery(ListForm form).
     * @param query String
     * @param keys  keys in json format used to project the result
     * @return      mongo cursor
     */
    public MongoCursor<T> findByQueryWithProjection(String query, String keys) {
        MongoCollection collection = MongoDBPlugin.getCollection(collectionName);
        return collection.find(query).projection(keys).as(elementClass);
    }
	
	/**
	 * use to replace controllers.CommonController.nativeMongoDBAggregate(ListForm form).
	 * @param query String
	 * @return      mongo cursor
	 */
	public Aggregate.ResultsIterator<T> findByAggregate(String query) {
		MongoCollection collection = MongoDBPlugin.getCollection(collectionName);
		BasicDBList mongoList = (BasicDBList)JSON.parse(query);
		final Aggregate aggregateQuery = collection.aggregate(((com.mongodb.BasicDBObject)mongoList.get(0)).toJson());
		for(int i = 1; i < mongoList.size(); i++){
			aggregateQuery.and(((com.mongodb.BasicDBObject)mongoList.get(i)).toJson());
		}
		
		Aggregate.ResultsIterator<T> all = aggregateQuery.options(AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build())
				.as(elementClass);
		return all;
		
	}
	
	/**
	 * Construct a builder from some fields.
	 * Use to update a mongodb document.
	 * @param value  DBObject
	 * @param fields {@literal List<String>}
	 * @return       Builder
	 */
	public Builder getBuilder(T value, List<String> fields) {
		return getBuilder(value, fields, null);
	}
	
	/**
	 * Construct a builder from some fields.
	 * Use to update a mongodb document.
	 * @param value  DBObject
	 * @param fields {@literal List<String>}
	 * @param prefix String if you want to access to an encapsulated object
	 * @return       Builder
	 */
	public Builder getBuilder(T value, List<String> fields, String prefix) {
		Builder builder = new Builder();
		try {
			for (String field: fields) {
				Matcher matcher = FIELD_PATTERN.matcher(field);
				if(prefix != null) {
					String fieldName = prefix + "." + field ;
					BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
					Object o = wrapper.getPropertyValue(prefix);
					builder.set(fieldName, wrapper.getPropertyType(prefix).getField(field).get(o));
				} else if(matcher.matches()){
					String fieldName = field ;
					String nPrefix = matcher.group(1);
					String nField = matcher.group(2);
					BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
					Object o = wrapper.getPropertyValue(nPrefix);
					builder.set(fieldName, wrapper.getPropertyType(nPrefix).getField(nField).get(o));
				} else {
					builder.set(field, elementClass.getField(field).get(value));
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return builder;
	}

//	public Iterable<T> aggregate(Pipeline<Expression<?>> pipeline) {
//		return MongoDBDAO.getCollection(collectionName, elementClass).aggregate(pipeline, elementClass).results();
//	}
	public Iterable<T> aggregate(Pipeline<Expression<?>> pipeline) {
		return MongoDBDAO.aggregate(collectionName, elementClass, pipeline);
	}
	
	/// ---- Recablage iso 
	
	public boolean checkObjectExist(String key, String keyValue) {
		//logger.debug("collectionName=" + collectionName + " key="+ key + " keyValue=" + keyValue);
		return MongoDBDAO.checkObjectExist(collectionName, elementClass, key, keyValue);
	}
	
	public void update(T o) throws DAOException {
		MongoDBDAO.update(collectionName, o);
	}
	
	public void update(Query query, Builder builder) throws DAOException {
		//logger.debug("KKKKKKKKKKKKKKKKK   Dans GenericMongDAO.update collectionName=" + collectionName + " elementClass=" +elementClass );
		MongoDBDAO.update(collectionName, elementClass, query, builder);
	}
	
	public void deleteByCode(String code) throws DAOException {
		MongoDBDAO.deleteByCode(collectionName,  elementClass, code);
	}
	
	public MongoDBResult<T> dao_find(Query q) {
		return MongoDBDAO.find(collectionName, elementClass, q);
	}

    public String getCollectionName() {
        return collectionName;
    }

    public Class<T> getElementClass() {
        return elementClass;
    }
	
}

