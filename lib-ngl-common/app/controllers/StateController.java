package controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.commons.api.StateBatchElement;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.support.LoggerHolder;
import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import views.components.datatable.DatatableBatchResponseElement;

public interface StateController extends NGLApplicationHolder, NGLForms, LoggerHolder {

	/**
	 * Update the state of a resource (retrieved by its code).
	 * @param code the code of the object to update
	 * @return 	   HTTP result
	 */
	@Authenticated
	@Authorized.Write
	default Result updateState(String code) {
		Form<State> stateForm = getNGLApplication().formFactory().form(State.class);
		try {
			Form<State> filledForm =  getFilledForm(stateForm, State.class);
			State state = filledForm.get();
			state.date = new Date();
			state.user = getCurrentUser();
			return okAsJson(updateStateImpl(code, state, getCurrentUser()));
		} catch (APIValidationException e) {
		    if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
			if(e.getErrors() != null) {
				return badRequestAsJson(errorsAsJson(e.getErrors()));
			} else {
				return badRequestAsJson(e.getMessage());
			}
		} catch (APIException e) {
			getLogger().error(e.getMessage(), e);
			return badRequestAsJson(e.getMessage());
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
			return nglGlobalBadRequest(e.getMessage());
		}
	}
	
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	default Result updateStateBatch() {
		try {
			Form<StateBatchElement> batchElementForm =  getNGLApplication().formFactory().form(StateBatchElement.class);
			List<Form<StateBatchElement>> filledForms =  getFilledFormList(batchElementForm, StateBatchElement.class);
			final Lang lang = Http.Context.Implicit.lang();
			String currentUser = getCurrentUser();
			List<DatatableBatchResponseElement> response = filledForms.parallelStream()
					.map(filledForm -> {
						StateBatchElement element = filledForm.get();
						State state = element.data.state;
						state.date = new Date();
						state.user = currentUser;
						try {
							Object o = updateStateImpl(element.data.code, state, currentUser);
							return new DatatableBatchResponseElement(play.mvc.Http.Status.OK, o, element.index);
						} catch (APIValidationException e) {
							return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
						} catch (APIException e) {
							return new DatatableBatchResponseElement(play.mvc.Http.Status.BAD_REQUEST, element.index);
						}
					}).collect(Collectors.toList());
			return okAsJson(response);
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
			return nglGlobalBadRequest(e.getMessage());
		}
	}	
	
	/**
	 * These method defines the specific updateState behavior for each resource. 
	 * @param  code         code of resource object to update
	 * @param  state        new state of resource object
	 * @param currentUser   user
	 * @return              updated object
	 * @throws APIException exception
	 */
	public abstract Object updateStateImpl(String code, State state, String currentUser) throws APIException;
	
	public abstract JsonNode errorsAsJson(Map<String, List<ValidationError>> errors);
	public abstract JsonNode errorsAsJson(Lang lang, Map<String, List<ValidationError>> errors);
	public abstract Result nglGlobalBadRequest(String message);
	public abstract String getCurrentUser();
	public abstract Result okAsJson(Object o);
	public abstract Result badRequestAsJson(Object o);

}
