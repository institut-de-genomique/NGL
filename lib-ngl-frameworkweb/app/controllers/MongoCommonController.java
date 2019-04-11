package controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bson.BSONObject;
import org.jongo.Aggregate;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import com.google.common.collect.Iterators;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
// import static fr.cea.ig.util.Streamer.IStreamer.write;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.mongo.MongoStreamer;
import play.data.Form;
import play.libs.Json;
import play.modules.jongo.MongoDBPlugin;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableForm;

// TODO: cleanup, comment

/**
 * Mongo utility methods on top of the api common controller.
 * 
 * @author vrd
 *
 * @param <T> controller object type
 */
public abstract class MongoCommonController<T extends DBObject> extends APICommonController<T> {

	private static final play.Logger.ALogger logger = play.Logger.of(MongoCommonController.class);
	
	/**
	 * Mongo collection name.
	 */
	protected String collectionName;
	
	protected List<String> defaultKeys;
	
	// This is an abstract class so protected or public constructors should not
	// make a difference.
	
//	protected MongoCommonController(NGLContext ctx, String collectionName, Class<T> type) {
//		super(ctx,type);
//		this.collectionName = collectionName;
//	}
//	
//	protected MongoCommonController(NGLContext ctx, String collectionName, Class<T> type, List<String> defaultKeys) {
//		super(ctx,type);
//		this.collectionName = collectionName;
//		this.defaultKeys = defaultKeys;
//	}

	protected MongoCommonController(LFWApplication ctx, String collectionName, Class<T> type) {
		super(ctx,type);
		this.collectionName = collectionName;
	}
	
	protected MongoCommonController(LFWApplication ctx, String collectionName, Class<T> type, List<String> defaultKeys) {
		super(ctx,type);
		this.collectionName = collectionName;
		this.defaultKeys = defaultKeys;
	}

	protected T getObject(String code, BasicDBObject keys) {
    	return MongoDBDAO.findByCode(collectionName, type, code, keys);
    }
	
	protected T getObject(String code) {
    	return MongoDBDAO.findByCode(collectionName, type, code);
    }
	
	protected T getObject(Query query) {
    	return MongoDBDAO.findOne(collectionName, type, query);
    }
	
	protected boolean isObjectExist(String code) {
		return MongoDBDAO.checkObjectExistByCode(collectionName, type, code);
	}
	
	protected boolean isObjectExist(Query query) {
		return MongoDBDAO.checkObjectExist(collectionName, type, query);
	}
	
	protected T saveObject(T o) {
		return MongoDBDAO.save(collectionName, o);
	}
	
	protected void updateObject(T o) {
		MongoDBDAO.update(collectionName, o);
	}
	
	protected void updateObject(Query query, Builder builder){
		MongoDBDAO.update(collectionName, type, query, builder);
	}
	
	protected void deleteObject(String code){
		MongoDBDAO.deleteByCode(collectionName,  type, code);
	}
	
	/*
	 * A finder for mongoDB
	 * @param collection
	 * @param form
	 * @param type (DBObject)
	 * @param query
	 * @return a MongoDBResult
	 */
	// Moved to GenericMongoDAO
//	protected MongoDBResult<T> mongoDBFinder(ListForm form,  DBQuery.Query query) {
//		MongoDBResult<T> results = null;
//		if (form.datatable) {
//			results = MongoDBDAO.find(collectionName, type, query) 
//					            .sort(form.orderBy, Sort.valueOf(form.orderSense));
//			if (form.isServerPagination()) {
//				results.page(form.pageNumber,form.numberRecordsPerPage); 
//			}
//		} else {
//			results = MongoDBDAO.find(collectionName, type, query) 
//					            .sort(form.orderBy, Sort.valueOf(form.orderSense));
//			if (form.limit != -1) {
//				results.limit(form.limit);
//			}
//		}	
//		return results;
//	}
	protected MongoDBResult<T> mongoDBFinder(ListForm form,  DBQuery.Query query) {
		MongoDBResult<T> results = MongoDBDAO.find(collectionName, type, query) 
	                                         .sort(form.orderBy, Sort.valueOf(form.orderSense));
		if (form.datatable && form.isServerPagination())
			results.page(form.pageNumber,form.numberRecordsPerPage); 
		else if (!form.datatable && form.limit != -1)
			results.limit(form.limit);
		return results;
	}

	// Moved to GenericMongoDAO
//	protected MongoDBResult<T> mongoDBFinder(ListForm form, DBQuery.Query query, BasicDBObject keys) {
//		MongoDBResult<T> results = null;
//		
//		if (form.datatable) {
//			results = MongoDBDAO.find(collectionName, type, query, keys) 
//					            .sort(form.orderBy, Sort.valueOf(form.orderSense));
//			if(form.isServerPagination()){
//				results.page(form.pageNumber,form.numberRecordsPerPage); 
//			}
//		} else {
//			results = MongoDBDAO.find(collectionName, type, query, keys) 
//					            .sort(form.orderBy, Sort.valueOf(form.orderSense));
//			if (form.limit != -1) {
//				results.limit(form.limit);
//			}
//		}
//		return results;
//	}
	protected MongoDBResult<T> mongoDBFinder(ListForm form, DBQuery.Query query, BasicDBObject keys) {
		MongoDBResult<T> results = MongoDBDAO.find(collectionName, type, query, keys) 
	                                         .sort(form.orderBy, Sort.valueOf(form.orderSense));		
		if (form.datatable && form.isServerPagination())
			results.page(form.pageNumber,form.numberRecordsPerPage); 
		else if (!form.datatable  && form.limit != -1)
			results.limit(form.limit);
		return results;
	}
	
