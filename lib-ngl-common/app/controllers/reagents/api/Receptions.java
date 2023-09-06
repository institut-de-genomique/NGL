package controllers.reagents.api;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.reagents.CatalogsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.reagent.instance.AbstractReception;
import play.data.Form;

public class Receptions extends NGLAPIController<ReceptionsAPI, ReceptionsDAO, AbstractReception> implements StateController {

	private static final String NEW_COMMENT_PROPERTY = "newComment";

	private final Form<AbstractReception> form;

	@Inject
	public Receptions(NGLApplication app, ReceptionsAPI api, CatalogsAPI catalogsApi) {
		super(app, api, ReceptionSearchForm.class);
		this.form = app.formFactory().form(AbstractReception.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractReception saveImpl() throws APIException {
		final AbstractReception reception = this.getFilledForm(this.form, AbstractReception.class).get();
		return this.api().create(reception, this.getCurrentUser());
	}

	/**
	 * Check in query body if new comment has been defined, add new comment in
	 * comments list.
	 *
	 * @param reception
	 * @param user
	 */
	private void checkNewComment(AbstractReception reception, String user) {
		final JsonNode newComment = request().body().asJson().get(NEW_COMMENT_PROPERTY);
		if (newComment != null && newComment.isTextual()) {
			reception.addNewComment(newComment.asText(), user);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractReception updateImpl(String code) throws Exception, APIException, APIValidationException {
		final AbstractReception reception = this.getFilledForm(this.form, AbstractReception.class).get();

		final String user = this.getCurrentUser();
		this.checkNewComment(reception, user);

		if (reception.code.equals(code)) {
			if (api().isObjectExist(code) && !reception.state.code.equals(api().get(code).state.code)) {
				throw new Exception("You can not change the state code. Please use the state url ! ");
			}
			
			final QueryFieldsForm queryFieldsForm = this.filledFormQueryString(this.updateForm, QueryFieldsForm.class)
					.get();
			return CollectionUtils.isEmpty(queryFieldsForm.fields) ? this.api().update(reception, user)
					: this.api().update(reception, user, queryFieldsForm.fields);
			
		} else {
			throw new Exception("Receptions codes are not the same");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public Object updateStateImpl(String code, State state, String currentUser) throws APIException {
        return this.api().updateState(code, state, currentUser);
    }

}
