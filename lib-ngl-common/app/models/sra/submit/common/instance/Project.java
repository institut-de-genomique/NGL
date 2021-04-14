package models.sra.submit.common.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class Project extends DBObject implements IValidation, IStateReference {
	private static final play.Logger.ALogger logger = play.Logger.of(Project.class);
	
	//public String alias;             // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num

	public String title = "";	                // required next soon      
	public String description = "";
	public String accession;  
	public String externalId;
	public String name = "";                    // projectCode
	public String submissionProjectType = "";   // required et constraint 
	public Date   firstSubmissionDate;   // date de premiere soumission du project.
  	public String centerName = VariableSRA.centerName;        // required pour nos stats valeur fixee à GSC */
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
									  // pour gerer les differents etats de l'objet.
	public List<String> locusTagPrefixs;     // champs pouvant etre rempli lors d'une mise à jour uniquement
 	public List <String> projectCodes = new ArrayList<>();    

	
	
	public void validateInvariants(ContextValidation contextValidation) {
		logger.debug("!!!!!!!!!!!! Dans Project.validateInvariants");
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);

		// Attention on peut vouloir regrouper dans un project_code virtuel ?? 
		ValidationHelper      .validateNotEmpty     (contextValidation, name , "name");
		SraValidationHelper   .requiredAndConstraint(contextValidation, centerName,	VariableSRA.mapCenterName(), "centerName");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_PROJECT_COLL_NAME);
		SraValidationHelper   .validateFreeText     (contextValidation, "title",         title);
		SraValidationHelper   .validateFreeText     (contextValidation, "description",   description);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_PROJECT_COLL_NAME);
		SraValidationHelper   .requiredAndConstraint(contextValidation, submissionProjectType, VariableSRA.mapSubmissionProjectType(), "submissionProjectType");
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		if(projectAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		if(! projectAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		if(! projectAPI.dao_checkObjectExist("code", code)) {
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
}
