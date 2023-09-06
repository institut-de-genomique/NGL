package controllers.sra.submissions.tpl;

import javax.inject.Inject;

import controllers.NGLController;
//import controllers.NGLBaseController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.submissions.activate;
import views.html.submissions.consultation;
import views.html.submissions.create;
import views.html.submissions.details;
import views.html.submissions.home;
import views.html.submissions.validation;

// import controllers.CommonController;                  // done
//public class Submissions extends -CommonController {   // done
public class Submissions extends NGLController implements NGLJavascript  { // NGLBaseController {
	
	private final home         home;
	private final create       create;
	private final details      details;
	private final activate     activate;
	private final consultation consultation;
	private final validation   validation;
	
	@Inject
	public Submissions(NGLApplication app, home home, create create, details details, activate activate, consultation consultation, validation validation) {
		super(app);
		this.home         = home;
		this.create       = create;
		this.details      = details;
		this.activate     = activate;
		this.consultation = consultation;	
		this.validation   = validation;	
	}
	
	@Authenticated
	@Historized
	// @Authorized.Read 
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	@Authenticated
	@Historized
	// @Authorized.Read 
	public Result get(String code) {
		return ok(home.render("search"));
	}
	
	@Authenticated
	@Historized
	// @Authorized.Read 
	public Result activate()	{
		return ok(activate.render());
	}	
	
	// @Authenticated
	// @Historized
	// @Authorized.Read 
	// No annotation for tpl 
	public Result consultation()	{
		return ok(consultation.render());
	}

	// @Authenticated
	// @Historized
	// @Authorized.Read 
	// No annotation for tpl 
	public Result create() {
		return ok(create.render());
	}
	
	// @Authenticated
	// @Historized
	// @Authorized.Read 
	// No annotation for tpl 
	public Result details() {
		return ok(details.render());
	}
	
	// @Authenticated
	// @Historized
	// @Authorized.Read 
	// No annotation for tpl 
	public Result validation()	{
		return ok(validation.render());
	}
	
	// No annotation for tpl 
	public Result javascriptRoutes() {
		return jsRoutes(controllers.sra.submissions.tpl.routes.javascript.Submissions.home(),
						controllers.projects.api.routes.javascript.Projects.list(),
						controllers.sra.projects.api.routes.javascript.Projects.get(),

						controllers.sra.api.routes.javascript.Variables.get(),
						controllers.sra.api.routes.javascript.Variables.list(),
						controllers.commons.api.routes.javascript.States.list(),
						controllers.sra.projects.api.routes.javascript.Projects.list(),
						controllers.sra.projects.api.routes.javascript.Projects.get(),
						controllers.sra.studies.api.routes.javascript.Studies.list(),
						controllers.sra.studies.api.routes.javascript.Studies.get(),
						controllers.sra.studies.api.routes.javascript.Studies.update(),
						controllers.sra.configurations.api.routes.javascript.Configurations.list(),
						controllers.sra.configurations.api.routes.javascript.Configurations.get(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.list(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.get(),
						controllers.sra.analyzes.api.routes.javascript.Analyzes.update(),
						controllers.readsets.api.routes.javascript.ReadSets.list(),
						controllers.sra.submissions.api.routes.javascript.Submissions.list(),
						controllers.sra.submissions.api.routes.javascript.Submissions.save(),
	    	      		controllers.sra.submissions.api.routes.javascript.SubmissionComments.save(),
	      	    		controllers.sra.submissions.api.routes.javascript.SubmissionComments.update(),
	      	    		controllers.sra.submissions.api.routes.javascript.SubmissionComments.delete(),
						controllers.sra.submissions.api.routes.javascript.Submissions.get(),
						controllers.sra.submissions.api.routes.javascript.Submissions.update(),
						controllers.sra.submissions.api.routes.javascript.Submissions.updateState(),
						controllers.sra.submissions.tpl.routes.javascript.Submissions.get(),
						controllers.sra.samples.api.routes.javascript.Samples.list(),
						controllers.sra.samples.api.routes.javascript.Samples.get(),
						controllers.sra.samples.api.routes.javascript.Samples.update(),
						controllers.sra.experiments.api.routes.javascript.Experiments.list(),
						controllers.sra.experiments.api.routes.javascript.Experiments.get(),
						controllers.sra.experiments.api.routes.javascript.Experiments.update(),
	    				controllers.sra.experiments.api.routes.javascript.ExperimentsRawDatas.delete(),
	    				controllers.authorisation.routes.javascript.User.get()
					);
	}

}
