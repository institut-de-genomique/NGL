package controllers.sra.experiments.api;

import java.util.ArrayList;
import java.util.Date;
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
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;

public class ExperimentsRawDatas extends DocumentController<Experiment> {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsRawDatas.class);

	final Form<ExperimentsSearchForm> experimentsSearchForm;
	final Form<RawData>               rawDataForm;

	//	@Inject
	//	public ExperimentsRawDatas(NGLContext ctx) {
	//		super(ctx,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
	//		experimentsSearchForm = ctx.form(ExperimentsSearchForm.class);
	//		rawDataForm = ctx.form(RawData.class);
	//	}

	@Inject
	public ExperimentsRawDatas(NGLApplication app) {
		super(app,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
		experimentsSearchForm = app.form(ExperimentsSearchForm.class);
		rawDataForm = app.form(RawData.class);
	}

	public Result list() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			List<RawData> allRawDatas = new ArrayList<>();
			Form<ExperimentsSearchForm> form = filledFormQueryString(experimentsSearchForm, ExperimentsSearchForm.class);
			ExperimentsSearchForm formExp = form.get();
			Query query = getQuery(formExp);
			MongoDBResult<Experiment> results = mongoDBFinder(formExp, query);		
			List<Experiment> list = results.toList();
			for(Experiment experiment : list){
				//logger.debug("experimentCode = " + experiment.code);
				if(experiment.run!=null && experiment.run.listRawData!=null)
					allRawDatas.addAll(experiment.run.listRawData);
			}
			return ok(Json.toJson(allRawDatas));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	public Result get(String code, String relatifName)
	{
		Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, code);
		for(RawData rawData : experiment.run.listRawData){
			if(rawData.relatifName.equals(relatifName))
				return ok(Json.toJson(rawData));
		}
		return badRequest("No rawData for experiment "+code+" file "+relatifName);
	}
	// Demande de deletion du fichier indiqué .gz pour l'experiment exp_BYQ_AAACOSDA_4_C9GL9ACXX.12BA217
	//http://localhost:9000/api/sra/experiments/exp_BYQ_AAACOSDA_4_C9GL9ACXX.12BA217/rawDatas/BYQ_AAACOSDA_4_1_C9GL9ACXX.12BA217_clean.fastq.gz

	public Result delete(String code, String relatifName) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {	
			//Recuperer l'experiment 
			System.out.println("-----------------------------Entree dans delete avec code='"+code+"' et relatifName='"+relatifName+"'");
			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, code);		
			if (experiment == null) {
				System.out.println("-----------------------------Impossible retrouver l'experiment " + code );
				return badRequest("Impossible retrouver l'experiment " + code + " dans la base");
			}
			Submission submission = MongoDBDAO.findOne(InstanceConstants.SRA_SUBMISSION_COLL_NAME,
					Submission.class, DBQuery.and(DBQuery.in("experimentCodes", experiment.code)));

			if (submission == null) {
				System.out.println("-----------------------------Impossible retrouver la soumission associé à l'experiment " + code );

				return badRequest("Impossible de retrouver la soumission associee à l'experiment " + experiment.code);
			}
			for (RawData rawData : experiment.run.listRawData) {
				if (rawData.relatifName.equals(relatifName)) {
					experiment.run.listRawData.remove(rawData);
					//				ContextValidation contextValidation = new ContextValidation(getCurrentUser());
					//				contextValidation.setUpdateMode();
					ContextValidation contextValidation = ContextValidation.createUpdateContext(getCurrentUser());
					experiment.traceInformation.modifyUser = getCurrentUser();
					experiment.traceInformation.modifyDate = new Date();
					experiment.validate(contextValidation);
					if (contextValidation.hasErrors()) {
						System.out.println("-----------------------------ContextValidation avec erreur");
						contextValidation.displayErrors(logger, "debug");
						contextValidation.addError(relatifName, "la suppression dans la soumission courante '" 
								+ submission.code + "', du fichier '"+relatifName+"' rend l'experiment invalide ", "sans aucun fichier de donnée brute associé");
						return badRequest("la suppression dans la soumission courante '" + submission.code + "', du fichier '"+relatifName+"' rend l'experiment invalide "+contextValidation.getErrors());
					} else {
						System.out.println("-----------------------------Sauvegarde de l'experiment apres deletion de " + rawData.relatifName);
						MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
						return ok(Json.toJson(rawData));
					}
				}
			}
			return badRequest("No rawData for experiment "+code+" file "+relatifName);
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	public Result update(String code, String relatifName) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {	
			Form<RawData> filledForm = getFilledForm(rawDataForm, RawData.class);

			RawData rawData = filledForm.get();
			Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, code);
			List<RawData> newRawDatas = new ArrayList<>();
			for(RawData rawDataDB : experiment.run.listRawData){
				if(rawDataDB.relatifName.equals(relatifName))
					newRawDatas.add(rawData);
				else
					newRawDatas.add(rawDataDB);
			}
			experiment.run.listRawData=newRawDatas;
			MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
			return ok(Json.toJson(experiment));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
	//http://localhost:9000/api/sra/experiments/rawDatas?code=exp_BYQ_AAACOSDA_4_C9GL9ACXX.12BA217
	//http://localhost:9000/api/sra/experiments/rawDatas?runCode=run_BYQ_AAACOSDA_4_C9GL9ACXX.12BA217
	//http://localhost:9000/api/sra/experiments/rawDatas?submissionCode=CNS_BYQ_AWF_24RF4HJFI
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

		if(StringUtils.isNotBlank(form.code)) {
			queries.add(DBQuery.is("code", form.code));
		} else if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}

		// ajout pour interface release study :
		if (StringUtils.isNotBlank(form.studyCode)) {
			queries.add(DBQuery.in("studyCode", form.studyCode));
		}

		if(StringUtils.isNotBlank(form.runCode)){
			queries.add(DBQuery.is("run.code", form.runCode));
		}
		// end ajout
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
}
