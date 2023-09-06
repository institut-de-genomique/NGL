package controllers.reagents.api;

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
import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import validation.utils.ValidationHelper;
import views.components.datatable.DatatableResponse;

public class BoxCatalogs extends DocumentController<BoxCatalog> {
	
	private final Form<BoxCatalogSearchForm> boxCatalogSearchForm;
	
//	@Inject
//	public BoxCatalogs(NGLContext ctx) {
//		super(ctx,InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class);
//		boxCatalogSearchForm = ctx.form(BoxCatalogSearchForm.class);
//	}
	
	private final ReceptionsAPI receptionApi;
	
	private final ExperimentReagentsAPI experimentReagentsApi;
	
	private final ReagentCatalogs reagentCatalogs;
	
	@Inject
	public BoxCatalogs(NGLApplication app, ReceptionsAPI receptionApi, ExperimentReagentsAPI experimentReagentsApi, ReagentCatalogs reagentCatalogs) {
		super(app,InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class);
		boxCatalogSearchForm = app.form(BoxCatalogSearchForm.class);
		this.receptionApi = receptionApi;
		this.experimentReagentsApi = experimentReagentsApi;
		this.reagentCatalogs = reagentCatalogs;
	}

	public Result save() {
		Form<BoxCatalog> boxCatalogFilledForm = getMainFilledForm();
		BoxCatalog boxCatalog = boxCatalogFilledForm.get();
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm.errors());
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser(), boxCatalogFilledForm);
		boxCatalog.setTraceCreationStamp(contextValidation, getCurrentUser());
		// GA: change not update autorized here !!!!
		if (ValidationHelper.validateNotEmpty(contextValidation, boxCatalog.name, "name")) {
			if (boxCatalog._id == null) {
				boxCatalog.code = ReagentCodeHelper.getInstance().generateBoxCatalogCode(boxCatalog.kitCatalogCode);
				contextValidation.setCreationMode();
			} else {
				contextValidation.setUpdateMode();
			}
//			boxCatalog = (BoxCatalog)InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
			boxCatalog = InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
		}
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(boxCatalog));		
	}
	
	public Result update(String code){
		Form<BoxCatalog> boxCatalogFilledForm = getMainFilledForm();
		BoxCatalog boxCatalog = boxCatalogFilledForm.get();
		
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm);
//		contextValidation.setUpdateMode();
		ContextValidation contextValidation = ContextValidation.createUpdateContext(getCurrentUser(), boxCatalogFilledForm);
		boxCatalog.setTraceModificationStamp(contextValidation, getCurrentUser());
		
		BoxCatalog boxCatalogInBase = getObject(code);
		if(!boxCatalogInBase.catalogRefCode.equals(boxCatalog.catalogRefCode) && receptionApi.isAnyCatalogReferenceInReceptions(boxCatalogInBase.catalogRefCode)) {
			contextValidation.addError("catalogRefCode", "error.reagent.reception.modify.kit.catalog");
		}
		
		if (contextValidation.hasErrors()) {
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		
//		boxCatalog = (BoxCatalog)InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
		boxCatalog = InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(boxCatalog));
	}
	
	private void checkIfReagentCatalogsAreUsed(String boxCatalogCode, ContextValidation contextValidation) {
		Query findKitCatalogReagents = DBQuery.and(DBQuery.is("boxCatalogCode", boxCatalogCode), DBQuery.is("category", "Reagent"));
		MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, findKitCatalogReagents)
		.getCursor()
		.forEach((ReagentCatalog reagentCatalog) -> reagentCatalogs.checkIfCatalogIsUsed(reagentCatalog.code, contextValidation));
	}
	
	protected void checkIfCatalogIsUsed(String code, ContextValidation contextValidation) {
		BoxCatalog boxCatalogInBase = getObject(code);
		if(experimentReagentsApi.isAnyReferenceInReagentUseds(code)) contextValidation.addError("experiments", "error.delete.used.catalog", "Box", boxCatalogInBase.name);
		String catalogRefCode = boxCatalogInBase.catalogRefCode;
		if(receptionApi.isAnyCatalogReferenceInReceptions(catalogRefCode)) contextValidation.addError("receptions", "error.delete.reception.catalog", "Box", boxCatalogInBase.name);
		checkIfReagentCatalogsAreUsed(code, contextValidation);
	}
	
	@Override
	public Result delete(String code){
		ContextValidation contextValidation = ContextValidation.createDeleteContext(getCurrentUser());
		checkIfCatalogIsUsed(code, contextValidation);
		if(contextValidation.hasErrors()) return badRequest(errorsAsJson(contextValidation.getErrors()));
		MongoDBDAO.delete(InstanceConstants.REAGENT_CATALOG_COLL_NAME, AbstractCatalog.class, DBQuery.or(DBQuery.is("code", code),DBQuery.is("boxCatalogCode", code)));
		return ok();
	}
		
	public Result list() {
		Form<BoxCatalogSearchForm> boxCatalogFilledForm = filledFormQueryString(boxCatalogSearchForm,BoxCatalogSearchForm.class);
		BoxCatalogSearchForm boxCatalogSearch = boxCatalogFilledForm.get();
//		BasicDBObject keys = 
				getKeys(boxCatalogSearch);
		DBQuery.Query query = getQuery(boxCatalogSearch);
		Logger.debug("query : "+query);

		if (boxCatalogSearch.datatable) {
			MongoDBResult<BoxCatalog> results =  mongoDBFinder(boxCatalogSearch, query);
			List<BoxCatalog> boxCatalogs = results.toList();
			
			return ok(Json.toJson(new DatatableResponse<>(boxCatalogs, results.count())));
		/*}else if (boxCatalogSearch.list){
			keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("name", 1);
			keys.put("category", 1);
			keys.put("kitCatalogCode",1);
			
			if(null == boxCatalogSearch.orderBy)boxCatalogSearch.orderBy = "code";
			if(null == boxCatalogSearch.orderSense)boxCatalogSearch.orderSense = 0;				
			
			MongoDBResult<BoxCatalog> results = mongoDBFinder(boxCatalogSearch, query, keys);
			List<BoxCatalog> boxCatalogs = results.toList();
			List<ListObject> los = new ArrayList<ListObject>();
			for(BoxCatalog p: boxCatalogs){					
					los.add(new ListObject(p.code, p.name));	// Pour une recherche avec le code du kitCatalogCode...							
			}
			
			return Results.ok(Json.toJson(los));*/
		}
		if (boxCatalogSearch.orderBy    == null) boxCatalogSearch.orderBy = "code";
		if (boxCatalogSearch.orderSense == null) boxCatalogSearch.orderSense = 0;

		MongoDBResult<BoxCatalog> results = mongoDBFinder(boxCatalogSearch, query);
		List<BoxCatalog> boxCatalogs = results.toList();

		return ok(Json.toJson(boxCatalogs));
	}
	
	private static Query getQuery(BoxCatalogSearchForm boxCatalogSearch){
		//List<Query> queries = new ArrayList<Query>();
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query = null;
		queryElts.add(DBQuery.is("category", "Box"));
		
		if(StringUtils.isNotEmpty(boxCatalogSearch.kitCatalogCode)){
			Logger.debug("kitCatalogCode : "+boxCatalogSearch.kitCatalogCode);
			queryElts.add(DBQuery.is("kitCatalogCode", boxCatalogSearch.kitCatalogCode));			
		} else if(CollectionUtils.isNotEmpty(boxCatalogSearch.kitCatalogCodes)){
			queryElts.add(DBQuery.in("kitCatalogCode", boxCatalogSearch.kitCatalogCodes));
		}
		
		if(StringUtils.isNotEmpty(boxCatalogSearch.catalogRefCode)){
			queryElts.add(DBQuery.is("catalogRefCode", boxCatalogSearch.catalogRefCode));
		}
		
		if (null != boxCatalogSearch.isActive) {
			queryElts.add(DBQuery.is("active", boxCatalogSearch.isActive));
			Logger.debug("box active: " + boxCatalogSearch.catalogRefCode + " - " + boxCatalogSearch.isActive);
		}
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}

		return query;
	}
}
