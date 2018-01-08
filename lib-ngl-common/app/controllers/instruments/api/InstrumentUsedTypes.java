package controllers.instruments.api;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.Logger;
import play.api.modules.spring.Spring;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;
import controllers.CommonController;

public class InstrumentUsedTypes extends CommonController{

	final static Form<InstrumentUsedTypesSearchForm> instrumentUsedTypeForm = form(InstrumentUsedTypesSearchForm.class);

	public static Result list() throws DAOException{
		Form<InstrumentUsedTypesSearchForm> processTypeFilledForm = filledFormQueryString(instrumentUsedTypeForm,InstrumentUsedTypesSearchForm.class);
		InstrumentUsedTypesSearchForm instrumentUsedsSearch = processTypeFilledForm.get();

		List<InstrumentUsedType> instrumentUseds;

		try{		
			if(instrumentUsedsSearch.experimentTypeCode != null){
				instrumentUseds = InstrumentUsedType.find.findByExperimentTypeCode(instrumentUsedsSearch.experimentTypeCode);
			}else{
				instrumentUseds = InstrumentUsedType.find.findAll();
			}
			if(instrumentUsedsSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<InstrumentUsedType>(instrumentUseds, instrumentUseds.size()))); 
			}else if(instrumentUsedsSearch.list){
				List<ListObject> lop = new ArrayList<ListObject>();
				for(InstrumentUsedType et:instrumentUseds){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(instrumentUseds));
			}
		}catch (DAOException e) {
			Logger.error("DAO error: "+e.getMessage(),e);
			return  internalServerError(e.getMessage());
		}	
	}
	
	public static Result get(String code){
		
		try {
			InstrumentUsedTypeDAO instrumentUsedTypesDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
			InstrumentUsedType instrumentUsedType = null;

			instrumentUsedType = instrumentUsedTypesDAO.findByCode(code);
			Logger.debug("instrumentUsedType : "+ instrumentUsedType);
			if(null != instrumentUsedType){
				return ok(Json.toJson(instrumentUsedType));
			}else{
				return notFound();
			}
			
		} catch (DAOException e) {
			Logger.error("DAO error",e);
			return internalServerError(e.getMessage());
		}

		
	}
}
