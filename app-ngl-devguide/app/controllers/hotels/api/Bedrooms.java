package controllers.hotels.api;

import java.util.List;

import models.hotel.Bedroom;
import play.libs.Json;
import play.mvc.Result;
import controllers.CommonController;

public class Bedrooms extends CommonController {
	
		//recherche des chambres d'un hotel
		public /*static*/ Result list(String hotelCode){
			List<Bedroom> results = null;
			return ok(Json.toJson(results));
		}
		//chargement d'une chambre par son identifiant	
		public /*static*/ Result get(String hotelCode, String code){
			Bedroom result = null;
			return ok(Json.toJson(result));
		}
		//creation et mise à jour d'une chambre
		public /*static*/ Result save(String hotelCode){
			Bedroom result = null;
			return ok(Json.toJson(result));
		}
		//suppression d'une chambre
		public /*static*/ Result delete(String hotelCode, String code){
				
			return ok();
		}			
		
}
