//package ngl.sq;
//
//import org.junit.Test;
//import static play.mvc.Http.Status.BAD_REQUEST;
//import static play.mvc.Http.Status.NOT_FOUND;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//
//import fr.cea.ig.play.test.DevAppTesting;
//import fr.cea.ig.play.test.WSHelper;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.State;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import models.laboratory.container.description.ContainerCategory;
//import models.laboratory.container.description.ContainerSupportCategory;
//import models.laboratory.container.description.dao.ContainerCategoryDAO;
//import models.laboratory.container.instance.Container;
//import models.laboratory.container.instance.ContainerSupport;
//import models.laboratory.container.instance.Content;
//import models.laboratory.container.instance.LocationOnContainerSupport;
//import models.laboratory.sample.instance.Sample;
//
//// TODO: comment
//
//public class TestContainers extends AbstractSQServerTest {
//
//	private static final play.Logger.ALogger logger = play.Logger.of(TestContainers.class);
//	
//	private static final String containersUrl = "/api/containers";
//	
//	@Test
//	public void testCreation() throws IOException {
//		// Need to create a new sample, or fetch one
//		Sample sample = SampleFactory.freshInstance(ws, SampleFactory.res_00);
//		Container container = ContainerFactory.freshInstance(ws,ContainerFactory.res_00,sample);
//		// Check that the container instance at least passes a RUR test.  
//		DevAppTesting.rurNeqTraceInfo(ws, containersUrl, container);
//	}
//
//	@Test
//	public void testNoStateTransition() throws IOException {
//		Sample sample = SampleFactory.freshInstance(ws, SampleFactory.res_00);
//		Container container = ContainerFactory.freshInstance(ws,ContainerFactory.res_00,sample);
//		container = WSHelper.getObject(ws,containersUrl + "/" + container.getCode(),Container.class);
//		container.state.code = "ZZZZ";
//		WSHelper.putObject(ws,containersUrl + "/" + container.getCode(),container,BAD_REQUEST);
//	}
//	
//	@Test
//	public void testNotFound() {
//		WSHelper.get(ws,containersUrl + "/NOT_FOUND",NOT_FOUND);
//	}
//
//	@Test
//	public void testMinimalCreation() {
//		logger.debug("** categories and states");
//		for (ContainerCategory c : ContainerCategory.find.findAll())
//			logger.debug("container category " + c.code);
//		for (ContainerSupportCategory c : ContainerSupportCategory.find.findAll())
//			logger.debug("support category   " + c.code);
//		for (models.laboratory.common.description.State s : models.laboratory.common.description.State.find.findAll())
//			logger.debug("state              " + s.code);
//		logger.debug("**");	
//		
//		Sample sample = SampleFactory.createSample(ws);
//		logger.debug("created sample {}",sample.code);
//		
//		ContainerSupport support = new ContainerSupport();
//		support.code = DevAppTesting.newCode();
//		
//		Container container = new Container();
//		container.code = DevAppTesting.newCode();
//		// We do manual creation stamping
//		container.getTraceInformation().forceCreationStamp("aloa");
//		// Embedded state initialization, requires proper state contruction.
//		container.state = new State();
//		container.state.code  = "IW-P";
//		container.state.user  = "aloa";
//		
//		container.sampleCodes = new HashSet<String>(Arrays.asList(sample.code));
//		container.properties  = new HashMap<>(); // <String,PropertyValue>();
//		container.properties.put("meta", new PropertySingleValue(false));
//		container.contents = new ArrayList<Content>();
//		container.contents.add(ContentFactory.createContent(sample, 100, "BXL"));
//		// Union of sample projects
//		container.projectCodes = sample.projectCodes; 
//		
//		container.categoryCode = "tube";
//		container.support = new LocationOnContainerSupport();
//		container.support.code = container.code;
//		container.support.categoryCode = "tube";
//		
//		// TODO: use creation exposed from CRUD
//    	DevAppTesting.savage(container,Container.class,models.utils.InstanceConstants.CONTAINER_COLL_NAME);
//    	DevAppTesting.rurNeqTraceInfo(ws, containersUrl, container);
//	}
//	
//}
