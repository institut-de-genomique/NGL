package controllers.sra.documentation.tpl;

import play.mvc.Controller;
//import play.Routes;
import play.mvc.Result;
import views.html.documentation.home;

import javax.inject.Inject;

// import controllers.CommonController;                   // done
// public class Documentation extends -CommonController{  // done

public class Documentation extends Controller {
	
	private home home;
	
	@Inject
	public Documentation(home home) {
		this.home = home;
	}
	
	// No annotation for documentation
	public Result home() {
		return ok(home.render());
	}
	
}

