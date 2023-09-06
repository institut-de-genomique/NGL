package models.sra.submit.sra.instance;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.util.VariableSRA;
import play.Logger;
import validation.ContextValidation;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class UserExperimentExtendedType {
	private String code = null;
	private String libraryStrategy  = null;
	private String librarySource    = null;
	private String librarySelection = null;
	private String libraryProtocol  = null;
	private String nominalLength    = null;
	private String title            = null;
	private String spotLength       = null;
	private String lastBaseCoordonnee = null;
	private String studyAccession     = null;
	private String sampleAccession    = null;


	public UserExperimentExtendedType() {
	}
	
	public UserExperimentExtendedType(String code) {
		this.setCode(code);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLibraryStrategy() {
		return libraryStrategy;
	}

	public void setLibraryStrategy(String libraryStrategy) {
		//verifier que libraryStrategy est  autorisee 
//		String lcLibraryStrategy = libraryStrategy.toLowerCase();
//		if (! VariableSRA.mapLibraryStrategy().containsKey(lcLibraryStrategy)) {
//			throw new SraException("La library strategy indiquee '" + libraryStrategy +
//					"' n'appartient pas a la liste des valeurs autorisees :\n" +
//					VariableSRA.mapLibraryStrategy().keySet().toString());
//		}
		this.libraryStrategy = libraryStrategy.toLowerCase();
	}

	public String getLibrarySource() {
		return librarySource;
	}

	public void setLibrarySource(String librarySource) {
		//verifier que librarySource est  autorisee 
//		String lcLibrarySource = librarySource.toLowerCase();
//		if (! VariableSRA.mapLibrarySource().containsKey(lcLibrarySource)) {
//			throw new SraException("La library source indiquee '" + librarySource +
//					"' n'appartient pas a la liste des valeurs autorisees :\n" +
//					VariableSRA.mapLibrarySource().keySet().toString());
//		}
		this.librarySource = librarySource.toLowerCase();
	}

	public String getLibrarySelection() {
		return librarySelection;
	}

	public void setLibrarySelection(String librarySelection) {
//		//verifier que librarySelection est  autorisee 
//		String lcLibrarySelection = librarySelection.toLowerCase();
//		if (! VariableSRA.mapLibrarySelection().containsKey(lcLibrarySelection)) {
//			throw new SraException("La library selection indiquee '" + librarySelection +
//					"' n'appartient pas a la liste des valeurs autorisees :\n" +
//					VariableSRA.mapLibrarySelection().keySet().toString());
//		}
		this.librarySelection = librarySelection.toLowerCase();
	}
	public String getLibraryProtocol() {
		return libraryProtocol;
	}

	public void setLibraryProtocol(String libraryProtocol){
		//verifier que librarySelection est  autorisee => Controle deporté dans validation
		this.libraryProtocol = libraryProtocol;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSpotLength() {
		return spotLength;
	}

	public void setSpotLength(String spotLength) {
		this.spotLength = spotLength;
	}
	
	public String getLastBaseCoordonnee() {
		return lastBaseCoordonnee;
	}

	public void setLastBaseCoordonnee(String lastBaseCoordonnee) {
		this.lastBaseCoordonnee = lastBaseCoordonnee;
	}
	
	public String getNominalLength() {
		return nominalLength;
	}

	public void setNominalLength(String nominalLength) {
		this.nominalLength = nominalLength;
	}
	
	public String getStudyAccession() {
		return studyAccession;
	}

	public void setStudyAccession(String studyAccession) {
		this.studyAccession = studyAccession;
	}

	public String getSampleAccession() {
		return sampleAccession;
	}

	public void setSampleAccession(String sampleAccession) {
		this.sampleAccession = sampleAccession;
	}
	
	public void validate(ContextValidation contextValidation) {
		Logger.debug("Dans UserExperimentType.validate: ");
		String mess = "UserExperimentExtendedType ";
		if (StringUtils.isNotBlank(code)) {
			mess += "." + code;
		}
		contextValidation.addKeyToRootKeyName(mess);
		ValidationHelper.validateNotEmpty(contextValidation, code, "code");
		//verifier contraintes des differents champs:
		SraValidationHelper.noRequiredButConstraint(contextValidation, this.getLibrarySelection(), VariableSRA.mapLibrarySelection(), "librarySelection");
		SraValidationHelper.noRequiredButConstraint(contextValidation, this.getLibraryStrategy(), VariableSRA.mapLibraryStrategy(), "libraryStrategy");
		SraValidationHelper.noRequiredButConstraint(contextValidation, this.getLibrarySource(), VariableSRA.mapLibrarySource(), "librarySource");
		SraValidationHelper.noRequiredInt(contextValidation, this.getLastBaseCoordonnee(), "lastBaseCoordonnee"); 
		SraValidationHelper.noRequiredInt(contextValidation, this.getNominalLength(), "nominalLength"); 
		SraValidationHelper.noRequiredInt(contextValidation, this.getSpotLength(), "spotLength"); 
		contextValidation.removeKeyFromRootKeyName(mess);

	}

}
