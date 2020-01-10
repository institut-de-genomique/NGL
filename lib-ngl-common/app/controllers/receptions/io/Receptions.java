package controllers.receptions.io;

import javax.inject.Inject;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reception.instance.ReceptionConfiguration;
import models.utils.InstanceConstants;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.io.reception.FileService;
import services.io.reception.ReceptionFileService;
import validation.ContextValidation;

public class Receptions extends TPLCommonController {
	
	private final NGLApplication          app;
	private final Form<PropertyFileValue> fileForm;
	private final ReceptionFileService    receptionFileService;

	@Inject
	public Receptions(NGLApplication app, ReceptionFileService receptionFileService) {
		fileForm                  = app.form(PropertyFileValue.class);
		this.app                  = app;
		this.receptionFileService = receptionFileService;
	}

	private ReceptionConfiguration getReceptionConfig(String code) {
		return MongoDBDAO.findByCode(InstanceConstants.RECEPTION_CONFIG_COLL_NAME, ReceptionConfiguration.class, code);
	}
	
	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value={"writing"})
	public Result importFile(String receptionConfigCode) {
		ReceptionConfiguration configuration = getReceptionConfig(receptionConfigCode);
		if (configuration == null)
			return badRequest("ReceptionConfiguration not exist");
		Form<PropertyFileValue> filledForm = getFilledForm(fileForm,PropertyFileValue.class);
		PropertyFileValue pfv = filledForm.get();
		if (pfv == null) 
			return badRequest("missing file");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser(), filledForm);
		try {				
			FileService fileService = receptionFileService.getFileService(configuration, pfv, contextValidation);
			fileService.analyse();
		} catch (Throwable e) {
			e.printStackTrace();
			contextValidation.addError("Error", (e.getMessage() != null)?e.getMessage():"null");
		}
		if (contextValidation.hasErrors()) 
			return badRequest(app.errorsAsJson(contextValidation.getErrors()));
		return ok();
	}
	
}
