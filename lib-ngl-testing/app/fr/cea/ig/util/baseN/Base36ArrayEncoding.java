package fr.cea.ig.util.baseN;

public class Base36ArrayEncoding extends CharArrayBaseNEncoding {
	
	private static final char[] baseChars = { 
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
		};
	
	public Base36ArrayEncoding() {
		super(baseChars);
	}
	
}
