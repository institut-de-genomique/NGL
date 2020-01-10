package controllers.plates.api;


import static validation.utils.ValidationHelper.validateNotEmpty;

import java.util.List;

import javax.inject.Inject;

import controllers.CommonController;
import controllers.MaterielManipSearch;
import fr.cea.ig.ngl.NGLApplication;
import lims.cns.dao.LimsManipDAO;
import lims.models.Plate;
import models.utils.CodeHelper;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Plates extends CommonController {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(Plates.class);
		
	private final NGLApplication            app;
	private final Form<Plate>               wellsForm;
	private final Form<MaterielManipSearch> manipForm;
	
//	@Inject
//	public Plates(NGLContext ctx) {
////		this.ctx = ctx;
//		wellsForm = ctx.form(Plate.class);
//		manipForm = ctx.form(MaterielManipSearch.class);
//	}
	
	@Inject
	public Plates(NGLApplication app) {
		this.app = app;
		wellsForm = app.form(Plate.class);
		manipForm = app.form(MaterielManipSearch.class);
	}
	
	public Result save() {
		Form<Plate> filledForm = getFilledForm(wellsForm, Plate.class);
		Plate plate = filledForm.get();
		boolean isUpdate = true;
		logger.info("SAVE Plate : " + plate);
		if (plate.code == null) {
			plate.code = newCode(plate.wells[0].typeCode);
			if (plate.wells.length > 0) {
				plate.typeName = plate.wells[0].typeName;
				plate.typeCode = plate.wells[0].typeCode;
			}
			isUpdate = false;
		}
//		Map<String, List<ValidationError>> errors    = new TreeMap<String, List<ValidationError>>();
		ContextValidation ctx = ContextValidation.createUndefinedContext(getCurrentUser());
//		validatePlate(plate, errors, isUpdate);
		validatePlate(plate, ctx, isUpdate);
//		if (errors.isEmpty()) {
		if (!ctx.hasErrors()) {
			logger.debug(plate.toString());
			if (!isUpdate) {
//				Spring.getBeanOfType(LimsManipDAO.class).createPlate(plate,getCurrentUser());
				limsManipDAO().createPlate(plate,getCurrentUser());
			} else {
//				Spring.getBeanOfType(LimsManipDAO.class).updatePlate(plate,getCurrentUser());
				limsManipDAO().updatePlate(plate,getCurrentUser());
			}
//			plate = Spring.getBeanOfType(LimsManipDAO.class).getPlate(plate.code);  
			plate = limsManipDAO().getPlate(plate.code);  
			return ok(Json.toJson(plate));
		} else {
//			return badRequest(NGLContext._errorsAsJson(errors)); 
//			return badRequest(NGLContext._errorsAsJson(ctx.getErrors())); 
			return badRequest(app.errorsAsJson(ctx.getErrors())); 
		}
	}
	
	public Result list(){
		Form<MaterielManipSearch> filledForm =  manipForm.bindFromRequest();
//		LimsManipDAO  limsManipDAO = Spring.getBeanOfType(LimsManipDAO.class);
		logger.info("Manip Form :"+filledForm.toString());		
		List<Plate> plates = limsManipDAO().findPlates(filledForm.get().etmanip,filledForm.get().project, filledForm.get().plaqueId, 
				filledForm.get().matmanom, filledForm.get().percodc, filledForm.get().fromDate, filledForm.get().toDate);		
		return ok(Json.toJson(new DatatableResponse<>(plates, plates.size())));
	}
	
	public Result get(String code){
		logger.info("GET Plate : "+code);
//		LimsManipDAO  limsManipDAO = Spring.getBeanOfType(LimsManipDAO.class);
//		Plate plate = limsManipDAO.getPlate(code);
		Plate plate = limsManipDAO().getPlate(code);
		if (plate != null) {			
			return ok(Json.toJson(plate));					
		} else {
			return notFound();
		}
	}
	
	public Result remove(String code){
		logger.info("DELETE Plate : "+code);
//		LimsManipDAO  limsManipDAO = Spring.getBeanOfType(LimsManipDAO.class);
//		limsManipDAO.deletePlate(code);
		limsManipDAO().deletePlate(code);
		return ok();
	}
	
//	private /*static*/ void validatePlate(Plate plate, Map<String, List<ValidationError>> errors, boolean isUpdate) {
//		if(required(errors, plate, "plate")){
//			required(errors, plate.code, "code");
//			required(errors, plate.typeCode, "typeCode");
//			if(required(errors, plate.wells, "wells")){
//				for(int i = 0 ; i < plate.wells.length ; i++){
//					required(errors, plate.wells[i].x, "wells["+i+"]"+".x");
//					required(errors, plate.wells[i].y, "wells["+i+"]"+".y");
//					required(errors, plate.wells[i].code, "wells["+i+"]"+".code");
//					if(!plate.wells[i].typeCode.equals(plate.typeCode)){
//						addErrors(errors, "wells["+i+"]"+".typeName", "plates.error.typecode.different", plate.typeName, plate.wells[i].typeName);
//					}
//				}
//				if(!isUpdate){
//					validatePlateCode(plate, errors); 
//				}
//				
//				for(int i = 0 ; i < plate.wells.length ; i++){
//					for(int j = 0 ; j < plate.wells.length ; j++){
//						if(i != j){
//							if(plate.wells[i].code.equals(plate.wells[j].code)){
//								addErrors(errors, "wells["+i+"]"+".name", "plates.error.severalsamewellcode", plate.wells[i].name);
//							}
//							
//							if(plate.wells[i].x != null && plate.wells[i].y != null && plate.wells[i].x.equals(plate.wells[j].x) && plate.wells[i].y.equals(plate.wells[j].y)){
//								addErrors(errors, "wells["+i+"]", "plates.error.wellwithsamecoord", plate.wells[i].x, plate.wells[i].y);
//							}							
//						}
//					}
//				}
//			}
//		}
//	}
	
	private void validatePlate(Plate plate, ContextValidation ctx, boolean isUpdate) {
		if (validateNotEmpty(ctx, plate, "plate")) {
			validateNotEmpty(ctx, plate.code, "code");
			validateNotEmpty(ctx, plate.typeCode, "typeCode");
			if (validateNotEmpty(ctx, plate.wells, "wells")) {
				for (int i = 0 ; i < plate.wells.length ; i++) {
					validateNotEmpty(ctx, plate.wells[i].x, "wells["+i+"]"+".x");
					validateNotEmpty(ctx, plate.wells[i].y, "wells["+i+"]"+".y");
					validateNotEmpty(ctx, plate.wells[i].code, "wells["+i+"]"+".code");
					if(!plate.wells[i].typeCode.equals(plate.typeCode)){
//						addErrors(ctx, "wells["+i+"]"+".typeName", "plates.error.typecode.different", plate.typeName, plate.wells[i].typeName);
						ctx.addError("wells["+i+"]"+".typeName", "plates.error.typecode.different", plate.typeName, plate.wells[i].typeName);
					}
				}
				if (!isUpdate) {
					validatePlateCode(plate, ctx); 
				}
				for (int i = 0 ; i < plate.wells.length ; i++) {
					for (int j = 0 ; j < plate.wells.length ; j++) {
						if(i != j){
							if(plate.wells[i].code.equals(plate.wells[j].code)){
//								addErrors(errors, "wells["+i+"]"+".name", "plates.error.severalsamewellcode", plate.wells[i].name);
								ctx.addError("wells["+i+"]"+".name", "plates.error.severalsamewellcode", plate.wells[i].name);
							}
							if(plate.wells[i].x != null && plate.wells[i].y != null && plate.wells[i].x.equals(plate.wells[j].x) && plate.wells[i].y.equals(plate.wells[j].y)){
//								addErrors(errors, "wells["+i+"]", "plates.error.wellwithsamecoord", plate.wells[i].x, plate.wells[i].y);
								ctx.addError("wells["+i+"]", "plates.error.wellwithsamecoord", plate.wells[i].x, plate.wells[i].y);
							}							
						}
					}
				}
			}
		}
	}

//	private /*static*/ void validatePlateCode(Plate plate, Map<String, List<ValidationError>> errors) {
//		if (Spring.getBeanOfType(LimsManipDAO.class).isPlateExist(plate.code)) {
//			addErrors(errors, "code", "plates.error.code.exist");
//		}
//	}

	private void validatePlateCode(Plate plate, ContextValidation ctx) {
//		if (Spring.getBeanOfType(LimsManipDAO.class).isPlateExist(plate.code)) {
		if (limsManipDAO().isPlateExist(plate.code))
			ctx.addError("code", "plates.error.code.exist");
	}

//	private /*static*/ String newCode(Integer typeCode) {
//		String code = CodeHelper.getInstance().generateContainerSupportCode();
//		if(Integer.valueOf(12).equals(typeCode)){
//		    code = "FRG_"+code;
//		}else if(Integer.valueOf(13).equals(typeCode)){
//		    code = "LIB_"+code;
//		}else if(Integer.valueOf(18).equals(typeCode)){
//		    code = "PCR_"+code;
//		}else if(Integer.valueOf(14).equals(typeCode)){
//		    code = "STK_"+code;
//		}else{
//		    code = "PL_"+code;
//		}
//		return code;
//	}
	
	private String newCode(Integer typeCode) {
		String code = CodeHelper.getInstance().generateContainerSupportCode();
		switch (typeCode) {
		case 12 : code = "FRG_" + code; break;
		case 13 : code = "LIB_" + code; break;
		case 18 : code = "PCR_" + code; break;
		case 14 : code = "STK_" + code; break;
		default : code = "PL_"  + code;
		}
		return code;
	}

	public static LimsManipDAO limsManipDAO() {
		return play.api.modules.spring.Spring.getBeanOfType(LimsManipDAO.class);
	}
	
}
