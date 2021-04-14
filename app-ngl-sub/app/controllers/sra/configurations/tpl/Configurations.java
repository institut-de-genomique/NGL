package controllers.sra.configurations.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.configurations.consultation;
import views.html.configurations.create;
import views.html.configurations.home;

public class Configurations extends NGLController implements NGLJavascript  { // extends NGLBaseController {
	
	private home home;
	private create create;
	private consultation consultation;
	
	@Inject
	public Configurations(NGLApplication app, home home, create create, consultation consultation) {
		super(app);
		this.home         = home;
		this.create       = create;
		this.consultation = consultation;
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
	

	// No annotation for tpl 
	public Result javascriptRoutes() {
		return jsRoutes(controllers.sra.configurations.tpl.routes.javascript.Configurations.home(),
						controllers.projects.api.routes.javascript.Projects.list(),
						controllers.commons.api.routes.javascript.States.list(),
						controllers.sra.configurations.api.routes.javascript.Configurations.save(),
						controllers.sra.api.routes.javascript.Variables.get(),
						controllers.sra.api.routes.javascript.Variables.list(),
						controllers.sra.configurations.api.routes.javascript.Configurations.get(),
						controllers.sra.configurations.tpl.routes.javascript.Configurations.get(),
						controllers.sra.configurations.api.routes.javascript.Configurations.list(),
						controllers.sra.configurations.api.routes.javascript.Configurations.update());
	}
	
  	 
}
