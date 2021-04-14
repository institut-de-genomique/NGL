package controllers.instance.protocol;

import javax.inject.Inject;

import models.Constants;
import nglapps.DataService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import validation.ContextValidation;

public class Protocols extends Controller { 
	
	public static play.Logger.ALogger logger = play.Logger.of("Protocols");
		
	private final DataService dataService;
	
	@Inject
	public Protocols(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
		ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
		try {
			dataService.saveProtocolData(ctx);
			if (ctx.getErrors().size() > 0) {
				ctx.displayErrors(logger);
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

