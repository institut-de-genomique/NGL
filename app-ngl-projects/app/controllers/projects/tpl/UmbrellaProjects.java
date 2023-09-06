package controllers.projects.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.umbrellaprojects.*;

/**
 * Controller around UmbrellaProject object
 * 
 * @author dnoisett
 */
public class UmbrellaProjects extends NGLController implements NGLJavascript {
	
	private home home;
	
	@Inject
	public UmbrellaProjects(NGLApplication app, home home) {
		super(app);
		this.home = home;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read	
	public Result home(String homecode) {
		if (getConfig().isCNSInstitute()) {
			return ok(home.render(homecode));
		}

		return ok();
	}
	
	public Result get(String code) {
		if (getConfig().isCNSInstitute()) {
			return ok(home.render("search")); 
		}

		return ok();
	}
	
	// No annotation for tpl
	public Result search(String type) {
		if (getConfig().isCNSInstitute()) {
			return ok(search.render(Boolean.TRUE));
		}

		return ok();
	}

	// No annotation for tpl
	public Result details(String typeForm) {
		if (getConfig().isCNSInstitute()) {
			return ok(details.render(typeForm));
		}

		return ok();
	}
	
	// No annotation for tpl
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.projects.tpl.routes.javascript.Projects.home(),
						controllers.projects.tpl.routes.javascript.UmbrellaProjects.home(),  
  	    				controllers.projects.tpl.routes.javascript.UmbrellaProjects.get(), 
  	    				controllers.projects.tpl.routes.javascript.UmbrellaProjects.search(),
  	    				controllers.projects.tpl.routes.javascript.UmbrellaProjects.details(),
  	    				controllers.projects.api.routes.javascript.UmbrellaProjects.get(),
  	    				controllers.projects.api.routes.javascript.UmbrellaProjects.update(),
  	    				controllers.projects.api.routes.javascript.UmbrellaProjects.list(),
  	    				controllers.projects.api.routes.javascript.UmbrellaProjects.save(),
						controllers.projects.api.routes.javascript.Projects.list(),
						controllers.projects.api.routes.javascript.UmbrellaProjectComments.save(),
						controllers.projects.api.routes.javascript.UmbrellaProjectComments.update(),
						controllers.projects.api.routes.javascript.UmbrellaProjectComments.delete(),
						controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
						controllers.reporting.api.routes.javascript.ReportingConfigurations.get()
  	    		);
  	  }
}