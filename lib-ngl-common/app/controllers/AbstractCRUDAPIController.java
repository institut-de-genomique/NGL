package controllers;

import java.lang.reflect.Field;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.instance.ITracingAccess;
import models.utils.InstanceHelpers;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import validation.ICRUDValidatable;
import views.components.datatable.DatatableForm;

// Aborted prototype implementation.

/**
 * CRUD API controller base for DBObject subclasses.
 * This uses a heavy strategy for the updates as it pulls the
 * old object from the data base so update validation can use the
 * past and the future values. 
 * 
 * Implementation relies on
 * <ul>
 *   <li>{@link validation.IValidation}</li>
 *   <li>{@link validation.ICRUDValidatable}</li>
 *   <li>{@link models.laboratory.common.instance.ITracingAccess}</li>
 *   <li>{@link controllers.ICommentable}</li>
 * </ul>
 * 
 * @author vrd
 *
 * @param <T> class to provide CRUD for
 */
// TODO: extend MongoController instead of DocumentController
public abstract class AbstractCRUDAPIController<T extends DBObject & ICRUDValidatable<T>> extends DocumentController<T> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(AbstractCRUDAPIController.class);
	
	/**
	 * Construct a CRUD controller for the type that is stored in a named collection
	 * @param ctx            NGL context
	 * @param collectionName Mongo collection name
	 * @param type           type of objects to provide CRUD for
	 * @param defaultKeys    default list of field name
	 */
	public AbstractCRUDAPIController(NGLContext ctx, String collectionName, Class<T> type, List<String> defaultKeys) {
		super(ctx,collectionName, type, defaultKeys);	
	}

	// --------------------- CREATE -----------------------------------------
	// Overridable hooks for the creation, null return aborts early
	/**
	 * Ran before object validation.
	 * The default implementation returns the input object.
	 * @param ctx    validation context
	 * @param t   object to modify and/or validate
	 * @return       null if the creation must be aborted, the modified instance otherwise
	 */
	public T beforeCreationValidation(ContextValidation ctx, T t) {
		return t;
	}
	
	/**
	 * Ran after object validation.
	 * The default implementation returns the input object.
	 * @param ctx validation context
	 * @param t   object to modify and/or validate
	 * @return    null if the creation must be aborted, the modified instance otherwise
	 */
	public T afterCreationValidation(ContextValidation ctx, T t) {
		return t;
	}
	
	/**
	 * Application level create operation.
	 * @param ctx validation context 
	 * @param t   instance to persist in the database 
	 * @return    persisted instance
	 */
	public T create(ContextValidation ctx, T t) {
		logger.debug("api - create " + t);
		// Validate t as a DBObject
		if (t._id != null)
			ctx.addError("_id", "must not be present when creating an object, maybe you wanted to update the objet");
		// If t is ITraceable, stamp the creation
		if (t instanceof ITracingAccess) {
			ITracingAccess ta = (ITracingAccess)t;
			logger.debug("" + t + " / ITracingAccess : setting creation stamp");
			ta.setTraceCreationStamp(ctx, getCurrentUser());
			logger.debug("" + t + " / ITracingAccess " + ta.getTraceInformation().createUser + " " + ta.getTraceInformation().creationDate);
		}
		if (t instanceof ICommentable)
			((ICommentable) t).setComments(InstanceHelpers.updateComments(((ICommentable) t).getComments(), ctx));
		// Call before creation hook
		if ((t = beforeCreationValidation(ctx,t)) == null)
			return null;
		// If it's a IValidatable, set context mode to creation and 
		// call the validate.
//		if (t instanceof ICRUDValidatable) {
//			((ICRUDValidatable<?>)t).validateInvariants(ctx);
//			((ICRUDValidatable<?>)t).validateCreation(ctx);
//		} else if (t instanceof IValidation) {
//			ctx.setCreationMode();
//			((IValidation)t).validate(ctx);
//		}
		// Enforced by type constraints in class declaration
		t.validateInvariants(ctx);
		t.validateCreation(ctx);
		if ((t = afterCreationValidation(ctx,t)) == null)
			return null;
		// Check for validation errors and abort creation if errors are present 
		if (ctx.hasErrors())
			return null;
		// save object
		return saveObject(t);
	}
	
	/**
	 * Route level object creation. The data needed for the creation is
	 * retrieved from the http request.
	 * @return either the created object as json or errors as json 
	 */
	public Result create() {
		// Fetch instance to create from request
		Form<T> filledForm = getMainFilledForm();
		T t = filledForm.get();
		// Create a validation context
//		ContextValidation ctx = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation ctx = new ContextValidation(getCurrentUser(), filledForm);
		t = create(ctx,t);
		return checkedResult(ctx,t);
	}

	// --------------------------------- READ -----------------------------------
	// -- expected method : return MongoDBDAO.findByCode(collectionName, type, code, keys);
	// see get(code) implementation in MongoCommonController
	
	public Result read(String code) {
		ContextValidation ctx = new ContextValidation(getCurrentUser());
		T t = null;
		if (request().queryString().size() > 0) {
			DatatableForm form = filledFormQueryString(DatatableForm.class);
			t =  getObject(code, getKeys(updateForm(form)));		
		} else { 
			t =  getObject(code);
		}
		if (t == null)
			ctx.addError("object","no object found for code %s",code);
		return checkedResult(ctx,t);
	}

	// -------------------------------- UPDATE ----------------------------------
	
	/**
	 * Applies known updates for the common interfaces. 
	 * @param ctx validation context
	 * @param t   object to apply updates to
	 */
	private void applyKnownUpdateInterfaces(ContextValidation ctx, T t) {
		if (t instanceof ITracingAccess)
			((ITracingAccess)t).setTraceUpdateStamp(ctx, getCurrentUser());
		if (t instanceof ICommentable)
			((ICommentable) t).setComments(InstanceHelpers.updateComments(((ICommentable) t).getComments(), ctx));		
	}

	/**
	 * List of names of field that allow updates.
	 * @return List of name of field that allow updates.
	 */
	public abstract List<String> getAuthorizedUpdateFields();
	
	// Update hooks

	/**
	 * Ran before the object validation.
	 * Default implementation return the input object.
	 * @param ctx validation context
	 * @param past   past state of object
	 * @param future object to modify and/or validate
	 * @return    null if the update must be aborted otherwise the future object
	 */
	public T beforeUpdateValidation(ContextValidation ctx, T past, T future) {
		return future;
	}
	
	/**
	 * Ran after object validation.
	 * @param ctx validation context
	 * @param past   past state of object
	 * @param future object to modify and/or validate
	 * @return    null if the creation should be aborted otherwise the future object
	 */
	public T afterUpdateValidation(ContextValidation ctx, T past, T future) {
		return future;
	}

	/*
	 * Api level update. This triggers the full validation.
	 * For partial validation @see #partialUpdate(ContextValidation, List, DBObject) 
	 * @param ctx validation context
	 * @param t   new object values
	 * @return    updated object
	 */
	/*public T update(ContextValidation ctx, T t) {
		T past = getObject(t.getCode());
		return update(ctx,past,t);
	}*/
	
	/**
	 * 
	 * @param ctx    validation context
	 * @param past   past state of object
	 * @param t      object to modify and/or validate
	 * @return       updated object
	 */
	public T update(ContextValidation ctx, T past, T t) {
		if (t._id == null)
			ctx.addError("_id", "must be present when updating an object, maybe you wanted to create the objet");
		// If t is ITraceable, stamp the creation
		/*if (t instanceof ITracingAccess)
			((ITracingAccess)t).setTraceUpdateStamp(ctx, getCurrentUser());
		if (t instanceof ICommentable)
			((ICommentable) t).setComments(InstanceHelpers.updateComments(((ICommentable) t).getComments(), ctx));*/
		applyKnownUpdateInterfaces(ctx,t);
		// Call before update hook
		if ((t = beforeUpdateValidation(ctx,past,t)) == null)
			return null;
		// If it's a IValidatable, set context mode to creation and 
		// call the validate.
//		if (t instanceof ICRUDValidatable) {
////			((ICRUDValidatable)t).validateInvariants(ctx);
////			((ICRUDValidatable)t).validateUpdate(ctx,past);
//			t.validateInvariants(ctx);
//			t.validateUpdate(ctx,past);
//		} else if (t instanceof IValidation) {
//			ctx.setUpdateMode();
//			((IValidation)t).validate(ctx);
//		}
		// Enforced by class declaration constraints
		t.validateInvariants(ctx);
		t.validateUpdate(ctx,past);
		if ((t = afterUpdateValidation(ctx,past,t)) == null)
			return null;
		// Check for validation errors and abort creation if errors are present 
		if (ctx.hasErrors())
			return null;
		// save object
		MongoDBDAO.update(getCollectionName(), t);
		return t;
	}

	/**
	 * Check that a list of fields is included in the authorized field updates.
	 * @param ctx    validation context
	 * @param fields list of field names to validate
	 */
	protected void validateAuthorizedUpdateFields(ContextValidation ctx, List<String> fields) {
		List<String> authorizedUpdateFields = getAuthorizedUpdateFields();
		for (String field: fields)
			if (!authorizedUpdateFields.contains(field))
				ctx.addError("fields", "error.valuenotauthorized", field);
	}

	/**
	 * Check that the list of fields are backed by actual fields in the object.
	 * @param ctx    validation context
	 * @param fields list of field names to validate
	 * @param t      object that has the fields
	 */
	protected void validateIfFieldsArePresentInObject(ContextValidation ctx, List<String> fields, T t) {
		Class<?> clazz = t.getClass();
		for (String field: fields) {
			try {
				Field f = clazz.getField(field);
				if (f == null)
					ctx.addErrors(field, "error.notdefined");
				else if (f.get(t) == null) {
					ctx.addErrors(field, "error.notdefined");
				}
			} catch(Exception e) {
				Logger.error(e.getMessage());
				ctx.addError(field, "exception", e);
			}
		}	
	}
	
	/**
	 * Ran before the partial update. This is where the partial validation
	 * is supposed to occur.
	 * DEfault implementation returns the input object.
	 * @param ctx    validation context
	 * @param fields fields that are updated
	 * @param t      object to validate and/modify
	 * @return       null if the update must be aborted or the modified instance
	 */
	public T beforePartialUpdate(ContextValidation ctx, List<String > fields, T t) {
		return t;
	}

	// Missing builder hook before partial update

	// Need to abstract what a partial update is, currently a list of fields
	// Is the instance partially filled ?
	
	/**
	 * Partial update. The object must have a valid code field. 
	 * @param ctx    validation context
	 * @param fields fields to update
	 * @param t      object to update
	 * @return       persisted instance
	 */
	public T partialUpdate(ContextValidation ctx, List<String > fields, T t) {
		if (t.getCode() == null) {
			ctx.addError("code", "instance code must be provided for partial updates");
			return null;
		}
		ctx.setUpdateMode();
		// Are the provided update fields in the white list ?
		validateAuthorizedUpdateFields    (ctx, fields);
		// The provided fields have filled the object so this seems quite
		// pointless (extra fields are ignored at best). 
		validateIfFieldsArePresentInObject(ctx, fields, t);

		if (!ctx.hasErrors()) {
			/*if (t instanceof ITracingAccess)
				 ((ITracingAccess)t).setTraceUpdateStamp(ctx, getCurrentUser());
			 if (t instanceof ICommentable)
				 ((ICommentable) t).setComments(InstanceHelpers.updateComments(((ICommentable) t).getComments(), ctx));*/
			applyKnownUpdateInterfaces(ctx,t);
			// Call before update hook
			if ((t = beforePartialUpdate(ctx,fields,t)) == null)
				return null;
			// Check for validation errors and abort creation if errors are present 
			if (ctx.hasErrors())
				return null;
			// Should call hook for builder
			Builder b = getBuilder(t, fields);
			if (t instanceof ITracingAccess)
				b.set("traceInformation",((ITracingAccess) t).getTraceInformation());
			updateObject(DBQuery.is("code", t.getCode()), b);
		}		
		return t;
	}

	public Result update(String code) {
		// Test query string, could just test if it exists in the first place
		Form<QueryFieldsForm> filledQueryFieldsForm = getQueryStringForm(QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		Form<T> filledForm = getMainFilledForm();
		T t = filledForm.get();
//		ContextValidation ctx = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation ctx = new ContextValidation(getCurrentUser(), filledForm);
		// TODO: We could use a possibly faster test (!isObjectExist(code))
		T dbo = getObject(code);
		if (dbo == null) {
			ctx.addError("object", "object with code %s not found", code);
		} else if (queryFieldsForm.fields != null) {
			if (t.getCode() == null) {
				t.setCode(code);
				partialUpdate(ctx,queryFieldsForm.fields,t);
			} else if (!t.getCode().equals(code)) {
				ctx.addError("object", "route and instance code do not match");
			} else {
				partialUpdate(ctx,queryFieldsForm.fields,t);
			}
		} else {
			if (t.getCode() == null) {
				ctx.addError("object", "instance code is not set");
			} else if (!t.getCode().equals(code)) {
				ctx.addError("object", "route and instance code do not match");
			} else {
				t = update(ctx,dbo,t);
			}
		}
		return checkedResult(ctx,t);
	}
	
	// --------------------------------- DELETE ------------------------------------------
	
	public T beforeDeletionValidation(ContextValidation ctx, T t) { return t; }
	public T afterDeletionValidation(ContextValidation ctx, T t) { return t; }
	
	public T delete(ContextValidation ctx, T t) {
		if (t._id == null)
			ctx.addError("_id", "must be present when deleting an object, maybe you wanted to create the objet");
		if ((t = beforeDeletionValidation(ctx,t)) == null)
			return null;
		// If it's a IValidatable, set context mode to creation and 
		// call the validate.
//		if (t instanceof IValidation) {
//			ctx.setDeleteMode();
//			((IValidation)t).validate(ctx);
//		} else if (t instanceof ICRUDValidatable) {
//			((ICRUDValidatable<T>)t).validateInvariants(ctx);
//			((ICRUDValidatable<T>)t).validateDelete(ctx);
//		}
		// Enforced by class declaration type constraints
		t.validateInvariants(ctx);
		t.validateDelete(ctx);
		if ((t = afterDeletionValidation(ctx,t)) == null)
			return null;
		// Check for validation errors and abort creation if errors are present 
		if (ctx.hasErrors())
			return null;
		MongoDBDAO.deleteByCode(getCollectionName(),  type, t.getCode());
		return t;
	}
	
	@Override
	public Result delete(String code) {
		ContextValidation ctx = new ContextValidation(getCurrentUser());
		T t = getObject(code);
		if (t == null) 
			ctx.addError("object", "object with code %s not found", code);
		else
			delete(ctx,t);
		return checkedResult(ctx,t);		
	}
	
	// ---------------------------- LIST ----------------------
	public abstract DBQuery.Query getQuery(ContextValidation ctx, ListForm f);
	
	public <F extends ListForm> Result list(Class<F> clazz) {
		
		throw new RuntimeException();
		
		// Voir Experiments
		
//		F searchForm = filledFormQueryString(clazz);
//		if (searchForm.reporting)
//			return nativeMongoDBQuery(searchForm);
//		
//		ContextValidation ctx = new ContextValidation(getCurrentUser());
//		DBQuery.Query query = getQuery(ctx,searchForm);
//		
//		if (searchForm.datatable) {
//			BasicDBObject keys = getKeys(updateForm(searchForm));
//			MongoDBResult<T> results = mongoDBFinder(searchForm,query, keys);
//			// return ok(MongoStreamer.streamUDT(results)).as("application/json");
//			return MongoStreamer.okStreamUDT(results);
//		} else if (searchForm.count) {
//			// This really should be some almost plain mongo access and plain json generation
//			BasicDBObject keys = new BasicDBObject();
//			keys.put("_id",  0); //Don't need the _id field
//			keys.put("code", 1);
//			// MongoDBResult<Container> results = mongoDBFinder(InstanceConstants.CONTAINER_COLL_NAME, containersSearch, Container.class, query, keys);
//			MongoDBResult<T> results = mongoDBFinder(searchForm, query, keys);
//			int count = results.count();
//			// Map<String, Integer> m = new HashMap<String, Integer>(1);
//			// m.put("result", count);
//			// return ok(Json.toJson(m));
//			return ok("{ \"result\" : " + count + " }");
//		} else if (searchForm.list) {
//			BasicDBObject keys = getKeys(updateForm(searchForm));
//			// MongoDBResult<Container> results = mongoDBFinder(InstanceConstants.CONTAINER_COLL_NAME, containersSearch, Container.class, query, keys);
//			MongoDBResult<T> results = mongoDBFinder(searchForm, query, keys);
//			List<T> containers = results.toList();
//			List<ListObject> los = new ArrayList<ListObject>();
//			for (T p: containers) {
//				los.add(new ListObject(p.code, p.code));
//			}
//			return ok(Json.toJson(los));
//		} else {
//			// DBQuery.Query query = getQuery(ctx,searchForm);
//			if (ctx.hasErrors())
//				return badRequest(errorsAsJson(ctx.getErrors()));			
//			return mongoJackQuery(searchForm, query);			
//		}		
	}
	
	
	// TODO: log error
	public <A> Result checkedResult(ContextValidation ctx, A a) {
		if (a == null && !ctx.hasErrors())
			ctx.addError("internal", "null object reference and no error in context");
		if (ctx.hasErrors())
			return badRequest(errorsAsJson(ctx.getErrors()));
		return ok(Json.toJson(a));
	}

	// TODO: fix source
	public Result badRequest(String message, Object... args) {
		// Could build a validation context and use the standard error return.
		return badRequest(ctx.errorAsJson(message,args));
	}

	
	// Uses partial update mode
	
	/*Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);

	QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
	// Form<Sample> filledForm = getFilledForm(sampleForm, Sample.class);
	Form<T> filledForm = getMainFilledForm();
	T sampleInForm = filledForm.get();

	
	 
		/*
		public Meta<T> meta(String name, BiConsumer<ContextValidation,T> validator, BiConsumer<T,T> updater) {
			return new Meta<T>(name,validator,updater);
		}
		// Give a list of fields to update. validate against defined lists.
		// for each possible field, we define the validation function
		// as some external validate method. 
		public void partialUpdate(ContextValidation ctx, T t, List<String> fields) {
			// A partial update maps a field name to some validation function.
			// The name must belong to the authorized update fields.
			// The partial update triggers weak validation that is epxressed
			// in some provided mapping.
			Map<String,Meta<T>> meta = meta();
			for (String fn : fields) 
				// If the validator is defined, the the update is authorized.
				// Could use function and null return as fail early
				if (meta.containsKey(fn)) 
					meta.get(fn).validator.accept(ctx, t);
			// Build the query for the partial update
			
		}
		
		// Given a dboject and a form object, copy the provided fields by names.
		// Then trigger the full update. Seems simple enough.
		public T partialAsFullUpdate(ContextValidation ctx, T dbo, T ino, List<String> fields) {
			for (String fn : fields)
				// If the validator is defined, the the update is authorized.
				// Could use function and null return as fail early
				if (meta.containsKey(fn)) 
					meta.get(fn).updater.accept(ino,dbo);
			return fullUpdate(ctx,dbo);
		}
		
			// The update is either a full or a partial update. This makes no sense to have a compound
	// application api.
	public T update(ContextValidation ctx, T t) {
		T u = getObject(t.getCode());
		// Abort early if the object to update is not in the database.
		if (u == null) {
			// TODO:check if this is the proper call
			ctx.addError("MongoDB", "object %s does not exist in database", t.getCode());
			return null;
		}
		
		return null;
	}
	
	public void partialUpdateValidation() {
		
	}
	
	private void applyPartialUpdate() {
	}
	
	// Information about partial updates.
	public static class Meta<T> {
		String name; // field name in class
		BiConsumer<ContextValidation,T> validator;
		BiConsumer<T,T> updater;
		public Meta(String name, BiConsumer<ContextValidation,T> validator, BiConsumer<T,T> updater) {
			this.name = name;
			this.validator = validator;
			this.updater = updater;
		}
	}
	// Reflection copy to lighten the definition.

*/		
	// We index the meta info by class so the build meta results
	// are cached.
	/*private static Map<Class<?>,Map<String,Meta<?>>> metaCatalog;
	protected Map<String,Meta<T>> meta() {
		Map<String,Meta<T>> meta = metaCatalog.get(type);
		if (meta == null) {
			meta = new HashMap<String,Meta<T>>();
			for (Meta<T> m : buildMeta()) {
				meta.put(m.name,m);
			}
			metaCatalog.put(type, meta);
		}
		return meta;
	}*/
	// Can we control a bit more ?
	/*
	private Map<String,Meta<T>> meta = null;
	protected abstract Collection<Meta<T>> buildMeta();
	protected Map<String,Meta<T>> meta() {
		if (meta == null) {
			meta = new HashMap<String,Meta<T>>();
			for (Meta<T> m : buildMeta()) {
				meta.put(m.name,m);
			}
		}
		return meta;
	}
	public BiConsumer<ContextValidation,T> pass = (ctx,val) -> {};
	*/
	// This would require the validation context to return proper errors.
	// Could be passed using a 3rd arg.
	/*public BiConsumer<T,T> reflectCopy(String n) {
		Field f = type.getField(n);
		return (from,to) -> {
			try {
				f.set(to,f.get(from));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		};	
	}
	public Meta<T> metareflect(String n) {
		return new Meta<T>(n,pass,reflectCopy(n));
	}*/

}
