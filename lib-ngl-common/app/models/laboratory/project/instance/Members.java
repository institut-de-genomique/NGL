package models.laboratory.project.instance;

import java.util.ArrayList;
import java.util.List;

//import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;

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
	
//	public Members() {}

	@Override
	public void validate(ContextValidation contextValidation) {
//		CommonValidationHelper.validateRequiredInstanceCode(codeProjet, "code", Project.class, InstanceConstants.PROJECT_COLL_NAME, contextValidation);
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, Project.find.get(), codeProjet, "code");
		if (contextValidation.isCreationMode() && admins.size()==0) {
//			contextValidation.addErrors("admins",ValidationConstants.ERROR_REQUIRED_MSG, codeProjet);
			contextValidation.addError("admins",ValidationConstants.ERROR_REQUIRED_MSG, codeProjet);
		} else if(contextValidation.isUpdateMode() && (users.size()==0 && admins.size()==0)) {
//			contextValidation.addErrors("users",ValidationConstants.ERROR_REQUIRED_MSG, codeProjet);
			contextValidation.addError("users",ValidationConstants.ERROR_REQUIRED_MSG, codeProjet);
		}
	}
	
}
