package controllers.administration.authorisation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.Page;

import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import controllers.utils.DataTableForm;
import models.administration.authorisation.Team;
import views.html.administration.authorisation.addTeam;
import views.html.administration.authorisation.adminTeams;
import static play.data.Form.form;

public class Teams extends Controller {

	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);

	public static Result home() {
		return ok(adminTeams.render(datatableForm, form(Team.class)));
	}


	public static Result add() {
		return ok(addTeam.render(form(Team.class),true));
	}


	public static Result show(java.lang.Integer id){
		Team result = Team.find.byId(java.lang.Integer.valueOf(id));
		Form<Team> filledForm =  form(Team.class).fill(result);
		return ok(addTeam.render(filledForm, Boolean.FALSE));		
	}

	//list Team
	@BodyParser.Of(BodyParser.Json.class)
	public static Result list() {
		Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
		ObjectNode result = Json.newObject();
		Page<Team> p = Team.page(0, 10, "nom", "asc", filledForm.get().sSearch.get(1));
		result.put("iTotalRecords", p.getTotalRowCount());
		result.put("iTotalDisplayRecords", p.getTotalRowCount());
		result.put("sEcho", filledForm.get().sEcho);
		result.put("aaData", Json.toJson(p.getList()));
		return ok(result);
	}

	public static Result edit(java.lang.Integer id){
		Team result = Team.find.byId(Integer.valueOf(id));
		Form<Team> filledForm = form(Team.class).fill(result);
		return ok(addTeam.render(filledForm, Boolean.TRUE));		
	}

	// @With(controllers.history.user.UserHistory.class)
	public static Result createOrUpdate(String format) {
		Form<Team> filledForm = form(Team.class).bindFromRequest();

		if (filledForm.hasErrors()) {
			if ("json".equals(format)) {
				return badRequest(filledForm.errorsAsJson( )); // legit
			} else {
				return badRequest(addTeam.render(filledForm,true));
			}

		} else {
			Team bean = filledForm.get();

			if(bean.id == null){
				bean.save();
			}else{
				bean.update();
			}

			filledForm = filledForm.fill(bean);
			if("json".equals(format)){
				return ok(Json.toJson(bean));
			}else{
				return ok(addTeam.render(filledForm,true));
			}
		}		
	}

	public static Result remove(java.lang.Integer id) {
		Team.find.byId(id).delete();
		return ok();
	}
}
