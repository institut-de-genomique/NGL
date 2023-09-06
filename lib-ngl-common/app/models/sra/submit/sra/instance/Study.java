package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.laboratory.common.description.ObjectType;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class Study extends AbstractStudy  {

	// StudyType
	//public String alias;             // required mais remplacé par code herité de DBObject, et valeur = study_projectCode_num
	public String title = "";	       // required next soon   
	public String studyAbstract = "";  // required next soon  // conservé mais mis à meme valeur que description.
	public String description = "";  
	public String existingStudyType;   // required et constraint 
	public Date firstSubmissionDate;   // date de premiere soumission du study.
// 	public String accession = null;      // numeros d'accession attribué par ebi */
 	public String externalId = null;
  	public Date releaseDate;             // required, date de mise à disposition en public par l'EBI
  	public String centerName = VariableSRA.centerName;        // required pour nos stats valeur fixee à GSC */
    public String centerProjectName;      // required pour nos stats valeur fixée à projectCode
 	public List <String> projectCodes = new ArrayList<>();    // required pour nos stats  
	public List<String> locusTagPrefixs;     // champs pouvant etre rempli lors d'une mise à jour uniquement
	public List<String> idsPubmed;           // champs pouvant etre rempli lors d'une mise à jour uniquement
    // et heritage de state et traceInformation.
	public static final String initialStateCode = "NONE";
	//public String adminComment; // deja dans AbstractStudy
    public String taxonId;          // dans le cas des project ERGA, les projects  peuvent etre associés à un taxonId et a scientificName
    public String scientificName;   // dans le cas des project ERGA, les projects  peuvent etre associés à un taxonId et a scientificName

	
	public Study() {
		super(AbstractStudy.studyType);
	}
	
	
	public void validateInvariants(ContextValidation contextValidation) {
		//logger.debug("!!!!!!!!!!!! Dans Study.validateInvariants");

		if (contextValidation.getObject("copy")!= null && (boolean) contextValidation.getObject("copy")) {
			//boolean copy = true;
			List <String> studyCodesNoValidable = new ArrayList<String>();
			studyCodesNoValidable.add("ERP003734");
			studyCodesNoValidable.add("ERP001812"); 
			studyCodesNoValidable.add("ERP000673");
			studyCodesNoValidable.add("ERP003211");
			studyCodesNoValidable.add("ERP002664"); 
			studyCodesNoValidable.add("ERP000095");
			studyCodesNoValidable.add("ERP003719");
			studyCodesNoValidable.add("ERP000674");
			studyCodesNoValidable.add("ERP007080");
			studyCodesNoValidable.add("ERP014715");
			studyCodesNoValidable.add("ERP014714");	
			if (StringUtils.isNotBlank(this.accession) && studyCodesNoValidable.contains(this.accession)) {
				return;
			}
		}
		contextValidation = contextValidation.appendPath("study");

		// verifier que projectCodes est bien renseigné et existe dans lims:
		CommonValidationHelper.validateProjectCodes (contextValidation, projectCodes);
		// Attention on peut vouloir regrouper dans un project_code virtuel ?? 
		ValidationHelper      .validateNotEmpty     (contextValidation, centerProjectName , "centerProjectName");
		ValidationHelper      .validateNotEmpty     (contextValidation, title,         "title");
		ValidationHelper      .validateNotEmpty     (contextValidation, studyAbstract, "studyAbstract"); 
		if (StringUtils.isNotBlank(description) && StringUtils.isNotBlank(studyAbstract)) {
			if( ! description.equals(studyAbstract)) {
				contextValidation.addError("description", "valeur '" + description + "' avec valeur differente de studyAbstract  " + studyAbstract);
			}
		}
		ValidationHelper      .validateNotEmpty     (contextValidation, description,   "description"); 
		SraValidationHelper   .requiredAndConstraint(contextValidation, centerName,	VariableSRA.mapCenterName(), "centerName");
		CommonValidationHelper.validateIdPrimary    (contextValidation, this);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_STUDY_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		if (state != null && StringUtils.isNotBlank(state.code) && !initialStateCode.equals(state.code)) {
			CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, state);
		}
		SraValidationHelper   .validateFreeText     (contextValidation, "centerProjectName",         centerProjectName);
		SraValidationHelper   .validateFreeText     (contextValidation, "title",         title);
		SraValidationHelper   .validateFreeText     (contextValidation, "studyAbstract", studyAbstract);
		SraValidationHelper   .validateFreeText     (contextValidation, "description",   description);
		CommonValidationHelper.validateCodePrimary  (contextValidation, this, InstanceConstants.SRA_STUDY_COLL_NAME);
		SraValidationHelper   .requiredAndConstraint(contextValidation, existingStudyType, VariableSRA.mapExistingStudyType(), "existingStudyType");

		if(this.locusTagPrefixs != null) {
			for(String locusTag : this.locusTagPrefixs) {
				//logger.debug("XXXXXXXXXXXXXXXXX   locusTag='" + locusTag + "'");
				if( StringUtils.isBlank(locusTag) ) {
					contextValidation.addError("locusTagPrefixs", " avec donnee sans caractere visible");
				}
				if (StringUtils.isNotBlank(locusTag)) {
					if ( ! locusTag.matches("([a-zA-Z_0-9]|-)+")) {
						contextValidation.addError("locusTagPrefixs", "'" +locusTag + "' contient un caratere non attendu, (autre que [a-zA-Z_0-9]|-)");
					}
				}
			}
			// verifier qu'il n'y pas de doublon dans la liste des loci:
			Set<String> set = new HashSet<String>(this.locusTagPrefixs);
			if(set.size() < this.locusTagPrefixs.size()) {
				String liste_loci = "";
				for (String c: this.locusTagPrefixs) {
					liste_loci += c + ", \n";
				}
				liste_loci = liste_loci.replaceFirst(",\\s*$", ""); // oter ',' terminale si besoin
				contextValidation.addError("locusTagPrefixs", " Il existe des doublons dans la liste " + liste_loci);
			}
		}
		

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
		
	
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("study");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		StudyAPI studyAPI = StudyAPI.get();
		if(studyAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
		boolean copy = false;
		if (contextValidation.getObject("copy")!= null && (boolean) contextValidation.getObject("copy")) {
			copy = true;
		}
// On peut avoir un import de study soumis hors NGL-SUB		
//		if(! copy) {
//			if(  ! ( StringUtils.isNotBlank(this.state.code) && this.state.code.equals(NONE) ) )  { 
//				contextValidation.addError("state.code", "valeur '" + state.code + "qui devrait etre a NONE");
//			}
//		}
	
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
		AbstractStudyAPI abstStudyAPI = AbstractStudyAPI.get();
		if(! abstStudyAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE UPDATE");
		}	
		AbstractStudy abstStudy = abstStudyAPI.dao_getObject(code);
		if(abstStudy instanceof ExternalStudy) {
			contextValidation.addError("code", code + " est un ExternalStudy et  MODE UPDATE");
		}			
	}

	@Override
	public void validate(ContextValidation contextValidation) {
		String mess = "validation du study ";
		if (StringUtils.isNotBlank(code)) {
			mess += "." + code;
		}
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
