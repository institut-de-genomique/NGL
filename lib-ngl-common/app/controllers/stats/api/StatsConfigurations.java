package controllers.stats.api;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import controllers.authorisation.Permission;
import controllers.reporting.api.ConfigurationsSearchForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.stats.StatsConfiguration;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class StatsConfigurations extends DocumentController<StatsConfiguration> {
	
	private final Form<StatsConfiguration>       reportConfigForm;
	private final Form<ConfigurationsSearchForm> searchForm;
	
//	@Inject
//	public StatsConfigurations(NGLContext ctx) {
//		super(ctx, InstanceConstants.STATS_CONFIG_COLL_NAME, StatsConfiguration.class);
//		reportConfigForm = ctx.form(StatsConfiguration.class);
//		searchForm = ctx.form(ConfigurationsSearchForm.class);
//	}
	
	@Inject
	public StatsConfigurations(NGLApplication app) {
		super(app, InstanceConstants.STATS_CONFIG_COLL_NAME, StatsConfiguration.class);
		reportConfigForm = app.form(StatsConfiguration.class);
		searchForm = app.form(ConfigurationsSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result list() {
		Form<ConfigurationsSearchForm> filledForm = filledFormQueryString(searchForm, ConfigurationsSearchForm.class);
		ConfigurationsSearchForm form = filledForm.get();
		
		Query q = getQuery(form);
		BasicDBObject keys = getKeys(form);
		if (form.datatable) {			
			MongoDBResult<StatsConfiguration> results = mongoDBFinder(form, q, keys).sort("name");
//			MongoDBResult<StatsConfiguration> results = mongoDBFinder(InstanceConstants.STATS_CONFIG_COLL_NAME, form, StatsConfiguration.class, q, keys).sort("name");
			List<StatsConfiguration> statsConfigurations = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(statsConfigurations, results.count())));
		} else if(form.count) {
			MongoDBResult<StatsConfiguration> results = mongoDBFinder(form, q, keys).sort("name");
//			MongoDBResult<StatsConfiguration> results = mongoDBFinder(InstanceConstants.STATS_CONFIG_COLL_NAME, form, StatsConfiguration.class, q, keys).sort("name");
			int count = results.count();
			Map<String, Integer> m = new HashMap<>(1);
			m.put("result", count);
			return ok(Json.toJson(m));
		} else {
			MongoDBResult<StatsConfiguration> results = mongoDBFinder(form, q, keys).sort("name");
//			MongoDBResult<StatsConfiguration> results = mongoDBFinder(InstanceConstants.STATS_CONFIG_COLL_NAME, form, StatsConfiguration.class, q, keys).sort("name");
			List<StatsConfiguration> statsConfigurations = results.toList();
			return ok(Json.toJson(statsConfigurations));
		}
	}
	
	private static Query getQuery(ConfigurationsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (CollectionUtils.isNotEmpty(form.pageCodes)) { //all
			queries.add(DBQuery.in("pageCodes", form.pageCodes));
		}
		if (queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

	@Override
	@Permission(value={"reading"})
	public /*static*/ Result get(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);		
		if (statsConfiguration != null)
			return ok(Json.toJson(statsConfiguration));	
		return notFound();
	}
	
	@Permission(value={"writing"})
	public /*static*/ Result save() {
		Form<StatsConfiguration> filledForm = getFilledForm(reportConfigForm, StatsConfiguration.class);
		StatsConfiguration statsConfiguration = filledForm.get();
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		if (statsConfiguration._id == null) {
//			statsConfiguration.traceInformation = new TraceInformation();
//			statsConfiguration.traceInformation.setTraceInformation(getCurrentUser());
			statsConfiguration.setTraceCreationStamp(ctxVal, getCurrentUser());
			statsConfiguration.code = generateStatsConfigurationCode();
		} else {
			return badRequest("use PUT method to update the run");
		}
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
		statsConfiguration.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			statsConfiguration = MongoDBDAO.save(InstanceConstants.STATS_CONFIG_COLL_NAME, statsConfiguration);
			return ok(Json.toJson(statsConfiguration));
		} else {
			// return badRequest(filledForm.errors-AsJson());
//			return badRequest(NGLContext._errorsAsJson(ctxVal.getErrors()));
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	@Permission(value={"writing"})
	public /*static*/ Result update(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);
		if (statsConfiguration == null)
			return badRequest("StatsConfiguration with code "+code+" does not exist"); // TODO: probably a not found
		Form<StatsConfiguration> filledForm = getFilledForm(reportConfigForm, StatsConfiguration.class);
		StatsConfiguration statsConfigurationInput = filledForm.get();

		if (statsConfigurationInput.code.equals(code)) {
//			if(null != statsConfigurationInput.traceInformation){
//				statsConfigurationInput.traceInformation.setTraceInformation(getCurrentUser());
//			}else{
//				Logger.error("traceInformation is null !!");
//			}
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
//			ctxVal.setUpdateMode();
			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			statsConfigurationInput.setTraceUpdateStamp(ctxVal, getCurrentUser());
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
			statsConfigurationInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.STATS_CONFIG_COLL_NAME, statsConfigurationInput);
				return ok(Json.toJson(statsConfigurationInput));
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
	@Permission(value={"writing"})
	public /*static*/ Result delete(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);
		if (statsConfiguration == null)
			return badRequest("StatsConfiguration with code "+statsConfiguration+" does not exist"); // TODO: probably a not found
		MongoDBDAO.deleteByCode(InstanceConstants.STATS_CONFIG_COLL_NAME,  StatsConfiguration.class, statsConfiguration.code);
		return ok();
	}
	
	public static String generateStatsConfigurationCode(){
		return ("RC-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).toUpperCase();		
	}
	
	private static StatsConfiguration getStatsConfiguration(String code) {
		StatsConfiguration statsConfiguration = MongoDBDAO.findByCode(InstanceConstants.STATS_CONFIG_COLL_NAME, StatsConfiguration.class, code);
    	return statsConfiguration;
	}
	
}
