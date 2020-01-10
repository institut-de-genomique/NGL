package fr.cea.ig.lfw.reflect;

import static fr.cea.ig.play.test.TestAssertions.assertThrows;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Optional;

import org.junit.Test;

import fr.cea.ig.lfw.reflect.ReflectionUtils;

public class ReflectionTest {

	private static final HashMap<String,String[]> NO_PARAMETER = new HashMap<>();
	private static final String S_hello = "hello";
	private static final HashMap<String,String[]> AS_hello = m("f0", v(S_hello));
	
	private static int[] a(int... vs) {
		return vs;
	}
	
	private static String[] v(String... vs) {
		return vs;
	}
	
	private static HashMap<String,String[]> m(String key, String[] values) {
		HashMap<String,String[]> m = new HashMap<>();
		m.put(key,  values);
		return m;
	}
	
	private static HashMap<String,String[]> m(String k0, String[] v0,
			                                  String k1, String[] v1) {
		HashMap<String,String[]> m = m(k0, v0);
		m.put(k1, v1);
		return m;
	}
	
	private static HashMap<String,String[]> m(String k0, String[] v0,
                                              String k1, String[] v1,
                                              String k2, String[] v2) {
		HashMap<String,String[]> m = m(k0, v0, k1, v1);
		m.put(k2, v2);
		return m;
	}

	private static HashMap<String,String[]> m(String k0, String[] v0,
            	                              String k1, String[] v1,
            	                              String k2, String[] v2,
            	                              String k3, String[] v3) {
		HashMap<String,String[]> m = m(k0, v0, k1, v1, k2, v2);
		m.put(k3, v3);
		return m;
	}

	private static HashMap<String,String[]> m(String k0, String[] v0,
                                              String k1, String[] v1,
                                              String k2, String[] v2,
                                              String k3, String[] v3,
                                              String k4, String[] v4) {
		HashMap<String,String[]> m = m(k0, v0, k1, v1, k2, v2, k3, v3);
		m.put(k4, v4);
		return m;
	}

	static class S {
		public String f0;
	}
	
	static class SA {
		public String[] f0;
	}
	
	static class I {
		public int f0;
	}
	
	static class IA {
		public int[] f0;
	}

	static class SO {
		public Optional<String> f0;
	}
	
	static class B {
		public boolean f0;
	}
	
	static class FA {
		public float[] f0;
	}
	
	static class DA {
		public double[] f0;
	}
	
	static class SIBSO {
		public String           f0;
		public int              f1;
		public boolean          f2;
		public Optional<String> f3;
	}
	
	static class M_static {
		public static String f0;
	}
	
	static class M_protected {
		protected String f0;
	}
	
	static class M_private {
		@SuppressWarnings("unused")
		private String f0;
	}
	
	static class M_inherited extends M_static {
	}
	
	// ----------------------------------------------------------
	// no field object tests
	
	@Test
	public void populateEmpty_success() throws Exception {
		ReflectionUtils.readInstance(Object.class, NO_PARAMETER);
	}
	
