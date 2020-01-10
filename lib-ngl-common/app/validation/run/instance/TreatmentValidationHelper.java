package validation.run.instance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class TreatmentValidationHelper {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(TreatmentValidationHelper.class);
	
//	/**
//	 * Get level from context (context parameter "level").
//	 * @param contextValidation validation context
//	 * @return                  level code from context
//	 */
//	@Deprecated
//	private static Level.CODE getLevelFromContext(ContextValidation contextValidation) {
////		return getObjectFromContext("level", Level.CODE.class, contextValidation);
//		return contextValidation.getTypedObject("level");
//	}
	
	// ---------------------------------------------------------------
	// arguments reordered
	
//	@Deprecated
//	public static void validationTreatments(Map<String, Treatment> treatments, ContextValidation contextValidation) {
//		TreatmentValidationHelper.validationTreatments(contextValidation, treatments);
//	}
//
//	@Deprecated
//	public static void validationTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments) {
//		if (treatments != null) {
//			List<String> trNames = new ArrayList<>();
//			contextValidation.addKeyToRootKeyName("treatments");
//			for (Treatment t : treatments.values()) {
//				logger.debug("validationTreatments start {} (errors:{})", t.code, contextValidation.getErrors().size());
//				contextValidation.addKeyToRootKeyName(t.code);
//				if (!trNames.contains(t.code) && treatments.containsKey(t.code)) {										
//					trNames.add(t.code);
//					logger.debug("validationTreatments treatment.validate : {}", t.code);
//					t.validate(contextValidation);					
//				} else if (trNames.contains(t.code)) {
//					logger.debug("validationTreatments not unique : {}", t.code);
//					contextValidation.addError("code", ValidationConstants.ERROR_NOTUNIQUE_MSG, t.code);
//				} else {
//					logger.debug("validationTreatments value not 'authorized' : {}", t.code);
//					contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, t.code);
//				}
//				contextValidation.removeKeyFromRootKeyName(t.code);
//				logger.debug("validationTreatments end {} (errors:{})", t.code, contextValidation.getErrors().size());
//			}
//			contextValidation.removeKeyFromRootKeyName("treatments");
//		}
//	}
	
	/**
	 * Validate treatments.
	 * @param contextValidation validation context
	 * @param treatments        treatments to validate
	 * @param validation        individual treatment validation
	 */
	private static void validateTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments, BiConsumer<ContextValidation,Treatment> validation) {
		if (treatments == null) 
			return;
		Set<String> trNames = new HashSet<>();
		contextValidation.addKeyToRootKeyName("treatments");
		for (Treatment t : treatments.values()) {
			logger.debug("validationTreatments start {} (errors:{})", t.code, contextValidation.getErrors().size());
			contextValidation.addKeyToRootKeyName(t.code);
			if (!trNames.contains(t.code) && treatments.containsKey(t.code)) {										
				trNames.add(t.code);
				logger.debug("validationTreatments treatment.validate : {}", t.code);
				validation.accept(contextValidation,t);					
			} else if (trNames.contains(t.code)) {
				logger.debug("validationTreatments not unique : {}", t.code);
				contextValidation.addError("code", ValidationConstants.ERROR_NOTUNIQUE_MSG, t.code);
			} else {
				logger.debug("validationTreatments value not 'authorized' : {}", t.code);
				contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, t.code);
			}
			contextValidation.removeKeyFromRootKeyName(t.code);
			logger.debug("validationTreatments end {} (errors:{})", t.code, contextValidation.getErrors().size());
		}
		contextValidation.removeKeyFromRootKeyName("treatments");
	}
	
	/**
	 * Validate treatments of an analysis.
	 * @param contextValidation validation context
	 * @param treatments        treatments to validate
	 * @param analysis          analysis
	 */
	public static void validateTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments, Analysis analysis) {
		validateTreatments(contextValidation, treatments, (c,t) -> t.validate(c, analysis));
	}
	
	/**
	 * Validate treatments of a read set.
	 * @param contextValidation validation context
	 * @param treatments        treatments to validate 
	 * @param readSet           read set
	 */
	public static void validateTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments, ReadSet readSet) {
		validateTreatments(contextValidation, treatments, (c,t) -> t.validate(c, readSet));
	}
	
	/**
	 * Validate treatments of a run.
	 * @param contextValidation validation context
	 * @param treatments        treatments to validate
	 * @param run               run
	 */
	public static void validateTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments, Run run) {
		validateTreatments(contextValidation, treatments, (c,t) -> t.validate(c, run));
	}
	
	/**
	 * Validate treatments of a lane (and associated run).
	 * @param contextValidation validation context
	 * @param treatments        treatments to validate
	 * @param run               run
	 * @param lane              lane
	 */
	public static void validateTreatments(ContextValidation contextValidation, Map<String, Treatment> treatments, Run run, Lane lane) {
		validateTreatments(contextValidation, treatments, (c,t) -> t.validate(c, run, lane));
	}

	// ---------------------------------------------------------------
	// renamed and arguments reordered
	
