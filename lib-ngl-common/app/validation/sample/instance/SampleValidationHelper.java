package validation.sample.instance;

import static models.utils.InstanceConstants.EXPERIMENT_COLL_NAME;//FDS
import static validation.common.instance.CommonValidationHelper.FIELD_EXPERIMENT; //FDS

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import fr.cea.ig.MongoDBDAO;//FDS

import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;//FDS

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.laboratory.experiment.instance.Experiment; //FDS
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class SampleValidationHelper {

	// ---------------------------------------------------------------------
	// renamed and arguments reordered
	
	//NGL-4046
	public static void validateTaxonCode(ContextValidation contextValidation, String taxonCode) {
		String patternTaxonCode = "^\\s*\\d+\\s*$"; // string avec uniquement des chiffres, avec espaces autorisés en debut ou fin de chaine
		if(StringUtils.isNotBlank(taxonCode)) {
			if (! taxonCode.matches(patternTaxonCode)) {
				contextValidation.addError("taxonCode." + taxonCode, ValidationConstants.ERROR_BAD_FORMAT, taxonCode);
			} 
		}
	}	
	
	/**
	 * Validate sample category code.
	 * @param categoryCode      sample category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateSampleCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate sample category code.
	 * @param contextValidation validation context
	 * @param categoryCode      sample category code
	 */
	public static void validateSampleCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, SampleCategory.miniFind.get(), categoryCode, "categoryCode", false);
	}
	
	// -----------------------------------------------------------------------
	
//	public static void validateSampleType(String typeCode,
//										  String importTypeCode, 
//										  Map<String, PropertyValue> properties,
//										  ContextValidation contextValidation) {
//
//		List<PropertyDefinition> proDefinitions = new ArrayList<>();
////		SampleType sampleType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, typeCode, "typeCode", SampleType.find.get(),true);
//		SampleType sampleType = validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), typeCode, "typeCode", true);
//		if (sampleType != null) {
//			proDefinitions.addAll(sampleType.getPropertiesDefinitionDefaultLevel());				
//		}
////		ImportType importType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find.get(),true);
//		ImportType importType = validateCodeForeignRequired(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
//		if (importType != null) {
//			proDefinitions.addAll(importType.getPropertiesDefinitionSampleLevel());			
//		}
//		if (proDefinitions.size() > 0) {
//			ValidationHelper.validateProperties(contextValidation,properties, proDefinitions, false); // need false because we generate sample from another sample
//		}
//	}
	
	/**
	 * Validate the sample type (typeCode), the import type (importTypeCode) and the
	 * properties (properties).
	 * @param typeCode          sample type code
	 * @param importTypeCode    sample import type code
	 * @param properties        sample properties
	 * @param contextValidation validation context
	 */
	public static void validateSampleType(String                     typeCode,
			                              String                     importTypeCode, 
			                              Map<String, PropertyValue> properties,
			                              ContextValidation          contextValidation) {
		List<PropertyDefinition> proDefinitions = new ArrayList<>();
		SampleType sampleType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), typeCode, "typeCode", true);
		if (sampleType != null)
//			proDefinitions.addAll(sampleType.getPropertiesDefinitionDefaultLevel()); // getPropertyDefinitionByLevel(Level.CODE.Sample)			
			proDefinitions.addAll(sampleType.getPropertiesDefinitionSampleLevel());			
		ImportType importType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
		if (importType != null)
			proDefinitions.addAll(importType.getPropertiesDefinitionSampleLevel());	
		if (proDefinitions.size() > 0)
			// need false (ignore extraneous properties) because we generate sample from another sample
			ValidationHelper.validateProperties(contextValidation, properties, proDefinitions, false); 
		
	}
	
	// NGL-4099
	/**
	 * Validate the importTypeCode external-readsets-import 
	 * @param importTypeCode    sample import type code
	 * @param categoryCode      sample category code
	 * @param properties        sample properties
	 * @param contextValidation validation context
	 */
	public static void validateExternalReadsetsImport(  String                     importTypeCode, 
														String                     categoryCode,
														Map<String, PropertyValue> properties,
														ContextValidation          contextValidation) {
		if (importTypeCode != null && importTypeCode.equals("external-readsets-import") ) {
			// verifier si amplicon que les proprietes targetedRegion et amplificationPrimer existent
			if (categoryCode != null && categoryCode.equals(("amplicon"))) {
				if (! properties.containsKey("targetedRegion") 
						|| properties.get("targetedRegion").getValue() == null ) {
					contextValidation.addError("targetedRegion", ValidationConstants.ERROR_REQUIRED_MSG);

				}

				if(! properties.containsKey("amplificationPrimers") 
						|| properties.get("amplificationPrimers").getValue() == null ) {
					contextValidation.addError("amplificationPrimers", ValidationConstants.ERROR_REQUIRED_MSG);
				}
			}
			// verifier si autre que amplicon que les proprietes targetedRegion et amplificationPrimer n'existent pas
			if ( categoryCode != null && ! categoryCode.equals(("amplicon"))) {
				if ( properties.containsKey("targetedRegion") ) {
					contextValidation.addError("targetedRegion", ValidationConstants.ERROR_COMPATIBILITY_TYPECODE_PROPERTY_MSG, "Region ciblee", categoryCode);
				}
				if ( properties.containsKey("amplificationPrimers") ) {
					contextValidation.addError("amplificationPrimers", ValidationConstants.ERROR_COMPATIBILITY_TYPECODE_PROPERTY_MSG, "Primers", categoryCode);
				}
			}
		}
	}
	
	
	/**
	 * Validate rules.
	 * @param sample            sample
	 * @param contextValidation validation context
	 * @deprecated use {@link CommonValidationHelper#validateRulesWithObjects(ContextValidation, Object...)}
	 */
	@Deprecated
	public static void validateRules(Sample sample, ContextValidation contextValidation) {
		CommonValidationHelper.validateRulesWithObjects(contextValidation, sample);
	}
	

	/**
	 * Validates that sample code not already used in a experiment 
	 * someone may be currently creating another experiment with same sample as output...
	 * @author fdsantos 
	 * @since 13/09/2019
	 * @param sampleCode     sample code to validate
	 * @param contextValidation validation context ( contains current experiment)
	 */
	public static void validateSampleNotUsedInOtherExperiment(String sampleCode,ContextValidation contextValidation) {

		Experiment exp = contextValidation.getTypedObject(FIELD_EXPERIMENT);
		//System.out.println("FDS-DEBUG: experiment="+ exp.code );
		String rootKeyName="outputContainerUseds[0].experimentProperties.sampleCode";
		
		if ( null != exp ) {
		  //System.out.println("FDS-DEBUG: experiment="+ exp.code );
		  // rechercher dans les experiences differente de l'experience en cours...
		  if (MongoDBDAO.checkObjectExist(EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.notEquals("code", exp.code).in("sampleCodes", sampleCode))){
			  contextValidation.addError(rootKeyName, "error.validationexp.sample.alreadyused",sampleCode);
		  }
		} else {
			//System.out.println("FDS-DEBUG: no experiment");
			// rechercher toutes les experiences ....JAMAIS UTILISE ?????
			if (MongoDBDAO.checkObjectExist(EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("sampleCodes", sampleCode))){
			  contextValidation.addError(rootKeyName, "error.validationexp.sample.alreadyused",sampleCode);
			}
		}
	}
}
