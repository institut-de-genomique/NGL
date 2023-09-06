package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.dao.api.factory.run.RunFactory;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.LaneValidationHelper;
import validation.run.instance.RunValidationHelper;
import validation.run.instance.TreatmentValidationHelper;
import workflows.run.RunWorkflows;

/**
 * Test de l'entité RUN.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, RunValidationHelper.class, TreatmentValidationHelper.class,
		LaneValidationHelper.class, MongoDBDAO.class })
public class RunAPITest {

	private final RunsDAO runDAO = Mockito.mock(RunsDAO.class);

	private final ReadSetsDAO rsDAO = Mockito.mock(ReadSetsDAO.class);

	private final RunWorkflows runWF = Mockito.mock(RunWorkflows.class);

	private final RunsAPI runAPI = new RunsAPI(runDAO, runWF, rsDAO);

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests.
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(RunValidationHelper.class);
		PowerMockito.mockStatic(TreatmentValidationHelper.class);
		PowerMockito.mockStatic(LaneValidationHelper.class);
		PowerMockito.mockStatic(MongoDBDAO.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary", any(ContextValidation.class), any(Run.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateCodePrimary", any(ContextValidation.class), any(Run.class), any(String.class));
			PowerMockito.doNothing().when(RunValidationHelper.class, "validateRunTypeCodeRequired", any(ContextValidation.class), any(String.class), any(Map.class));
			PowerMockito.doNothing().when(RunValidationHelper.class, "validateRunCategoryCodeRequired", any(ContextValidation.class), any(String.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateValuationRequired", any(ContextValidation.class), any(String.class), any(Valuation.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired", any(ContextValidation.class), any(TraceInformation.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateContainerSupportCodeRequired", any(ContextValidation.class), any(String.class), any(String.class));
			PowerMockito.doNothing().when(LaneValidationHelper.class, "validateLanes", any(ContextValidation.class), any(Run.class), any(List.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetByCode() throws ParseException {
		String randomCode = UUID.randomUUID().toString();

		Run run = RunFactory.getRandomRun();
		run.code = randomCode;

		when(runDAO.findByCode(randomCode)).thenReturn(run);

		Run runGetApi = runAPI.get(randomCode);

		assertNotNull(runGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(runGetApi.code));
	}

	@Test
	public void testCreateValid() {
		String randomCode = UUID.randomUUID().toString();

		Run run = RunFactory.getRandomRun();
		run.code = randomCode;

		when(runDAO.save(run)).thenReturn(run);

		try {
			Run runCreateAPI = runAPI.create(run, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", runCreateAPI.code.equals(run.code));
			assertNotNull(runCreateAPI);
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
		
		// On tente de créer un run avec un _id déjà existant.
		Run run = mock(Run.class);
		run._id = "1234";
		run.code = randomCode;
		
		when(runDAO.saveObject(run)).thenReturn(run);
		
		boolean exceptFired = false;
		
		Run runCreateAPI = null;
		
		try {
			runCreateAPI = runAPI.create(run, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid run, should have been refused.");
		}
		
		assertNull(runCreateAPI);
	}
	
	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void testDeleteValid() {
		Run run = RunFactory.getRandomRun();
		
		when(runDAO.isObjectExist(run.code)).thenReturn(true);

		when(MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code)).thenReturn(run);

		MongoDBResult dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(), any(), any())).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(SampleFactory.getRandomSampleList());

		try {
			runAPI.delete(run.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(runDAO.findByCode(run.code)).thenReturn(null);
		
		Run runFindAPI = runAPI.get(run.code);
		
		assertNull("Run still exists on testDelete()", runFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		Run run = RunFactory.getRandomRun();
		
		when(runDAO.isObjectExist(run.code)).thenReturn(false);
		when(runDAO.getElementClass()).thenReturn(Run.class);
		
		boolean exceptFired = false;
		
		try {
			runAPI.delete(run.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid run, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() {
		Run run = mock(Run.class);

		ContextValidation ctxVal = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
		
		when(runDAO.isObjectExist(run.code)).thenReturn(Boolean.TRUE);
		when(runDAO.findByCode(run.code)).thenReturn(run);
		
		run.categoryCode = "testUpdate";
		
		when(runDAO.save(run)).thenReturn(run);
		doNothing().when(run).validate(ctxVal);
		
		Run runUpdateAPI = null;
		
		try {
			runUpdateAPI = runAPI.update(run, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(runUpdateAPI);
	} 
	
	@Test
	public void testUpdateInvalid() {
		Run run = mock(Run.class);
		
		when(runDAO.isObjectExist(run.code)).thenReturn(Boolean.FALSE);
		when(runDAO.getElementClass()).thenReturn(Run.class);
		
		Run runUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			runUpdateAPI = runAPI.update(run, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid run, should have been refused.");
		}
		
		assertNull(runUpdateAPI);
	}
}
