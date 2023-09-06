package controllers.sra.analyzes.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;

import views.html.analyzes.consultation;
import views.html.analyzes.create;
import views.html.analyzes.home;
import views.html.analyzes.details;

public class Analyzes extends NGLController implements NGLJavascript  { // extends NGLBaseController {
	
	private final home home;
	private final create create;
	private final consultation consultation;
	private final details details;

	
	@Inject
	public Analyzes(NGLApplication app, home home, create create, consultation consultation, details details) {
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
	public Result create() {
		return ok(create.render());
	}
	
	// No annotation for tpl 
	public Result consultation() {
		return ok(consultation.render());
	}	
	
	public Result details() {
		return ok(details.render());
	}	
	

	// No annotation for tpl 
	public Result javascriptRoutes() {
		return jsRoutes(controllers.projects.api.routes.javascript.Projects.list(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.list(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.save(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.update(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.get(),
						controllers.sra.analyzes.tpl.routes.javascript.Analyzes.get(),
						controllers.sra.analyzes.tpl.routes.javascript.Analyzes.home(),
						controllers.authorisation.routes.javascript.User.get(),
						controllers.sra.analyzes.api.routes.javascript.AnalysisComments.save(),
						controllers.sra.analyzes.api.routes.javascript.AnalysisComments.update(),
						controllers.sra.analyzes.api.routes.javascript.AnalysisComments.delete(),
						controllers.sra.api.routes.javascript.Variables.get(),
						controllers.sra.api.routes.javascript.Variables.list(),
						controllers.commons.api.routes.javascript.States.list()
						);
	}
	
  	 
}
