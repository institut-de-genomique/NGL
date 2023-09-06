package controllers.samples.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.dao.ImportTypeDAO;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ImportTypes extends APICommonController<ImportTypesSearchForm> {
	
	/**
	 * Logger.
	 */
	private final static play.Logger.ALogger logger = play.Logger.of(ImportTypes.class);
	
	
	private final Form<ImportTypesSearchForm> importTypeForm;
	
	@Inject
	public ImportTypes(NGLApplication ctx) {
		super(ctx, ImportTypesSearchForm.class);
		this.importTypeForm = ctx.form(ImportTypesSearchForm.class);
	}
	
	@Permission(value={"reading"})
	public Result list() throws DAOException {
		Form<ImportTypesSearchForm> importTypeFilledForm = filledFormQueryString(importTypeForm,ImportTypesSearchForm.class);
		ImportTypesSearchForm importTypesSearch = importTypeFilledForm.get();
		List<ImportType> importTypes = new ArrayList<>();
		ImportTypeDAO itfind = ImportType.find.get();
		try{					
			importTypes = itfind.findAll();
			
			if(importTypesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(importTypes, importTypes.size()))); 
			}else if(importTypesSearch.list){
				List<ListObject> lop = importTypes.parallelStream().map((ImportType it) -> new ListObject(it.code, it.name)).collect(Collectors.toList());				
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(importTypes));
			}
		}catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}

}