	@Test
	public void populateEmpty_fail() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(Object.class, m("key", v("value"))));
	}

	// --------------------------------------------------------
	// string field tests
	
	@Test
	public void populateS_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(S.class, NO_PARAMETER));
	}

	@Test
	public void populateS_success() throws Exception {
		assertEquals(S_hello, ReflectionUtils.readInstance(S.class, AS_hello).f0);
	}
	
	@Test
	public void populateS_failBlankArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(S.class, m("f0", v(" "))));
	}
	
	@Test
	public void populateS_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(S.class, m("f0", v("v0"),
				                                                                           "f1", v("v1"))));
	}
	
	// --------------------------------------------------------
	// string array field tests
	
	@Test
	public void populateSA_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SA.class, NO_PARAMETER));
	}

	@Test
	public void populateSA_success() throws Exception {
		assertArrayEquals(v("value","value") , ReflectionUtils.readInstance(SA.class, m("f0", v("value", "value"))).f0);
	}
	
	@Test
	public void populateSA_failBlankArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SA.class, m("f0", v("s", " "))));
	}
	
	@Test
	public void populateSA_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SA.class, m("f0", v("v0"),
				                                                                        "f1", v("v1"))));
	}
	
	// ---------------------------------------------------------
	// int field test
	
	@Test
	public void populateI_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(I.class, NO_PARAMETER));
	}

	@Test
	public void populateI_success() throws Exception {
		assertEquals(17, ReflectionUtils.readInstance(I.class, m("f0", v("17"))).f0);
	}
	
	@Test
	public void populateI_failBadArg() {
		assertThrows(NumberFormatException.class, () -> ReflectionUtils.readInstance(I.class, m("f0", v("not a number"))));
	}
	
	@Test
	public void populateI_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(I.class, m("f0", v("v0"),
				                                                                           "f1", v("v1"))));
	}
	
	// ---------------------------------------------------------
	// int field test
	
	@Test
	public void populateIA_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(IA.class, NO_PARAMETER));
	}

	@Test
	public void populateIA_success() throws Exception {
		assertArrayEquals(a(17,42), ReflectionUtils.readInstance(IA.class, m("f0", v("17", "42"))).f0);
	}
	
	@Test
	public void populateIA_failBadArg() {
		assertThrows(NumberFormatException.class, () -> ReflectionUtils.readInstance(IA.class, m("f0", v("22", "not a number"))));
	}
	
	@Test
	public void populateIA_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(IA.class, m("f0", v("v0"),
				                                                                            "f1", v("v1"))));
	}
		
	// --------------------------------------------------------
	// Optional string
	
	@Test
	public void populateSO_underArg() throws Exception {
		assertFalse(ReflectionUtils.readInstance(SO.class, NO_PARAMETER).f0.isPresent());
	}

	@Test
	public void populateSO_success() throws Exception {
		assertEquals("42", ReflectionUtils.readInstance(SO.class, m("f0", v("42"))).f0.get());
	}
	
	@Test
	public void populateSO_failBadArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SO.class, m("f0", v("a", "b"))));
	}
	
	@Test
	public void populateSO_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SO.class, m("f0", v("v0"),
				                                                                            "f1", v("v1"))));
	}

	// ----------------------------------------------------------
	// Boolean
	
	@Test
	public void populateB_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(B.class, NO_PARAMETER));
	}
	
	@Test
	public void populateB_noValue() throws Exception {
		assertTrue(ReflectionUtils.readInstance(B.class, m("f0", v())).f0);
	}
	
	@Test
	public void populateB_success() throws Exception {
		assertTrue(ReflectionUtils.readInstance(B.class, m("f0", v("true"))).f0);
	}
	
	@Test
	public void populateB_success_yes() throws Exception {
		assertTrue(ReflectionUtils.readInstance(B.class, m("f0", v("yes"))).f0);
	}
	
	@Test
	public void populateB_success_false() throws Exception {
		assertFalse(ReflectionUtils.readInstance(B.class, m("f0", v("FaLsE"))).f0);
	}
	
	@Test
	public void populateB_failBadArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(B.class, m("f0", v("a"))));
	}
	
	@Test
	public void populateB_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(B.class, m("f0", v("v0"),
				                                                                           "f1", v("v1"))));
	}
	
	// ----------------------------------------------------------
	// numeric arrays 
	
	@Test
	public void populateFA_success() throws Exception {
		assertEquals(0, 0.5f, ReflectionUtils.readInstance(FA.class, m("f0", v("0.5"))).f0[0]);
	}
	
	@Test
	public void populateDA_success() throws Exception {
		assertEquals(0, 0.5, ReflectionUtils.readInstance(DA.class, m("f0", v("0.5"))).f0[0]);
	}
	
	// ----------------------------------------------------------
	// Compound
	
	@Test
	public void populateSIBSO_underArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SIBSO.class, NO_PARAMETER));
	}
	
	@Test
	public void populateSIBSO_underArg1() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SIBSO.class, m("f0", v())));
	}
	
	@Test
	public void populateSIBSO_underArg2() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SIBSO.class, m("f0", v(),
				                                                                               "f1", v())));
	}
	
	@Test
	public void populateSIBSO_underArg3() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SIBSO.class, m("f0", v(),
                                                                                               "f1", v(),
                                                                                               "f2", v())));
	}
	
	@Test
	public void populateSIBSO_success() throws Exception {
		SIBSO v = ReflectionUtils.readInstance(SIBSO.class, m("f0", v("hello"),
                			                                  "f1", v("42"),
                			                                  "f2", v("true"),
                			                                  "f3", v("opt")));
		assertEquals("hello", v.f0);
		assertEquals(42,      v.f1);
		assertTrue  (         v.f2);
		assertEquals("opt",   v.f3.get());
	}
	
	@Test
	public void populateSIBSO_overArg() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(SIBSO.class, m("f0", v(),
                                                                                               "f1", v(),
                                                                                               "f2", v(),
                                                                                               "f3", v(),
                                                                                               "f4", v())));
	}
	
	// ----------------------------------------------------------
	// Access
	
	@Test
	public void modifierStatic() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(M_static.class, AS_hello));
	}
	
	@Test
	public void modifierProtected() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(M_protected.class, AS_hello));
	}
	
	@Test
	public void modifierPrivate() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(M_private.class, AS_hello));
	}
	
	@Test
	public void modifierInherited() {
		assertThrows(RuntimeException.class, () -> ReflectionUtils.readInstance(M_inherited.class, AS_hello));
	}
	
}
