//package ngl.sq;
//
//
//import static fr.cea.ig.play.test.DevAppTesting.cr;
//import static fr.cea.ig.play.test.DevAppTesting.newCode;
//import static fr.cea.ig.play.test.DevAppTesting.rurNeqTraceInfo;
//import static play.mvc.Http.Status.OK;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
//import static play.mvc.Http.Status.BAD_REQUEST;
//import static play.mvc.Http.Status.NOT_FOUND;
//import static ngl.sq.SampleFactory.from;
//import static ngl.sq.SampleFactory.fresh;
//import static ngl.sq.SampleFactory.create_00;
//import static ngl.sq.SampleFactory.samplesUrl;
//import static ngl.sq.SampleFactory.res_00;
//
//import org.junit.Test;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import fr.cea.ig.play.test.DevAppTesting;
//import fr.cea.ig.play.test.WSHelper;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import models.laboratory.sample.instance.Sample;
//
//public class TestSamples extends AbstractSQServerTest {
//
//	
//	@Test
//	public void testCreation_00() throws IOException {
//		JsonNode sample_0 = create_00(newCode());
//		cr(ws,samplesUrl,sample_0);
//		rurNeqTraceInfo(ws,samplesUrl,sample_0);
//	}
//
//	@Test
//	public void testTemplateFail() throws IOException {
//		// Template data has the id and the creation date that will
//		// make the creation fail. We leave the id that fails.
//		Sample sample = from(res_00);
//		WSHelper.postObject(ws,samplesUrl,sample,BAD_REQUEST);
//	}
//	
//	@Test
//	public void testTraceInfoFail() throws IOException {
//		// Template data has the id and the creation date that will
//		// make the creation fail.
//		Sample sample = from(res_00);
//		sample._id = null;
//		WSHelper.postObject(ws,samplesUrl,sample,BAD_REQUEST);
//	}
//	
//	@Test
//	public void testFresh() throws IOException {
//		Sample sample = fresh(from(res_00));
//		WSHelper.postObject(ws,samplesUrl,sample,OK);
//	}
//	
//	// We would expect this to fail as there is no taxon code defined.
//	// Could be correct though.
//	@Test
//	public void testNoTaxon() throws IOException {
//		Sample sample = fresh(from(res_00));
//		sample.taxonCode = null;
//		// WSHelper.postObject(ws,samplesUrl,sample,BAD_REQUEST);
//		WSHelper.postObject(ws,samplesUrl,sample,OK);
//	}
//	
//	// the get method in the base controller returns NOT_FOUND, all the
//	// other methods return BAD_REQUEST
//	@Test
//	public void testNotFound() {
//		WSHelper.get(ws,samplesUrl + "/NOT_FOUND",NOT_FOUND);
//	}
//	// ngl-common taxon is supposedly working
//	// could fecth the taxon by id.
//	@Test
//	public void testMinimalCreation() {
//		SampleFactory.createSample(ws);
//	}
//
//}
