package controllers.containers.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsAPI;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.CodeHelper;
import play.data.Form;
import play.mvc.Result;
import workflows.container.ContSupportWorkflows;

public class ContainerSupports extends NGLAPIController<ContainerSupportsAPI, ContainerSupportsDAO, ContainerSupport> implements StateController {
	
	private final Form<ContainerSupport>             containerSupportForm;
	
	@Inject
	public ContainerSupports(NGLApplication app, ContainerSupportsAPI api, ContSupportWorkflows workflows) {
		super(app, api, ContainerSupportsSearchForm.class);
		containerSupportForm       = app.formFactory().form(ContainerSupport.class);
	}
	
	@Override
	public Object updateStateImpl(String code, State state, String currentUser) throws APIValidationException, APIException {
		return api().updateState(code, state, currentUser);
	}
	
	
	@Override
	public ContainerSupport saveImpl()throws APIValidationException, APIException {
		ContainerSupport input = getFilledForm(containerSupportForm, ContainerSupport.class).get();
		ContainerSupport cs = api().create(input, getCurrentUser());
		return cs;
	}

	@Override
	public ContainerSupport updateImpl(String code) throws Exception, APIException, APIValidationException {
		ContainerSupport input = getFilledForm(containerSupportForm, ContainerSupport.class).get();
		if(code.equals(input.code)) { 
			ContainerSupport containerSupportInDB = api().get(code);
			if (!containerSupportInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
			
			QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
			ContainerSupport cs = null;
				if(queryFieldsForm.fields == null) { 
					cs = api().update(input, getCurrentUser());
				} else {
					cs = api().update(input, getCurrentUser(), queryFieldsForm.fields);
				}
				return cs;
		} else {
			throw new Exception("Container support codes are not the same");
		}
	}
	
	@Authenticated
	@Authorized.Write
	public Result saveCode(Integer numberOfCode) {
	    return globalExceptionHandler(() -> {
			List<String> codes = new ArrayList<>(numberOfCode);
			IntStream.range(0, numberOfCode).forEach(i -> {
				codes.add(CodeHelper.getInstance().generateContainerSupportCode());
			});
			return okAsJson(codes);
	    });
	}

}

