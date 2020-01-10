package controllers.experiments.api;

// import static play.data.Form.form;
//import static fr.cea.ig.play.IGGlobals.form;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
//import controllers.CommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.experiment.description.ExperimentCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
//import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;


public class ExperimentCategories extends APICommonController<ExperimentCategoriesSearchForm> { 
	
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentCategories.class);
	
	private final Form<ExperimentCategoriesSearchForm> experimentCategoryForm;
	
//	@Inject
//	public ExperimentCategories(NGLContext ctx) {
//		super(ctx, ExperimentCategoriesSearchForm.class);
//		experimentCategoryForm = ctx.form(ExperimentCategoriesSearchForm.class);
//	}

	@Inject
	public ExperimentCategories(NGLApplication ctx) {
		super(ctx, ExperimentCategoriesSearchForm.class);
		experimentCategoryForm = ctx.form(ExperimentCategoriesSearchForm.class);
	}
	
	@Permission(value={"reading"})
	public Result list() throws DAOException{
		Form<ExperimentCategoriesSearchForm>  experimentCategoryFilledForm = filledFormQueryString(experimentCategoryForm,ExperimentCategoriesSearchForm.class);
		ExperimentCategoriesSearchForm experimentCategoriesSearch = experimentCategoryFilledForm.get();
		try {
			List<ExperimentCategory> experimentCategories;
			
			if (StringUtils.isNotBlank(experimentCategoriesSearch.processTypeCode)) {
				experimentCategories = ExperimentCategory.find.get().findByProcessTypeCode(experimentCategoriesSearch.processTypeCode);
			} else {
				experimentCategories = ExperimentCategory.find.get().findAll();
			}
			if (experimentCategoriesSearch.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(experimentCategories, experimentCategories.size()))); 
			} else if(experimentCategoriesSearch.list) {
			 	List<ListObject> lop = new ArrayList<>();
				for (ExperimentCategory et : experimentCategories) {
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			} else {
				return Results.ok(Json.toJson(experimentCategories));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
	
}
