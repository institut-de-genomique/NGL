package controllers.reagents.api;

// import static play.data.Form.form;
//import static fr.cea.ig.play.IGGlobals.form;

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
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
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
	
	private final /*static*/ Form<BoxCatalogSearchForm> boxCatalogSearchForm; // = form(BoxCatalogSearchForm.class);
	
	@Inject
	public BoxCatalogs(NGLContext ctx) {
		super(ctx,InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class);
		boxCatalogSearchForm = ctx.form(BoxCatalogSearchForm.class);
	}
	
	public Result save() {
		Form<BoxCatalog> boxCatalogFilledForm = getMainFilledForm();
		BoxCatalog boxCatalog = boxCatalogFilledForm.get();
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm.errors());
		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm);
		//TODO change not update autorized here !!!!
		if (ValidationHelper.required(contextValidation, boxCatalog.name, "name")) {
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
		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), boxCatalogFilledForm);
		contextValidation.setUpdateMode();
		
//		boxCatalog = (BoxCatalog)InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
		boxCatalog = InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, boxCatalog, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(boxCatalog));
	}
	
	@Override
	public Result delete(String code){
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
