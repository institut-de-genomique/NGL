package models.laboratory.container.instance;


import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

// This link : {@link models.laboratory.container.instance.LocationOnContainerSupport}

/**
 * 
 * Embedded data in collection Container
 * Associate support and container with a position (column, line)
 * 
 * If container category is  tube, the position is (column,line)=(1,1) and support category is 'VIDE'
 * 
 * A support instance defines by unique supportCode /line/column, a supportCode (ex barCode) can be referenced in many container.support 
 * 
 * @author mhaquell
 *
 */
public class LocationOnContainerSupport implements IValidation {
	
	/**
	 * Container code.
	 */
	public String code;
		
	/**
	 * Support category (type of container support) ({@link models.laboratory.container.description ContainerSupportCategory}).
	 */
	public String categoryCode;

	public String storageCode;
	
	// Container coordinates in support
	public String column;
	public String line;
	
	/**
	 * Validate
	 * <ul>
	 *   <li>the required container support code</li>
	 *   <li>the coordinates on support are unique in the database</li>
	 *   <li>the required container support category code</li>
	 * </ul>
	 */
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper          .validateContainerSupportCodeRequired        (contextValidation, code, "code");
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition  (contextValidation, this);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, categoryCode);
		// NGL-2332 ajout validation de cohérence line/column du support
		ContainerSupportValidationHelper.validateContainerSupportLineColumnConsistancy(contextValidation, this);
	}

}
