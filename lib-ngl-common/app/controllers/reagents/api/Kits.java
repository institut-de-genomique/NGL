package controllers.reagents.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
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
import models.laboratory.reagent.instance.Kit;
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

public class Kits extends DocumentController<Kit> {

	private final Form<KitSearchForm> kitSearchForm;

//	@Inject
//	public Kits(NGLContext ctx) {
//		super(ctx,InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Kit.class);
//		kitSearchForm = ctx.form(KitSearchForm.class);
//	}

	@Inject
	public Kits(NGLApplication app) {
		super(app, InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Kit.class);
		kitSearchForm = app.form(KitSearchForm.class);
	}

	@Override
	public Result get(String code) {
		Kit kit = getObject(code);
		if (kit != null)
			return ok(Json.toJson(kit));
		return badRequest();
	}

	@Override
	public Result delete(String code) {
		MongoDBDAO.delete(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, AbstractCatalog.class, DBQuery.or(DBQuery.is("code", code),DBQuery.is("kitCode", code)));
		return ok();
	}

	public Result save() {
		Form<Kit> kitFilledForm = getMainFilledForm();

		Kit kit = kitFilledForm.get();
		kit.code = ReagentCodeHelper.getInstance().generateKitCode();
		kit.traceInformation = new TraceInformation();
		kit.traceInformation.createUser =  getCurrentUser();
		kit.traceInformation.creationDate = new Date();

//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitFilledForm);
//		contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(getCurrentUser(), kitFilledForm);
		/*if(ValidationHelper.required(contextValidation, kit.name, "name")){
				kitCatalog.code = CodeHelper.getInstance().generateKitCatalogCode(kitCatalog.name);
			}*/
//		kit = (Kit)InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, kit, contextValidation);
		kit = InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, kit, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(kit));
		// legit, spaghetti above
	}

	public Result update(String code){
		Form<Kit> kitFilledForm = getMainFilledForm();
		
		Kit kit = kitFilledForm.get();

		kit.traceInformation.modifyUser =  getCurrentUser();
		kit.traceInformation.modifyDate = new Date();
		
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitFilledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), kitFilledForm);
//		contextValidation.setUpdateMode();
		ContextValidation contextValidation = ContextValidation.createUpdateContext(getCurrentUser(), kitFilledForm);

//		kit = (Kit)InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, kit, contextValidation);
		kit = InstanceHelpers.save(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, kit, contextValidation);
		if (contextValidation.hasErrors())
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		return ok(Json.toJson(kit));
		// legit, spaghetti above
	}

	public Result list() {
		Form<KitSearchForm> kitFilledForm = filledFormQueryString(kitSearchForm,KitSearchForm.class);
		KitSearchForm kitSearch = kitFilledForm.get();
		BasicDBObject keys = getKeys(kitSearch);
		DBQuery.Query query = getQuery(kitSearch);

		if (kitSearch.datatable) {
			MongoDBResult<Kit> results =  mongoDBFinder(kitSearch, query);
			List<Kit> kits = results.toList();

			return ok(Json.toJson(new DatatableResponse<>(kits, results.count())));
		} else if (kitSearch.list) {
			keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("category", 1);

			if (kitSearch.orderBy == null)    kitSearch.orderBy    = "code";
			if (kitSearch.orderSense == null) kitSearch.orderSense = 0;				

			MongoDBResult<Kit> results = mongoDBFinder(kitSearch, query, keys);
			List<Kit> kits = results.toList();
			List<ListObject> los = new ArrayList<>();
			for(Kit p: kits){					
				los.add(new ListObject(p.code, p.code));								
			}

			return Results.ok(Json.toJson(los));
		} else {
			if (kitSearch.orderBy == null)    kitSearch.orderBy    = "code";
			if (kitSearch.orderSense == null) kitSearch.orderSense = 0;

			MongoDBResult<Kit> results = mongoDBFinder(kitSearch, query);
			List<Kit> kits = results.toList();

			return ok(Json.toJson(kits));
		}
	}

	private static Query getQuery(KitSearchForm kitSearch){
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query = null;
		queryElts.add(DBQuery.is("category", "Kit"));

		if(StringUtils.isNotBlank(kitSearch.code)){
			queryElts.add(DBQuery.is("code", kitSearch.code));
		}
		
		if(CollectionUtils.isNotEmpty(kitSearch.catalogCodes)){
			queryElts.add(DBQuery.in("catalogCode", kitSearch.catalogCodes));
		}
		
		if(StringUtils.isNotBlank(kitSearch.catalogCode)){
			queryElts.add(DBQuery.is("catalogCode", kitSearch.catalogCode));
		}
		
		if(StringUtils.isNotBlank(kitSearch.catalogRefCode)){
			queryElts.add(DBQuery.is("catalogRefCode", kitSearch.catalogRefCode));
		}
		
		if(StringUtils.isNotBlank(kitSearch.createUser)){
			queryElts.add(DBQuery.is("traceInformation.createUser", kitSearch.createUser));
		}
		
		if(StringUtils.isNotBlank(kitSearch.providerOrderCode)){
			queryElts.add(DBQuery.is("orderInformations.providerOrderCode", kitSearch.providerOrderCode));
		}
		
		if(kitSearch.startToUseDate != null){
			queryElts.add(DBQuery.greaterThanEquals("startToUseDate", kitSearch.startToUseDate));
		}
		
		if(kitSearch.stopToUseDate != null){
			queryElts.add(DBQuery.lessThanEquals("stopToUseDate", (DateUtils.addDays(kitSearch.stopToUseDate, 1))));
		}
		
		if(kitSearch.fromReceptionDate != null){
			queryElts.add(DBQuery.greaterThanEquals("receptionDate", kitSearch.fromReceptionDate));
		}
		
		if(kitSearch.toReceptionDate != null){
			queryElts.add(DBQuery.lessThanEquals("receptionDate", (DateUtils.addDays(kitSearch.toReceptionDate, 1))));
		}
		
		if(StringUtils.isNotBlank(kitSearch.barCode)){
			queryElts.add(DBQuery.regex("barCode", Pattern.compile(kitSearch.barCode+"_|_"+kitSearch.barCode)));
		}
		
		if(StringUtils.isNotBlank(kitSearch.stateCode)){
			queryElts.add(DBQuery.is("state.code", kitSearch.stateCode));
		}
		
		if(StringUtils.isNotBlank(kitSearch.orderCode)){
			queryElts.add(DBQuery.is("orderInformations.orderCode", kitSearch.orderCode));
		}
		
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}

		return query;
	}
}
