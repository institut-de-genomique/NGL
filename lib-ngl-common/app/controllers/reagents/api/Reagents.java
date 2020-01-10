package controllers.reagents.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.instance.Box;
import models.laboratory.reagent.instance.Reagent;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.ListObject;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Reagents extends DocumentController<Reagent> {
	
	private final Form<ReagentSearchForm> reagentSearchForm;

//	@Inject
//	public Reagents(NGLContext ctx) {
//		super(ctx,InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Reagent.class);
//		reagentSearchForm = ctx.form(ReagentSearchForm.class);
//	}

	@Inject
	public Reagents(NGLApplication app) {
		super(app,InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Reagent.class);
		reagentSearchForm = app.form(ReagentSearchForm.class);
	}

	@Override
	public Result get(String code){
		Reagent reagent = getObject(code);
		if (reagent != null)
			return ok(Json.toJson(reagent));
		return badRequest();
	}

	@Override
	public Result delete(String code){
		MongoDBDAO.delete(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, AbstractCatalog.class, DBQuery.or(DBQuery.is("code", code),DBQuery.is("reagentCode", code)));
		return ok();
	}

	public Result save(){
		Form<Reagent> reagentFilledForm = getMainFilledForm();
		Reagent reagent = reagentFilledForm.get();
		reagent.code = ReagentCodeHelper.getInstance().generateReagentCode();
		
		reagent.traceInformation = new TraceInformation();
		reagent.traceInformation.createUser =  getCurrentUser();
		reagent.traceInformation.creationDate = new Date();
		
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), reagentFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), reagentFilledForm);
//		contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(getCurrentUser(), reagentFilledForm);
		/*if(ValidationHelper.required(contextValidation, reagent.name, "name")){
			reagentCatalog.code = CodeHelper.getInstance().generateReagentCatalogCode(reagentCatalog.name);
		}*/
//		reagent = (Reagent)InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, reagent, contextValidation);
		reagent = InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, reagent, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(reagent));
		 // legit, spaghetti above
	}

	public Result update(String code) {
		Form<Reagent> reagentFilledForm = getMainFilledForm();
		Reagent reagent = reagentFilledForm.get();

		reagent.traceInformation.modifyUser =  getCurrentUser();
		reagent.traceInformation.modifyDate = new Date();
		
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), reagentFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), reagentFilledForm);
//		contextValidation.setUpdateMode();
		ContextValidation contextValidation = ContextValidation.createUpdateContext(getCurrentUser(), reagentFilledForm);
//		reagent = (Reagent)InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, reagent, contextValidation);
		reagent = InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, reagent, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(reagent));
		 // legit, spaghetti above
	}

	public Result list() {
		Form<ReagentSearchForm> reagentFilledForm = filledFormQueryString(reagentSearchForm,ReagentSearchForm.class);
		ReagentSearchForm reagentSearch = reagentFilledForm.get();
		BasicDBObject keys = getKeys(reagentSearch);
		DBQuery.Query query = getQuery(reagentSearch);

		if (reagentSearch.datatable) {
			MongoDBResult<Reagent> results =  mongoDBFinder(reagentSearch, query);
			List<Reagent> reagents = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(reagents, results.count())));
		} else if (reagentSearch.list) {
			keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("category", 1);
			if (reagentSearch.orderBy    == null) reagentSearch.orderBy    = "code";
			if (reagentSearch.orderSense == null) reagentSearch.orderSense = 0;				
			MongoDBResult<Reagent> results = mongoDBFinder(reagentSearch, query, keys);
			List<Reagent> reagents = results.toList();
			List<ListObject> los = new ArrayList<>();
			for (Reagent p: reagents)					
				los.add(new ListObject(p.code, p.code));								
			return Results.ok(Json.toJson(los));
		} else {
			if (reagentSearch.orderBy    == null) reagentSearch.orderBy    = "code";
			if (reagentSearch.orderSense == null) reagentSearch.orderSense = 0;
			MongoDBResult<Reagent> results = mongoDBFinder(reagentSearch, query);
			List<Reagent> reagents = results.toList();
			return ok(Json.toJson(reagents));
		}
	}

	private static Query getQuery(ReagentSearchForm reagentSearch){
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query = null;
		queryElts.add(DBQuery.is("category", "Reagent"));

		if(StringUtils.isNotBlank(reagentSearch.boxCode)){
			queryElts.add(DBQuery.is("boxCode", reagentSearch.boxCode));
		}
		
		if(StringUtils.isNotBlank(reagentSearch.kitCode)){
			queryElts.add(DBQuery.is("kitCode", reagentSearch.kitCode));
		}
		
		if(StringUtils.isNotEmpty(reagentSearch.providerID) && StringUtils.isNotEmpty(reagentSearch.boxBarCode)){
			BoxSearchForm boxSearch = new BoxSearchForm();
			boxSearch.providerID = reagentSearch.boxBarCode;
			boxSearch.lotNumber = reagentSearch.boxBarCode;
			BasicDBObject keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("category", 1);
			List<Box> boxes = MongoDBDAO.find(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Box.class, Boxes.getQuery(boxSearch), keys).toList();
			List<String> boxCodes = new ArrayList<>();
			for(Box b:boxes){
				boxCodes.add(b.code);
			}
			queryElts.add(DBQuery.or(DBQuery.regex("barCode", Pattern.compile(reagentSearch.providerID)),DBQuery.in("boxCode", boxCodes)));
		}else if(StringUtils.isNotEmpty(reagentSearch.boxBarCode)){
			queryElts.add(DBQuery.is("boxBarCode", reagentSearch.boxBarCode));
		} 
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}

		return query;
	}
}
