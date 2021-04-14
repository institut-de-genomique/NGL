package controllers.reagents.api;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.reagents.CatalogsAPI;
import fr.cea.ig.ngl.dao.reagents.CatalogsDAO;
import models.laboratory.reagent.description.AbstractCatalog;
import play.data.Form;

public class Catalogs extends NGLAPIController<CatalogsAPI, CatalogsDAO, AbstractCatalog> {

	private final Form<AbstractCatalog> form;

	@Inject
	public Catalogs(NGLApplication app, CatalogsAPI api) {
		super(app, api, CatalogSearchForm.class);
		this.form = app.formFactory().form(AbstractCatalog.class);
	}

	@Override
	public AbstractCatalog saveImpl() throws APIException {
		final AbstractCatalog catalog = this.getFilledForm(this.form, AbstractCatalog.class).get();
		return this.api().create(catalog, this.getCurrentUser());
	}

	@Override
	public AbstractCatalog updateImpl(String code) throws Exception, APIException, APIValidationException {
		final AbstractCatalog catalog = this.getFilledForm(this.form, AbstractCatalog.class).get();

		if (!catalog.code.equals(code)) {
			throw new Exception("Catalog codes are not the same");
		}

		final String user = this.getCurrentUser();
		final QueryFieldsForm queryFieldsForm = this.filledFormQueryString(this.updateForm, QueryFieldsForm.class)
				.get();
		return CollectionUtils.isEmpty(queryFieldsForm.fields) ? this.api().update(catalog, user)
				: this.api().update(catalog, user, queryFieldsForm.fields);
	}

}
