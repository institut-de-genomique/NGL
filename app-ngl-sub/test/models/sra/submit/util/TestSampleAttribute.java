package models.sra.submit.util;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import validation.ContextValidation;
import validation.sra.SraValidationHelper;

public class TestSampleAttribute {
	
	public void assertGoodAttributes(String attributesValue) {
		String user = "william";
		String nameField = "attributes";
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);
		assertTrue(SraValidationHelper.newValidateAttributesRequired(contextValidation, nameField, attributesValue));
	}
	
	public void assertBadAttributes(String attributesValue) {
		String user = "william";
		String nameField = "attributes";
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);
		assertFalse(SraValidationHelper.newValidateAttributesRequired(contextValidation, nameField, attributesValue));	
	}
	
	@Test
	public void testRegExp_1() {
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG><VALUE>titi</VALUE></SAMPLE_ATTRIBUTE>");
	}
	@Test
	public void testRegExp_2() {
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG>" 
				+ "<VALUE>titi</VALUE></SAMPLE_ATTRIBUTE>");
	}
	@Test
	public void testRegExp_3() {
		assertGoodAttributes("<SAMPLE_ATTRIBUTE>    <TAG>     Clone   </TAG>" 
				+ "\n"
				+ "        <VALUE>titi</VALUE></SAMPLE_ATTRIBUTE>");
	}
	@Test
	public void testRegExp_4() {
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG><VALUE>cloneTiti</VALUE></SAMPLE_ATTRIBUTE>" 
				+ "\n"
				+ "<SAMPLE_ATTRIBUTE><TAG>Gene</TAG><VALUE>geneTiti</VALUE></SAMPLE_ATTRIBUTE>");
	}
	@Test
	public void testRegExp_5() {
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG><VALUE>cloneTiti</VALUE></SAMPLE_ATTRIBUTE>" 
				+ "\n" 
				+ "<SAMPLE_ATTRIBUTE><TAG>PROFONDEUR</TAG><VALUE>3</VALUE><UNITS>m</UNITS></SAMPLE_ATTRIBUTE>"
				+ "\n" 
				+ "<SAMPLE_ATTRIBUTE><TAG>Gene</TAG><VALUE>geneTiti</VALUE></SAMPLE_ATTRIBUTE>\n");
	}
	@Test
	public void testRegExp_6() {
		assertBadAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG><VALUE>titi</VALUE><SAMPLE_ATTRIBUTE>");
	}
	@Test
	public void testRegExp_7() {	
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>mixed sample</TAG><VALUE>in silico mixture</VALUE></SAMPLE_ATTRIBUTE><SAMPLE_ATTRIBUTE><TAG>member sample</TAG><VALUE>ERS999586, ERS999587, ERS999588, ERS999589</VALUE></SAMPLE_ATTRIBUTE><SAMPLE_ATTRIBUTE><TAG>Environment (Biome)</TAG><VALUE>marine biome (ENVO:00000447)</VALUE></SAMPLE_ATTRIBUTE><SAMPLE_ATTRIBUTE><TAG>Project Name</TAG><VALUE>Tara Oceans expedition (2009-2013)</VALUE></SAMPLE_ATTRIBUTE><SAMPLE_ATTRIBUTE><TAG>ENA-CHECKLIST</TAG><VALUE>ERC000011</VALUE></SAMPLE_ATTRIBUTE>");
 		
 		}
	@Test
	public void testRegExp_8() {		
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>geo_loc_name</TAG><VALUE>Saclay Essone France</VALUE></SAMPLE_ATTRIBUTE>\n");
	}
	@Test
	public void testRegExp_9() {		
		assertGoodAttributes("<SAMPLE_ATTRIBUTE><TAG>titi</TAG><VALUE>in  fsq sdfqd dsfqdf </VALUE></SAMPLE_ATTRIBUTE>\n");
	}
	@Test
	public void testRegExp_10() {
		assertBadAttributes("<SAMPLE_ATTRIBUTE><TAG>Clone</TAG><VALUE>Saclay   	Essone France     </VALUE><SAMPLE_ATTRIBUTE>");
	}
}
