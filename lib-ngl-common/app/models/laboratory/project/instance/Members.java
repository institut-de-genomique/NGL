package models.laboratory.project.instance;

import java.util.ArrayList;
import java.util.List;

//import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

/**
 * Members instance define members of Project instance and permission group associated
 * Members instance outside DB server
 * @author ejacoby
 *
 */
public class Members implements IValidation {

	public String            codeProjet;
	public List<UserMembers> admins         = new ArrayList<>();
	public List<UserMembers> users          = new ArrayList<>();
	public String            groupName;
	public String            adminGroupName;
	

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, codeProjet, "code");

		if(contextValidation.isUpdateMode() && (users.size()==0 && admins.size()==0)) {
			contextValidation.addError("users",ValidationConstants.ERROR_REQUIRED_MSG, codeProjet);
		}
	}
	
}
