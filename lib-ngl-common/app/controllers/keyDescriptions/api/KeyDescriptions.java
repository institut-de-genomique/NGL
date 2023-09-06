package controllers.keyDescriptions.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.keyDescriptions.KeyDescriptionAPI;
import fr.cea.ig.ngl.dao.keyDescriptions.KeyDescriptionDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.administration.authorisation.instance.KeyDescription;
import play.data.Form;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.smrtlink.RunDesign;
import services.smrtlink.SMRTLinkAuthorizationException;
import services.smrtlink.SMRTLinkServices;

public class KeyDescriptions extends NGLAPIController<KeyDescriptionAPI, KeyDescriptionDAO, KeyDescription> {
	
	private final Form<KeyDescription>              form;
	private final SMRTLinkServices smrtLinkService;

	@Inject
	public KeyDescriptions(NGLApplication app, KeyDescriptionAPI api, SMRTLinkServices smrtLinkService) {
		super(app, api, KeyDescriptionsSearchForm.class);
		this.form             = app.formFactory().form(KeyDescription.class);
		this.smrtLinkService = smrtLinkService;
	}
	
	@Authenticated
	@Authorized.Admin
    @Authorized.Read
    public Result list() {
        return super.list();
    }

    @Authenticated
    @Authorized.Admin
    @Authorized.Read
    public Result get(String code) {
        return super.get(code);
    }

    @Authenticated
	@Authorized.Admin
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result save() {
        return super.save();
    }
    
    @Authenticated
    @Authorized.Read
	public Result init(String code) throws APIException {
		try {
			String apiKey = api().init(code, getCurrentUser());
			return ok(apiKey);
        } catch (APIValidationException e) {
            return badRequestLoggingForValidationException(e);
        } catch (APIException e) {
            getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return nglGlobalBadRequest(e.getMessage());
        }
	}
	
	@Authenticated
	@AuthorizedApiKey.Renew
	@Authorized.Read
	public Result renew(String code) throws APIException {
		try {
			String apiKey = api().renew(code, getCurrentUser());
			return ok(apiKey);
        } catch (APIValidationException e) {
            return badRequestLoggingForValidationException(e);
        } catch (APIException e) {
            getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return nglGlobalBadRequest(e.getMessage());
        }
	}
	
	@Authenticated
	@Authorized.Admin
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result update(String code) {
        return super.update(code);
    }
	
	@Authenticated
	@Authorized.Admin
    @Authorized.Write
	@Override
	public Result delete(String code){ 
		return super.delete(code);
	}
	
	@Override
	public KeyDescription saveImpl() throws APIException {
		KeyDescription input = getFilledForm(form, KeyDescription.class).get();
        return api().create(input, getCurrentUser());
	}
	
	private boolean isFormUpdate(QueryFieldsForm queryFieldsForm) {
		return queryFieldsForm.fields != null;
	}
	
	private void handleKeyCodesConsistency(String code, KeyDescription input) {
		if (!code.equals(input.code)) {
			throw new IllegalStateException("Key codes are not the same");
        }
	}
	
	private KeyDescription doSimpleUpdate(String code, KeyDescription input) throws APIValidationException, APIException {
		handleKeyCodesConsistency(code, input);
		return api().update(input, getCurrentUser());
	}
	
	private KeyDescription doFormUpdate(String code, KeyDescription input, QueryFieldsForm queryFieldsForm) throws APIValidationException, APIException {
		if (input.code == null) {
			input.code = code; // in ngl-bi the code is not in input object when only specific fields are updated 
		}
        return api().update(input, getCurrentUser(), queryFieldsForm.fields);
	}

	@Override
	public KeyDescription updateImpl(String code) throws APIException, APIValidationException {
		KeyDescription input = getFilledForm(form, KeyDescription.class).get();
        QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
        if (isFormUpdate(queryFieldsForm)) {
        	return doFormUpdate(code, input, queryFieldsForm);
        } else {
        	return doSimpleUpdate(code, input);
        }
	}
	
	public Result testCallSMRTLinkStatus() {
		try {
			CompletableFuture<WSResponse> future = this.smrtLinkService.getStatus().toCompletableFuture();
			WSResponse response = future.get();
			return okAsJson(response.getBody());
		} catch (InterruptedException | ExecutionException | SMRTLinkAuthorizationException e) {
			getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
		}
	}
	
	public Result testCallSMRTLinkRunDesign(String code) {
		try {
			CompletableFuture<WSResponse> future = this.smrtLinkService.getRunDesign(code).toCompletableFuture();
			WSResponse response = future.get();
			return okAsJson(response.getBody());
		} catch (InterruptedException | ExecutionException | SMRTLinkAuthorizationException e) {
			getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
		}
	}
	
	public Result testCallSMRTLinkImport() {
		try {
			CompletableFuture<WSResponse> future = this.smrtLinkService.importRunDesign(new RunDesign()).toCompletableFuture();
			WSResponse response = future.get();
			return okAsJson(response.getBody());
		} catch (InterruptedException | ExecutionException | SMRTLinkAuthorizationException e) {
			getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
		}
	}

}
