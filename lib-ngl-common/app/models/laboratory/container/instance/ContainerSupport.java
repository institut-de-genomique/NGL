package models.laboratory.container.instance;

//import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.utils.ValidationHelper;

// This link : {@link models.laboratory.container.instance.ContainerSupport}

/**
 * Name of the collection is {@link models.utils.InstanceConstants#CONTAINER_SUPPORT_COLL_NAME}.
 */
public class ContainerSupport extends DBObject implements IValidation {
	
	public static final Supplier<ContainerSupportsDAO> find = new GuiceSupplier<>(ContainerSupportsDAO.class);
	
	/**
	 * Category code (type of container support) ({@link models.laboratory.container.description ContainerSupportCategory}).
	 */
	public String categoryCode;
	
	public State state;
	
	public String storageCode;
	public Valuation valuation; // GA: Must be disappear ???
	
	/**
	 * Access trace.
	 */
	public TraceInformation traceInformation;
	
	public Set<String> projectCodes;
	public Set<String> sampleCodes;
	public Set<String> fromTransformationTypeCodes; // GA: useful ???
	public Map<String, PropertyValue> properties;
	public Integer nbContainers;
	public Integer nbContents;
	
	/**
	 * Comments.
	 */
	public List<Comment> comments;
	
	public List<StorageHistory> storages;
	
	public ContainerSupport() {
		projectCodes                = new HashSet<>();
		sampleCodes                 = new HashSet<>();
		fromTransformationTypeCodes = new HashSet<>();
		valuation                   = new Valuation();
	}

	@JsonIgnore
	@Override
//	@Deprecated
	public void validate(ContextValidation contextValidation) {
		// No method call requires this parameter
//		if (contextValidation.getObject(FIELD_STATE_CODE) == null) {
//			contextValidation.putObject(FIELD_STATE_CODE , state.code);			
//		}
		CommonValidationHelper          .validateIdPrimary                           (contextValidation, this);
		CommonValidationHelper          .validateCodePrimary                         (contextValidation, this, InstanceConstants.CONTAINER_SUPPORT_COLL_NAME);
		CommonValidationHelper          .validateStateRequired                       (contextValidation, ObjectType.CODE.Container, state);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, categoryCode);
		CommonValidationHelper          .validateProjectCodes                        (contextValidation, projectCodes);
		CommonValidationHelper          .validateSampleCodes                         (contextValidation, sampleCodes);
		CommonValidationHelper          .validateExperimentTypeCodes                 (contextValidation, fromTransformationTypeCodes);
		CommonValidationHelper			.validateFieldMaxSize						 (contextValidation, "code", code, 30);
		ValidationHelper                .validateNotEmpty                            (contextValidation, nbContainers, "nbContainers");
		ValidationHelper                .validateNotEmpty                            (contextValidation, nbContents,   "nbContents");
		
		// GA: Validate properties
	}
	
}
