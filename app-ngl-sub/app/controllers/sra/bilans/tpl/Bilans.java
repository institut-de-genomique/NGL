package controllers.sra.bilans.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.bilans.consultation;
import views.html.bilans.home;

// import controllers.CommonController;                  // done
//public class Submissions extends -CommonController {   // done
public class Bilans extends NGLController implements NGLJavascript  { // NGLBaseController {
	
	private final home         home;
	private final consultation consultation;
	
	@Inject
	public Bilans(NGLApplication app,  home home, consultation consultation) {
		super(app);
		this.home         = home;
		this.consultation = consultation;	
	}
	
//	@Authenticated
//	@Historized
//	// @Authorized.Read 
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	

	
	// @Authenticated
	// @Historized
	// @Authorized.Read 
	// No annotation for tpl 
	public Result consultation()	{
		return ok(consultation.render());
	}

	
	// No annotation for tpl 
	public Result javascriptRoutes() {
		return jsRoutes(controllers.sra.bilans.tpl.routes.javascript.Bilans.home(),
						controllers.sra.bilans.tpl.routes.javascript.Bilans.consultation(),
						controllers.projects.api.routes.javascript.Projects.list(),
					    controllers.commons.api.routes.javascript.States.list(),
					    controllers.readsets.api.routes.javascript.ReadSets.list(),	
					    controllers.readsets.api.routes.javascript.ReadSets.get(),		
					    controllers.sra.studies.api.routes.javascript.Studies.get(),
					    controllers.sra.studies.api.routes.javascript.Studies.list(),
						controllers.sra.samples.api.routes.javascript.Samples.list(),
						controllers.sra.samples.api.routes.javascript.Samples.get(),
						controllers.sra.samples.api.routes.javascript.Samples.update(),						
						controllers.sra.experiments.api.routes.javascript.Experiments.get(),
						controllers.sra.experiments.tpl.routes.javascript.Experiments.get(),
						controllers.sra.experiments.api.routes.javascript.Experiments.list(),
						controllers.sra.experiments.api.routes.javascript.Experiments.update(),
				   		controllers.sra.analyzes.api.routes.javascript.Analyzes.get(),
				   		controllers.sra.analyzes.api.routes.javascript.Analyzes.list());
	}
}