package controllers.receptions.api;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.DocumentController;
import controllers.ListForm;
import controllers.authorisation.Permission;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.reception.instance.DefaultFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration;
import models.laboratory.sample.description.ImportType;
import models.utils.InstanceConstants;
import models.utils.ListObject;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class ReceptionConfigurations extends DocumentController<ReceptionConfiguration> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReceptionConfigurations.class);
	
//	@Inject
//	public ReceptionConfigurations(NGLContext ctx) {
//		super(ctx,InstanceConstants.RECEPTION_CONFIG_COLL_NAME, ReceptionConfiguration.class);	
////		reportConfigForm = ctx.form(ReceptionConfiguration.class);
//	}
	
	@Inject
	public ReceptionConfigurations(NGLApplication app) {
		super(app,InstanceConstants.RECEPTION_CONFIG_COLL_NAME, ReceptionConfiguration.class);	
	}

	@Permission(value={"reading"})
	public Result list() {
		ListForm searchForm = filledFormQueryString(ListForm.class);
		DBQuery.Query query = DBQuery.empty();

		if (searchForm.datatable) {
			MongoDBResult<ReceptionConfiguration> results = mongoDBFinder(searchForm, query);
			List<ReceptionConfiguration> configurations = getActiveReceptions(results.toList());
			return ok(Json.toJson(new DatatableResponse<>(configurations, results.count())));
		} else if(searchForm.list) {
			MongoDBResult<ReceptionConfiguration> results = mongoDBFinder(searchForm,query).sort("displayOrder");
			List<ReceptionConfiguration> configurations = getActiveReceptions(results.toList());
			List<ListObject> los = new ArrayList<>();
			for(ReceptionConfiguration p: configurations)
				los.add(new ListObject(p.code, p.name));
			return Results.ok(Json.toJson(los));
		} else {
			MongoDBResult<ReceptionConfiguration> results = mongoDBFinder(searchForm, query);
			List<ReceptionConfiguration> configurations = getActiveReceptions(results.toList());
			return Results.ok(Json.toJson(configurations));
		}		
	}

	private List<ReceptionConfiguration> getActiveReceptions(List<ReceptionConfiguration> configurations) {
		List<ReceptionConfiguration> res = new ArrayList<>();
		List<ImportType> itList = ImportType.find.get().findAll();

		for (int i = 0; i < configurations.size(); i++) {
			if (configurations.get(i).configs.get("container") != null && configurations.get(i).configs.get("container").get("importTypeCode") != null) {
				DefaultFieldConfiguration fc = (DefaultFieldConfiguration) configurations.get(i).configs.get("container").get("importTypeCode");

				if (fc != null) {
					for (int j = 0; j < itList.size(); j++) {
						ImportType it = itList.get(j);

						if ((it != null) && it.code.equals(fc.value)) {
							if (it.active) {
								res.add(configurations.get(i));
							}
						} 
					}
				}
			} else {
				res.add(configurations.get(i));
			}
		};

		return res;
	}
	
	@Permission(value={"writing"})
	public Result save() {
		Form<ReceptionConfiguration> filledForm = getMainFilledForm();
		ReceptionConfiguration input = filledForm.get();

		if (input._id == null) {
			input.traceInformation = new TraceInformation();
			input.traceInformation.setTraceInformation(getCurrentUser());
			input.code = generateReceptionConfigurationCode();
		} else {
			return badRequest("use PUT method to update the ReceptionConfiguration");
		}

//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		input.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			input = saveObject(input);			
			return ok(Json.toJson(input));
		} else {
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}	
	}
	
	@Permission(value={"writing"})
	public Result update(String code) {
		ReceptionConfiguration objectInDB = getObject(code);
		if (objectInDB == null) {
			return badRequest("ReceptionConfiguration with code "+code+" not exist");
		}
		Form<ReceptionConfiguration> filledForm = getMainFilledForm();
		ReceptionConfiguration input = filledForm.get();
		
		if (code.equals(input.code)) {
			if (input.traceInformation != null) {
				input.traceInformation.setTraceInformation(getCurrentUser());
			} else {
				logger.error("traceInformation is null !!");
			}
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
			input.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				updateObject(input);
				return ok(Json.toJson(input));
			} else {
				//return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			return badRequest("ReceptionConfiguration codes are not the same");
		}
	}
	
	private static String generateReceptionConfigurationCode(){
		return ("RC-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).toUpperCase();		
	}
	
}
