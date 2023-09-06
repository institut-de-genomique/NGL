package controllers.sra.studies.api;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Study;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
//Pas utilisée dans le code de ngl-sub
@Deprecated
public class StudiesInternal extends DocumentController<Study> {
	
	final static List<String> authorizedUpdateFields = Arrays.asList("accession","externalId","firstSubmissionDate","releaseDate");

	final Form<Study>           studyForm;
	final Form<QueryFieldsForm> updateForm;
	
//	@Inject
//	public StudiesInternal(NGLContext ctx) {
//		super(ctx,InstanceConstants.SRA_STUDY_COLL_NAME, Study.class);
//		studyForm = ctx.form(Study.class);
//		updateForm = ctx.form(QueryFieldsForm.class);
//	}

	@Inject
	public StudiesInternal(NGLApplication app) {
		super(app,InstanceConstants.SRA_STUDY_COLL_NAME, Study.class);
		studyForm = app.form(Study.class);
		updateForm = app.form(QueryFieldsForm.class);
	}

	public Result update(String code) {
		Study study = getObject(code);

		Form<Study> filledForm = getFilledForm(studyForm, Study.class);
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser(), filledForm); 	

		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();

		if (study == null) {
			//return badRequest("Study with code "+code+" not exist");
			ctxVal.addError("study ", " not exist");
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		Study studyInput = filledForm.get();

		if (queryFieldsForm.fields != null) {
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
			ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
			validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
			
			if (!ctxVal.hasErrors()) {
				updateObject(DBQuery.and(DBQuery.is("code", code)), 
						getBuilder(studyInput, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(study.traceInformation)));
				return ok(Json.toJson(getObject(code)));
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
		}
		return ok(Json.toJson(getObject(code)));
	}


}
