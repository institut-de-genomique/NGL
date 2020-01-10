package controllers.sra.studies.tpl;

import javax.inject.Inject;

import controllers.NGLController;
//import controllers.NGLBaseController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.studies.consultation;
import views.html.studies.create;
import views.html.studies.details;
import views.html.studies.home;

//import controllers.CommonController;               // done
// public class Studies extends -CommonController {  // done
public class Studies extends NGLController implements NGLJavascript  { // NGLBaseController {
	
	private final home home;
	private final create create;
	private final consultation consultation;
	private final details details;
	
	@Inject
	public Studies(NGLApplication app, home home, create create, consultation consultation, details details) {
		super(app);
		this.home         = home; 
		this.create       = create;
		this.consultation = consultation;
		this.details      = details;
	}
	
	@Authenticated
	@Historized
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	@Authenticated
	@Historized
	public Result get(String code) {
		return ok(home.render("search"));
	}

	// No annotation for tpl 
	public Result consultation() {
		return ok(consultation.render());
	}	
	
	// No annotation for tpl 
	public Result create() {
		return ok(create.render());
	}
	
	// No annotation for tpl 
	public Result details() {
		return ok(details.render());
	}	
	
	/*public static Result release() {
		return ok(release.render());
	}	
	*/
	
	// No annotation for tpl 
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.sra.studies.tpl.routes.javascript.Studies.home(),
  	    				controllers.projects.api.routes.javascript.Projects.list(),
  	    				controllers.commons.api.routes.javascript.States.list(),
  	    				controllers.sra.api.routes.javascript.Variables.get(),
  	    				controllers.sra.api.routes.javascript.Variables.list(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.save(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.get(),
  	    				controllers.sra.studies.tpl.routes.javascript.Studies.get(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.list(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.update(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.release(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.list(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.get(),
  	    				controllers.sra.submissions.api.routes.javascript.Submissions.createFromStudy());
 	    
	}

	/*
	public Result javascriptRoutes() {
  	    response().setContentType("text/javascript");
  	    return ok(  	    		
  	    		// Routes.javascriptRouter("jsRoutes",
  	    		// Routes
  	    		JavaScriptReverseRouter.create("jsRoutes",
  	    				controllers.sra.studies.tpl.routes.javascript.Studies.home(),
  	    				controllers.projects.api.routes.javascript.Projects.list(),
  	    				controllers.commons.api.routes.javascript.States.list(),
  	    				controllers.sra.api.routes.javascript.Variables.get(),
  	    				controllers.sra.api.routes.javascript.Variables.list(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.save(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.get(),
  	    				controllers.sra.studies.tpl.routes.javascript.Studies.get(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.list(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.update(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.release(),
  	    				controllers.sra.studies.api.routes.javascript.Studies.updateState(),
  	    				controllers.sra.samples.api.routes.javascript.Samples.list(),
  	    				controllers.sra.samples.api.routes.javascript.Samples.get(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.list(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.get()
  	    				)
  	    		);
	}
	*/
	
}
