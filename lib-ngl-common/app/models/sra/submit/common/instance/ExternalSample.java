package models.sra.submit.common.instance;

import fr.cea.ig.ngl.dao.api.sra.ExternalSampleAPI;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class ExternalSample extends AbstractSample {
	
	public static final play.Logger.ALogger logger = play.Logger.of(ExternalSample.class);
	
	public ExternalSample() {
		super(AbstractSample.externalSampleType);	
		state = new State("F-SUB", null); // Reference sur "models.laboratory.common.instance.state"
	}

	public void validateInvariants(ContextValidation contextValidation) {
//		logger.debug("ok dans ExternalSample.validate\n");
		contextValidation = contextValidation.appendPath("externalSample");
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_SAMPLE_COLL_NAME);
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, this.state);
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("externalSample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ExternalSampleAPI externalSampleAPI = ExternalSampleAPI.get();
		if(externalSampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("externalSample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ExternalSampleAPI externalSampleAPI = ExternalSampleAPI.get();
		if(! externalSampleAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("externalSample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ExternalSampleAPI externalSampleAPI = ExternalSampleAPI.get();
		if(! externalSampleAPI.dao_checkObjectExist("code", code)) {
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
