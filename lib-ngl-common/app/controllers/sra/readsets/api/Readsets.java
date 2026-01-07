package controllers.sra.readsets.api;



import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.sra.api.ReadsetAPI;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import models.sra.submit.sra.instance.Readset;
import play.data.Form;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

public class Readsets extends  NGLAPIController<ReadsetAPI, ReadsetDAO, Readset>  {

	private static final play.Logger.ALogger         logger                     = play.Logger.of(Readsets.class);
	private final Form<Readset>                      readsetForm;



	@Inject
	public Readsets(NGLApplication app,
					   ReadsetAPI  api) {

		super(app, api, ReadsetsSearchForm.class);
		this.readsetForm             = app.formFactory().form(Readset.class);
	}


	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
		return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Readset readset = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (readset == null) {
				return notFound();
			} 
			return okAsJson(readset);
		});	
	}

	@Override
	public Readset saveImpl() throws APIException {
		throw new APIException("methode non implementee");
		//return null;
	}

	@Override
	public Readset updateImpl(String code) throws Exception, APIException, APIValidationException {
		throw new APIException("methode non implementee");
		//return null;
	}	


}
