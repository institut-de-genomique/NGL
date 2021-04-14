package controllers.sra.samples.api;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.common.instance.Sample;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;


// Pas utilisée dans le code de ngl-sub.
@Deprecated
public class SamplesInternal extends DocumentController<Sample> {

	final static List<String> authorizedUpdateFields = Arrays.asList("accession","externalId", "scientificName");

	private final Form<Sample>          sampleForm;
	private final Form<QueryFieldsForm> updateForm;

//	@Inject
//	public SamplesInternal(NGLContext ctx) {
//		super(ctx,InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class);
//		sampleForm = ctx.form(Sample.class);
//		updateForm = ctx.form(QueryFieldsForm.class);
//	}
	
	@Inject
	public SamplesInternal(NGLApplication app) {
		super(app,InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class);
		sampleForm = app.form(Sample.class);
		updateForm = app.form(QueryFieldsForm.class);
	}
	
	@Override
	public Result get(String code) {
		return ok(Json.toJson(getSample(code)));
	}

	public Result update(String code) {
		//Get Submission from DB 
		Sample sample = getSample(code);
		Form<Sample> filledForm = getFilledForm(sampleForm, Sample.class);
		
		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
//		ValidationContext ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (sample == null) {
//			filledForm.reject("Sample " +  code, "not exist in database");  // si solution filledForm.reject
//			return badRequest(filledForm.errorsAsJson( )); // legit
			ctxVal.addError("Sample " +  code, "not exist in database");  // si solution filledForm.reject
			return badRequest(errorsAsJson(ctxVal.getErrors())); 
		}
		System.out.println(" ok je suis dans SamplesInternal.update\n");
		Sample sampleInput = filledForm.get();

		if (queryFieldsForm.fields != null) {
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
			validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
			validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);

			if (!ctxVal.hasErrors()) {
				updateObject(DBQuery.and(DBQuery.is("code", code)), 
						getBuilder(sampleInput, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(sample.traceInformation)));

				return ok(Json.toJson(getObject(code)));
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}		
		}
		return ok(Json.toJson(getObject(code)));
	}

	private Sample getSample(String code) {
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, code);
		return sample;
	}

}
