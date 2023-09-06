package controllers.experiments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ExperimentTypes extends APICommonController<ExperimentTypesSearchForm> {
	
	/**
	 * Logger.
	 */
	private final static play.Logger.ALogger logger = play.Logger.of(ExperimentTypes.class);
	
	
	private final Form<ExperimentTypesSearchForm> experimentTypeForm;
	
	@Inject
	public ExperimentTypes(NGLApplication ctx) {
		super(ctx, ExperimentTypesSearchForm.class);
		this.experimentTypeForm = ctx.form(ExperimentTypesSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result get(String code){
		ExperimentType experimentType = null;
		try {
			experimentType = ExperimentType.find.get().findByCode(code);
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			// throw new RuntimeException("get experiment('" + code + "') failed",e);
		}
		if (experimentType == null)
			return notFound();
		return ok(Json.toJson(experimentType));
	}
	
	@Permission(value={"reading"})
	public Result list() throws DAOException {
		Form<ExperimentTypesSearchForm> experimentTypeFilledForm = filledFormQueryString(experimentTypeForm,ExperimentTypesSearchForm.class);
		ExperimentTypesSearchForm experimentTypesSearch = experimentTypeFilledForm.get();
		List<ExperimentType> experimentTypes = new ArrayList<>();
		ExperimentTypeDAO etfind = ExperimentType.find.get();
		try{		
			
			if(StringUtils.isNotBlank(experimentTypesSearch.categoryCode) && experimentTypesSearch.withoutOneToVoid !=null  && experimentTypesSearch.withoutOneToVoid){
				experimentTypes = etfind.findByCategoryCodeWithoutOneToVoid(experimentTypesSearch.categoryCode);				
			}else if(experimentTypesSearch.categoryCodes != null && experimentTypesSearch.categoryCodes.size()>0 && experimentTypesSearch.withoutOneToVoid !=null  && experimentTypesSearch.withoutOneToVoid){
				experimentTypes = etfind.findByCategoryCodesWithoutOneToVoid(experimentTypesSearch.categoryCodes);		
			}else if(StringUtils.isNotBlank(experimentTypesSearch.categoryCode) && experimentTypesSearch.processTypeCode == null){
				experimentTypes = etfind.findByCategoryCode(experimentTypesSearch.categoryCode);
			}else if(experimentTypesSearch.categoryCodes != null && experimentTypesSearch.categoryCodes.size()>0 && experimentTypesSearch.processTypeCode == null){
					experimentTypes = etfind.findByCategoryCodes(experimentTypesSearch.categoryCodes);
			}else if(StringUtils.isNotBlank(experimentTypesSearch.categoryCode) && StringUtils.isNotBlank(experimentTypesSearch.processTypeCode)){
				experimentTypes = etfind.findByCategoryCodeAndProcessTypeCode(experimentTypesSearch.categoryCode, experimentTypesSearch.processTypeCode);
			}else if(StringUtils.isNotBlank(experimentTypesSearch.previousExperimentTypeCode) 
					&& StringUtils.isNotBlank(experimentTypesSearch.processTypeCode)){
				//experimentTypes = ExperimentType.find.findByPreviousExperimentTypeCodeInProcessTypeContext(experimentTypesSearch.previousExperimentTypeCode, experimentTypesSearch.processTypeCode);
				experimentTypes = etfind.findNextExperimentTypeForAnExperimentTypeCodeAndProcessTypeCode(experimentTypesSearch.previousExperimentTypeCode, experimentTypesSearch.processTypeCode);
			}else if(StringUtils.isNotBlank(experimentTypesSearch.previousExperimentTypeCode)){
				experimentTypes = etfind.findNextExperimentTypeCode(experimentTypesSearch.previousExperimentTypeCode);
			}else if(Boolean.TRUE.equals(experimentTypesSearch.withoutExtTo)){
				experimentTypes = etfind.findWithoutExtTo(experimentTypesSearch.code, experimentTypesSearch.codes, experimentTypesSearch.instrumentUsedTypeCode, experimentTypesSearch.instrumentUsedTypeCodes, experimentTypesSearch.propertyDefinitionName, experimentTypesSearch.propertyDefinitionNames);
			}else{
				experimentTypes = etfind.findAll();
			}
			
			if(experimentTypesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(experimentTypes, experimentTypes.size()))); 
			}else if(experimentTypesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(ExperimentType et:experimentTypes){
					if(null == experimentTypesSearch.isActive){
						lop.add(new ListObject(et.code, et.name));
					}else if(experimentTypesSearch.isActive.equals(et.active)){
						lop.add(new ListObject(et.code, et.name));
					}
					
					
				}				
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(experimentTypes));
			}
		}catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}

	@Permission(value={"reading"})
	public Result getDefaultFirstExperiments(String processTypeCode) throws DAOException{		
			List<ExperimentType> expTypes = ExperimentType.find.get().findInputExperimentTypeForAnProcessTypeCode(processTypeCode);
		return ok(Json.toJson(expTypes));		
	}

}
