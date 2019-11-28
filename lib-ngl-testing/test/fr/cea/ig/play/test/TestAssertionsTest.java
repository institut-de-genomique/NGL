package fr.cea.ig.play.test;

import static fr.cea.ig.play.test.TestAssertions.assertFail;
import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestAssertionsTest {
	
	private static List<Integer> intList(Integer... is) {
		return asList(is);
	}

	@Test
	public void testAssertOne_Empty() {
		assertFail(() -> assertOne(asList()));
	}
	
	@Test
	public void testAssertOne_one() {
		assertOne(asList(1));
	}
	
	@Test
	public void testAssertOne_one_success() {
		assertOne(intList(2), i -> { assertEquals(2, i.intValue()); });
	}
	
	@Test
	public void testAssertOne_one_fail() {
		assertFail(() -> assertOne(intList(2), i -> { assertEquals(3, i.intValue()); }));
	}
	
	@Test
	public void testAssertOne_two_fail() {
		assertFail(() -> assertOne(intList(2,3)));
	}
	
	@Test
	public void testAssertOne_ten_fail() {
		assertFail(() -> assertOne(intList(2,3,4,5,6,7,8,8,8,9)));
	}
	
	@Test
	public void testThrows() {
		assertFail(() -> assertFail(() -> {}));
	}
	
}
