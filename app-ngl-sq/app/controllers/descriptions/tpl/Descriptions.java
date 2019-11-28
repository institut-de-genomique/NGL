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
import views.html.descriptions.propertydefinitions;
import views.html.descriptions.protocols;
import views.html.descriptions.experiments;

public class Descriptions extends NGLController implements NGLJavascript, NGLForms {
	
	private final home home;
	private final instruments instruments;
	private final propertydefinitions propertydefinitions;
	private final protocols protocols;
	private final experiments experiments;

	@Inject
	public Descriptions(NGLApplication app, home home, instruments instruments, propertydefinitions propertydefinitions, protocols protocols, experiments experiments) {
		super(app);
		this.home = home;
		this.instruments = instruments;
		this.propertydefinitions = propertydefinitions;
		this.protocols = protocols;
		this.experiments = experiments;
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
	
	public Result propertyDefinitions() {
		return ok(propertydefinitions.render());
	}
	
	public Result protocols() {
		return ok(protocols.render());
	}

	public Result experiments(){
		return ok(experiments.render());
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(
				controllers.descriptions.tpl.routes.javascript.Descriptions.home(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.instruments(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.propertyDefinitions(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.protocols(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.experiments(),
				controllers.instruments.api.routes.javascript.InstrumentUsedTypes.list(),
				controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
				controllers.samples.api.routes.javascript.ImportTypes.list(),
				controllers.protocols.api.routes.javascript.Protocols.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypeNodes.list()
				);
				
	}

}
