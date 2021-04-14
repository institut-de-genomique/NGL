package controllers.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import models.utils.ModelDAOs;
import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.description.common.MeasureService;
import services.description.container.ContainerService;

public class NGLSeq extends Controller {

	private static final play.Logger.ALogger logger = play.Logger.of(NGLSeq.class);
	
	private final ModelDAOs   mdao;
	private final DataService dataService;
	
	@Inject
	public NGLSeq(ModelDAOs mdao, DataService dataService) {
		this.mdao        = mdao;
		this.dataService = dataService;
	}
	
	public Result save() {
		try {
			Map<String,List<ValidationError>> errors = new HashMap<>();
			//InstituteService.main(errors);
			//ObjectTypeService.main(errors);
			//StateService.main(errors); 
			//ResolutionService.main(errors); 
			//LevelService.main(errors);
			new MeasureService  (mdao).saveData(errors);
			new ContainerService(mdao).saveData(errors);
			dataService.saveInstrumentData(errors);
			dataService.saveSampleData    (errors);
			dataService.saveImportData    (errors);
			dataService.saveExperimentData(errors);
			dataService.saveProcessData   (errors);
			//RunService.main(errors);
			//TreatmentService.main(errors);
			if (errors.size() > 0) {
				return badRequest(Json.toJson(errors));
			} else {
				logger.info("NGLSeq description is loaded!");
				return ok();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}				
	}
	
}
