package controllers.instruments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.InstrumentUsedTypeQueryParams;
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

public class InstrumentUsedTypes extends APICommonController<InstrumentUsedTypesSearchForm> {

	private final Form<InstrumentUsedTypesSearchForm> instrumentUsedTypeForm;

//	@Inject
//	public InstrumentUsedTypes(NGLContext ctx) {
//		super(ctx, InstrumentUsedTypesSearchForm.class);
//		instrumentUsedTypeForm = ctx.form(InstrumentUsedTypesSearchForm.class);
//	}
	
	@Inject
	public InstrumentUsedTypes(NGLApplication ctx) {
		super(ctx, InstrumentUsedTypesSearchForm.class);
		instrumentUsedTypeForm = ctx.form(InstrumentUsedTypesSearchForm.class);
	}

	public Result list() throws DAOException {
		Form<InstrumentUsedTypesSearchForm> processTypeFilledForm = filledFormQueryString(instrumentUsedTypeForm,InstrumentUsedTypesSearchForm.class);
		InstrumentUsedTypesSearchForm instrumentUsedsSearch = processTypeFilledForm.get();

		List<InstrumentUsedType> instrumentUseds;

		try {		
			InstrumentUsedTypeQueryParams queryParams = instrumentUsedsSearch.getInstrumentUsedTypesQueryParams();
			if(queryParams.isAtLeastOneParam()) {
				instrumentUseds = InstrumentUsedType.find.get().findByQueryParams(queryParams);
			} else if (instrumentUsedsSearch.experimentTypeCode != null) {
				instrumentUseds = InstrumentUsedType.find.get().findByExperimentTypeCode(instrumentUsedsSearch.experimentTypeCode, instrumentUsedsSearch.isActive);
			} else {
				instrumentUseds = InstrumentUsedType.find.get().findAll();
			}
			if (instrumentUsedsSearch.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(instrumentUseds, instrumentUseds.size()))); 
			} else if(instrumentUsedsSearch.list) {
				List<ListObject> lop = new ArrayList<>();
				for(InstrumentUsedType et:instrumentUseds){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			} else {
				return Results.ok(Json.toJson(instrumentUseds));
			}
		} catch (DAOException e) {
			Logger.error("DAO error: "+e.getMessage(),e);
			return  internalServerError(e.getMessage());
		}	
	}
	
	public Result get(String code) {
		try {
			InstrumentUsedTypeDAO instrumentUsedTypesDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
			InstrumentUsedType instrumentUsedType = null;

			instrumentUsedType = instrumentUsedTypesDAO.findByCode(code);
			if (instrumentUsedType != null) {
				return ok(Json.toJson(instrumentUsedType));
			} else {
				return notFound();
			}
		} catch (DAOException e) {
			Logger.error("DAO error",e);
			return internalServerError(e.getMessage());
		}
	}
	
}
