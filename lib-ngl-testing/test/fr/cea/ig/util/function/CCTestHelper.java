package fr.cea.ig.util.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class CCTestHelper {
	
	/**
	 * List of values that have been 'computed'.
	 */
	private static List<Object>      done         = new ArrayList<>();
	
	/**
	 * Stack of values that track the currently managed values. 
	 */
	private static Stack<Object>     doneStack    = new Stack<>();
	
	/**
	 * Each consumer execution is stored in this list so the tests postcondition
	 * is that there is a single element in this list.
	 */
	private static List<Consumer<?>> consumerExec = new ArrayList<>();
	
	/**
	 * Build an action whose execution is monitored.
	 * @param a return value
	 * @return  CC1
	 */
	public static <A> CC1<A> cc1(A a) {
		return nc -> {
			try {
				done.add(a);
				doneStack.push(a);
				nc.accept(a);
			} finally {
				assertTrue(doneStack.size() > 0);
				Object b = doneStack.pop();
				assertSame(a, b);
			}
		};
	}
	
	/**
	 * Tests the execution of a given CC1.
	 * @param cc         CC to test
	 * @param depth      maximum number of managed values
	 * @param c          user code to execute
	 * @throws Exception execution exception
	 */
	public static <A> void testCC1(CC1<A> cc, int depth, Consumer<A> c) throws Exception {
		done.clear();
		doneStack.clear();
		consumerExec.clear();
		try {
			cc.accept(a -> {
				assertEquals(depth, done.size());
				assertEquals(depth, doneStack.size());
				consumerExec.add(c);
				c.accept(a);
			});
		} finally {
			assertEquals(depth, done        .size());
			assertEquals(    0, doneStack   .size());
			assertEquals(    1, consumerExec.size());
		}
	}
	
	public static <A> void testCC1(CC1<A> cc, int depth) throws Exception {
		testCC1(cc, depth, a -> {});
	}
	

	public static <A> void testCC1Fail(CC1<A> cc, int depth) throws Exception {
		done.clear();
		doneStack.clear();
		try {
			cc.accept(a -> {
				assertEquals(depth, done.size());
				assertEquals(depth, doneStack.size());
			});
		} finally {
			assertEquals(depth, done        .size());
			assertEquals(    0, doneStack   .size());
		}
	}
	
	public static final Integer N1 = 1;
	public static final Integer N2 = 2;
	public static final Integer N3 = 3;
	public static final Integer N4 = 4;
	public static final Integer N5 = 5;
	public static final Integer N6 = 6;
	
	public static final String S1 = "toto";
	public static final String S2 = "titi";
	public static final String S3 = "tata";
	public static final String S4 = "tutu";
	public static final String S5 = "lolo";
	public static final String S6 = "lili";
	
	public static final CC1<Integer> cc1_n1 = cc1(N1);
	public static final CC1<Integer> cc1_n2 = cc1(N2);
	public static final CC1<Integer> cc1_n3 = cc1(N3);
	public static final CC1<Integer> cc1_n4 = cc1(N4);
	public static final CC1<Integer> cc1_n5 = cc1(N5);
	public static final CC1<Integer> cc1_n6 = cc1(N6);
		
	public static final CC1<String>  cc1_s1 = cc1(S1); 
	public static final CC1<String>  cc1_s2 = cc1(S2); 
	public static final CC1<String>  cc1_s3 = cc1(S3); 
	public static final CC1<String>  cc1_s4 = cc1(S4);
	public static final CC1<String>  cc1_s5 = cc1(S5); 
	public static final CC1<String>  cc1_s6 = cc1(S6); 

	public static final CC2<Integer,Integer>                                 cc2_int = cc1_n1.and(cc1_n2);
	public static final CC3<Integer,Integer,Integer>                         cc3_int = cc2_int.and(cc1_n3);
	public static final CC4<Integer,Integer,Integer,Integer>                 cc4_int = cc3_int.and(cc1_n4);
	public static final CC5<Integer,Integer,Integer,Integer,Integer>         cc5_int = cc4_int.and(cc1_n5);
	public static final CC6<Integer,Integer,Integer,Integer,Integer,Integer> cc6_int = cc5_int.and(cc1_n6);
	
	public static final CC2<String,String>                             cc2_string = cc1_s1.and(cc1_s2);
	public static final CC3<String,String,String>                      cc3_string = cc2_string.and(cc1_s3);
	public static final CC4<String,String,String,String>               cc4_string = cc3_string.and(cc1_s4);
	public static final CC5<String,String,String,String,String>        cc5_string = cc4_string.and(cc1_s5);
	public static final CC6<String,String,String,String,String,String> cc6_string = cc5_string.and(cc1_s6);
	
	static class CC1FailException extends Exception { private static final long serialVersionUID = 1L; }
	public static final CC1<Class<Void>> cc1_fail = nc -> { throw new CC1FailException(); };
	
	public static <A> CC1<A> cc1_fail(A a) { 
		return nc -> { throw new CC1FailException(); };
	}

}
