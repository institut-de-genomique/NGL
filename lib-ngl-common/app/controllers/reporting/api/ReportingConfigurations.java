package controllers.reporting.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.reporting.instance.ReportingConfiguration;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class ReportingConfigurations extends DocumentController<ReportingConfiguration> {

//	private static final play.Logger.ALogger logger = play.Logger.of(ReportingConfigurations.class);
	
	private final Form<ReportingConfiguration>   reportConfigForm;
	private final Form<ConfigurationsSearchForm> searchForm;
	
	@Inject
	public ReportingConfigurations(NGLApplication ctx) {
		super(ctx, InstanceConstants.REPORTING_CONFIG_COLL_NAME, ReportingConfiguration.class);
		reportConfigForm = ctx.form(ReportingConfiguration.class);
		searchForm = ctx.form(ConfigurationsSearchForm.class);
	}

	public Result list() {
		Form<ConfigurationsSearchForm> filledForm = filledFormQueryString(searchForm, ConfigurationsSearchForm.class);
		ConfigurationsSearchForm form = filledForm.get();
		
		Query q = getQuery(form);
		BasicDBObject keys = getKeys(form);
		if (form.datatable) {			
			MongoDBResult<ReportingConfiguration> results = mongoDBFinder(form, q, keys);
			List<ReportingConfiguration> reportingConfigurations = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(reportingConfigurations, results.count())));
		} else {
			MongoDBResult<ReportingConfiguration> results = mongoDBFinder(form, q, keys);	
			List<ReportingConfiguration> reportingConfigurations = results.toList();
			return ok(Json.toJson(reportingConfigurations));
		}
	}
	
	private static Query getQuery(ConfigurationsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		if (CollectionUtils.isNotEmpty(form.pageCodes)) { //all
			queries.add(DBQuery.in("pageCodes", form.pageCodes));
		}
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

	@Override
	public Result get(String code) {
		ReportingConfiguration reportingConfiguration =  getReportingConfiguration(code);		
		if (reportingConfiguration != null) 
			return ok(Json.toJson(reportingConfiguration));	
		return notFound();
	}
	
	public Result save() {
		Form<ReportingConfiguration> filledForm = getFilledForm(reportConfigForm, ReportingConfiguration.class);
		ReportingConfiguration reportingConfiguration = filledForm.get();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		if (reportingConfiguration._id == null) {
			reportingConfiguration.setTraceCreationStamp(ctxVal, getCurrentUser());
			reportingConfiguration.code = generateReportingConfigurationCode();
		} else {
			return badRequest("use PUT method to update the ReportingConfiguration");
		}
		reportingConfiguration.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			reportingConfiguration = MongoDBDAO.save(InstanceConstants.REPORTING_CONFIG_COLL_NAME, reportingConfiguration);
			return ok(Json.toJson(reportingConfiguration));
		} else {
			return badRequest(app.errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	public Result update(String code) {
		ReportingConfiguration reportingConfiguration =  getReportingConfiguration(code);
		if (reportingConfiguration == null)
			return badRequest("ReportingConfiguration with code "+code+" does not exist");
		Form<ReportingConfiguration> filledForm = getFilledForm(reportConfigForm, ReportingConfiguration.class);
		ReportingConfiguration reportingConfigurationInput = filledForm.get();

		if (reportingConfigurationInput.code.equals(code)) {
			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
			reportingConfigurationInput.setTraceModificationStamp(ctxVal, getCurrentUser());
			reportingConfigurationInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.REPORTING_CONFIG_COLL_NAME, reportingConfigurationInput);
				return ok(Json.toJson(reportingConfigurationInput));
			} else {
				return badRequest(app.errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			return badRequest("ReportingConfiguration code are not the same");
		}				
	}
	
	@Override
	public Result delete(String code) {
		ReportingConfiguration reportingConfiguration =  getReportingConfiguration(code);
		if (reportingConfiguration == null)
			return badRequest("ReportingConfiguration with code "+reportingConfiguration+" does not exist");
		MongoDBDAO.deleteByCode(InstanceConstants.REPORTING_CONFIG_COLL_NAME,  ReportingConfiguration.class, reportingConfiguration.code);
		return ok();
	}
	
	public static String generateReportingConfigurationCode() {
		return ("RC-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).toUpperCase();		
	}
	
	private static ReportingConfiguration getReportingConfiguration(String code) {
		ReportingConfiguration reportingConfiguration = MongoDBDAO.findByCode(InstanceConstants.REPORTING_CONFIG_COLL_NAME, ReportingConfiguration.class, code);
    	return reportingConfiguration;
	}
	
}
