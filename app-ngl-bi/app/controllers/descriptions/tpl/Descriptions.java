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
import views.html.descriptions.treatments;
import views.html.descriptions.types;

public class Descriptions extends NGLController implements NGLJavascript, NGLForms {
	
	private final home home;
	private final treatments treatments;
	private final types types;

	@Inject
	public Descriptions(NGLApplication app, home home, treatments treatments, types types) {
		super(app);
		this.home = home;
		this.treatments = treatments;
		this.types = types;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}
	
	public Result treatments(){
		return ok(treatments.render());
	}

	public Result types(){
		return ok(types.render());
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(
				controllers.descriptions.tpl.routes.javascript.Descriptions.home(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.treatments(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.types(),
				controllers.treatmenttypes.api.routes.javascript.TreatmentTypes.list(),
				controllers.treatmenttypes.api.routes.javascript.TreatmentCategories.list(), // ajout NGL-3530
				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
				controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
				controllers.runs.api.routes.javascript.RunTypes.list()
				);
	}

}
