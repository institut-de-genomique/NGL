package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.cea.ig.ngl.dao.readsets.ReadSetTreatmentsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;

public class ReadSetTreatmentsAPITest {

    //Tested API
    private static APIRef<ReadSetTreatmentsAPI> api = APIRef.readsetTreatment;
    private static APIRef<ReadSetsAPI> readSetApi = APIRef.readset;

    public static final CC3<TestContext, ReadSet, Treatment> illuminaData = 
//    		RReadSet.createIlluminaRunAndReadSetRWC
    		RApplication.contextResource
    		.nest5(RReadSet::createIlluminaRunAndReadSet)
            .cc3((context, exp, refRun, run, refReadSet, readSet) -> {
                Treatment t = TestReadsetFactory.globalTreatment();
                Treatment treatment = context.apis().readsetTreatment().save(readSet, t, RConstant.USER);
                return T.t3(context, context.apis().readset().get(readSet.code), treatment);   
            });

    @Test
    public void illumina_checkObjectExistTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            assertTrue(api.get().checkObjectExist(readset.code, treatment.code));
        });
    }

    @Test
    public void deleteTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            api.get().delete(readset, treatment.code, RConstant.USER);
            assertNull(api.get().getSubObject(readSetApi.get().get(readset.code), treatment.code));
        });
    }

    @Test
    public void deleteAllTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            api.get().deleteAll(readset, RConstant.USER);
            assertEquals(0, api.get().getSubObjects(readSetApi.get().get(readset.code)).size());
        });
    }

    @Test
    public void getSubObjectsTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            assertNotNull(api.get().getSubObjects(readset));
            assertEquals(1, api.get().getSubObjects(readset).size());
        });
    }

    @Test
    public void getSubObjectTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            Treatment t = api.get().getSubObject(readset, treatment.code);
            assertNotNull(t);
            assertEquals(treatment.code,         t.code);
            assertEquals(treatment.categoryCode, t.categoryCode);
            assertEquals(treatment.typeCode,     t.typeCode);
        });
    }

    @Test
    public void saveTest() throws Exception {
//        RReadSet.createIlluminaRunAndReadSetRWC.accept((__, ___, ____, _____, refReadSet, readSet) -> {
    	RApplication.contextResource
    	.nest5(RReadSet::createIlluminaRunAndReadSet)
    	.accept((__, ___, ____, _____, refReadSet, readSet) -> {
            Treatment t = TestReadsetFactory.globalTreatment();
            Treatment treatment = api.get().save(readSet, t, RConstant.USER);
            readSet = readSetApi.get().get(readSet.code);
            assertEquals(1, readSet.treatments.keySet().size());
            assertEquals(t.code, treatment.code);
            assertEquals(t.code, readSet.treatments.get(t.code).code);

        });
    }

    @Test
    public void updateTest() throws Exception {
        illuminaData.accept((context, readset, treatment) -> {
            Treatment t = TestReadsetFactory.globalTreatment();
            String resultKey = "default";
            String key = "usefulBases";
            t.results.get(resultKey).put(key, new PropertySingleValue(30000));
            Treatment tt = api.get().update(readset, t, RConstant.USER);

//            assertNotEquals((Long)treatment.results.get(resultKey).get(key).value, (Long)tt.results.get(resultKey).get(key).value);
//            assertEquals(   (Long)t.results.get(resultKey).get(key).value,         (Long)tt.results.get(resultKey).get(key).value);
          assertNotEquals(treatment.results.get(resultKey).get(key).value, tt.results.get(resultKey).get(key).value);
          assertEquals   (t.results.get(resultKey).get(key).value,         tt.results.get(resultKey).get(key).value);
        });
    }

}
