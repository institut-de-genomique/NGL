package controllers.reagents.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.declarations.home;
import views.html.declarations.kitsCreation;
import views.html.declarations.kitsSearch;

//import controllers.CommonController;
// public class Kits extends -CommonController {
public class Kits extends NGLController implements NGLJavascript { // NGLBaseController {
	
	private final home         home;
	private final kitsSearch   kitsSearch;
	private final kitsCreation kitsCreation;
	
	@Inject
	public Kits(NGLApplication app, home home, kitsSearch kitsSearch, kitsCreation kitsCreation) {
		super(app);
		this.home         = home;
		this.kitsSearch   = kitsSearch;
		this.kitsCreation = kitsCreation;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code + ".kits"));
	}
	
	public Result search() {
		return ok(kitsSearch.render());
	}
	
	public Result get(String code) {
		return ok(home.render(code));
	}
	
	public Result createOrEdit() {
		return ok(kitsCreation.render());
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.reagents.tpl.routes.javascript.Orders.createOrEdit(),
						controllers.reagents.tpl.routes.javascript.Kits.createOrEdit(),
						controllers.reagents.tpl.routes.javascript.Kits.get(),
						controllers.reagents.api.routes.javascript.Kits.save(),
						controllers.reagents.api.routes.javascript.KitCatalogs.list(),
						controllers.reagents.api.routes.javascript.BoxCatalogs.list(),
						controllers.reagents.api.routes.javascript.ReagentCatalogs.list(),
						controllers.reagents.api.routes.javascript.Kits.delete(),
						controllers.reagents.api.routes.javascript.Kits.list(),
						controllers.reagents.api.routes.javascript.Boxes.list(),
						controllers.reagents.api.routes.javascript.Boxes.save(),
						controllers.reagents.api.routes.javascript.Boxes.update(),
						controllers.reagents.api.routes.javascript.Boxes.delete(),
						controllers.reagents.api.routes.javascript.Kits.update(),
						controllers.reagents.api.routes.javascript.Kits.get(),
						controllers.reagents.tpl.routes.javascript.Kits.home(),
						controllers.reagents.tpl.routes.javascript.Boxes.home(),
						controllers.reagents.tpl.routes.javascript.Boxes.search(),
						controllers.reagents.tpl.routes.javascript.Kits.search(),
						controllers.reagents.api.routes.javascript.Reagents.save(),
						controllers.reagents.api.routes.javascript.Reagents.update(),
						controllers.reagents.api.routes.javascript.Reagents.delete(),
						controllers.reagents.api.routes.javascript.Reagents.list(),
		  	    		controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
		  	    		controllers.commons.api.routes.javascript.States.list());
	}
	
	/*
	public Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(	  	      
				// Routes.javascriptRouter("jsRoutes",
				 JavaScriptReverseRouter.create("jsRoutes",
						// Routes	
						controllers.reagents.tpl.routes.javascript.Orders.createOrEdit(),
						controllers.reagents.tpl.routes.javascript.Kits.createOrEdit(),
						controllers.reagents.tpl.routes.javascript.Kits.get(),
						controllers.reagents.api.routes.javascript.Kits.save(),
						controllers.reagents.api.routes.javascript.KitCatalogs.list(),
						controllers.reagents.api.routes.javascript.BoxCatalogs.list(),
						controllers.reagents.api.routes.javascript.ReagentCatalogs.list(),
						controllers.reagents.api.routes.javascript.Kits.delete(),
						controllers.reagents.api.routes.javascript.Kits.list(),
						controllers.reagents.api.routes.javascript.Boxes.list(),
						controllers.reagents.api.routes.javascript.Boxes.save(),
						controllers.reagents.api.routes.javascript.Boxes.update(),
						controllers.reagents.api.routes.javascript.Boxes.delete(),
						controllers.reagents.api.routes.javascript.Kits.update(),
						controllers.reagents.api.routes.javascript.Kits.get(),
						controllers.reagents.tpl.routes.javascript.Kits.home(),
						controllers.reagents.tpl.routes.javascript.Boxes.home(),
						controllers.reagents.tpl.routes.javascript.Boxes.search(),
						controllers.reagents.tpl.routes.javascript.Kits.search(),
						controllers.reagents.api.routes.javascript.Reagents.save(),
						controllers.reagents.api.routes.javascript.Reagents.update(),
						controllers.reagents.api.routes.javascript.Reagents.delete(),
						controllers.reagents.api.routes.javascript.Reagents.list(),
		  	    		controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
		  	    		controllers.commons.api.routes.javascript.States.list()
						)
				);
	}*/
	
}
