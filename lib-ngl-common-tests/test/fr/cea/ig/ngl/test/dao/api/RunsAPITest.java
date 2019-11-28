package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.ngl.test.resource.RRun;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CC4;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.RunStateNames;

public class RunsAPITest {
	
    private static final play.Logger.ALogger logger = play.Logger.of(RunsAPITest.class);
    
    // Tested API
    private static APIRef<RunsAPI> api = APIRef.run;
    
    public static final CC4<TestContext, Experiment, Run, ReadSet> nanoporeData = 
    		RApplication.contextResource
    		.nest3(RReadSet::createNanoporeRunAndReadSet);
    
    public static final CC4<TestContext, Experiment, Run, ReadSet> nanoporeDataLog = 
    		RApplication.ctxLog
    		.nest3(RReadSet::createNanoporeRunAndReadSet);
    
//    public static final CC3<TestContext, Run, Run>                 illuminaData = 
//    		RApplication.contextResource
//    		.nest3(RRun::createIlluminaRun)
//    		.cc3  ((context, exp, refRun, run) -> T.t3(context, refRun, run));
    public static final CC3<TestContext, Run, Run>                 illuminaData = 
    		RApplication.contextResource
    		.nest2(RunsAPITest::illuminaData);
    
    public static final CC3<TestContext, Run, Run>                 illuminaDataLog = 
    		RApplication.ctxLog
    		.nest2(RunsAPITest::illuminaData);
    
    public static final <C extends TestContext> CC2<Run, Run> illuminaData(C ctx) {
    	return RRun.createIlluminaRun(ctx)
    			.cc2  ((exp, refRun, run) -> T.t2(refRun, run));
    }
    
    // --- Tests on Nanopore Runs
    
    @Test
    public void nanopore_createTest() throws Exception {
        nanoporeData.accept((context, exp, run, __) -> {
            logger.info("Create Test (Nanopore)");
            assertEquals("nanopore",                                        run.categoryCode);
            assertEquals("RPROMETHION",                                     run.typeCode);
            logger.error("exp:{}, run:{}", exp.getCode(), run.getCode());
            assertEquals("experiment sample codes and run sample codes do not match",
            		     exp.sampleCodes,                                   run.sampleCodes);
            assertEquals(exp.projectCodes,                                  run.projectCodes);
            assertEquals(exp.outputContainerSupportCodes.iterator().next(), run.containerSupportCode);
            assertEquals(exp.instrument.code,                               run.instrumentUsed.code);
            assertFalse (run.dispatch);
        });
    }
    
    @Test
    public void nanopore_updateState() throws Exception {
        nanoporeData.accept((context, exp, run, __) -> {
            logger.info("Update State Test (Nanopore)");
            logger.debug("State before update " + run.state.code);
            Run r = api.get().updateState(run.code, RunStateNames.IW_RG, RConstant.USER);
            logger.info("State after update {}", r.state.code);
            assertEquals(RunStateNames.IW_RG, r.state.code);
        });
    }
    
    @Test
    public void nanopore_updateFields() throws Exception {
        nanoporeData.accept((context, exp, run, __) -> {
            logger.info("Update fields Test (Nanopore)");
            
            assertFalse(run.deleted);
            
            Run input = new Run();
            input.code = run.code;
            input.deleted = true;
            Run r = api.get().update(input, RConstant.USER, Arrays.asList("deleted"));
            
            assertTrue(r.deleted);
        });
    }
    
    // ------------------------------------------------------------------------
    // Test tracing.
    
    @Test
    public void nanopore_updateTest() throws Exception {
        nanoporeData.accept((context, exp, run,__) -> {
            logger.info("Update Test (Nanopore)");
            logger.info("thread {}", Thread.currentThread());
        		logger.info("thread {}", Thread.currentThread());
           assertFalse(run.deleted);
            
            run.deleted = true;
            Run r = api.get().update(run, RConstant.USER);
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void nanopore_valuationTest() throws Exception {
        nanoporeData.accept((context, exp, run,__) -> {
            logger.info("Valuation Test (Nanopore)");
            Valuation valuation = new Valuation();
            valuation.user = RConstant.USER;
            valuation.comment = "test valuation";
            valuation.date = new Date();
            valuation.valid = TBoolean.FALSE;
            
            Run r = api.get().valuation(run.code, valuation, RConstant.USER);
            assertEquals(valuation.comment, r.valuation.comment);
            assertEquals(valuation.date, r.valuation.date);
            assertEquals(valuation.valid, r.valuation.valid);
        });
    }
    
    @Test
    public void nanopore_deleteTest() throws Exception {
        nanoporeData.accept((context, exp, run,__) -> {
            logger.info("Delete Test (Nanopore)");
            api.get().delete(run.code);
            assertNull(api.get().get(run.code));
        });
    }
    
    // --- Tests on Illumina Runs    
    @Test
    public void illumina_createTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Create Test (Illumina)");
            assertEquals(refRun.categoryCode,         run.categoryCode);
            assertEquals(refRun.typeCode,             run.typeCode);
            assertEquals(refRun.containerSupportCode, run.containerSupportCode);
            assertEquals(refRun.instrumentUsed.code,  run.instrumentUsed.code);
            assertFalse(run.dispatch);
        });
    }
    
    @Test
    public void illumina_updateState() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Update State Test (Illumina)");
            logger.debug("State before update " + run.state.code);
            State state = new State();
            state.code = "IW-RG";
            state.user = RConstant.USER;
            state.date = new Date();
            Run r = api.get().updateState(run.code, state, RConstant.USER);
            logger.debug("State after update " + r.state.code);
            
            assertEquals(state.code, r.state.code);
            assertNotEquals(run.state.code, r.state.code);
        });
    }
    
    @Test
    public void illumina_updateFields() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Update fields Test (Illumina)");
            
            assertFalse(run.deleted);
            
            Run input = new Run();
            input.code = run.code;
            input.deleted = true;
            Run r = api.get().update(input, RConstant.USER, Arrays.asList("deleted"));
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void illumina_updateTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Update Test (Illumina)");
            assertFalse(run.deleted);
            
            run.deleted = true;
            Run r = api.get().update(run, RConstant.USER);
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void illumina_valuationTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Valuation Test (Illumina)");
            Valuation valuation = new Valuation();
            valuation.user = RConstant.USER;
            valuation.comment = "test valuation";
            valuation.date = new Date();
            valuation.valid = TBoolean.FALSE;
            
            Run r = api.get().valuation(run.code, valuation, RConstant.USER);
            assertEquals(valuation.comment, r.valuation.comment);
            assertEquals(valuation.date, r.valuation.date);
            assertEquals(valuation.valid, r.valuation.valid);
        });
    }
    
    @Test
    public void illumina_deleteTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Delete Test (Illumina)");
            api.get().delete(run.code);
            assertNull(api.get().get(run.code));
        });
    }
    
}
