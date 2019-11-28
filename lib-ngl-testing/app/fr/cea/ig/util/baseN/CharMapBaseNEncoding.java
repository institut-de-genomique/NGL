package fr.cea.ig.util.baseN;

/**
 * Encoding using a char to digit mapping defined by methods ({@link CharMapBaseNEncoding#decodeBaseValue(char)},
 * {@link CharMapBaseNEncoding#encodeBaseValue(int)}.
 * 
 * @author vrd
 *
 */
public abstract class CharMapBaseNEncoding implements LongEncoding {
	
	@Override
	public String encode(long value, int minLength) {
		if (value < 0)
			throw new IllegalArgumentException("value to encode cannot be negative");
		minLength = Math.max(1, minLength);
		StringBuilder sb = new StringBuilder();
		int base = base();
		while (value != 0) {
			long div = value / base;
			long rem = value % base;
			sb.append(encodeBaseValue((int)rem));
			value = div;
		}
		while (sb.length() < minLength)
			sb.append(encodeBaseValue(0));
		return sb.reverse().toString();
	}
	
	@Override
	public long decode(String s) {
		long value = 0;
		long base = base();
		for (int i=0; i<s.length(); i++) {
			value *= base;
			char c = s.charAt(i);
			value += decodeBaseValue(c);
		}
		return value;
	}
	
	/**
	 * Base to use.
	 * @return encoding base to use
	 */
	public abstract int base();
	
	/**
	 * Encode a digit from the base ({@link #base()}).
	 * @param value value to convert to a char
	 * @return      char representation of the value
	 */
	public abstract char encodeBaseValue(int value);
	
	/**
	 * Decode a char as a digit from the base ({@link #base()}).
	 * @param c char to decode
	 * @return  char digit value
	 */
	public abstract int decodeBaseValue(char c);
	
}
