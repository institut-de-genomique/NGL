package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import controllers.runs.api.RunsSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;

public class TestRunFactory {

    public static Run run(Experiment exp, String user) {
        InstrumentUsed instrument = new InstrumentUsed();
        instrument.code     = exp.instrument.code;
        instrument.typeCode = exp.instrument.typeCode;
        return run(user, 
                   exp.projectCodes, 
                   exp.sampleCodes, 
                   exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code, 
                   instrument,
                   exp.state.date);
    }
    
    /**
     * build a run like it is done in production (minimal object)
     * @param user          user login
     * @param projCodes     codes of projects
     * @param sampleCodes   codes of samples
     * @param supportCode   code of support of flowcell used
     * @param instrument    instrument used during sequencing
     * @param seqDate       sequencing date
     * @return              a Run
     */
    public static Run run(String user, Set<String>  projCodes, Set<String> sampleCodes, String supportCode, InstrumentUsed instrument, Date seqDate) {       
        Run run = new Run();
        run.code                 = DevAppTesting.newCode();
        run.typeCode             = "RHS4000";
        run.categoryCode         = "illumina";
        run.instrumentUsed       = instrument;
        run.containerSupportCode = supportCode;
        run.sequencingStartDate  = seqDate;
        // need to be defined after creation of readset(s)
        // run.sampleCodes          = sampleCodes;
        // run.projectCodes         = projCodes; 
        return run;
    }
    
    public static Run run(String user, String projCode, String sampleCode, String supportCode, InstrumentUsed instrument, Date seqDate) {
        return run(user, new HashSet<>(Arrays.asList(projCode)), new HashSet<>(Arrays.asList(sampleCode)), supportCode, instrument, seqDate);
    }
    
    public static Lane lane(Integer number, List<String> readSetCodes) {
        Lane lane = new Lane();
        lane.number = number;
        lane.readSetCodes = readSetCodes;
        return lane;
    }
    
    
    /* --- Wrappers --- */
    public static ListFormWrapper<Run> wrapper(String projCode) throws Exception {
        return wrapper(projCode, null, null);
    }
    
    public static ListFormWrapper<Run> wrapper(String projCode, QueryMode reporting, RenderMode render) throws Exception {
        RunsSearchForm form = new RunsSearchForm();
        return new TestListFormWrapperFactory<Run>().wrapping().apply(form, reporting, render);
    }

    public static Treatment savTreatment() {
        Map<String, Map<String, PropertyValue>> results = new HashMap<>();
        String key = "default";
        results.put(key, new HashMap<>());
        results.get(key).put("yieldTotal",             new PropertySingleValue(143.23));
        results.get(key).put("Q30PercTotal",           new PropertySingleValue(89.81));
        results.get(key).put("nonIndexedYieldTotal",   new PropertySingleValue(141.53));
        results.get(key).put("nonIndexedQ30PercTotal", new PropertySingleValue(90.48));
        Treatment t = treatment("sav", "sav", "sequencing", results);
        return t;
    }
    
    public static Treatment savTreatmentForLane() {
        Map<String, Map<String, PropertyValue>> results = new HashMap<>();
        
        String key = "read1";
        results.put(key, new HashMap<>());
        results.get(key).put("prephasing",          new PropertySingleValue(0.057));
        results.get(key).put("clusterPFPerc",       new PropertySingleValue(94.99));
        results.get(key).put("errorRatePercStd",    new PropertySingleValue(0.16));
        results.get(key).put("phasing",             new PropertySingleValue(0.076));
        results.get(key).put("greaterQ30Perc",      new PropertySingleValue(93.96));
        results.get(key).put("errorRatePerc",       new PropertySingleValue(1.28));
        results.get(key).put("clusterPFPercStd",    new PropertySingleValue(1.33));
        results.get(key).put("cyclesErrRated",      new PropertySingleValue(250));
        results.get(key).put("intensityCycle1",     new PropertySingleValue(4375));
        results.get(key).put("intensityCycle1Std",  new PropertySingleValue(311));
        results.get(key).put("alignedPercStd",      new PropertySingleValue(0.03));
        results.get(key).put("clusterDensityStd",   new PropertySingleValue(107));
        results.get(key).put("clusterDensity",      new PropertySingleValue(787));
        results.get(key).put("alignedPerc",         new PropertySingleValue(0.64));
        
        //results.put("read2", new HashMap<>());
        
        Treatment t = treatment("sav", "sav", "sequencing", results);
        return t;
    }
    
    
    protected static Treatment treatment(String code, String typeCode, String categoryCode, Map<String, Map<String, PropertyValue>> results) {
        Treatment t = new Treatment();
        t.code         = code;
        t.typeCode     = typeCode;
        t.categoryCode = categoryCode;
        t.results = results;
        return t;
    }

    public static Valuation valuation() {
        Valuation v = new Valuation();
        
        return v;
    }
}
