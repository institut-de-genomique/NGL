package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import fr.cea.ig.ngl.test.dao.api.factory.TestAnalysesFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC5;
import fr.cea.ig.util.function.T;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.ReadSetStateNames;
import ngl.refactoring.state.RunStateNames;

/**
 * Analysis actions.
 * 
 * @author vrd
 *
 */
public class RAnalysis {

	private static final play.Logger.ALogger logger = play.Logger.of(RAnalysis.class);
	
	/**
	 * Create an analysis from one illumina run and readset ({@link RReadSet#createIlluminaRunAndReadSet(TestContext)})
	 * @param ctx test context
	 * @return    analysis creation action
	 */
	// EJACOBY Non fonctionnel car il faut faie évoluer run et readset pour avoir un état satisfaisant pour la création d'une analyse
	// It is used to generate data in the AnalysisAPITest class.
	public static <C extends TestContext> CC5<Experiment, Run, ReadSet, Analysis, Analysis> createIlluminaAnalysisRWC(C ctx) {
		return RReadSet.createIlluminaRunAndReadSet(ctx)
	        .cc3((exp, refRun, run, refReadset, readset) -> {
	            //add file to readset
	            File input = TestReadsetFactory.rawFile();
	            ctx.apis().readsetFile().save(readset, input, USER);
	            logger.info("############################read state " + readset.state.code);
	            readset = ctx.apis().readset().updateState(readset.code, ReadSetStateNames.IW_VBA, USER);
	            logger.info("############################updateState");
	            
	            readset =  ctx.apis().readset().valuation(readset.code, TestReadsetFactory.valuation(USER), USER);
	            logger.info("############################valuation");
	            run = ctx.apis().run().updateState(run.code, RunStateNames.F_V, USER);
	            logger.info("############################read state " + readset.state.code);
	            return T.t3(exp, run, readset); 
	        })
	        .nest2((exp, run, readset) -> Actions.using2(USER, () -> TestAnalysesFactory.analysis(USER, readset)))
	        .cc5((exp, run, readset, refa, a) -> {
	            logger.info("creation faire!!!!############################");
	            return T.t5(exp, run, readset, refa, a);
	        });
	}
	
}
