package models.laboratory.storage.instance;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import validation.ContextValidation;
import validation.IValidation;

public class StorageHistory implements IValidation {
	
	//ContainerSupport ref
	public String barCode;
	public String createUser;
	public Date creationDate;
	
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
	}

}
