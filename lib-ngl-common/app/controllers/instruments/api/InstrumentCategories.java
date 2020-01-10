package controllers.instruments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.instrument.description.InstrumentCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class InstrumentCategories extends APICommonController<InstrumentCategoriesSearchForm> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(InstrumentCategories.class);
	
	private final Form<InstrumentCategoriesSearchForm> instrumentCategoriesForm;

//	@Inject
//	public InstrumentCategories(NGLContext ctx) {
//		super(ctx, InstrumentCategoriesSearchForm.class);
//		instrumentCategoriesForm = ctx.form(InstrumentCategoriesSearchForm.class);
//	}
	
	@Inject
	public InstrumentCategories(NGLApplication ctx) {
		super(ctx, InstrumentCategoriesSearchForm.class);
		instrumentCategoriesForm = ctx.form(InstrumentCategoriesSearchForm.class);
	}

	public Result list() throws DAOException{
		Form<InstrumentCategoriesSearchForm> instrumentCategoriesTypeFilledForm = filledFormQueryString(instrumentCategoriesForm,InstrumentCategoriesSearchForm.class);
		InstrumentCategoriesSearchForm instrumentCategoriesQueryParams = instrumentCategoriesTypeFilledForm.get();

		List<InstrumentCategory> instrumentCategories;

		try {		
			if(StringUtils.isNotBlank(instrumentCategoriesQueryParams.instrumentTypeCode)){
				instrumentCategories = InstrumentCategory.find.get().findByInstrumentUsedTypeCode(instrumentCategoriesQueryParams.instrumentTypeCode);
			}else{
				instrumentCategories = InstrumentCategory.find.get().findAll();
			}
			if(instrumentCategoriesQueryParams.datatable){
				return ok(Json.toJson(new DatatableResponse<>(instrumentCategories, instrumentCategories.size()))); 
			}else if(instrumentCategoriesQueryParams.list){
				List<ListObject> lop = new ArrayList<>();
				for(InstrumentCategory et:instrumentCategories){
					lop.add(new ListObject(et.code, et.name));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(instrumentCategories));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
}
