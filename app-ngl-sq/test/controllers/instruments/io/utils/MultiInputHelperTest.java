/**
 * 
 */
package controllers.instruments.io.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fest.util.Collections;
import org.junit.Test;

import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

/**
 * @author aprotat
 *
 */
public class MultiInputHelperTest {

	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#getIcuPositionsMap(models.laboratory.experiment.instance.Experiment)}.
	 */
	@Test
	public void testGetIcuPositionsMap() {
		
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		
		Map<String, InputContainerUsed> icuPositionsMap = MultiInputHelper.getIcuPositionsMap(experiment);
		
		assertEquals("Wrong icuPositionsMap size", 96, icuPositionsMap.size());
		
		for(String character: new String[] {"A", "B", "C", "D", "E", "F", "G", "H"}) {
			for(int i=1; i <= 12; i++) {
				String position = character + i;
				assertTrue("icuPositionsMap doesn't contains key '" + position + "'", 
						icuPositionsMap.containsKey(position));
				assertNotNull("icuPositionsMap doesn't contains value for position '" + position + "'", 
						icuPositionsMap.get(position));
				assertTrue("icuPositionsMap doesn't contains ICU for position '" + position + "'", 
						icuPositionsMap.get(position) instanceof InputContainerUsed);
			}
		}
	}

	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#handleMissingPositions(java.util.Set, java.util.Set, validation.ContextValidation)}.
	 */
	@Test
	public void testHandleMissingPositionsValid() {
		
		Set<String> icuPositions = Collections.set("A1", "A2", "B1", "B2", "C3", "C4");
		Set<String> filePositions = Collections.set("A1", "A2", "B1", "B2", "C3", "C4");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		MultiInputHelper.handleMissingPositions(icuPositions, filePositions, contextValidation);
		
		assertFalse("Identical sets should return true", contextValidation.hasErrors());
		
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#handleMissingPositions(java.util.Set, java.util.Set, validation.ContextValidation)}.
	 */
	@Test
	public void testHandleMissingPositionsInvalid() {
		
		Set<String> icuPositions = Collections.set("A1", "A2", "B1", "B2", "C3", "C4");
		Set<String> filePositions = Collections.set("A1", "B2", "C3");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		MultiInputHelper.handleMissingPositions(icuPositions, filePositions, contextValidation);
		
		assertTrue("Missing positions expected", contextValidation.hasErrors());
		assertNotNull("Expected error key: 'Erreurs fichier'", contextValidation.getErrors().get("Erreurs fichier"));
		String missingPositions = (String) contextValidation.getErrors().get("Erreurs fichier").get(0).arguments().get(0);
		assertEquals("Expected missing positions: 'A2, B1, C4', got: '" + missingPositions + "'", "A2, B1, C4", missingPositions);
		
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#handleGlobalFiles(models.laboratory.experiment.instance.Experiment, controllers.instruments.io.utils.AbstractMultiInput, java.util.Map, validation.ContextValidation)}.
	 */
	@Test
	public void testHandleGlobalFilesEmptyKey() {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		AbstractMultiInput inputHandler = mock(AbstractMultiInput.class);
		Map<String, PropertyFileValue> globalFilesMap = new HashMap<>();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(inputHandler.getGlobalFileKeys()).thenReturn(Collections.set());
		
		try {
			MultiInputHelper.handleGlobalFiles(experiment, inputHandler, globalFilesMap, contextValidation);
			verify(inputHandler, times(0)).importGlobalFile(any(), any(), any(), any());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		assertFalse("Expected error", contextValidation.hasErrors());
	}

	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#handleGlobalFiles(models.laboratory.experiment.instance.Experiment, controllers.instruments.io.utils.AbstractMultiInput, java.util.Map, validation.ContextValidation)}.
	 */
	@Test
	public void testHandleGlobalFilesEmptyMap() {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		AbstractMultiInput inputHandler = mock(AbstractMultiInput.class);
		Map<String, PropertyFileValue> globalFilesMap = new HashMap<>();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		when(inputHandler.getGlobalFileKeys()).thenReturn(Collections.set("Summary.htm"));
		
		try {
			MultiInputHelper.handleGlobalFiles(experiment, inputHandler, globalFilesMap, contextValidation);
			verify(inputHandler, times(0)).importGlobalFile(any(), any(), any(), any());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		assertTrue("Expected error", contextValidation.hasErrors());
		assertTrue("Expected error key: 'Erreurs fichier'", contextValidation.getErrors().containsKey("Erreurs fichier"));
		String missingKey = (String) contextValidation.getErrors().get("Erreurs fichier").get(0).arguments().get(0);
		assertEquals("Expected 'Summary.htm'", "Summary.htm", missingKey);
	}
	
	/**
	 * Test method for {@link controllers.instruments.io.utils.MultiInputHelper#handleGlobalFiles(models.laboratory.experiment.instance.Experiment, controllers.instruments.io.utils.AbstractMultiInput, java.util.Map, validation.ContextValidation)}.
	 */
	@Test
	public void testHandleGlobalFilesValid() {
		Experiment experiment = ExperimentFactory.getRandomExperimentMiseq();
		AbstractMultiInput inputHandler = mock(AbstractMultiInput.class);
		Map<String, PropertyFileValue> globalFilesMap = new HashMap<>();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);
		
		globalFilesMap.put("Summary.htm", new PropertyFileValue());
		
		try {
			when(inputHandler.getGlobalFileKeys()).thenReturn(Collections.set("Summary.htm"));
			when(inputHandler.importGlobalFile(any(Experiment.class), any(PropertyFileValue.class), anyString(), any(ContextValidation.class)))
			.thenReturn(experiment);
			
			MultiInputHelper.handleGlobalFiles(experiment, inputHandler, globalFilesMap, contextValidation);
			
			verify(inputHandler, times(1)).importGlobalFile(any(), any(), any(), any());
			assertFalse("Expected valid", contextValidation.hasErrors());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
