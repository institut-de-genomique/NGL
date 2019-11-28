package fr.cea.ig.ngl.test.workflow;

import java.util.List;

import org.junit.Test;

import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RContainer;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC5;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import ngl.refactoring.state.ContainerStateNames;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import workflows.container.ContWorkflows;

/**
 * ContextValidation parameter detection tests to ensure that the replacement
 * methods are correct.
 * 
 * @author vrd
 *
 */
public class ContWorflowsTest {
	
	
	private static CC5<TestContext,ContainerSupport,List<Container>,ContextValidation,ContWorkflows> plateContext =
			RApplication.contextResource
			.and2(RContainer.createPlateResource)
			.cc5 ((c, plate, wells) -> T.t5(c, plate, wells,
			        		                          ContextValidation.createUpdateContext(RConstant.USER), 
			        		                          c.app().injector().instanceOf(ContWorkflows.class)));
	
	@Test
	public void test_01n() throws Exception {
		plateContext.accept((c, plate, wells, vc, cwf) -> 
			cwf.setState(vc, wells.get(0), new State(ContainerStateNames.A_PF, RConstant.USER), "workflow", true));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void test_01o() throws Exception {
		plateContext.accept((c, plate, wells, vc, cwf) -> {
			vc.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT,        "workflow");
			vc.putObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, true);
			cwf.setState(vc, wells.get(0), new State(ContainerStateNames.A_PF, RConstant.USER));
		});
	}
	
	@Test
	public void test_02n() throws Exception {
		plateContext.accept((c, plate, wells, vc, cwf) ->
			cwf.setState(vc, wells.get(0), new State(ContainerStateNames.A_PF, RConstant.USER), "controllers", true));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void test_02o() throws Exception {
		plateContext.accept((c, plate, wells, vc, cwf) -> {
			vc.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT,        "controllers");
			vc.putObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, true);
			cwf.setState(vc, wells.get(0), new State(ContainerStateNames.A_PF, RConstant.USER));
		});
	}
	
}
