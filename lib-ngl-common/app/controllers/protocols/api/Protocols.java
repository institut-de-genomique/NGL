package controllers.protocols.api;


import javax.inject.Inject;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsDAO;
import models.laboratory.protocol.instance.Protocol;
import play.data.Form;
import play.mvc.Result;

public class Protocols extends NGLAPIController<ProtocolsAPI, ProtocolsDAO, Protocol> {
	
	private final Form<Protocol> protocolForm;

	@Inject
	public Protocols(NGLApplication app, ProtocolsAPI api) {
		super(app, api, ProtocolsSearchForm.class);
		this.protocolForm = app.formFactory().form(Protocol.class);
	}

	@Override
	public Protocol saveImpl() throws APIException {
		Protocol input = getFilledForm(protocolForm, Protocol.class).get();
		return api().create(input, getCurrentUser());
	}

	@Override
    public Result delete(String code){
        return globalExceptionHandler(() -> {
            try {
                api().remove(code, getCurrentUser());
                return ok();
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            } 
        });
    }

	@Override
	@Authorized.Admin
	public Protocol updateImpl(String code) throws Exception, APIException, APIValidationException {
		Form<Protocol> filledForm = getFilledForm(protocolForm, Protocol.class);
		Protocol protocolInForm = filledForm.get();
		
		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
        QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();

		if (code.equals(protocolInForm.code)) { 
			String comment = queryFieldsForm.comment;
			Protocol protocolInBase  = api().update(protocolInForm, getCurrentUser(), comment);
			
			return protocolInBase ;
		} else {
			throw new Exception("Protocol codes are not the same");
		}
	}	
	
}
