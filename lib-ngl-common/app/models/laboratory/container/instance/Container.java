package models.laboratory.container.instance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.containers.ContainersDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.tree.TreeOfLifeNode;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;

// link to this : {@link models.laboratory.container.instance.Container}

/**
 * Container is referenced from Experiment, Purifying, TransferMethod, 
 * Extraction, QC in embedded class ListInputOutputContainer.
 * The Relationship between containers aren't stored in the container 
 * but in class/collection RelationshipContainer. 
 * In Container, the link with experiment is the attribute 'fromExperimentTypes' 
 * who helps to manage Container in workflow. 
 *  
 * Container collection name is defined as {@link models.utils.InstanceConstants#CONTAINER_COLL_NAME}.
 * 
 * @author mhaquell
 *
 */
public class Container extends DBObject implements IValidation, ITracingAccess {

	public static final Supplier<ContainersDAO> find = new GuiceSupplier<>(ContainersDAO.class);
	
	// duplication for input in exp : code, categoryCode, contents, mesured*, // contents just for tag and tagCategory 
	// duplication for output in exp :code, categoryCode, contents, mesured*, // contents just for tag and tagCategory
	
	public String importTypeCode;
	
	/**
	 * Category code ({@link models.laboratory.container.description.ContainerCategory}). 
	 */
	public String categoryCode;

	/**
	 * State information, see {@link models.laboratory.common.instance.State} for definition.
	 */
	public State state;
	
	/**
	 * Valuation information.
	 */
	public Valuation valuation;

	/**
	 * Access information.
	 */
	public TraceInformation traceInformation;
	
	/**
	 * Properties.
	 */
	public Map<String, PropertyValue> properties;
	
	/**
	 * Comments.
	 */
	public List<Comment> comments = new ArrayList<>(0);

	/**
	 * Relation with container support.
	 */
	public LocationOnContainerSupport support; 

	/**
	 * Content description as a list of content that describe the
	 * parts contained in this container (percentages of samples).
	 */
	public List<Content> contents;
	
	/**
	 * QC Experiment results (projection). 
	 */
	public List<QualityControlResult> qualityControlResults; 

	public PropertySingleValue volume;        
	public PropertySingleValue concentration; 
	public PropertySingleValue quantity; 	
	public PropertySingleValue size;
	
	// For search optimisation
	public Set<String> projectCodes; // getProjets
	public Set<String> sampleCodes; // getSamples
	// ExperimentType must be an internal or external experiment ( origine )
	// List for pool experimentType
	public Set<String> fromTransformationTypeCodes;
	public Set<String> fromTransformationCodes; 
	public Set<String> processTypeCodes;
	public Set<String> processCodes;

	public String fromPurificationTypeCode;
	public String fromPurificationCode; 
	public String fromTransfertCode;
	public String fromTransfertTypeCode;
	
	/**
	 * Container history tree.
	 */
	public TreeOfLifeNode treeOfLife;
	
	public Container() {
		contents          = new ArrayList<>();
		traceInformation  = new TraceInformation();
		projectCodes      = new HashSet<>();
		sampleCodes       = new HashSet<>();
		fromTransformationTypeCodes = new HashSet<>();
		valuation         = new Valuation();	
	}
	
