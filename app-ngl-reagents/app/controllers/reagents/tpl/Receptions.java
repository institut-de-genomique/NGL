package controllers.reagents.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.receptions.create;
import views.html.receptions.details;
import views.html.receptions.home;
import views.html.receptions.newFromFile;
import views.html.receptions.search;

public class Receptions extends NGLController implements NGLJavascript {

	private final home home;
	private final search search;
	private final create create;
	private final newFromFile newFromFile;
	private final details details;

	@Inject
	public Receptions(NGLApplication app, home home, search search, create create, newFromFile newFromFile, details details) {
		super(app);
		this.home = home;
		this.search = search;
		this.create = create;
		this.newFromFile = newFromFile;
		this.details = details;
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(this.home.render(code));
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result get(String code) {
		return ok(home.render("search"));
	}

	public Result search() {
		return ok(this.search.render());
	}

	public Result create() {
		return ok(this.create.render());
	}

	public Result newFromFile() {
		return ok(this.newFromFile.render());
	}
	
	public Result details() {
		return ok(details.render());
	}

	public Result javascriptRoutes() {
		return this.jsRoutes(controllers.reagents.tpl.routes.javascript.Receptions.home(),
				controllers.reagents.tpl.routes.javascript.Receptions.get(),
				controllers.reagents.tpl.routes.javascript.Receptions.search(),
				controllers.reagents.tpl.routes.javascript.Receptions.create(),
				controllers.reagents.tpl.routes.javascript.Receptions.newFromFile(),
				controllers.reagents.api.routes.javascript.Receptions.list(),
				controllers.reagents.api.routes.javascript.Receptions.get(),
				controllers.reagents.api.routes.javascript.Receptions.save(),
				controllers.reagents.api.routes.javascript.Receptions.update(),
				controllers.reagents.api.routes.javascript.Receptions.updateState(),
				controllers.reagents.api.routes.javascript.Receptions.updateStateBatch(),
				controllers.reagents.io.routes.javascript.Receptions.importFile(),
				controllers.commons.api.routes.javascript.States.list(),
				controllers.commons.api.routes.javascript.Users.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypes.list());
	}

}
