package controllers.sra.experiments.tpl;

import javax.inject.Inject;

import controllers.CommonController;
import play.mvc.Result;
import views.html.experiments.home;
import views.html.experiments.consultation;
import views.html.experiments.details;

public class Experiments extends CommonController {

	private final home home;
	private final consultation consultation;
	private final details details;

	@Inject
	public Experiments(home home, consultation consultation, details details) {
		this.home         = home; 
		this.consultation = consultation;
		this.details      = details;
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

	public Result details() {
		return ok(details.render());
	}	
//
//	public Result javascriptRoutes() {
////		response().setContentType("text/javascript");
//	    return ok(  	    		
//	    		// Routes.javascriptRouter("jsRoutes",
//	    		// Routes
//	    		JavaScriptReverseRouter.create("jsRoutes",
//	    				controllers.sra.experiments.tpl.routes.javascript.Experiments.home(),
//	    				controllers.projects.api.routes.javascript.Projects.list(),
//	    				controllers.commons.api.routes.javascript.States.list(),
//	    				controllers.sra.api.routes.javascript.Variables.get(),
//	    				controllers.sra.api.routes.javascript.Variables.list(),
//	    				controllers.sra.experiments.api.routes.javascript.Experiments.get(),
//	    				controllers.sra.experiments.tpl.routes.javascript.Experiments.get(),
//	    				controllers.sra.experiments.api.routes.javascript.Experiments.list(),
//	    				controllers.sra.experiments.api.routes.javascript.Experiments.update()
//	    				)
//	    ).as("text/javascript");
//	}
	// No annotation for tpl 
	public Result javascriptRoutes() {	
		return jsRoutes(controllers.sra.experiments.tpl.routes.javascript.Experiments.home(),
						controllers.projects.api.routes.javascript.Projects.list(),
						controllers.commons.api.routes.javascript.States.list(),
						controllers.sra.api.routes.javascript.Variables.get(),
						controllers.sra.api.routes.javascript.Variables.list(),
						controllers.sra.experiments.api.routes.javascript.Experiments.get(),
						controllers.sra.experiments.tpl.routes.javascript.Experiments.get(),
						controllers.sra.experiments.api.routes.javascript.Experiments.list(),
						controllers.sra.experiments.api.routes.javascript.Experiments.update());	
	}
}