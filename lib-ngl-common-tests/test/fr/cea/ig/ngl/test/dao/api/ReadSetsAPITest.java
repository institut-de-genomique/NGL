package fr.cea.ig.ngl.test.dao.api;

import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Test;

import controllers.readsets.api.ReadSetValuation;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC6;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.ReadSetStateNames;

public class ReadSetsAPITest {

	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetsAPITest.class);
    
    // Tested API
    private static APIRef<ReadSetsAPI> api = APIRef.readset;
    
    public static final CC6<TestContext, Experiment, Run, Run, ReadSet, ReadSet> illuminaData = 
    		RApplication.contextResource
    		.nest5(RReadSet::createIlluminaRunAndReadSet);
    
    @Test
    public void illumina_createTest() throws Exception {
        logger.debug("Create Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            assertEquals(refReadset.typeCode, readset.typeCode);
            assertEquals(refReadset.location, readset.location);
            assertEquals(ReadSetStateNames.N, readset.state.code);
        });
    }
    
    @Test
    public void illumina_updateTest() throws Exception {
        logger.debug("Update Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            readset.dispatch = ! readset.dispatch;
            Date date = new Date();
            readset.archiveDate = date;
            
            ReadSet read = api.get().update(readset, RConstant.USER);
            
            assertNotEquals(refReadset.dispatch,    read.dispatch);
            assertEquals   (readset.dispatch,       read.dispatch);
            
            assertNotEquals(refReadset.archiveDate, read.archiveDate);
            assertEquals   (readset.archiveDate,    read.archiveDate);
        });
    }
    
    @Test
    public void illumina_updateFieldsTest() throws Exception {
        logger.debug("Update Fields Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            readset.path = "/new/arbitrary/path/";
            ReadSet read = api.get().update(readset, RConstant.USER, Arrays.asList("path"));
            assertNotEquals(refReadset.path, read.path);
            assertEquals   (readset.path,    read.path);
        });
    }
    
    @Test
    public void illumina_updateStateTest() throws Exception {
        logger.debug("Update State Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            State s = new State();
            s.code  = ReadSetStateNames.IP_RG;
            s.user  = RConstant.USER;
            ReadSet read = api.get().updateState(readset.code, s, RConstant.USER);
            assertNotEquals(readset.state.code, read.state.code);
            assertEquals   (s.code,             read.state.code);
        });
    }
    
    @Test
    public void illumina_deleteTest() throws Exception {
        logger.debug("Delete Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            api.get().delete(readset.code);
            assertNull(api.get().get(readset.code));
        });
    }
    
    @Test
    public void illumina_deleteByRunCodeTest() throws Exception {
        logger.debug("Delete using run code Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            api.get().deleteByRunCode(run.code);
            assertNull(api.get().get(readset.code));
        });
    }
    
    @Test
    public void getTest() throws Exception {
        logger.debug("Get Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            ReadSet read = api.get().get(readset.code);
            assertEquals(readset._id,         read._id);
            assertEquals(readset.runTypeCode, read.runTypeCode);
        });
    }
    
    @Test
    public void illumina_valuationTest() throws Exception {
        logger.debug("Valuation Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            ReadSetValuation valuations = TestReadsetFactory.valuation(RConstant.USER);
            ReadSet read = api.get().valuation(readset.code, valuations, RConstant.USER);
            assertEquals(valuations.bioinformaticValuation.comment, read.bioinformaticValuation.comment);
            assertEquals(valuations.bioinformaticValuation.valid,   read.bioinformaticValuation.valid);
            assertEquals(valuations.bioinformaticValuation.user,    read.bioinformaticValuation.user);
        });
    }
        
    @Test
    public void listTest() throws Exception {
        logger.debug("List test");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            //---------- default mode ----------
            logger.debug("default mode");
            final String projCode = exp.projectCodes.iterator().next();
            ListFormWrapper<ReadSet> wrapper = TestReadsetFactory.wrapper(projCode);
            
            Consumer<ReadSet> assertions =
                      read -> {
                          assertEquals(refReadset.code,     read.code);
                          assertEquals(refReadset.typeCode, read.typeCode);
                      };
            
            assertOne(api.get().listObjects(wrapper), assertions);

            //---------- reporting mode----------
            logger.debug("reporting mode");
            wrapper = TestReadsetFactory.wrapper(projCode, QueryMode.REPORTING, null);

            assertOne(api.get().listObjects(wrapper), assertions);
            
            //---------- aggregate mode----------
            logger.debug("aggregate mode");
            wrapper = TestReadsetFactory.wrapper(projCode, QueryMode.AGGREGATE, null);
            assertOne(api.get().listObjects(wrapper), assertions);
        });
    }
    
    @Test
    public void illumina_updatePropertiesTest() throws Exception {
        logger.debug("Update properties Test (Illumina)");
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            readset.properties = new HashMap<>();
            readset.properties.put("localDataDeleted", new PropertySingleValue(true));
            ReadSet read = api.get().update(readset, RConstant.USER, Arrays.asList("properties"));
            assertEquals(1, read.properties.keySet().size());
            assertTrue((Boolean)read.properties.get("localDataDeleted").value);
        });
        
        illuminaData.accept((context, exp, refRun, run, refReadset, readset) -> {
            Map<String, PropertyValue> properties = new HashMap<>();
            properties.put("localDataDeleted", new PropertySingleValue(true));
            ReadSet read = api.get().updateProperties(readset.code, properties, RConstant.USER);
            assertEquals(1, read.properties.keySet().size());
            assertTrue((Boolean)read.properties.get("localDataDeleted").value);
        });
    }
    
}
