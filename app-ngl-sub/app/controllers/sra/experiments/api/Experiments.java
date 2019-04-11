package controllers.sra.experiments.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Experiments extends DocumentController<Experiment> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Experiments.class);

	private static final List<String> authorizedUpdateFields = Arrays.asList("accession");

	final Form<ExperimentsSearchForm> experimentsSearchForm;
	final Form<Experiment>            experimentForm;
	final Form<QueryFieldsForm>       updateForm;

//	@Inject
//	public Experiments(NGLContext ctx) {
//		super(ctx,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
//		experimentsSearchForm = ctx.form(ExperimentsSearchForm.class);
//		experimentForm = ctx.form(Experiment.class);
//		updateForm = ctx.form(QueryFieldsForm.class);
//	}

	@Inject
	public Experiments(NGLApplication app) {
		super(app,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
		experimentsSearchForm = app.form(ExperimentsSearchForm.class);
		experimentForm = app.form(Experiment.class);
		updateForm = app.form(QueryFieldsForm.class);
	}

	@Override
	public Result get(String code) {
		Experiment exp  = MongoDBDAO.findOne(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, 
				                             Experiment.class, 
				                             DBQuery.is("code", code));
		if (exp != null) {
			return ok(Json.toJson(exp));
		} else {
			return notFound();
		}	
	}

	public Result list() {
		//if (true){return ok(Json.toJson(new ArrayList<Experiment>()));}
		Form<ExperimentsSearchForm> experimentssSearchFilledForm = filledFormQueryString(experimentsSearchForm, ExperimentsSearchForm.class);
		ExperimentsSearchForm form = experimentssSearchFilledForm.get();
		Query query = getQuery(form);
		MongoDBResult<Experiment> results = mongoDBFinder(form, query);							
		List<Experiment> list = results.toList();
		if (form.datatable) {
			return ok(Json.toJson(new DatatableResponse<>(list, list.size())));
		} else {
			return ok(Json.toJson(list));
		}
	}

	// Met a jour l'experiment dont le code est indiqué avec les valeurs presentes dans l'experiment recuperé du formulaire (userExperiment)
	public Result update(String code) {
		
		Form<Experiment> filledForm = getFilledForm(experimentForm, Experiment.class);
		Experiment userExperiment = filledForm.get();

		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();

//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		Experiment experiment = getObject(code);
		if (experiment == null) {
			//return badRequest("Submission with code "+code+" not exist");
			ctxVal.addError("experiments ", " not exist");
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! updateExperiment : {}", userExperiment.code );
		if (queryFieldsForm.fields == null) {
			if (code.equals(userExperiment.code)) {
//				ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//				ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//				ctxVal.setUpdateMode();
				ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
//				ctxVal.getContextObjects().put("type", "sra");
				ctxVal.putObject("type", "sra");
				userExperiment.traceInformation.setTraceInformation(getCurrentUser());

				//userExperiment.state = new State("V-SUB", getCurrentUser());
				userExperiment.validate(ctxVal);
				logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! updateExperiment : {}", userExperiment.code );
				logger.debug("experiment.state : {}", userExperiment.state.code );
				if (!ctxVal.hasErrors()) {
					MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, userExperiment);
					return ok(Json.toJson(userExperiment));
				} else {
					logger.debug("contextValidation.errors pour experiment : {}", userExperiment.code);
					ctxVal.displayErrors(logger);
					// return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
//				filledForm.reject("experiment code " + code + " and userExperiment.code " + userExperiment.code , " are not the same");
				ctxVal.addError("experiment code " + code + " and userExperiment.code " + userExperiment.code , " are not the same");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
		} else {
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
			ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
			validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
			if (!ctxVal.hasErrors()) {
				updateObject(DBQuery.and(DBQuery.is("code", code)), 
						getBuilder(userExperiment, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(experiment.traceInformation)));

				return ok(Json.toJson(getObject(code)));
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}		
		}
	}

	private Query getQuery(ExperimentsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		if (StringUtils.isNotBlank(form.studyCode)) { //all
			queries.add(DBQuery.in("studyCode", form.studyCode));
		}
		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
			queries.add(DBQuery.in("projectCode", form.projCodes)); 
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		} else if (StringUtils.isNotBlank(form.stateCode)) { //all
			queries.add(DBQuery.in("state.code", form.stateCode));
		}
		if(CollectionUtils.isNotEmpty(form.codes)) {
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		if(CollectionUtils.isNotEmpty(form.accessions)) {
			for (String ac: form.accessions) {
				logger.debug("accession=" + ac);
			}
			queries.add(DBQuery.in("accession", form.accessions));
		} else if(StringUtils.isNotBlank(form.accessionRegex)){
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
	
}
