package fr.cea.ig.ngl.dao.samples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.ResolutionHistoryEntry;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceHelpers;
import models.utils.instance.SampleHelper;
import validation.ContextValidation;

@Singleton
public class SamplesAPI extends GenericAPI<SamplesDAO, Sample> {

	private static final play.Logger.ALogger logger = play.Logger.of(SamplesAPI.class);
	
	private final List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("comments", "valuation", "resolutionCodes");
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
															     "traceInformation",
															     "resolutionCodes");
	
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
	
	private List<ResolutionHistoryEntry> initResolutionHistory(List<ResolutionHistoryEntry> previousHistory, Set<String> previousResolutionCodes, String user) {
		if(previousHistory != null) return previousHistory;
		if(previousResolutionCodes == null) return  new ArrayList<>();
		List<ResolutionHistoryEntry> initialHistory = new ArrayList<>();
		ResolutionHistoryEntry firstResolutionHistoryEntry = new ResolutionHistoryEntry(0, previousResolutionCodes, user);
		initialHistory.add(firstResolutionHistoryEntry);
		return initialHistory;
	}
	
	private int nextIndex(List<ResolutionHistoryEntry> previousHistory) {
		return previousHistory.stream()
				.mapToInt(entry -> entry.index)
				.max()
				.getAsInt() + 1;
	}

	private List<ResolutionHistoryEntry> nextResolutionHistory(List<ResolutionHistoryEntry> previousHistory, Set<String> nextResolutionCodes, String user) {
		int nextIndex = previousHistory.isEmpty() ? 0 : nextIndex(previousHistory);
		ResolutionHistoryEntry rhe = new ResolutionHistoryEntry(nextIndex, nextResolutionCodes, user);
		previousHistory.add(rhe);
		return previousHistory;
	}
	
	private List<ResolutionHistoryEntry> updatedResolutionHistory(Sample sampleInDb, Sample input, String currentUser) {
		List<ResolutionHistoryEntry> initialResolutionHistory = initResolutionHistory(sampleInDb.resolutionHistory, sampleInDb.resolutionCodes, currentUser);
		return nextResolutionHistory(initialResolutionHistory, input.resolutionCodes, currentUser);
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
					Builder builder = dao.getBuilder(input, fields).set("traceInformation", ti);
					if(fields.contains("resolutionCodes")) {
						List<ResolutionHistoryEntry> resolutionHistory = updatedResolutionHistory(sampleInDb, input, currentUser);
						builder = builder.set("resolutionHistory", resolutionHistory);
					}
					dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), builder);
					return get(input.code);
				} else {
					throw new APIValidationException("Invalid fields", ctxVal.getErrors());
				}
			} else {
				throw new APIValidationException("Invalid Sample object", ctxVal.getErrors());
			}
		}
	}
	
	private boolean isResolutionCodesUpdate(Sample sampleInDb, Sample input) {
		return input.resolutionCodes == null ? CollectionUtils.isNotEmpty(sampleInDb.resolutionCodes) : !input.resolutionCodes.equals(sampleInDb.resolutionCodes);
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
				if(isResolutionCodesUpdate(sampleInDb, input)) input.resolutionHistory = updatedResolutionHistory(sampleInDb, input, currentUser);
				dao.updateObject(input);
				return get(input.code);
			} else {
				throw new APIValidationException("Invalid Sample object", ctxVal.getErrors());
			}
		}
	}
	
}
