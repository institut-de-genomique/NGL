 package controllers.treatmenttypes.api;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.run.description.TreatmentType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class TreatmentTypes extends APICommonController<TreatmentTypesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(TreatmentTypes.class);
	
	private final Form<TreatmentTypesSearchForm> treatmentTypesForm;

//	@Inject
//	public TreatmentTypes(NGLContext ctx) {
//		super(ctx, TreatmentTypesSearchForm.class);
//		treatmentTypesForm = ctx.form(TreatmentTypesSearchForm.class);
//	}

	@Inject
	public TreatmentTypes(NGLApplication app) {
		super(app, TreatmentTypesSearchForm.class);
		treatmentTypesForm = app.form(TreatmentTypesSearchForm.class);
	}

	public Result list() {
		Form<TreatmentTypesSearchForm> treatmentTypesFilledForm = filledFormQueryString(treatmentTypesForm,TreatmentTypesSearchForm.class);
		TreatmentTypesSearchForm searchForm = treatmentTypesFilledForm.get();

		List<TreatmentType> treatments;
		
		// NGL-3530 ajout filtres de recherche par"typeCode", par categorieName
		// la recherche par level sert elle encore ??????
		try {		
			if (searchForm.levels != null) {
				treatments = TreatmentType.find.get().findByLevels(searchForm.levels);
			} else if (CollectionUtils.isNotEmpty(searchForm.codes) && CollectionUtils.isNotEmpty(searchForm.categoryNames) ){
				treatments = TreatmentType.find.get().findByCodesAndCategoryNames(searchForm.categoryNames, searchForm.codes);
			} else if(StringUtils.isNotBlank(searchForm.code)) {
				//  ce cas avec un seul code sert il vraiement ????	
				logger.error("search treatments by 1 type ??");
				treatments = Arrays.asList(TreatmentType.find.get().findByCode(searchForm.code));
			} else if(CollectionUtils.isNotEmpty(searchForm.codes)) {
				treatments = TreatmentType.find.get().findByCodes(searchForm.codes);
			} else if(CollectionUtils.isNotEmpty(searchForm.names)) {
				treatments = TreatmentType.find.get().findByNames(searchForm.names);
			} else if(CollectionUtils.isNotEmpty(searchForm.categoryNames)) {
				int numberCategories =searchForm.categoryNames.size();
				String[] categoriesTab = searchForm.categoryNames.toArray(new String[numberCategories]);
				treatments = TreatmentType.find.get().findByTreatmentCategoryNames(categoriesTab);
			} else {
				treatments = TreatmentType.find.get().findAll();
			}
			
			if (searchForm.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(treatments, treatments.size()))); 
			} else if(searchForm.list){
				// FDS NGL-3530 (code repris depuis RunCategories.java)
				List<ListObject> lop = treatments.stream()
						.map((TreatmentType treatment) -> {
							return new ListObject(treatment.code, treatment.name);
						}).collect(Collectors.toList());
				return Results.ok(Json.toJson(lop));
			} else {	
				//  cas  ???
				return ok(Json.toJson(treatments));
			}
		} catch (DAOException e) {
			logger.error(e.getMessage());
			return  internalServerError(e.getMessage());
		}	
	}
	
	
	public Result get(String code) {
		TreatmentType treatmentType =  getTreatmentType(code);		
		if (treatmentType != null) {
			return ok(Json.toJson(treatmentType));	
		} 		
		else {
			return notFound();
		}	
	}

	private static TreatmentType getTreatmentType(String code) {
		try {
			return TreatmentType.find.get().findByCode(code);
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}		
	}
}
