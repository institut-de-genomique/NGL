package utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import play.libs.Json;

public class RunMockHelper {
	
	public static JsonNode getJsonRun(Run run) {
		return Json.toJson(run);
	}
	
	public static JsonNode getJsonLane(Lane lane) {
		return Json.toJson(lane);
	}
	
	public static JsonNode getJsonReadSet(ReadSet readSet) {
		return Json.toJson(readSet);
	}
	
	public static JsonNode getJsonFile(File file) {
		return Json.toJson(file);
	}
	
	public static JsonNode getJsonTreatment(Treatment treatment) {
		return Json.toJson(treatment);
	}
	
	public static JsonNode getJsonState(State state) {
		return Json.toJson(state);
	}
	
	
	

	
	
	public static Run newRun(String code){
		Run run = new Run();
		run.code = code;
		run.containerSupportCode = "containerName";
		run.dispatch = true;
		
		run.instrumentUsed = new InstrumentUsed();
		run.instrumentUsed.code = "HISEQ2";
		run.instrumentUsed.typeCode = "HISEQ2000";
		
		run.typeCode = "RHS2000";
		run.categoryCode = "illumina";
		State state = new State();
		run.state = state;
		run.state.code = "F";
		run.state.user = "tests";
		run.state.date = new Date();
		
		run.valuation = getValuation(TBoolean.UNSET);
		
		List<String> lResos = new ArrayList<String>();
		lResos.add("reso1");
		lResos.add("reso2");
		
		TraceInformation ti = new TraceInformation(); 
		ti.setTraceInformation("dnoisett");
		run.traceInformation = ti; 

		return run;
	}
	
	public static Lane newLane(int number){
		Lane lane = new Lane();
		lane.number = number;
		
		List<String> lResos = new ArrayList<String>();
		lResos.add("reso1");
		lResos.add("reso2");		

				
		lane.valuation = getValuation(TBoolean.UNSET);
				
		return lane;
	}
	
	
	public static Sample newSample(String code) {
		Sample s = new Sample();
		s.code = "SampleCode";
		s.typeCode = "sampleType";
		s.categoryCode = "sampleCategory";
		s.name = "sampleName";
		s.referenceCollab = "Ref collab";
		
		s.valuation = new Valuation();
		s.valuation.valid = TBoolean.UNSET;
		
		Set<String> lp = new HashSet<String>();
		lp.add("ProjectCode");
		s.projectCodes = lp;
		
		TraceInformation ti = new TraceInformation(); 
		ti.setTraceInformation("dnoisett");
		s.traceInformation = ti;
		
		return s;
	}
	
	public static SampleOnContainer newSampleOnContainer(String sampleCode)
	{
		SampleOnContainer soc = new SampleOnContainer();
		soc.sampleCode=sampleCode;
		soc.containerSupportCode="support";
		soc.properties=new HashMap<String, PropertyValue>();
		soc.properties.put("libProcessTypeCode", new PropertySingleValue("W"));
		return soc;
	}
	
	
	public static Project newProject(String code) {
		Project p = new Project();
		
		p.code = "ProjectCode";
		TraceInformation ti = new TraceInformation(); 
		ti.setTraceInformation("dnoisett");
		p.traceInformation = ti;
		//new
		p.bioinformaticParameters = new BioinformaticParameters();
		p.bioinformaticParameters.biologicalAnalysis = Boolean.FALSE; 
		
		return p;
	}
	
	public static ReadSet newReadSet(String code){
		ReadSet r = new ReadSet();
		r.code = code;
		r.path = "/";
		r.sampleCode = "SampleCode";
		r.projectCode = "ProjectCode";
		r.dispatch = false;
		r.laneNumber = 1;
		
		
		r.state = getState("F-QC");
		Set<String> lResos = new HashSet<String>();
		lResos.add("reso1");
		lResos.add("reso2");
		r.state.resolutionCodes=lResos;
		TraceInformation ti = new TraceInformation(); 
		ti.setTraceInformation("dnoisett");
		r.traceInformation = ti; 
		
		
		
		r.typeCode = "default-readset"; 
		
		r.bioinformaticValuation = getValuation(TBoolean.TRUE);
		
		r.productionValuation = getValuation(TBoolean.TRUE);
		
		
		return r;
	}
	
	public static JsonNode getArchiveJson(String archiveId){
		 System.out.println( Json.newObject().textNode("archiveId:"+archiveId));
		 //System.out.println("parsed::"+Json.parse("[{\"archiveId\":\""+archiveId+"\"}]"));
		 return Json.parse("{\"archiveId\":\""+archiveId+"\"}");
		 
	 }
	 

	
	public static File newFile(String code){
		File file = new File();
		file.fullname = code;
		file.extension = ".exe";
		file.typeCode = "42";
		file.usable = true;
		
		file.properties.put("label", new PropertySingleValue("thelabel"));
		file.properties.put("asciiEncoding", new PropertySingleValue("xxx"));
		
		return file;
		
	}
	public static Valuation getValuation(TBoolean b) {
			Valuation v = new Valuation();
			v.valid = b;
			v.date = new Date();
			v.user = "test";
			return v;
	}
	
	public static State getState(String code) {
		State state = new State();
		state.code = code;
		state.user = "tests";
		state.date = new Date();
		return state;
}
}
