package controllers.samples.api;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import fr.cea.ig.ngl.dao.samples.SamplesDAO;
import models.laboratory.sample.instance.Sample;
import play.data.Form;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

public class Samples extends NGLAPIController<SamplesAPI, SamplesDAO, Sample> {
	
	private final Form<Sample> sampleForm;

	@Inject
	public Samples(NGLApplication app, SamplesAPI api) {
		super(app, api, SamplesSearchForm.class);
		this.sampleForm = app.formFactory().form(Sample.class);
	}

	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
	    return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Sample sample = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (sample == null) {
				return notFound();
			} 
			return okAsJson(sample);
		});	
	}	
	
	/* (non-Javadoc)
	 * @see controllers.NGLAPIController#saveImpl()
	 */
	@Override
	public Sample saveImpl() throws APIValidationException, APISemanticException {
		Sample input = getFilledForm(sampleForm, Sample.class).get();
		Sample s = api().create(input, getCurrentUser());
		return s;
	}

	/* (non-Javadoc)
	 * @see controllers.NGLAPIController#updateImpl(java.lang.String)
	 */
	@Override
	public Sample updateImpl(String code) throws Exception, APIException, APIValidationException {
		getLogger().debug("update Sample with code " + code);
		Form<Sample> filledForm = getFilledForm(sampleForm, Sample.class);
		Sample sampleInForm = filledForm.get();
		if (code.equals(sampleInForm.code)) { 
			QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
			Sample s = null;
			if (queryFieldsForm.fields == null) { 
				s = api().update(sampleInForm, getCurrentUser());
			} else {
				s = api().update(sampleInForm, getCurrentUser(), queryFieldsForm.fields);
			}
			return s;
		} else {
			throw new Exception("Sample codes are not the same");
		}
	}

}
