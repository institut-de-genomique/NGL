package fr.cea.ig.util.baseN;

/**
 * String to positive long encoding such that: 
 * <ul>
 *   <li>encode(decode(s)) == s</li>
 *   <li>decode(encode(i)) == i</li>
 *   <li>decode(encode(i,l)) == i</li>
 * </ul>
 * 
 * @author vrd
 *
 */
public interface LongEncoding {
	
	/**
	 * Encode value as a string.
	 * @param value value to encode
	 * @return      encoded value as string
	 */
	default String encode(long value) {
		return encode(value,1);
	}
	
	/**
	 * Encode value as a string of at least minLength length.
	 * @param value     value to encode
	 * @param minLength minimum result string length ({@literal >= 1})
	 * @return          value encoded as a string of at least length minLength 
	 */
	String encode(long value, int minLength);
	
	/**
	 * Decode a string as a long value. A runtime exception is thrown if the
	 * string to decode is not a valid representation of an encoded value.
	 * @param s string to decode
	 * @return  decoded value
	 */
	long decode(String s);
	
}
