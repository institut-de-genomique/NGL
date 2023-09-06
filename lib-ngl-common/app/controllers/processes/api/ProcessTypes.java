package controllers.processes.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.processes.description.ProcessType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ProcessTypes extends APICommonController<ProcessTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProcessCategories.class);
	
	private final Form<ProcessTypesSearchForm> processTypeForm;
	
//	@Inject
//	public ProcessTypes(NGLContext ctx) {
//		super(ctx, ProcessTypesSearchForm.class);
//		processTypeForm = ctx.form(ProcessTypesSearchForm.class);
//	}
	
	@Inject
	public ProcessTypes(NGLApplication ctx) {
		super(ctx, ProcessTypesSearchForm.class);
		processTypeForm = ctx.form(ProcessTypesSearchForm.class);
	}

	@Permission(value={"reading"})
	public /*static*/ Result list() throws DAOException{
		Form<ProcessTypesSearchForm> processTypeFilledForm = filledFormQueryString(processTypeForm,ProcessTypesSearchForm.class);
		ProcessTypesSearchForm processTypesSearch = processTypeFilledForm.get();
		
		List<ProcessType> processTypes;
		
		try{	
			if (CollectionUtils.isNotEmpty(processTypesSearch.propertyDefinitionCodes)){
				processTypes = ProcessType.find.get().findByPropertyDefinitions(processTypesSearch.propertyDefinitionCodes, processTypesSearch.categoryCodes, processTypesSearch.codes);
			} else if(CollectionUtils.isNotEmpty(processTypesSearch.codes)){
				processTypes = ProcessType.find.get().findByCodes(processTypesSearch.codes);
			}else if(StringUtils.isNotBlank(processTypesSearch.categoryCode)){
				processTypes = ProcessType.find.get().findByProcessCategoryCodes(processTypesSearch.categoryCode);
			}else if(CollectionUtils.isNotEmpty(processTypesSearch.categoryCodes)){
				processTypes = ProcessType.find.get().findByProcessCategoryCodes(processTypesSearch.categoryCodes.toArray(new String[0]));
			}else {
				processTypes = ProcessType.find.get().findAll(processTypesSearch.light);
			}
			if(processTypesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(processTypes, processTypes.size()))); 
			}else if(processTypesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(ProcessType et:processTypes){
					if (processTypesSearch.isActive == null) {
						lop.add(new ListObject(et.code, et.name));
					} else if (processTypesSearch.isActive.equals(et.active)) {
						lop.add(new ListObject(et.code, et.name));
					}
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(processTypes));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}

	@Permission(value={"reading"})
	public Result get(String code) throws DAOException {		 
		ProcessType processType = ProcessType.find.get().findByCode(code);
		if (processType != null) {
			return ok(Json.toJson(processType));
		}			
		return notFound();		
	}
	
}
