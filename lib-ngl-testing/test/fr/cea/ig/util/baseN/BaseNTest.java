package fr.cea.ig.util.baseN;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BaseNTest {

	private static final void testEncoding(LongEncoding e) {
		for (int i=0; i<1024; i++)
			for (int l=1; l<128; l++)
				assertEquals(i, e.decode(e.encode(i,l)));
	}

	@Test
	public void testBase2Direct() {
		testEncoding(new Base2DirectEncoding());
	}
	
	@Test
	public void testBase2Array() {
		testEncoding(new Base2ArrayEncoding());
	}
	
	@Test
	public void testBase10Encoding() {
		LongEncoding e = new Base10ArrayEncoding();
		for (int i=0; i<1024; i++) {
			String es = e.encode(i);
			String is = Integer.toString(i);
			assertEquals(is,es);
			assertEquals(i, e.decode(es));
		}
	}
	
	@Test
	public void testBase26Encoding() {
		LongEncoding e = new Base26ArrayEncoding();
		assertEquals("BAA", e.encode(26*26));
	}
	
	@Test
	public void testBase52Encoding() {
		LongEncoding e = new Base52ArrayEncoding();
		assertEquals("BA", e.encode(52));
	}
	
	@Test
	public void testBase62Encoding() {
		LongEncoding e = new Base62ArrayEncoding();
		assertEquals("BA", e.encode(62));
	}
	
	@Test
	public void testBase2Custom() {
		LongEncoding e = new CharArrayBaseNEncoding(new char[] { ' ' , '#' });
		assertEquals(   " ", e.encode( 0));
		assertEquals( "#  ", e.encode( 4));
		assertEquals("####", e.encode(15));
		assertEquals(17, e.decode(e.encode(17,8)));
	}
	
}
