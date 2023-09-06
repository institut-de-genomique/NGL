package fr.cea.ig.ngl.dao.samples;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceHelpers;
import models.utils.instance.SampleHelper;
import validation.ContextValidation;

@Singleton
public class SamplesAPI extends GenericAPI<SamplesDAO, Sample> {

	private static final play.Logger.ALogger logger = play.Logger.of(SamplesAPI.class);
	
	private final List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("comments", "valuation");
	private final List<String> DEFAULT_KEYS = Arrays.asList("code",
															     "typeCode",
															     "categoryCode",
															     "projectCodes",
															     "referenceCollab",
															     "properties",
															     "valuation",
															     "taxonCode",
															     "ncbiScientificName",
															     "comments",
															     "traceInformation");
	
	@Inject
	public SamplesAPI(SamplesDAO dao) {
		super(dao);
	}
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return this.AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return this.DEFAULT_KEYS;
	}
	
	/* (non-Javadoc)
	 * @see fr.cea.ig.ngl.dao.api.GenericAPI#create(fr.cea.ig.DBObject, java.lang.String)
	 */
	@Override
	public Sample create(Sample input, String currentUser) throws APIValidationException, APISemanticException {
//		ContextValidation ctxVal = new ContextValidation(currentUser);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		if (input._id == null) { 
			input.traceInformation = new TraceInformation();
			input.traceInformation.creationStamp(ctxVal, currentUser);
		} else {
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG); 
		}
		SampleHelper.executeSampleCreationRules(input);
		input.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			return dao.saveObject(input);
		} else {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
	}
	
	/**
	 * Define only some fields to update (not the entire object). 
	 * <br>
	 * list of editable field list is defined in {@link #AUTHORIZED_UPDATE_FIELDS} constant
	 * @see SamplesAPI#AUTHORIZED_UPDATE_FIELDS
	 */
	@Override
	public Sample update(Sample input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		Sample sampleInDb = get(input.code);
		if (sampleInDb == null) {
			throw new APIException("Sample with code " + input.code + " not exist");
		} else {
//			ValidationContext ctxVal = new ContextValidation(currentUser);
//			ctxVal.setUpdateMode();
			ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
			checkAuthorizedUpdateFields(ctxVal, fields);
			checkIfFieldsAreDefined(ctxVal, fields, input);
			if (!ctxVal.hasErrors()) {
				input.comments = InstanceHelpers.updateComments(ctxVal, input.comments);
				TraceInformation ti = sampleInDb.traceInformation;
				if (ti != null) {
					ti.modificationStamp(ctxVal, currentUser);
				} else {
					logger.error("traceInformation is null !!");
				}
				
				if (!ctxVal.hasErrors()) {
					dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
					return get(input.code);
				} else {
					throw new APIValidationException("Invalid fields", ctxVal.getErrors());
				}
			} else {
				throw new APIValidationException("Invalid Sample object", ctxVal.getErrors());
			}
		}
	}

	/* (non-Javadoc)
	 * @see fr.cea.ig.ngl.dao.api.GenericAPI#update(fr.cea.ig.DBObject, java.lang.String)
	 */
	@Override
	public Sample update(Sample input, String currentUser) throws APIException, APIValidationException {
		Sample sampleInDb = get(input.code);
		if (sampleInDb == null) {
			throw new APIException("Sample with code " + input.code + " not exist");
		} else {
//			ContextValidation ctxVal = new ContextValidation(currentUser);
//			ctxVal.setUpdateMode();
			ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
			if (input.traceInformation != null) {
				input.traceInformation.modificationStamp(ctxVal, currentUser);
			} else {
				logger.error("traceInformation is null !!");
			}
			input.comments = InstanceHelpers.updateComments(ctxVal, input.comments);
			input.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				dao.updateObject(input);
				return get(input.code);
			} else {
				throw new APIValidationException("Invalid Sample object", ctxVal.getErrors());
			}
		}
	}
	
}
