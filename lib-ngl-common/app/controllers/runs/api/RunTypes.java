package controllers.runs.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.description.RunType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class RunTypes extends APICommonController<RunTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(RunTypes.class);
	
	private final Form<RunTypesSearchForm> runTypesForm;
	
	@Inject
	public RunTypes(NGLApplication ctx) {
		super(ctx, RunTypesSearchForm.class);
		runTypesForm = ctx.form(RunTypesSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result list(){
		Form<RunTypesSearchForm> runTypeFilledForm = filledFormQueryString(runTypesForm,RunTypesSearchForm.class);
		RunTypesSearchForm runTypesSearch = runTypeFilledForm.get();
		
		List<RunType> runTypes;
		
		try{		
			runTypes = RunType.find.get().findAll();
			
			if(runTypesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(runTypes, runTypes.size()))); 
			}else if(runTypesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(RunType et:runTypes){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(runTypes));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
}
