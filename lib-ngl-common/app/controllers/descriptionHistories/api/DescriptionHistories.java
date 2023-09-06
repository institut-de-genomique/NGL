package controllers.descriptionHistories.api;

import fr.cea.ig.ngl.dao.api.APIException;
import javax.inject.Inject;
import controllers.NGLAPIController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesAPI;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesDAO;
import models.laboratory.descriptionHistory.instance.DescriptionHistory;
import play.data.Form;
import play.mvc.Result;


public class DescriptionHistories extends NGLAPIController<DescriptionHistoriesAPI, DescriptionHistoriesDAO, DescriptionHistory> {

  private final Form<DescriptionHistory> descriptionHistoryForm;

  @Inject
	public DescriptionHistories(NGLApplication app, DescriptionHistoriesAPI api) {
    super(app,api,null);
		this.descriptionHistoryForm = app.formFactory().form(DescriptionHistory.class);
	}

  
  @Override
  public DescriptionHistory saveImpl() throws APIException {
      Form<DescriptionHistory> filledForm = getFilledForm(descriptionHistoryForm, DescriptionHistory.class);
      DescriptionHistory descriptionHistoryInForm = filledForm.get();
      return api().create(descriptionHistoryInForm, getCurrentUser());
	}

  @Override
  public Result get(String code) {
    return okAsJson(api().getAllHistory(code));
  }

  @Override
  public DescriptionHistory updateImpl(String code) throws Exception, APIException, APIValidationException {
    // TODO Auto-generated method stub
     return null;
  }
}