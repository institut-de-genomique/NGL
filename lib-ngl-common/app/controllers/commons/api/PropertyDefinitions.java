package controllers.commons.api;

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
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class PropertyDefinitions extends APICommonController<PropertyDefinition> {

	private final static play.Logger.ALogger logger = play.Logger.of(PropertyDefinition.class);
	
	private final Form<PropertyDefinitionsSearchForm> form;

	@Inject
	public PropertyDefinitions(NGLApplication app) {
		super(app,PropertyDefinition.class);	
		form = getNGLContext().form(PropertyDefinitionsSearchForm.class);
	}

	public  Result list() throws DAOException {
		Form<PropertyDefinitionsSearchForm> filledForm =  filledFormQueryString(form, PropertyDefinitionsSearchForm.class);
		PropertyDefinitionsSearchForm propertyDefinitionsSearch = filledForm.get();
		List<PropertyDefinition> values;
		
		if(propertyDefinitionsSearch.objectTypeCode != null) {
			values = PropertyDefinition.find.get().findAllByObjectType(
					propertyDefinitionsSearch.objectTypeCode, 
					propertyDefinitionsSearch.code, propertyDefinitionsSearch.codes, 
					propertyDefinitionsSearch.typeName, 
					propertyDefinitionsSearch.typeNames
					);
		} else if (propertyDefinitionsSearch.levelCode != null) {
			values = PropertyDefinition.find.get().findUnique(Level.CODE.valueOf(propertyDefinitionsSearch.levelCode));
		} else {
			values = PropertyDefinition.find.get().findUnique();
		}
		
		if (propertyDefinitionsSearch.datatable) {
			return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if(propertyDefinitionsSearch.list) {
			return ok(Json.toJson(values.parallelStream().map(pd -> pd.code).distinct().map(code -> new ListObject(code,code)).collect(Collectors.toList())));
		} else if(propertyDefinitionsSearch.count) {
			Map<String, Integer> m = new HashMap<>(1);
			m.put("result", values.size());
			return ok(Json.toJson(m));
		} else {
			return ok(Json.toJson(values));
		}
	}
	
	
	
}
