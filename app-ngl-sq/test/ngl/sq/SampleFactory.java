package ngl.sq;

import static play.mvc.Http.Status.OK;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.play.test.DBObjectFactory;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.play.test.JsonHelper;
import fr.cea.ig.play.test.WSHelper;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.instance.Sample;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

// More like helpers atm
public class SampleFactory extends DBObjectFactory {

	public static final String samplesUrl = "/api/samples";
	public static final String res_00 = "data/sample_00";

	public static JsonNode apply(JsonNode n, Consumer<Sample> modification) {
		return apply(n,Sample.class,modification);
	}
	
	/**
	 * Generate a fresh JSON sample data from template 00.
	 * @param  code
	 * @return creation ready json node
	 */
	public static JsonNode create_00(String code) throws IOException {
		return apply(JsonHelper.getJson("data/sample_00"),
				s -> { 
					s._id              = null;
					s.traceInformation = null;
					s.code             = code;
				});
	}
	
	// Puts the sample in a state that allows creation
	public static Sample fresh(Sample s) {
		s._id              = null;
		s.traceInformation = null;
		s.code             = DevAppTesting.newCode();
		return s;
	}
	
	public static Sample from(String resourceName) throws IOException {
		return from(resourceName,Sample.class);
	}
	
	public static Sample freshInstance(WSClient ws, String resourceName) throws IOException {
		Sample sample = fresh(from(resourceName));
		WSResponse r = WSHelper.postObject(ws,samplesUrl,sample,OK);
		return Json.fromJson(Json.parse(r.getBody()), Sample.class);
	}
	
	public static Sample createSample(WSClient ws) {
		return createSample(ws, s -> { });
	}
	
	
	// typeCode importTypeCode 
	
	public static Sample createSample(WSClient ws, Consumer<Sample> init) {
		Sample sample = new Sample();
		sample.code           = DevAppTesting.newCode();
		sample.typeCode       = "DNA";
		sample.importTypeCode = "dna-reception"; 
		sample.categoryCode   = "DNA";
		sample.projectCodes   = new HashSet<>(Arrays.asList("BXL"));
		sample.properties     = new HashMap<>(); // <String,PropertyValue>();
		sample.properties.put("meta", new PropertySingleValue(false));
		init.accept(sample);
		WSHelper.postObject(ws,samplesUrl,sample,OK);
		DevAppTesting.rurNeqTraceInfo(ws, samplesUrl, sample);
		return sample;
	}
	
}
