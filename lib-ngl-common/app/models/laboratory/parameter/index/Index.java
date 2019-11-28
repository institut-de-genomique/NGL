package models.laboratory.parameter.index;

import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

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
	 * Validate a sequence (depends on categoryCode).
	 * <ul>
	 *   <li> SINGLE-INDEX &amp; MID : only nucleotides    </li>
	 *   <li> DUAL-INDEX : nucleotides + 1 "-" </li>
	 *   <li> POOL-INDEX : nucleotides + 3 "-" </li>
	 * </ul>
	 * 
	 * Note: validate also the categoryCode !!!???
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
	 * if at most one of the sequence char is not valid then return false.
	 * 
	 * @param sequence           sequence to validate
	 * @param validCh            authorized characters
	 * @return                   true if the input sequence contains only valid chars
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
	// __FDS__: use fail fast directly
	public static boolean validChars_1(String sequence, String validCh) {
		for (int i = 0; i < sequence.length(); i++) 
			if (validCh.indexOf( sequence.charAt(i) ) < 0)
				return false;
		return true;
	}
	// __FDS__: use predicate (for fun)
	public static final IntPredicate isACGT = c -> { 
		switch (c) {
		case 'A': case 'C': case 'G': case 'T': return true;
		default : return false;
		}
	};
	public static final IntPredicate isACGT2 = c -> "ACGT".indexOf(c) >= 0;
	public static boolean isACGT(String sequence) { return validChars_2(sequence, isACGT); }
	public static boolean validChars_2(String sequence, IntPredicate p) {
		for (int i = 0; i < sequence.length(); i++) 
			if (!p.test(sequence.charAt(i)))
				return false;
		return true;
	}


}
