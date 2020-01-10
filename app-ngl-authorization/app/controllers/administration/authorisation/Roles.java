package controllers.administration.authorisation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.Page;

import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.utils.DataTableForm;
import static play.data.Form.form;
import models.administration.authorisation.Role;
import  views.html.administration.authorisation.addRole;
import views.html.administration.authorisation.adminRoles;
import controllers.authorisation.PermissionHelper;
import static play.data.Form.form;

public class Roles extends Controller {
	
	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);
	
  public static Result home() {
		  
		  return ok(adminRoles.render(datatableForm, form(Role.class)));
	  }
	
	public static Result remove(java.lang.Integer id) {
		Role.find.byId(id).delete();
		return ok();
	}

	  public static Result add() {
		  return ok(addRole.render(form(Role.class),PermissionHelper.getMapPerm(),"permissions","id",true));
	  }
	  
	  @BodyParser.Of(BodyParser.Json.class)
	  public static Result list() {
			Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
			ObjectNode result = Json.newObject();
			Page<Role> p = Role.page(0, 10, "label", "asc", filledForm.get().sSearch.get(1));
			result.put("iTotalRecords", p.getTotalRowCount());
			result.put("iTotalDisplayRecords", p.getTotalRowCount());
			result.put("sEcho", filledForm.get().sEcho);
			result.put("aaData", Json.toJson(p.getList()));
			return ok(result);
	 }
	  
	  public static Result createOrUpdate(String format) {
			Form<Role> filledForm = form(Role.class).bindFromRequest();
			
			if (filledForm.hasErrors()) {
				if ("json".equals(format)){
					return badRequest(filledForm.errorsAsJson( )); // legit
				} else {
					return badRequest(addRole.render(filledForm,PermissionHelper.getMapPerm(),"permissions","id",true));
				}
				
			} else {
				Role bean = filledForm.get();
				for(int i = 0;i<bean.permissions.size();i++) {
					if(bean.permissions.get(i).id!=null) 
						bean.permissions.set(i, PermissionHelper.getpermission(bean.permissions.get(i).id));
					else {
						bean.permissions.remove(i);
						i--;
					}
				}
				
				if(bean.id == null){
					bean.save();
				}else{
					bean.update();
				}
				
				filledForm = filledForm.fill(bean);
				if("json".equals(format)){
					return ok(Json.toJson(bean));
				}else{
					return ok(addRole.render(filledForm,PermissionHelper.getMapPerm(),"permissions","id",true));
				}
			}		
		}
	  
	  public static Result show(java.lang.Integer id){
			Role result = Role.find.byId(java.lang.Integer.valueOf(id));
			Form<Role> filledForm =  form(Role.class).fill(result);
			return ok(addRole.render(filledForm,PermissionHelper.getMapPerm(),"permissions","id", Boolean.FALSE));		
	  }
	  
	  public static Result edit(java.lang.Integer id){
			Role result = Role.find.byId(Integer.valueOf(id));
			Form<Role> filledForm = form(Role.class).fill(result);
			return ok(addRole.render(filledForm,PermissionHelper.getMapPerm(),"permissions","id", Boolean.TRUE));		
	  }
	  
}
