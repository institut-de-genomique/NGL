package controllers.description.samples;

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

public class Samples extends Controller { // CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Samples.class);
	
	private final DataService dataService;
	
	@Inject
	public Samples(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save(){
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			//SampleService.main(errors);
			dataService.saveImportData(errors);
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
