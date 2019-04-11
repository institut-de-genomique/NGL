package controllers.plates.tpl;

//import java.util.ArrayList;
//import java.util.List;
import javax.inject.Inject;

import controllers.NGLController;
// import controllers.plates.tpl.routes.javascript;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
//import play.Routes;
// import play.routing.JavaScriptReverseRouter;
import play.mvc.Result;

import views.html.plates.home;
import views.html.plates.searchManips;
import views.html.plates.fromFile;
import views.html.plates.search;
import views.html.plates.details;

//import controllers.CommonController;
//public class Plates extends CommonController {
public class Plates extends NGLController
                   implements NGLJavascript {

	private home         home;
	private searchManips searchManips;
	private fromFile     fromFile;
	private search       search;
	private details      details;
	
	@Inject
	public Plates(NGLApplication app, home home, searchManips searchManips, fromFile fromFile, search search, details details) {
		super(app);
		this.home         = home;
		this.searchManips = searchManips;
		this.fromFile     = fromFile;
		this.search       = search;
		this.details      = details;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	public Result get(String code) {
		return ok(home.render("search"));
	}

	public Result searchManips() {
		return ok(searchManips.render());
	}

	public Result fromFile() {
		return ok(fromFile.render());
	}

	public Result search(){
		return ok(search.render());
	}

	public Result details(){
		return ok(details.render());
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.plates.tpl.routes.javascript.Plates.home() ,
						controllers.plates.tpl.routes.javascript.Plates.get(),
						controllers.plates.tpl.routes.javascript.Plates.details() ,
						controllers.plates.api.routes.javascript.Plates.list(),
						controllers.plates.api.routes.javascript.Plates.get(),
						controllers.plates.api.routes.javascript.Plates.save(),
						controllers.plates.api.routes.javascript.Plates.remove(),
						controllers.plates.io.routes.javascript.Plates.importFile(),
						controllers.manips.api.routes.javascript.Manips.list(),
						controllers.combo.api.routes.javascript.Lists.projects(),
						controllers.combo.api.routes.javascript.Lists.etmateriels(),
						controllers.combo.api.routes.javascript.Lists.etmanips(),
						controllers.combo.api.routes.javascript.Lists.users());
	}

//	public Result javascriptRoutes() {
//		response().setContentType("text/javascript");
//		return ok(  	    		
//				JavaScriptReverseRouter.create("jsRoutes",
//						//Routes.javascriptRouter("jsRoutes",
//						// Routes
//						controllers.plates.tpl.routes.javascript.Plates.home() ,
//						controllers.plates.tpl.routes.javascript.Plates.get(),
//						controllers.plates.tpl.routes.javascript.Plates.details() ,
//						controllers.plates.api.routes.javascript.Plates.list(),
//						controllers.plates.api.routes.javascript.Plates.get(),
//						controllers.plates.api.routes.javascript.Plates.save(),
//						controllers.plates.api.routes.javascript.Plates.remove(),
//						controllers.plates.io.routes.javascript.Plates.importFile(),
//						controllers.manips.api.routes.javascript.Manips.list(),
//						controllers.combo.api.routes.javascript.Lists.projects(),
//						controllers.combo.api.routes.javascript.Lists.etmateriels(),
//						controllers.combo.api.routes.javascript.Lists.etmanips(),
//						controllers.combo.api.routes.javascript.Lists.users()
//						)	  	      
//				);
//	}
	
}
