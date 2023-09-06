package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.mongojack.DBUpdate.Builder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.AnalysesDAO;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.api.factory.AnalysisFactory;
import fr.cea.ig.ngl.dao.api.factory.ReadsetFactory;
import fr.cea.ig.ngl.dao.api.factory.ValuationFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import workflows.analyses.AnalysisWorkflows;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.AnalysisValidationHelper;
import validation.run.instance.FileValidationHelper;
import validation.run.instance.TreatmentValidationHelper;
import validation.utils.ValidationHelper;

/**
 * Test de l'API de l'entité ANALYSIS.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, AnalysisValidationHelper.class, ValidationHelper.class, TreatmentValidationHelper.class, FileValidationHelper.class })
public class AnalysisAPITest {
	
	private final AnalysesDAO analysesDAO = Mockito.mock(AnalysesDAO.class);

	private final AnalysisWorkflows workflows = Mockito.mock(AnalysisWorkflows.class);
	
	private final ReadSetsDAO readSetDAO = Mockito.mock(ReadSetsDAO.class);

	private final AnalysesAPI analysesAPI = new AnalysesAPI(analysesDAO, workflows, readSetDAO);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(AnalysisValidationHelper.class);
		PowerMockito.mockStatic(ValidationHelper.class);
		PowerMockito.mockStatic(TreatmentValidationHelper.class);
		PowerMockito.mockStatic(FileValidationHelper.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary", any(ContextValidation.class), any(Analysis.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateCodePrimary", any(ContextValidation.class), any(Analysis.class), any(String.class));
			PowerMockito.doNothing().when(AnalysisValidationHelper.class, "validateAnalysisTypeRequired", any(ContextValidation.class), any(String.class), any(Map.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateValuationRequired", any(ContextValidation.class), any(String.class), any(Valuation.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired", any(ContextValidation.class), any(TraceInformation.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		Analysis analysis = AnalysisFactory.getRandomAnalysis();
		analysis.code = randomCode;
		
		when(analysesDAO.findByCode(randomCode)).thenReturn(analysis);
		
		Analysis analysisGetApi = analysesAPI.get(randomCode);
		
		assertNotNull(analysisGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(analysisGetApi.code));
	}
	
	@Test
	public void testCreateValid() {
		String randomCode = UUID.randomUUID().toString();

		Analysis analysis = AnalysisFactory.getRandomAnalysis();
		analysis.code = randomCode;

		/*
		 * NGL-3741: Do not allow duplicates in projectCodes!! change List to Set
		 * 		analysis.projectCodes = new ArrayList<>();
		 */
		analysis.projectCodes = new TreeSet<>();
		ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date());


		when(readSetDAO.findByCode(analysis.masterReadSetCodes.get(0))).thenReturn(readset);
		when(analysesDAO.save(analysis)).thenReturn(analysis);

		try {
			Analysis analysisCreateAPI = analysesAPI.create(analysis, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", analysisCreateAPI.code.equals(analysis.code));
			assertNotNull(analysisCreateAPI);
		} catch (APIValidationException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}		
	}
	
	@Test
	public void testCreateInvalid() {
		String randomCode = UUID.randomUUID().toString();
		
		// On tente de créer une analyse avec un _id déjà existant.
		Analysis analysis = mock(Analysis.class);
		analysis._id = "1234";
		analysis.code = randomCode;
		analysis.masterReadSetCodes = new ArrayList<>();
		
		when(analysesDAO.saveObject(analysis)).thenReturn(analysis);
		
		boolean exceptFired = false;
		
		Analysis analysisCreateAPI = null;
		
		try {
			analysisCreateAPI = analysesAPI.create(analysis, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid analysis, should have been refused.");
		}
		
		assertNull(analysisCreateAPI);
	}

	@Test
	public void testUpdateValid() {
		Analysis analysis = mock(Analysis.class);
		
		when(analysesDAO.findByCode(analysis.code)).thenReturn(analysis);
		when(analysesDAO.save(analysis)).thenReturn(analysis);
		
		Analysis analysisUpdateAPI = null;
		
		try {
			analysisUpdateAPI = analysesAPI.update(analysis, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(analysisUpdateAPI);
	}
	
	@Test
	public void testUpdateInvalid() {
		Analysis analysis = mock(Analysis.class);
		analysis.code = "1234";
		
		when(analysesDAO.findByCode(analysis.code)).thenReturn(null);
		when(analysesDAO.getElementClass()).thenReturn(Analysis.class);

		Analysis analysisUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			analysisUpdateAPI = analysesAPI.update(analysis, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid analysis, should have been refused.");
		}
		
		assertNull(analysisUpdateAPI);
	}

	@Test
	public void testUpdatePartialValid() {
		Analysis analysis = mock(Analysis.class);
		analysis = AnalysisFactory.fillRandomAnalysis(analysis);
		
		when(analysesDAO.findByCode(analysis.code)).thenReturn(analysis);

		List<String> masterRSCodes = new ArrayList<String>();
		masterRSCodes.add("BFY_AAAAOSF_1_A737Y.IND2");
		
		analysis.masterReadSetCodes = masterRSCodes;
		
		List<String> fields = new ArrayList<>();
		fields.add("masterReadSetCodes");

		Builder builder = mock(Builder.class);
		when (analysesDAO.getBuilder(any(), any())).thenReturn(builder);

		Analysis analysisUpdateAPI = null;
		
		try {
			analysisUpdateAPI = analysesAPI.update(analysis, TestUtils.CURRENT_USER, fields);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(analysisUpdateAPI);
		assertEquals(analysisUpdateAPI.masterReadSetCodes.get(0), "BFY_AAAAOSF_1_A737Y.IND2");
	}

	@Test
	public void testUpdatePartialInvalid() {
		Analysis analysis = mock(Analysis.class);
		analysis = AnalysisFactory.fillRandomAnalysis(analysis);

		when(analysesDAO.findByCode(analysis.code)).thenReturn(null);
		when(analysesDAO.getElementClass()).thenReturn(Analysis.class);

		List<String> fields = new ArrayList<>();
		fields.add("masterReadSetCodes");

		boolean exceptFired = false;
		Analysis analysisUpdateAPI = null;

		try {
			analysisUpdateAPI = analysesAPI.update(analysis, TestUtils.CURRENT_USER, fields);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid analysis. Should have been refused.");
		}

		assertNull(analysisUpdateAPI);
	}

	@Test
	public void testUpdateStateValid() {
		Analysis analyse = AnalysisFactory.getRandomAnalysis();

		State state = new State();
		state.code = "N";
		state.date = new Date();

		when(analysesDAO.findByCode(analyse.code)).thenReturn(analyse);

		try {
			this.analysesAPI.updateState(analyse.code, state, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateStateValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUpdateStateInvalid() {
		Analysis analyse = AnalysisFactory.getRandomAnalysis();
		
		State state = new State();
		state.code = "N";
		state.date = new Date();
		
		boolean exceptFired = false;

		when(analysesDAO.findByCode(analyse.code)).thenReturn(null);
		when(analysesDAO.getElementClass()).thenReturn(Analysis.class);

		try {
			this.analysesAPI.updateState(analyse.code, state, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid analysis. Should have been refused.");
		}
	}

	@Test
	public void testUpdatePropertiesValid() {
		Analysis analyse = AnalysisFactory.getRandomAnalysis();

		when(analysesDAO.findByCode(analyse.code)).thenReturn(analyse);

		Map<String, PropertyValue> properties = new HashMap<>();

		try {
			this.analysesAPI.updateProperties(analyse.code, properties, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdatePropertiesValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUpdatePropertiesInvalid() {
		Analysis analyse = AnalysisFactory.getRandomAnalysis();
		Map<String, PropertyValue> properties = new HashMap<>();
		boolean exceptFired = false;

		when(analysesDAO.findByCode(analyse.code)).thenReturn(null);
		when(analysesDAO.getElementClass()).thenReturn(Analysis.class);

		try {
			this.analysesAPI.updateProperties(analyse.code, properties, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid analysis. Should have been refused.");
		}
	}

	@Test
	public void testValuationValid() {
		Valuation valuation = ValuationFactory.getRandomValuation();

		Analysis analyse = mock(Analysis.class);
		analyse = AnalysisFactory.fillRandomAnalysis(analyse);

		when(this.analysesDAO.findByCode(analyse.code)).thenReturn(analyse);

		try {
			Analysis analyseUpdated = this.analysesAPI.valuation(analyse.code, valuation, TestUtils.CURRENT_USER);
			
			assertNotNull(analyseUpdated);
			verify(this.analysesDAO, times(1)).updateObject(any(), any());
		} catch (APIValidationException e) {
			Logger.error("Exception occured during testValuationValid()");
			fail(e.getMessage());
		} catch (APIException e) {
			Logger.error("Exception occured during testValuationValid()");
			fail(e.getMessage());
		}
	}

	@Test
	public void testValuationInvalidEmptyValuationParameter() {
		Analysis analyse = mock(Analysis.class);
		analyse = AnalysisFactory.fillRandomAnalysis(analyse);

		boolean exceptFired = false;

		when(this.analysesDAO.findByCode(analyse.code)).thenReturn(null);
		when(this.analysesDAO.getElementClass()).thenReturn(Analysis.class);

		try {
			this.analysesAPI.valuation(analyse.code, null, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			exceptFired = true;
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid valuation. Should have been refused.");
		}
	}

	@Test
	public void testValuationInvalidNoValuationDB() {
		Valuation valuation = ValuationFactory.getRandomValuation();

		Analysis analyse = mock(Analysis.class);
		analyse = AnalysisFactory.fillRandomAnalysis(analyse);

		boolean exceptFired = false;

		when(this.analysesDAO.findByCode(analyse.code)).thenReturn(null);
		when(this.analysesDAO.getElementClass()).thenReturn(Analysis.class);

		try {
			this.analysesAPI.valuation(analyse.code, valuation, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			exceptFired = true;
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			Logger.error("Invalid valuation. Should have been refused.");
		}
	}
}
