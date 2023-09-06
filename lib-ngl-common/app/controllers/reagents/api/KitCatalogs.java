package controllers.reagents.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import validation.utils.ValidationHelper;
import views.components.datatable.DatatableResponse;

public class KitCatalogs extends DocumentController<KitCatalog> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(KitCatalogs.class);
	
	private final Form<KitCatalogSearchForm> kitCatalogSearchForm;
	
	private final ReceptionsAPI receptionApi;
	
	private final ExperimentReagentsAPI experimentReagentsApi;
	
	private final BoxCatalogs boxCatalogs;
	
//	@Inject
//	public KitCatalogs(NGLContext ctx) {
//		super(ctx,InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class);
//		kitCatalogSearchForm = ctx.form(KitCatalogSearchForm.class);
//	}
	
	@Inject
	public KitCatalogs(NGLApplication app, ReceptionsAPI receptionApi, ExperimentReagentsAPI experimentReagentsApi, BoxCatalogs boxCatalogs, ReagentCatalogs reagentCatalogs) {
		super(app,InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class);
		kitCatalogSearchForm = app.form(KitCatalogSearchForm.class);
		this.receptionApi = receptionApi;
		this.experimentReagentsApi = experimentReagentsApi;
		this.boxCatalogs = boxCatalogs;
	}

	@Override
	public Result get(String code){
		KitCatalog kitCatalog = getObject(code);
		if (kitCatalog != null)
			return ok(Json.toJson(kitCatalog));
		return badRequest();
	}
	
	private void checkIfBoxCatalogsAreUsed(String kitCatalogCode, ContextValidation contextValidation) {
		Query findKitCatalogBoxes = DBQuery.and(DBQuery.is("kitCatalogCode", kitCatalogCode), DBQuery.is("category", "Box"));
		MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, findKitCatalogBoxes)
		.getCursor()
		.forEach((BoxCatalog boxCatalog) -> boxCatalogs.checkIfCatalogIsUsed(boxCatalog.code, contextValidation));
	}
	
	private void checkIfKitCatalogIsUsed(String code, ContextValidation contextValidation) {
		KitCatalog kitCatalogInBase = getObject(code);
		if(experimentReagentsApi.isAnyReferenceInReagentUseds(code)) contextValidation.addError("experiments", "error.delete.used.catalog", "Kit", kitCatalogInBase.name);
		if(receptionApi.isAnyKitCatalogReferenceInReceptions(kitCatalogInBase.name)) contextValidation.addError("receptions", "error.delete.reception.catalog", "Kit", kitCatalogInBase.name);
		checkIfBoxCatalogsAreUsed(code, contextValidation);
	}
	
	@Override
	public Result delete(String code){
		ContextValidation contextValidation = ContextValidation.createDeleteContext(getCurrentUser());
		checkIfKitCatalogIsUsed(code, contextValidation);
		if(contextValidation.hasErrors()) return badRequest(errorsAsJson(contextValidation.getErrors()));
		MongoDBDAO.delete(InstanceConstants.REAGENT_CATALOG_COLL_NAME, AbstractCatalog.class, DBQuery.or(DBQuery.is("code", code),DBQuery.is("kitCatalogCode", code)));
		return ok();
	}
	
	public Result save(){
		Form<KitCatalog> kitCatalogFilledForm = getMainFilledForm();
		KitCatalog kitCatalog = kitCatalogFilledForm.get();
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitCatalogFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitCatalogFilledForm);
//		contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(getCurrentUser(), kitCatalogFilledForm);
		kitCatalog.setTraceCreationStamp(contextValidation, getCurrentUser());
		if (ValidationHelper.validateNotEmpty(contextValidation, kitCatalog.name, "name")) {
			
			if(receptionApi.isAnyKitCatalogReferenceInReceptions(kitCatalog.name)) {
				contextValidation.addError("name", "error.reagent.reception.create.kit");
			}
			
			if (contextValidation.hasErrors()) {
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			
			kitCatalog.code = ReagentCodeHelper.getInstance().generateKitCatalogCode();
//			kitCatalog = (KitCatalog)InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, kitCatalog, contextValidation);
			kitCatalog = InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, kitCatalog, contextValidation);
		}
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(kitCatalog));	
		 // legit, spaghetti above
	}
	
	public Result update(String code){
		Form<KitCatalog> kitCatalogFilledForm = getMainFilledForm();
		KitCatalog kitCatalog = kitCatalogFilledForm.get();
		
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitCatalogFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitCatalogFilledForm);
//		contextValidation.setUpdateMode();
		ContextValidation contextValidation = ContextValidation.createUpdateContext(getCurrentUser(), kitCatalogFilledForm);
		kitCatalog.setTraceModificationStamp(contextValidation, getCurrentUser());
		
		KitCatalog kitCatalogInBase = getObject(code);
		if(!kitCatalogInBase.name.equals(kitCatalog.name) && receptionApi.isAnyKitCatalogReferenceInReceptions(kitCatalogInBase.name)) {
			contextValidation.addError("name", "error.reagent.reception.modify.kit.catalog");
		}
		if(!kitCatalogInBase.providerCode.equals(kitCatalog.providerCode) && receptionApi.isAnyKitCatalogReferenceInReceptions(kitCatalogInBase.name)) {
			contextValidation.addError("providerCode", "error.reagent.reception.modify.kit.catalog");
		}
		
		if (contextValidation.hasErrors()) {
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		
//		kitCatalog = (KitCatalog)InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, kitCatalog, contextValidation);
		kitCatalog = InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, kitCatalog, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(kitCatalog));
		 // legit, spaghetti above
	}
	
	public Result list(){
		Form<KitCatalogSearchForm> kitCatalogFilledForm = filledFormQueryString(kitCatalogSearchForm,KitCatalogSearchForm.class);
		KitCatalogSearchForm kitCatalogSearch = kitCatalogFilledForm.get();
		BasicDBObject keys = getKeys(kitCatalogSearch);
		DBQuery.Query query = getQuery(kitCatalogSearch);
		logger.debug("key kits: " + keys);

		if(kitCatalogSearch.datatable){
			MongoDBResult<KitCatalog> results =  mongoDBFinder(kitCatalogSearch, query);
			List<KitCatalog> kitCatalogs = results.toList();
			
			return ok(Json.toJson(new DatatableResponse<>(kitCatalogs, results.count())));
			
			
	/*	}else if (kitCatalogSearch.list){
			keys = getKeys(kitCatalogSearch);
			keys.put("code", 1);
			keys.put("name", 1);
			keys.put("category", 1);
			
			if(null == kitCatalogSearch.orderBy)kitCatalogSearch.orderBy = "code";
			if(null == kitCatalogSearch.orderSense)kitCatalogSearch.orderSense = 0;				
			
			MongoDBResult<KitCatalog> results = mongoDBFinder(kitCatalogSearch, query, keys);
			List<KitCatalog> kitCatalogs = results.toList();
			List<ListObject> los = new ArrayList<ListObject>();
			for(KitCatalog p: kitCatalogs){					
					los.add(new ListObject(p.code, p.name));								
			}
			
			return Results.ok(Json.toJson(los)); */ 
		} else {
			if (kitCatalogSearch.orderBy    == null) kitCatalogSearch.orderBy     = "code";
			if (kitCatalogSearch.orderSense == null) kitCatalogSearch.orderSense = 0;
			
			MongoDBResult<KitCatalog> results = mongoDBFinder(kitCatalogSearch, query);
			List<KitCatalog> kitCatalogs = results.toList();
			
			return ok(Json.toJson(kitCatalogs));
		}
	}
	
	private static Query getQuery(KitCatalogSearchForm kitCatalogSearch){
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query = null;
		queryElts.add(DBQuery.is("category", "Kit"));
		if(StringUtils.isNotBlank(kitCatalogSearch.code)){
			queryElts.add(DBQuery.is("code", kitCatalogSearch.code));
		} 
		
		if(CollectionUtils.isNotEmpty(kitCatalogSearch.codes)) {
			logger.debug("Codes: "+kitCatalogSearch.codes);
			queryElts.add(DBQuery.in("code",kitCatalogSearch.codes));
		}
		
		if(StringUtils.isNotBlank(kitCatalogSearch.name)){
			queryElts.add(DBQuery.regex("name", Pattern.compile(kitCatalogSearch.name, Pattern.CASE_INSENSITIVE)));
		}
		
		if(StringUtils.isNotBlank(kitCatalogSearch.providerRefName)){
			queryElts.add(DBQuery.regex("providerRefName", Pattern.compile(kitCatalogSearch.providerRefName,Pattern.CASE_INSENSITIVE)));
		}
		
		if(StringUtils.isNotBlank(kitCatalogSearch.providerCode)){
			queryElts.add(DBQuery.regex("providerCode", Pattern.compile(kitCatalogSearch.providerCode,Pattern.CASE_INSENSITIVE)));
		}
		
		if(StringUtils.isNotBlank(kitCatalogSearch.catalogRefCode)){
			queryElts.add(DBQuery.is("catalogRefCode", kitCatalogSearch.catalogRefCode));
		}
		
		if (CollectionUtils.isNotEmpty(kitCatalogSearch.codesFromBoxCatalog)) {
			queryElts.add(DBQuery.in("code",kitCatalogSearch.codesFromBoxCatalog));
		}
		
		if (CollectionUtils.isNotEmpty(kitCatalogSearch.codesFromReagentCatalog)) {
			queryElts.add(DBQuery.in("code",kitCatalogSearch.codesFromReagentCatalog));
		}
		
		if(kitCatalogSearch.experimentTypeCodes != null){
			queryElts.add(DBQuery.in("experimentTypeCodes", kitCatalogSearch.experimentTypeCodes));
		}
	
		if (null != kitCatalogSearch.isActive) {
			queryElts.add(DBQuery.is("active", kitCatalogSearch.isActive));
		}
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}
		return query;
	}
	
}
