package controllers.reagents.tpl;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.declarations.home;
import views.html.declarations.boxesSearch;

import javax.inject.Inject;

import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;

// import controllers.CommonController;


// public class Boxes  extends -CommonController {
public class Boxes extends Controller {
	
	private final home        home;
	private final boxesSearch boxesSearch;
	
	@Inject
	public Boxes(home home, boxesSearch boxesSearch) {
		this.home        = home;
		this.boxesSearch = boxesSearch;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code+".boxes"));
	}
	
	public Result search() {
		return ok(boxesSearch.render());
	}
	
}
