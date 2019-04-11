package controllers.alerts.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.alert.instance.Alert;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class Alerts extends DocumentController<Alert> {

	private final Form<AlertsSearchForm> searchForm;
	
//	@Inject
//	public Alerts(NGLContext ctx) {
//		super(ctx, InstanceConstants.ALERT_COLL_NAME, Alert.class);
//		this.searchForm = getNGLContext().form(AlertsSearchForm.class);
//	}

	@Inject
	public Alerts(NGLApplication ctx) {
		super(ctx, InstanceConstants.ALERT_COLL_NAME, Alert.class);
		this.searchForm = getNGLContext().form(AlertsSearchForm.class);
	}
	
	//@Permission(value={"reading"})
	public Result list() {
		Form<AlertsSearchForm> filledForm = filledFormQueryString(searchForm, AlertsSearchForm.class);
		AlertsSearchForm form = filledForm.get();
		
		Query q = getQuery(form);
		if(form.datatable){
			MongoDBResult<Alert> results = mongoDBFinder(form, q);
			//MongoDBResult<Alert> results = mongoDBFinder(InstanceConstants.ALERT_COLL_NAME, form, Alert.class, q);				
			List<Alert> alerts = results.toList();
			return ok(Json.toJson(new DatatableResponse<>(alerts, results.count())));
		}
		MongoDBResult<Alert> results = mongoDBFinder(form, q);
		// MongoDBResult<Alert> results = mongoDBFinder(InstanceConstants.ALERT_COLL_NAME, form, Alert.class, q);
		List<Alert> alerts = results.toList();
		return ok(Json.toJson(alerts));
	}
	
	
	private Query getQuery(AlertsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		if (StringUtils.isNotBlank(form.regexCode)) { //all
			queries.add(DBQuery.regex("code", Pattern.compile(form.regexCode)));
		}
		return query;
	}
	
	//@Permission(value={"reading"})
	@Override
	public Result get(String code) {
		Alert alert = getAlert(code);
		if (alert != null)	
			return ok(Json.toJson(alert));					
		return notFound();
	}
	
	//@Permission(value={"reading"})
	@Override
	public Result head(String code) {
		if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.ALERT_COLL_NAME, Alert.class, code))			
			return ok();					
		return notFound();
	}
	
	private Alert getAlert(String code) {
		Alert alert = MongoDBDAO.findByCode(InstanceConstants.ALERT_COLL_NAME, Alert.class, code);
		return alert;
	}
	
}
