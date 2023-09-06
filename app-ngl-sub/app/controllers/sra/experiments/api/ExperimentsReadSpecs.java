package controllers.sra.experiments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;

public class ExperimentsReadSpecs extends DocumentController<Experiment> {

	final Form<ExperimentsSearchForm> experimentsSearchForm;

	//	@Inject
	//	public ExperimentsReadSpecs(NGLContext ctx) {
	//		super(ctx,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
	//		experimentsSearchForm = ctx.form(ExperimentsSearchForm.class);
	//	}

	@Inject
	public ExperimentsReadSpecs(NGLApplication ctx) {
		super(ctx,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
		experimentsSearchForm = ctx.form(ExperimentsSearchForm.class);
	}

	public Result list() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			List<ReadSpec> allReadSpecs = new ArrayList<>();

			Form<ExperimentsSearchForm> form = filledFormQueryString(experimentsSearchForm, ExperimentsSearchForm.class);
			ExperimentsSearchForm formExp = form.get();
			Query query = getQuery(formExp);
			MongoDBResult<Experiment> results = mongoDBFinder(formExp, query);		
			List<Experiment> list = results.toList();
			for(Experiment experiment : list){
				if(experiment.readSpecs!=null)
					allReadSpecs.addAll(experiment.readSpecs);
			}
			return ok(Json.toJson(allReadSpecs));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	private Query getQuery(ExperimentsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;

		if (StringUtils.isNotBlank(form.submissionCode)) {
			//Get submissionFromCode
			Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, form.submissionCode);
			if(submission!=null && CollectionUtils.isNotEmpty(submission.experimentCodes)){
				queries.add(DBQuery.in("code", submission.experimentCodes));
			} else {
				queries.add(DBQuery.in("code", "NULL")); 
			}
		}
		if(StringUtils.isNotBlank(form.code)){
			queries.add(DBQuery.is("code", form.code));
		}else if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}

		// ajout pour interface release study :
		if (StringUtils.isNotBlank(form.studyCode)) {
			queries.add(DBQuery.in("studyCode", form.studyCode));
		}
		// end ajout
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

}