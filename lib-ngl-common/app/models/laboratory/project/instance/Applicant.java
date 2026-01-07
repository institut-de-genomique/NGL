package models.laboratory.project.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import validation.ContextValidation;
import validation.IValidation;

public class Applicant implements IValidation {

	public int id;

	public String prenom;

	public String nom;

	public String affiliation;
	
	@Override
	@JsonIgnore
	public void validate(ContextValidation contextValidation) {

	}
}
