package controllers.runs.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.description.RunCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class RunCategories extends APICommonController<RunCategoriesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(RunCategories.class);
	
	private final Form<RunCategoriesSearchForm> runCategoriesForm;
	
//	@Inject
//	public RunCategories(NGLContext ctx) {
//		super(ctx, RunCategoriesSearchForm.class);
//		runCategoriesForm = ctx.form(RunCategoriesSearchForm.class);
//	}
	
	@Inject
	public RunCategories(NGLApplication ctx) {
		super(ctx, RunCategoriesSearchForm.class);
		runCategoriesForm = ctx.form(RunCategoriesSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result list(){
		Form<RunCategoriesSearchForm> runCategoryFilledForm = filledFormQueryString(runCategoriesForm,RunCategoriesSearchForm.class);
		RunCategoriesSearchForm runCategoriesSearch = runCategoryFilledForm.get();
		
		List<RunCategory> runCategories;
		
		try{		
			runCategories = RunCategory.find.get().findAll();
			
			if(runCategoriesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(runCategories, runCategories.size()))); 
			}else if(runCategoriesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(RunCategory et:runCategories){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(runCategories));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
}
