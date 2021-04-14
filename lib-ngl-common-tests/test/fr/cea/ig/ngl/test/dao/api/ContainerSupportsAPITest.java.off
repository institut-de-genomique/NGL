package fr.cea.ig.ngl.test.dao.api;

import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static fr.cea.ig.util.function.T.t2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import fr.cea.ig.ngl.dao.containers.ContainerSupportsAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RContainer;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import ngl.refactoring.state.ContainerStateNames;

public class ContainerSupportsAPITest {

	private static final play.Logger.ALogger logger = play.Logger.of(ContainerSupportsAPITest.class);
	private static final String USER                = RConstant.USER;	
	
	// Tested API
	private static APIRef<ContainerSupportsAPI> api = APIRef.containerSupport;

	private static final CC2<ContainerSupport, ContainerSupport> supportResource = 
			RContainer.createContainerOnTubeFull
			.cc2((refSample,refContainerSupport,containerSupport,refContainer,container) -> t2(refContainerSupport,containerSupport));

	private static final CC2<ContainerSupport,ContainerSupport> testData = 
			RApplication.contextResource
			.nest2((context) 						 			 -> supportResource)
			.cc2((context, refContainerSupport,containerSupport) -> t2(refContainerSupport,containerSupport));
	
	public static final CC2< ContainerSupport, List<Container>> plateResource = 
			RApplication.contextResource
			.and2(RContainer.createPlateResource)
			.cc2 ((context,containerSupport,containers) -> t2(containerSupport,containers));
			
	@Test
	public void createTest() throws Exception {
		testData.accept((refContainerSupport, containerSupport) -> {
			logger.debug("creation test");
			assertEquals(refContainerSupport.categoryCode, containerSupport.categoryCode);
			assertEquals(refContainerSupport.code,         containerSupport.code);
			assertEquals(refContainerSupport.nbContainers, containerSupport.nbContainers);
			assertEquals(refContainerSupport.nbContents,   containerSupport.nbContents);
		});
	}
	
	@Test
	public void updateTest() throws Exception {
		logger.debug("update test");
		testData.accept((refContainerSupport, containerSupport) -> {
			ContainerSupport supportToUpdate = containerSupport;
			supportToUpdate.nbContainers     = 3;
			String storageCode               = "Bt20_70_A1";
			supportToUpdate.storageCode      = storageCode;
			ContainerSupport support = api.get().update(supportToUpdate, USER);			
			
			assertEquals   (refContainerSupport.categoryCode, support.categoryCode);
			assertNotEquals(refContainerSupport.nbContainers, support.nbContainers);
			assertEquals   (supportToUpdate.nbContainers,     support.nbContainers);
			assertNotEquals(refContainerSupport.storageCode,  support.storageCode);
		});
	}
	
	@Test 
	public void updateFieldsTest() throws Exception {
		logger.debug("update only some fields test");
		testData.accept((refContainerSupport, containerSupport) -> {
			ContainerSupport supportToUpdate = new ContainerSupport();
			supportToUpdate.code             = containerSupport.code;
			String storageCode               = "Bt20_70_A1";
			supportToUpdate.storageCode      = storageCode;
			ContainerSupport support = api.get().update(supportToUpdate, USER, Arrays.asList("storageCode"));
			
			assertNotEquals(refContainerSupport.storageCode, support.storageCode);
			assertEquals   (storageCode,                     support.storageCode);
			assertEquals   (storageCode,                     support.storageCode);
			assertEquals   (1,                               support.storages.size());
		});
	}
	
	@Test
	public void deleteTest() throws Exception {
		testData.accept((refContainerSupport, data) -> {
			api.get().delete(data.code);
			assertNull(api.get().get(data.getCode()));
		});
	}

	@Test
	public void getTest() throws Exception {
		logger.debug("get test");
		testData.accept((refContainerSupport, data) -> { 
			ContainerSupport support = api.get().get(refContainerSupport.code);
			assertNotNull(support);
			assertEquals(data.get_id(),  support.get_id());
			assertEquals(data.getCode(), support.getCode());
		});
	}
	
	@Test
	public void updateStateTest() throws Exception {
		logger.debug("update state test");
		testData.accept((refContainerSupport, data) -> {
			State state = new State(ContainerStateNames.IS, USER);
			ContainerSupport cont = api.get().updateState(refContainerSupport.code, state, USER);

			assertEquals(state.code, cont.state.code);
		});
	}
	
	@Test
	public void listTest() throws Exception {
		logger.debug("List test");
		testData.accept((refContainerSupport, data) -> {
			
			//---------- default mode ----------
			logger.debug("default mode");
			final String projCode = refContainerSupport.projectCodes.iterator().next();
			ListFormWrapper<ContainerSupport> wrapper = TestContainerFactory.wrapperSupport(projCode);
			
			Consumer<ContainerSupport> containerSupportAssertions =
					  c -> {
						  assertEquals(refContainerSupport.code, c.code);
						  assertEquals(refContainerSupport.categoryCode, c.categoryCode);
					  };	
			
			assertOne(api.get().listObjects(wrapper), containerSupportAssertions);

			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestContainerFactory.wrapperSupport(projCode, QueryMode.REPORTING, null);

			assertOne(api.get().listObjects(wrapper), containerSupportAssertions);
		});
	}
	
}
