package workflows;

import java.util.Arrays;
// import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

// import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import models.laboratory.common.instance.State;
// import play.api.modules.spring.Spring;
import utils.AbstractTests;
import workflows.container.ContSupportWorkflows;

/**
 * This belong to some subproject and the test should actually define
 * rules.key and rules.kbasename that should be set on a per test basis.
 * The simpler way is to have this test moved to the proper subproject.

 * @author vrd
 *
 */
public class ContainerSupportWorkflowTests extends AbstractTests {

	private ContSupportWorkflows workflows() {
		// Spring.get BeanOfType(ContSupportWorkflows.class); 
		return app.injector().instanceOf(ContSupportWorkflows.class);
	}
	
	@Test
	public void validateGetNextStateFromContainersIWD() {
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IU","IW-E","IW-P", "A-TM", "A-TF", "A-PF", "A-QC", "UA", "IS", "N", "IW-D"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("IW-D", s.code);
	}
		
	@Test
	public void validateGetNextStateFromContainersIU() {
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IU","IW-E","IW-P", "A-TM", "A-TF", "A-PF", "A-QC", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("IU", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersIWE() {
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-E","IW-P", "A-TM", "A-TF", "A-PF", "A-QC", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("IW-E", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersA() {
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("A-TM", "A-TF", "A-PF", "A-QC", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("A", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersATM(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-P", "A-TM", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("A-TM", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersAQC(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-P", "A-QC", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("A-QC", s.code);
	}
	
	// TODO : failed: expected:<[A]-P> but was:<[IW]-P>
	// @Test
	public void validateGetNextStateFromContainersAP(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-P", "A-P", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("A-P", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersATF(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-P", "A-TF", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("A-TF", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersIWP(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("IW-P", "UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("IW-P", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersIS(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("UA", "IS"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("IS", s.code);
	}
	
	@Test
	public void validateGetNextStateFromContainersUA(){
		ContSupportWorkflows worflows = workflows();
		Set<String> containerStates = new TreeSet<>(Arrays.asList("UA"));
		State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
		Assert.assertEquals("UA", s.code);
	}
	
}
