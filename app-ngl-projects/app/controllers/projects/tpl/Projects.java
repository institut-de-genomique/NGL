package controllers.projects.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.support.NGLJavascript;
//import play.Routes;
// import play.routing.JavaScriptReverseRouter;

import play.mvc.Result;
import views.html.projects.*;

/**
 * Controller around Project object
 * @author dnoisett
 *
 */
// import controllers.CommonController;
// public class Projects extends CommonController {
public class Projects extends NGLController
                     implements NGLJavascript {
	
	private home home;
//	private NGLConfig config;
	
	@Inject
	public Projects(NGLApplication app, home home) {
		super(app);
		this.home = home;
//		this.config=app.nglConfig();
	}
	
	@Authenticated
	@Historized
	@Authorized.Read	
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result get(String code) {
		return ok(home.render("search")); 
	}
	
	public Result search(String type) {
		return ok(search.render(Boolean.TRUE));
	}

	public Result details() {
		//TODO EJACOBY AD
		//return ok(details.render(config.getActiveDirectoryDefaultGroupAccess(), config.getActiveDirectoryOrganozationUnitUsersName()));
		return ok(details.render());
	}
		
	public Result javascriptRoutes() {
  	    return jsRoutes(controllers.projects.tpl.routes.javascript.Projects.home(),  
  	    				controllers.projects.tpl.routes.javascript.Projects.get(), 
  	    				controllers.projects.tpl.routes.javascript.Projects.search(),
  	    				controllers.projects.api.routes.javascript.Projects.get(),
  	    				controllers.projects.api.routes.javascript.Projects.update(),
  	    				controllers.projects.api.routes.javascript.Projects.list(),
  	    				controllers.projects.api.routes.javascript.ProjectBioinformaticParameters.list(),
  	    				controllers.projects.api.routes.javascript.Projects.save(),
  	    				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
  	    				controllers.commons.api.routes.javascript.Values.list(),
  	    				controllers.commons.api.routes.javascript.States.list(),
  	    				//TODO EJACOBY AD
  	    				//controllers.commons.api.routes.javascript.Users.list(),
  	    				controllers.projects.api.routes.javascript.ProjectTypes.list(),
  	    				controllers.projects.api.routes.javascript.ProjectCategories.list(),
  	    				controllers.projects.api.routes.javascript.UmbrellaProjects.list());
  	    				//TODO EJACOBY AD
  	    				/*controllers.projects.api.routes.javascript.MembersProjects.update(),
  	    				//controllers.projects.api.routes.javascript.MembersProjects.get(),
  	    				//controllers.projects.api.routes.javascript.UserMembersProjects.get(),
  	    				//controllers.projects.api.routes.javascript.UserMembersProjects.list(),
  	    				//controllers.authorisation.routes.javascript.User.get());*/
  	  }
	
//	public Result javascriptRoutes() {
//  	    response().setContentType("text/javascript");
//  	    return ok(  	    		
//  	      //Routes.javascriptRouter("jsRoutes",
//  	    		JavaScriptReverseRouter.create("jsRoutes",
//  	    				// Routes
//  	    		controllers.projects.tpl.routes.javascript.Projects.home(),  
//  	    		controllers.projects.tpl.routes.javascript.Projects.get(), 
//  	    		controllers.projects.tpl.routes.javascript.Projects.search(),
//  	    		controllers.projects.api.routes.javascript.Projects.get(),
//  	    		controllers.projects.api.routes.javascript.Projects.update(),
//  	    		controllers.projects.api.routes.javascript.Projects.list(),
//  	    		controllers.projects.api.routes.javascript.ProjectBioinformaticParameters.list(),
//  	    		controllers.projects.api.routes.javascript.Projects.save(),
//  	    		controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
//  	    		controllers.commons.api.routes.javascript.States.list(),
//  	    		controllers.projects.api.routes.javascript.ProjectTypes.list(),
//  	    		controllers.projects.api.routes.javascript.ProjectCategories.list(),
//  	    		controllers.projects.api.routes.javascript.UmbrellaProjects.list()
//  	    	)	  	      
//  	    );
//  	  }
	
}

