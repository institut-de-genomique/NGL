package controllers.reagents.tpl;

import javax.inject.Inject;

import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.declarations.home;
import views.html.declarations.kitsSearch;
import views.html.declarations.ordersCreation;

//import controllers.CommonController;
public class Orders extends Controller { // -CommonController {
	
	private final home           home;
	private final kitsSearch     kitsSearch;
	private final ordersCreation ordersCreation;
	
	@Inject
	public Orders(home home, kitsSearch kitsSearch, ordersCreation ordersCreation) {
		this.home           = home;
		this.kitsSearch     = kitsSearch;
		this.ordersCreation = ordersCreation;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code + ".order"));
	}
	
	public Result search() {
		return ok(kitsSearch.render());
	}
	
	public Result get(String code) {
		return ok(home.render(code));
	}
	
	public Result createOrEdit() {
		return ok(ordersCreation.render());
	}
	
}
