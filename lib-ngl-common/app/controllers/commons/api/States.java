package controllers.commons.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.CommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.State;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class States extends CommonController {
	
    private final Form<StatesSearchForm> stateForm;

//    @Inject
//    public States(NGLContext ctx) {
//    	this.stateForm = ctx.form(StatesSearchForm.class);
//    }
    
    @Inject
    public States(NGLApplication ctx) {
    	this.stateForm = ctx.form(StatesSearchForm.class);
    }

    public Result list() throws DAOException {
		Form<StatesSearchForm> stateFilledForm = filledFormQueryString(
			stateForm, StatesSearchForm.class);
		StatesSearchForm statesSearch = stateFilledForm.get();
	
		List<State> values = new ArrayList<>(0);
		if (statesSearch.display != null) {
		    values = State.find.get().findByDisplayAndObjectTypeCode(statesSearch.display, ObjectType.CODE
			    .valueOf(statesSearch.objectTypeCode));
		}
		else {
			if (StringUtils.isNotBlank(statesSearch.objectTypeCode)) 
			    values = State.find.get().findByObjectTypeCode(ObjectType.CODE.valueOf(statesSearch.objectTypeCode));
			else 
				return notFound();
		}
	
		if (statesSearch.datatable) {
		    return ok(Json.toJson(new DatatableResponse<>(values, values
			    .size())));
		} else if (statesSearch.list) {
		    List<ListObject> valuesListObject = new ArrayList<>();
		    for (State s : values) {
		    	valuesListObject.add(new ListObject(s.code, s.name));
		    }
		    return ok(Json.toJson(valuesListObject));
		} else {
		    return ok(Json.toJson(values));
		}
    }

    public Result get(String code) throws DAOException {
		State state = State.find.get().findByCode(code);
		if (state != null) 
		    return ok(Json.toJson(state));
		return notFound();
    }
    
}
