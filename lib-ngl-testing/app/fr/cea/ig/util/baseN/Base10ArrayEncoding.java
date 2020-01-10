package fr.cea.ig.util.baseN;

/**
 * Natural base 10 encoding (0 = '0').
 * 
 * @author vrd
 *
 */
public class Base10ArrayEncoding extends CharArrayBaseNEncoding {
	
	private static final char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	
	public Base10ArrayEncoding() {
		super(chars);
	}
	
}
