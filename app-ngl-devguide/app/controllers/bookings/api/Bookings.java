package controllers.bookings.api;

import java.util.List;

import models.booking.Booking;

import play.libs.Json;
import play.mvc.Result;
import controllers.CommonController;

public class Bookings extends CommonController {
	//recherche des réservation
	public /*static*/ Result list(){
		List<Booking> results = null; //search code
		return ok(Json.toJson(results));
	}
	//chargement d'une réservation par son identifiant	
	public /*static*/ Result get(String code){
		Booking result = null; //load code
		return ok(Json.toJson(result));
	}
	//creation et mise à jour d'une réservation
	public /*static*/ Result save(){
		Booking result = null; //save code
		return ok(Json.toJson(result));
	}
	//suppression d'une réservation
	public /*static*/ Result delete(String code){
		//delete code
		return ok();
	}					
}
