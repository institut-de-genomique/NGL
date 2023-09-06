package controllers.description.instruments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import models.utils.ModelDAOs;
import nglapps.DataService;
// import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
//import services.description.instrument.InstrumentService;

// import controllers.CommonController;
public class Instruments extends Controller { // CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Instruments.class);
	
	private final DataService dataService;
	
	public Instruments(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
//			InstrumentService.main(errors);
			dataService.saveInstrumentData(errors);
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
