package controllers.hotels.tpl;

import controllers.CommonController;

import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

public class Hotels extends CommonController {
	
	public Result home(String homecode){		
		return ok(views.html.hotels.home.render(homecode));
	}
		
	public Result get(String code){
		return ok(views.html.hotels.home.render("search"));
	}
	
	public Result search(){		
		return ok(views.html.hotels.search.render());
	}
		
	public Result details(){
		return ok(views.html.hotels.details.render());
	}
	
	public Result javascriptRoutes() {
	  	    return ok(  	    		
	  	    		JavaScriptReverseRouter.create("jsRoutes",
	  	        // Routes
	  	    		controllers.hotels.tpl.routes.javascript.Hotels.home(),
	  	    		controllers.hotels.tpl.routes.javascript.Hotels.details(),
	  	    		controllers.hotels.api.routes.javascript.Hotels.get(),
	  	    		controllers.hotels.api.routes.javascript.Hotels.list(),
	  	    		controllers.hotels.api.routes.javascript.Bedrooms.get(),
	  	    		controllers.hotels.api.routes.javascript.Bedrooms.list(),
	  	    		controllers.hotels.api.routes.javascript.Bedrooms.save(),
	  	    		controllers.hotels.api.routes.javascript.Bedrooms.delete())
	  	    		).as("text/javascript"); 	  
	}
}