	// moved to DBObjectRestrictor
	protected BasicDBObject getKeys(DatatableForm form) {
		BasicDBObject keys = new BasicDBObject();
		if (!form.includes.contains("*")) {
//			keys.putAll((BSONObject)getIncludeKeys(form.includes.toArray(new String[form.includes.size()])));
			keys.putAll(getIncludeKeys(form.includes));
		}
//		keys.putAll((BSONObject)getExcludeKeys(form.excludes.toArray(new String[form.excludes.size()])));		
		keys.putAll(getExcludeKeys(form.excludes));		
		return keys;
	}
	
	// BSONObject
	protected BSONObject getIncludeKeys(Set<String> keys) {
		return getIncludeKeys(keys.toArray(new String[keys.size()]));
	}
	// moved to DBObjectRestrictor
	protected BasicDBObject getIncludeKeys(String[] keys) {
		// Why is it sorted ?
		Arrays.sort(keys, Collections.reverseOrder());
		BasicDBObject values = new BasicDBObject();
		for (String key : keys) {
		    values.put(key, 1);
		}
		return values;
    }
	
	protected BSONObject getExcludeKeys(Set<String> keys) {
		return getExcludeKeys(keys.toArray(new String[keys.size()]));
	}
	// moved to DBObjectRestrictor
	protected BasicDBObject getExcludeKeys(String[] keys) {
		// Why is it sorted ?
		Arrays.sort(keys, Collections.reverseOrder());
		BasicDBObject values = new BasicDBObject();
		for (String key : keys) {
		    values.put(key, 0);
		}
		return values;
    }

	protected Builder getBuilder(Object value, List<String> fields) {
		return getBuilder(value, fields, value.getClass(), null);
	}

	/*
	 * Construct a builder from some fields
	 * Use to update a mongodb document
	 * @param value
	 * @param fields
	 * @param clazz
	 * @return
	 */
	protected Builder getBuilder(Object value, List<String> fields, Class<?> clazz) {
		return getBuilder(value, fields, clazz, null);
	}
	
