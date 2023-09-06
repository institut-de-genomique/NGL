package controllers.sra.analyzes.api;

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
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Submission;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;

public class AnalyzesRawDatas extends DocumentController<Analysis> {
 
	private static final play.Logger.ALogger logger = play.Logger.of(AnalyzesRawDatas.class);

	final Form<AnalyzesSearchForm>    analyzesSearchForm;
	final Form<RawData>               rawDataForm;

	@Inject
	public AnalyzesRawDatas(NGLApplication app) {
		super(app,InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class);
		analyzesSearchForm = app.form(AnalyzesSearchForm.class);
		rawDataForm = app.form(RawData.class);
	}

	public Result list() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		//logger.debug("111111111111111  appel de list de AnalyzesRawDatas");
		try {
			List<RawData> allRawDatas = new ArrayList<>();
			Form<AnalyzesSearchForm> form = filledFormQueryString(analyzesSearchForm, AnalyzesSearchForm.class);
			AnalyzesSearchForm formAnalysis = form.get();
			Query query = getQuery(formAnalysis);
			MongoDBResult<Analysis> results = mongoDBFinder(formAnalysis, query);		
			List<Analysis> list = results.toList();
			for(Analysis analysis : list){
				//logger.debug("analysisCode=" + analysis.code);
				if(analysis!=null && analysis.listRawData!=null)
					allRawDatas.addAll(analysis.listRawData);
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

	public Result get(String code, String relatifName) {
		Analysis analysis = MongoDBDAO.findByCode(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, code);
		for(RawData rawData : analysis.listRawData){
			if(rawData.relatifName.equals(relatifName))
				return ok(Json.toJson(rawData));
		}
		return badRequest("No rawData for analysis "+code+" file "+relatifName);
	}
	
	//http://localhost:9000/api/sra/analyzes/ANALYSIS_72FE46AAD/rawDatas/CHE_AH_NLRS_1_ENFK-VL6L-PRPG-TNWU_A.bnx.gz


	public Result update(String code, String relatifName) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {	
			Form<RawData> filledForm = getFilledForm(rawDataForm, RawData.class);

			RawData rawData = filledForm.get();
			Analysis analysis = MongoDBDAO.findByCode(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, code);
			List<RawData> newRawDatas = new ArrayList<>();
			for(RawData rawDataDB : analysis.listRawData){
				if(rawDataDB.relatifName.equals(relatifName))
					newRawDatas.add(rawData);
				else
					newRawDatas.add(rawDataDB);
			}
			analysis.listRawData=newRawDatas;
			MongoDBDAO.update(InstanceConstants.SRA_ANALYSIS_COLL_NAME, analysis);
			return ok(Json.toJson(analysis));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	private Query getQuery(AnalyzesSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;

		if (StringUtils.isNotBlank(form.submissionCode)) {
			//Get submissionFromCode
			Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, form.submissionCode);
			if(submission!=null && StringUtils.isNotBlank(submission.analysisCode)){
				queries.add(DBQuery.in("code", submission.analysisCode));
			} else {
				queries.add(DBQuery.in("code", "NULL")); 
			}
		}

		if(StringUtils.isNotBlank(form.code)) {
			queries.add(DBQuery.in("code", form.code));
		} else if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}


		// end ajout
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
}
