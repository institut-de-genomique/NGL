package controllers.sra.readsets.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;


public class Readsets extends NGLController implements NGLJavascript  { // extends NGLBaseController {
	

	
	@Inject
	public Readsets(NGLApplication app) {
		super(app);
	}
	

	public Result get(String code) {
		return ok();
	}

	// No annotation for tpl 
	public Result javascriptRoutes() {
		return jsRoutes(
						controllers.sra.readsets.api.routes.javascript.Readsets.get()
						);
	}
	
  	 
}
