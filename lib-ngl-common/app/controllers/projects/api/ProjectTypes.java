package controllers.projects.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.project.description.ProjectType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ProjectTypes extends APICommonController<ProjectTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectTypes.class);
	
	private final Form<ProjectTypesSearchForm> projectTypeForm ;
	
//	@Inject
//	public ProjectTypes(NGLContext ctx) {
//		super(ctx, ProjectTypesSearchForm.class);
//		projectTypeForm = ctx.form(ProjectTypesSearchForm.class);
//	}

	@Inject
	public ProjectTypes(NGLApplication ctx) {
		super(ctx, ProjectTypesSearchForm.class);
		projectTypeForm = ctx.form(ProjectTypesSearchForm.class);
	}
	
	public /*static*/ Result list() throws DAOException{
		Form<ProjectTypesSearchForm> projectTypeFilledForm = filledFormQueryString(projectTypeForm,ProjectTypesSearchForm.class);
		ProjectTypesSearchForm projectTypesSearch = projectTypeFilledForm.get();
		
		List<ProjectType> projectTypes;
		
		try{	
			projectTypes = ProjectType.find.get().findAll();

			if(projectTypesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(projectTypes, projectTypes.size()))); 
			}else if(projectTypesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(ProjectType et:projectTypes){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(projectTypes));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
}