//	/**
//	 * Validate a treatment code.
//	 * @param treatmentType     treatment type
//	 * @param code              treatment code
//	 * @param contextValidation validation context
//	 * @deprecated use {@link #validateTreatmentCode(ContextValidation, TreatmentType, String)}
//	 */
//	@Deprecated
//	public static void validateCode_(TreatmentType treatmentType, String code, ContextValidation contextValidation) {
//		TreatmentValidationHelper.validateTreatmentCode(contextValidation, treatmentType, code);
//	}
//
//	/**
//	 * Validate a treatment code (context parameter "level" and dependent
//	 * "readSet", "run", "lane", "analysis").
//	 * @param contextValidation validation context
//	 * @param treatmentType     treatment type
//	 * @param code              treatment code
//	 * @deprecated use specific existence test
//	 */
//	@Deprecated
//	public static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
//			if (contextValidation.isCreationMode() && isTreatmentExist(contextValidation, code)) {
//				contextValidation.addError("code", ValidationConstants.ERROR_CODE_NOTUNIQUE_MSG, code, treatmentType.code);		    	
//			} else if (contextValidation.isUpdateMode() && !isTreatmentExist(contextValidation, code)) {
//				contextValidation.addError("code", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code, treatmentType.code);
//			}
//			if (!treatmentType.names.contains(code)) {
//				contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, code);
//			}		
//		}
//	}
	
	/**
	 * Validate treatment code.
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param code              treatment code
	 * @param exists            treatment code existence test 
	 */
	private static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code, Supplier<Boolean> exists) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, code, "code"))
			return;
		if (contextValidation.isCreationMode() && exists.get()) {
			contextValidation.addError("code", ValidationConstants.ERROR_CODE_NOTUNIQUE_MSG, code, treatmentType.code);		    	
		} else if (contextValidation.isUpdateMode() && !exists.get()) {
			contextValidation.addError("code", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, code, treatmentType.code);
		}
		if (!treatmentType.names.contains(code)) {
			contextValidation.addError("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, code);
		}		
	}
	
	/**
	 * Validate treatment code for an analysis.
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param code              treatment code 
	 * @param analysis          analysis
	 */
	public static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code, Analysis analysis) {
		validateTreatmentCode(contextValidation, treatmentType, code, () -> isTreatmentExist(contextValidation, code, analysis));
	}
	
	/**
	 * Validate treatment code for a read set.
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param code              treatment code 
	 * @param readSet           read set
	 */
	public static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code, ReadSet readSet) {
		validateTreatmentCode(contextValidation, treatmentType, code, () -> isTreatmentExist(contextValidation, code, readSet));
	}

	/**
	 * Validate treatment code for a run.
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param code              treatment code 
	 * @param run               run
	 */
	public static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code, Run run) {
		validateTreatmentCode(contextValidation, treatmentType, code, () -> isTreatmentExist(contextValidation, code, run));
	}
	
	/**
	 * Validate treatment code for a lane (and associated run).
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param code              treatment code 
	 * @param run               run
	 * @param lane              lane 
	 */
	public static void validateTreatmentCode(ContextValidation contextValidation, TreatmentType treatmentType, String code, Run run, Lane lane) {
		validateTreatmentCode(contextValidation, treatmentType, code, () -> isTreatmentExist(contextValidation, code, run, lane));
	}
	
	// ---------------------------------------------------------------

