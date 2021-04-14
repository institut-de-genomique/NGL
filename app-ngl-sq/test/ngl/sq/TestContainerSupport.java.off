//package ngl.sq;
//
//import java.util.ArrayList;
////import java.util.Arrays;
////import java.util.HashMap;
//import java.util.HashSet;
//
//import org.junit.Test;
//
//import fr.cea.ig.play.test.DevAppTesting;
//import fr.cea.ig.play.test.ReadUpdateReadObjectTest;
//import fr.cea.ig.play.test.WSHelper;
////import models.laboratory.common.instance.Comment;
////import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.State;
//import models.laboratory.common.instance.TraceInformation;
//import models.laboratory.common.instance.Valuation;
////import models.laboratory.common.instance.property.PropertySingleValue;
////import models.laboratory.container.instance.Container;
//import models.laboratory.container.instance.ContainerSupport;
////import models.laboratory.container.instance.Content;
////import models.laboratory.container.instance.LocationOnContainerSupport;
//import play.libs.Json;
//import play.libs.ws.WSResponse;
//
//import static play.mvc.Http.Status.OK;
//
//public class TestContainerSupport extends AbstractSQServerTest {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(TestContainerSupport.class);
//	
//	public static final String containerSupportsUrl = "/api/supports";
//	
//	@Test
//	public void testMinimalCreation() {
//		ContainerSupport support = new ContainerSupport();
//		support.code = DevAppTesting.newCode();
//		support.traceInformation = new TraceInformation();
//		support.traceInformation.forceCreationStamp("gogo");
//		support.state = new State();
//		support.comments        = new ArrayList<>();
//		support.projectCodes    = new HashSet<>();
//		support.sampleCodes     = new HashSet<>();
//		support.fromTransformationTypeCodes = new HashSet<>();
//		support.valuation       = new Valuation();
//		support.storages        = new ArrayList<>(); 
//		// TODO: use creation exposed from CRUD
//    	DevAppTesting.savage(support,ContainerSupport.class,models.utils.InstanceConstants.CONTAINER_SUPPORT_COLL_NAME);
//    	String url = containerSupportsUrl + "/" + support.code;
//    	WSResponse r = WSHelper.get(ws, url, OK);
//    	logger.debug(r.getBody());
//    	ContainerSupport x = Json.fromJson(Json.parse(r.getBody()), ContainerSupport.class);
//    	logger.debug("read container support instance " + x);
//    	ReadUpdateReadObjectTest.build(containerSupportsUrl + "/" + support.code,ContainerSupport.class)
//    		.run(ws);
//	}
//	// /api/supports/TESTVRDIFAPJFAAAA
//}
