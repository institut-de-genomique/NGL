package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import controllers.readsets.api.ReadSetValuation;
import controllers.readsets.api.ReadSetsSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import ngl.refactoring.state.ReadSetStateNames;


public class TestReadsetFactory {

//    private static final String SEPARATOR = "_";

    public static ReadSet readset(String user,
                                  String typeCode,
                                  String runCode,
                                  String runTypeCode,
                                  String sampleCode,
                                  Integer laneNumber,
                                  String containerSupportCode,
                                  String seqTechCode, 
                                  String projectCode) {
        ReadSet readset     = new ReadSet();
        readset.runCode     = runCode;
        readset.runTypeCode = runTypeCode;
        readset.laneNumber  = laneNumber;
        readset.sampleCode  = sampleCode;
        readset.projectCode = projectCode;
        readset.code        = DevAppTesting.newCode();
        readset.typeCode    = typeCode;
        readset.location    = "TEST"; //TODO AJ arbitrary value maybe change to TEST ?
        readset.path        = "/path/test";
        return readset;
    }

    
    
    public static ReadSet readsetIllumina(String user, Run run, Integer laneNumber) {
        return readset(user,
                       "rsillumina",
                       run.code,
                       run.typeCode,
                       run.sampleCodes.iterator().next(),
                       laneNumber,
                       run.containerSupportCode,
                       "rsillumina",
                       run.projectCodes.iterator().next());
    }
    
    public static ReadSet readsetNanopore(String user, Run run, Integer laneNumber) {
        return readset(user,
                       "rsnanopore",
                       run.code,
                       run.typeCode,
                       run.sampleCodes.iterator().next(),
                       laneNumber,
                       run.containerSupportCode,
                       "ONT",
                       run.projectCodes.iterator().next());
    }
    
	public static ListFormWrapper<ReadSet> wrapper(String projCode) throws Exception {
		return wrapper(projCode, QueryMode.MONGOJACK, null);
	}

	public static ListFormWrapper<ReadSet> wrapper(String projCode, QueryMode queryMode, RenderMode render) throws Exception {
		ReadSetsSearchForm form = new ReadSetsSearchForm();
		form.projectCode = projCode;
		form.includes = new HashSet<>(Arrays.asList("default"));
		if (queryMode != null && queryMode.equals(QueryMode.REPORTING)) {
    		form.reportingQuery = "{$or:"
                    + "["
                        + "{$and:["
                            + "{\"projectCode\":\""+projCode+"\","
                            + "\"state.code\":{$in:[\""+ReadSetStateNames.N+"\"]}"
                            + "}"
                            + "]},"
                        + "{\"typeCode\":\"rsillumina\"}"
                    + "]"
                    + "}";
		}
		return new TestListFormWrapperFactory<ReadSet>().wrapping().apply(form, queryMode, render);
	}
	
//	/**
//     * @param sampleCode            code of sample
//     * @param laneNumber            the lane index
//     * @param containerSupportCode  the code of flowcell
//     * @param seqTechCode           the code of sequencing technology used
//     * @return                      a readset code
//     */
//    private static String readsetCodeGenerator(String sampleCode,
//                                               Integer laneNumber,
//                                               String containerSupportCode,
//                                               String seqTechCode) {
//        return sampleCode + SEPARATOR + seqTechCode + SEPARATOR + laneNumber.intValue() + SEPARATOR + containerSupportCode;
//    }



    public static ReadSetValuation valuation(String user) {
        ReadSetValuation rv            = new ReadSetValuation();
        rv.productionValuation         = new Valuation();
        rv.productionValuation.comment = "usefull comment";
        rv.productionValuation.date    = new Date();
        rv.productionValuation.user    = user;
        rv.productionValuation.valid   = TBoolean.TRUE;
        rv.productionValuation.user    = user;
        
        rv.bioinformaticValuation         = new Valuation();
        rv.bioinformaticValuation.comment = "usefull comment";
        rv.bioinformaticValuation.date    = new Date();
        rv.bioinformaticValuation.user    = user;
        rv.bioinformaticValuation.valid   = TBoolean.TRUE;
        rv.bioinformaticValuation.user    = user;
        return rv;
    }
    
    
    /**
     * @return a RAW ReadSet File
     */
    public static File rawFile() {
        File file = file("RAW", "fastq", "test.fastq", false, "READ1");
        return file;
    }
    
    /**
     * @return a CLEAN ReadSet File
     */
    public static File cleanFile() {
        File file = file("CLEAN", "fastq.gz", "test.fastq.gz", true, "READ1");
        return file;
    }
    
    public static File file(String typeCode, String extension, String fullname, boolean usable, String label) {
        File file = new File();
        file.extension = extension;
        file.fullname = fullname;
        file.typeCode = typeCode;
        file.usable = usable;
        file.properties = new HashMap<>();
        file.properties.put("label", new PropertySingleValue(label));
        file.properties.put("asciiEncoding", new PropertySingleValue("33"));
        return file;
    }
    
    /**
     * @return a global treatment
     */
    public static Treatment globalTreatment() {
        Map<String, Map<String, PropertyValue>> results = new HashMap<>();
        results.put("default", new HashMap<>());
        results.get("default").put("usefulSequences", new PropertySingleValue(5));
        results.get("default").put("usefulBases",     new PropertySingleValue(2500));
        Treatment t = TestRunFactory.treatment("global", "global", "global", results);
        return t;
    }
    
    

}
