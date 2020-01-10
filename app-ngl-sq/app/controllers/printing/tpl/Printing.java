package controllers.printing.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.printing.home;

public class Printing extends NGLController implements NGLJavascript  {

	private final home home;

	@Inject
	public Printing(NGLApplication app, home home) {
		super(app);
		this.home = home;
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code){
		return ok(home.render(code));
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.printing.api.routes.javascript.Tags.list(),
						controllers.printing.api.routes.javascript.Tags.print(),
						controllers.commons.api.routes.javascript.Parameters.list(),
						controllers.printing.tpl.routes.javascript.Printing.home(),
						controllers.printing.tpl.routes.javascript.Tags.display());
	}
	
//	// tpl
//	public Result javascriptRoutes() {
//		response().setContentType("text/javascript");
//		return ok(  	    		
//				// Routes.javascriptRouter("jsRoutes",
//				JavaScriptReverseRouter.create("jsRoutes",
//						// Routes
//						controllers.printing.api.routes.javascript.Tags.list(),
//						controllers.printing.api.routes.javascript.Tags.print(),
//						controllers.commons.api.routes.javascript.Parameters.list(),
//						controllers.printing.tpl.routes.javascript.Printing.home(),
//						controllers.printing.tpl.routes.javascript.Tags.display()
//
//						)	  	      
//				);
//	}
	
}
