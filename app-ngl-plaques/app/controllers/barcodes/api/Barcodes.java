package controllers.barcodes.api;

import static validation.utils.ValidationHelper.required;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import controllers.CommonController;
import fr.cea.ig.play.migration.NGLContext;
import lims.cns.dao.LimsManipDAO;
import models.utils.CodeHelper;
//import play.Logger;
import play.api.modules.spring.Spring;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;

// TODO: fix readonly errors from the use of filledForm.errors()
public class Barcodes extends CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(Barcodes.class);
	
//	private final NGLContext ctx;
	
	@Inject
	public Barcodes(NGLContext ctx) {
//		this.ctx = ctx;
		form = ctx.form(BarcodesForm.class);
	}
	final /*static*/ Form<BarcodesForm> form;// = form(BarcodesForm.class);
	
	public /*static*/ Result save() {
		Form<BarcodesForm> filledForm = getFilledForm(form, BarcodesForm.class);
		BarcodesForm form = filledForm.get();
		Map<String, List<ValidationError>> errors    = new TreeMap<>();
		validate(form, errors);
		if (errors.isEmpty()) {
    	    Set<String> set = new TreeSet<>();
    	    logger.debug("number = " + form.number);
    	    for(int i = 0 ; i < form.number; i++){
    	    	String newCode = newCode(form.typeCode, form.projectCode);
    	    	Spring.getBeanOfType(LimsManipDAO.class).createBarcode(newCode, form.typeCode,getCurrentUser());
    	    	set.add(newCode);
    	    	
    	    }
    	    return ok(Json.toJson(set));
    	} 
    	return badRequest(NGLContext._errorsAsJson(errors));
	}
	
	public /*static*/ Result list(){
		return ok(Json.toJson(Spring.getBeanOfType(LimsManipDAO.class).findUnusedBarCodes()));
	}
	
	public /*static*/ Result delete(String code){
		Spring.getBeanOfType(LimsManipDAO.class).deletePlate(code);
		return ok();
	}
	
	private /*static*/ void validate(BarcodesForm form, Map<String, List<ValidationError>> errors) {
		if (required(errors, form, "form")) {
			required(errors, form.number,       "number");
			required(errors, form.typeCode,     "typeCode");
			required(errors, form.projectCode, "projectCode");
		}
	}

	private String newCode(Integer typeCode, String project) {
		String code = project.trim() + "_" + CodeHelper.getInstance().generateContainerSupportCode();
		switch (typeCode) {
		case 12 : code = "FRGE_" + code; break;
		case 13 : code = "LIBE_" + code; break;
		case 18 : code = "PCRE_" + code; break;
		case 14 : code = "STKE_" + code; break;
		default : code = "PLE_"  + code;
		}
		logger.debug(code);
		return code;
	}

//	private /*static*/ String newCode(Integer typeCode, String project) {
//		String code = project.trim() + "_" + CodeHelper.getInstance().generateContainerSupportCode();
//		if(Integer.valueOf(12).equals(typeCode)){
//		    code = "FRGE_"+code;
//		}else if(Integer.valueOf(13).equals(typeCode)){
//		    code = "LIBE_"+code;
//		}else if(Integer.valueOf(18).equals(typeCode)){
//		    code = "PCRE_"+code;
//		}else if(Integer.valueOf(14).equals(typeCode)){
//		    code = "STKE_"+code;
//		}else{
//		    code = "PLE_"+code;
//		}
//		logger.debug(code);
//		return code;
//	}
	
}
