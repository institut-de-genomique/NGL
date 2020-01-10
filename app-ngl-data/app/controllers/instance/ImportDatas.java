package controllers.instance;

import models.Constants;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.instance.container.UpdateTaraPropertiesCNS;
import validation.ContextValidation;
 
public class ImportDatas extends Controller {
	
	
	public static Result updateTara(){
		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
		try {
			UpdateTaraPropertiesCNS.updateSampleFromTara(ctx, null);
			if (ctx.errors.size() > 0) {
				return badRequest(Json.toJson(ctx.errors));
			} else {
				return ok("Update Tara OK");
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}	

	}
}
