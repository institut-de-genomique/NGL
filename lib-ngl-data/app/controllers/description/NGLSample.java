package controllers.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

//import controllers.CommonController;
import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
//import services.description.sample.ImportService;
//import services.description.sample.SampleService;

public class NGLSample extends Controller { // CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NGLSample.class);
	
	private final DataService dataService;
	
	@Inject
	public NGLSample(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			//InstituteService.main(errors);
			//ObjectTypeService.main(errors);
			//StateService.main(errors); 
			//ResolutionService.main(errors); 
			//LevelService.main(errors);
			//MeasureService.main(errors);
			//ContainerService.main(errors);
			//InstrumentService.main(errors);
//			SampleService.main(errors);
//			SampleService.main(errors);
			dataService.saveSampleData(errors);
			dataService.saveImportData(errors);
			//ExperimentService.main(errors);
			//ProcessService.main(errors);
			//RunService.main(errors);
			//TreatmentService.main(errors);
			if (errors.size() > 0) {
				return badRequest(Json.toJson(errors));
			} else {
				return ok();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}				
	}
	
}
