package controllers.description.runs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.CommonController;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;
import services.description.run.TreatmentService;

public class Treatments extends CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(Treatments.class);
	
	public static Result save(){
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			TreatmentService.main(errors);
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
