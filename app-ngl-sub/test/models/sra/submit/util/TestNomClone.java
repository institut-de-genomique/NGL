package models.sra.submit.util;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class TestNomClone {
	public void assertGoodNameClone(String clone) {
//		assertTrue(clone.matches(SraCodeHelper.cloneRegExp));
		assertTrue(SraCodeHelper.isGoodNameClone(clone));
	}
	public void assertBadNameClone(String clone) {
//		assertFalse(clone.matches(SraCodeHelper.cloneRegExp));
		assertFalse(SraCodeHelper.isGoodNameClone(clone));
	}
	@Test
	public void testRegExp_0() {
		assertGoodNameClone("NOM-DE_CLONE_0");
	}
	@Test
	public void testRegExp_1() {
		assertGoodNameClone("NOM_DE_CLONE_1");
	}
	
	@Test
	public void testRegExp_2() {
		assertBadNameClone("?NOM_DE_CLONE_1");
	}
	@Test
	public void testRegExp_3() {
		assertBadNameClone("NOM_DE_CLONE_1!");
	}	
	@Test
	public void testRegExp_4() {
		assertBadNameClone("NOM_DE  CLONE_1!");
	}	
	@Test
	public void testRegExp_5() {
		assertBadNameClone("NOM_Dé  CLONE_1!");
	}	
	@Test
	public void testRegExp_6() {
		assertBadNameClone("NOM.DeCLONE_!");
	}	
	
}
