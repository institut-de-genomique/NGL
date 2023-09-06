package fr.cea.ig.ngl.workflows;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.NoMoreInteractions;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.experiment.instance.Experiment;
import ngl.refactoring.state.ExperimentStateNames;
import validation.ContextValidation;
import workflows.experiment.*;

/**
 * Test du workflow de l'entité EXPERIMENT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class })
public class ExperimentWorkflowsTest {

	private final ExpWorkflowsHelper expWorkflowsHelper = Mockito.mock(ExpWorkflowsHelper.class);

	private final ExpWorkflows expWorkflows = new ExpWorkflows(expWorkflowsHelper);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(MongoDBDAO.class);
    }

    @Test
	public void testDeleteNewExperiment() {
		Experiment experiment = ExperimentFactory.getRandomExperiment(ExperimentStateNames.N);
		ContextValidation ctx = ContextValidation.createDeleteContext(TestUtils.CURRENT_USER);
		
		expWorkflows.delete(ctx, experiment);

		assertTrue("Context has errors", ctx.getErrors().isEmpty());
		PowerMockito.verifyStatic(atLeastOnce());
	}

	@Test
	public void testDeleteInProgressExperiment() {
		Experiment experiment = ExperimentFactory.getRandomExperiment(ExperimentStateNames.IP);
		ContextValidation ctx = ContextValidation.createDeleteContext(TestUtils.CURRENT_USER);
		
		expWorkflows.delete(ctx, experiment);

		assertTrue("Context has errors", ctx.getErrors().isEmpty());
		PowerMockito.verifyStatic(atLeastOnce());
	}

	@Test
	public void testDeleteTerminatedExperiment() {
		Experiment experiment = ExperimentFactory.getRandomExperiment(ExperimentStateNames.F);
		ContextValidation ctx = ContextValidation.createDeleteContext(TestUtils.CURRENT_USER);
		
		expWorkflows.delete(ctx, experiment);

		assertTrue("Context has no error", !ctx.getErrors().isEmpty());
		PowerMockito.verifyStatic(new NoMoreInteractions());
	}
}
