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
import fr.cea.ig.ngl.test.TUResources;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CC4;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;

public class RunsAPITest {
	
    private static final play.Logger.ALogger logger = play.Logger.of(RunsAPITest.class);
    
    //Tested API
    private static APIRef<RunsAPI> api = APIRef.run;
    
    public static final CC4<TestContext, Experiment, Run, ReadSet> nanoporeData = TUResources.createNanoporeRunAndReadSetRWC;
    public static final CC3<TestContext, Run, Run>        illuminaData = TUResources.createIlluminaRunRWC
                                                                                    .cc3((context, exp, refRun, run) -> T.t3(context, refRun, run));
    // --- Tests on Nanopore Runs
    
    @Test
    public void nanopore_createTest() throws Exception {
        nanoporeData.accept((context, exp, run, __) -> {
            logger.info("Create Test (Nanopore)");
            assertEquals("nanopore",                                        run.categoryCode);
            assertEquals("RPROMETHION",                                     run.typeCode);
            assertEquals(exp.sampleCodes,                                   run.sampleCodes);
            assertEquals(exp.projectCodes,                                  run.projectCodes);
            assertEquals(exp.outputContainerSupportCodes.iterator().next(), run.containerSupportCode);
            assertEquals(exp.instrument.code,                               run.instrumentUsed.code);
            assertFalse(run.dispatch);
        });
    }
    
    @Test
    public void nanopore_updateState() throws Exception {
        nanoporeData.accept((context, exp, run, __) -> {
            logger.info("Update State Test (Nanopore)");
            logger.debug("State before update " + run.state.code);
            State state = new State();
            state.code = "IW-RG";
            state.user = TUResources.USER;
            state.date = new Date();
            Run r = api.get().updateState(run.code, state, TUResources.USER);
            logger.debug("State after update {}", r.state.code);
            assertEquals(state.code, r.state.code);
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
            Run r = api.get().update(input, TUResources.USER, Arrays.asList("deleted"));
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void nanopore_updateTest() throws Exception {
        nanoporeData.accept((context, exp, run,__) -> {
            logger.info("Update Test (Nanopore)");
            assertFalse(run.deleted);
            
            run.deleted = true;
            Run r = api.get().update(run, TUResources.USER);
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void nanopore_valuationTest() throws Exception {
        nanoporeData.accept((context, exp, run,__) -> {
            logger.info("Valuation Test (Nanopore)");
            Valuation valuation = new Valuation();
            valuation.user = TUResources.USER;
            valuation.comment = "test valuation";
            valuation.date = new Date();
            valuation.valid = TBoolean.FALSE;
            
            Run r = api.get().valuation(run.code, valuation, TUResources.USER);
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
            state.user = TUResources.USER;
            state.date = new Date();
            Run r = api.get().updateState(run.code, state, TUResources.USER);
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
            Run r = api.get().update(input, TUResources.USER, Arrays.asList("deleted"));
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void illumina_updateTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Update Test (Illumina)");
            assertFalse(run.deleted);
            
            run.deleted = true;
            Run r = api.get().update(run, TUResources.USER);
            
            assertTrue(r.deleted);
        });
    }
    
    @Test
    public void illumina_valuationTest() throws Exception {
        illuminaData.accept((context, refRun, run) -> {
            logger.info("Valuation Test (Illumina)");
            Valuation valuation = new Valuation();
            valuation.user = TUResources.USER;
            valuation.comment = "test valuation";
            valuation.date = new Date();
            valuation.valid = TBoolean.FALSE;
            
            Run r = api.get().valuation(run.code, valuation, TUResources.USER);
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
