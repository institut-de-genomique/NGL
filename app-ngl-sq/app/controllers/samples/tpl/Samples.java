package controllers.samples.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;

import views.html.samples.details;
import views.html.samples.home;
import views.html.samples.search;

public class Samples extends NGLController implements NGLJavascript {

	private final home home;
	private final search search;
	private final details details;
	
	@Inject
	public Samples(NGLApplication app, home home, search search, details details) {
		super(app);
		this.home = home;
		this.search = search;
		this.details = details;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result get(String code) {
		return ok(home.render("search"));
	}

	// tpl
	public Result search() {
		return ok(search.render());
	}

	// tpl
	public Result details() {
		return ok(details.render());
	}

	// tpl
	// FDS reordered routes...
	public Result javascriptRoutes() {
		return jsRoutes(
				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
				controllers.commons.api.routes.javascript.Values.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.commons.api.routes.javascript.Users.list(),
				controllers.commons.api.routes.javascript.Parameters.list(),
				controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
				
				controllers.projects.api.routes.javascript.Projects.list(),
				
				controllers.samples.api.routes.javascript.Samples.list(),
				controllers.samples.api.routes.javascript.Samples.get(),
				controllers.samples.api.routes.javascript.Samples.update(),
				controllers.samples.tpl.routes.javascript.Samples.get(),
				controllers.samples.tpl.routes.javascript.Samples.search(),
				controllers.samples.tpl.routes.javascript.Samples.details(),
				controllers.samples.tpl.routes.javascript.Samples.home(),

				controllers.processes.api.routes.javascript.ProcessTypes.list(),
				controllers.processes.api.routes.javascript.Processes.list(),
				controllers.processes.api.routes.javascript.ProcessCategories.list(),
				controllers.processes.tpl.routes.javascript.Processes.home(),
				
				controllers.containers.api.routes.javascript.ContainerSupportCategories.list(),
				controllers.containers.api.routes.javascript.Containers.list(),
				controllers.containers.api.routes.javascript.Containers.get(),
				
				controllers.experiments.api.routes.javascript.Experiments.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
				controllers.experiments.tpl.routes.javascript.Experiments.get(),
				
				controllers.reporting.api.routes.javascript.FilteringConfigurations.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.get(),

				controllers.resolutions.api.routes.javascript.Resolutions.list(),

				controllers.protocols.api.routes.javascript.Protocols.list()
				);
	}
	
}
