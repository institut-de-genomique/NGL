package ngl.data.data;

import org.junit.Test;

import ngl.data.Global;

// Test data population switch. This is not a proper test as it is a speed
// estimation test.
public class DataSwitchTest {

	private void testCNS() throws Exception {
		Global.afCNS.run(app -> {});
	}
	
	private void testCNG() throws Exception {
		Global.afCNG.run(app -> {});
	}
	
	@Test
	public void test00() throws Exception {
		testCNS();
	}
	
	@Test
	public void test02() throws Exception {
		testCNS();
	}
	
	@Test
	public void test04() throws Exception {
		testCNS();
	}
	
	@Test
	public void test06() throws Exception {
		testCNS();
	}
	
	@Test
	public void test08() throws Exception {
		testCNS();
	}
	
	@Test
	public void test01() throws Exception {
		testCNG();
	}
	
	@Test
	public void test03() throws Exception {
		testCNG();
	}
	
	@Test
	public void test05() throws Exception {
		testCNG();
	}
	
	@Test
	public void test07() throws Exception {
		testCNG();
	}
	
	@Test
	public void test09() throws Exception {
		testCNG();
	}
	
}
