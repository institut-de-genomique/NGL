package validation.run.instance;

import java.util.Map;

import static org.mongojack.DBQuery.*;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ReadSetValidationHelper extends CommonValidationHelper {
		
	public static void validateReadSetCodeInRunLane(String readSetCode, String runCode, Integer laneNumber, ContextValidation contextValidation) {
		if (contextValidation.isUpdateMode() && !checkReadSetInRun(readSetCode, runCode, laneNumber)) {
			contextValidation.addError("code", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, readSetCode);
		}
	}
	
//	private static boolean checkReadSetInRun(String readSetCode, String runCode, Integer laneNumber) {
//		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
//				DBQuery.and(
//						DBQuery.is("code", runCode), 
//						DBQuery.elemMatch("lanes", 
//							DBQuery.and(
//								DBQuery.is("number", laneNumber),
//								DBQuery.in("readSetCodes", readSetCode)))));
//	}
	private static boolean checkReadSetInRun(String readSetCode, String runCode, Integer laneNumber) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				                           and(is       ("code",  runCode), 
						                       elemMatch("lanes", and(is("number",       laneNumber),
						                    		                  in("readSetCodes", readSetCode)))));
	}
	
	// -----------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required foreign read set type code.
	 * @param typeCode          type code to validate
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateReadSetTypeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateReadSetType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ReadSetValidationHelper.validateReadSetTypeRequired(contextValidation, typeCode, properties);
	}

	/**
	 * Validate a required foreign read set type code.
	 * @param contextValidation validation context
	 * @param typeCode          type code to validate
	 * @param properties        properties
	 */
	public static void validateReadSetTypeRequired(ContextValidation contextValidation,	String typeCode, Map<String, PropertyValue> properties) {
		ReadSetType readSetType = validateCodeForeignRequired(contextValidation, ReadSetType.miniFind.get(), typeCode, "typeCode", true);
		if (readSetType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, readSetType.getPropertyDefinitionByLevel(Level.CODE.ReadSet), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}		
	}

	// -----------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a run code.
	 * @param runCode           run code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateReadSetRunCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateReadSetRunCode(String runCode, ContextValidation contextValidation) {
		ReadSetValidationHelper.validateReadSetRunCodeRequired(contextValidation, runCode);
	}
	
	/**
	 * Validate a run code.
	 * @param contextValidation validation context
	 * @param runCode           run code
	 */
	public static void validateReadSetRunCodeRequired(ContextValidation contextValidation, String runCode) {
//		validateRequiredInstanceCode(runCode, "runCode",  Run.class, InstanceConstants.RUN_ILLUMINA_COLL_NAME, contextValidation);
		validateCodeForeignRequired(contextValidation, Run.find.get(), runCode, "runCode");		
	}

	// -----------------------------------------------------------------------

	public static void validateReadSetLaneNumber(String runCode, Integer laneNumber, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, runCode, "runCode") && 
				ValidationHelper.validateNotEmpty(contextValidation, laneNumber, "laneNumber")) {
			if (!isLaneExist(runCode, laneNumber, contextValidation)) {
				contextValidation.addError("runCode",    ValidationConstants.ERROR_NOTEXISTS_MSG, runCode);
				contextValidation.addError("laneNumber", ValidationConstants.ERROR_NOTEXISTS_MSG, laneNumber);
			}
		}		
	}
	
//	private static boolean isLaneExist(String runCode, Integer laneNumber, ContextValidation contextValidation) {		
//		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
//				                           DBQuery.and(DBQuery.is("code", runCode), 
//				                        		       DBQuery.is("lanes.number", laneNumber)));
//	}
	private static boolean isLaneExist(String runCode, Integer laneNumber, ContextValidation contextValidation) {		
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				                           and(is("code",         runCode), 
				                        	   is("lanes.number", laneNumber)));
	}
	
	public static void validateSampleOnContainer(SampleOnContainer sampleOnContainer, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, sampleOnContainer, "sampleOnContainer")) {
			sampleOnContainer.validate(contextValidation);
		}
	}

}
