package models.sra.submit.sra.instance;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class Attribute implements IValidation {
	public String tag;
	public String value;
	public String unit;	
	//public String code;
	public String _type;
	//public String createUser;
	//public Date   creationDate;
	
	public Attribute() {
	}

	public Attribute(String type, String tag, String value) {
		this._type = type;
		this.tag   = tag;
		this.value = value;
		//this.createUser   = user;
		//this.creationDate = new Date();
		//this.code = generateAttributeCode(this);		
	}	
	public Attribute(String type, String tag, String value, String unit) {
		this._type = type;
		this.tag   = tag;
		this.value = value;
		this.unit  = unit;
		//this.createUser   = tag;
		//this.creationDate = new Date();
		//this.code = generateAttributeCode(this);		
	}	
	

	
	public synchronized String generateAttributeCode(Attribute attribute) {
		//		return (attribute.createUser + getSimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + Math.random()).toUpperCase();
		return (attribute.tag + getNowFormatted("yyyyMMdd_HHmmss") + Math.random()).toUpperCase();
	}
	
	/**
	 * Format now (Date()) using a given format.
	 * @param format date output format
	 * @return       formatted date
	 */
	private String getNowFormatted(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}
	
	
	@Override
	public void validate(ContextValidation contextValidation) {
		//ValidationHelper.validateNotEmpty(contextValidation, code,         "code"); // GA: check if not exist on the same object
		ValidationHelper.validateNotEmpty(contextValidation, _type,       "_type"); 
		ValidationHelper.validateNotEmpty(contextValidation, tag,         "tag");
		ValidationHelper.validateNotEmpty(contextValidation, value,       "value"); 
	}
	
	public String toString() {
		
		String strAttribute = "";
		if(StringUtils.isBlank(this.unit)) {
			strAttribute = "<" + this._type +"><TAG>" + this.tag + "</TAG><VALUE>" + this.value + "</VALUE></" + this._type + ">";
		} else {
			strAttribute = "<" + this._type +"><TAG>" + this.tag + "</TAG><VALUE>" + this.value + "</VALUE><UNIT>" + this.unit + "</UNIT></" + this._type + ">";
		}
		return strAttribute;
	}
	
}
