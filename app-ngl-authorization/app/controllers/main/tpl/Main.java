package controllers.main.tpl;

import play.Logger;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.index;
import play.mvc.Http.Context;
import controllers.authorisation.Permission;


public class Main extends Controller {

  public static Result home(String id) {
    return ok(index.render("Welcome to the Next LIMS Generation",id));
  }

  public static Result javascriptRoutes() {
	  	    response().setContentType("text/javascript");
	  	    return ok(
	  	      Routes.javascriptRouter("jsRoutes",
	  	        // Routes
	  	        controllers.administration.authorisation.routes.javascript.Permissions.createOrUpdate(),
	  	        controllers.administration.authorisation.routes.javascript.Permissions.show(),
	  	        controllers.administration.authorisation.routes.javascript.Roles.createOrUpdate(),
	  	        controllers.administration.authorisation.routes.javascript.Roles.show(),
	  	        controllers.administration.authorisation.routes.javascript.Users.createOrUpdate(),
	  	        controllers.administration.authorisation.routes.javascript.Users.show(),
	  	        controllers.administration.authorisation.routes.javascript.Users.remove(),
	  	        controllers.administration.authorisation.routes.javascript.Teams.show(),
	  	        controllers.administration.authorisation.routes.javascript.Teams.createOrUpdate(),
	  	        controllers.administration.authorisation.routes.javascript.Roles.remove(),
	  	        controllers.administration.authorisation.routes.javascript.Permissions.remove(),
	  	        controllers.administration.authorisation.routes.javascript.Teams.remove(),
	  	        controllers.administration.authentication.routes.javascript.User.logOut(),
	  	      controllers.administration.authorisation.routes.javascript.Applications.createOrUpdate(),
	  	        controllers.administration.authorisation.routes.javascript.Applications.show(),
	  	      controllers.administration.authorisation.routes.javascript.Applications.remove()
	  	      )
	  	    );
	  	  }
}