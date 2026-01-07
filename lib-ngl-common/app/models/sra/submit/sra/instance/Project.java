package models.sra.submit.sra.instance;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.sra.api.ProjectAPI;
import fr.cea.ig.DBObject;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import services.sra.SraValidationHelper;
import services.sra.VariableSRA;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

// Cette classe sert uniquement à stoquer les project umbrella.
// Les informations de project de type sequencing sont maintenant stoquees dans l'objet Study

public class Project extends DBObject implements IValidation, IStateReference {

	public enum submissionProjectType {
		//SEQUENCING_PROJECT,
		UMBRELLA_PROJECT
	}
	
	private static final play.Logger.ALogger logger = play.Logger.of(Project.class);
	
//  public String alias;                    // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num
	public String title       = "";	        // required next soon      
	public String description = "";
  	public String centerName  = VariableSRA.centerName; // required pour nos stats valeur fixee à GSC */
	public String accession; 
	//@Deprecated
	//public String externalId;
	public String name;                         // pas possible aujourd'hui de mettre un nom au project umbrella alors que devrait etre permis par le model de l'EBI
	public String submissionProjectType = "";   // required et constraint 
	public Date   firstSubmissionDate;          // date de premiere soumission du project.
	public TraceInformation traceInformation = new TraceInformation(); // Reference sur "models.laboratory.common.instance.TraceInformation" 
	public State state = new State();           // Reference sur "models.laboratory.common.instance.state" 
									            // pour gerer les differents etats de l'objet.
	// NGL-4013 : ajout parent umbrella
	public List<String> parentProjectAccessions;  // numéros d'accession de la forme PRJEB du project parent (pour les project umbrella avec parent)
	//@Deprecated
	//public List<String> locusTagPrefixs;      // champs transferé dans study
//	@Deprecated
// 	public List<String> projectCodes;  // Pas de notions de projectCodes pour les projects umbrella  
	public List<String> childrenProjectAccessions; 
	// ne pas mettre public List<String> childrenProjectAccessions = new ArrayList<>();  
	// car sinon si recuperation depuis .js, avec  jsRoutes.controllers.sra.projects.api.Projects.list() 
	// on a un project avec childrenProjectAccessions avec tableau vide meme si dans base, project sans childrenProjectAccessions.
	public String adminComment; // commentaire privé "reprise historique"				
    public List<Comment>comments = new ArrayList<Comment>();// commentaires des utilisateurs.

	public List<String> idsPubmed;  // liste des ref bibliographiques pubmed.
    public String taxonId;          // dans le cas des project ERGA, les projects umbrella peuvent etre associés à un taxonId et a scientificName
    public String scientificName;   // dans le cas des project ERGA, les projects umbrella peuvent etre associés à un taxonId et a scientificName
 
    //NGL-4708 + NGL-4809
    //public List<Attribute> attributes;
    public String attributes;
    
