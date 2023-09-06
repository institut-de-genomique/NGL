package controllers.commons.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;

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
					propertyDefinitionsSearch.name, propertyDefinitionsSearch.names,
					propertyDefinitionsSearch.typeName, propertyDefinitionsSearch.typeNames
					);
		}  else if (propertyDefinitionsSearch.levelCode != null) {
			values = PropertyDefinition.find.get().findUnique(Level.CODE.valueOf(propertyDefinitionsSearch.levelCode));
		} else {
			values = PropertyDefinition.find.get().findUnique();
		}

		if (propertyDefinitionsSearch.datatable) {
			return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if(propertyDefinitionsSearch.list) {
			return ok(Json.toJson(toUniqueListObjects(values)));
		} else if(propertyDefinitionsSearch.count) {
			Map<String, Integer> m = new HashMap<>(1);
			m.put("result", values.size());
			return ok(Json.toJson(m));
		} else {
			return ok(Json.toJson(values));
		}
	}
	
	private List<ListObject> toUniqueListObjects(List<PropertyDefinition> values) {
		return values.stream()
		.collect(Collectors.groupingBy((PropertyDefinition pd) -> new ImmutablePair<>(pd.code, pd.getName()), Collectors.counting()))
		.keySet().parallelStream()
		.map(pair -> new ListObject(pair.getLeft(), pair.getRight()))
		.sorted((listObject1, listObject2) -> listObject1.code.compareTo(listObject2.code))
		.collect(Collectors.toList());
	}
	
}
