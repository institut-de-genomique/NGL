package controllers.commons.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.Value;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class Values extends APICommonController<ValuesSearchForm> {
	
    private final Form<ValuesSearchForm> form;

//    @Inject
//    public Values(NGLContext ctx) {
//    	super(ctx, ValuesSearchForm.class);
//    	this.form = ctx.form(ValuesSearchForm.class);
//    }
    
    @Inject
    public Values(NGLApplication app) {
    	super(app, ValuesSearchForm.class);
    	this.form = app.form(ValuesSearchForm.class);
    }

    public Result list() throws DAOException {
		Form<ValuesSearchForm> filledForm = filledFormQueryString(
				form, ValuesSearchForm.class);
		ValuesSearchForm valuesSearch = filledForm.get();
	
		List<Value> values = new ArrayList<>(0);
		if (StringUtils.isNotBlank(valuesSearch.propertyDefinitionCode)) 
		    values = Value.find.get().findUnique(valuesSearch.propertyDefinitionCode);
		else 
			return notFound();
	
		if (valuesSearch.datatable) {
		    return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if (valuesSearch.list) {
		    List<ListObject> valuesListObject = new ArrayList<>();
		    for (Value s : values) {
		    	valuesListObject.add(new ListObject(s.code, s.name));
		    }
		    return ok(Json.toJson(valuesListObject));
		} else {
		    return ok(Json.toJson(values));
		}
    }
    
}
