package validation.reagentCatalogs.instance;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class KitCatalogValidationHelper {
	
	private static KitCatalog getKitCatalog(String code) {
		return MongoDBDAO.findByCode(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, code);
	}
	
	private static <T extends DBObject> Stream<T> mongoStream(String collectionName, Class<T> t, Query query){
		Iterator<T> iterator = MongoDBDAO.find(collectionName, t, query).getCursor().iterator();
		Spliterator<T> splititerator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
		return StreamSupport.stream(splititerator, false);
	}
	
	private static <T extends DBObject> List<String> getCodes(String collectionName, Class<T> t, Query query) {
		return mongoStream(collectionName, t, query)
				.map(DBObject::getCode)
				.collect(Collectors.toList());
	}
	
	private static List<String> getBoxCatalogCodesFromKit(String kitCatalogCode) {
		Query boxCatalogQuery = DBQuery.and(
				DBQuery.is("category", "Box"), 
				DBQuery.is("kitCatalogCode", kitCatalogCode)
				);
		return getCodes(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, boxCatalogQuery);
	}
	
	private static List<String> getReagentCatalogCodesFromKit(String kitCatalogCode) {
		Query reagentCatalogQuery = DBQuery.and(
				DBQuery.is("category", "Reagent"), 
				DBQuery.is("kitCatalogCode", kitCatalogCode)
				);
		return getCodes(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, reagentCatalogQuery);
	}

	// --------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a list of required experiment type codes. 
	 * @param experimentTypes   experiment type codes 
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentTypeCodes(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateExperimentTypes(List<String> experimentTypes, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateExperimentTypeCodes(contextValidation, experimentTypes);
	}
	
	/**
	 * Validate a list of required experiment type codes. 
	 * @param contextValidation validation context
	 * @param experimentTypes   experiment type codes 
	 */
	public static void validateExperimentTypeCodes(ContextValidation contextValidation, List<String> experimentTypes) {
		for (String et : experimentTypes)
			CommonValidationHelper.validateCodeForeignRequired(contextValidation, ExperimentType.miniFind.get(), et, "experimentTypeCodes", true);
	}
	
	/**
	 * Validate updated list of required experiment type codes.
	 * @param contextValidation validation context
	 * @param kitCatalogCode kit catalog code
	 * @param updatedExperimentTypes experiment type codes
	 */
	public static void validateExperimentTypeCodesUpdate(ContextValidation contextValidation, String kitCatalogCode, List<String> updatedExperimentTypes) {
		KitCatalog kitCatalog = getKitCatalog(kitCatalogCode);
		UpdatedExperimentTypesValidator validator = new UpdatedExperimentTypesValidator(kitCatalogCode, updatedExperimentTypes);
		validateExperimentTypeCodesUpdate(contextValidation, kitCatalog.experimentTypeCodes, validator);
	}
	
	/**
	 * Validate updated list of required experiment type codes.
	 * @param contextValidation validation context
	 * @param kitCatalogCode kit catalog code
	 * @param updatedExperimentTypes experiment type codes
	 */
	public static void validateExperimentTypeCodesUpdate(ContextValidation contextValidation, List<String> experimentTypeCodes, UpdatedExperimentTypesValidator validator) {
		for(String experimentTypeCode : experimentTypeCodes) {
			validator.validateIsNotRemovingUsedExperimentType(contextValidation, experimentTypeCode);
		}
	}

	// --------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate a required kit catalog code.
	 * @param kitCatalogCode    kit catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateKitCatalogCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateKitCatalogCode(String kitCatalogCode, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateKitCatalogCode(contextValidation, kitCatalogCode);
	}

	/**
	 * Validate a required kit catalog code.
	 * @param contextValidation validation context
	 * @param kitCatalogCode    kit catalog code
	 */
	public static void validateKitCatalogCode(ContextValidation contextValidation, String kitCatalogCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, kitCatalogCode, "kitCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("code",kitCatalogCode))) {
				contextValidation.addError("kitCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, kitCatalogCode);
			}
		}
	}

	// --------------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate a required box catalog code.
	 * @param boxCatalogCode    box catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateBoxCatalogCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateBoxCatalogCode(String boxCatalogCode, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateBoxCatalogCode(contextValidation, boxCatalogCode);
	}

	/**
	 * Validate a required box catalog code.
	 * @param contextValidation validation context
	 * @param boxCatalogCode    box catalog code
	 */
	public static void validateBoxCatalogCode(ContextValidation contextValidation, String boxCatalogCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, boxCatalogCode, "boxCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.and(DBQuery.is("code",boxCatalogCode)))){
				contextValidation.addError("boxCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, boxCatalogCode);
			}
		}
	}
	
	// ---------------------------------------------------------------------------
	
	/**
	 * KitCatalog's experimentTypes update validator
	 * @author aprotat
	 * @see validation.reagentCatalogs.instance.KitCatalogValidationHelper#validateExperimentTypeCodesUpdate(ContextValidation, String, List)
	 */
	public static class UpdatedExperimentTypesValidator {
		
		private static final int EXPERIMENT_LIST_SIZE_LIMIT = 3;
		
		public static final String ERROR_PROPERTY = "experimentTypeCodes";
		public static final String ERROR_MSG = "error.reagent.catalog.remove.used.experiment.type";
		
		final String kitCatalogCode;
		final Set<String> updatedExperimentTypes;
		
		Query catalogCodesQuery;
		
		public UpdatedExperimentTypesValidator(String kitCatalogCode, Collection<String> updatedExperimentTypes) {
			this.kitCatalogCode = kitCatalogCode;
			this.updatedExperimentTypes = new HashSet<>(updatedExperimentTypes);
		}
		
		private Query getCatalogCodesQuery() {
			if(catalogCodesQuery == null) {
				List<String> boxCatalogCodes = getBoxCatalogCodesFromKit(kitCatalogCode);
				List<String> reagentCatalogCodes = getReagentCatalogCodesFromKit(kitCatalogCode);
				catalogCodesQuery = DBQuery.or(
						DBQuery.is("reagents.kitCatalogCode", kitCatalogCode),
						DBQuery.in("reagents.boxCatalogCode", boxCatalogCodes),
						DBQuery.in("reagents.reagentCatalogCode", reagentCatalogCodes)
						);
			} return catalogCodesQuery;
		}
		
		private List<String> getExperimentsUsingTheKitByType(String experimentTypeCode) {
			Query query = DBQuery.and(
					DBQuery.is("typeCode", experimentTypeCode),
					getCatalogCodesQuery()
					);
			return getCodes(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query);
		}
		
		private String formatExperimentCodesList(List<String> experimentsUsingKitCodes) {
			if(experimentsUsingKitCodes.size() > EXPERIMENT_LIST_SIZE_LIMIT) {
				List<String> experimentsSubList = experimentsUsingKitCodes.subList(0, EXPERIMENT_LIST_SIZE_LIMIT);
				return String.join(", ", experimentsSubList) + ", ...";
			} else {
				return String.join(", ", experimentsUsingKitCodes);
			}
		}
		
		private void addErrorToContext(ContextValidation contextValidation, String experimentTypeCode, List<String> experimentsUsingKitCodes) {
			String experimentCodesListAsString = formatExperimentCodesList(experimentsUsingKitCodes);
			contextValidation.addError(ERROR_PROPERTY, ERROR_MSG, experimentTypeCode, experimentCodesListAsString);
		}
		
		/**
		 * Is current experimentTypeCodes update removing the given experimentTypeCode (assuming it was in kitCatalog's experimentTypeCodes at first).
		 * @param experimentTypeCode experiment type code
		 * @return boolean
		 */
		public boolean isRemovingThis(String experimentTypeCode) {
			return !updatedExperimentTypes.contains(experimentTypeCode);
		}
		
		/**
		 * Add error to context if current update try to remove an experimentType, where at least one experiment of this type is already using the KitCatalog (or their children).
		 * @param contextValidation validation context
		 * @param experimentTypeCode experiment type code
		 */
		public void validateIsNotRemovingUsedExperimentType(ContextValidation contextValidation, String experimentTypeCode) {
			if(isRemovingThis(experimentTypeCode)) {
				List<String> experimentsUsingKitCodes = getExperimentsUsingTheKitByType(experimentTypeCode);
				if(!experimentsUsingKitCodes.isEmpty()) {
					addErrorToContext(contextValidation, experimentTypeCode, experimentsUsingKitCodes);
				}
			}
		}
	}

}
