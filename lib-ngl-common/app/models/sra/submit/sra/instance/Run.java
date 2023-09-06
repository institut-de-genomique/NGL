package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.util.VariableSRA;
import validation.ContextValidation;
import validation.IValidation;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class Run implements IValidation {
	// RunType
	public String code;            // champs alias required mais remplacé par code 
	public Date runDate;           
	public String runCenter;       // required pour nos stats valeur fixee à GSC 
	public String accession;       // numeros d'accession attribué par ebi 
	public String expCode;
	public String expAccession;
	public List <RawData> listRawData = new ArrayList<>();
	public String adminComment; // commentaire privé "reprise historique"				
	
	public void validateLight(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("run");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);

		// Verifier que runDate est bien renseigné :
		ValidationHelper.validateNotEmpty(contextValidation, this.runDate , "runDate");
		SraValidationHelper.requiredAndConstraint(contextValidation, this.runCenter, VariableSRA.mapCenterName(), "runCenter");
	}
	
	public void validateInvariants(ContextValidation contextValidation) {
		validateLight(contextValidation);
		contextValidation = contextValidation.appendPath("run");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		if(listRawData.isEmpty()) {
			contextValidation.addError("run.listRawData", this.code + " sans fichier de donnée brute associé");
		}
		for(RawData rawData : listRawData) {
			rawData.validate(contextValidation);
		}
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		String mess = "validation du run ";
		if (StringUtils.isNotBlank(code)) {
			mess += code;
		}
		// Attention :  l'ajout ici dans addKeyToRootKeyName  provoque une erreur qui n'a pas de message ce qui pose gros problemes sur interfaces
		//contextValidation.addKeyToRootKeyName(mess);
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
		// a ne pas mettre  : contextValidation.removeKeyFromRootKeyName(mess);

	}

	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("run");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "run.code")) 
			contextValidation = contextValidation.appendPath(code);
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(experimentAPI.dao_checkObjectExist("run.code", this.code)) {
			contextValidation.addError("run.code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("run");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "run.code")) 
			contextValidation = contextValidation.appendPath(code);
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(! experimentAPI.dao_checkObjectExist("run.code", code)) {
			contextValidation.addError("run.code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("run");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "run.code")) 
			contextValidation = contextValidation.appendPath(code);
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		if(! experimentAPI.dao_checkObjectExist("run.code", code)) {
			contextValidation.addError("run.code", code + " n'existe pas dans la base de données et MODE UPDATE");
		}	
	}
	
	
	
}
