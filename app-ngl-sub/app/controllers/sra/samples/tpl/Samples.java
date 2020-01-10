package controllers.sra.samples.tpl;

import javax.inject.Inject;
import controllers.CommonController;
//import play.routing.JavaScriptReverseRouter;
import play.mvc.Result;
import views.html.samples.home;
import views.html.samples.consultation;
//import views.html.samples.details;

public class Samples extends CommonController {

	private final home home;
	private final consultation consultation;
	//private final details details;

	@Inject
	public Samples(home home, consultation consultation) {
		this.home         = home; 
		this.consultation = consultation;

	}

	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	public Result consultation() {
		return ok(consultation.render());
	}	

	public Result get(String code) {
		return ok(home.render("search"));
	}

	/*public Result details() {
		return ok(details.render());
	}	
*/
//	public Result javascriptRoutes() {
////		response().setContentType("text/javascript");
//	    return ok(  	    		
//	    		// Routes.javascriptRouter("jsRoutes",
//	    		// Routes
//	    		JavaScriptReverseRouter.create("jsRoutes",
//	    				controllers.sra.samples.tpl.routes.javascript.Samples.home(),
//	    				controllers.projects.api.routes.javascript.Projects.list(),
//	    				controllers.commons.api.routes.javascript.States.list(),
//	    				controllers.sra.api.routes.javascript.Variables.get(),
//	    				controllers.sra.api.routes.javascript.Variables.list(),
//	    				controllers.sra.samples.api.routes.javascript.Samples.get(),
//	    				controllers.sra.samples.tpl.routes.javascript.Samples.get(),
//	    				controllers.sra.samples.api.routes.javascript.Samples.list(),
//	    				controllers.sra.samples.api.routes.javascript.Samples.update()
//	    				)
//	    ).as("text/javascript");
//	}
	
	
	// No annotation for tpl 
	public Result javascriptRoutes() {	
		return jsRoutes(controllers.sra.samples.tpl.routes.javascript.Samples.home(),
				controllers.projects.api.routes.javascript.Projects.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.sra.api.routes.javascript.Variables.get(),
				controllers.sra.api.routes.javascript.Variables.list(),
				controllers.sra.samples.api.routes.javascript.Samples.get(),
				controllers.sra.samples.tpl.routes.javascript.Samples.get(),
				controllers.sra.samples.api.routes.javascript.Samples.list(),
				controllers.sra.samples.api.routes.javascript.Samples.update());
	}
}