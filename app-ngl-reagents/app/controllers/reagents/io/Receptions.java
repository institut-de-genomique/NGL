package controllers.reagents.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import controllers.reagents.io.receptions.helpers.ReceptionBuilder;
import controllers.reagents.io.receptions.helpers.ReceptionFiller;
import controllers.reagents.io.receptions.helpers.ReceptionRowProcessor;
import controllers.reagents.io.receptions.helpers.ReceptionsFileReader;
import controllers.reagents.io.receptions.mappers.AbstractReceptionMapper;
import controllers.reagents.io.receptions.mappers.impl.ReceptionMapperProvider;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reagent.instance.AbstractReception;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;
import validation.reagent.instance.ReceptionValidationHelper;

public class Receptions extends TPLCommonController {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger LOGGER = play.Logger.of(Receptions.class);

	/**
	 * Application.
	 */
	private final NGLApplication app;

	/**
	 * File form.
	 */
	private final Form<PropertyFileValue> fileForm;

	/**
	 * Receptions API
	 */
	private final ReceptionsAPI api;
	
	/**
	 * Reception Mapper Provider
	 */
	private final ReceptionMapperProvider receptionMapperProvider;
	
	/**
	 * Reception Builder
	 */
	private final ReceptionBuilder receptionBuilder;
	
	/**
	 * Reception Filler
	 */
	private final ReceptionFiller receptionFiller;
	
	/**
	 * Receptions File reader
	 */
	private final ReceptionsFileReader receptionsFileReader;

	@Inject
	public Receptions(NGLApplication app, ReceptionsAPI receptionsAPI, ReceptionMapperProvider receptionMapperProvider, ReceptionBuilder receptionBuilder, ReceptionFiller receptionFiller, ReceptionsFileReader receptionsFileReader) {
		this.app = app;
		this.fileForm = app.formFactory().form(PropertyFileValue.class);
		this.api = receptionsAPI;
		this.receptionMapperProvider = receptionMapperProvider;
		this.receptionBuilder = receptionBuilder;
		this.receptionFiller = receptionFiller;
		this.receptionsFileReader = receptionsFileReader;
	}
	
	private ContextValidation getCreationContext(Form<PropertyFileValue> filledForm) {
		ContextValidation ctxtValidation = ContextValidation.createCreationContext(this.getCurrentUser(), filledForm);
		ctxtValidation.putObject(ReceptionValidationHelper.FIELD_STATE_RECEPTION_CONTEXT, ReceptionValidationHelper.STATE_CONTEXT_IMPORT_FILE_ILLUMINA);
		return ctxtValidation;
	}
	
	private List<AbstractReception> readAll(PropertyFileValue pfv, ReceptionRowProcessor rowProcessor) throws APIException {
		Iterator<Row> rows = receptionsFileReader.getRows(pfv);
		List<AbstractReception> receptions = new ArrayList<>();
		while (rows.hasNext()) {
			final Row row = rows.next();
			rowProcessor.handleRowProcessing(row, receptions);
		} 
		return receptions;
	}
	
	private List<AbstractReception> getReceptionsFromFile(String importFormat, PropertyFileValue pfv, ContextValidation contextValidation) throws APIException {
		AbstractReceptionMapper receptionMapper = receptionMapperProvider.getReceptionMapper(importFormat, contextValidation);
		ReceptionRowProcessor rowProcessor = new ReceptionRowProcessor(receptionBuilder, receptionFiller, receptionMapper, contextValidation, getCurrentUser());
		return readAll(pfv, rowProcessor);
	}
	
	private void saveFileReceptions(List<AbstractReception> receptions) throws APIValidationException, APIException {
		for (final AbstractReception reception : receptions) {
			this.api.create(reception, getCurrentUser());
		}
	}
	
	private Result processFile(String importFormat, PropertyFileValue pfv, ContextValidation contextValidation) throws APIValidationException, APIException {
		List<AbstractReception> receptions = getReceptionsFromFile(importFormat, pfv, contextValidation);
		if (contextValidation.hasErrors()) {
			return badRequest(this.app.errorsAsJson(contextValidation.getErrors()));
		}
		saveFileReceptions(receptions);
		return ok();
	}
	
	private Result handleFileProcessing(String importFormat, PropertyFileValue pfv, ContextValidation contextValidation) {
		try {
			return processFile(importFormat, pfv, contextValidation);
		} catch (final APIException e) {
			LOGGER.error(e.getMessage(), e);
			contextValidation.addError("Error :", String.valueOf(e.getMessage()));
			return badRequest(this.app.errorsAsJson(contextValidation.getErrors()));
		}
	}
	
	private Result importReceptionsFile(String importFormat) {
		final Form<PropertyFileValue> filledForm = this.getFilledForm(this.fileForm, PropertyFileValue.class);
		final PropertyFileValue pfv = filledForm.get();
		final ContextValidation contextValidation = getCreationContext(filledForm);
		return handleFileProcessing(importFormat, pfv, contextValidation);
	}

	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value = { "writing" })
	public Result importFile(String importFormat) {
		try {
			return importReceptionsFile(importFormat);
		} catch (final NoSuchElementException e) {
			return badRequest(this.app.errorAsJson("missing file"));
		}
	}

}
