package controllers.reagents.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.reagents.CatalogsAPI;
import fr.cea.ig.ngl.dao.reagents.ReceptionsAPI;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.AbstractReception;
import models.laboratory.reagent.instance.BoxReception;
import models.laboratory.reagent.instance.ReagentReception;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;

public class Receptions extends TPLCommonController {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger LOGGER = play.Logger.of(Receptions.class);

	private static final String STATE_IN_PROGRESS_CODE = "IP";

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
	 * Excel row mapper
	 */
	private final IlluminaReceptionMapper mapper;

	/**
	 * Catalogs API
	 */
	private final CatalogsAPI catalogsAPI;

	@Inject
	public Receptions(NGLApplication app, ReceptionsAPI receptionsAPI, CatalogsAPI catalogsAPI,
			IlluminaReceptionMapper illuminaReceptionMapper) {
		this.app = app;
		this.fileForm = app.formFactory().form(PropertyFileValue.class);
		this.api = receptionsAPI;
		this.catalogsAPI = catalogsAPI;
		this.mapper = illuminaReceptionMapper;
	}

	private AbstractReception getReception(String catalogRefCode, String provider, String kitCatalogName) {
		if (StringUtils.isBlank(catalogRefCode)) {
			throw new IllegalStateException("Import reagent-reception: catalogRefCode cannot be empty!");
		} else if (StringUtils.isBlank(provider)) {
			throw new IllegalStateException("Import reagent-reception: provider cannot be empty!");
		} else if (StringUtils.isBlank(kitCatalogName)) {
			throw new IllegalStateException("Import reagent-reception: kitCatalogName cannot be empty!");
		}
		
		final AbstractCatalog catalog = this.catalogsAPI.getByRefAndProvider(catalogRefCode, provider, kitCatalogName);
		if (catalog == null) {
			throw new IllegalStateException("Import reagent-reception: cannot find catalog for Reference '"
					+ String.valueOf(catalogRefCode) + "', provider '" + String.valueOf(provider) + 
					"' and kitCatalog name '" + kitCatalogName + "'");
		}
		if (catalog instanceof BoxCatalog) {
			return this.mapper.withCorrectTypes(new BoxReception());
		} else if (catalog instanceof ReagentCatalog) {
			return this.mapper.withCorrectTypes(new ReagentReception());
		} else {
			throw new IllegalStateException("Import reagent-reception: unknown catalog type for Reference '"
					+ String.valueOf(catalogRefCode) + "'");
		}
	}

	private List<AbstractReception> extractReceptions(AbstractReceptionMapper mapper, PropertyFileValue pfv,
			ContextValidation contextValidation)
			throws APIValidationException, APIException {

		try {
			final Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(pfv.byteValue()));
			final Sheet sheet = wb.getSheetAt(0);
			final Iterator<Row> rows = sheet.rowIterator();

			// remove columns
			rows.next();

			final List<AbstractReception> receptions = new ArrayList<>();
			while (rows.hasNext()) {
				final Row row = rows.next();
				try {

					final String refCatalogCode = mapper.getRefCatalog(row);
					final String provider = mapper.getProvider(row);
					final String kitCatalogName = mapper.getKitCatalogName(row);
					if (refCatalogCode != null && provider != null) {
						final AbstractReception reception = mapper.mapRow(row,
								this.getReception(refCatalogCode, provider, kitCatalogName),
								this.getCurrentUser());
						if (reception != null) {
							reception.code = ReagentCodeHelper.getInstance().generateReceptionCode();
							// Default state for receptions is 'IP'
							reception.state = new State(STATE_IN_PROGRESS_CODE, this.getCurrentUser(), new Date());
							reception.traceInformation = new TraceInformation(this.getCurrentUser());
							reception.validate(contextValidation);
							receptions.add(reception);
						}
					}
				} catch(IllegalStateException | IllegalArgumentException e) {
					contextValidation.addError("Error - row " + (row.getRowNum() + 1) + ":", String.valueOf(e.getMessage()));
				}
			}
			return receptions;
		} catch(InvalidFormatException | IOException e) {
			throw new APIException("An error has occured trying to extract receptions from file: " + e.getMessage(), e);
		}
	}

	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value = { "writing" })
	public Result importFile() {

		try {
			final Form<PropertyFileValue> filledForm = this.getFilledForm(this.fileForm, PropertyFileValue.class);
			final PropertyFileValue pfv = filledForm.get();

			final ContextValidation contextValidation = ContextValidation.createCreationContext(this.getCurrentUser(),
					filledForm);

			if (contextValidation.hasErrors()) {
				return badRequest(this.app.errorsAsJson(contextValidation.getErrors()));
			}

			try {
				final List<AbstractReception> receptions = this.extractReceptions(this.mapper, pfv, contextValidation);

				if (contextValidation.hasErrors()) {
					return badRequest(this.app.errorsAsJson(contextValidation.getErrors()));
				}

				for (final AbstractReception reception : receptions) {
					this.api.create(reception, this.getCurrentUser());
				}

				return ok();
			} catch (final APIException e) {
				LOGGER.error(e.getMessage(), e);
				contextValidation.addError("Error :", String.valueOf(e.getMessage()));
				return badRequest(this.app.errorsAsJson(contextValidation.getErrors()));
			}

		} catch (final NoSuchElementException e) {
			return badRequest(this.app.errorAsJson("missing file"));
		}
	}

}
