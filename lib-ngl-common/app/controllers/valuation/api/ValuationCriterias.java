package controllers.valuation.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.valuation.instance.ValuationCriteria;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class ValuationCriterias extends DocumentController<ValuationCriteria> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ValuationCriterias.class);
	
	private final Form<ValuationCriteria>            valuationCriteriaForm;
	private final Form<ValuationCriteriasSearchForm> searchForm;
	
//	@Inject
//	public ValuationCriterias(NGLContext ctx) {
//		super(ctx, InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class);
//		searchForm = ctx.form(ValuationCriteriasSearchForm.class);
//		valuationCriteriaForm = ctx.form(ValuationCriteria.class);
//	}
	
	@Inject
	public ValuationCriterias(NGLApplication app) {
		super(app, InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class);
		searchForm = app.form(ValuationCriteriasSearchForm.class);
		valuationCriteriaForm = app.form(ValuationCriteria.class);
	}

	public Result list() {
		Form<ValuationCriteriasSearchForm> filledForm = filledFormQueryString(searchForm, ValuationCriteriasSearchForm.class);
		ValuationCriteriasSearchForm form = filledForm.get();
		
		Query q = getQuery(form);
		BasicDBObject keys = getKeys(form);
		if (form.datatable) {			
			MongoDBResult<ValuationCriteria> results = mongoDBFinder(form, q, keys);
//			MongoDBResult<ValuationCriteria> results = mongoDBFinder(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, form, ValuationCriteria.class, q, keys);
			List<ValuationCriteria> list = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(list, results.count())));
		} else if(form.list) {
			MongoDBResult<ValuationCriteria> results = mongoDBFinder(form, q, keys);
//			MongoDBResult<ValuationCriteria> results = mongoDBFinder(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, form, ValuationCriteria.class, q, keys);
			List<ValuationCriteria> criterias = results.toList();
			return ok(Json.toJson(criterias));
		} else {
			MongoDBResult<ValuationCriteria> results = mongoDBFinder(form, q, keys);
//			MongoDBResult<ValuationCriteria> results = mongoDBFinder(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, form, ValuationCriteria.class, q, keys);							
			List<ValuationCriteria> criterias = results.toList();
			return ok(Json.toJson(criterias));
		}
	}
	
	private Query getQuery(ValuationCriteriasSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (form.objectTypeCode != null) { //all
			queries.add(DBQuery.is("objectTypeCode", form.objectTypeCode.toString()));
		}
		
		if (CollectionUtils.isNotEmpty(form.typeCodes)) { //all
			queries.add(DBQuery.in("typeCodes", form.typeCodes));
		} else if(StringUtils.isNotBlank(form.typeCode)) {
			queries.add(DBQuery.in("typeCodes", form.typeCode));
		}
		
		//Filter by active property
		//queries.add(DBQuery.is("active", Boolean.TRUE));
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		
		return query;
	}

	@Override
	public Result get(String code) {
		ValuationCriteria reportingConfiguration =  getByCode(code);		
		if (reportingConfiguration != null) {
			return ok(Json.toJson(reportingConfiguration));	
		} else {
			return notFound();
		}			
	}
	
	public Result save() {
		Form<ValuationCriteria> filledForm = getFilledForm(valuationCriteriaForm, ValuationCriteria.class);
		ValuationCriteria objectInput = filledForm.get();

		if (objectInput._id == null) {
			objectInput.traceInformation = new TraceInformation();
			objectInput.traceInformation
					.setTraceInformation(getCurrentUser());
			objectInput.code = generateCode();
		} else {
			return badRequest("use PUT method to update the run");
		}
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		objectInput.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			objectInput = MongoDBDAO.save(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, objectInput);
			return ok(Json.toJson(objectInput));
		} else {
			// return badRequest(filledForm.errors-AsJson());
//			return badRequest(NGLContext._errorsAsJson(ctxVal.getErrors()));
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	public Result update(String code) {
		ValuationCriteria objectFromDB = getByCode(code);
		if (objectFromDB == null) {
			return badRequest("ValuationCriteria with code "+objectFromDB+" does not exist");
		}
		Form<ValuationCriteria> filledForm = getFilledForm(valuationCriteriaForm, ValuationCriteria.class);
		ValuationCriteria objectInput = filledForm.get();

		if (objectFromDB.code.equals(code)) {
			if (objectInput.traceInformation != null) {
				objectInput.traceInformation.setTraceInformation(getCurrentUser());
			} else {
				logger.error("traceInformation is null !!");
			}
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
//			ctxVal.setCreationMode();
			ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
			objectFromDB.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, objectInput);
				return ok(Json.toJson(objectInput));
			} else {
				// return badRequest(filledForm.errors-AsJson());
//				return badRequest(NGLContext._errorsAsJson(ctxVal.getErrors()));
				return badRequest(app.errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			return badRequest("readset code are not the same");
		}				
	}
	
	@Override
	public Result delete(String code) {
		ValuationCriteria objectFromDB =  getByCode(code);
		if(objectFromDB == null) {
			return badRequest("ValuationCriteria with code "+objectFromDB+" does not exist");
		}
		MongoDBDAO.deleteByCode(InstanceConstants.VALUATION_CRITERIA_COLL_NAME,  ValuationCriteria.class, objectFromDB.code);
		return ok();
	}
	
	private static String generateCode(){
		return ("VC-"+(new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date())).toUpperCase();		
	}
	
	private static ValuationCriteria getByCode(String code) {
		ValuationCriteria reportingConfiguration = MongoDBDAO.findByCode(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, code);
    	return reportingConfiguration;
	}
	
}
