package controllers.description.projects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Projects extends Controller {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Projects.class);
	
	private final DataService dataService;
	
	@Inject
	public Projects(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			dataService.saveProjectData(errors);
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
