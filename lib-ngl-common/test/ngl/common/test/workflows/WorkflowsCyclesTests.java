package ngl.common.test.workflows;

import org.junit.Test;

import utils.AbstractTests;

/**
 * This does not test anything else than the fact that the workflows
 * implementations can be injected.
 * 
 * @author vrd
 *
 */
public class WorkflowsCyclesTests extends AbstractTests {

	private void instanciate(Class<?> c) {
		app.injector().instanceOf(c);
	}
	
	// ---- workflows.analyses
	
	@Test
	public void analysisWorkflows() {
		instanciate(workflows.analyses.AnalysisWorkflows.class);
	}
	
	@Test
	public void analysisWorkflowsHelper() {
		instanciate(workflows.analyses.AnalysisWorkflowsHelper.class);
	}
	
	// ---- workflows.container
	
	@Test
	public void contentHelper() {
		instanciate(workflows.container.ContentHelper.class);
	}
	
	@Test
	public void contSupportWorkflows() {
		instanciate(workflows.container.ContSupportWorkflows.class);
	}
	
	@Test
	public void contWorkflows() {
		instanciate(workflows.container.ContWorkflows.class);
	}
	
	// ---- workflows.experiment
	
	@Test
	public void expWorkflows() {
		instanciate(workflows.experiment.ExpWorkflows.class);
	}
	
	@Test
	public void expWorkflowsHelper() {
		instanciate(workflows.experiment.ExpWorkflowsHelper.class);
	}
	
	// ---- workflows.process
	
	@Test
	public void procWorkflows() {
		instanciate(workflows.process.ProcWorkflows.class);
	}
	
	@Test
	public void procWorkflowHelper() {
		instanciate(workflows.process.ProcWorkflowHelper.class);
	}
	
	// ---- workflows.readset
	
	@Test
	public void readSetWorkflows() {
		instanciate(workflows.readset.ReadSetWorkflows.class);
	}
	
	@Test
	public void readSetWorkflowsHelper() {
		instanciate(workflows.readset.ReadSetWorkflowsHelper.class);
	}
	
	// ---- workflows.run
	
	@Test
	public void runWorkflows() {
		instanciate(workflows.run.RunWorkflows.class);
	}
	
	@Test
	public void runWorkflowsHelper() {
		instanciate(workflows.run.RunWorkflowsHelper.class);
	}
	
	// ---- workflows.sra.sample
	
	/*@Test
	public void sraSampleWorkflows() {
		instanciate(workflows.sra.sample.SampleWorkflows.class);
	}
	
	// ---- workflows.sra.study
	
	@Test
	public void sraStudyWorkflows() {
		instanciate(fr.cea.ig.ngl.dao.sra.StudyDAO.StudyWorkflows.class);
	}
	
	@Test
	public void sraStudyWorkflowsHelper() {
		instanciate(workflows.sra.study.StudyWorkflowsHelper.class);
	}*/
	
	// ---- workflows.sra.submission
	
	@Test
	public void sraConfigurationWorkflows() {
		instanciate(workflows.sra.submission.ConfigurationWorkflows.class);
	}
	
	@Test
	public void sraSubmissionWorkflows() {
		instanciate(workflows.sra.submission.SubmissionWorkflows.class);
	}
	
	@Test
	public void sraSubmissionWorkflowsHelper() {
		instanciate(workflows.sra.submission.SubmissionWorkflowsHelper.class);
	}
	
	
}
