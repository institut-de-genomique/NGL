package controllers.sra.projects.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;


import views.html.projects.consultation;
import views.html.projects.home;
import views.html.projects.update;

public class Projects extends  NGLController implements NGLJavascript {

	private final home home;
	private final consultation consultation;
	//private final details details;
	private final update update;


	
	@Inject
	public Projects(NGLApplication app, home home, consultation consultation,update update) {
		super(app);
		this.home         = home; 
		this.consultation = consultation;
		this.update       = update;
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

	// No annotation for tpl 
	public Result update() {
		return ok(update.render());
	}
	
//	public Result details() {
//		return ok(details.render());
//	}	

	
	// No annotation for tpl 
	public Result javascriptRoutes() {	
		return jsRoutes(controllers.sra.projects.tpl.routes.javascript.Projects.home(),
				controllers.projects.api.routes.javascript.Projects.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.sra.api.routes.javascript.Variables.get(),
				controllers.sra.api.routes.javascript.Variables.list(),
				controllers.sra.projects.api.routes.javascript.Projects.get(),
				controllers.sra.projects.tpl.routes.javascript.Projects.get(),
				controllers.sra.projects.api.routes.javascript.Projects.list(),
				controllers.sra.projects.api.routes.javascript.Projects.update(),
				controllers.sra.submissions.api.routes.javascript.Submissions.createForUpdate(),
  				controllers.sra.submissions.api.routes.javascript.Submissions.updateState());
	}


}