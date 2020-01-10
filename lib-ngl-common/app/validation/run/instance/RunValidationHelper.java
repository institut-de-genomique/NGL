package validation.run.instance;

import static fr.cea.ig.lfw.utils.Iterables.range;
import static fr.cea.ig.lfw.utils.Iterables.zip;

import java.util.Map;
import java.util.Set;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.project.instance.Project;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class RunValidationHelper {
		
	// ------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required instrument used. 
	 * @param instrumentUsed    instrument used
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRunInstrumentUsedRequired(ContextValidation, InstrumentUsed)}
	 */
	@Deprecated
	public static void validateRunInstrumentUsed(InstrumentUsed instrumentUsed, ContextValidation contextValidation) {
		RunValidationHelper.validateRunInstrumentUsedRequired(contextValidation, instrumentUsed);
	}

	/**
	 * Validate a required instrument used. 
	 * @param contextValidation validation context
	 * @param instrumentUsed    instrument used
	 */
	public static void validateRunInstrumentUsedRequired(ContextValidation contextValidation, InstrumentUsed instrumentUsed) {
		if (ValidationHelper.validateNotEmpty(contextValidation, instrumentUsed, "instrumentUsed")) {
			contextValidation.addKeyToRootKeyName("instrumentUsed");
			instrumentUsed.validate(contextValidation); 
			contextValidation.removeKeyFromRootKeyName("instrumentUsed");
		}
	}

	// ------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required run type code and properties.
	 * @param typeCode          run type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRunTypeCodeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateRunType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		RunValidationHelper.validateRunTypeCodeRequired(contextValidation, typeCode, properties);
	}
			
	/**
	 * Validate a required run type code and properties.
	 * @param contextValidation validation context
	 * @param typeCode          run type code
	 * @param properties        properties
	 */
	public static void validateRunTypeCodeRequired(ContextValidation contextValidation,	String typeCode, Map<String, PropertyValue> properties) {
		RunType runType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, RunType.miniFind.get(), typeCode, "typeCode", true);
		if (runType != null) {
			contextValidation.addKeyToRootKeyName("properties");
			ValidationHelper.validateProperties(contextValidation, properties, runType.getPropertyDefinitionByLevel(Level.CODE.Run), true);
			contextValidation.removeKeyFromRootKeyName("properties");
		}		
	}
	
	// ------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required run category code.
	 * @param categoryCode      run category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRunCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationRunCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		RunValidationHelper.validateRunCategoryCodeRequired(contextValidation, categoryCode);
	}			

	/**
	 * Validate a required run category code.
	 * @param contextValidation validation context
	 * @param categoryCode      run category code
	 */
	public static void validateRunCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, RunCategory.miniFind.get(), categoryCode, "categoryCode", false);
	}
	
	// ------------------------------------------------------------------------------
	
//	public static void validationLaneReadSetCodes(Integer number, List<String> readSetCodes, ContextValidation contextValidation) {
//		if (readSetCodes != null && readSetCodes.size() > 0) {
//			List<String> readSetCodesTreat = new ArrayList<>();
//			for (int i=0; i< readSetCodes.size(); i++) {
//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCodes.get(i));
//				if (readSet == null || !number.equals(readSet.laneNumber)) {
//					contextValidation.addError("readSetCodes[" + i + "]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, readSetCodes.get(i), "ReadSet");
//				}
//				if (readSetCodesTreat.contains(readSetCodes.get(i))) {
//					contextValidation.addError("readSetCodes[" + i + "]", ValidationConstants.ERROR_CODE_DOUBLE_MSG, readSetCodes.get(i));
//				}
//				readSetCodesTreat.add(readSetCodes.get(i));
//			}
//		}
//	}

	// ------------------------------------------------------------------------------
	// packed and arguments reordered
	
