package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import fr.cea.ig.ngl.dao.runs.TreatmentsAPI;
import fr.cea.ig.ngl.test.TUResources;
import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.dao.DAOException;

public class RunTreatmentsAPITest {

    //Tested API
    private static APIRef<TreatmentsAPI> api = APIRef.runTreatment;

    public static final CC3<TestContext, Run, Treatment> illuminaData = TUResources.createIlluminaRunAndReadSetRWC
            .cc3((context, exp, refRun, run, refReadSet, readSet) -> {
                Treatment t = TestRunFactory.savTreatment();
                Treatment treatment = context.apis().runTreatment().save(run, t, TUResources.USER);
                return T.t3(context, context.apis().run().get(run.code), treatment);   
            });

    @Test
    public void illumina_getTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
           Run r = api.get().get(run.code, treatment.code);
           assertEquals(run.categoryCode,               r.categoryCode);
           assertEquals(run.typeCode,                   r.typeCode);
           assertEquals(run.treatments.keySet().size(), r.treatments.keySet().size());
        });
    }
    
    @Test
    public void illumina_checkObjectExistTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            assertTrue(api.get().checkObjectExist(run.code, treatment.code));
        });
    }

    @Test
    public void illumina_deleteTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            api.get().delete(run, treatment.code, TUResources.USER);
            try {
                run = api.get().get(run.code, treatment.code);
            } catch (DAOException e) {
                run = null;
            }
            assertNull(run);
        });
    }


    @Test
    public void illumina_getSubObjectsTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            assertNotNull(api.get().getSubObjects(run));
            assertEquals(1, api.get().getSubObjects(run).size());
        });
    }

    @Test
    public void illumina_getSubObjectTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Treatment t = api.get().getSubObject(run, treatment.code);
            assertNotNull(t);
            assertEquals(treatment.code,         t.code);
            assertEquals(treatment.categoryCode, t.categoryCode);
            assertEquals(treatment.typeCode,     t.typeCode);
        });
    }

    @Test
    public void illumina_saveTest() throws Exception {
        TUResources.createIlluminaRunAndReadSetRWC.accept((context, __, refRun, run, ___, ____) -> {
            Treatment t = TestRunFactory.savTreatment();
            Treatment treatment = context.apis().runTreatment().save(run, t, TUResources.USER);  
            assertEquals(t.code,         treatment.code);
            assertEquals(t.categoryCode, treatment.categoryCode);
            assertEquals(t.typeCode,     treatment.typeCode);
        });
    }
    
    @Test
    public void illumina_listTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Map<String, Treatment> treatments = api.get().list(run.code);
            assertEquals(1, treatments.keySet().size());
            Treatment t = treatments.get(treatment.code);
            assertEquals(treatment.code,         t.code);
            assertEquals(treatment.categoryCode, t.categoryCode);
            assertEquals(treatment.typeCode,     t.typeCode);
        });
    }

    @Test
    public void illumina_updateTest() throws Exception {
        illuminaData.accept((__, run, treatment) -> {
            Treatment input = TestRunFactory.savTreatment();
            String key = "default";
            String prop = "yieldTotal";
            input.results.get(key).put(prop, new PropertySingleValue(10.01));
            Treatment result = api.get().update(run, input, TUResources.USER);
            assertEquals(input.results.get(key).get(prop).value,        result.results.get(key).get(prop).value);
            assertNotEquals(treatment.results.get(key).get(prop).value, result.results.get(key).get(prop).value);
        });
    }

}
