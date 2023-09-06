package controllers.administration.authorisation;

import static play.data.Form.form;
import models.administration.authorisation.Application;
import models.administration.authorisation.Permission;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.Page;

import controllers.utils.DataTableForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.administration.authorisation.addApplication;
import views.html.administration.authorisation.adminApplications;

public class Applications extends Controller{
	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);
	
	public static Result home() {
		  return ok(adminApplications.render(datatableForm,  form(models.administration.authorisation.Application.class)));
	}
	
	public static Result remove(java.lang.Integer id) {
		models.administration.authorisation.Application.find.byId(id).delete();
		return ok();
	}
	
	 public static Result add() {
		  return ok(addApplication.render( form(models.administration.authorisation.Application.class),true));
	  }
	 
	 @BodyParser.Of(BodyParser.Json.class)
	  public static Result list() {
			Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
			ObjectNode result = Json.newObject();
			Page<Application> p = Application.page(0, 10, "code", "asc", filledForm.get().sSearch.get(1),  filledForm.get().sSearch.get(2));
			result.put("iTotalRecords", p.getTotalRowCount());
			result.put("iTotalDisplayRecords", p.getTotalRowCount());
			result.put("sEcho", filledForm.get().sEcho);
			result.put("aaData", Json.toJson(p.getList()));
			return ok(result);
	 }
	  
	  //create or update permission
	  public static Result createOrUpdate(String format) {
			Form<Application> filledForm = form(Application.class).bindFromRequest();

			if (filledForm.hasErrors()) {
				if ("json".equals(format)) {
					return badRequest(filledForm.errorsAsJson( )); // legit
				} else {
					return badRequest(addApplication.render(filledForm,true));
				}
				
			} else {
				Application bean = filledForm.get();	
				if(bean.id == null){
					bean.save();
				}else{
					bean.update();
				}						
				filledForm = filledForm.fill(bean);
				if("json".equals(format)){
					return ok(Json.toJson(bean));
				}else{
					return ok(addApplication.render(filledForm,true));
				}
			}		
		}
	  
	  //show permission
	  public static Result show(java.lang.Integer id){
		  	Application result = Application.find.byId(java.lang.Integer.valueOf(id));
			Form<Application> filledForm =  form(Application.class).fill(result);
			return ok(addApplication.render(filledForm, Boolean.FALSE));		
	  }
	  
	  //edit permission
	  public static Result edit(java.lang.Integer id){
		  	Application result = Application.find.byId(Integer.valueOf(id));
			Form<Application> filledForm = form(Application.class).fill(result);
			return ok(addApplication.render(filledForm, Boolean.TRUE));		
	  }
}
