package fr.cea.ig.util.function;

import static fr.cea.ig.play.test.TestAssertions.assertFail;
import static fr.cea.ig.play.test.TestAssertions.assertThrows;
import static fr.cea.ig.util.function.CCTestHelper.N1;
import static fr.cea.ig.util.function.CCTestHelper.N2;
import static fr.cea.ig.util.function.CCTestHelper.N3;
import static fr.cea.ig.util.function.CCTestHelper.N4;
import static fr.cea.ig.util.function.CCTestHelper.N5;
import static fr.cea.ig.util.function.CCTestHelper.N6;
import static fr.cea.ig.util.function.CCTestHelper.S1;
import static fr.cea.ig.util.function.CCTestHelper.S2;
import static fr.cea.ig.util.function.CCTestHelper.S3;
import static fr.cea.ig.util.function.CCTestHelper.S4;
import static fr.cea.ig.util.function.CCTestHelper.S5;
import static fr.cea.ig.util.function.CCTestHelper.S6;
import static fr.cea.ig.util.function.CCTestHelper.cc1_fail;
import static fr.cea.ig.util.function.CCTestHelper.cc1_n1;
import static fr.cea.ig.util.function.CCTestHelper.cc1_s1;
import static fr.cea.ig.util.function.CCTestHelper.cc1_s2;
import static fr.cea.ig.util.function.CCTestHelper.cc2_int;
import static fr.cea.ig.util.function.CCTestHelper.cc2_string;
import static fr.cea.ig.util.function.CCTestHelper.cc3_string;
import static fr.cea.ig.util.function.CCTestHelper.cc4_string;
import static fr.cea.ig.util.function.CCTestHelper.cc5_string;
import static fr.cea.ig.util.function.CCTestHelper.cc6_int;
import static fr.cea.ig.util.function.CCTestHelper.cc6_string;
import static fr.cea.ig.util.function.CCTestHelper.testCC1;
import static fr.cea.ig.util.function.CCTestHelper.testCC1Fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.cea.ig.util.function.CCTestHelper.CC1FailException;

/**
 * Tries to test CCN and CTN implementations.
 * 
 * <br>Implementations of 'and' methods rely on the 'nest' implementation so testing 'and' is a good as testing 'nest' and
 * 'and'.
 * <br>Test method is defined for CC1 and all the constructions are transformed to CTN for the
 * test so 'ct' methods are tested.
 * 
 * @author vrd
 *
 */
public class CCTest {

	
	@Test
	public void test_cc1() throws Exception {
		testCC1(cc1_s1, 1);
	}
	
	@Test
	public void test_cc1_fail() throws Exception {
		assertFail(() -> testCC1(cc1_s2, 1, a -> fail()));
	}
	
	@Test
	public void test_cc1_fail_depth() throws Exception {
		assertFail(() -> testCC1(cc1_s2, 0));
	}	
	
	@Test
	public void test_cc1_and_cc1() throws Exception {
		testCC1(cc2_string.ct(), 2);
	}
	
	@Test
	public void test_cc1_and_cc1_vals() throws Exception {
		testCC1(cc2_string.ct(),
				2,
				t -> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
				});
	}
	
	@Test
	public void test_cc2_proj() throws Exception {
		testCC1(cc2_string.cc1((a,b) -> b),
				2,
				s -> assertEquals(S2, s));
	}
	
	@Test
	public void test_cc3() throws Exception {
		testCC1(cc3_string.ct(), 3,
				t-> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
				});
	}
	
	@Test
	public void test_cc4() throws Exception {
		testCC1(cc4_string.ct(), 4,
				t-> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
					assertEquals(S4, t.d);
				});
	}
	
	@Test
	public void test_cc5() throws Exception {
		testCC1(cc5_string.ct(), 5,
				t-> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
					assertEquals(S4, t.d);
					assertEquals(S5, t.e);
				});
	}
	
	@Test
	public void test_cc6() throws Exception {
		testCC1(cc6_string.ct(), 6,
				t-> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
					assertEquals(S4, t.d);
					assertEquals(S5, t.e);
					assertEquals(S6, t.f);
				});
	}
	
	@Test
	public void test_cc6_int() throws Exception {
		testCC1(cc6_int.ct(), 6,
				t-> {
					assertEquals(N1, t.a);
					assertEquals(N2, t.b);
					assertEquals(N3, t.c);
					assertEquals(N4, t.d);
					assertEquals(N5, t.e);
					assertEquals(N6, t.f);
				});
	}
	
	@Test
	public void test_cc4_2() throws Exception {
		testCC1(cc4_string.cc1((a,b,c,d) -> b)
				.and2(cc2_string)
				.cc2((a,b,c) -> new T2<>(a,b))
				.ct(),
				6,
				t -> {
					assertEquals(S2, t.a);
					assertEquals(S1, t.b);
				});
	}
	
	// The max depth is 1 as the failing cc1 forbids the execution
	// of the next cc1.
	@Test
	public void test_cc1_fail_1() throws Exception {
		assertThrows(CC1FailException.class, () -> testCC1Fail(cc1_s1.and(cc1_fail).and(cc1_n1).ct(),1));
	}
	
	@Test
	public void test_cc1_fail_1_assert() throws Exception {
		assertThrows(AssertionError.class, () -> testCC1Fail(cc1_s1.and(cc1_fail).and(cc1_n1).ct(), 0));
	}
	
	// ---------------------------------------------
	// and2
	
	@Test
	public void test_cc1_and2() throws Exception {
		testCC1(cc1_s1.and2(cc2_int).ct(),
				3,
				t -> {
					assertEquals(S1, t.a);
					assertEquals(N1, t.b);
					assertEquals(N2, t.c);
				});
	}
	
	@Test
	public void test_cc2_and2() throws Exception {
		testCC1(cc2_string.and2(cc2_int).ct(),
				4,
				t -> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(N1, t.c);
					assertEquals(N2, t.d);
				});
	}
	
	@Test
	public void test_cc3_and2() throws Exception {
		testCC1(cc3_string.and2(cc2_int).ct(),
				5,
				t -> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
					assertEquals(N1, t.d);
					assertEquals(N2, t.e);
				});
	}
	
	@Test
	public void test_cc4_and2() throws Exception {
		testCC1(cc4_string.and2(cc2_int).ct(),
				6,
				t -> {
					assertEquals(S1, t.a);
					assertEquals(S2, t.b);
					assertEquals(S3, t.c);
					assertEquals(S4, t.d);
					assertEquals(N1, t.e);
					assertEquals(N2, t.f);
				});
	}
	
	// Test a relatively fat managed object structure (36 objects).
	@Test
	public void test_cc_fat() throws Exception {
		CC1<String> cc1 = cc6_string.cc1((a,b,c,d,e,f) -> a);
		CC1<String> ccf =
				cc1.and(cc1).and(cc1).and(cc1).and(cc1).and(cc1).cc1((a,b,c,d,e,f) -> a);
		testCC1(ccf, 36, s -> assertEquals(S1,s));
	}
	
}
