package controllers.instance.resolution;

import models.Constants;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import services.instance.resolution.ResolutionServiceGET;
import validation.ContextValidation;
import controllers.CommonController;

public class Resolutions extends CommonController {
	
	public static Result save(){
		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
		ctx.setCreationMode();
		try {
			ResolutionServiceGET.main(ctx);
			if (ctx.errors.size() > 0) {
				return badRequest(Json.toJson(ctx.errors));
			} else {
				return ok();
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}	
	}
}


