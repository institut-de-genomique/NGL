package models.laboratory.parameter.index;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.parameter.Parameter;
import validation.ContextValidation;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public abstract class Index extends Parameter {

	public String sequence;
	
	public String shortName; //used by NGS-RG
	
	public Map<String,String> supplierName;
	
	public List<String> groupNames; // NGL 1350; same index can belong to several groups (kits ?)
	
	public Index(String typeCode) {
		super(typeCode);		
	}

	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		ValidationHelper.validateNotEmpty(contextValidation, sequence,  "sequence");
		ValidationHelper.validateNotEmpty(contextValidation, shortName, "shortName");
		// Added 17/01/2019
		validateSequence(contextValidation, sequence, categoryCode);
	}
	
	/**
	 * Validate a sequence (depends on categoryCode).<br>
	 * 
	 * SINGLE-INDEX & MID: only nucleotides<br>
	 * DUAL-INDEX: nucleotides + 1 "-"<br>
	 * POOL-INDEX: nucleotides + 3 "-"<br>
	 * 
	 * Note: validate also the categoryCode !!!???<br>
	 * 
	 * @param contextValidation  validation context
	 * @param sequence           sequence
	 * @param categoryCode		 category of index
	 * 
	 * @author fdsantos
	 */
	public void validateSequence(ContextValidation contextValidation, String sequence, String categoryCode) {
		
		String separator="-";       // default separator
		String nucleotids= "ACGT";  // standard nucleotids
		
		switch (categoryCode) {
			case "SINGLE-INDEX":
			case "MID":	
				if (! validChars(sequence, nucleotids)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence);
				}
				break;
			case "DUAL-INDEX":
				if (! validChars(sequence,nucleotids+separator)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence);
				}
				if ( StringUtils.countMatches(sequence, separator) != 1) {
					contextValidation.addError("Index sequence","exactly 1 '-' expected for "+ categoryCode, sequence);
				}
				break;
			case "POOL-INDEX":
				if (! validChars(sequence,nucleotids+separator)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence);
				}
				if ( StringUtils.countMatches(sequence, separator) != 3) {
					contextValidation.addError("Index sequence","exactly 3 '-'expected for "+ categoryCode, sequence);
				}
				break;	
			default:
				contextValidation.addError("Index categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
		}
	}
	
	/** 
	 * if atmost one of the sequence char is not valid then return false
	 * 
	 * @param sequence           sequence to validate
	 * @param validCh            authorized characters
	 * 
	 * @author fdsantos (adapted from https://codereview.stackexchange.com/questions/39953/)
	 */
	public static boolean validChars (String sequence, String validCh) {
		
		boolean isValid = true;
		for (int i = 0; isValid && i < sequence.length(); i++) {
			isValid = ! (validCh.indexOf( sequence.charAt(i) ) < 0);
		}
		return isValid;
	}
	

}
