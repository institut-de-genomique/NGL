package fr.cea.ig.util.baseN;

/**
 * Base n encoding using a char array definition. Digits encoding is done
 * using the array indices.
 * 
 * @author vrd
 *
 */
public class CharArrayBaseNEncoding extends CharMapBaseNEncoding {
	
	/**
	 * Ordinal mapping.
	 */
	private final char[] chars;
	
	/**
	 * Construct a new encoding with a given alphabet.
	 * @param chars alphabet to use
	 */
	public CharArrayBaseNEncoding(char[] chars) {
		this.chars = chars;
	}
	
	@Override
	public int base() { 
		return chars.length; 
	}
	
	@Override
	public char encodeBaseValue(int value) { 
		return chars[value]; 
	}
	
	@Override
	public int decodeBaseValue(char c) {
		for (int i=0; i<chars.length; i++)
			if (c == chars[i])
				return i;
		throw new IllegalArgumentException("char '" + c + "' does not belong to the encoding");
	}
	
}
