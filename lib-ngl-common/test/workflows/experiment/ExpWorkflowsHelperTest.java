/**
 * 
 */
package workflows.experiment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import workflows.container.ContSupportWorkflows;
import workflows.container.ContWorkflows;
import workflows.process.ProcWorkflows;

/**
 * @author aprotat
 *
 */
public class ExpWorkflowsHelperTest {
	
	private NGLApplication nglApp = mock(NGLApplication.class);
	
	private ContWorkflows contW = mock(ContWorkflows.class);
	
	private ContSupportWorkflows contSupportW = mock(ContSupportWorkflows.class);
	
	private ProcWorkflows procW = mock(ProcWorkflows.class);
	
	private final ExpWorkflowsHelper expWorkflowsHelper = new ExpWorkflowsHelper(nglApp, contW, contSupportW, procW);

	/**
	 * Test method for {@link workflows.experiment.ExpWorkflowsHelper#isNotQuantifiableByFluo(java.lang.String, models.laboratory.experiment.instance.InputContainerUsed)}.
	 */
	@Test
	public final void testIsNotQuantifiableByFluo() {
		Experiment experimentNotQuantifiable = ExperimentFactory.getRandomExperimentFluoNotQuantifiable();
		InputContainerUsed icuNotQuantifiable = experimentNotQuantifiable.atomicTransfertMethods.get(0).inputContainerUseds.get(0);
		
		assertTrue(expWorkflowsHelper.isNotQuantifiableByFluo(experimentNotQuantifiable.typeCode, icuNotQuantifiable));
		
		
		Experiment experimentQuantifiable = ExperimentFactory.getRandomExperimentFluoQuantifiable();
		InputContainerUsed icuQuantifiable = experimentQuantifiable.atomicTransfertMethods.get(0).inputContainerUseds.get(0);
		
		assertFalse(expWorkflowsHelper.isNotQuantifiableByFluo(experimentQuantifiable.typeCode, icuQuantifiable));
	}

	/**
	 * Test method for {@link workflows.experiment.ExpWorkflowsHelper#clearCurrentQuantification(models.laboratory.container.instance.Container)}.
	 */
	@Test
	public final void testClearCurrentQuantification() {
		Container container = ContainerFactory.getRandomFilledContainer("F");
		
		assertNotNull(container.quantity);
		assertNotNull(container.concentration);
		
		expWorkflowsHelper.clearCurrentQuantification(container);
		
		assertNull(container.quantity);
		assertNull(container.concentration);
	}

}
