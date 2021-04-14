package controllers.reagents.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.catalogs.home;

// public class BoxCatalogs extends -CommonController {
public class BoxCatalogs extends NGLController implements NGLJavascript { // NGLBaseController {

	private final home home;
	
	@Inject
	public BoxCatalogs(NGLApplication app, home home) {
		super(app);
		this.home = home;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}

	public Result get(String code) {
		return ok(home.render(code));
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(controllers.reagents.api.routes.javascript.BoxCatalogs.list());
	}

	/*
	public Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(	  	      
				// Routes.javascriptRouter("jsRoutes",
				JavaScriptReverseRouter.create("jsRoutes",
						// Routes	
						controllers.reagents.api.routes.javascript.BoxCatalogs.list()
						)
				);
	}
*/
	
}
