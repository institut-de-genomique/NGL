package controllers.instance.parameters;

import models.Constants;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.mvc.Result;
import services.instance.parameter.PrinterCNS;
import services.instance.protocol.ProtocolServiceCNG;
import services.instance.protocol.ProtocolServiceCNS;
import validation.ContextValidation;
import controllers.CommonController;

public class Printers extends CommonController {
	
	public static ALogger logger= Logger.of("Printers");
		
	public static Result save(){
		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
		ctx.setCreationMode();
		try {
//			if (play.Play.application().configuration().getString("institute").equals("CNS")){
//				PrinterCNS.main(ctx);
//			}else if(play.Play.application().configuration().getString("institute").equals("CNG")){
//			}else
			if(play.Play.application().configuration().getString("institute").equals("GET")){
				
			}else{
				Logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
			}
			
			if (ctx.hasErrors()) {
				ctx.displayErrors(logger);
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

