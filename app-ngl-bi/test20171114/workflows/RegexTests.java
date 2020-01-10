package workflows;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class RegexTests {
	@Test
	public void regexpWorkflow() {
		assertThat("AWK_DOSW_1_A8GKJ.IND16".matches("^.+_.+F_.+_.+$")).isEqualTo(false);
		assertThat("AWK_DOSF_1_A8GKJ.IND16".matches("^.+_.+F_.+_.+$")).isEqualTo(true);
		assertThat("AWK_AAAOSF_1_A8GKJ.IND16".matches("^.+_.+F_.+_.+$")).isEqualTo(true);
		
	}
	
	/*
BCI_AOSN_1_1_A7PDE.IND19
BCI_ADNOSF_1_1_A7B5R.IND32
BCI_AHEOSF_1_1_A7B5R.IND25
BCI_AHNOSF_1_1_A7B5R.IND26
BCI_AIBOSF_1_1_A7B5R.IND27
BCI_AKHOSF_1_1_A7B5R.IND30
BCI_AQQOSF_1_1_A7B5R.IND33
BCI_BAMOSF_1_1_A7B5R.IND28
BCI_BIDOSF_1_1_A7B5R.IND34
BCI_GVOSF_1_1_A7B5R.IND31
BCI_INOSF_1_1_A7B5R.IND29
	 */
	
}
