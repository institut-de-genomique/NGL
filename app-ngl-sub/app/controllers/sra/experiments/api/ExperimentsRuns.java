package controllers.sra.experiments.api;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;

public class ExperimentsRuns extends DocumentController<Experiment> {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsRuns.class);

	final static List<String>   authorizedUpdateFields = Arrays.asList("accession");
	final Form<Experiment>      experimentForm;
	final Form<QueryFieldsForm> updateForm;

	//	@Inject
	//	public ExperimentsRuns(NGLContext ctx) {
	//		super(ctx,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
	//		experimentForm = ctx.form(Experiment.class);
	//		updateForm = ctx.form(QueryFieldsForm.class);
	//	}

	@Inject
	public ExperimentsRuns(NGLApplication app) {
		super(app,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
		experimentForm = app.form(Experiment.class);
		updateForm = app.form(QueryFieldsForm.class);
	}

	@Override
	public Result get(String code) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			Experiment exp  = getExperiment(code);

			if (exp != null) {
				return ok(Json.toJson(exp.run));
			} else {
				return notFound();
			}	
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	public Result update(String code) {
		Form<Experiment> filledForm = getFilledForm(experimentForm, Experiment.class);
		Experiment userExperiment = filledForm.get();

		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		//logger.debug("" + queryFieldsForm.fields);
		//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		//		ValidationContext ctxVal = new ContextValidation(this.getCurrentUser(), filledForm);
		//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(this.getCurrentUser(), filledForm);
		try {
			Experiment experiment = getExperiment(code);
			if (experiment == null) {
				//return badRequest("Submission with code "+code+" not exist");
				ctxVal.addError("experiments ", " not exist");
				//return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			if (queryFieldsForm.fields != null) {
				//			ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
				validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
				validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
				if (!ctxVal.hasErrors()) {
					updateObject(DBQuery.and(DBQuery.is("run.code", code)), 
							getBuilder(userExperiment, queryFieldsForm.fields, Experiment.class, "run").set("traceInformation", getUpdateTraceInformation(experiment.traceInformation)));

					return ok(Json.toJson(getExperiment(code)));
				}else{
					//return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}		
			}
			return ok();
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	private Experiment getExperiment(String code) {
		Experiment exp  = MongoDBDAO.findOne(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, 
				Experiment.class, 
				DBQuery.is("run.code", code));
		return exp;
	}

}