package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.runs.LanesAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.play.test.TestAssertions;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;

public class RunLanesAPITest {

    //Tested API
    private static APIRef<LanesAPI> api = APIRef.runLane;

    private static APIRef<RunsAPI> runApi = APIRef.run;
    private static APIRef<ReadSetsAPI> readSetApi = APIRef.readset;

//    public static final CC3<TestContext, Run, Run> illuminaData = RReadSet.createIlluminaRunAndReadSetRWC
//            .cc3((context, exp, refRun, run, refReadSet, readSet) -> T.t3(context, refRun, run));
    public static final CC3<TestContext, Run, Run> illuminaData = 
    		RApplication.contextResource
    		.nest5(RReadSet::createIlluminaRunAndReadSet)
            .cc3((context, exp, refRun, run, refReadSet, readSet) -> T.t3(context, refRun, run));

    @Test
    public void illumina_checkObjectExistTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            assertTrue(api.get().checkObjectExist(run.code, 1));
        });
    }

    @Test
    public void getRunTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Run r = api.get().getRun(run.code, 1);
            assertNotNull(r);
            assertEquals(refRun.categoryCode, r.categoryCode);
            assertEquals(refRun.typeCode,     r.typeCode);
        });
    }

    @Test
    public void getTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Lane lane = api.get().get(run.code, 1);
            Lane lane2 = api.get().get(run, 1);
            Lane lane3 = api.get().getSubObject(run, "1");
            assertNotNull(lane);
            assertNotNull(lane2);
            assertNotNull(lane3);
            assertEquals(lane2.number, lane.number);
        });
    }

    @Test
    public void getSubOjectsTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Collection<Lane> lanes = api.get().getSubObjects(run);
            assertEquals(run.lanes.size(), lanes.size());
        });
    }

    @Test
    public void listObjectsTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Query query = DBQuery.and(DBQuery.is("code", run.code));
            Iterable<Run> runs = api.get().listObjects(run.code, query);
            TestAssertions.assertOne(runs);
        });
    }

    @Test
    public void saveTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Lane input = TestRunFactory.lane(2, null);
            Lane lane2 = api.get().save(run, input, RConstant.USER);
            Run r = api.get().getRun(run.code, 2);
            assertEquals(refRun.typeCode, r.typeCode);
            assertEquals(2, r.lanes.size());
            assertEquals(input.number, lane2.number);
        });
    }

    @Test
    public void updateTest() throws Exception {
        illuminaData
        .nest((context, refRun, run)         -> Actions.using(RConstant.USER, () -> TestReadsetFactory.readsetIllumina(RConstant.USER, run, 1)))
        .cc3((context, refRun, run, readSet) -> T.t3(context, run, readSet))
        .accept((__, run, readSet) -> {
            //ReadSet readset = readSetApi.get().create(TestReadsetFactory.readsetIllumina(TUResources.USER, run, 1), TUResources.USER);
            Lane lane = run.lanes.stream().filter(l -> l.number == 1).findFirst().get();
            assertEquals(1, lane.readSetCodes.size());
            lane.readSetCodes.add(readSet.code);
            Lane output = api.get().update(run, lane, RConstant.USER);
            assertEquals(2, output.readSetCodes.size());
        });
    }

    @Test
    public void valuationTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            Valuation valuation = TestRunFactory.valuation();
            Lane l = api.get().valuation(run, 1, valuation, RConstant.USER);
            assertEquals(valuation.criteriaCode, l.valuation.criteriaCode);
        });
    }

    @Test
    public void deleteAllLanesTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            List<String> readCodes = new ArrayList<>();
            run.lanes.forEach(lane -> {
                readCodes.addAll(lane.readSetCodes);
            });

            api.get().deleteAllLanes(run, RConstant.USER);

            readCodes.forEach(code -> {
                assertFalse(readSetApi.get().isObjectExist(code));    
            });            
            run = runApi.get().get(run.code);
            assertNull(run.lanes);
        });
    }

    @Test
    public void deleteTest() throws Exception {
        illuminaData.accept((__, refRun, run) -> {
            api.get().delete(run, 1, RConstant.USER);
            assertFalse(api.get().checkObjectExist(run.code, 1));
        });
    }

}
