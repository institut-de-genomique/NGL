package controllers.projects.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.InstanceConstants;
import models.utils.ListObject;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

/**
 * Controller around Project object
 *
 */
public class UmbrellaProjects extends DocumentController<UmbrellaProject> {

	private static final play.Logger.ALogger logger = play.Logger.of(UmbrellaProjects.class);
	
	final static List<String> authorizedUpdateFields = Arrays.asList("keep");

	private final Form<UmbrellaProjectsSearchForm> searchForm; 
	private final Form<QueryFieldsForm>            updateForm;
	
//	@Inject
//	public UmbrellaProjects(NGLContext ctx) {
//		super(ctx,InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class);		
//		searchForm = ctx.form(UmbrellaProjectsSearchForm.class); 
////		projectForm = ctx.form(UmbrellaProject.class);
//		updateForm = ctx.form(QueryFieldsForm.class);
//	}

	@Inject
	public UmbrellaProjects(NGLApplication app) {
		super(app,InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class);		
		searchForm = app.form(UmbrellaProjectsSearchForm.class); 
		updateForm = app.form(QueryFieldsForm.class);
	}

	public Result list() {
		Form<UmbrellaProjectsSearchForm> filledForm = filledFormQueryString(searchForm, UmbrellaProjectsSearchForm.class);
		UmbrellaProjectsSearchForm form = filledForm.get();
		Query q = getQuery(form);
		BasicDBObject keys = getKeys(form);
		if (form.datatable) {			
			MongoDBResult<UmbrellaProject> results = mongoDBFinder(form, q, keys);			
			List<UmbrellaProject> umbrellaProjects = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(umbrellaProjects, results.count())));
		} else if (form.list) {
			keys = new BasicDBObject();
			keys.put("_id", 0);//Don't need the _id field
			keys.put("name", 1);
			keys.put("code", 1);
			if (null == form.orderBy) form.orderBy = "code";
			if (null == form.orderSense) form.orderSense = 0;
			MongoDBResult<UmbrellaProject> results = mongoDBFinder(form, q, keys);			
			List<UmbrellaProject> umbrellaProjects = results.toList();			
			return ok(Json.toJson(toListObjects(umbrellaProjects)));
		} else {
			if (null == form.orderBy) form.orderBy = "code";
			if (null == form.orderSense) form.orderSense = 0;
			MongoDBResult<UmbrellaProject> results = mongoDBFinder(form, q, keys);	
			List<UmbrellaProject> projects = results.toList();
			return ok(Json.toJson(projects));
		}
	}

	private List<ListObject> toListObjects(List<UmbrellaProject> proj) {
		List<ListObject> lo = new ArrayList<>();
		for (UmbrellaProject p : proj) {
			lo.add(new ListObject(p.code, p.name));
		}
		return lo;
	}
	
	private Query getQuery(UmbrellaProjectsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (CollectionUtils.isNotEmpty(form.projectCodes)) {
			queries.add(DBQuery.in("code", form.projectCodes));
		} else if(StringUtils.isNotBlank(form.projectCode)) {
			queries.add(DBQuery.is("code", form.projectCode));
		}
				
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


	public Result save() {
		Form<UmbrellaProject> filledForm = getMainFilledForm();
		UmbrellaProject projectInput = filledForm.get();

		if (projectInput._id == null) { 
			projectInput.traceInformation = new TraceInformation();
			projectInput.traceInformation.setTraceInformation(getCurrentUser());			
		} else {
			return badRequest("use PUT method to update the project");
		}

//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm); 
		projectInput.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			projectInput = saveObject(projectInput);
			return ok(Json.toJson(projectInput));
		} else {
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	public Result update(String code) {
		UmbrellaProject objectInDB = getObject(code);
		if (objectInDB == null)
			return badRequest("ProjectUmbrella with code "+code+" not exist");

		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		Form<UmbrellaProject> filledForm = getMainFilledForm();
		UmbrellaProject projectInput = filledForm.get();
		if (queryFieldsForm.fields == null) {
			if (code.equals(projectInput.code)) {
				if (projectInput.traceInformation != null) {
					projectInput.traceInformation.setTraceInformation(getCurrentUser());
				} else {
					logger.error("traceInformation is null !!");
				}
//				ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//				ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//				ctxVal.setUpdateMode();
				ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
				projectInput.validate(ctxVal);
				if (!ctxVal.hasErrors()) {
					updateObject(projectInput);
					return ok(Json.toJson(projectInput));
				} else {
					// return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
				return badRequest("Project codes are not the same");
			}	
		} else {
			// warning no validation !!!
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ValidationContext ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
			validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
			// if(!filledForm.hasErrors()){
			if (!ctxVal.hasErrors()) {
				updateObject(DBQuery.and(DBQuery.is("code", code)), 
						getBuilder(projectInput, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
				return ok(Json.toJson(getObject(code)));
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}			
		}
	}


}

