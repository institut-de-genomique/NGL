package controllers.projects.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;

import views.html.projects.details;
import views.html.projects.search;
import views.html.projects.home;

/**
 * Controller around Project object
 * 
 * @author dnoisett
 *
 */
public class Projects extends NGLController implements NGLJavascript {

	private final home            home;
	private NGLConfig       config;
	private final search    search;
	private final details   details;

	@Inject
	public Projects(NGLApplication app, home home, search search, details details) {
		super(app);
		this.home    = home;
		this.config  = app.nglConfig();
		this.search  = search;
		this.details = details;
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode) {
		return ok(home.render(homecode));
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result get(String code) {
		return ok(home.render("search"));
	}

	public Result search(String type) {
		return ok(search.render(Boolean.TRUE));
	}

	public Result details() {
		// EJACOBY in waiting AD CNS implementation
		if (config.isCNGInstitute()) {
			return ok(details.render(config.getActiveDirectoryDefaultGroupAccess(),
					config.getActiveDirectoryOrganizationUnitGroupsLaboName(), config.getActiveDirectoryDefaultGroupAdmin()));
		} else
			return ok(details.render(null, null, null));
	}

	public Result javascriptRoutes() {
		return jsRoutes(controllers.projects.tpl.routes.javascript.Projects.home(),
				controllers.projects.tpl.routes.javascript.Projects.get(),
				controllers.projects.tpl.routes.javascript.Projects.search(),
				controllers.projects.api.routes.javascript.Projects.get(),
				controllers.projects.api.routes.javascript.Projects.update(),
				controllers.projects.api.routes.javascript.Projects.list(),
				controllers.projects.api.routes.javascript.ProjectBioinformaticParameters.list(),
				controllers.projects.api.routes.javascript.Projects.save(),
	      		controllers.projects.api.routes.javascript.ProjectComments.save(),
  	    		controllers.projects.api.routes.javascript.ProjectComments.update(),
  	    		controllers.projects.api.routes.javascript.ProjectComments.delete(),
				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
				controllers.commons.api.routes.javascript.Values.list(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.commons.api.routes.javascript.Users.list(),
				controllers.projects.api.routes.javascript.ProjectTypes.list(),
				controllers.projects.api.routes.javascript.ProjectCategories.list(),
				controllers.projects.api.routes.javascript.UmbrellaProjects.list(),
				controllers.projects.api.routes.javascript.UmbrellaProjects.get(),
				controllers.projects.api.routes.javascript.MembersProjects.update(),
				controllers.projects.api.routes.javascript.MembersProjects.get(),
				controllers.projects.api.routes.javascript.MembersProjects.delete(),
				controllers.projects.api.routes.javascript.UserMembersProjects.get(),
				controllers.projects.api.routes.javascript.UserMembersProjects.list(),
				controllers.projects.api.routes.javascript.GroupMembersProjects.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.list(),
				controllers.reporting.api.routes.javascript.ReportingConfigurations.get(),
				controllers.reporting.api.routes.javascript.FilteringConfigurations.list(),
				controllers.reporting.api.routes.javascript.FilteringConfigurations.get(),
				controllers.authorisation.routes.javascript.User.get());
	}
}
