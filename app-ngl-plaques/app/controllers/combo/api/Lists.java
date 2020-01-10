package controllers.combo.api;

import controllers.CommonController;
import lims.cns.dao.LimsManipDAO;
import play.api.modules.spring.Spring;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

public class Lists extends CommonController {
	
//	public Lists() {
//	}
	
	public /*static*/ Result projects() {
		return Results.ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).getListObjectFromProcedureLims("pl_Projet")));	
	}
	
	public /*static*/ Result samples() {
		return Results.ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).getListObjectFromProcedureLims("pl_Materiel")));	
	}
	
	public /*static*/ Result etmanips() {
		return Results.ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).getListObjectFromProcedureLims("pl_EtmanipPlaque")));	
	}
	
	public /*static*/ Result etmateriels() {
		return Results.ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).getListObjectFromProcedureLims("pl_Etmateriel")));	
	}
	
	public /*static*/ Result users() {
		return Results.ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).getListObjectFromProcedureLims("pl_Perint")));			
	}	
	
}
