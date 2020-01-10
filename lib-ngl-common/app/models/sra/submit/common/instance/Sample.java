package models.sra.submit.common.instance;

import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import models.laboratory.common.description.ObjectType;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.property.PropertySingleValue;
import models.utils.InstanceConstants;
//import play.Logger;
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
	public String clone;             // champs qui doit apparaitre dans datatable mais qui apparaitra dans le xml dans attributes         
	//public String accession;       // numeros d'accession attribué par ebi champs mis dans AbstractSample
 	public String externalId = null;
	public Date releaseDate;         // required, date de mise à disposition en public par l'EBI
	//public State state; //= new State();// Reference sur "models.laboratory.common.instance.state" mis dans lAbstractSample
	//public TraceInformation traceInformation = new TraceInformation(); champs mis dans AbstractSample
	public Date firstSubmissionDate;        

	// Representation sous forme de string de listSampleAttributes , pour affichage et edition dans datatable. facultatif
	public String attributes = "";
	
	public Sample() {
		super(AbstractSample.sampleType);
	}
	
	public void validateInvariants(ContextValidation contextValidation) {
		//Logger.debug("ok dans Sample.validate\n");
		contextValidation = contextValidation.appendPath("sample");
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		SraValidationHelper   .validateFreeText (contextValidation,"description", this.description);
		SraValidationHelper   .validateFreeText (contextValidation,"title", this.title);
		SraValidationHelper   .validateFreeText (contextValidation,"anonymizedName", this.anonymizedName);
		SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		//SraValidationHelper .requiredAndConstraint(contextValidation, this.state.code , VariableSRA.mapStatus, "state.code");
		CommonValidationHelper.validateStateRequired      (contextValidation, ObjectType.CODE.SRASubmission, this.state);
		CommonValidationHelper.validateProjectCodeRequired(contextValidation, projectCode);
		CommonValidationHelper.validateCodePrimary        (contextValidation, this, InstanceConstants.SRA_SAMPLE_COLL_NAME);
		logger.debug("sortie de sample.validate pour {}", this.code);
	}
	

	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("sample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		SampleAPI sampleAPI = SampleAPI.get();
		if(sampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
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
		SampleAPI sampleAPI = SampleAPI.get();
		if(! sampleAPI.dao_checkObjectExist("code", code)) {
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
}