//	public static void validateRunProjectCodes(String runCode, Set<String> projectCodes, ContextValidation contextValidation) {
//		
//		if (projectCodes != null && projectCodes.size() > 0) {
//			int i=0;
//			for (Iterator<String> it = projectCodes.iterator(); it.hasNext(); ) {
//				 String projectCode = it.next();
//				if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", projectCode))) {
//					contextValidation.addError("projectCode["+i+"]",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  projectCode, "Project");
//				}
//				if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("projectCode", projectCode)))) {
//					contextValidation.addError("projectCode["+i+"]",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  projectCode, "ReadSet");
//				}
//				i++;
//			}
//			
//			/*
//			//More advanced validation : checking consistency of data ...
//			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", runCode)).toList();
//			if (readSets != null) {
//				Set<String> readSetProjectCodes = new TreeSet<String>(); 
//				for (ReadSet readSet : readSets) {
//					readSetProjectCodes.add(readSet.projectCode);
//				} 
//
//					int i=0;
//					for (Iterator<String> it = projectCodes.iterator(); it.hasNext(); ) {
//						 String projectCode = it.next();
//						if (! readSetProjectCodes.contains(projectCode) ) {
//							contextValidation.addErrors("projectCodes["+i+"]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, projectCode, "Run");
//						}
//						i++;
//					}
//
//					for (Iterator<String> it = readSetProjectCodes.iterator(); it.hasNext(); ) {
//						String readSetProjectCode = it.next();
//						if (!projectCodes.contains(readSetProjectCode)) {
//							contextValidation.addErrors("projectCodes[]", ValidationConstants.ERROR_CODE_MISSING_MSG, readSetProjectCode, "Run");
//						}
//					}
//			}
//			else {
//				Logger.debug("in pt2");
//				contextValidation.addErrors("projectCodes", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, projectCodes.toString(), "Run");
//			}
//			*/
//		}
//	}

	/**
	 * Validate run project codes (collection of required codes).
	 * @param runCode           run code
	 * @param projectCodes      project code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRunProjectCodes(ContextValidation, String, Set)}
	 */
	@Deprecated
	public static void validateRunProjectCodes(String runCode, Set<String> projectCodes, ContextValidation contextValidation) {
		RunValidationHelper.validateRunProjectCodes(contextValidation, runCode, projectCodes);
	}

	/**
	 * Validate run project codes (collection of required codes).
	 * @param contextValidation validation context
	 * @param runCode           run code
	 * @param projectCodes      project code
	 */
	public static void validateRunProjectCodes(ContextValidation contextValidation, String runCode, Set<String> projectCodes) {
		zip(range(0), projectCodes)
		    .unzipEach((i, projectCode) -> {
		    	if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", projectCode)))
		    		contextValidation.addError("projectCode["+i+"]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  projectCode, "Project");
		    	if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("projectCode", projectCode))))
		    		contextValidation.addError("projectCode["+i+"]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  projectCode, "ReadSet");
		    });
	}
	
	// --------------------------------------------------------------------
	// packed and arguments reordered
	
//	public static void validateRunSampleCodes(String runCode, Set<String> sampleCodes, ContextValidation contextValidation) {
//		if (sampleCodes != null && sampleCodes.size() > 0) {
//			int i=0;
//			for (Iterator<String> it = sampleCodes.iterator(); it.hasNext(); ) {
//				 String sampleCode = it.next();
//				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode))) {
//					contextValidation.addError("sampleCode["+i+"]",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  sampleCode, "Sample");
//				}
//				if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("sampleCode", sampleCode)))) {
//					contextValidation.addError("sampleCode["+i+"]",ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  sampleCode, "ReadSet");
//				}
//				i++;
//			}
//		}
//	}
	
	/**
	 * Validate a list of sample codes for a run (collection of required codes).
	 * @param runCode           run code
	 * @param sampleCodes       sample codes
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRunSampleCodes(ContextValidation, String, Set)}
	 */
	@Deprecated
	public static void validateRunSampleCodes(String runCode, Set<String> sampleCodes,	ContextValidation contextValidation) {
		RunValidationHelper.validateRunSampleCodes(contextValidation, runCode, sampleCodes);
	}

	/**
	 * Validate a list of sample codes for a run (collection of required codes).
	 * @param contextValidation validation context
	 * @param runCode           run code
	 * @param sampleCodes       sample codes
	 */
	public static void validateRunSampleCodes(ContextValidation contextValidation, String runCode, Set<String> sampleCodes) {
		zip(range(0), sampleCodes)
		   .unzipEach((i, sampleCode) -> {
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode)))
					contextValidation.addError("sampleCode["+i+"]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  sampleCode, "Sample");
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("runCode", runCode), DBQuery.is("sampleCode", sampleCode))))
					contextValidation.addError("sampleCode["+i+"]", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG,  sampleCode, "ReadSet");
		   });
	}

}
