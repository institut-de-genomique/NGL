package controllers.containers.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.containerSupports.details;
import views.html.containerSupports.home;
import views.html.containerSupports.homeScanner;
import views.html.containerSupports.search;

public class ContainerSupports extends NGLController implements NGLJavascript {
	
	private final home        home;
	private final details     details;
	private final homeScanner homeScanner;
	
	@Inject
	public ContainerSupports(NGLApplication app, home home, details details, homeScanner homeScanner) {
		super(app);
		this.home        = home;
		this.details     = details;
		this.homeScanner = homeScanner;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {		
		return ok(home.render(code));		
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result homeScanner() {		
		return ok(homeScanner.render());		
	}
	
	// No annotation for tpl
	public Result search() {
		return ok(search.render());
	}
	
	// No annotation for tpl
	public Result get(String code) {
		return ok(home.render("search"));
	}
	
	// No annotation for tpl
	public Result details() {
		return ok(details.render());
	}
	
	// No annotation for tpl
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.containers.tpl.routes.javascript.ContainerSupports.search(),
  	    				controllers.containers.tpl.routes.javascript.ContainerSupports.home(),
  	    				controllers.printing.tpl.routes.javascript.Printing.home(),
  	    				controllers.containers.tpl.routes.javascript.ContainerSupports.homeScanner(),
  	    				controllers.containers.tpl.routes.javascript.ContainerSupports.details(),
  	    				controllers.containers.api.routes.javascript.ContainerSupports.list(),
  	    				controllers.containers.tpl.routes.javascript.ContainerSupports.get(),
  	    				controllers.containers.api.routes.javascript.ContainerSupports.get(),
  	    				controllers.containers.api.routes.javascript.ContainerSupports.update(),        // 26/05/2016 NLG-825
  	    				controllers.containers.api.routes.javascript.ContainerSupportCategories.list(),
  	    				controllers.containers.api.routes.javascript.ContainerSupports.updateStateBatch(),
  	    				controllers.projects.api.routes.javascript.Projects.list(),
  	    				controllers.samples.api.routes.javascript.Samples.list(),
  	    				controllers.samples.api.routes.javascript.Samples.save(),
  	    				controllers.containers.api.routes.javascript.Containers.list(),
  	    				controllers.experiments.api.routes.javascript.ExperimentTypes.list(),  	    		
  	    				controllers.commons.api.routes.javascript.States.list(),
  	    				controllers.processes.api.routes.javascript.ProcessTypes.list(),
  	    				controllers.processes.api.routes.javascript.ProcessCategories.list(),
  	    				controllers.containers.api.routes.javascript.ContainerCategories.list(),
  	    				controllers.commons.api.routes.javascript.Users.list(),
  	    				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
  	    				controllers.containers.tpl.routes.javascript.Containers.get());
  	}
		
}