	/*
	 * Construct a builder from some fields
	 * Use to update a mongodb document
	 * @param value
	 * @param fields
	 * @param clazz
	 * @return
	 */
	// moved to GenericMongoDAO<T extends DBObject>
	protected Builder getBuilder(Object value, List<String> fields, Class<?> clazz, String prefix) {
		Builder builder = new Builder();
		try {
			for (String field: fields) {
				String fieldName = (null != prefix) ? prefix + "." + field : field;
				builder.set(fieldName, type.getField(field).get(value));
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return builder;
	}
	
	/*
	 * Validate authorized field for specific update field
	 * @param ctxVal
	 * @param fields
	 * @param authorizedUpdateFields
	 */
	//redefined into SamplesAPI TODO move to an interface ? 
	protected void validateAuthorizedUpdateFields(ContextValidation ctxVal, List<String> fields,
			List<String> authorizedUpdateFields) {
		for (String field: fields) {
			if (!authorizedUpdateFields.contains(field)) {
				ctxVal.addError("fields", "error.valuenotauthorized", field);
			}
		}				
	}
	
	/*
	 * Validate if the field is present in the form
	 * @param ctxVal
	 * @param fields
	 * @param filledForm
	 */
	//redefined into SamplesAPI TODO move to an interface ?
	protected void validateIfFieldsArePresentInForm(ContextValidation ctxVal, List<String> fields, Form<?> filledForm) {
		Object o = filledForm.get();
		for (String field: fields) {
			try {
				if (o.getClass().getField(field).get(o) == null) {
					ctxVal.addError(field, "error.notdefined");
				}
			} catch(Exception e) {
				// Not a an error in the context ?
				logger.error(e.getMessage());
			}
		}	
	}
	
	//@Permission(value={"delete_readset"}) 
	public Result delete(String code) { 
		T objectInDB =  getObject(code);
		if (objectInDB == null) {
			return notFound();
		}
		deleteObject(code);
		return ok();
	}
	
	public Result get(String code) {
		DatatableForm form = filledFormQueryString(DatatableForm.class);
		T o =  getObject(code, getKeys(updateForm(form)));		
		if (o == null) {
			return notFound();
		} 
		return ok(Json.toJson(o));		
	}
		
	//@Permission(value={"reading"})
	public Result head(String code) {
		if (!isObjectExist(code)) {			
			return notFound();					
		}
		return ok();	
	}
		
	//moved to GenericMongoDAO<T extends DBObject>
	protected Result nativeMongoDBQuery(ListForm form) {
		MongoCollection collection = MongoDBPlugin.getCollection(collectionName);
//		MongoCursor<T> all = (MongoCursor<T>) collection.find(form.reportingQuery).as(type);
		MongoCursor<T> all = collection.find(form.reportingQuery).as(type);
		if (form.datatable) {
			// return ok(getUDTChunk(all)).as("application/json");
			return MongoStreamer.okStreamUDT(all);
		} else if(form.list) {
			// return ok(getChunk(all)).as("application/json");
			return MongoStreamer.okStream(all);
		} else if(form.count) {
//			int count = all.count();
//			Map<String, Integer> m = new HashMap<>(1);
//			m.put("result", count);
//			return ok(Json.toJson(m));
			return okCountResult(all.count());
		} else {
			return badRequest();
		}
	}
	
	protected Result nativeMongoDBAggregate(ListForm form) {
		MongoCollection collection = MongoDBPlugin.getCollection(collectionName);
		Aggregate.ResultsIterator<T> all = collection
				.aggregate(form.reportingQuery)
				.options(AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build())
				.as(type);
		
//		if (form.datatable) {
//			// return ok(getUDTChunk(all)).as("application/json");
//			// return ok(MongoStreamer.streamUDT(all)).as("application/json");
//			return MongoStreamer.okStream(all);			
//		} else if(form.list) {
//			//return ok(getChunk(all)).as("application/json");
//			// return ok(MongoStreamer.stream(all)).as("application/json");
//			return MongoStreamer.okStream(all);
		if (form.datatable || form.list) {
			return MongoStreamer.okStream(all);
		} else if(form.count) {
//			int count = Iterators.size(all);
//			Map<String, Integer> m = new HashMap<>(1);
//			m.put("result", count);
//			return ok(Json.toJson(m));
			return okCountResult(Iterators.size(all));
		} else {
			return badRequest();
		}
	}
	
	protected Result mongoJackQuery(ListForm searchForm, Query query) {
		BasicDBObject keys = getKeys(updateForm(searchForm));
		if (searchForm.datatable) {
			MongoDBResult<T> results =  mongoDBFinder(searchForm, query,keys);
			// return ok(getUDTChunk(results)).as("application/json");
			return MongoStreamer.okStreamUDT(results);
		} else if (searchForm.list) {
			keys = new BasicDBObject();
			keys.put("_id", 0); // Don't need the _id field
			keys.put("code", 1);
			if (searchForm.orderBy    == null) searchForm.orderBy    = "code";
			if (searchForm.orderSense == null) searchForm.orderSense = 0;			

			MongoDBResult<T> results = mongoDBFinder(searchForm, query, keys);
			// return ok(getLOChunk(results)).as("application/json");
			return getLOChunk(results);
		} else if (searchForm.count) {
//			keys = new BasicDBObject();
//			keys.put("_id", 1); //Don't need the _id field
//			MongoDBResult<T> results =  mongoDBFinder(searchForm, query);
//			Map<String, Integer> m = new HashMap<>(1);
//			m.put("result", results.count());
//			return ok(Json.toJson(m));
//			return okCountResult(results.count());
			return okCountResult(mongoDBFinder(searchForm, query).count());
		} else {
			if (searchForm.orderBy == null)    searchForm.orderBy    = "code";
			if (searchForm.orderSense == null) searchForm.orderSense = 0; 
			MongoDBResult<T> results = mongoDBFinder(searchForm, query,keys);
			// return ok(getChunk(results)).as("application/json");
			return MongoStreamer.okStream(results);
		}
	}
	
	protected Result okCountResult(int count) {
		return ok("{ result: " + count + "}"); // .as(JSON);
	}
	
	//moved to DBObjectRestrictor
	protected DatatableForm updateForm(DatatableForm form) {
		if (form.includes.contains("default")) {
			form.includes.remove("default");
			if (defaultKeys != null) {
				form.includes.addAll(defaultKeys);
			}
		}
		return form;
	}

	// TODO: Better implementation to choose which property must be used to populate list_object
	//       the better way is to implement getListObject inside DBObject
	private Result getLOChunk(MongoDBResult<T> all) {
		/*return Streamer.okStream(new Streamer.IStreamer() {
			@Override
			public void streamTo(ActorRef out)  {
				Iterator<T> iter = all.cursor;
		    	write(out,"[");
			    while (iter.hasNext()) {
			    	T o = iter.next();
			    	write(out,Json.toJson(new ListObject(o.code, o.code)).toString());
		            if(iter.hasNext()) write(out,",");
		        }					
		        write(out,"]");				
			}
		});*/
//		return MongoStreamer.okStream(all, o -> { return Json.toJson(new ListObject(o.code, o.code)); });
		return MongoStreamer.okStream(all, o -> Json.toJson(new ListObject(o.code, o.code)));
	}
	
	public String getCollectionName() { return collectionName; }
	
}

