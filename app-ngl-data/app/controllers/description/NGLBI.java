package controllers.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.description.run.TreatmentService;

public class NGLBI extends Controller { 

	private static final play.Logger.ALogger logger = play.Logger.of(NGLBI.class);
	
	private final DataService dataService;
	
	@Inject
	public NGLBI(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save(){
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
			//SampleService.main(errors);
			//ImportService.main(errors);
			//ExperimentService.main(errors);
			//ProcessService.main(errors);
//			RunService.main(errors);
			dataService.saveRunData(errors);
			TreatmentService.main(errors);
			if (errors.size() > 0) {
				return badRequest(Json.toJson(errors));
			} else {
				logger.info("NGLBI description is loaded!");
				return ok();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}				
	}
	
}
