package controllers.balancesheets.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.balancesheets.general;
import views.html.balancesheets.home;
import views.html.balancesheets.year;

public class BalanceSheets extends NGLController 
                          implements NGLJavascript {
	
	private final home    home;
	private final year    year;
	private final general general;
	
	@Inject
	public BalanceSheets(NGLApplication app, home home, year year, general general) {
		super(app);
		this.home    = home;
		this.year    = year;
		this.general = general;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String homecode, String year) {
		return ok(home.render(homecode, year));
	}
	
	public Result year() {
		return ok(year.render());
	}
	
	public Result general() {
		return ok(general.render());
	}
	
	public Result javascriptRoutes() {
		return jsRoutes(controllers.balancesheets.tpl.routes.javascript.BalanceSheets.home(),
						controllers.balancesheets.tpl.routes.javascript.BalanceSheets.year(),
						controllers.balancesheets.tpl.routes.javascript.BalanceSheets.general(),
						controllers.balancesheets.api.routes.javascript.BalanceSheets.list(),
						controllers.balancesheets.api.routes.javascript.BalanceSheets.excelReport(),
						controllers.readsets.api.routes.javascript.ReadSets.list(),
						controllers.runs.api.routes.javascript.Runs.list(),
						controllers.projects.api.routes.javascript.Projects.list(),
						controllers.sampletypes.api.routes.javascript.SampleTypes.list(),
						controllers.commons.api.routes.javascript.CommonInfoTypes.list());
	}
	
	
}
