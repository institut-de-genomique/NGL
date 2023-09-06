package controllers.treatmenttypes.api;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.description.TreatmentCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class TreatmentCategories extends APICommonController<TreatmentCategoriesSearchForm> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(TreatmentCategories.class);
	
	private final Form<TreatmentCategoriesSearchForm> treatmentCategoriesForm;

	@Inject
	public TreatmentCategories(NGLApplication app) {
		super(app, TreatmentCategoriesSearchForm.class);
		treatmentCategoriesForm = app.form(TreatmentCategoriesSearchForm.class);
	}
	
	/* NGL-3530 FDS 16/06/2022 pour l'instant on a besoin que de la methode list() */
	public Result list() {
		Form<TreatmentCategoriesSearchForm> treatmentCategoriesFilledForm = filledFormQueryString(treatmentCategoriesForm,TreatmentCategoriesSearchForm.class);
		TreatmentCategoriesSearchForm searchForm = treatmentCategoriesFilledForm.get();
		
		try {
			List<TreatmentCategory> categories = TreatmentCategory.find.get().findAll();
			
			if (searchForm.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(categories, categories.size()))); 
			} else if(searchForm.list){
				// ne garder que code et name!!
				List<ListObject> lop = categories.stream()
						.map((TreatmentCategory category) -> {
							return new ListObject(category.code, category.name);
						})
						.collect(Collectors.toList());
				return Results.ok(Json.toJson(lop));
			} else {
				return ok(Json.toJson(categories));
			}
		} catch (DAOException e) {
			logger.error(e.getMessage());
			return  internalServerError(e.getMessage());
		}
	}
}
