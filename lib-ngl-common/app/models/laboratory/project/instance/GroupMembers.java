package models.laboratory.project.instance;

import java.util.List;

import validation.ContextValidation;
import validation.IValidation;

/**
 * Define a group of user members
 * 
 * @author ejacoby
 *
 */
public class GroupMembers implements IValidation {

	public String name;
	public String displayName;
	
	public List<UserMembers> members;
	
	public GroupMembers() {
	}

	public GroupMembers(String name) {
		this.name = name;
	}

	@Override
	public void validate(ContextValidation contextValidation) {
	}

}