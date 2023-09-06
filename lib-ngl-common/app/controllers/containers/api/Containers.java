package controllers.containers.api;


import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.dao.containers.ContainersDAO;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import play.data.Form;
import workflows.container.ContWorkflows;


public class Containers extends NGLAPIController<ContainersAPI, ContainersDAO, Container> implements StateController {
	
	private final Form<Container> form;
	
	@Inject
	public Containers(NGLApplication app, ContainersAPI api, ContWorkflows workflows) {
		super(app, api, ContainersSearchForm.class);
		form = app.formFactory().form(Container.class);
	}

	@Override
	public Container saveImpl() throws APIValidationException, APISemanticException {
		Container input = getFilledForm(form, Container.class).get();
		Container c = api().create(input, getCurrentUser());
		return c;
	}

	@Override
	public Container updateImpl(String code) throws Exception, APIException, APIValidationException {
		Container input = getFilledForm(form, Container.class).get();
		if (code.equals(input.code)) { 
			
			QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
			Container c = null;
				if(queryFieldsForm.fields == null) { 
					Container containerInDB = api().get(code);
					if (!containerInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
					c = api().update(input, getCurrentUser());
				} else {
					c = api().update(input, getCurrentUser(), queryFieldsForm.fields);
				}
				return c;
		} else {
			throw new Exception("Container codes are not the same");
		}
	}

	@Override
	public Object updateStateImpl(String code, State state, String currentUser) throws APIValidationException, APIException {
		return api().updateState(code, state, currentUser);
	}

}
