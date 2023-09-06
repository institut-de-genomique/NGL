package fr.cea.ig.play.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DevAppTestingTest {

//	// -- test encoding base26
//	
//	@Test
//	public void testEncoding26_negative() {
//		try {
//			DevAppTesting.base26encode(-1);
//			fail();
//		} catch (Exception e) {
//		}
//	}
//	
//	@Test
//	public void testEncoding26_zero() {
//		assertEquals("A", DevAppTesting.base26encode(0));
//	}
//	
//	@Test
//	public void testEncoding26_zero_pad() {
//		assertEquals("AAA", DevAppTesting.base26encode(0,3));
//	}
//
//	@Test
//	public void testEncoding26_1() {
//		assertEquals("B", DevAppTesting.base26encode(1));
//	}
//
//	@Test
//	public void testEncoding26_1_pad() {
//		assertEquals("AAB", DevAppTesting.base26encode(1,3));
//	}
//	
//	@Test
//	public void testEncoding26_51() {
//		assertEquals("BZ", DevAppTesting.base26encode(51));
//	}
//	
//	@Test
//	public void testEncoding26_51_pad() {
//		assertEquals("AAAABZ", DevAppTesting.base26encode(51,6));
//	}
//
//	// -- binary encoding
//	
//	private static final char[] binaryChars = { '0', '1' };
//	
//	@Test
//	public void testEncodingBinary_7() {
//		assertEquals("111", DevAppTesting.baseEncode(binaryChars, 7));
//	}
//	
//	@Test
//	public void testEncodingBinary_7_pad() {
//		assertEquals("00000111", DevAppTesting.baseEncode(binaryChars, 7, 8));
//	}
//	
//	// -- hexadecimal encoding
//	
//	private static final char[] hexaChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
//	
//	@Test
//	public void testEncodingHexa_26() {
//		assertEquals("1A", DevAppTesting.baseEncode(hexaChars, 26));
//	}
//	
//	@Test
//	public void testEncodingHexa_26_pad() {
//		assertEquals("001A", DevAppTesting.baseEncode(hexaChars, 26, 4));
//	}
//	
	// -- code 
	
	@Test
	public void testCode() {
		String prefix = DevAppTesting.codePrefix();
		String code0  = DevAppTesting.newCode();
		String code1  = DevAppTesting.newCode();
		assertTrue("code0 prefix", code0.startsWith(prefix));
		assertTrue("code1 prefix", code1.startsWith(prefix));
		assertNotEquals(code0, code1);
		//assertEquals(code0, code1);
	}
	
}
