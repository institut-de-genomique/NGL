package controllers.printing.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.APICommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.printing.Tag;
import models.utils.InstanceConstants;
import play.api.modules.spring.Spring;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.print.PrinterService;
import validation.ContextValidation;


public class Tags extends APICommonController<Tag> {
	
	private final Form<TagPrintForm> printForm;
	
//	@Inject
//	public Tags(NGLContext ctx) {
//		super(ctx,Tag.class);
//		printForm = ctx.form(TagPrintForm.class);
//	}

	@Inject
	public Tags(NGLApplication app) {
		super(app,Tag.class);
		printForm = app.form(TagPrintForm.class);
	}

	public Result list() {
		TagListForm form = filledFormQueryString(TagListForm.class);
		List<Object> facts = getFacts(form);
		// List<Object> tags = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), "tags", facts);
		List<Object> tags = ((NGLApplication)app).rulesServices6("tags", facts);
		return ok(Json.toJson(tags));
	}

	public Result print() {
		Form<TagPrintForm> form = getFilledForm(printForm, TagPrintForm.class);
		TagPrintForm input = form.get();
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), form.errors());
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser(), form);
		
		Spring.getBeanOfType(PrinterService.class).printTags(input.printerCode, input.barcodePositionId, input.tags, ctxVal);
		if (!ctxVal.hasErrors()) {
			return ok();
		} else {
			// return badRequest(form.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	private List<Object> getFacts(TagListForm form) {
		List<Object> facts = new ArrayList<>();	
		if (StringUtils.isNotBlank(form.experimentCode)) {
			Experiment exp = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, form.experimentCode);		
			facts.add(exp);
		} else if(CollectionUtils.isNotEmpty(form.containerSupportCodes)) {
			List<ContainerSupport> supports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("code", form.containerSupportCodes)).toList();
			facts.addAll(supports);			
		}		
		return facts;
	}
	
}
