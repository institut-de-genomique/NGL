package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.ExperimentStateNames;

/**
 * Run actions.
 * 
 * @author vrd
 *
 */
public class RRun {

	/**
	 * Create a run from Illumina Depot experiment ({@link RExperiment#createExpDepotIllumina(TestContext)})
	 * @param ctx test context
	 * @return    run creation action
	 */
	public static final <C extends TestContext> CC3<Experiment, Run, Run> createIlluminaRun(C ctx)  {
		return RExperiment.createExpDepotIllumina(ctx)
	        .cc1  ((refExperience, experience) -> ctx.apis().experiment().updateState(experience.code, ExperimentStateNames.IP, USER))
	        .nest2((experiment)                -> Actions.using2(USER, () -> TestRunFactory.run(experiment, USER)));
	}
	
}
