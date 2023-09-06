package models.sra.submit.sra.instance;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

// Declaration d'une collection Experiment (herite de DBObject)
public class Experiment extends DBObject implements IValidation, IStateReference {
	// ExperimentType
	//public String alias;         // required mais remplacé par code herité de DBObject, et valeur = projectCode_num
	public String projectCode = null;     // required pour nos stats
	public String title = null;	       // required et champs de la proc */
	public String librarySelection = null;// required et constraint: champs de la proc surchargeable dans conf ou par utilisateurs */
	public String libraryStrategy = null; // required et constraint. Valeur mise par defaut à "WGS" mais surchargeable */
	public String librarySource = null;   // required et constraint. Valeur mise par defaut à "GENOMIC" mais surchargeable */
	public String libraryLayout = null;   // required et constraint. Valeur du champs de la proc à SINGLE ou PAIRED */
	public Integer libraryLayoutNominalLength = null;// required : champs de la proc. bigInteger dans objetSRA mettre à 0 si null ou XX */
	public String libraryLayoutOrientation = null;	 // champs de la proc renseigné à "forward" ou ssi paired "forward-reverse"  ou "reverse-forward"*/
	public String libraryName = null;               // required 
	public String libraryConstructionProtocol = null; // fixé à "none provided" par defaut, mais forme libre
	public String typePlatform = null;      // required et contrainte, L454 ou illumina en fonction de plateform_map de la proc
	public String instrumentModel = null;   // required et contrainte et depend de plateformType.
	// Actuellement forcement Illumina puisque collection Illumina
    public Integer lastBaseCoord = null; // valeur renseignee ssi illumina paired */
	public Long spotLength = null;    // required : champs spot_length de la proc et si 0 ou null ou XX mettre 0 en bigInteger */
	public String accession = null;         // numeros d'accession attribué par ebi */
 	//SPOTDECODESPEC.READSPEC a remplir en fonction de typePlatform, libraryLayout, libraryLayoutOrientation
 	// voir dans create_ExperimentType
	//Projects ref
	public String sampleCode = null;   // champs qui peut etre null, si rappatriement à partir de l'EBI
									   // ou si utilisation d'un sampleAC soumis par les collaborateurs.
	public String studyCode = null;    // champs qui peut etre null, si rappatriement à partir de l'EBI
	                                   // ou si utilisation d'un sampleAC soumis par les collaborateurs.
	public String sampleAccession = null;  // champs qui doit etre renseigné si state > SUB-F
	public String studyAccession = null;
	public String readSetCode = null;
	public List<ReadSpec> readSpecs = new ArrayList<>();
	public Run run = null; // le run est rattache à l'experiment
	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
							 // pour gerer les differents etats de l'objet.
	
	public String adminComment; // commentaire privé "reprise historique"				
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
		// pour loguer les dernieres modifications utilisateurs
	
	// champs releaseDate uniquement sur le study
	@Deprecated
	public Date releaseDate; 
	
	public Date firstSubmissionDate; 
    public List<Comment>comments = new ArrayList<Comment>();// commentaires des utilisateurs.


	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	
	@Override
	public TraceInformation getTraceInformation() {
		return traceInformation;
	}
	

