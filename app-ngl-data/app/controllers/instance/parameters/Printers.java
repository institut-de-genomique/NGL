package controllers.instance.parameters;

import javax.inject.Inject;

import models.Constants;
import nglapps.IApplicationData;
//import play.Logger;
//import play.Logger.ALogger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
//import services.instance.protocol.ProtocolServiceCNG;
//import services.instance.protocol.ProtocolServiceCNS;
import validation.ContextValidation;


public class Printers extends Controller { 
	
	public static final play.Logger.ALogger logger = play.Logger.of(Printers.class);
		

	public final IApplicationData appData;
	
	@Inject
	public Printers(IApplicationData appData) {
		this.appData = appData;
	}
	

	public Result save() {
		ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
		try {
			appData.getPrinterService().accept(ctx);
			if (ctx.hasErrors()) {
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

