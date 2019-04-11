package validation.run.instance;


import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;


public class AnalysisValidationHelper extends CommonValidationHelper {
	
	// ---------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an analysis type code.
	 * @param typeCode          analysis type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateAnalysisTypeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateAnalysisType(String                     typeCode,
			                                Map<String, PropertyValue> properties,
			                                ContextValidation          contextValidation) {
		AnalysisValidationHelper.validateAnalysisTypeRequired(contextValidation, typeCode, properties);
	}

	
	/**
	 * Validate an analysis type code.
	 * @param contextValidation validation context
	 * @param typeCode          analysis type code
	 * @param properties        properties
	 */
	public static void validateAnalysisTypeRequired(ContextValidation          contextValidation,	
			                                        String                     typeCode, 
			                                        Map<String, PropertyValue> properties) {
		AnalysisType analysisType = validateCodeForeignRequired(contextValidation, AnalysisType.miniFind.get(), typeCode, "typeCode", true);
		if (analysisType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, analysisType.getPropertyDefinitionByLevel(Level.CODE.Analysis), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}		
	}
	
	// ---------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate read set codes of an analysis.
	 * @param analysis          analysis
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateReadSetCodes(ContextValidation, Analysis)}
	 */
	@Deprecated
	public static void validateReadSetCodes(Analysis analysis, ContextValidation contextValidation) {
		AnalysisValidationHelper.validateReadSetCodes(contextValidation, analysis);
	}

	/**
	 * Validate read set codes of an analysis.
	 * @param contextValidation validation context
	 * @param analysis          analysis
	 */
	public static void validateReadSetCodes(ContextValidation contextValidation, Analysis analysis) {
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, analysis.masterReadSetCodes, "masterReadSetCodes", ReadSet.class,InstanceConstants.READSET_ILLUMINA_COLL_NAME,false);
		validateRequiredInstanceCodes(contextValidation, analysis.masterReadSetCodes, "masterReadSetCodes", ReadSet.class, InstanceConstants.READSET_ILLUMINA_COLL_NAME, false);
		// TODO: possibly fix the path that could be "readSetCodes" or leave a comment why it's not
//		BusinessValidationHelper.validateRequiredInstanceCodes(contextValidation, analysis.readSetCodes, "masterReadSetCodes", ReadSet.class,InstanceConstants.READSET_ILLUMINA_COLL_NAME,false);
		validateRequiredInstanceCodes(contextValidation, analysis.readSetCodes, "masterReadSetCodes", ReadSet.class, InstanceConstants.READSET_ILLUMINA_COLL_NAME, false);
		
		if ("N".equals(analysis.state.code)) {
			//validateReadSetsState(analysis.masterReadSetCodes, "masterReadSetCodes", "IW-BA", contextValidation);
			BasicDBObject keys = new BasicDBObject();
			keys.put("code",  1);
			keys.put("state", 1);
			int i = 0;
			for (String code : analysis.masterReadSetCodes) {
				//Get readSet
				ReadSet readSetMaster = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, code, keys);
				if (!readSetMaster.state.code.equals("IW-BA") && !readSetMaster.state.code.equals("IP-BA")) {
					contextValidation.addError("masterReadSetCodes["+i+"]", ValidationConstants.ERROR_BADSTATE_MSG, code);
				} else if (readSetMaster.state.code.equals("IP-BA") && MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
						                          DBQuery.and(DBQuery.is("state.code","IP-BA"),
						                        		      DBQuery.in("masterReadSetCodes", code)))) {
					contextValidation.addError("masterReadSetCodes["+i+"]", ValidationConstants.ERROR_BADSTATE_MSG, code);
				}
				i++;
			}
		} else if("IP-BA".equals(analysis.state.code)) {
			validateReadSetsState(analysis.masterReadSetCodes, "masterReadSetCodes", "IP-BA", contextValidation);
		}
	}

	private static void validateReadSetsState(List<String> readSetCodes, String pName, String waitingState, ContextValidation contextValidation) {
		int i = 0;
		for (String code : readSetCodes) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("code", code), DBQuery.is("state.code", waitingState)))) {
				contextValidation.addError(pName+"["+i+"]", ValidationConstants.ERROR_BADSTATE_MSG, code);
			}
			i++;
		}		
	}



}
