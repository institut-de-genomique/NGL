package controllers.printing.tpl;

// import controllers.history.UserHistory;
import play.mvc.Controller;
import play.mvc.Result;
// import play.mvc.With;
import views.html.printing.tags.display;

// @With({fr.cea.ig.authentication.Authenticate.class, UserHistory.class})
public class Tags extends Controller {
	
	// tpl
	public Result display(){
		return ok(display.render());
	}
	
}
