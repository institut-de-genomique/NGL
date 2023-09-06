package controllers.description.commons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
// import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
//import services.description.common.InstituteService;

public class Institutes extends Controller {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Institutes.class);
	
	private final DataService dataService;	
	
	public Institutes(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
//			InstituteService.main(errors);
			dataService.saveInstitutes(errors);
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
