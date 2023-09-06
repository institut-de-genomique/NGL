package controllers.hotels.tpl;

import controllers.CommonController;


import play.mvc.Result;


public class Bedrooms extends CommonController {
	
	public /*static*/ Result list(){		
		return ok(views.html.hotels.bedrooms.list.render());
	}
		
	public /*static*/ Result details(){
		return ok(views.html.bookings.details.render());
	}
	
}
