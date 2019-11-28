package controllers.commons.api;

import java.util.ArrayList;
import java.util.List;

import controllers.CommonController;
import models.administration.authorisation.Role;
import models.utils.ListObjectNumber;
import models.utils.dao.DAOException;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

/**
 * 
 * @author michieli
 *
 */
public class Roles extends CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Roles.class);
	
	/*
	 * List Method
	 */
	public /*static*/ Result list() throws DAOException{
		try{
			List<Role> roles = Role.find.get().findAll();
			
			//if(form.list){
			List<ListObjectNumber> lop = new ArrayList<>();
			for(Role r:roles){
				lop.add(new ListObjectNumber(r.id, r.label));
			}
			return Results.ok(Json.toJson(lop));
		} catch (DAOException e) {
			logger.error("DAO error: " + e.getMessage());
			return  Results.internalServerError(e.getMessage());
		}
	}

}
