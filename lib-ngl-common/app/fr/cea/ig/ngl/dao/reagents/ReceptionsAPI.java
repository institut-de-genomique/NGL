package fr.cea.ig.ngl.dao.reagents;

import static org.mongojack.DBQuery.and;
import static org.mongojack.DBQuery.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.AbstractReception;
import models.laboratory.reagent.instance.BoxReception;
import models.laboratory.reagent.instance.ReagentReception;
import models.laboratory.reagent.utils.ReagentCodeHelper;
import play.Logger;
import play.Logger.ALogger;
import validation.ContextValidation;
import workflows.reception.ReceptionWorkflows;

public class ReceptionsAPI extends GenericAPI<ReceptionsDAO, AbstractReception> {

	private static final ALogger LOGGER = Logger.of(ReceptionsAPI.class);

	private final static List<String> authorizedUpdateFields = Collections.unmodifiableList(Arrays.asList());
	private final static List<String> defaultKeys = Collections.unmodifiableList(Arrays.asList());

	private final CatalogsAPI catalogsAPI;
	
	private final ReceptionWorkflows workflows;

	@Inject
	public ReceptionsAPI(ReceptionsDAO dao, CatalogsAPI catalogsAPI, ReceptionWorkflows workflows) {
		super(dao);
		this.catalogsAPI = catalogsAPI;
		this.workflows = workflows;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	protected List<String> defaultKeys() {
		return defaultKeys;
	}

	private AbstractCatalog getCatalogByCode(String catalogCode) {
		final AbstractCatalog catalog = this.catalogsAPI.get(catalogCode);
		if (catalog == null) {
			throw new IllegalStateException("No catalog found for code: " + catalogCode);
		}
		return catalog;
	}

	private AbstractCatalog getCatalogByRefAndProvider(String catalogRefCode, String provider, String kitCatalogCode) {
		final AbstractCatalog catalog = this.catalogsAPI.getByRefAndProvider(catalogRefCode, provider, kitCatalogCode);
		if (catalog == null) {
			throw new IllegalStateException("No catalog found for ref: " + catalogRefCode);
		}
		return catalog;
	}

	private void setCatalogsForBox(BoxReception box) {
		final BoxCatalog catalog = (BoxCatalog) box.catalog;
		if (StringUtils.isNotBlank(catalog.kitCatalogCode)) {
			box.catalogKit = (KitCatalog) this.getCatalogByCode(catalog.kitCatalogCode);
		}
	}

	private void setCatalogsForReagent(ReagentReception reagent) {
		final ReagentCatalog catalog = (ReagentCatalog) reagent.catalog;
		if (StringUtils.isNotBlank(catalog.boxCatalogCode)) {
			reagent.catalogBox = (BoxCatalog) this.getCatalogByCode(catalog.boxCatalogCode);
		}
		if (StringUtils.isNotBlank(catalog.kitCatalogCode)) {
			reagent.catalogKit = (KitCatalog) this.getCatalogByCode(catalog.kitCatalogCode);
		}
	}

	public AbstractReception setCatalogs(AbstractReception reception) {
		if (StringUtils.isBlank(reception.catalogRefCode)) {
			throw new IllegalStateException("Invalid catalogRefCode '" + String.valueOf(reception.catalogRefCode)
					+ "' for reception '" + String.valueOf(reception.code) + "'");
		} else if (StringUtils.isBlank(reception.providerCode)) {
			throw new IllegalStateException("Invalid providerCode '" + String.valueOf(reception.providerCode)
					+ "' for reception '" + String.valueOf(reception.code) + "'");
		} else if (StringUtils.isBlank(reception.kitCatalogName)) {
			throw new IllegalStateException("Invalid providerCode '" + String.valueOf(reception.kitCatalogName)
			+ "' for reception '" + String.valueOf(reception.code) + "'");
		} else {
			reception.catalog = this.getCatalogByRefAndProvider(reception.catalogRefCode, reception.providerCode, reception.kitCatalogName);

			if (reception instanceof BoxReception) {
				this.setCatalogsForBox((BoxReception) reception);

			} else if (reception instanceof ReagentReception) {
				this.setCatalogsForReagent((ReagentReception) reception);

			} else {
				final String category = reception.getClass().getSimpleName();
				throw new IllegalStateException("Unrecognized category [" + String.valueOf(category)
						+ "] for reception " + String.valueOf(reception.code));
			}
			return reception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<AbstractReception> listObjects(ListFormWrapper<AbstractReception> wrapper) throws APIException {
		if(wrapper.isList() || wrapper.isCount()) {
			return super.listObjects(wrapper);
		} else {
			final Iterator<AbstractReception> delegate = super.listObjects(wrapper).iterator();
			return () -> {
				return new Iterator<AbstractReception>() {

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public AbstractReception next() {
						return ReceptionsAPI.this.setCatalogs(delegate.next());
					}

				};
			};
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractReception get(String code) {
		return this.setCatalogs(super.get(code));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractReception getObject(String code, BasicDBObject keys) {
		return this.setCatalogs(super.getObject(code, keys));
	}

	private void clearCategories(AbstractReception input) {
		input.catalog = null;
		input.catalogKit = null;
		if (input instanceof ReagentReception) {
			((ReagentReception) input).catalogBox = null;
		}
	}

	@Override
	public AbstractReception create(AbstractReception input, String currentUser)
			throws APIValidationException, APIException {
		input.code = ReagentCodeHelper.getInstance().generateReceptionCode();

		input.traceInformation = new TraceInformation();
		input.traceInformation.setTraceInformation(currentUser);

		this.clearCategories(input);

		final ContextValidation contextValidation = ContextValidation.createCreationContext(currentUser);
		input.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, contextValidation.getErrors());
		}

		return this.setCatalogs(this.dao.save(input));
	}

	@Override
	public AbstractReception update(AbstractReception input, String currentUser)
			throws APIException, APIValidationException {
		// fix input code
		final String code = input.code;

		if (!this.dao.checkObjectExistByCode(code)) {
			throw new APIException(
					this.dao.getElementClass().getSimpleName() + " with code " + input.code + " doesn't exist");
		}

		final ContextValidation contextValidation = ContextValidation.createUpdateContext(currentUser);

		if (input.traceInformation == null) {
			LOGGER.warn("traceInformation is null !!");
		} else {
			input.traceInformation.modificationStamp(contextValidation, currentUser);
		}

		this.clearCategories(input);

		input.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, contextValidation.getErrors());
		}

		this.dao.update(input);
		return this.get(code);
	}

	@Override
	public AbstractReception update(AbstractReception input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		// fix input code
		final String code = input.code;

		if (!this.dao.checkObjectExistByCode(code)) {
			throw new APIException(
					this.dao.getElementClass().getSimpleName() + " with code " + input.code + " doesn't exist");
		}

		final ContextValidation contextValidation = ContextValidation.createUpdateContext(currentUser);
		this.checkAuthorizedUpdateFields(contextValidation, fields);
		this.checkIfFieldsAreDefined(contextValidation, fields, input);

		if (input.traceInformation == null) {
			LOGGER.warn("traceInformation is null !!");
		} else {
			input.traceInformation.modificationStamp(contextValidation, currentUser);
		}

		this.clearCategories(input);

		input.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, contextValidation.getErrors());
		}

		this.dao.updateObject(and(is("code", input.code)), this.dao.getBuilder(input, fields));
		return this.get(code);
	}
	
	public AbstractReception updateState(String code, State state, String currentUser) throws APIValidationException, APIException {
		AbstractReception reception = get(code);
        if (reception == null) 
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        state.date = new Date();
        state.user = currentUser;
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
        workflows.setState(ctxVal, reception, state);
        if (ctxVal.hasErrors()) 
        	throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
        return get(code);
    }

	public Iterable<AbstractReception> listByCatalogRefCodes(Collection<String> catalogRefCodes) {
		return this.dao.find(DBQuery.in("catalogRefCode", catalogRefCodes));
	}
	
	public boolean isAnyKitCatalogReferenceInReceptions(String kitCatalogName) {
		return this.dao.find(DBQuery.is("kitCatalogName", kitCatalogName)).iterator().hasNext();
	}
	
	public boolean isAnyCatalogReferenceInReceptions(String CatalogRefCode) {
		return this.dao.find(DBQuery.is("catalogRefCode", CatalogRefCode)).iterator().hasNext();
	}

}
