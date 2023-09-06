package models.laboratory.sample.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.samples.SamplesDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.laboratory.sample.instance.reporting.SampleProcessesStatistics;
import models.laboratory.sample.instance.tree.SampleLife;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sample.instance.SampleValidationHelper;
import validation.utils.ValidationHelper;

// Link to this : {@link models.laboratory.sample.instance.Sample}

/**
 * Sample information as required by the laboratory (the L in LIMS).
 * 
 * Sample collection name is defined as {@link models.utils.InstanceConstants#SAMPLE_COLL_NAME}.
 * 
 * @author mhaquell
 * 
 */
public class Sample extends DBObject implements IValidation, ITracingAccess {

	public static final Supplier<SamplesDAO> find = new GuiceSupplier<>(SamplesDAO.class);
	
	// @JsonIgnore
	public final static String HEADER = 
			"Sample.code;Sample.projectCodes;Sample.name;Sample.referenceCollab;Sample.taxonCode;Sample.comments";

	// ngl-data/services.description.sample.SampleServiceCNS
	
	/*
	 * Type code, defined in ngl-data project {@link services.description.sample.SampleServiceCNS}.
	 * More probably from : {@link models.laboratory.sample.description.dao.SampleTypeDAO}.
	 * In the end : {@link models.laboratory.sample.description.SampleType}.
	 * To be understood as something like: CodeReference<SampleType> type;
	 * Possibly use some @JsonValue that would allow type annotation.
	 */
//	@NGLConstraints.Foreign(models.laboratory.sample.description.SampleType.class)
//	@NGLConstraints.Required
	public String typeCode;
	
	/*
	 * Sample type category code, implied by the type definition and defined 
	 * in ngl-data project {@link services.description.sample.SampleServiceCNS}.
	 * More probably from: {@link models.laboratory.sample.description.dao.SampleCategoryDAO}.
	 * In the end: {@link models.laboratory.sample.description.SampleCategory}.
	 * Would be {@code type.category}.
	 */
//	@NGLConstraints.Foreign(models.laboratory.sample.description.SampleCategory.class)
//	@NGLConstraints.Required
	public String categoryCode;
	
	/*
	 * Import source type (import file type or so).
	 * {@link models.laboratory.sample.description.ImportType} linked to some 
	 * {@link models.laboratory.sample.description.ImportCategory}.
	 */
//	@NGLConstraints.Foreign(models.laboratory.sample.description.ImportType.class)
//	@NGLConstraints.Required	
	public String importTypeCode;

	/**
	 * Set of projects code this sample is used in.
	 */
	public Set<String> projectCodes;

	/**
	 * Name = localized code (default:null)
	 */
//	@NGLConstraints.Required	
	public String name;
	
	// ?? Wath is difference with code / referenceCollbab => code s'est interne au genoscope
	/**
	 * Name of the sample in the collab referential
	 */
	public String referenceCollab;
	
	/**
	 * Mandatory : meta : false (meta:metagenomic,metatrucs)
	 */
	public Map<String,PropertyValue> properties;
	
	/**
	 * Unused (not yet ?).
	 */
	public Valuation valuation;
	//public List<CollaboratorInvolve> collaborators;
	
	// Expanded taxonomy information retrieved from the NCBI.
	// See https://www.ncbi.nlm.nih.gov/taxonomy.
	
	/**
	 * Taxonomy code (@see <a href="https://www.ncbi.nlm.nih.gov/taxonomy">taxonomy</a>).
	 */
	public String taxonCode;
	
	/**
	 * Scientific name (@see <a href="https://www.ncbi.nlm.nih.gov/taxonomy">taxonomy</a>).
	 * Auto filled (at least on prod).
	 */
	public String ncbiScientificName;
	
	/**
	 * Lineage (@see <a href="https://www.ncbi.nlm.nih.gov/taxonomy">taxonomy</a>).
	 * Auto filled (at least on prod).
	 */
	public String ncbiLineage;
	
	/**
	 * Comments.
	 */
	public List<Comment> comments;

	/**
	 * Commentaires techniques.
	 */
	public List<Comment> technicalComments;
	
	/**
	 * System maintained access information.
	 */
	public TraceInformation traceInformation;

	/*
	 * Sample parent if any.
	 * 
	 * life: {
from: {
projectCode: "BUP",
sampleCode: "BUP_AAAA",
sampleTypeCode: "DNA",
experimentCode: "TAG-PCR-20160819_130125AHH",
experimentTypeCode: "tag-pcr",
containerCode: "18ID3I6DL",
supportCode: "18ID3I6DL",
fromTransformationTypeCodes: null,
fromTransformationCodes: null,
processTypeCodes: [
"tag-pcr-and-dna-library"
],
processCodes: [
"BUP_AAAA_TAG-PCR-AND-DNA-LIBRARY_18IF26TQZ"
],
},
path: ",CO-0000140,BUP_AAAA",
},
	 */
	public SampleLife life;
	
	/**
	 * List of projections of process that use this sample.
	 * Populated when experiments/processes are done.
	 */
	public List<SampleProcess> processes;
	
	/**
	 * Summary (count read sets and other stuff).
	 */
	public SampleProcessesStatistics processesStatistics;
	
	/**
	 * Time of the last automated update.
	 */
	public Date processesUpdatedDate;

	public Date sampleProcessesFinishUpdateDate;

	private Date processesToLaunchDate;
	
	/**
	 * Constructs a new Sample.
	 */
	public Sample() {
		traceInformation = new TraceInformation();
		comments         = new ArrayList<>(0);
	}

	// This could possibly be better implemented as some external process that would
	// be the validator itself and not a helper.
	/**
	 * Validate this sample.
	 */
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
    	CommonValidationHelper.validateIdPrimary                 (contextValidation, this);
    	CommonValidationHelper.validateCodePrimary               (contextValidation, this, InstanceConstants.SAMPLE_COLL_NAME);
		ValidationHelper      .validateNotEmpty                  (contextValidation, name, "name");
		ValidationHelper	  .validateNotEmpty					 (contextValidation, referenceCollab, "referenceCollab");
		//NGL-4046 : ajout validation taxonCode
		SampleValidationHelper.validateTaxonCode                 (contextValidation, taxonCode);
		SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, categoryCode);
		CommonValidationHelper.validateProjectCodes              (contextValidation, projectCodes);
		SampleValidationHelper.validateSampleType                (typeCode,importTypeCode,properties,contextValidation);
		CommonValidationHelper.validateTraceInformationRequired  (contextValidation, traceInformation);
		CommonValidationHelper.validateRulesWithObjects          (contextValidation, this);
		// GA: validation taxon	
		// NGL-4099 :
		SampleValidationHelper.validateExternalReadsetsImport    (importTypeCode, categoryCode , properties, contextValidation);
	}

	// ---- Interfaces implementations
	
	// We cannot @JsonIgnore setters or getters otherwise the corresponding
	// field serialization is disabled.
	
	// IAccessTracking
	
	//@JsonIgnore
	@Override
	public TraceInformation getTraceInformation() {
		if (traceInformation == null)
			traceInformation = new TraceInformation();
		return traceInformation;
	}

	public Date getProcessesToLaunchDate() {
		return this.processesToLaunchDate;
	}

	public void setProcessesToLaunchDate(Date processesToLaunchDate) {
		if (!this.projectCodes.contains("CEA")) {
			this.processesToLaunchDate = processesToLaunchDate;
		}
	}

}
