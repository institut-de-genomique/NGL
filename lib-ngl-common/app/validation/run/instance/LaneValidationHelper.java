package validation.run.instance;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.description.RunType;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class LaneValidationHelper extends CommonValidationHelper {
	
	/**
	 * Validate a collection of lanes in a run.
	 * @param contextValidation validation context
	 * @param run               run
	 * @param lanes             lanes to validate
	 */
	public static void validateLanes(ContextValidation contextValidation, Run run, List<Lane> lanes) {
		// GA: number of lanes (depends of the type run and the mode incremental insert or full insert !!!)
		// GA: validate lane number
		if (CollectionUtils.isEmpty(lanes))
			return;
		int index = 0;
		Set<Integer> laneNumbers = new HashSet<>();
		for (Lane lane : lanes) {
			if (lane != null) {
				contextValidation.addKeyToRootKeyName("lanes[" + index + "]");
				lane.validate(contextValidation, run);
				if (laneNumbers.contains(lane.number)) {
					contextValidation.addError("number", ValidationConstants.ERROR_NOTUNIQUE_MSG, lane.number);
				}
				laneNumbers.add(lane.number);
				contextValidation.removeKeyFromRootKeyName("lanes[" + index + "]");
			}
			index++;
		}
	}

	/**
	 * Validate a lane number for a context run (context parameter "run").
	 * @param number            lane number
	 * @param contextValidation validation context
	 */
	@Deprecated
	public static void validationLaneNumber(Integer number, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, number, "number")) {
			//Validate unique lane.number if run already exist
			if (contextValidation.isCreationMode() && isLaneExist(number, contextValidation)) {
				contextValidation.addError("number",ValidationConstants.ERROR_NOTUNIQUE_MSG, number);
			} else if(contextValidation.isUpdateMode() && !isLaneExist(number, contextValidation)) {
				contextValidation.addError("number",ValidationConstants.ERROR_NOTEXISTS_MSG, number);				
			}						
		}
	}
	
	/**
	 * Validate a lane number for a run.
	 * @param contextValidation validation context
	 * @param run               run
	 * @param number            lane number
	 */
	public static void validateLaneNumber(ContextValidation contextValidation, Run run, Integer number) {
		if (ValidationHelper.validateNotEmpty(contextValidation, number, "number")) {
			//Validate unique lane.number if run already exist
			if (contextValidation.isCreationMode() && isLaneExist(contextValidation, run, number)) {
				contextValidation.addError("number",ValidationConstants.ERROR_NOTUNIQUE_MSG, number);
			} else if(contextValidation.isUpdateMode() && !isLaneExist(contextValidation, run, number)) {
				contextValidation.addError("number",ValidationConstants.ERROR_NOTEXISTS_MSG, number);				
			}						
		}
	}

	/**
	 * Does the lane with given number exists in the context parameter (context parameter "run").
	 * @param number            lane number
	 * @param contextValidation validation context
	 * @return                  true if the lane exists in the context run
	 * @deprecated use {@link #isLaneExist(ContextValidation, Run, Integer)}
	 */
	@Deprecated
	private static boolean isLaneExist(Integer number, ContextValidation contextValidation) {
		Run run = getRunFromContext(contextValidation);
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				DBQuery.and(DBQuery.is("code", run.code), DBQuery.is("lanes.number", number)));
	}
	
	/**
	 * Does a lane number exist in a run (from the database data) ? 
	 * @param contextValidation validation context
	 * @param run               run
	 * @param laneNumber        lane number
	 * @return                  true if the lane exists for the run in the database
	 */
	private static boolean isLaneExist(ContextValidation contextValidation, Run run, Integer laneNumber) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				                           DBQuery.and(DBQuery.is("code",         run.code), 
				                        		       DBQuery.is("lanes.number", laneNumber)));
	}

	// ----------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate that the read set are assigned to a given lane.
	 * @param number            lane number
	 * @param readSetCodes      read set codes to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateLaneReadSetCodes(ContextValidation, Integer, List)}
	 */
	@Deprecated
	public static void validationLaneReadSetCodes_(Integer number, List<String> readSetCodes, ContextValidation contextValidation) {
		LaneValidationHelper.validateLaneReadSetCodes(contextValidation, number, readSetCodes);
	}

	/**
	 * Validate that the read set are assigned to a given lane.
	 * @param contextValidation validation context
	 * @param number            lane number
	 * @param readSetCodes      read set codes to validate
	 */
	public static void validateLaneReadSetCodes(ContextValidation contextValidation, Integer number, List<String> readSetCodes) {
		if (CollectionUtils.isEmpty(readSetCodes)) 
			return;
		Set<String> readSetCodesTreat = new HashSet<>();
		for (int i=0; i< readSetCodes.size(); i++) {
			String  readSetCode = readSetCodes.get(i);
			ReadSet readSet     = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
			if (readSet == null || !number.equals(readSet.laneNumber)) {
				contextValidation.addError("readSetCodes["+i+"]",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  readSetCode, "ReadSet");
			}
			if (readSetCodesTreat.contains(readSetCode)) {
				contextValidation.addError("readSetCodes["+i+"]",ValidationConstants.ERROR_CODE_DOUBLE_MSG,  readSetCode);
			}
			readSetCodesTreat.add(readSetCode);
		}
	}

	// ----------------------------------------------------------------
	
	@Deprecated
	public static void validationLaneProperties(Map<String, PropertyValue> properties,	ContextValidation contextValidation) {
		Run run = getRunFromContext(contextValidation);
		try {
			RunType  runType = RunType.find.get().findByCode(run.typeCode);
			if (runType != null) {
				contextValidation.addKeyToRootKeyName("properties");
				ValidationHelper.validateProperties(contextValidation, properties, runType.getPropertyDefinitionByLevel(Level.CODE.Lane), true);
				contextValidation.removeKeyFromRootKeyName("properties");
			}
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Validate lane properties for a run.
	 * @param contextValidation validation context
	 * @param run               run
	 * @param properties        properties
	 */
	public static void validateLaneProperties(ContextValidation contextValidation, Run run, Map<String, PropertyValue> properties) {
		RunType  runType = RunType.find.get().findByCode(run.typeCode);
		if (runType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, runType.getPropertyDefinitionByLevel(Level.CODE.Lane), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}
	}
	
	/**
	 * Get the run context parameter (context parameter "run").
	 * @param contextValidation validation context
	 * @return                  run from context
	 */
	@Deprecated
	private static Run getRunFromContext(ContextValidation contextValidation) {
		return contextValidation.getTypedObject("run");
	}

	/**
	 * Validate a valuation for an implicit run parameter (context parameter "run").
	 * @param valuation         valuation to validate
	 * @param contextValidation validation context
	 * @deprecated use explicit run parameter {@link #validateLaneValuation(ContextValidation, Run, Valuation)}
	 */
	@Deprecated
	public static void validateLaneValuation(Valuation valuation, ContextValidation contextValidation) {
		Run run = getRunFromContext(contextValidation);
		validateValuationRequired(contextValidation, run.typeCode, valuation);		
	}
	
	/**
	 * Validate a lane valuation in a run context.
	 * @param contextValidation validation context
	 * @param run               run
	 * @param valuation         valuation to validate
	 */
	public static void validateLaneValuation(ContextValidation contextValidation, Run run, Valuation valuation) {
		validateValuationRequired(contextValidation, run.typeCode, valuation);		
	}

}
