package models.laboratory.project.instance;

import java.util.List;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

/**
 * UserMembers instance define permission to admin project by list of adminGroups membership
 * @author ejacoby
 *
 */
public class UserMembers implements IValidation{

	public String login;
	public String displayName;
	public List<String> adminGroups;
	
	public UserMembers() {
	}

	public UserMembers(String login) {
		this.login = login;
	}
	

	public UserMembers(String login, String displayName) {
		super();
		this.login = login;
		this.displayName = displayName;
	}
	
	public UserMembers(String login, String displayName, List<String> adminGroups) {
		super();
		this.login = login;
		this.displayName = displayName;
		this.adminGroups = adminGroups;
	}

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, login, "login");
	}

}
