package controllers.sra.samples.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.samples.create;
import views.html.samples.consultation;
import views.html.samples.home;
import views.html.samples.update;
import views.html.samples.details;

public class Samples extends NGLController implements NGLJavascript  {
	
	private final home home;
	private final create create;
	private final consultation consultation;
	private final details details;
	private final update update;

	
	@Inject
	public Samples(NGLApplication app, home home, create create, consultation consultation, details details, update update) {
		super(app);
		this.home         = home; 
		this.create       = create;
		this.consultation = consultation;
		this.details      = details;
		this.update       = update;
	}
	
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	public Result consultation() {
		return ok(consultation.render());
	}
	
	public Result create() {
		return ok(create.render());
	}	

	public Result get(String code) {
		return ok(home.render("search"));
	}
	
	// No annotation for tpl 
	public Result update() {
		return ok(update.render());
	}
	
	// No annotation for tpl 
	public Result details() {
		return ok(details.render());
	}	
	
	
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
				controllers.sra.samples.api.routes.javascript.Samples.save(),
				controllers.sra.samples.api.routes.javascript.Samples.loadUserFileSample(),
				controllers.authorisation.routes.javascript.User.get(),
				controllers.sra.samples.api.routes.javascript.SampleComments.save(),
				controllers.sra.samples.api.routes.javascript.SampleComments.update(),
				controllers.sra.samples.api.routes.javascript.SampleComments.delete(),
				controllers.sra.experiments.api.routes.javascript.Experiments.list(),
  				controllers.sra.experiments.api.routes.javascript.Experiments.get(),
  				controllers.sra.analyzes.api.routes.javascript.Analyzes.list(),
  				controllers.sra.analyzes.api.routes.javascript.Analyzes.get(),
				controllers.sra.submissions.api.routes.javascript.Submissions.createForUpdate(),
  				controllers.sra.submissions.api.routes.javascript.Submissions.updateState());
	}
}