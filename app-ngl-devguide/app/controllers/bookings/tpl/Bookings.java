package controllers.bookings.tpl;

import controllers.CommonController;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

public class Bookings extends CommonController {
	
//	public Bookings() {
//		
//	}
	
	public Result home(String homecode){		
		return ok(views.html.bookings.home.render(homecode));
	}
		
	public Result get(String code){
		return ok(views.html.bookings.home.render("search"));
	}
	
	public Result search(){		
		return ok(views.html.bookings.search.render());
	}
		
	public Result details(){
		return ok();
	}
	
	public Result javascriptRoutes() {
	  	    return ok(  	    		
	  	    		JavaScriptReverseRouter.create("jsRoutes",
	  	        // Routes
	  	    		controllers.bookings.tpl.routes.javascript.Bookings.home(),
	  	    		controllers.bookings.tpl.routes.javascript.Bookings.details(),
	  	    		controllers.bookings.api.routes.javascript.Bookings.get(),
	  	    		controllers.bookings.api.routes.javascript.Bookings.list(),
	  	    		controllers.bookings.api.routes.javascript.Bookings.get(),
	  	    		controllers.bookings.api.routes.javascript.Bookings.save(),
	  	    		controllers.bookings.api.routes.javascript.Bookings.delete())
	  	      ).as("text/javascript"); 	      
	}
	
}
