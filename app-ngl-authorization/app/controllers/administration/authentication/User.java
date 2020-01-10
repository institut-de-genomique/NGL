package controllers.administration.authentication;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.Context;

public class User extends Controller{
	  
		public static Result authenticate(){
			String login = request().getHeader("login");
			String password = request().getHeader("password");
			
			models.administration.authorisation.User user = models.administration.authorisation.User.find.where("login LIKE '"+login+"'").findUnique();
			if(user!= null && user.technicaluser == 1){
				if(user.password.equals(password)){
					createCookie(Http.Context.current(),login);
					return ok("ok");
				}
				else
					return unauthorized("Bad username or password");
			} else {
				return badRequest("User doesn't exist or is not a technical user");
			}
		}
	  
		private static void createCookie(Http.Context context, String id) {
				java.util.Date date= new java.util.Date();
				int timeStamp =  (int)date.getTime();
				
				context.session().put("CAS_FILTER_USER", id);
				context.session().put("CAS_FILTER_TIMEOUT", String.valueOf(timeStamp));
		}
		
		public static Result logOut(){
			Http.Context.current().session().remove("CAS_FILTER_USER");
			Http.Context.current().session().remove("CAS_FILTER_TIMEOUT");
			return ok();//portail d'application
		}
}
