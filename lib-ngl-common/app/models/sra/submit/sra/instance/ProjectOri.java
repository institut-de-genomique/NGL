package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

public class ProjectOri extends DBObject implements IValidation, IStateReference {

	public enum submissionProjectType {
		SEQUENCING_PROJECT,
		UMBRELLA_PROJECT
	}
	
	private static final play.Logger.ALogger logger = play.Logger.of(ProjectOri.class);
	
	//public String alias;             // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num

	public String title = "";	                // required next soon      
	public String description = "";
	public String accession;  
	public String externalId;
	public String name;                    // projectCode
	public String submissionProjectType = "";   // required et constraint 
	public Date   firstSubmissionDate;   // date de premiere soumission du project.
  	public String centerName = VariableSRA.centerName;        // required pour nos stats valeur fixee à GSC */
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
									  // pour gerer les differents etats de l'objet.
	public List<String> locusTagPrefixs;     // champs pouvant etre rempli lors d'une mise à jour uniquement
 	public List <String> projectCodes = new ArrayList<>();    

	public List<String> childrenProjectAccessions; 
	// ne pas mettre public List<String> childrenProjectAccessions = new ArrayList<>();  
	// car sinon si recuperation depuis .js, avec  jsRoutes.controllers.sra.projects.api.Projects.list() 
	// on a un project avec childrenProjectAccessions avec tableau vide meme si dans base, project sans childrenProjectAccessions.
	
	public void validateInvariants(ContextValidation contextValidation) {
		logger.debug("!!!!!!!!!!!! Dans Project.validateInvariants");
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
			contextValidation = contextValidation.appendPath(code);
		}
//		logger.debug("project.centerName='" + this.centerName +"'" );
//		logger.debug("project.code='" + this.code +"'" );
//		logger.debug("project.title='" + this.title +"'" );
//		logger.debug("project.description='" + this.description +"'" );
//		logger.debug("project.submissionProjectType='" + this.submissionProjectType +"'" );
//		logger.debug("project.childrenProjectAccessions='" + this.childrenProjectAccessions +"'" );
		
		SraValidationHelper   .requiredAndConstraint(contextValidation, centerName,	VariableSRA.mapCenterName(), "centerName");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_PROJECT_COLL_NAME);
		SraValidationHelper   .validateFreeText     (contextValidation, "title",         title);
		SraValidationHelper   .validateFreeText     (contextValidation, "description",   description);
		SraValidationHelper   .requiredAndConstraint(contextValidation, submissionProjectType, VariableSRA.mapSubmissionProjectType(), "submissionProjectType");
		if(StringUtils.isNotBlank(submissionProjectType) && submissionProjectType.equalsIgnoreCase("SEQUENCING_PROJECT")) {
			ValidationHelper      .validateNotEmpty     (contextValidation, name , "name");
		}		
		if(this.childrenProjectAccessions != null) {
			for(String child : this.childrenProjectAccessions) {
				//logger.debug("XXXXXXXXXXXXXXXXX   children='" + child + "'");
				if ( StringUtils.isBlank(child) ) {
					contextValidation.addError("childrenProjectAccessions", " avec donnee sans caractere visible");
				}
				if (StringUtils.isNotBlank(child)) {
					if (! child.startsWith("PRJEB") ) {
						contextValidation.addError("childrenProjectAccessions", "'" + child + "' ne commence pas par PRJEB");
					}
					if ( ! child.matches("[a-zA-Z0-9]+")) {
						contextValidation.addError("locusTagPrefixs", "'"+ child + "' contient un caratere non attendu (autre que [a-zA-Z0-9])");
					}
				}
			}
		}
	}
	
	
	private void validateCreation(ContextValidation contextValidation) {
		
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX          Entree dans validationCreation\n");
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		if ( projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " existe deja dans la base de donnees et MODE CREATION");
		}	
		
//		Project project = MongoDBDAO.findByCode(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, code);
//		if (project != null) {
//			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
//		}	
//		if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, code)) {
//			contextValidation.addError("code", code + " existe deja dans la base de donnees et MODE CREATION");
//		}
		
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Sortie de validationCreation\n");

		
	}


	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		if (! projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " n'existe pas dans la base de donnees et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		//if (! projectAPI.dao_checkObjectExist("code", code) ) {
		if (! projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " n'existe pas dans la base de donnees et MODE UPDATE");
		}		
	}


	@Override
	public void validate(ContextValidation contextValidation) {
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Entree dans validate\n");

		switch (contextValidation.getMode()) {
		case CREATION:
			logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Appel de validateCreation\n");

			validateCreation(contextValidation);
			break;
		case UPDATE:
			logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Appel de validateUpdate\n");

			validateUpdate(contextValidation);
			break;
		case DELETE:
			validateDelete(contextValidation);
			break;
		default: // autre cas undefined notamment
			contextValidation.addError("ERROR", "contextValidation.getMode() != de CREATION, UPDATE ou DELETE (undefined ?)");
			break;	
		} 
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Appel de validateInvariants\n");
		validateInvariants(contextValidation);
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Sortie de validate\n");
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
