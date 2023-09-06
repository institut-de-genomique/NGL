package controllers.description.experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nglapps.DataService;
// import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
//import services.description.experiment.ExperimentService;

// import controllers.CommonController;
public class Experiments extends Controller { // CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Experiments.class);
	
	private final DataService dataService;
	
	public Experiments(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
//			ExperimentService.main(errors);
			dataService.saveExperimentData(errors);
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
