package models.sra.submit.common.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.laboratory.common.description.ObjectType;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class Study extends AbstractStudy {
	private static final play.Logger.ALogger logger = play.Logger.of(Study.class);

	// StudyType
	//public String alias;             // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num
	public String title = "";	       // required next soon      
	public String studyAbstract = "";  // required next soon 
	public String description = "";    
	public int bioProjectId;           // doit etre mis à 0 si absent.
	public String existingStudyType;   // required et constraint 
	public Date firstSubmissionDate;   // date de premiere soumission du study.
// 	public String accession = null;      // numeros d'accession attribué par ebi */
 	public String externalId = null;
  	public Date releaseDate;             // required, date de mise à disposition en public par l'EBI
  	public String centerName = VariableSRA.centerName;        // required pour nos stats valeur fixee à GSC */
    public String centerProjectName;      // required pour nos stats valeur fixée à projectCode
 	public List <String> projectCodes = new ArrayList<>();    // required pour nos stats  
    // et heritage de state et traceInformation.
	public static final String initialStateCode = "NONE";
	public String adminComment;

	
	public void validateInvariants(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("study");

		//		contextValidation.addKeyToRootKeyName("study");
		logger.debug("dans validate");

		// verifier que projectCodes est bien renseigné et existe dans lims:
		CommonValidationHelper.validateProjectCodes (contextValidation, projectCodes);
		// Attention on peut vouloir regrouper dans un project_code virtuel ?? 
		ValidationHelper      .validateNotEmpty     (contextValidation, centerProjectName , "centerProjectName");
		SraValidationHelper   .requiredAndConstraint(contextValidation, centerName,	VariableSRA.mapCenterName(), "centerName");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_STUDY_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		if (state != null && StringUtils.isNotBlank(state.code) && !initialStateCode.equals(state.code)) {
			CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, state);
		}
		SraValidationHelper   .validateFreeText     (contextValidation, "title",         title);
		SraValidationHelper   .validateFreeText     (contextValidation, "studyAbstract", studyAbstract);
		SraValidationHelper   .validateFreeText     (contextValidation, "description",   description);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_STUDY_COLL_NAME);
		SraValidationHelper   .requiredAndConstraint(contextValidation, existingStudyType, VariableSRA.mapExistingStudyType(), "existingStudyType");
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("study");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		StudyAPI studyAPI = StudyAPI.get();
		if(studyAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("study");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		StudyAPI studyAPI = StudyAPI.get();
		if(! studyAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("study");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		StudyAPI studyAPI = StudyAPI.get();
		if(! studyAPI.dao_checkObjectExist("code", code)) {
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