	public List<Content> getContents() {
		return contents;
	}
		
//	/**
//	 * Validate a container (optional context parameter {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}).
//	 */
//	@JsonIgnore
//	@Override
//	@Deprecated
//	public void validate(ContextValidation contextValidation) {
//		
//		if (contextValidation.getObject(CommonValidationHelper.FIELD_STATE_CODE) == null)
//			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE , state.code);			
//		
//		CommonValidationHelper.   validateIdPrimary               (contextValidation, this);
//		CommonValidationHelper.   validateCodePrimary             (contextValidation, this, InstanceConstants.CONTAINER_COLL_NAME);
//		ContainerValidationHelper.validateContainerStateRequired  (contextValidation, state);
//		CommonValidationHelper.   validateTraceInformationRequired(contextValidation, traceInformation);
//		ContainerValidationHelper.validateContainerCategoryCodeRequired   (contextValidation, categoryCode, support.categoryCode);
//		// GA: processTypeCodes
//		CommonValidationHelper   .validateProjectCodes            (contextValidation, projectCodes);
//		CommonValidationHelper   .validateSampleCodes             (contextValidation, sampleCodes);
//		CommonValidationHelper   .validateExperimentTypeCodes     (contextValidation, fromTransformationTypeCodes);
//		ContainerValidationHelper.validateImportTypeOptional              (contextValidation, importTypeCode ,properties);
//		
//		if (importTypeCode != null) 
//			contextValidation.putObject(CommonValidationHelper.FIELD_IMPORT_TYPE_CODE , importTypeCode);			
//		
//		ContainerValidationHelper.validateContents                (contextValidation, contents);
//		ContainerValidationHelper.validateContainerSupportRequired        (contextValidation, support);       // bug here Yann
//		ContainerValidationHelper.validateInputProcessCodes       (contextValidation, processCodes);
//		
//		ContainerValidationHelper.validateQualityControlResults   (contextValidation, qualityControlResults);
//		
//		ContainerValidationHelper.validateConcentrationOptional   (contextValidation, concentration);
//		ContainerValidationHelper.validateQuantityOptional        (contextValidation, quantity);
//		ContainerValidationHelper.validateVolumeOptional          (contextValidation, volume);
//		ContainerValidationHelper.validateSizeOptional            (contextValidation, size);
//		ContainerValidationHelper.validateRules                   (contextValidation, this);
//	}
	
	/**
	 * Validate a container (optional context parameter {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}).
	 */
	@JsonIgnore
	@Override
	@Deprecated
	public void validate(ContextValidation contextValidation) {
		validate(contextValidation, null, null);
	}
	
	// Root calls look like validate(ctx, null, null) 
	public void validate(ContextValidation contextValidation, String importTypeCode_, String stateCode) {
		
//		if (contextValidation.getObject(CommonValidationHelper.FIELD_STATE_CODE) == null)
//			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE , state.code);			
		if (stateCode == null)
			stateCode = state.code;
		
		CommonValidationHelper.   validateIdPrimary                    (contextValidation, this);
		CommonValidationHelper.   validateCodePrimary                  (contextValidation, this, InstanceConstants.CONTAINER_COLL_NAME);
		ContainerValidationHelper.validateContainerStateRequired       (contextValidation, state);
		CommonValidationHelper.   validateTraceInformationRequired     (contextValidation, traceInformation);
		ContainerValidationHelper.validateContainerCategoryCodeRequired(contextValidation, categoryCode, support.categoryCode);
		// GA: processTypeCodes
		CommonValidationHelper   .validateProjectCodes                 (contextValidation, projectCodes);
		CommonValidationHelper   .validateSampleCodes                  (contextValidation, sampleCodes);
		CommonValidationHelper   .validateExperimentTypeCodes          (contextValidation, fromTransformationTypeCodes);
		ContainerValidationHelper.validateImportTypeOptional           (contextValidation, importTypeCode, properties);
		
//		if (importTypeCode != null) 
//			contextValidation.putObject(CommonValidationHelper.FIELD_IMPORT_TYPE_CODE , importTypeCode);			
		if (importTypeCode != null)
			importTypeCode_ = importTypeCode;
		
		ContainerValidationHelper.validateContents                     (contextValidation, contents, importTypeCode_);
		ContainerValidationHelper.validateContainerSupportRequired     (contextValidation, support);       // bug here Yann
		ContainerValidationHelper.validateInputProcessCodes            (contextValidation, processCodes, stateCode);		
		ContainerValidationHelper.validateQualityControlResults        (contextValidation, qualityControlResults);
		ContainerValidationHelper.validateConcentrationOptional        (contextValidation, concentration);
		ContainerValidationHelper.validateQuantityOptional             (contextValidation, quantity);
		ContainerValidationHelper.validateVolumeOptional               (contextValidation, volume);
		ContainerValidationHelper.validateSizeOptional                 (contextValidation, size);
		ContainerValidationHelper.validateRules                        (contextValidation, this);
	}

	// IAccessTracking
	
	@Override
	public TraceInformation getTraceInformation() {
		if (traceInformation == null)
			traceInformation = new TraceInformation();
		return traceInformation;
	}

//	// ICommentable
//	
//	@Override
//	public List<Comment> getComments() {
//		return comments;
//	}
//
//	@Override
//	public void setComments(List<Comment> comments) {
//		this.comments = comments;
//	}

}
