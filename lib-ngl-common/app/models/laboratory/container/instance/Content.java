package models.laboratory.container.instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContentValidationHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link models.laboratory.container.instance.Container} content.
 * Container contains a number of embedded Content {@link models.laboratory.container.instance.Container#contents}.
 *  
 */
public class Content {

	// Embedded Sample information
	
	/**
	 * Code the contained sample ({@link models.laboratory.sample.instance.Sample#code}).
	 */
	public String sampleCode;
	
	/**
	 * Type code of the contained sample ({@link models.laboratory.sample.instance.Sample#typeCode}).
	 */
	public String sampleTypeCode;
	
	/**
	 * Category code of the contained sample ({@link models.laboratory.sample.instance.Sample#categoryCode}).
	 */
	public String sampleCategoryCode;
	
	/**
	 * The content is defined for a given project.
	 */
	public String projectCode;
	
	/**
	 * Percentage of this content in the container.
	 */
	public Double percentage;
	
	/**
	 * Sample collaborator ({@link models.laboratory.sample.instance.Sample#referenceCollab}).
	 */
	public String referenceCollab;
	
	/**
	 * Taxonomy code of the contained sample ({@link models.laboratory.sample.instance.Sample#taxonCode}).
	 */
	public String taxonCode;
	
	/**
	 * Taxonomy scientific name ({@link models.laboratory.sample.instance.Sample#ncbiScientificName}).
	 */
	public String ncbiScientificName;

	public Map<String,PropertyValue> properties;

	/* Put process properties to analyse container*/
	//public String processTypeCode;
	//public String processCode;
	public Map<String,PropertyValue> processProperties;
	
	public List<Comment> processComments;

	public Content() {
		properties = new HashMap<>();
	}

	@JsonIgnore
	public Content(String sampleCode, String typeCode, String categoryCode) {
		this.sampleCode         = sampleCode;
		this.sampleTypeCode     = typeCode;
		this.sampleCategoryCode = categoryCode;
		this.properties         = new HashMap<>();		
	}


	/**
	 * Validate this content (context parameter {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}).
	 * @param contextValidation validation context
	 * @deprecated use {@link #validate(ContextValidation, String)}
	 */
	@JsonIgnore
	@Deprecated
	public void validate(ContextValidation contextValidation) {
		ContentValidationHelper.validateSampleCodeRequired        (contextValidation, sampleCode);
		CommonValidationHelper .validateProjectCodeRequired       (contextValidation, projectCode);
		ContentValidationHelper.validateSampleCodeWithProjectCode (contextValidation, projectCode, sampleCode);
		ContentValidationHelper.validateSampleCategoryCodeRequired(contextValidation, sampleCategoryCode);
		ContentValidationHelper.validateSampleTypeCodeRequired    (contextValidation, sampleTypeCode);
		ContentValidationHelper.validatePercentageContentRequired (contextValidation, percentage);
		ContentValidationHelper.validateProperties                (contextValidation, sampleTypeCode, properties);
	}
	
	/**
	 * Validate this context using an optional import type code.
	 * @param contextValidation validation context
	 * @param importTypeCode    optional import type code
	 */
	public void validate(ContextValidation contextValidation, String importTypeCode) {
		ContentValidationHelper.validateSampleCodeRequired        (contextValidation, sampleCode);
		CommonValidationHelper .validateProjectCodeRequired       (contextValidation, projectCode);
		ContentValidationHelper.validateSampleCodeWithProjectCode (contextValidation, projectCode, sampleCode);
		ContentValidationHelper.validateSampleCategoryCodeRequired(contextValidation, sampleCategoryCode);
		ContentValidationHelper.validateSampleTypeCodeRequired    (contextValidation, sampleTypeCode);
		ContentValidationHelper.validatePercentageContentRequired (contextValidation, percentage);
		ContentValidationHelper.validateProperties                (contextValidation, sampleTypeCode, properties, importTypeCode);
	}

	@Override
	public Content clone() {
		Content clone = new Content();

		clone.projectCode        = projectCode;
		clone.sampleCode         = sampleCode;
		clone.sampleCategoryCode = sampleCategoryCode;
		clone.sampleTypeCode     = sampleTypeCode;
		clone.referenceCollab    = referenceCollab;
		clone.percentage         = percentage;
		if (properties != null)
			clone.properties = new HashMap<>(properties);
		clone.taxonCode          = taxonCode;
		clone.ncbiScientificName = ncbiScientificName;
		if (processProperties != null)
			clone.processProperties = new HashMap<>(processProperties);
		clone.processComments    = processComments;
		return clone;
	}
	
}