    // NGL-4593
    public String dropBox;
    
    
    public void validateInvariants(ContextValidation contextValidation) {
		//logger.debug("!!!!!!!!!!!! Dans Project.validateInvariants");
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
		//logger.debug("project.attributes='" + this.attributes +"'" );
		
		SraValidationHelper   .requiredAndConstraint(contextValidation, centerName,	VariableSRA.mapCenterName(), "centerName");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_PROJECT_COLL_NAME);
		SraValidationHelper   .validateFreeText     (contextValidation, "title",         title);
		SraValidationHelper   .validateFreeText     (contextValidation, "description",   description);
		SraValidationHelper   .requiredAndConstraint(contextValidation, submissionProjectType, VariableSRA.mapSubmissionProjectType(), "submissionProjectType");
//      La collection project est utilise uniquement pour le stoquage des projects umbrella dans nouveau modele
//		if(StringUtils.isNotBlank(submissionProjectType) && submissionProjectType.equalsIgnoreCase("SEQUENCING_PROJECT")) {
//			ValidationHelper      .validateNotEmpty     (contextValidation, name , "name");
//		}	
		// NGL-3585 :  il faut autoriser project umbrella sans enfants
//		if (this.childrenProjectAccessions.isEmpty()) {
//			String code = "";
//			if (StringUtils.isNotBlank(this.code)) {
//				code = this.code;
//			}
//			contextValidation.addError("childrenProjectAccession pour  : " + code , "Tentative de creation d'un project UMBRELLA sans projects enfants"); 
//			//logger.debug("Tentative de creation d'un project UMBRELLA sans projects enfants");
//		}
		if(this.idsPubmed != null) {
			for(String idPubmed : this.idsPubmed) {
				//logger.debug("XXXXXXXXXXXXXXXXX   locusTag='" + locusTag + "'");
				if( StringUtils.isBlank(idPubmed) ) {
					contextValidation.addError("idsPubmed", " avec donnee sans caractere visible");
				}
				if (StringUtils.isNotBlank(idPubmed)) {
					if ( ! idPubmed.matches("([0-9])+")) {
						contextValidation.addError("idsPubmed", "'" +idPubmed + "' contient un caratere non attendu, (autre que [0-9])");
					}
				}
			}
			// verifier qu'il n'y pas de doublon dans la liste des references bibliographiques:
			Set<String> set = new HashSet<String>(this.idsPubmed);
			if(set.size() < this.idsPubmed.size()) {
				String liste_idsPubmed = "";
				for (String c: this.idsPubmed) {
					liste_idsPubmed += c + ", \n";
				}
				liste_idsPubmed = liste_idsPubmed.replaceFirst(",\\s*$", ""); // oter ',' terminale si besoin
				contextValidation.addError("idsPubmed", " Il existe des doublons dans la liste " + liste_idsPubmed);
			}
		}
		if(this.childrenProjectAccessions != null) {
			for(String child : this.childrenProjectAccessions) {
				//logger.debug("XXXXXXXXXXXXXXXXX   children='" + child + "'");
				if ( StringUtils.isBlank(child) ) {
					contextValidation.addError("childrenProjectAccessions", " avec donnee sans caractere visible");
				}
				if (StringUtils.isNotBlank(child)) {
					if (! (child.startsWith("PRJEB")||child.startsWith("PRJNA") )) {
						contextValidation.addError("childrenProjectAccessions", "'" + child + "' ne commence pas par PRJEB ou PRJNA");
					}
					if ( ! child.matches("[a-zA-Z0-9]+")) {
						contextValidation.addError("childrenProjectAccessions", "'"+ child + "' contient un caratere non attendu (autre que [a-zA-Z0-9])");
					}
				}
			}
			// verifier qu'il n'y pas de doublon dans la liste des enfants :
			Set<String> set = new HashSet<String>(this.childrenProjectAccessions);
			if(set.size() < this.childrenProjectAccessions.size()) {
				String liste_children = "";
				for (String c: this.childrenProjectAccessions) {
					liste_children += c + ", \n";
				}
				liste_children = liste_children.replaceFirst(",\\s*$", ""); // oter ',' terminale si besoin
				contextValidation.addError("childrenProjectAccessions", " Il existe des doublons dans la liste " + liste_children);
			}
		}
		// verifier qu'un project enfant n'est pas enfant de plusieurs projects ?

