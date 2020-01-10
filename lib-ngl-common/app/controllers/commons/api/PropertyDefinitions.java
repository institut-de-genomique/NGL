package controllers.commons.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class PropertyDefinitions extends APICommonController<PropertyDefinition> {

//	@Inject
//	public PropertyDefinitions(NGLContext ctx) {
//		super(ctx,PropertyDefinition.class);		
//	}

	@Inject
	public PropertyDefinitions(NGLApplication app) {
		super(app,PropertyDefinition.class);		
	}

	public  Result list() throws DAOException {
		DynamicForm filledForm =  listForm.bindFromRequest();
		
		List<PropertyDefinition> values = new ArrayList<>(0);
		if (filledForm.get("levelCode") != null) {
			values = PropertyDefinition.find.get().findUnique(Level.CODE.valueOf(filledForm.get("levelCode")));
		} else {
			values = PropertyDefinition.find.get().findUnique();
		}
		
		if (filledForm.get("datatable") != null) {
			return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if(filledForm.get("list") != null) {
			return ok(Json.toJson(values.parallelStream().map(pd -> new ListObject(pd.code,pd.code)).collect(Collectors.toList())));
		} else if(filledForm.get("count") != null) {
			Map<String, Integer> m = new HashMap<>(1);
			m.put("result", values.size());
			return ok(Json.toJson(m));
		} else {
			return ok(Json.toJson(values));
		}				
	}
	
	
	
}
