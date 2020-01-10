package controllers.processes.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.processes.description.ProcessCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ProcessCategories extends APICommonController<ProcessCategoriesSearchForm> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ProcessCategories.class);
	
	private final Form<ProcessCategoriesSearchForm> processCategoryForm;
	
//	@Inject
//	public ProcessCategories(NGLContext ctx) {
//		super(ctx, ProcessCategoriesSearchForm.class);
//		processCategoryForm = ctx.form(ProcessCategoriesSearchForm.class);
//	}
	
	@Inject
	public ProcessCategories(NGLApplication app) {
		super(app, ProcessCategoriesSearchForm.class);
		processCategoryForm = app.form(ProcessCategoriesSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result list() throws DAOException {
		Form<ProcessCategoriesSearchForm> processCategoryFilledForm = filledFormQueryString(processCategoryForm,ProcessCategoriesSearchForm.class);
		ProcessCategoriesSearchForm processCategoriesSearch = processCategoryFilledForm.get();
		
		List<ProcessCategory> processCategories;
		
		try {		
			processCategories = ProcessCategory.find.get().findAll();
			
			if (processCategoriesSearch.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(processCategories, processCategories.size()))); 
			} else if(processCategoriesSearch.list) {
				List<ListObject> lop = new ArrayList<>();
				for (ProcessCategory et:processCategories) {
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			} else {
				return Results.ok(Json.toJson(processCategories));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
	
}
