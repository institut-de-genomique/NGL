 package controllers.treatmenttypes.api;


import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.description.TreatmentType;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class TreatmentTypes extends APICommonController<TreatmentTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(TreatmentTypes.class);
	
	private final Form<TreatmentTypesSearchForm> treatmentTypesForm;

//	@Inject
//	public TreatmentTypes(NGLContext ctx) {
//		super(ctx, TreatmentTypesSearchForm.class);
//		treatmentTypesForm = ctx.form(TreatmentTypesSearchForm.class);
//	}

	@Inject
	public TreatmentTypes(NGLApplication app) {
		super(app, TreatmentTypesSearchForm.class);
		treatmentTypesForm = app.form(TreatmentTypesSearchForm.class);
	}

	public Result list() {
		Form<TreatmentTypesSearchForm> treatmentTypesFilledForm = filledFormQueryString(treatmentTypesForm,TreatmentTypesSearchForm.class);
		TreatmentTypesSearchForm searchForm = treatmentTypesFilledForm.get();

		List<TreatmentType> treatments;

		try {		
			if (searchForm.levels != null) {
				treatments = TreatmentType.find.get().findByLevels(searchForm.levels);
			} else {
				treatments = TreatmentType.find.get().findAll();
			}
			if (searchForm.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(treatments, treatments.size()))); 
			} else {
				return ok(Json.toJson(treatments));
			}
		} catch (DAOException e) {
			logger.error(e.getMessage());
			return  internalServerError(e.getMessage());
		}	
	}
	
	
	public Result get(String code) {
		TreatmentType treatmentType =  getTreatmentType(code);		
		if (treatmentType != null) {
			return ok(Json.toJson(treatmentType));	
		} 		
		else {
			return notFound();
		}	
	}

	private static TreatmentType getTreatmentType(String code) {
		try {
			return TreatmentType.find.get().findByCode(code);
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}		
	}
}
