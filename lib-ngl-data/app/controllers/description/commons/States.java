package controllers.description.commons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.utils.ModelDAOs;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.description.common.StateService;

public class States extends Controller {
	
	private static final play.Logger.ALogger logger = play.Logger.of(States.class);
	
	private final ModelDAOs mdao;
	
	@Inject
	public States(ModelDAOs mdao) {
		this.mdao = mdao;
	}
	
	public Result save() {
		try {
			Map<String, List<ValidationError>> errors = new HashMap<>();
			new StateService(mdao).saveData(errors);
			if (errors.size() > 0) {
				return badRequest(Json.toJson(errors));
			} else {
				return ok();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return internalServerError(e.getMessage());
		}				
	}

}
