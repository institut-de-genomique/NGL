package controllers.administration.authorisation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.Page;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.utils.DataTableForm;
import models.administration.authorisation.User;
import  views.html.administration.authorisation.addUser;
import  views.html.administration.authorisation.adminUsers;
import controllers.authorisation.PermissionHelper;
import static play.data.Form.form;


public class Users extends Controller {
	
	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);

	 public static Result home() {
		  
		  return ok(adminUsers.render(datatableForm, form(User.class)));
	  }
	 
	
	public static Result remove(java.lang.Integer id) {
		User.find.byId(id).delete();
		return ok();
	}
	
	  
	  public static Result add() {
		  return ok(addUser.render(form(User.class),true));
	  }
	
	  @BodyParser.Of(BodyParser.Json.class)
	  public static Result list() {
			Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
			ObjectNode result = Json.newObject();
			Page<User> p = User.page(0, 10, "login", "asc", filledForm.get().sSearch.get(1),filledForm.get().sSearch.get(2),filledForm.get().sSearch.get(3),filledForm.get().sSearch.get(4));
			result.put("iTotalRecords", p.getTotalRowCount());
			result.put("iTotalDisplayRecords", p.getTotalRowCount());
			result.put("sEcho", filledForm.get().sEcho);
			result.put("aaData", Json.toJson(p.getList()));
			return ok(result);
	 }
		
	//show user
	  public static Result show(java.lang.Integer id){
			User result = User.find.byId(java.lang.Integer.valueOf(id));
			Form<User> filledForm =  form(User.class).fill(result);
			return ok(addUser.render(filledForm, Boolean.FALSE));		
	  }
	  
	  public static Result edit(java.lang.Integer id){
			User result = User.find.byId(Integer.valueOf(id));
			Form<User> filledForm = form(User.class).fill(result);
			return ok(addUser.render(filledForm, Boolean.TRUE));		
	  }
	  
	  public static Result createOrUpdate(String format) {
			Form<User> filledForm = form(User.class).bindFromRequest();
			if (filledForm.hasErrors()) {
				if ("json".equals(format)) {
					return badRequest(filledForm.errorsAsJson( )); // legit
				} else {
					return badRequest(addUser.render(filledForm,true));
				}
				
			} else {
				
				User bean = filledForm.get();
				if(bean.password != null && !bean.password.equals("")){
					
					bean.technicaluser =1;
				}
				for(int i = 0;i<bean.roles.size();i++) {
					if(bean.roles.get(i).id!=null) 
						bean.roles.set(i, PermissionHelper.getRole(bean.roles.get(i).id));
					else {
						bean.roles.remove(i);
						i--;
					}
				}
				
				for(int i = 0;i<bean.teams.size();i++) {
					if(bean.teams.get(i).id!=null) 
						bean.teams.set(i, PermissionHelper.getTeam(bean.teams.get(i).id));
					else {
						bean.teams.remove(i);
						i--;
					}
				}
				
				for(int i = 0;i<bean.applications.size();i++) {
					if(bean.applications.get(i).id!=null) 
						bean.applications.set(i, PermissionHelper.getApplication(bean.applications.get(i).id));
					else {
						bean.applications.remove(i);
						i--;
					}
				}
				
				if(bean.id == 0){
					bean.save();
				}else{
					bean.update();
				}
				
				filledForm = filledForm.fill(bean);
				if("json".equals(format)){
					return ok(Json.toJson(bean));
				}else{
					return ok(addUser.render(filledForm,true));
				}
				
				
				
			}		
		}
	  

	  
}
