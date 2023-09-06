package controllers.administration.authorisation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.avaje.ebean.Page;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import models.administration.authorisation.Permission;
import controllers.utils.DataTableForm;
import views.html.administration.authorisation.addPermission;
import views.html.administration.authorisation.adminPermissions;
import views.html.*;
import static play.data.Form.form;

public class Permissions extends Controller {
	
	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);
	
	public static Result home() {
		  return ok(adminPermissions.render(datatableForm,  form(models.administration.authorisation.Permission.class)));
	}
	
	public static Result remove(java.lang.Integer id) {
		models.administration.authorisation.Permission.find.byId(id).delete();
		return ok();
	}
	
	 public static Result add() {
		  return ok(addPermission.render( form(models.administration.authorisation.Permission.class),true));
	  }
	 
	 @BodyParser.Of(BodyParser.Json.class)
	  public static Result list() {
			Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
			ObjectNode result = Json.newObject();
			Page<Permission> p = Permission.page(0, 10, "code", "asc", filledForm.get().sSearch.get(1),  filledForm.get().sSearch.get(2));
			result.put("iTotalRecords", p.getTotalRowCount());
			result.put("iTotalDisplayRecords", p.getTotalRowCount());
			result.put("sEcho", filledForm.get().sEcho);
			result.put("aaData", Json.toJson(p.getList()));
			return ok(result);
	 }
	  
	  //create or update permission
	  public static Result createOrUpdate(String format) {
			Form<Permission> filledForm = form(Permission.class).bindFromRequest();

			if(filledForm.hasErrors()) {
				if ("json".equals(format)) {
					return badRequest(filledForm.errorsAsJson( )); // legit
				} else {
					return badRequest(addPermission.render(filledForm,true));
				}
				
			} else {
				Permission bean = filledForm.get();	
				if(bean.id == null){
					bean.save();
				}else{
					bean.update();
				}						
				filledForm = filledForm.fill(bean);
				if("json".equals(format)){
					return ok(Json.toJson(bean));
				}else{
					return ok(addPermission.render(filledForm,true));
				}
			}		
		}
	  
	  //show permission
	  public static Result show(java.lang.Integer id){
			Permission result = Permission.find.byId(java.lang.Integer.valueOf(id));
			Form<Permission> filledForm =  form(Permission.class).fill(result);
			return ok(addPermission.render(filledForm, Boolean.FALSE));		
	  }
	  
	  //edit permission
	  public static Result edit(java.lang.Integer id){
			Permission result = Permission.find.byId(Integer.valueOf(id));
			Form<Permission> filledForm = form(Permission.class).fill(result);
			return ok(addPermission.render(filledForm, Boolean.TRUE));		
	  }
}
