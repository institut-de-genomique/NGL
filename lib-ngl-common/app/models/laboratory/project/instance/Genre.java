package models.laboratory.project.instance;

import validation.ContextValidation;
import validation.IValidation;

public class Genre implements IValidation {

	public String name;
	public String code;
	
	public Genre() {
	}

	public Genre(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public void validate(ContextValidation contextValidation) {
	}

}