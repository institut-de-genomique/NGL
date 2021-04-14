package ngl.common.test.workflows;

import org.junit.Test;

import fr.cea.ig.util.function.C1;
import ngl.common.Global;

/**
 * This does not test anything else than the fact that the workflows
 * implementations can be injected.
 * 
 * @author vrd
 *
 */
public class WorkflowsCyclesTests {

	private static final C1<Class<?>> inject =			
			c -> Global.afSq.cc1().accept(app -> app.injector().instanceOf(c));
	
	// ---- workflows.analyses
	
	@Test
	public void analysisWorkflows() throws Exception {
		inject.accept(workflows.analyses.AnalysisWorkflows.class);
	}
	
	@Test
	public void analysisWorkflowsHelper() throws Exception {
		inject.accept(workflows.analyses.AnalysisWorkflowsHelper.class);
	}
	
	// ---- workflows.container
	
	@Test
	public void contentHelper() throws Exception {
		inject.accept(workflows.container.ContentHelper.class);
	}
	
	@Test
	public void contSupportWorkflows() throws Exception {
		inject.accept(workflows.container.ContSupportWorkflows.class);
	}
	
	@Test
	public void contWorkflows() throws Exception {
		inject.accept(workflows.container.ContWorkflows.class);
	}
	
	// ---- workflows.experiment
	
	@Test
	public void expWorkflows() throws Exception {
		inject.accept(workflows.experiment.ExpWorkflows.class);
	}
	
	@Test
	public void expWorkflowsHelper() throws Exception {
		inject.accept(workflows.experiment.ExpWorkflowsHelper.class);
	}
	
	// ---- workflows.process
	
	@Test
	public void procWorkflows() throws Exception {
		inject.accept(workflows.process.ProcWorkflows.class);
	}
	
	@Test
	public void procWorkflowHelper() throws Exception {
		inject.accept(workflows.process.ProcWorkflowHelper.class);
	}
	
	// ---- workflows.readset
	
	@Test
	public void readSetWorkflows() throws Exception {
		inject.accept(workflows.readset.ReadSetWorkflows.class);
	}
	
	@Test
	public void readSetWorkflowsHelper() throws Exception {
		inject.accept(workflows.readset.ReadSetWorkflowsHelper.class);
	}
	
	// ---- workflows.run
	
	@Test
	public void runWorkflows() throws Exception {
		inject.accept(workflows.run.RunWorkflows.class);
	}
	
	@Test
	public void runWorkflowsHelper() throws Exception {
		inject.accept(workflows.run.RunWorkflowsHelper.class);
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
	
//	@Test
//	public void sraConfigurationWorkflows() throws Exception {
//		inject.accept(workflows.sra.submission.ConfigurationWorkflows.class);
//	}
	
	@Test
	public void sraSubmissionWorkflows() throws Exception {
		inject.accept(workflows.sra.submission.SubmissionWorkflows.class);
	}
	
	@Test
	public void sraSubmissionWorkflowsHelper() throws Exception {
		inject.accept(workflows.sra.submission.SubmissionWorkflowsHelper.class);
	}
	
	
}
