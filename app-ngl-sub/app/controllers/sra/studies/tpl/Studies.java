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
import views.html.studies.update;

//import controllers.CommonController;               // done
// public class Studies extends -CommonController {  // done
public class Studies extends NGLController implements NGLJavascript  { // NGLBaseController {
	
	private final home home;
	private final create create;
	private final consultation consultation;
	private final details details;
	private final update update;

	
	@Inject
	public Studies(NGLApplication app, home home, create create, consultation consultation, details details, update update) {
		super(app);
		this.home         = home; 
		this.create       = create;
		this.consultation = consultation;
		this.details      = details;
		this.update       = update;
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
	
	// No annotation for tpl 
	public Result update() {
		return ok(update.render());
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
						controllers.authorisation.routes.javascript.User.get(),
						controllers.sra.studies.api.routes.javascript.StudyComments.save(),
						controllers.sra.studies.api.routes.javascript.StudyComments.update(),
						controllers.sra.studies.api.routes.javascript.StudyComments.delete(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.list(),
  	    				controllers.sra.experiments.api.routes.javascript.Experiments.get(),
  	    				controllers.sra.analyzes.api.routes.javascript.Analyzes.list(),
  	    				controllers.sra.analyzes.api.routes.javascript.Analyzes.get(),
  	    				controllers.sra.submissions.api.routes.javascript.Submissions.createFromStudyRelease(),
  	    				controllers.sra.submissions.api.routes.javascript.Submissions.createForUpdate(),
  	    				controllers.sra.submissions.api.routes.javascript.Submissions.updateState()
  	    				);
	}
	
}
