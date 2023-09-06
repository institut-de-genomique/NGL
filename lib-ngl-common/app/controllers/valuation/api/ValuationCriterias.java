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
			List<ValuationCriteria> list = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(list, results.count())));
		} else if(form.list) {
			MongoDBResult<ValuationCriteria> results = mongoDBFinder(form, q, keys);
			List<ValuationCriteria> criterias = results.toList();
			return ok(Json.toJson(criterias));
		} else {
			MongoDBResult<ValuationCriteria> results = mongoDBFinder(form, q, keys);
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
		ValuationCriteriasUrlParamForm valuationCriteriasUrlParamForm = filledFormQueryString(this.app.formFactory()
				.form(ValuationCriteriasUrlParamForm.class), 
				ValuationCriteriasUrlParamForm.class).get();

		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);

		boolean copy = false;
		if (valuationCriteriasUrlParamForm != null && valuationCriteriasUrlParamForm.copy != null && valuationCriteriasUrlParamForm.copy) {
			copy = true;
		}
		if (objectInput._id != null) {
			return badRequest("use PUT method to update the run");
		}	


		if (StringUtils.isNotBlank(objectInput.code)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			if (! valuationCriteriasUrlParamForm.copy) {
				return badRequest("valuation criteria avec code='" + objectInput.code + "' qui peut etre renseigné uniquement en mode copy"); 
			}
		} else { // generer le code uniquement si on n'est pas en mode copy
			if (! valuationCriteriasUrlParamForm.copy) {
				objectInput.code = generateCode();
				while (MongoDBDAO.checkObjectExist(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, DBQuery.and(DBQuery.is("code",objectInput.code)))) {
					objectInput.code = generateCode();
				}	
			}
		}
		if (!copy) { //generer traceInformation si on n'est pas en mode copy
			objectInput.traceInformation = new TraceInformation();
			objectInput.traceInformation.creationStamp(ctxVal, getCurrentUser());
		}
		
		// test de l'unicité du code ou du name dans methode validate de l'object vc
		objectInput.validate(ctxVal);
		logger.debug("Dans controllers.valuation.api.update, dans objectInput, traceInformation. =" + objectInput.traceInformation.createUser);
		logger.info("Dans controllers.valuation.api.update, dans objectInput, active =" + objectInput.active);
		if (!ctxVal.hasErrors()) {
			ValuationCriteria objectFromDb = MongoDBDAO.save(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, objectInput);
			logger.info("Dans controllers.valuation.api.update, dans objectFromDB apres update, active =" + objectFromDb.active);
			logger.info("Dans controllers.valuation.api.update, dans objectFromDB, traceInformation. =" + objectFromDb.traceInformation.createUser);
			return ok(Json.toJson(objectFromDb));
		} else {
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}

	}

	public Result update(String code) {
		ValuationCriteria objectFromDB = getByCode(code);
		if (objectFromDB == null) {
			return badRequest("ValuationCriteria avec code " + code + " qui n'existe pas dans la base");
		}
		Form<ValuationCriteria> filledForm = getFilledForm(valuationCriteriaForm, ValuationCriteria.class);
		ValuationCriteria objectInput = filledForm.get();
		ValuationCriteriasUrlParamForm valuationCriteriasUrlParamForm = filledFormQueryString(this.app.formFactory()
				.form(ValuationCriteriasUrlParamForm.class), 
				ValuationCriteriasUrlParamForm.class).get();
		
		boolean copy = false;
		
		if (valuationCriteriasUrlParamForm != null && valuationCriteriasUrlParamForm.copy != null && valuationCriteriasUrlParamForm.copy) {
			copy = true;
		}
		logger.info("Dans controllers.valuation.api.update, copy=" + copy);
		
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		ctxVal.putObject("copy", copy);
		
		if ( ! objectInput.code.equals(code) ) {
			return badRequest("valuationCriteria avec code '" + objectInput.code + "' qui n'est pas celui indiqué dans l'url " + code);
		}	


		// En mode copy, l'id de l'objet qui provient d'une autre collection a ete mis à null, ou bien ne correspond pas à l'id de l'objet dans la collection visée :			
		if ( copy) {
			objectInput._id = objectFromDB._id;
		} else {
			if (objectInput._id == null) {
				return badRequest("valuation criteria '" + objectInput.code + "' sans id");
			}
			if ( ! objectInput._id.equals(objectFromDB._id)) {
				return badRequest("valuation criteria '" + objectInput.code + "' avec id " + objectInput._id + " different de l'id en base " + objectFromDB._id);
			}
		}

		// Si on n'estpas en mode copy, alors modifyUser et modifyDate imposés :
		if ( ! copy ) {
			objectInput.traceInformation.forceModificationStamp(getCurrentUser());
		}
		
		objectInput.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}
		logger.debug("Dans controllers.valuation.api.update, dans objectInput, traceInformation. =" + objectInput.traceInformation.createUser);
		logger.info("Dans controllers.valuation.api.update, dans objectInput, active =" + objectInput.active);
		MongoDBDAO.update(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, objectInput);
		objectFromDB = getByCode(code);
		logger.info("Dans controllers.valuation.api.update, dans objectFromDB apres update, active =" + objectFromDB.active);
		logger.info("Dans controllers.valuation.api.update, dans objectFromDB, traceInformation. =" + objectFromDB.traceInformation.createUser);

		return ok(Json.toJson(objectInput));			
	}

	
	@Override
	public Result delete(String code) {
		ValuationCriteria objectFromDB =  getByCode(code);
		if(objectFromDB == null) {
			return badRequest("ValuationCriteria with code " + code + " does not exist");
		}
		MongoDBDAO.deleteByCode(InstanceConstants.VALUATION_CRITERIA_COLL_NAME,  ValuationCriteria.class, objectFromDB.code);
		return ok();
	}
	
	private static String generateCode(){
		return ("VC-"+(new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date())).toUpperCase();		
	}
	
	private static ValuationCriteria getByCode(String code) {
		ValuationCriteria valuationCriteria = MongoDBDAO.findByCode(InstanceConstants.VALUATION_CRITERIA_COLL_NAME, ValuationCriteria.class, code);
    	return valuationCriteria;
	}
	
}
