package controllers.description.containers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.description.container.ContainerService;

// import controllers.CommonController;
public class Containers extends Controller { // CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(Containers.class);
	
	public static Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			ContainerService.main(errors);
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
