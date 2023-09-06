package controllers.instance.resolution;

import javax.inject.Inject;

import models.Constants;
import nglapps.DataService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import validation.ContextValidation;

public class Resolutions extends Controller {
		
	private static final play.Logger.ALogger logger = play.Logger.of(Resolutions.class);
	
	private final DataService dataService;
	
	@Inject
	public Resolutions(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
//		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
//		ctx.setCreationMode();
		ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
		try {
			// ResolutionService.main(ctx);
			dataService.saveResolutionData(ctx);
			if (ctx.getErrors().size() > 0) {
				return badRequest(Json.toJson(ctx.getErrors()));
			} else {
				return ok();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}	
	}
	
}


