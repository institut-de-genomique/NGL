package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.test.resource.RAnalysis;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;

public class AnalysesAPITest {

    // Tested API
    private static APIRef<AnalysesAPI> api = APIRef.analysis;

//    private static final CC2<Analysis, Analysis> data = RAnalysis.createIlluminaAnalysisRWC
//            .cc2((context, exp, run, readset, refAnalysis, analysis) -> T.t2(refAnalysis, analysis));

    private static final CC2<Analysis, Analysis> data = 
    		RApplication.contextResource
    		.nest5(RAnalysis::createIlluminaAnalysisRWC)
            .cc2  ((context, exp, run, readset, refAnalysis, analysis) -> T.t2(refAnalysis, analysis));

    //@Test // EJACOBY (AJ) ressource non fonctionnelle pour le moment 
    //il faudra réactiver le test lorsque celle-ci le sera
    public void createTest() throws Exception {
        data.accept((refAnalysis, analysis) -> {
            assertEquals(refAnalysis.masterReadSetCodes.get(0), analysis.masterReadSetCodes.get(0));
            assertEquals(refAnalysis.readSetCodes.get(0), analysis.readSetCodes.get(0));
        });
    }

    //@Test // EJACOBY (AJ) ressource non fonctionnelle pour le moment 
    //il faudra réactiver le test lorsque celle-ci le sera
    public void valuationTest() throws Exception {
        data.accept((refAnalysis, analysis) -> {
            Valuation val = new Valuation();
            val.date = new Date();
            val.user = RConstant.USER;
            val.valid = TBoolean.FALSE;
            Analysis a = api.get().valuation(analysis.code, val, RConstant.USER);
            assertEquals(analysis.code, a.code);
            assertNotEquals(analysis.valuation.valid.value, a.valuation.valid.value);
            assertEquals(val.valid.value, a.valuation.valid.value);

        });
    }
    
}
