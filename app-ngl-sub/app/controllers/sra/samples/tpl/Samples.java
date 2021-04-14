package controllers.sra.samples.tpl;

import javax.inject.Inject;

import controllers.CommonController;
import play.mvc.Result;


//import controllers.NGLController;
//import controllers.NGLBaseController;
//import fr.cea.ig.authentication.Authenticated;
//import fr.cea.ig.lfw.Historized;
//import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.support.NGLJavascript;
import views.html.samples.consultation;
import views.html.samples.home;
import views.html.samples.update;

public class Samples extends CommonController {

	private final home home;
	private final consultation consultation;
	//private final details details;
	private final update update;

	@Inject
	public Samples(home home, 
				   consultation consultation,
				   update       update) {
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
		return jsRoutes(controllers.sra.samples.tpl.routes.javascript.Samples.home(),
				controllers.projects.api.routes.javascript.Projects.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.sra.api.routes.javascript.Variables.get(),
				controllers.sra.api.routes.javascript.Variables.list(),
				controllers.sra.samples.api.routes.javascript.Samples.get(),
				controllers.sra.samples.tpl.routes.javascript.Samples.get(),
				controllers.sra.samples.api.routes.javascript.Samples.list(),
				controllers.sra.samples.api.routes.javascript.Samples.update(),
				controllers.sra.samples.api.routes.javascript.Samples.loadUserFileSample(),
				controllers.sra.submissions.api.routes.javascript.Submissions.createForUpdate(),
  				controllers.sra.submissions.api.routes.javascript.Submissions.updateState());
	}
}