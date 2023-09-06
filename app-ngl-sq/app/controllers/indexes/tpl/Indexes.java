package controllers.indexes.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;

// import 
//    views/indexes/home.scala.html 
//    views/indexes/search.scala.html
//    views/indexes/details.scala.html 
// NOTE: meme si eclipse affiche des warnings pour ces imports ce n'est pas grave...
import views.html.indexes.home;
import views.html.indexes.search;
import views.html.indexes.details;

/**
 * @author fdsantos based on Samples.java
 * @since 19/11/2018
 */
public class Indexes extends NGLController implements NGLJavascript {

	private final home home;
	private final search search;
	private final details details;
	
	@Inject
    public Indexes(NGLApplication app, home home, search search, details details) {
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
	// liste copiee de samples, tout necesssaire ???
	public Result javascriptRoutes() {
		return jsRoutes(

				controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.commons.api.routes.javascript.Users.list(),
				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
				controllers.commons.api.routes.javascript.Values.list(),
				controllers.commons.api.routes.javascript.Parameters.list(),  // pour l'instant on réutilise l'API de Parameters. _FDS_: séparer les index des autres parametres
				
				controllers.projects.api.routes.javascript.Projects.list(),

				controllers.processes.api.routes.javascript.ProcessCategories.list(),
				controllers.processes.api.routes.javascript.ProcessTypes.list(),
				controllers.processes.api.routes.javascript.Processes.list(),
				controllers.processes.tpl.routes.javascript.Processes.home(),

				controllers.containers.api.routes.javascript.Containers.list(),
				controllers.containers.api.routes.javascript.Containers.get(),
				controllers.containers.api.routes.javascript.ContainerSupportCategories.list(),

				controllers.samples.api.routes.javascript.Samples.list(),
				controllers.samples.api.routes.javascript.Samples.get(),
				controllers.samples.api.routes.javascript.Samples.update(),
				controllers.samples.tpl.routes.javascript.Samples.get(),
				controllers.samples.tpl.routes.javascript.Samples.search(),
				controllers.samples.tpl.routes.javascript.Samples.details(),
				controllers.samples.tpl.routes.javascript.Samples.home(),

				controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
				controllers.experiments.api.routes.javascript.Experiments.list(),
				controllers.experiments.tpl.routes.javascript.Experiments.get(),
				
				controllers.indexes.tpl.routes.javascript.Indexes.home(),
				controllers.indexes.tpl.routes.javascript.Indexes.search(),
				controllers.indexes.tpl.routes.javascript.Indexes.get(),
				controllers.indexes.tpl.routes.javascript.Indexes.details(),
						
				controllers.reporting.api.routes.javascript.FilteringConfigurations.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.get(),
				
				controllers.resolutions.api.routes.javascript.Resolutions.list(),
				
				controllers.protocols.api.routes.javascript.Protocols.list()
				);
	}
}