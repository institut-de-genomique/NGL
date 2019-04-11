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
		// Form<VariablesSearchForm> filledForm = filledFormQueryString(form, VariablesSearchForm.class);
		// Form<VariablesSearchForm> filledForm = filledFormQueryString(VariablesSearchForm.class);
		// VariablesSearchForm variableSearch = filledForm.get();
		VariablesSearchForm variableSearch = objectFromRequestQueryString(VariablesSearchForm.class);
		logger.debug("variableSearch {}", variableSearch);
		return list(variableSearch);
	}

	@Authenticated
	@Historized
	public Result get(String type, String code) {
		logger.debug("Get {}  code {}", type, code);
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
			parameter.value = VariableSRA.mapStrategyStudy.get("code"); // TODO : fix null
			return ok(Json.toJson(parameter));*/
			return okAsJson(new SraParameter(code,type,VariableSRA.mapStrategyStudy.get("code"))); // TODO : fix null
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
				logger.debug("parameter {}",parameter);
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
	}

	// TODO: pack creation source using builder
	/*private Query getQuery(VariablesSearchForm form) {
		List<Query> queries = new ArrayList<Query>();
		Query query = null;
		if (StringUtils.isNotBlank(form.type)) { 
			queries.add(DBQuery.is("type", form.type));
		}
		if (StringUtils.isNotBlank(form.code)) { 
			queries.add(DBQuery.is("code", form.code));
		}
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}*/
	
//	private Query getQuery(VariablesSearchForm form) {
//		return query(and(is("type", form.type), 
//				         is("code", form.code)));
//	}
	
	private Result list(VariablesSearchForm variableSearch) {
		logger.debug("variableSearch type " + variableSearch.type);
		// if (variableSearch.type != null && variableSearch.type.equalsIgnoreCase("strategySample")) {
		// type 'strategyStudy' and 'strategySample' are not in SraParameters table
		if ("strategySample".equalsIgnoreCase(variableSearch.type)) {
			// return ok(Json.toJson(toListObjects(VariableSRA.mapStrategySample)));
			return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategySample).sort());
		// } else if (variableSearch.type != null && variableSearch.type.equalsIgnoreCase("strategyStudy")) {
		} else if ("strategyStudy".equalsIgnoreCase(variableSearch.type)) {
			// return ok(Json.toJson(toListObjects(VariableSRA.mapStrategyStudy)));
			return okAsJson(CodeAndNameList.from(VariableSRA.mapStrategyStudy).sort());
		} else {
			/*Query query = getQuery(variableSearch);		

			List<SraParameter> values = MongoDBDAO.find(InstanceConstants.SRA_PARAMETER_COLL_NAME, SraParameter.class, query).toList();

			List<ListObject> valuesListObject = new ArrayList<ListObject>();
			for (SraParameter s : values) {
				valuesListObject.add(new ListObject(s.code, s.value));
			}
			return ok(Json.toJson(valuesListObject));*/
			return result(() -> { return okAsJson(CodeAndNameList.from(getSraParameterAPI().getParameter(variableSearch.type))); },
					      "error while retrieving SraParameter collection");
			/*try {
				return okAsJson(CodeAndNameList.from(getSraParameterAPI().find(getQuery(variableSearch)),
						                             x -> x.code, x -> x.value));
			} catch (Exception e) {
				return failure(logger,"error while retrieving SraParameter collection",e);
			}*/
		}
	}

	/*private List<ListObject> toListObjects(Map<String, String> map){
		List<ListObject> lo = new ArrayList<ListObject>();
		for(String key : map.keySet()){
			lo.add(new ListObject(key, map.get(key)));
		}
		//Sort by code
		Collections.sort(lo, new Comparator<ListObject>(){
			public int compare(ListObject lo1, ListObject lo2) {
				return lo1.code.compareTo(lo2.code);
			}
		});
		return lo;
	}*/

}
