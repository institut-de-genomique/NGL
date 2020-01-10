package fr.cea.ig.lfw.utils;

import static fr.cea.ig.lfw.utils.Iterables.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

public class IterablesTest {

	@Test
	public void asStringTest() {
		ZenIterable<String> i = zen(Arrays.asList("Hello",", ","World"," !"));
		assertEquals("Hello, World !", i.asString());
	}
	
	@Test
	public void append() {
		ZenIterable<Integer> i = range(1,3).append(4);
		assertEquals("1234", i.asString());
	}
	
	@Test
	public void prepend() {
		ZenIterable<Integer> i = range(1,3).prepend(0);
		assertEquals("0123", i.asString());
	}
	
	@Test
	public void map() {
		ZenIterable<String> i = range(1,3).map(x -> "#" + x.toString());
		assertEquals("#1#2#3", i.asString());
	}
	
	@Test
	public void count() {
		ZenIterable<Integer> i = range(1,3);
		assertEquals(6,Iterables.sum(i));
	}
	
	@Test 
	public void foldIn() {
		ZenIterable<Integer> i = range(1,3).foldlIn(0, (a,b) -> a+b);
		assertEquals("1236", i.asString());
	}
	
	@Test
	public void filter() {
		ZenIterable<Integer> i = range(1,10).filter(x -> x < 4);
		assertEquals("123", i.asString());
	}
	
	@Test
	public void intercalate() {
		ZenIterable<Integer> i = range(1,3).intercalate(0);
		assertEquals("10203", i.asString());
	}
	
	@Test
	public void skip() {
		ZenIterable<Integer> i = range(1,6).skip(3);
		assertEquals("456", i.asString());
	}
	
	@Test
	public void skipMore( ) {
		ZenIterable<Integer> i = range(1,6).skip(99);
		assertEquals("", i.asString());		
	}
	
	@Test
	public void surround() {
		ZenIterable<String> i = repeat("A").take(3).surround("(", ",", ")");
		assertEquals("(A,A,A)", i.asString());
	}

	@Test
	public void rangeInc() {
		ZenIterable<Integer> i = range(1,3);
		assertEquals("123", i.asString());
	}
	
	@Test
	public void rangeInc2() {
		ZenIterable<Integer> i = range(1,10);
		assertEquals("12345678910", i.asString());
	}
	
	@Test
	public void rangeDec() {
		ZenIterable<Integer> i = range(3,1);
		assertEquals("321", i.asString());		
	}
	
	@Test
	public void repeatTake() {
		ZenIterable<String> i = repeat("A").take(5);
		assertEquals("AAAAA", i.asString());
	}
	
	@Test
	public void zip() {
		ZenIterable<ImmutablePair<Integer,String>> i = range(1,3).zip(repeat("A"));
		ZenIterable<String> j = i.map(p -> "(" + p.left + "," + p.right + ")");
		assertEquals("(1,A)(2,A)(3,A)", j.asString());
	}
	
	@Test
	public void unique() {
		ZenIterable<String> i = repeat("A").take(3);
		ZenIterable<String> j = i.unique();
		assertEquals("A", j.asString());
	}
	
}
