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
			List<StatsConfiguration> statsConfigurations = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(statsConfigurations, results.count())));
		} else if(form.count) {
			MongoDBResult<StatsConfiguration> results = mongoDBFinder(form, q, keys).sort("name");
			int count = results.count();
			Map<String, Integer> m = new HashMap<>(1);
			m.put("result", count);
			return ok(Json.toJson(m));
		} else {
			MongoDBResult<StatsConfiguration> results = mongoDBFinder(form, q, keys).sort("name");
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
	public Result get(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);		
		if (statsConfiguration != null)
			return ok(Json.toJson(statsConfiguration));	
		return notFound();
	}
	
	@Permission(value={"writing"})
	public Result save() {
		Form<StatsConfiguration> filledForm = getFilledForm(reportConfigForm, StatsConfiguration.class);
		StatsConfiguration statsConfiguration = filledForm.get();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		if (statsConfiguration._id == null) {
			statsConfiguration.setTraceCreationStamp(ctxVal, getCurrentUser());
			statsConfiguration.code = generateStatsConfigurationCode();
		} else {
			return badRequest("use PUT method to update the run");
		}
		statsConfiguration.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			statsConfiguration = MongoDBDAO.save(InstanceConstants.STATS_CONFIG_COLL_NAME, statsConfiguration);
			return ok(Json.toJson(statsConfiguration));
		} else {
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	@Permission(value={"writing"})
	public Result update(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);
		if (statsConfiguration == null)
			return badRequest("StatsConfiguration with code "+code+" does not exist");
		Form<StatsConfiguration> filledForm = getFilledForm(reportConfigForm, StatsConfiguration.class);
		StatsConfiguration statsConfigurationInput = filledForm.get();

		if (statsConfigurationInput.code.equals(code)) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			statsConfigurationInput.setTraceModificationStamp(ctxVal, getCurrentUser());
			statsConfigurationInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.STATS_CONFIG_COLL_NAME, statsConfigurationInput);
				return ok(Json.toJson(statsConfigurationInput));
			} else {
				return badRequest(app.errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			return badRequest("readset code are not the same");
		}				
	}
	
	@Override
	@Permission(value={"writing"})
	public Result delete(String code) {
		StatsConfiguration statsConfiguration =  getStatsConfiguration(code);
		if (statsConfiguration == null)
			return badRequest("StatsConfiguration with code "+statsConfiguration+" does not exist");
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
