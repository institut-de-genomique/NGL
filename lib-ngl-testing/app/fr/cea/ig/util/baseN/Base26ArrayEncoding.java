package fr.cea.ig.util.baseN;

/**
 * Use Latin capitals encoding (0 ='A').
 *  
 * @author vrd
 *
 */
public class Base26ArrayEncoding extends CharArrayBaseNEncoding {
	
	private static final char[] baseChars = { 
			'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
		};
	
	public Base26ArrayEncoding() {
		super(baseChars);
	}
	
}
