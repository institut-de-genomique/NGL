package models.sra.submit.sra.instance;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import controllers.sra.api.AnalysisAPI;
import fr.cea.ig.DBObject;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import services.sra.SraValidationHelper;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

// Declaration d'une collection Analysis (herite de DBObject)
public class Analysis extends DBObject implements IValidation, IStateReference {

	// AnalysisType
	//public String alias;         // required mais remplacé par code herité de DBObject, et valeur = projectCode_num
	public List <String> projectCodes = new ArrayList<>();    // required pour nos stats  	
	public String title       = null;	     // required et champs de la proc */
	public String description = null;	     // required et champs de la proc */

	
	public String accession   = null;        // numeros d'accession attribué par ebi */
	public String sampleCode  = null;        // champs qui peut etre null, si rappatriement à partir de l'EBI
									         // ou si utilisation d'un sampleAC soumis par les collaborateurs.
	public String studyCode   = null;        // champs qui peut etre null, si rappatriement à partir de l'EBI
	                                         // ou si utilisation d'un sampleAC soumis par les collaborateurs.
	
	public String sampleIdentifier = null;
	public String studyIdentifier = null;
	
	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
							 // pour gerer les differents etats de l'objet.
	
	public String adminComment; // commentaire privé "reprise historique"				
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
		// pour loguer les dernieres modifications utilisateurs
 
	public Date firstSubmissionDate;
	public List<RawData> listRawData; 
    public List<Comment>comments = new ArrayList<Comment>();// commentaires des utilisateurs.
    public boolean consolidatedPublicStatus; // mis à true si releaseDate > date du jour. Rempli par crons.
    public Date releaseDate;
	// NGL-4593
    public String dropBox;
    
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
	

	// Verifie validite de l'analysis :
	public void validateInvariants(ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
			contextValidation = contextValidation.appendPath(code);		
		}
		// Verifier que status est bien rensigne, et si != new alors libraryName renseigné :
		//System.out.println("Dans exp.validate, stateCode =" +state.code);
		SraValidationHelper.validateFreeText(contextValidation,"title", this.title);
		SraValidationHelper.validateFreeText(contextValidation,"description", this.description);
		// verifier que projectCodes est bien renseigné et existe dans lims:
		CommonValidationHelper.validateProjectCodes (contextValidation, this.projectCodes);
		ValidationHelper.validateNotEmpty(contextValidation, this.title , "title");

		// L'un des champs sampleAccession ou sampleCode doit etre defini :
		if (StringUtils.isBlank(this.sampleIdentifier)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.sampleCode , "sampleCode");
		} 
		if (StringUtils.isBlank(this.sampleCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.sampleIdentifier , "sampleAccession");
			
		}
		// le champs sampleAccession est contraint
		if (StringUtils.isNotBlank(this.sampleIdentifier)) {
			if( ! (sampleIdentifier.startsWith("ERS") || sampleIdentifier.startsWith("SRS") || sampleIdentifier.startsWith("SAM")) ) {
				contextValidation.addError("sampleAccession",  " avec valeur '" + this.sampleIdentifier + "' qui ne commence pas par ERS ou SRS ou ou SAM");
			}
		}
		// L'un des champs studyAccession ou studyCode doit etre defini :
		if (StringUtils.isBlank(this.studyIdentifier)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.studyCode , "studyCode");
		}
		if (StringUtils.isBlank(this.studyCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, this.studyIdentifier , "studyIdentifier");
		}	
		// Le champs studyAccession est contraint
		if (StringUtils.isNotBlank(this.studyIdentifier)) {
			if( ! (studyIdentifier.startsWith("ERP") || studyIdentifier.startsWith("PRJ")) ) {
				contextValidation.addError("studyIdentifier", " avec valeur '" + this.studyIdentifier + "' qui ne commence pas par ERP ou PRJ ");
			}
		}

		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_ANALYSIS_COLL_NAME);
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}
		

	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("analysis");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);	
		AnalysisAPI analysisAPI = AnalysisAPI.get();
		if(analysisAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("analysis");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		AnalysisAPI analysisAPI = AnalysisAPI.get();
		if(! analysisAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("analysis");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		AnalysisAPI analysisAPI = AnalysisAPI.get();
		if(! analysisAPI.dao_checkObjectExist("code", code)) {
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
