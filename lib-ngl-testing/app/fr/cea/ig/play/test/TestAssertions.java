package fr.cea.ig.play.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.function.Consumer;

import org.junit.Assert;

import fr.cea.ig.util.function.C0;

/**
 * Assertions that were not found in the JUnit assert class.
 * <br>Methods of this class should be removed and replaced by official 
 * JUnit methods when/if available.
 * 
 * @author vrd
 *
 */
public class TestAssertions {

	/**
	 * Assert that the iterable contains a single element that does not 
	 * trigger exception when supplied to the consumer.
	 * @param <A> element type
	 * @param i   iterable to test
	 * @param c   consumer that fails by throwing exceptions
	 */
	public static <A> void assertOne(Iterable<A> i, Consumer<A> c) {
		// fail fast implementation, do not iterate over the whole iterable
		assertNotNull(i);
		Iterator<A> it = i.iterator();
		assertTrue("no element", it.hasNext());
		c.accept(it.next());
		assertFalse("more than one element", it.hasNext());
	}

	/**
	 * Assert that the iterable contains a single element.
	 * @param <A> element type
	 * @param i   iterable to test
	 */
	public static <A> void assertOne(Iterable<A> i) {
		assertOne(i, a -> {});
	}
	
	/**
	 * Assert that the iterable is not null and has at least one element. 
	 * @param i   iterable to test
	 */
	public static void assertNotEmpty(Iterable<?> i) {
		assertNotNull(i);
		assertTrue(i.iterator().hasNext());
	}
	
	/**
	 * Asserts that the consumer fails by throwing an {@link java.lang.AssertionError}.
	 * @param c code to test for failure
	 */
	public static void assertFail(C0 c) {
		assertThrows(AssertionError.class, c);
	}
	
	/**
	 * Asserts that the code to execute throws an instance of a given exception.
	 * @param exceptionClass expected exception class
	 * @param c              code to test for failure
	 */
	public static void assertThrows(Class<? extends Throwable> exceptionClass, C0 c) {
		assertThrowable(t -> Assert.assertEquals(exceptionClass, t.getClass()), c);
	}
	
	/**
	 * Assert that the 'predicate' (exception consumer that fails by throwing an
	 * exception) does not fail for the exception caught.
	 * @param p exception consumer that fails by throwing an exception
	 * @param c code to test for an exception
	 */
	public static void assertThrowable(Consumer<Throwable> p, C0 c) {
		try {
			c.accept();
		} catch (Throwable e) {
			p.accept(e);
			return;
		}
		fail();
	}
	
}
