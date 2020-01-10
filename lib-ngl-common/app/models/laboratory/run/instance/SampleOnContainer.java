package models.laboratory.run.instance;

import java.util.Date;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.description.SampleType;
import validation.ContextValidation;
import validation.IValidation;
import validation.run.instance.SampleOnContainerValidationHelper;
import validation.utils.ValidationHelper;

/**
 * Class used to stock information from sample on the last container
 * 
 * @author galbini
 *
 */
public class SampleOnContainer implements IValidation {
	
	// last update date
	public Date lastUpdateDate;
	// Reference Sample code
	public String projectCode;
	// Reference Sample code
	public String sampleCode;
	// Reference SampleType code
	public String sampleTypeCode;
	// Reference SampleCategory code
	public String sampleCategoryCode;
	// Reference to the container support
	public String containerSupportCode;
	// Reference to the container
	public String containerCode;
	// Properties of the content in the container
	public Map<String,PropertyValue> properties;
	// Percentage of content on the container
	public Double percentage;
	// Collaborator's reference
	public String referenceCollab;
	// NCBI information from Sample
	public String taxonCode;
	public String ncbiScientificName;
	// Measured concentration from Container
	public PropertySingleValue containerConcentration;
	
	@Override
	public String toString() {
		return "SampleOnContainer [lastUpdateDate=" + lastUpdateDate
				+ ", sampleCode=" + sampleCode + ", sampleTypeCode="
				+ sampleTypeCode + ", sampleCategoryCode=" + sampleCategoryCode
				+ ", containerSupportCode=" + containerSupportCode
				+ ", containerCode=" + containerCode + ", referenceCollab="+ referenceCollab + "]";
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		// GA 09/11/2016 : projecCode validation after MEP 
		// SampleOnContainerValidationHelper.validateProjectCode(projectCode, contextValidation);
		SampleOnContainerValidationHelper.validateSampleCode(sampleCode, contextValidation);
		SampleType sampleType = SampleOnContainerValidationHelper.validateSampleTypeCode(sampleTypeCode, contextValidation);
		SampleOnContainerValidationHelper.validateSampleCategoryCode(sampleCategoryCode, sampleType, contextValidation);
		ValidationHelper                 .validateNotEmpty          (contextValidation, containerSupportCode, "containerSupportCode");
		ValidationHelper                 .validateNotEmpty          (contextValidation, containerCode, "containerCode");
		ValidationHelper                 .validateNotEmpty          (contextValidation, referenceCollab, "referenceCollab");
	}

}
