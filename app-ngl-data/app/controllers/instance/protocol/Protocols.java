package controllers.instance.protocol;

import javax.inject.Inject;

import models.Constants;
import nglapps.DataService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import validation.ContextValidation;

public class Protocols extends Controller { // NGLBaseController { //CommonController {
	
	public static play.Logger.ALogger logger = play.Logger.of("Protocols");
		
//	@Inject
//	public Protocols(NGLContext ctx) {
//		super(ctx);
//	}
	
//	private final String institute;
//	
//	@Inject
//	public Protocols(NGLConfig config) {
//		institute = config.getInstitute();
//	}
	
	private final DataService dataService;
	
	@Inject
	public Protocols(DataService dataService) {
		this.dataService = dataService;
	}
	
	public Result save() {
//		ContextValidation ctx = new ContextValidation(Constants.NGL_DATA_USER);
//		ctx.setCreationMode();
		ContextValidation ctx = ContextValidation.createCreationContext(Constants.NGL_DATA_USER);
		try {
//			if (play.Play.application().configuration().getString("institute").equals("CNS")) {
//				ProtocolServiceCNS.main(ctx);
//			} else if(play.Play.application().configuration().getString("institute").equals("CNG")) {
//				ProtocolServiceCNG.main(ctx);
//			} else if(play.Play.application().configuration().getString("institute").equals("TEST")) {
//				ProtocolServiceCNS.main(ctx);
//			} else {
//				logger.error("You need to specify only one institute ! Now, it's "+ play.Play.application().configuration().getString("institute"));
//			}
//			switch (institute) {
//			case "CNS"  : ProtocolServiceCNS.main(ctx); break;
//			case "CNG"  : ProtocolServiceCNG.main(ctx); break;
//			case "TEST" : ProtocolServiceCNS.main(ctx); break;
//			default     : logger.error("You need to specify only one institute ! Now, it's {}", institute);
//			}
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

