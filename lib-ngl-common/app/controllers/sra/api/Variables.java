package controllers.sra.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.support.LFWRequestParsing;
import fr.cea.ig.lfw.utils.CodeAndNameList;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.Executor;
import play.mvc.Result;
import services.sra.SraParameter;
import services.sra.VariableSRA;
import validation.ContextValidation;

// import controllers.CommonController;              // done
// public class Variables extends CommonController { // done

public class Variables extends NGLController implements LFWRequestParsing, Executor { 

	private final SraParameterAPI sraParameterAPI;
	@Inject
	public Variables(NGLApplication app, SraParameterAPI sraParameterAPI) {
		super(app);
		// form = form(VariablesSearchForm.class);
		this.sraParameterAPI = sraParameterAPI;
	}

	@Authenticated
	@Historized
	public Result list() {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {		
			// Form<VariablesSearchForm> filledForm = filledFormQueryString(form, VariablesSearchForm.class);
			// Form<VariablesSearchForm> filledForm = filledFormQueryString(VariablesSearchForm.class);
			// VariablesSearchForm variableSearch = filledForm.get();
			VariablesSearchForm variableSearch = objectFromRequestQueryString(VariablesSearchForm.class);
			//logger.debug("variableSearch {}", variableSearch);
			return list(variableSearch);
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}



	@Authenticated
	@Historized
	public Result get(String type, String code) {
		//logger.debug("Get {}  code {}", type, code);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			if (type.equalsIgnoreCase("strategySample")) {
				return okAsJson(new SraParameter(code,type, VariableSRA.mapStrategySample.get("code"))); // TODO: fix null
			} else if(type.equalsIgnoreCase("pseudoStateCodeToStateCodes")) {
				//return okAsJson(VariableSRA.mapPseudoStateCodeToStateCodes());
				return okAsJson(SraParameter.getParameters(type));
			}  else {
				return okAsJson(SraParameter.getParameter(type));
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}


	private Result list(VariablesSearchForm variableSearch) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			//logger.debug("variableSearch type " + variableSearch.type);
			switch(variableSearch.type.toLowerCase()) {
			case "strategysample" : 
				return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategySample).sort());
			case "strategystudy":
				return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategyStudy).sort());
			default :
				// si execution de la methode alors retour okAsJason, sinon affichage message erreur. Voir param result
				return result(() -> { return okAsJson(CodeAndNameList.from(sraParameterAPI.getParameter(variableSearch.type))); },
						"error while retrieving SraParameter collection");
			}

		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}



}
