package controllers.authorisation.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import play.mvc.Result;
import views.html.authorisation.users.*;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;

/**
 * 
 * @author michieli
 *
 */
public class Users extends NGLController 
                  implements NGLJavascript { 
	
	private final home home;
	private final search search;
	
	@Inject
	public Users(NGLApplication app, home home, search search) {
		super(app);
		this.home = home;
		this.search= search;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode){
		return ok(home.render(homecode));
	}

	public Result search(){
		return ok(search.render());
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.authorisation.tpl.routes.javascript.Users.home(),
						controllers.commons.api.routes.javascript.Users.list(),
						controllers.commons.api.routes.javascript.Roles.list(),
						controllers.commons.api.routes.javascript.Users.update());
	}

	
}
