package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;

import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;

public class TestAnalysesFactory {

    public static Analysis analysis(String user, ReadSet readset) {
        Analysis analysis = new Analysis();
        analysis.code = DevAppTesting.newCode();
        analysis.typeCode = "dietetic-assembly";
        analysis.valuation = valuation();
        analysis.masterReadSetCodes = Arrays.asList(readset.code);
        analysis.readSetCodes = Arrays.asList(readset.code);
        analysis.path = "/testing/path";
        return analysis;
    }

    private static Valuation valuation() {
        Valuation v = new Valuation();
        v.valid = TBoolean.UNSET;
        return v;
    }
    
}
