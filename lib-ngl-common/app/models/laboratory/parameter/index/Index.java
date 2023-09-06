package models.laboratory.parameter.index;

import java.util.List;
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
	
	public String supplierName;

	public String supplierIndexName;
	
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
		ValidationHelper.validateNotEmpty(contextValidation, name, "name");// Added 25/09/2020 
		ValidationHelper.validateNotEmpty(contextValidation, code, "code");// Added 25/09/2020 
		
		validateSequence(contextValidation, sequence, categoryCode);// Added 17/01/2019
		validateShortName(contextValidation, shortName);// Added 25/09/2020 NGL-3109
		validateCode(contextValidation, code);// Added 25/09/2020 NGL-3109
		validateName(contextValidation, name);// Added 25/09/2020 NGL-3109
		
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
	 * @param sequence           sequence of index
	 * @param categoryCode		 category of index
	 * @author fdsantos
	 */
	public void validateSequence(ContextValidation contextValidation, String sequence, String categoryCode) {
		
		String separator="-";       // default separator
		String nucleotids= "ACGT";  // standard nucleotids
		
		switch (categoryCode) {
			case "SINGLE-INDEX":
			case "MID":	
				if (! validChars(sequence, nucleotids)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence); //TODO mettre dans "message" ??
				}
				break;
			case "DUAL-INDEX":
				if (! validChars(sequence,nucleotids+separator)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence); //TODO mettre dans "message" ??
				}
				if ( StringUtils.countMatches(sequence, separator) != 1) {
					contextValidation.addError("Index sequence","exactly 1 '-' expected for "+ categoryCode, sequence); //TODO mettre dans "message" ??
				}
				break;
			case "POOL-INDEX":
				if (! validChars(sequence,nucleotids+separator)) {
					contextValidation.addError("Index sequence", "illegal nucleotid found", sequence);//TODO mettre dans "message" ??
				}
				if ( StringUtils.countMatches(sequence, separator) != 3) {
					contextValidation.addError("Index sequence","exactly 3 '-' expected for "+ categoryCode, sequence);//TODO mettre dans "message" ??
				}
				break;	
			default:
				contextValidation.addError("Index categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
		}
	}
	
	/**
	 * Validate a name: authorized chars: [A-Z],[a-z],[0-9], '_', '-','.'
	 * @param contextValidation  validation context
	 * @param name               parameter name to validate
	 * 
	 * @author fdsantos
	 */
	public void validateName(ContextValidation contextValidation, String name) {
		boolean isValid = true;
		for (int i = 0; isValid && i < name.length(); i++) {
			char c =name.charAt(i);
			// isLetterOrDigit  accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			// isAlphabetic     accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			if ( !Character.isDigit(c) && !(c>='a' && c<='z') && !(c>='A' && c<='Z') &&  c != '_' && c != '-'& c != '.') {
				isValid=false;
				contextValidation.addError("Index name", "illegal character '"+c+"' at position "+i, name); //TODO mettre dans "message" ??
			}	
		}
	}
	
	/**
	 * Validate a code: authorized chars: [A-Z],[a-z],[0-9], '_', '-'
	 *   DBObject code is usually calculated by NGL thus there is no need to control it. 
	 *   But here code is created by user, so it's better to control it !!!
	 * Duplication of validateName method to have different ranges of authorized chars if needed...
	 * @param contextValidation  validation context
	 * @param code               parameter code to validate
	 * 
	 * @author fdsantos
	 */
	public void validateCode(ContextValidation contextValidation, String code) {
		boolean isValid = true;
		for (int i = 0; isValid && i < code.length(); i++) {
			char c =code.charAt(i);
			// isLetterOrDigit  accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			// isAlphabetic     accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			if ( !Character.isDigit(c) && !(c>='a' && c<='z') && !(c>='A' && c<='Z') &&  c != '_' && c != '-') {
				isValid=false;
				contextValidation.addError("Index code", "illegal character '"+c+"' at position "+i, code); //TODO mettre dans "messages" ??
			}	
		}
	}
	
	/**
	 * Validate a shortName: authorized chars: [A-Z],[a-z],[0-9],
	 * Duplication of validateName method to have different ranges of authorized chars if needed...
	 * !! shortName is used by ngs-rg to create a filename: '.' is not autorized
	 * !! ngs-rg file is parsed with "_" as item sepator thus it must not be used
	 * !! "-" is used to concatenate Iary and IIary tags it must not be used
	 * @param contextValidation  validation context
	 * @param code               parameter code to validate
	 * 
	 * @author fdsantos
	 */
	public void validateShortName(ContextValidation contextValidation, String shortName) {
		boolean isValid = true;
		for (int i = 0; isValid && i < shortName.length(); i++) {
			char c =shortName.charAt(i);
			// isLetterOrDigit  accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			// isAlphabetic     accepte les accentués, cédilles etc..accepte aussi les lettres grecques !!!
			if ( !Character.isDigit(c) && !(c>='a' && c<='z') && !(c>='A' && c<='Z') ) {
				isValid=false;
				contextValidation.addError("Index shortName", "illegal character '"+c+"' at position "+i, shortName); //TODO mettre dans "messages" ??
			}	
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
	
	//...................alternative code: jean Verdier
	//  use fail fast directly
	public static boolean validChars_1(String sequence, String validCh) {
		for (int i = 0; i < sequence.length(); i++) 
			if (validCh.indexOf( sequence.charAt(i) ) < 0)
				return false;
		return true;
	}
	// use predicate (for fun)
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
