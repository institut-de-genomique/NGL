package models.administration.authorisation.instance;

import java.util.Set;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.key.instance.KeyDescriptionValidationHelper;

public class KeyDescription extends DBObject implements IValidation {

	public String name;

	public String hash;

	public KeyUser user;
	
	public TraceInformation traceInformation;
	
	public Set<String> owners;

	@Override
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper  .validateIdPrimary               (contextValidation, this);
		CommonValidationHelper  .validateCodePrimary             (contextValidation, this, InstanceConstants.API_KEY_COLL_NAME);
		CommonValidationHelper  .validateTraceInformationRequired(contextValidation, traceInformation);
		KeyDescriptionValidationHelper		.validateKeyHash(contextValidation, code, hash);
		KeyDescriptionValidationHelper		.validateKeyUser(contextValidation, user);
		KeyDescriptionValidationHelper		.validateKeyOwners(contextValidation, owners);
	}

}
