package controllers.hotels.api;

import java.util.List;

import models.hotel.Hotel;
import play.libs.Json;
import play.mvc.Result;
import controllers.CommonController;

public class Hotels extends CommonController {
	
	public /*static*/ Result list(){
		List<Hotel> hotels = null;
		return (ok(Json.toJson(hotels)));
	}
		
	public /*static*/ Result get(String code){
		Hotel hotel = null;
		return (ok(Json.toJson(hotel)));
	}
		
}
