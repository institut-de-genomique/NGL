package controllers.sampletypes.api;

import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class SampleTypes extends APICommonController<SampleTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(SampleTypes.class);
	
	private final Form<SampleTypesSearchForm> sampleTypesForm;

	@Inject
	public SampleTypes(NGLApplication app) {
		super(app, SampleTypesSearchForm.class);
		sampleTypesForm = app.form(SampleTypesSearchForm.class);
	}

	public Result list() {
		Form<SampleTypesSearchForm> sampleTypesFilledForm = filledFormQueryString(sampleTypesForm,SampleTypesSearchForm.class);
		SampleTypesSearchForm searchForm = sampleTypesFilledForm.get();

		List<SampleType> samples;

		try {		
			samples = SampleType.find.get().findAll();
			if (searchForm.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(samples, samples.size()))); 
			} else {
				return ok(Json.toJson(samples));
			}
		} catch (DAOException e) {
			logger.error(e.getMessage());
			return  internalServerError(e.getMessage());
		}	
	}
	
}
