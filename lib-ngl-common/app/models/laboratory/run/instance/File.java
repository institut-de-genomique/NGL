package models.laboratory.run.instance;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import validation.ContextValidation;
import validation.run.instance.FileValidationHelper;
import validation.utils.ValidationHelper;

// public class File implements IValidation {
public class File {

	// concatenation de flotseqname + flotseqext
	public String  fullname;
	public String  extension;
	public Boolean usable    = Boolean.FALSE;
	public String  typeCode; // id du type de fichier
	@JsonIgnore
	public State   state;    // GA: remove later
	public Map<String, PropertyValue> properties = new HashMap<>();

	/*
	asciiEncoding	encodage ascii du fichier
	label			id du label du fichier READ1 / READ2 / SINGLETON
	*/

//	/**
//	 * Validate a file (context parameter "objectClass", ("readSet" or "analysis")).
//	 */
//	@Override
//	public void validate(ContextValidation contextValidation) {
//		FileValidationHelper.validateFileFullName  (contextValidation, fullname);
//		ValidationHelper    .validateNotEmpty      (contextValidation, extension, "extension");
//		ValidationHelper    .validateNotEmpty      (contextValidation, typeCode,  "typeCode");
//		ValidationHelper    .validateNotEmpty      (contextValidation, usable,    "usable");
//		FileValidationHelper.validateFileProperties(contextValidation, properties);		
//	}

	public void validate(ContextValidation contextValidation, Analysis analysis) {
		FileValidationHelper.validateFileFullName  (contextValidation, fullname,   analysis);
		ValidationHelper    .validateNotEmpty      (contextValidation, extension,  "extension");
		ValidationHelper    .validateNotEmpty      (contextValidation, typeCode,   "typeCode");
		ValidationHelper    .validateNotEmpty      (contextValidation, usable,     "usable");
		FileValidationHelper.validateFileProperties(contextValidation, properties, analysis);		
	}

	public void validate(ContextValidation contextValidation, ReadSet readSet) {
		FileValidationHelper.validateFileFullName  (contextValidation, fullname,   readSet);
		ValidationHelper    .validateNotEmpty      (contextValidation, extension,  "extension");
		ValidationHelper    .validateNotEmpty      (contextValidation, typeCode,   "typeCode");
		ValidationHelper    .validateNotEmpty      (contextValidation, usable,     "usable");
		FileValidationHelper.validateFileProperties(contextValidation, properties, readSet);		
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("{File fullname:")
		 .append(fullname)
		 .append(" extension:")
		 .append(extension)
		 .append(" typecode:")
		 .append(typeCode);
		for (Map.Entry<String,PropertyValue> e : properties.entrySet())
			b.append(' ')
			 .append(e.getKey())
			 .append(':')
			 .append(e.getValue());
		b.append('}');
		return b.toString();
	}
	
}