		// NGL-4013
		if(this.parentProjectAccessions != null) {
			for(String parent : this.parentProjectAccessions) {
				//logger.debug("XXXXXXXXXXXXXXXXX   children='" + child + "'");
				if ( StringUtils.isBlank(parent) ) {
					contextValidation.addError("parentProjectAccessions", " avec donnee sans caractere visible");
				}
				if (StringUtils.isNotBlank(parent)) {
					if (! (parent.startsWith("PRJEB")||parent.startsWith("PRJNA") )) {
						contextValidation.addError("parentProjectAccessions", "'" + parent + "' ne commence pas par PRJEB ou PRJNA");
					}
					if ( ! parent.matches("[a-zA-Z0-9]+")) {
						contextValidation.addError("parentProjectAccessions", "'"+ parent + "' contient un caratere non attendu (autre que [a-zA-Z0-9])");
					}
				}
			}
			// verifier qu'il n'y pas de doublon dans la liste des parents :
			Set<String> set = new HashSet<String>(this.parentProjectAccessions);
			if(set.size() < this.parentProjectAccessions.size()) {
				String liste_parent = "";
				for (String c: this.parentProjectAccessions) {
					liste_parent += c + ", \n";
				}
				liste_parent = liste_parent.replaceFirst(",\\s*$", ""); // oter ',' terminale si besoin
				contextValidation.addError("parentProjectAccessions", " Il existe des doublons dans la liste " + liste_parent);
			}
		}	
		//NGL-4708
//		if(this.attributes != null && this.attributes.isEmpty()) {
//			logger.debug("attributes != null");
//			SraValidationHelper   .validateProjectAttributes (contextValidation, "attributes", this._getChaineAttributes());
//		}
		SraValidationHelper   .validateProjectAttributes (contextValidation,"attributes", this.attributes);

	}
	
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		//if ( projectAPI.dao_checkObjectExist("code", code) ) {
		if ( projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " existe deja dans la base de donnees et MODE CREATION");
		}	
	}


	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();
		//if (! projectAPI.dao_checkObjectExist("code", code) ) {
		if ( ! projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " n'existe pas dans la base de donnees et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("project");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ProjectAPI projectAPI = ProjectAPI.get();		
		//if (! projectAPI.dao_checkObjectExist("code", code) ) {
		if ( ! projectAPI.isObjectExist(code) ) {
			contextValidation.addError("code", code + " n'existe pas dans la base de donnees et MODE UPDATE");
		}		
	}


	@Override
	public void validate(ContextValidation contextValidation) {
		//logger.debug("XXXXXXXXXXXXXXXXX Entree dans validate\n");
		
		switch (contextValidation.getMode()) {
		case CREATION:
			//logger.debug("XXXXXXXXXXXXX Appel de validateCreation\n");
			validateCreation(contextValidation);
			break;
		case UPDATE:
			//logger.debug("XXXXXXXXXXXXX Appel de validateUpdate\n");
			validateUpdate(contextValidation);
			break;
		case DELETE:
			validateDelete(contextValidation);
			break;
		default: // autre cas undefined notamment
			contextValidation.addError("ERROR", "contextValidation.getMode() != de CREATION, UPDATE ou DELETE (undefined ?)");
			break;	
		} 
		//logger.debug("XXXXXXXXXXXXXXX Appel de validateInvariants\n");
		validateInvariants(contextValidation);
		if(contextValidation.hasErrors()) {
			logger.debug("debut displayError");
			contextValidation.displayErrors(logger, "debug");
			logger.debug("fin displayError");
		}
		//logger.debug("XXXXXXXXXXXXXXX Sortie de validate\n");
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
	
//	public String _getChaineAttributes() {
//		//logger.debug("kkkkkkkkkkkkkkkkkkkkkkk                 passage dans getAttributes");
//		String stringAttributes="";
//		if(this.attributes != null) {
//			for (Attribute att: this.attributes) {
//				stringAttributes += att.toString();
//			}
//		}
//		//stringAttributes = "<PROJECT_ATTRIBUTE><TAG>Funding Acknowledgement</TAG><VALUE>This research has received funding from the European Union's Horizon 2020 Research and Innovation Programme under grant agreement No 862923 (project AtlantECO). This output reflects only the author's view and the European Union cannot be held responsible for any use that may be made of the information contained therein. (https://cordis.europa.eu/project/id/862923)</VALUE></PROJECT_ATTRIBUTE>\\n\\n<PROJECT_ATTRIBUTE><TAG>Data Policy</TAG><VALUE>The AtlantECO Consortium relies on the fair, open and inclusive collaboration among its Partners, and the exploitation of jointly owned results, which is governed by the adoption of AtlantECO's Data Sharing and Publication Best Practices (https://doi.org/10.5281/zenodo.7791092).</VALUE></PROJECT_ATTRIBUTE>";
//		return stringAttributes;
//	}

//	public void _setChaineAttributes(String value, ContextValidation ctxVal) {
//		//logger.debug("kkkkkkkkkkkkkkkkkkkkkkk                 passage dans setAttributes avec value='{}'", value);
//		//appel suivant ne marche pas
//		this.attributes = SraValidationHelper.getProjectAttributes(value, ctxVal);
//		//appel suivant ne marche pas meme si definit dans la classe
//		//getProjectAttributes(value);
//	}
}
