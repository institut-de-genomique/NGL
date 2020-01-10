package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import java.util.ArrayList;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CC5;
import fr.cea.ig.util.function.T;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.ExperimentStateNames;
import ngl.refactoring.state.RunStateNames;

/**
 * Read sets actions.
 * 
 * @author vrd
 *
 */
public class RReadSet {

	private static final play.Logger.ALogger logger = play.Logger.of(RReadSet.class);
	
	/**
	 * Generate a run and a readset from Nanopore Depot experiment ({@link RExperiment#createEndedExpDepotNanopore(TestContext)})
	 * @param ctx test context
	 * @return    nanopore run creation action
	 */
//	public static final <C extends TestContext> CC3<Experiment, Run, ReadSet> createNanoporeRunAndReadSet(C ctx) {
//		return RExperiment.createEndedExpDepotNanopore(ctx)
////	        .nest ((refExperiment, exp) -> Actions.cleaningOne(() -> {
////	            logger.info("retrieve Nanopore Run from experiment {}", Iterables.surround(exp.projectCodes, "(project codes:",",",")").asString());
////	            logger.info("experiements samples : (sample codes:{})", Iterables.intercalate(exp.sampleCodes, ",").asString());
////	            Iterable<Run> runs = ctx.apis().run().listObjects(TestRunFactory.wrapper(exp.projectCodes.iterator().next()));
////	            logger.info("runs : [{}]", Iterables.map(runs,  r -> r.getCode()).intercalate(",").asString());
////	            return runs.iterator().next();
////	        }))
//	        .nest ((refExperiment, exp) -> Actions.cleaningOne(() ->
//		                   ctx.apis().run().listObjects(TestRunFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next()))
//	        .cc2  ((refExperiment, exp, run) -> T.t2(exp, run))
////	        .nest ((exp, run) -> Actions.cleaningOne(() -> {
////	            logger.info("retrieve Nanopore ReadSet");
////	            return ctx.apis().readset().listObjects(TestReadsetFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next();
////	        }));
//	        .nest ((exp, run) -> Actions.cleaningOne(() -> 
//	        			ctx.apis().readset().listObjects(TestReadsetFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next()));
//	}
	public static final <C extends TestContext> CC3<Experiment, Run, ReadSet> createNanoporeRunAndReadSet(C ctx) {
		return RExperiment.createEndedExpDepotNanopore(ctx)
	        .nest ((refExperiment, exp)      -> Actions.cleaningOne(() ->
	                    ctx.apis().run().listObjects(TestRunFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next()))
	        .cc2  ((refExperiment, exp, run) -> {
	        	 run = ctx.apis().run().updateState(run.getCode(), RunStateNames.F_S, USER);
	        	 return T.t2(exp, run);
	        })
	        .nest ((exp, run)                -> Actions.cleaningOne(() -> 
	        			ctx.apis().readset().listObjects(TestReadsetFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next()));
	}
	
	/**
	 * Create a ReadSet from Illumina run ({@link RRun#createIlluminaRun(TestContext)})
	 * @param ctx test context
	 * @return    readset cration action
	 */
	public static final <C extends TestContext> CC5<Experiment, Run, Run, ReadSet, ReadSet> createIlluminaRunAndReadSet(C ctx) {
		return RRun.createIlluminaRun(ctx)
	        .cc3((exp, refRun, run) -> {
	            // finish experiment
	            ctx.apis().experiment().updateState(exp.code, ExperimentStateNames.F, RConstant.USER);
	            // create the lane 1
	            ctx.apis().lane().save(run, TestRunFactory.lane(1, null), RConstant.USER);
	            run = ctx.apis().run().get(run.code);
	            // add project and sample codes to the run (update after creation of readset due to validation)
	            run.projectCodes    = exp.projectCodes;
	            run.sampleCodes     = exp.sampleCodes;
	            refRun.projectCodes = exp.projectCodes;
	            refRun.sampleCodes  = exp.sampleCodes;
	            logger.debug("createIlluminaRunAndReadSet: run {} {}, exp {} {}", run.getCode(), Iterables.surround(run.sampleCodes, "[", ",", "]").asString(), exp.getCode(), Iterables.surround(exp.sampleCodes, "[", ",", "]").asString());
	            return T.t3(exp, refRun, run);
	        })
	        .nest2((exp, refRun, run) -> Actions.using2(RConstant.USER, () -> TestReadsetFactory.readsetIllumina(RConstant.USER, run, 1)))
	        .cc5((exp, refRun, run, refReadset, readset) -> {
	            // update the run (project and sample codes)
	            run = ctx.apis().run().update(run, RConstant.USER);
	            // add readset in lane 1 of run (required if we want to update the readset later (validation purpose))
	            Lane lane = run.lanes.stream().filter(l -> l.number == 1).findFirst().get();
	            lane.readSetCodes = new ArrayList<>();
	            lane.readSetCodes.add(readset.code);
	            ctx.apis().lane().update(run, lane, RConstant.USER);
	            run = ctx.apis().run().get(run.code);
	            return T.t5(exp, refRun, run, refReadset, readset);
	        });
	}
	
}
