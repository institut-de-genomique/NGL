package fr.cea.ig.util.function;

import static fr.cea.ig.play.test.TestAssertions.assertThrows;
import static fr.cea.ig.util.function.CCTestHelper.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.cea.ig.util.function.CCTestHelper.CC1FailException;

public class TestCCActions {
	
	@Test
	public void test_cc_unwrap() throws Exception {
		List<CC1<Integer>> ccs = Arrays.asList(cc1_n1,cc1_n2,cc1_n3);
		testCC1(CCActions.unwrap(ccs),
				ccs.size(), // as many actions are executed as there are in the original thing
				l -> {
					assertEquals(ccs.size(), l.size());
					assertEquals(N1, l.get(0));
					assertEquals(N2, l.get(1));
					assertEquals(N3, l.get(2));
				});
	}
	
	@Test
	public void test_cc_unwrap_fail() throws Exception {
		List<CC1<Integer>> ccs = Arrays.asList(cc1_n1,cc1_n2,cc1_fail(42),cc1_n3,cc1_n4);
		// Execution of cc from list is aborted when the fail action i executed
		// so the execution count is 2.
		assertThrows(CC1FailException.class, () -> testCC1Fail(CCActions.unwrap(ccs), 2));
	}
	
	@Test
	public void test_cc_unwrap_fail_count() throws Exception {
		List<CC1<Integer>> ccs = Arrays.asList(cc1_n1,cc1_n2,cc1_fail(42),cc1_n3,cc1_n4);
		// Execution of cc from list is aborted when the fail action i executed
		// so the execution count is 2 so this triggers an assertion error and not
		// the CC1 fail.
		assertThrows(AssertionError.class, () -> testCC1Fail(CCActions.unwrap(ccs), 3));
	}
	
}
