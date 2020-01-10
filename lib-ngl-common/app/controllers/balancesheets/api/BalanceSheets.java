package controllers.balancesheets.api;

import javax.inject.Inject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.NGLAPIController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.balancesheets.BalanceSheetsAPI;
import fr.cea.ig.ngl.dao.balancesheets.BalanceSheetsDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.balancesheet.instance.BalanceSheet;
import play.data.Form;
import play.mvc.Result;

public class BalanceSheets extends NGLAPIController<BalanceSheetsAPI, BalanceSheetsDAO, BalanceSheet>{

	private final Form<BalanceSheet> sampleForm;

	@Inject
	public BalanceSheets(NGLApplication app, BalanceSheetsAPI api) {
		super(app, api, BalanceSheetsSearchForm.class);
		this.sampleForm = app.formFactory().form(BalanceSheet.class);
	}	
	
	@Override
	@Authenticated
    @Authorized.Read
    public Result list() {
		return globalExceptionHandler(() -> {
            try {
                Source<ByteString, ?> resultsAsStream = api().list(new ListFormWrapper<>(objectFromRequestQueryString(this.searchFormClass), form -> generateBasicDBObjectFromKeys(form)));
                return Streamer.okStream(resultsAsStream);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }

	@Override
	public BalanceSheet saveImpl() throws APIException {
		BalanceSheet bs = getFilledForm(sampleForm, BalanceSheet.class).get();
		return api().create(bs, getCurrentUser());
	}

	@Override
	public BalanceSheet updateImpl(String code) throws Exception, APIException, APIValidationException {
		BalanceSheet bs = getFilledForm(sampleForm, BalanceSheet.class).get();
		return api().update(bs, getCurrentUser());
	}

}
