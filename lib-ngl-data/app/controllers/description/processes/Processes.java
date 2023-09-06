package controllers.description.processes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nglapps.DataService;
// import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
//import services.description.process.ProcessService;

// import controllers.CommonController;
public class Processes extends Controller { // CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Processes.class);
	
	public final DataService dataService;
	
	public Processes(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
//			ProcessService.main(errors);
			dataService.saveProcessData(errors);
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