//	/**
//	 * Does the treatment exists in a context (context parameter "level" and dependent
//	 * "readSet", "run", "lane", "analysis").
//	 * @param contextValidation validation context
//	 * @param code              treatment code
//	 * @return                  true if the treatment exists in the database for the context object
//	 * @deprecated use explicit argument methods (no "level" or dependent argument).
//	 */
//	@Deprecated
//	private static boolean isTreatmentExist(ContextValidation contextValidation, String code) {
//		Level.CODE levelCode = getLevelFromContext(contextValidation);
//		
//		if (Level.CODE.ReadSet.equals(levelCode)) {
////			ReadSet readSet = (ReadSet) contextValidation.getObject("readSet");
//			ReadSet readSet = contextValidation.getTypedObject("readSet");
//			return MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//					                           DBQuery.and(DBQuery.is    ("code", readSet.code), 
//					                        		       DBQuery.exists("treatments."+code)));
//		} else if (Level.CODE.Lane.equals(levelCode)) {
////			Run run = (Run) contextValidation.getObject("run");
////			Lane lane = (Lane) contextValidation.getObject("lane");
//			Run  run  = contextValidation.getTypedObject("run");
//			Lane lane = contextValidation.getTypedObject("lane");
//			
//			return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
//					                           DBQuery.and(DBQuery.is("code", run.code), 
//					                        		       DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is    ("number", lane.number),
//					                        		    		                                  DBQuery.exists("treatments."+code)))));
//							
//		} else if (Level.CODE.Run.equals(levelCode)) {
////			Run run = (Run) contextValidation.getObject("run");
//			Run run = contextValidation.getTypedObject("run");
//			return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
//					                           DBQuery.and(DBQuery.is    ("code", run.code),
//					                        		       DBQuery.exists("treatments."+code)));			
//		} else if (Level.CODE.Analysis.equals(levelCode)) {
////			Analysis analysis = (Analysis) contextValidation.getObject("analysis");
//			Analysis analysis = contextValidation.getTypedObject("analysis");
//			return MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
//					                           DBQuery.and(DBQuery.is    ("code", analysis.code),
//					                        		       DBQuery.exists("treatments."+code)));	
//		}
//		return false;
//	}
	
	/**
	 * Does the treatment code exists in the analysis ?
	 * @param contextValidation validation context
	 * @param treatmentCode     treatment code
	 * @param analysis          analysis
	 * @return                  true if the treatment code exists in the analysis in the database
	 */
	public static boolean isTreatmentExist(ContextValidation contextValidation, String treatmentCode, Analysis analysis) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
				                           DBQuery.and(DBQuery.is    ("code", analysis.code),
				                        		       DBQuery.exists("treatments." + treatmentCode)));	
	}

	/**
	 * Does the treatment code exists in the read set ?
	 * @param contextValidation validation context
	 * @param treatmentCode     treatment code
	 * @param readSet           read set
	 * @return                  true if the treatment code exists in the read set in the database
	 */
	public static boolean isTreatmentExist(ContextValidation contextValidation, String treatmentCode, ReadSet readSet) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				                           DBQuery.and(DBQuery.is    ("code", readSet.code), 
					                       		       DBQuery.exists("treatments." + treatmentCode)));
	}
	
	/**
	 * Does the treatment code exists in the run ?
	 * @param contextValidation validation context
	 * @param treatmentCode     treatment code
	 * @param run               run
	 * @return                  true if the treatment code exists in the run in the database
	 */
	public static boolean isTreatmentExist(ContextValidation contextValidation, String treatmentCode, Run run) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				                           DBQuery.and(DBQuery.is    ("code", run.code),
				                        		       DBQuery.exists("treatments." + treatmentCode)));
	}
	
	/**
	 * Does the treatment code exists in the lane in a run ?
	 * @param contextValidation validation context
	 * @param treatmentCode     treatment code
	 * @param run               run
	 * @param lane              lane
	 * @return                  true if the treatment code exists in the lane of the run in the database
	 */
	public static boolean isTreatmentExist(ContextValidation contextValidation, String treatmentCode, Run run, Lane lane) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
				                           DBQuery.and(DBQuery.is("code", run.code), 
				                        		       DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is    ("number", lane.number),
				                        		    		                                  DBQuery.exists("treatments." + treatmentCode)))));
	}
					
