/**
 * 
 */
package controllers.instruments.io.cng.miseqqcmode;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import controllers.instruments.io.cng.miseqqcmode.MultiInput.HtmlValues;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.dao.api.factory.InputContainerUsedFactory;
import fr.cea.ig.ngl.dao.api.factory.propertyfilevalue.PropertyFileValueFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import play.data.validation.ValidationError;
import validation.ContextValidation;

/**
 * @author aprotat
 *
 */
public class MultiInputTest {
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getGlobalFileKeys()}.
	 */
	@Test
	public void testGetGlobalFileKeys() {
		Set<String> globalFileKeys = new MultiInput().getGlobalFileKeys();
		
		assertNotNull("Expected not null", globalFileKeys);
		assertFalse("Expected not empty", globalFileKeys.isEmpty());
		assertEquals("Expected 1 element", 1, globalFileKeys.size());
		assertTrue("Expected 'Summary.htm'", globalFileKeys.contains("Summary.htm"));
	}

	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getGlobalFilesMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetGlobalFilesMapAbsent() {
		List<PropertyFileValue> pfvs = new ArrayList<>();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> globalFilesMap = new MultiInput().getGlobalFilesMap(pfvs, contextValidation);
		
		assertNotNull(globalFilesMap);
		assertTrue(globalFilesMap.isEmpty());
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getGlobalFilesMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetGlobalFilesMapPresent() {
		List<PropertyFileValue> pfvs = Arrays.asList(
				PropertyFileValueFactory.getRandomPropertyFileValue("Summary.htm"));
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> globalFilesMap = new MultiInput().getGlobalFilesMap(pfvs, contextValidation);
		
		assertNotNull(globalFilesMap);
		assertFalse(globalFilesMap.isEmpty());
		assertEquals(1, globalFilesMap.size());
		assertTrue(globalFilesMap.containsKey("Summary.htm"));
		assertNotNull(globalFilesMap.get("Summary.htm"));
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getGlobalFilesMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetGlobalFilesMapDouble() {
		List<PropertyFileValue> pfvs = Arrays.asList(
				PropertyFileValueFactory.getRandomPropertyFileValue("Summary.htm"),
				PropertyFileValueFactory.getRandomPropertyFileValue("Summary.htm")
				);
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> globalFilesMap = new MultiInput().getGlobalFilesMap(pfvs, contextValidation);
		
		assertNotNull(globalFilesMap);
		assertFalse(globalFilesMap.isEmpty());
		assertTrue("Expected error", contextValidation.hasErrors());
		assertTrue("Expected error key: 'Erreurs fichier'", contextValidation.getErrors().containsKey("Erreurs fichier"));
		String doubleKey = (String) contextValidation.getErrors().get("Erreurs fichier").get(0).arguments().get(0);
		assertEquals("Expected 'Summary.htm'", "Summary.htm", doubleKey);
	}

	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#importGlobalFile(models.laboratory.experiment.instance.Experiment, models.laboratory.common.instance.property.PropertyFileValue, java.lang.String, validation.ContextValidation)}.
	 * @throws Exception 
	 */
	@Test
	public void testImportGlobalFileUnknownKey() throws Exception {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		PropertyFileValue pfv = PropertyFileValueFactory.getRandomPropertyFileValue("Unknown.htm");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		exceptionRule.expect(IllegalStateException.class);
	    exceptionRule.expectMessage("Unknown Global file: Unknown.htm");
	    
		new MultiInput().importGlobalFile(experiment, pfv, "Unknown.htm", contextValidation);
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#importGlobalFile(models.laboratory.experiment.instance.Experiment, models.laboratory.common.instance.property.PropertyFileValue, java.lang.String, validation.ContextValidation)}.
	 * @throws Exception 
	 */
	@Test
	public void testImportGlobalFileExistingValues() throws Exception {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		PropertyFileValue pfv = PropertyFileValueFactory.getRandomPropertyFileValue("Summary.htm");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		exceptionRule.expect(IllegalStateException.class);
	    exceptionRule.expectMessage("HtmlValues should be null on Summary.htm parsing!");
	    
	    MultiInput multiInput = new MultiInput();
	    multiInput.setHtmlValues(new HtmlValues("1", "1"));
	    
	    multiInput.importGlobalFile(experiment, pfv, "Summary.htm", contextValidation);
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#importGlobalFile(models.laboratory.experiment.instance.Experiment, models.laboratory.common.instance.property.PropertyFileValue, java.lang.String, validation.ContextValidation)}.
	 * @throws Exception 
	 */
	@Test
	public void testImportGlobalFileValid() throws Exception {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		PropertyFileValue pfv = MultiInputTestHelper.getSummaryHtmFile();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		MultiInput multiInput = new MultiInput();
	    Experiment experimentReturned = multiInput.importGlobalFile(experiment, pfv, "Summary.htm", contextValidation);
	    assertSame(experiment, experimentReturned);
	    
	    assertNotNull("Expected Html Values", multiInput.getHtmlValues());
	    assertEquals(10007482, multiInput.getHtmlValues().getClusters());
	    assertEquals(9644803, multiInput.getHtmlValues().getClustersPF());
	    assertEquals(96.4D, multiInput.getHtmlValues().getClustersPFpercent(), 0);
	}

	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getPositionsMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetPositionsMapInvalid() {
		List<PropertyFileValue> pfvs = Arrays.asList(
				PropertyFileValueFactory.getRandomPropertyFileValue("Summary.htm"),
				PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G - B6_S65.summary.csv")
				);
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> positionsMap = new MultiInput().getPositionsMap(pfvs, contextValidation);
		
		assertNotNull(positionsMap);
		assertTrue(positionsMap.isEmpty());
		assertTrue("Expected error", contextValidation.hasErrors());
		assertTrue("Expected error key: 'Erreurs fichier'", contextValidation.getErrors().containsKey("Erreurs fichier"));
		assertEquals("Expected 2 errors (1 by file)", 2, contextValidation.getErrors().get("Erreurs fichier").size());
		String htmlKey = (String) contextValidation.getErrors().get("Erreurs fichier").get(0).arguments().get(0);
		assertEquals("Expected 'Summary.htm'", "Summary.htm", htmlKey);
		String invalidParsingKey = (String) contextValidation.getErrors().get("Erreurs fichier").get(1).arguments().get(0);
		assertEquals("Expected 'A005R4G - B6_S65.summary.csv'", "A005R4G - B6_S65.summary.csv", invalidParsingKey);
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getPositionsMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetPositionsMapNotPositionPlate() {
		List<PropertyFileValue> pfvs = Arrays.asList(
				PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-Z1_S57.summary.csv"));
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> positionsMap = new MultiInput().getPositionsMap(pfvs, contextValidation);
		
		assertNotNull(positionsMap);
		assertTrue(positionsMap.isEmpty());
		assertTrue("Expected error", contextValidation.hasErrors());
		assertTrue("Expected error key: 'Erreurs fichier'", contextValidation.getErrors().containsKey("Erreurs fichier"));
		
		ValidationError isPlatePositionError = contextValidation.getErrors().get("Erreurs fichier").get(0);
		
		String lineNum = (String) isPlatePositionError.arguments().get(0);
		assertEquals("Expected line number '?'", "?", lineNum);
		String position = (String) isPlatePositionError.arguments().get(1);
		assertEquals("Expected 'Z1'", "Z1", position);
		int plFormat = (int) isPlatePositionError.arguments().get(2);
		assertEquals("Expected Plate Format: '96'", 96, plFormat);
		
		ValidationError parsingPositionError = contextValidation.getErrors().get("Erreurs fichier").get(1);
		
		String invalidPositionKey = (String) parsingPositionError.arguments().get(0);
		assertEquals("Expected position 'Z1'", "Z1", invalidPositionKey);
		String invalidFileKey = (String) parsingPositionError.arguments().get(1);
		assertEquals("Expected 'A005R4G-Z1_S57.summary.csv'", "A005R4G-Z1_S57.summary.csv", invalidFileKey);
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#getPositionsMap(java.util.List, validation.ContextValidation)}.
	 */
	@Test
	public void testGetPositionsMapValid() {
		PropertyFileValue A1 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A1_S25.summary.csv");
		PropertyFileValue A2 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A2_S33.summary.csv");
		PropertyFileValue A3 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A3_S41.summary.csv");
		PropertyFileValue A4 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A4_S49.summary.csv");
		PropertyFileValue A5 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A5_S57.summary.csv");
		PropertyFileValue A6 = PropertyFileValueFactory.getRandomPropertyFileValue("A005R4G-A6_S65.summary.csv");

		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		Map<String, PropertyFileValue> positionsMap = new MultiInput()
				.getPositionsMap(Arrays.asList(A1, A2, A3, A4, A5, A6), contextValidation);
		
		assertNotNull(positionsMap);
		assertFalse(positionsMap.isEmpty());
		
		assertTrue("Expected key: A1", positionsMap.containsKey("A1"));
		assertEquals("Expected value: A1", A1, positionsMap.get("A1"));
		
		assertTrue("Expected key: A2", positionsMap.containsKey("A2"));
		assertEquals("Expected value: A2", A2, positionsMap.get("A2"));
		
		assertTrue("Expected key: A3", positionsMap.containsKey("A3"));
		assertEquals("Expected value: A3", A3, positionsMap.get("A3"));
		
		assertTrue("Expected key: A4", positionsMap.containsKey("A4"));
		assertEquals("Expected value: A4", A4, positionsMap.get("A4"));
		
		assertTrue("Expected key: A5", positionsMap.containsKey("A5"));
		assertEquals("Expected value: A5", A5, positionsMap.get("A5"));
		
		assertTrue("Expected key: A6", positionsMap.containsKey("A6"));
		assertEquals("Expected value: A6", A6, positionsMap.get("A6"));
		
		assertFalse("Expected valid", contextValidation.hasErrors());
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#importPartialFile(models.laboratory.experiment.instance.Experiment, models.laboratory.common.instance.property.PropertyFileValue, models.laboratory.experiment.instance.InputContainerUsed, validation.ContextValidation)}.
	 * @throws Exception 
	 */
	@Test
	public void testImportPartialFileMissingHtmlValues() throws Exception {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		PropertyFileValue pfv = MultiInputTestHelper.getPositionFile();
		InputContainerUsed icu = InputContainerUsedFactory.getRandomInputContainerUsed();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		exceptionRule.expect(IllegalStateException.class);
	    exceptionRule.expectMessage("HtmlValues should not be null on csv parsing!");
		
		new MultiInput().importPartialFile(experiment, pfv, icu, contextValidation);
	}

	/**
	 * Test method for {@link controllers.instruments.io.cng.miseqqcmode.MultiInput#importPartialFile(models.laboratory.experiment.instance.Experiment, models.laboratory.common.instance.property.PropertyFileValue, models.laboratory.experiment.instance.InputContainerUsed, validation.ContextValidation)}.
	 * @throws Exception 
	 */
	@Test
	public void testImportPartialFile() throws Exception {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		PropertyFileValue pfv = MultiInputTestHelper.getPositionFile();
		InputContainerUsed icu = InputContainerUsedFactory.getRandomInputContainerUsed();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		MultiInput multiInput = new MultiInput();
	    multiInput.setHtmlValues(new HtmlValues("10007482", "9644803"));
		
		Experiment experimentReturned = multiInput.importPartialFile(experiment, pfv, icu, contextValidation);
		assertSame(experiment, experimentReturned);
		
		assertTrue("Expected key: 'RunPFclustersLRM'", icu.experimentProperties.containsKey("RunPFclustersLRM"));
		assertEquals(10007482, icu.experimentProperties.get("RunPFclustersLRM").value);
		
		assertTrue("Expected key: 'RunPFclustersPercentageLRM'", icu.experimentProperties.containsKey("RunPFclustersPercentageLRM"));
		assertEquals(96.4D, icu.experimentProperties.get("RunPFclustersPercentageLRM").value);
		
		assertTrue("Expected key: 'clustersLRM'", icu.experimentProperties.containsKey("clustersLRM"));
		assertEquals(174680, icu.experimentProperties.get("clustersLRM").value);
		
		assertTrue("Expected key: 'clustersPercentageLRM'", icu.experimentProperties.containsKey("clustersPercentageLRM"));	
		assertEquals(0.9D, icu.experimentProperties.get("clustersPercentageLRM").value);
		
		assertTrue("Expected key: 'R1Q30PercentageLRM'", icu.experimentProperties.containsKey("R1Q30PercentageLRM"));
		assertEquals(99.1D, icu.experimentProperties.get("R1Q30PercentageLRM").value);
		
		assertTrue("Expected key: 'R2Q30PercentageLRM'", icu.experimentProperties.containsKey("R2Q30PercentageLRM"));
		assertEquals(98.1D, icu.experimentProperties.get("R2Q30PercentageLRM").value);
		
		assertTrue("Expected key: 'R1MismatchPercentageLRM'", icu.experimentProperties.containsKey("R1MismatchPercentageLRM"));
		assertEquals(0.29D, icu.experimentProperties.get("R1MismatchPercentageLRM").value);
		
		assertTrue("Expected key: 'R2MismatchPercentageLRM'", icu.experimentProperties.containsKey("R2MismatchPercentageLRM"));
		assertEquals(0.33D, icu.experimentProperties.get("R2MismatchPercentageLRM").value);
		
		assertTrue("Expected key: 'R1AlignedPercentageLRM'", icu.experimentProperties.containsKey("R1AlignedPercentageLRM"));
		assertEquals(96.3D, icu.experimentProperties.get("R1AlignedPercentageLRM").value);
		
		assertTrue("Expected key: 'R2AlignedPercentageLRM'", icu.experimentProperties.containsKey("R2AlignedPercentageLRM"));
		assertEquals(96.1D, icu.experimentProperties.get("R2AlignedPercentageLRM").value);
		
		assertTrue("Expected key: 'medianInsertSizeLRM'", icu.experimentProperties.containsKey("medianInsertSizeLRM"));
		assertEquals(365, icu.experimentProperties.get("medianInsertSizeLRM").value);
		
		assertTrue("Expected key: 'SDInsertSizeLRM'", icu.experimentProperties.containsKey("SDInsertSizeLRM"));
		assertEquals(81, icu.experimentProperties.get("SDInsertSizeLRM").value);
		
		assertTrue("Expected key: 'minInsertSizeLRM'", icu.experimentProperties.containsKey("minInsertSizeLRM"));
		assertEquals(127, icu.experimentProperties.get("minInsertSizeLRM").value);
		
		assertTrue("Expected key: 'maxInsertSizeLRM'", icu.experimentProperties.containsKey("maxInsertSizeLRM"));
		assertEquals(671, icu.experimentProperties.get("maxInsertSizeLRM").value);
		
		assertTrue("Expected key: 'diversityLRM'", icu.experimentProperties.containsKey("diversityLRM"));
		assertEquals(48702832D, icu.experimentProperties.get("diversityLRM").value);
		
		assertTrue("Expected key: 'genomeLRM'", icu.experimentProperties.containsKey("genomeLRM"));
		assertEquals("Homo sapiens (UCSC hg19)", icu.experimentProperties.get("genomeLRM").value);
	}

}
