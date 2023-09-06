package models.sra.submit.sra.instance;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import models.laboratory.common.description.ObjectType;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class Sample extends AbstractSample {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Sample.class);
	
	// SampleType
	//public String alias;         // required mais remplacé par code herité de DBObject
	public String projectCode;     // required pour nos stats //Reference code de la collection project NGL  
	public int taxonId;            // required 
	public String commonName;     
	public String scientificName;  // required next soon 
	public String title;           // required next soon 
	public String anonymizedName;      // not required.
	public String description;  
 	@Deprecated
	public String clone;             // champs qui doit apparaitre dans datatable mais qui apparaitra dans le xml dans attributes  
	//public String refCollab;       // champs qui doit apparaitre dans datatable mais qui apparaitra dans le xml dans attributes  
	//public String accession;       // numeros d'accession attribué par ebi champs mis dans AbstractSample
 	//public String externalId = null;

	public Date firstSubmissionDate;        
	//public State state; //= new State();// Reference sur "models.laboratory.common.instance.state" mis dans lAbstractSample
	//public TraceInformation traceInformation = new TraceInformation(); champs mis dans AbstractSample

	// Representation sous forme de string de listSampleAttributes , pour affichage et edition dans datatable. facultatif
	public String attributes;
	
	public Sample() {
		super(AbstractSample.sampleType);
	}
	
	public void validateInvariants(ContextValidation contextValidation) {
		//logger.debug("Entree dans validateInvariants  wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
		
		contextValidation = contextValidation.appendPath("sample");
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		SraValidationHelper   .validateFreeText (contextValidation,"description", this.description);
		SraValidationHelper   .validateFreeText (contextValidation,"title", this.title);
		SraValidationHelper   .validateFreeText (contextValidation,"anonymizedName", this.anonymizedName);
		// NGL-4235 => Tant que les informations spacio-temporelles du sample ne sont pas dans NGL et ne sont pas recuperables par NGL-SUB
		// declencher une erreur uniquement à partir de la validation pour permettre aux utilisateurs de remplir ces champs à la validation.	
		//NGL-4235 : on veut laisser passer des samples invalides, sans attributes, qui pourront etre completes à la validation. 
		switch (contextValidation.getMode()) {
		case CREATION:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		case DELETE:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		case UPDATE:
			SraValidationHelper   .newValidateAttributesRequired (contextValidation,"attributes", this.attributes);
			break;
		case NOT_DEFINED:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		}
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		//SraValidationHelper .requiredAndConstraint(contextValidation, this.state.code , VariableSRA.mapStatus, "state.code");
		if(StringUtils.isNotBlank(this.state.code) && ! this.state.code.equals(NONE)) {
			CommonValidationHelper.validateStateRequired      (contextValidation, ObjectType.CODE.SRASubmission, this.state);
		}
		CommonValidationHelper.validateProjectCodeRequired(contextValidation, projectCode);
		CommonValidationHelper.validateCodePrimary        (contextValidation, this, InstanceConstants.SRA_SAMPLE_COLL_NAME);
	}
	

	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("sample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		SampleAPI sampleAPI = SampleAPI.get();
		if(sampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}
		boolean copy = false;
		if (contextValidation.getObject("copy")!= null && (boolean) contextValidation.getObject("copy")) {
			copy = true;
		}
// On peut avoir un import de study soumis hors NGL-SUB		
//		if(! copy) {
//			if(  ! ( StringUtils.isNotBlank(this.state.code) && (this.state.code.equals(SUB_N) || this.state.code.equals(NONE))) )  { 
//				contextValidation.addError("state.code", "valeur '" + state.code + " qui devrait etre a SUB-N ou NONE");
//			}
//		}
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("sample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		SampleAPI sampleAPI = SampleAPI.get();
		if(! sampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("sample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		AbstractSampleAPI abstSampleAPI = AbstractSampleAPI.get();
		if(! abstSampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE UPDATE");
		}	
		AbstractSample abstSample = abstSampleAPI.dao_getObject(code);
		if(abstSample instanceof ExternalSample) {
			contextValidation.addError("code", code + " est un ExternalSample et  MODE UPDATE");
		}
	}


	
	@Override
	public void validate(ContextValidation contextValidation) {
		String mess = "sample ";
		if (StringUtils.isNotBlank(code)) {
			mess += "." + code;
		}
		contextValidation.addKeyToRootKeyName(mess);
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
		contextValidation.removeKeyFromRootKeyName(mess);
	}
}
