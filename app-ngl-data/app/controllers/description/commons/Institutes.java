package controllers.description.commons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.validation.ValidationError;
import play.libs.Json;
// import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import services.description.common.InstituteService;

//import controllers.CommonController;
//public class Institutes extends CommonController {
public class Institutes extends Controller {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Institutes.class);
	
	public static Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			InstituteService.main(errors);
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
