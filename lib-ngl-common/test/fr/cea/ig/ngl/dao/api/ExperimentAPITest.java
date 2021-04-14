package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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

import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentsDAO;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.utils.CodeHelper;
import models.utils.code.Code;
import models.utils.instance.ExperimentHelper;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.experiment.instance.ExperimentValidationHelper;
import workflows.experiment.ExpWorkflows;

/**
 * Test de l'API de l'entité EXPERIMENT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExperimentHelper.class, CodeHelper.class, ExperimentValidationHelper.class, CommonValidationHelper.class })
public class ExperimentAPITest {
	
	private final ExperimentsDAO expDAO = Mockito.mock(ExperimentsDAO.class);
	
	private final ExpWorkflows expWF = Mockito.mock(ExpWorkflows.class);
	
	private final ExperimentsAPI expAPI = new ExperimentsAPI(expDAO, expWF);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(ExperimentHelper.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ExperimentValidationHelper.class);
		PowerMockito.mockStatic(CodeHelper.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary", any(ContextValidation.class), any(Experiment.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateCodePrimary", any(ContextValidation.class), any(Experiment.class), any(String.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateExperimentTypeRequired", any(ContextValidation.class), any(String.class), any(Map.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateExperimentCategoryCodeRequired", any(ContextValidation.class), any(String.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateStatusRequired", any(ContextValidation.class), any(String.class), any(Valuation.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateProtocolCode", any(ContextValidation.class), any(String.class), any(String.class), any(String.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateInstrumentUsed", any(ContextValidation.class), any(InstrumentUsed.class), any(Map.class), any(String.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateAtomicTransfertMethods", any(ContextValidation.class), any(String.class), any(String.class), any(InstrumentUsed.class), any(List.class), any(Experiment.class), any(String.class));
			PowerMockito.doNothing().when(ExperimentValidationHelper.class, "validateReagents", any(ContextValidation.class), any(List.class)); 
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired", any(ContextValidation.class), any(TraceInformation.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		Experiment run = ExperimentFactory.getRandomExperimentBionano();
		run.code = randomCode;
		
		when(expDAO.findByCode(randomCode)).thenReturn(run);
		
		Experiment expGetApi = expAPI.get(randomCode);
		
		assertNotNull(expGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(expGetApi.code));
	}
	
	@Test
	public void testCreateValid() throws Exception {
		String randomCode = UUID.randomUUID().toString();

		Experiment experiment = ExperimentFactory.getRandomExperimentBionano();
		experiment.code = randomCode;

		PowerMockito.doNothing().when(ExperimentHelper.class, "doCalculations", any(Experiment.class), anyString());
		PowerMockito.doNothing().when(expWF).applyPreStateRules(any(ContextValidation.class), any(Experiment.class), any(State.class));
		
		when(CodeHelper.getInstance()).thenReturn(mock(Code.class));
		when(CodeHelper.getInstance().generateExperimentCode(any(Experiment.class))).thenReturn(UUID.randomUUID().toString());
		
		when(expDAO.saveObject(experiment)).thenReturn(experiment);

		try {
			Experiment expCreateAPI = expAPI.create(experiment, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", expCreateAPI.code.equals(experiment.code));
			assertNotNull(expCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateInvalid() {
		String randomCode = UUID.randomUUID().toString();
		
		// On tente de créer une experience avec un _id déjà existant.
		Experiment exp = mock(Experiment.class);
		exp._id = "1234";
		exp.code = randomCode;
		
		when(expDAO.saveObject(exp)).thenReturn(exp);
		
		boolean exceptFired = false;
		
		Experiment expCreateAPI = null;
		
		try {
			expCreateAPI = expAPI.create(exp, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid experiment, should have been refused.");
		}
		
		assertNull(expCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		Experiment exp = ExperimentFactory.getRandomExperimentNanopore();
		
		when(expDAO.checkObjectExistByCode(exp.code)).thenReturn(true);
		
		try {
			expAPI.delete(exp.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(expDAO.findByCode(exp.code)).thenReturn(null);
		
		Experiment expFindAPI = expAPI.get(exp.code);
		
		assertNull("Experiment still exists on testDelete()", expFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		Experiment exp = ExperimentFactory.getRandomExperimentBionano();
		
		when(expDAO.isObjectExist(exp.code)).thenReturn(false);
		when(expDAO.getElementClass()).thenReturn(Experiment.class);
		
		boolean exceptFired = false;
		
		try {
			expAPI.delete(exp.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid run, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() throws Exception {
		Experiment exp = mock(Experiment.class);
		exp.code = "1234";
		exp.categoryCode = "testUpdate";
		
		when(expDAO.isObjectExist(exp.code)).thenReturn(Boolean.TRUE);
		when(expDAO.findByCode(exp.code)).thenReturn(exp);
		
		PowerMockito.doNothing().when(ExperimentHelper.class, "doCalculations", any(Experiment.class), anyString());
		PowerMockito.doNothing().when(expWF).applyPreStateRules(any(ContextValidation.class), any(Experiment.class), any(State.class));
		
		when(expDAO.saveObject(exp)).thenReturn(exp);
		
		Experiment expUpdateAPI = null;
		
		try {
			expUpdateAPI = expAPI.update(exp, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(expUpdateAPI);
		assertTrue("Different code on testUpdate()", expUpdateAPI.code.equals(exp.code));
	} 
	
	@Test
	public void testUpdateInvalid() {
		Experiment exp = mock(Experiment.class);
		
		when(expDAO.findByCode(exp.code)).thenReturn(null);
		
		Experiment expUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			expUpdateAPI = expAPI.update(exp, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid experiment, should have been refused.");
		}
		
		assertNull(expUpdateAPI);
	}
}
