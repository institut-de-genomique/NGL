package controllers.sra.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.support.LFWRequestParsing;
import fr.cea.ig.lfw.utils.CodeAndNameList;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.Executor;
import fr.cea.ig.ngl.support.api.SraParameterAPIHolder;
import models.sra.submit.util.SraParameter;
import models.sra.submit.util.VariableSRA;
import play.mvc.Result;
import validation.ContextValidation;

// import controllers.CommonController;              // done
// public class Variables extends CommonController { // done

public class Variables extends NGLController 
implements LFWRequestParsing, SraParameterAPIHolder, Executor { 

	// private static final play.Logger.ALogger logger = play.Logger.of(Variables.class);

	// private final NGLContext                ctx;
	// private final Form<VariablesSearchForm> form;// = form(VariablesSearchForm.class);

	@Inject
	public Variables(NGLApplication app) {
		super(app);
		// form = form(VariablesSearchForm.class);
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
	public Result get_old(String type, String code) {
		//logger.debug("Get {}  code {}", type, code);
		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			if (type.equalsIgnoreCase("strategySample")) {
				/*SraParameter parameter = new SraParameter();
			parameter.code  = code;
			parameter.type  = type;
			parameter.value = VariableSRA.mapStrategySample.get("code"); // TODO: fix null
			return ok(Json.toJson(parameter)); */
				return okAsJson(new SraParameter(code,type, VariableSRA.mapStrategySample.get("code"))); // TODO: fix null
			} else if ("strategyStudy".equalsIgnoreCase(type)) {
				/*SraParameter parameter = new SraParameter();
			parameter.code  = code;
			parameter.type  = type;
			parameter.value = VariableSRA.mapStrategyStudy.get("code"); 
			return ok(Json.toJson(parameter));*/
				return okAsJson(new SraParameter(code,type,VariableSRA.mapStrategyStudy.get("code"))); 
				
//			} else if ("pseudoStateCodeToStateCodes".equalsIgnoreCase(type)) {
//				 SraParameter parameter = MongoDBDAO.findOne(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.and(DBQuery.is("code", code),DBQuery.is("type", type)));
//				 List<SraParameter> parameters = (List<SraParameter>) MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.and(DBQuery.is("code", code),DBQuery.is("type", type)));
//					logger.debug("parameter " + parameter);
//					if (parameter != null) {
//						return okAsJson(parameter);				
//
//					} 
			} else {
				/* SraParameter parameter = MongoDBDAO.findOne(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, DBQuery.and(DBQuery.is("code", code),DBQuery.is("type", type)));
			logger.debug("parameter " + parameter);
			if (parameter != null) {
				return ok(Json.toJson(parameter));
			} else { 
				return notFound(); 
			}*/
				return result(() -> {
					SraParameter parameter = getSraParameterAPI().findOneByCodeAndType(code,type);
					
					//logger.debug("parameter {}",parameter);
					return okAsJson(parameter);				
				}, "SraParameter lookup failed (code:'" + code + "',type:'" + type + "')");

				/*try {
				SraParameter parameter = getSraParameterAPI().findOneByCodeAndType(code,type); 
				logger.debug("parameter {}",parameter);
				return okAsJson(parameter);
			} catch (DAOEntityNotFoundException e) {
				return notFound(); 
			} catch (Exception e) {
				return failure(logger,"SraParameter lookup failed (code:'" + code + "',type:'" + type + "')",e);
			}*/
			}
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
			// if (variableSearch.type != null && variableSearch.type.equalsIgnoreCase("strategySample")) {
			// type 'strategyStudy' and 'strategySample' are not in SraParameters table
			if ("strategySample".equalsIgnoreCase(variableSearch.type)) {
				return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategySample).sort());
				// } else if (variableSearch.type != null && variableSearch.type.equalsIgnoreCase("strategyStudy")) {
			} else if ("strategyStudy".equalsIgnoreCase(variableSearch.type)) {
				return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategyStudy).sort());
			} else {
				return result(() -> { return okAsJson(CodeAndNameList.from(getSraParameterAPI().getParameter(variableSearch.type))); },
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
