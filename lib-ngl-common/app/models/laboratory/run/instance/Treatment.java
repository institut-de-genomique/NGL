package models.laboratory.run.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.description.TreatmentType;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.TreatmentValidationHelper;

public class Treatment {

	private static final play.Logger.ALogger logger = play.Logger.of(Treatment.class);
	
	public String code;
	
	/**
	 * Required treatment type ({@link TreatmentType}). 
	 */
	public String typeCode;
	
	public String categoryCode;
	
	@JsonIgnore
	public Map<String, Map<String, PropertyValue>> results = new HashMap<>();
	
	@JsonAnyGetter
    public Map<String,Map<String,PropertyValue>> results() {
        return results;
    }

    @JsonAnySetter
    public void set(String name, Map<String,PropertyValue> value) {
    	results.put(name, value);
    }

//    /**
//     * Validate treatment ().
//     */
//	@Override
//	@Deprecated
//	public void validate(ContextValidation contextValidation) {
//		TreatmentType treatmentType = TreatmentValidationHelper.validateCodeForeignRequired(contextValidation, TreatmentType.miniFind.get(), typeCode, "typeCode",true);
//		if (treatmentType != null) {
//			logger.debug("validate - treatmentType : {} ({})", treatmentType, contextValidation.getRootKeyName());
//			TreatmentValidationHelper.validateTreatmentCode        (contextValidation, treatmentType, code);
//			TreatmentValidationHelper.validateTreatmentCategoryCode(contextValidation, treatmentType, categoryCode);
//			TreatmentValidationHelper.validateResults              (treatmentType, results,      contextValidation);						
//		} else {
//			logger.debug("validate SKIP - treatmentType : {} ({})", treatmentType, contextValidation.getRootKeyName());
//		}
//	}
	
    /**
     * Generic treatment context implementation.
     * @param contextValidation     validation context
     * @param validateTreatmentCode treatment code validation procedure
     * @param level                 level
     */
	private void validate(ContextValidation contextValidation, Consumer<TreatmentType> validateTreatmentCode, Level.CODE level) {
		TreatmentType treatmentType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, TreatmentType.miniFind.get(), typeCode, "typeCode",true);
		if (treatmentType != null) {
			logger.debug("validate - treatmentType : {} ({})", treatmentType, contextValidation.getRootKeyName());
			validateTreatmentCode.accept(treatmentType);
			TreatmentValidationHelper.validateTreatmentCategoryCode(contextValidation, treatmentType, categoryCode);
			TreatmentValidationHelper.validateResults              (contextValidation, treatmentType, results, level);						
		} else {
			logger.debug("validate SKIP - treatmentType : {} ({})", treatmentType, contextValidation.getRootKeyName());
		}		
	}
	
	/**
	 * Validate this treatment in an analysis context.
	 * @param contextValidation validation context
	 * @param analysis          analysis
	 */
	public void validate(ContextValidation contextValidation, Analysis analysis) {
		validate(contextValidation, tt -> TreatmentValidationHelper.validateTreatmentCode(contextValidation, tt, code, analysis), Level.CODE.Analysis); 
	}
	
	/**
	 * Validate this treatment in a read set context.
	 * @param contextValidation validation context
	 * @param readSet           read set
	 */
	public void validate(ContextValidation contextValidation, ReadSet readSet) {
		validate(contextValidation, tt -> TreatmentValidationHelper.validateTreatmentCode(contextValidation, tt, code, readSet), Level.CODE.ReadSet); 
	}
	
	/**
	 * Validate this treatment in a run context.
	 * @param contextValidation validation context
	 * @param run               run
	 */
	public void validate(ContextValidation contextValidation, Run run) {
		validate(contextValidation, tt -> TreatmentValidationHelper.validateTreatmentCode(contextValidation, tt, code, run), Level.CODE.Run); 
	}
	
	/**
	 * Validate this treatment in a lane (and associated run) context.
	 * @param contextValidation validation context
	 * @param run               lane run
	 * @param lane              lane
	 */
	public void validate(ContextValidation contextValidation, Run run, Lane lane) {
		validate(contextValidation, tt -> TreatmentValidationHelper.validateTreatmentCode(contextValidation, tt, code, run, lane), Level.CODE.Lane); 
	}

}
