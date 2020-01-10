package controllers.descriptions.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.descriptions.home;
import views.html.descriptions.instruments;

public class Descriptions extends NGLController implements NGLJavascript, NGLForms {
	
	private final home home;
	private final instruments instruments;

	@Inject
	public Descriptions(NGLApplication app, home home, instruments instruments) {
		super(app);
		this.home = home;
		this.instruments = instruments;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}
	
	public Result instruments(){
		return ok(instruments.render());
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(
				controllers.descriptions.tpl.routes.javascript.Descriptions.home(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.instruments(),
				controllers.instruments.api.routes.javascript.InstrumentUsedTypes.list()
				);
				
	}

}
