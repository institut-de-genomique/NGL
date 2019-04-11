package models.laboratory.project.instance;

import validation.ContextValidation;
import validation.IValidation;

/**
 * UserMembers instance define permission to admin project by list of adminGroups membership
 * @author ejacoby
 *
 */
public class GroupMembers implements IValidation{

	public String name;
	
	public GroupMembers() {
	}

	

	public GroupMembers(String name) {
		super();
		this.name = name;
	}



	@Override
	public void validate(ContextValidation contextValidation) {
		//TODO
	}

}