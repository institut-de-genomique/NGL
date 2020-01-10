package controllers.reporting.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.reporting.instance.FilteringConfiguration;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;

/**
 * Controller around ResolutionConfigurations object
 *
 */
public class FilteringConfigurations extends DocumentController<FilteringConfiguration> {
	
	private final Form<ConfigurationsSearchForm> searchForm; 
	
	@Inject
	public FilteringConfigurations(NGLApplication ctx) {
		super(ctx,InstanceConstants.FILTERING_CONFIG_COLL_NAME, FilteringConfiguration.class);
		searchForm = ctx.form(ConfigurationsSearchForm.class);
	}

	public Result list() {
		Form<ConfigurationsSearchForm> filledForm = filledFormQueryString(searchForm, ConfigurationsSearchForm.class);
		ConfigurationsSearchForm form = filledForm.get();
		Query q = getQuery(form);
		
		MongoDBResult<FilteringConfiguration> results = mongoDBFinder(form, q);			
		List<FilteringConfiguration> configurations = results.toList();			
		return ok(Json.toJson(configurations));		
	}
	
	private Query getQuery(ConfigurationsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (CollectionUtils.isNotEmpty(form.pageCodes)) { //all
			queries.add(DBQuery.in("pageCodes", form.pageCodes));
		}
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		
		return query;
	}
	
	public Result save() {
		Form<FilteringConfiguration> filledForm = getMainFilledForm();
		FilteringConfiguration configuration = filledForm.get();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		if (configuration._id == null) {
			configuration.setTraceCreationStamp(ctxVal, getCurrentUser());
			configuration.code = generateConfigurationCode();
		} else {
			return badRequest("use PUT method to update the filtering config");
		}
		configuration.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			configuration = saveObject(configuration);
			return ok(Json.toJson(configuration));
		} else {
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	public Result update(String code) {
		FilteringConfiguration configuration = getObject(code);
		if (configuration == null)
			return badRequest("FilteringConfiguration with code " + code + " does not exist");
		Form<FilteringConfiguration> filledForm = getMainFilledForm();
		FilteringConfiguration configurationInput = filledForm.get();

		if (configurationInput.code.equals(code)) {
			ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
			configurationInput.setTraceModificationStamp(ctxVal,getCurrentUser());
			configurationInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				updateObject(configurationInput);
				return ok(Json.toJson(configurationInput));
			} else {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			return badRequest("FilteringConfiguration code are not the same");
		}				
	}
	
	public static String generateConfigurationCode() {
		return ("FC-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).toUpperCase();		
	}
	
}
