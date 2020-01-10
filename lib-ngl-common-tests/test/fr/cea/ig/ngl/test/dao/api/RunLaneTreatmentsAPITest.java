package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import fr.cea.ig.ngl.dao.runs.LaneTreatmentsAPI;
import fr.cea.ig.ngl.dao.runs.LanesAPI;
import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;

public class RunLaneTreatmentsAPITest {

    //Tested API
    private static APIRef<LaneTreatmentsAPI> api = APIRef.runLaneTreatment;
    private static APIRef<LanesAPI> laneApi = APIRef.runLane;


//    public static final CC3<TestContext, Run, Treatment> illuminaData = RReadSet.createIlluminaRunAndReadSetRWC
    public static final CC3<TestContext, Run, Treatment> illuminaData = 
    		RApplication.contextResource
    		.nest5(RReadSet::createIlluminaRunAndReadSet)
            .cc3((context, exp, refRun, run, refReadSet, readSet) -> {
                Treatment t = TestRunFactory.savTreatmentForLane();
                Treatment treatment = context.apis().laneTreatment().save(run, t, 1, RConstant.USER);
                return T.t3(context, context.apis().run().get(run.code), treatment);   
            });

    @Test
    public void illumina_checkObjectExistTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            assertTrue(api.get().checkObjectExist(run.code, 1, treatment.code));
        });
    }

    @Test
    public void getTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Treatment t = api.get().get(run, 1, treatment.code);
            assertNotNull(t);
        });
    }

    @Test
    public void getRunTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Run r = api.get().getRun(run.code, 1, treatment.code);
            assertNotNull(r);
        });
    }

    @Test
    public void deleteTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            api.get().delete(run, 1, treatment.code, RConstant.USER);
            assertFalse(api.get().checkObjectExist(run.code, 1, treatment.code));
        });
    }


    @Test
    public void saveTest() throws Exception {
//        RReadSet.createIlluminaRunAndReadSetRWC.accept((__, exp, refRun, run, refReadSet, readSet) -> {
    	RApplication.contextResource
    	.nest5(RReadSet::createIlluminaRunAndReadSet)
    	.accept((__, exp, refRun, run, refReadSet, readSet) -> {
            Treatment t = TestRunFactory.savTreatmentForLane();
            Treatment treatment = api.get().save(run, t, 1, RConstant.USER);
            Lane l = laneApi.get().get(run.code, 1);
            assertEquals(treatment.code, l.treatments.get(treatment.code).code);
        });
    }

    @Test
    public void listTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Map<String, Treatment> treatments = api.get().list(run.code, 1);
            assertEquals(1, treatments.keySet().size());
            assertEquals(treatment.categoryCode, treatments.get(treatment.code).categoryCode);
        });
    }

    @Test
    public void updateTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Treatment input = TestRunFactory.savTreatmentForLane();
            input.results.put("read2", input.results.get("read1"));
            Treatment t = api.get().update(run, 1, input, RConstant.USER);
            assertEquals(treatment.code, t.code);
            assertNotEquals(treatment.results.keySet().size(), t.results.keySet().size());
            assertEquals(input.results.keySet().size(), t.results.keySet().size());

        });
    }

}
