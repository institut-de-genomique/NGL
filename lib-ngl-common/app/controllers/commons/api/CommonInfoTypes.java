package controllers.commons.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.CommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.ObjectType;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;


public class CommonInfoTypes extends CommonController {
	
//	private final NGLContext ctx;
//	
//	@Inject
//	public CommonInfoTypes(NGLContext ctx) {
//		this.ctx = ctx;
//	}
	
	private final NGLApplication app;
	
	@Inject
	public CommonInfoTypes(NGLApplication app) {
		this.app = app;
	}

	public Result list() throws DAOException {
//		DynamicForm filledForm =  listForm().bindFromRequest();
		DynamicForm filledForm =  app.form().bindFromRequest();
		List<CommonInfoType> values = new ArrayList<>(0);
		if (filledForm.get("objectTypeCode") != null) {
			values = CommonInfoType.find.get().findByObjectTypeCode(ObjectType.CODE.valueOf(filledForm.get("objectTypeCode")));
		}
		if (filledForm.get("datatable") != null) {
			return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if(filledForm.get("list") != null) {
			return ok(Json.toJson(ListObject.from(values)));
		} else {
			return ok(Json.toJson(values));
		}
	}
	
}
