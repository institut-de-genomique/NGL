package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.HashMap;
import java.util.Set;

import controllers.processes.api.ProcessesSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.processes.instance.Process;

public class TestProcessFactory {
	
	public static Process processQC(String containerCode, String supportCode) {
		Process p      = process(containerCode, supportCode);
		p.categoryCode = "satellites";
		p.typeCode     = "qc-transfert-purif";
		return p;
	}
	
	private static Process process(String containerCode, String supportCode) {
		Process p                   = new Process();
		p.inputContainerCode        = containerCode;
		p.inputContainerSupportCode = supportCode;
		return p;
	}

	public static Process processTransformationIllumina(String containerCode, String supportCode) {
		Process p      = process(containerCode, supportCode);
		p.categoryCode = "sequencing";
		p.typeCode     = "norm-fc-depot-illumina";
		
		// required properties
		p.properties = new HashMap<>();
		p.properties.put("sequencingType", 			new PropertySingleValue("Hiseq 4000"));
		p.properties.put("devProdContext", 			new PropertySingleValue("PROD"));
		p.properties.put("readLength", 	  			new PropertySingleValue("undefined"));
		p.properties.put("estimatedPercentPerLane", new PropertySingleValue("20"));
		p.properties.put("readType", 				new PropertySingleValue("PE"));
		
		return p;
	}
	
	public static Process processTransformationNanopore(String containerCode, String supportCode) {
        Process p = process(containerCode, supportCode);
        p.categoryCode = "nanopore-library";
        p.typeCode = "nanopore-rependprep-lig-depot-process";
        
        // required properties
        p.properties = new HashMap<>();
        p.properties.put("libProcessTypeCode", new PropertySingleValue("ONT"));
        p.properties.put("devProdContext",     new PropertySingleValue("PROD"));
        
        return p;
    }
	
	
	public static ListFormWrapper<Process> wrapper(Set<String> projCodes, QueryMode reporting, RenderMode render, String categoryCode) throws Exception {
		ProcessesSearchForm form = new ProcessesSearchForm();
		form.categoryCode = categoryCode;
		form.projectCodes = projCodes;
		return new TestListFormWrapperFactory<Process>().wrapping().apply(form, reporting, render);
	}
	
	public static ListFormWrapper<Process> wrapper(Set<String> projCodes, String categoryCode) throws Exception {
		return wrapper(projCodes, null, null, categoryCode);
	}
}
