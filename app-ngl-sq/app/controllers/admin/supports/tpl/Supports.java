package controllers.admin.supports.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.admin.supports.contentUpdate;
import views.html.admin.supports.home;
import views.html.admin.supports.searchSwitchIndex;

public class Supports extends NGLController implements NGLJavascript {

	private final home              home;
	private final searchSwitchIndex searchSwitchIndex;
	
	@Inject
	public Supports(NGLApplication app, home home, searchSwitchIndex searchSwitchIndex) {
		super(app);
		this.home              = home;
		this.searchSwitchIndex = searchSwitchIndex;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}
	
	// No annotation for tpl
	public Result search(String code) {
		if ("switch-index".equals(code)) {
			return ok(searchSwitchIndex.render());
		} else if("content-update".equals(code)) {
			return ok(contentUpdate.render());
		} else {
			return ok();
		}
	}

	// No annotation for tpl
	public Result javascriptRoutes() {
		return jsRoutes(controllers.projects.api.routes.javascript.Projects.list(),
						controllers.samples.api.routes.javascript.Samples.list(),
						controllers.containers.api.routes.javascript.Containers.list(),
						controllers.containers.api.routes.javascript.ContainerSupports.list(),
						controllers.experiments.api.routes.javascript.Experiments.list(),
						controllers.commons.api.routes.javascript.States.list(),
						controllers.readsets.api.routes.javascript.ReadSets.list(),
						controllers.commons.api.routes.javascript.Users.list(),
						controllers.commons.api.routes.javascript.Values.list(),
						controllers.commons.api.routes.javascript.Parameters.list(),
						controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
						controllers.admin.supports.tpl.routes.javascript.Supports.home(),
						controllers.admin.supports.tpl.routes.javascript.Supports.search(),
						controllers.admin.supports.api.routes.javascript.NGLObjects.list(),
						controllers.admin.supports.api.routes.javascript.NGLObjects.update());
	}
	
}
