package controllers.barcodes.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.barcodes.create;
import views.html.barcodes.home;
import views.html.barcodes.search;

//import controllers.CommonController;
//public class Barcodes extends CommonController {

public class Barcodes extends NGLController
                     implements NGLJavascript {
	
	private home   home;
	private create create;
	private search search;
	
	@Inject
	public Barcodes(NGLApplication app, home home, create create, search search) {
		super (app);
		this.home   = home;
		this.create = create;
		this.search = search;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	public Result create() {
		return ok(create.render());
	}

	public Result search() {
		return ok(search.render());
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.barcodes.tpl.routes.javascript.Barcodes.home() ,
						controllers.barcodes.api.routes.javascript.Barcodes.list(),
						controllers.barcodes.api.routes.javascript.Barcodes.save(),
						controllers.barcodes.api.routes.javascript.Barcodes.delete(),
						controllers.combo.api.routes.javascript.Lists.projects(),
						controllers.combo.api.routes.javascript.Lists.etmanips());
	}
	
	/*public Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(  	    		
				//Routes.javascriptRouter("jsRoutes",
				JavaScriptReverseRouter.create("jsRoutes",
						// Routes
						controllers.barcodes.tpl.routes.javascript.Barcodes.home() ,
						controllers.barcodes.api.routes.javascript.Barcodes.list(),
						controllers.barcodes.api.routes.javascript.Barcodes.save(),
						controllers.barcodes.api.routes.javascript.Barcodes.delete(),
						controllers.combo.api.routes.javascript.Lists.projects(),
						controllers.combo.api.routes.javascript.Lists.etmanips()
						)	  	      
				);
	}*/
	
}
