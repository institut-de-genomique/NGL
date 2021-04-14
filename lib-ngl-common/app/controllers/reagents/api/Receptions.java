package controllers.reagents.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.reagents.CatalogsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.instance.AbstractReception;
import play.data.Form;
import play.mvc.Result;

public class Receptions extends NGLAPIController<ReceptionsAPI, ReceptionsDAO, AbstractReception> {

	private static final String NEW_COMMENT_PROPERTY = "newComment";

	private final Form<AbstractReception> form;

	private final CatalogsAPI catalogsApi;

	@Inject
	public Receptions(NGLApplication app, ReceptionsAPI api, CatalogsAPI catalogsApi) {
		super(app, api, ReceptionSearchForm.class);
		this.form = app.formFactory().form(AbstractReception.class);
		this.catalogsApi = catalogsApi;
	}

	private void searchByExperimentTypeCode(ReceptionSearchForm form) {
		// clear catalogRefCodes
		form.catalogRefCode = null;
		if (form.catalogRefCodes == null) {
			form.catalogRefCodes = new ArrayList<>();
		} else {
			form.catalogRefCodes.clear();
		}
		// search KitCatalogs by ExperimentTypeCode
		final List<String> kitCatalogCodes = new ArrayList<>();
		this.catalogsApi.getByExperimentTypeCode(form.experimentTypeCode)
				.forEach((AbstractCatalog catalog) -> kitCatalogCodes.add(catalog.code));
		// search BoxCatalogs and ReagentCatalogs by KitCatalogCodes
		this.catalogsApi.getByKitCatalogCode(kitCatalogCodes)
				// register catalogRefCodes
				.forEach((AbstractCatalog catalog) -> form.catalogRefCodes.add(catalog.catalogRefCode));
	}

	@Authenticated
	@Authorized.Read
	@Override
	public Result list() {
		final ReceptionSearchForm receptionForm = (ReceptionSearchForm) this
				.objectFromRequestQueryString(this.searchFormClass);

		if (StringUtils.isNotBlank(receptionForm.experimentTypeCode)) {
			this.searchByExperimentTypeCode(receptionForm);
		}

		return this.globalExceptionHandler(() -> {
			try {
				final Source<ByteString, ?> resultsAsStream = this.api()
						.list(new ListFormWrapper<>(receptionForm, form -> this.generateBasicDBObjectFromKeys(form)));
				return Streamer.okStream(resultsAsStream);
			} catch (final APIException e) {
				this.getLogger().error(e.getMessage(), e);
				return this.badRequestAsJson(e.getMessage());
			}
		});
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

		if (!reception.code.equals(code)) {
			throw new Exception("Receptions codes are not the same");
		}

		final QueryFieldsForm queryFieldsForm = this.filledFormQueryString(this.updateForm, QueryFieldsForm.class)
				.get();
		return CollectionUtils.isEmpty(queryFieldsForm.fields) ? this.api().update(reception, user)
				: this.api().update(reception, user, queryFieldsForm.fields);
	}

}