	// Verifie validite de l'experiment sans verifier validite du run associé :
	public void validateInvariantsNoRun(ContextValidation contextValidation) {
		boolean copy = false;
		if (contextValidation.getObject("copy")!= null && (boolean) contextValidation.getObject("copy")) {
			copy = true;
			return;
		}

		//Logger.debug("Dans validateInvariantsNoRun");
		// nouvelle notation de Jean. Ne marche pas si put objets dans contextValidation :
		contextValidation = contextValidation.appendPath("experiment");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
			contextValidation = contextValidation.appendPath(code);		
		}
		// Verifier que status est bien rensigne, et si != new alors libraryName renseigné :
		//System.out.println("Dans exp.validate, stateCode =" +state.code);
		SraValidationHelper.validateFreeText(contextValidation,"title", this.title);
		//CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, this.state);
		ValidationHelper.validateNotEmpty(contextValidation, this.libraryName , "libraryName");
		// Verifer que projectCode est bien renseigné et qu'il existe bien dans lims :
		CommonValidationHelper.validateProjectCodeRequired(contextValidation, this.projectCode);
		ValidationHelper.validateNotEmpty(contextValidation, this.title , "title");
        // Verifer que librarySelection libraryStrategy librarySource et libraryLayout sont bien renseignés avec bonne valeur :		
		SraValidationHelper.requiredAndConstraint(contextValidation, this.librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		SraValidationHelper.requiredAndConstraint(contextValidation, this.libraryStrategy, VariableSRA.mapLibraryStrategy(), "libraryStrategy");
		SraValidationHelper.requiredAndConstraint(contextValidation, this.librarySource, VariableSRA.mapLibrarySource(), "librarySource");
		SraValidationHelper.requiredAndConstraint(contextValidation, this.libraryLayout, VariableSRA.mapLibraryLayout(), "libraryLayout"); // single ou paired
		//ValidationHelper.required(contextValidation, this.libraryName , "libraryName");
		//ValidationHelper.required(contextValidation, this.libraryConstructionProtocol , "libraryConstructionProtocol");
		SraValidationHelper.requiredAndConstraint(contextValidation, this.typePlatform, VariableSRA.mapTypePlatform(), "typePlatform");
		if(copy) {
			SraValidationHelper.requiredAndConstraint(contextValidation, this.instrumentModel, VariableSRA.mapAllInstrumentModel(), "allInstrumentModel");
		} else {
			SraValidationHelper.requiredAndConstraint(contextValidation, this.instrumentModel, VariableSRA.mapInstrumentModel(), "instrumentModel");
		}
		// L'un des champs sampleAccession ou sampleCode doit etre defini :
		if (StringUtils.isBlank(this.sampleAccession)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.sampleCode , "sampleCode");
		} 
		if (StringUtils.isBlank(this.sampleCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.sampleAccession , "sampleAccession");
			
		}
		// le champs sampleAccession est contraint
		if (StringUtils.isNotBlank(this.sampleAccession)) {
			if( ! (sampleAccession.startsWith("ERS") || sampleAccession.startsWith("SAM")) ) {
				contextValidation.addError("sampleAccession",  " avec valeur '" + this.sampleAccession + "' qui ne commence pas par ERS ou SAM");
			}
		}
		// L'un des champs studyAccession ou studyCode doit etre defini :
		if (StringUtils.isBlank(this.studyAccession)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.studyCode , "studyCode");
		}
		if (StringUtils.isBlank(this.studyCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.studyAccession , "studyAccession");
		}	
		// Le champs studyAccession est contraint
		if (StringUtils.isNotBlank(this.studyAccession)) {
			if( ! (studyAccession.startsWith("ERP") || studyAccession.startsWith("PRJ")) ) {
				contextValidation.addError("studyAccession", " avec valeur '" + this.studyAccession + "' qui ne commence pas par ERP ou PRJ ");
			}
		}

		ValidationHelper.validateNotEmpty(contextValidation, this.readSetCode , "readSetCode");
		// Dans le cas des illumina ou LS454
		if (! "OXFORD_NANOPORE".equalsIgnoreCase(this.typePlatform)) {
			if ("illumina".equalsIgnoreCase(this.typePlatform)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.spotLength , "spotLength");
			}
			if (StringUtils.isNotBlank(this.libraryLayout) && libraryLayout.equalsIgnoreCase("paired")){
				// Verifer que lastBaseCoord est bien renseigné ssi Illumina et paired:
				ValidationHelper.validateNotEmpty(contextValidation, this.lastBaseCoord , "lastBaseCoord");	
				// controle sur libraryLayoutNominalLength retiré car champs supprimé à l'EBI voir ticket NGL-4084
				//ValidationHelper.validateNotEmpty(contextValidation, this.libraryLayoutNominalLength , "libraryLayoutNominalLength");
				SraValidationHelper.requiredAndConstraint(contextValidation, this.libraryLayoutOrientation, VariableSRA.mapLibraryLayoutOrientation(), "libraryLayoutOrientation");
			}
		}
		// Si nanopore pas de readspec et pas spotLength, lastBasecoord, et libraryLayoutNominalLength
		// Verifier les readSpec :
		SraValidationHelper   .validateReadSpecs(contextValidation, this);
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_EXPERIMENT_COLL_NAME);
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}
	
	
	public void validateInvariants(ContextValidation contextValidation) {
		validateInvariantsNoRun(contextValidation);
		contextValidation = contextValidation.appendPath("experiment");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		if (! "OXFORD_NANOPORE".equalsIgnoreCase(typePlatform)) { 
			//pas mis dans validateLight car il existe dans reprise historique des illumina et LS454 sans lastBaseCoord 
			ValidationHelper.validateNotEmpty(contextValidation, lastBaseCoord , "lastBaseCoord");	
		}
		if (ValidationHelper.validateNotEmpty(contextValidation, run, "run")
				&& ValidationHelper.validateNotEmpty(contextValidation, run.code, "run"))
			run.validate(contextValidation);
		
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("experiment");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);	
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(experimentAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("experiment");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(! experimentAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("experiment");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(! experimentAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE UPDATE");
		}	
	}

	@Override
	public void validate(ContextValidation contextValidation) {
		switch (contextValidation.getMode()) {
		case CREATION:
			validateCreation(contextValidation);
			break;
		case UPDATE:
			validateUpdate(contextValidation);
			break;
		case DELETE:
			validateDelete(contextValidation);
			break;
		default: // autre cas undefined notamment
			contextValidation.addError("ERROR", "contextValidation.getMode() != de CREATION, UPDATE ou DELETE (undefined ?)");
			break;	
		} 
		validateInvariants(contextValidation);
	}
	
	public void validateNoRun(ContextValidation contextValidation) {
		switch (contextValidation.getMode()) {
		case CREATION:
			validateCreation(contextValidation);
			break;
		case UPDATE:
			validateUpdate(contextValidation);
			break;
		case DELETE:
			validateDelete(contextValidation);
			break;
		default: // autre cas undefined notamment
			contextValidation.addError("ERROR", "contextValidation.getMode() != de CREATION, UPDATE ou DELETE (undefined ?)");
			break;	
		} 
		validateInvariantsNoRun(contextValidation);
	}

}