//	/**
//	 * Validate results (dynamic objects) (context argument "level").
//	 * @param treatmentType     treatment type
//	 * @param results           results to validate
//	 * @param contextValidation validation context
//	 * @deprecated use {@link #validateResults(TreatmentType, Map, ContextValidation, Level.CODE)}
//	 */
//	@Deprecated
//	public static void validateResults(TreatmentType treatmentType, Map<String, Map<String, PropertyValue>> results, ContextValidation contextValidation) {
////		if (ValidationHelper.validateNotEmpty(contextValidation, results, "results")) {
////			Level.CODE levelCode = getLevelFromContext(contextValidation);
////			// validate all treatment key in input
////			for (Map.Entry<String, Map<String, PropertyValue>> entry : results.entrySet()) {
////				TreatmentTypeContext context = getTreatmentTypeContext(entry.getKey(), treatmentType.id);
////				if (context == null) {
////					contextValidation.addError(entry.getKey(),ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, entry.getKey());
////				}				
////			}
////			// validate if all treatment context are present
////			for (TreatmentTypeContext context : treatmentType.contexts) {
////				if (results.containsKey(context.code)) {
////					Map<String, PropertyValue> props = results.get(context.code);
////					contextValidation.addKeyToRootKeyName(context.code);					
////					ValidationHelper.validateProperties(contextValidation, props, treatmentType.getPropertyDefinitionByLevel(Level.CODE.valueOf(context.name), levelCode));
////					contextValidation.removeKeyFromRootKeyName(context.code);
////				} else if (context.required) {
////					contextValidation.addError(context.code,ValidationConstants.ERROR_REQUIRED_MSG, context.code);
////				}
////			}	
////		}
//		validateResults(treatmentType, results, contextValidation, getLevelFromContext(contextValidation));
//	}
	
	/**
	 * Validate results.
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type
	 * @param results           results to validate
	 * @param levelCode         treatment context object type
	 */
	public static void validateResults(ContextValidation contextValidation, TreatmentType treatmentType, Map<String, Map<String, PropertyValue>> results, Level.CODE levelCode) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, results, "results"))
			return;
		// validate all treatment key in input
		for (Map.Entry<String, Map<String, PropertyValue>> entry : results.entrySet()) {
			TreatmentTypeContext context = getTreatmentTypeContext(entry.getKey(), treatmentType.id);
			if (context == null) {
				contextValidation.addError(entry.getKey(),ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, entry.getKey());
			}				
		}
		// validate if all treatment context are present
		for (TreatmentTypeContext context : treatmentType.contexts) {
			if (results.containsKey(context.code)) {
				Map<String, PropertyValue> props = results.get(context.code);
				contextValidation.addKeyToRootKeyName(context.code);					
				ValidationHelper.validateProperties(contextValidation, props, treatmentType.getPropertyDefinitionByLevel(Level.CODE.valueOf(context.name), levelCode));
				contextValidation.removeKeyFromRootKeyName(context.code);
			} else if (context.required) {
				contextValidation.addError(context.code,ValidationConstants.ERROR_REQUIRED_MSG, context.code);
			}
		}	
	}
	
//	private static TreatmentTypeContext getTreatmentTypeContext(String contextCode, Long typeId) {
//		try {
//			return TreatmentTypeContext.find.get().findByTreatmentTypeId(contextCode, typeId);
//		} catch (DAOException e) {
//			throw new RuntimeException(e);
//		}
//	}
	/**
	 * Get a treatment type context from database.
	 * @param contextCode treatment context code
	 * @param typeId      treatment context type identifier
	 * @return            treatment type context
	 */
	private static TreatmentTypeContext getTreatmentTypeContext(String contextCode, Long typeId) {
		return TreatmentTypeContext.find.get().findByTreatmentTypeId(contextCode, typeId);
	}
	
	// ------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate that the treatment category code references the existing treatment type defined category.  
	 * @param treatmentType     treatment type to validate category of
	 * @param categoryCode      expected category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateTreatmentCategoryCode(ContextValidation, TreatmentType, String)}
	 */
	@Deprecated
	public static void validateTreatmentCategoryCode_(TreatmentType treatmentType, String categoryCode, ContextValidation contextValidation) {
		TreatmentValidationHelper.validateTreatmentCategoryCode(contextValidation, treatmentType, categoryCode);
	}

	/**
	 * Validate that the treatment category code references the existing treatment type defined category.  
	 * @param contextValidation validation context
	 * @param treatmentType     treatment type to validate category of
	 * @param categoryCode      expected category code
	 */
	public static void validateTreatmentCategoryCode(ContextValidation contextValidation, TreatmentType treatmentType, String categoryCode) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, categoryCode, "categoryCode"))
			return;
		TreatmentCategory tc = CommonValidationHelper.validateCodeForeignOptional(contextValidation, TreatmentCategory.miniFind.get(), categoryCode, "categoryCode", true);
		if (!treatmentType.category.equals(tc)) 
			contextValidation.addError("categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
	}

}
