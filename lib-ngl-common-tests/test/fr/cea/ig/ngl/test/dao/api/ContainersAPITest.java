package fr.cea.ig.ngl.test.dao.api;

import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static fr.cea.ig.play.test.TestAssertions.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.Test;

import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RContainer;
import fr.cea.ig.play.test.TestAssertions;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import ngl.refactoring.state.ContainerStateNames;

public class ContainersAPITest {

	private static final play.Logger.ALogger logger = play.Logger.of(ContainersAPITest.class);
	
	// Tested API
	private static APIRef<ContainersAPI> api = APIRef.container;

	private static final CC2<Container, Container> containerResource = 
			RContainer.createContainerOnTubeFull
			.cc2((refSample,refContainerSupport,containerSupport,refContainer,container) -> T.t2(refContainer,container));
	
//	private static final CC2<Container,Container> usingData = TUResources.contextResource
//			.nest2((context) 						 -> containerResource)
//			.cc2(  (context, refContainer,container) -> T.t2(refContainer,container));
	private static final CC2<Container,Container> usingData = 
			RApplication.app
//			RApplication.appLog
//			.nest2((context) 						 -> containerResource)
			.and2 (containerResource)
			.cc2  ((context, refContainer,container) -> T.t2(refContainer,container));

	@Test
	public void createTest() throws Exception {
		usingData.accept((refContainer, data) -> {
			logger.debug("Creation test");
			assertEquals(refContainer.categoryCode,    data.categoryCode);
			assertEquals(refContainer.code,            data.code);
			assertEquals(refContainer.concentration,   data.concentration);
			assertEquals(refContainer.volume,          data.volume);
			assertEquals(refContainer.quantity,        data.quantity);
			assertEquals(refContainer.contents.size(), data.contents.size());
		});
	}
	
	// This should probably fail as the only valid coordinates are (1,1).
	@Test
	public void createTestTubeAt22() throws Exception {
		RApplication.app
		.then2(app -> RContainer.createTubeAt22)
		.accept((refContainer, data) -> {
			logger.debug("Creation test");
			assertEquals(refContainer.categoryCode,    data.categoryCode);
			assertEquals(refContainer.code,            data.code);
			assertEquals(refContainer.concentration,   data.concentration);
			assertEquals(refContainer.volume,          data.volume);
			assertEquals(refContainer.quantity,        data.quantity);
			assertEquals(refContainer.contents.size(), data.contents.size());
			logger.debug("container location {} {}", data.support.line, data.support.column);
		});
	}
	
	@Test
	public void createTestWellOnTube() throws Exception {
		TestAssertions.assertThrows(APIValidationException.class,
				() -> RApplication.app
				.then2(app -> RContainer.createWellOnTube)
				.accept((refContainer, data) -> {
					logger.debug("Creation test");
					assertEquals(refContainer.categoryCode,    data.categoryCode);
					assertEquals(refContainer.code,            data.code);
					assertEquals(refContainer.concentration,   data.concentration);
					assertEquals(refContainer.volume,          data.volume);
					assertEquals(refContainer.quantity,        data.quantity);
					assertEquals(refContainer.contents.size(), data.contents.size());
					logger.debug("container location {} {}", data.support.line, data.support.column);
				})
		);
	}
	
	@Test
	public void createTestWellAt99() throws Exception {
		RApplication.app
		.then2(app -> RContainer.createTubeAt22)
		.accept((refContainer, data) -> {
			logger.debug("Creation test");
			assertEquals(refContainer.categoryCode,    data.categoryCode);
			assertEquals(refContainer.code,            data.code);
			assertEquals(refContainer.concentration,   data.concentration);
			assertEquals(refContainer.volume,          data.volume);
			assertEquals(refContainer.quantity,        data.quantity);
			assertEquals(refContainer.contents.size(), data.contents.size());
			logger.debug("container location {} {}", data.support.line, data.support.column);
		});
	}

	@Test
	public void updateTest() throws Exception {
		logger.debug("Update test");
		usingData.accept((refContainer, data) -> {
			Container contToUpdate                   = data;
			final double newVol                      = TestContainerFactory.VOL * 2;
			final double newConcentration            = TestContainerFactory.QUANTITY / newVol;
			contToUpdate.volume.value                = newVol;
			contToUpdate.concentration.value         = newConcentration;
			contToUpdate.traceInformation.modifyUser = RConstant.USER;
			contToUpdate.traceInformation.modifyDate = new Date();
			Container cont = api.get().update(contToUpdate, RConstant.USER);
			
			assertEquals   (refContainer.categoryCode,  cont.categoryCode);
			assertNotEquals(refContainer.volume,        cont.volume);
			assertEquals   (contToUpdate.volume,        cont.volume);
			assertNotEquals(refContainer.concentration, cont.concentration);
			assertEquals   (contToUpdate.concentration, cont.concentration);
		});
	}
	
	@Test
	public void changeCategoryCodeTest() throws Exception {
		logger.debug("Try setting container categoryCode to wrong value");
		usingData.accept((refContainer, data) -> {
			Container contToUpdate    = data;
			contToUpdate.categoryCode = TestContainerFactory.WELL;
			assertThrows(APIValidationException.class, () -> api.get().update(contToUpdate, RConstant.USER));
		});
	}
	
	@Test
	public void updateFieldsTest() throws Exception {
		logger.debug("Update only some fields test");
		usingData.accept((refContainer, data) -> {
			Container contToUpdate        = new Container();
			contToUpdate.code             = data.code;
			final double newVol           = TestContainerFactory.VOL * 2;
			final double newConcentration = TestContainerFactory.QUANTITY / newVol;
			contToUpdate.volume           = new PropertySingleValue(newVol, "µl");
			contToUpdate.concentration    = new PropertySingleValue(newConcentration, "µl");
			Container cont = api.get().update(contToUpdate, RConstant.USER, Arrays.asList("volume", "concentration"));
			
			assertEquals   (refContainer.categoryCode,  cont.categoryCode);
			assertNotEquals(refContainer.volume,        cont.volume);
			assertEquals   (contToUpdate.volume,        cont.volume);
			assertNotEquals(refContainer.concentration, cont.concentration);
			assertEquals   (contToUpdate.concentration, cont.concentration);
		});
	}
	
	@Test
	public void deleteTest() throws Exception {
		usingData.accept((refContainer, data) -> {
			logger.debug("Delete test");
			api.get().delete(refContainer.code);
			assertNull(api.get().get(refContainer.getCode()));
		});
	}

	@Test
	public void getTest() throws Exception {
		logger.debug("Get test");
		usingData.accept((refContainer, data) -> {
			Container cont = api.get().get(refContainer.code);
			assertNotNull(cont);
			assertEquals(data.get_id(),          cont.get_id());
			assertEquals(refContainer.getCode(), cont.getCode());
		});
	}
	
	@Test
	public void updateStateTest() throws Exception {
		logger.debug("update state test");
		usingData.accept((refContainer, data) -> {
			State state = new State(ContainerStateNames.IS, RConstant.USER);
			Container cont = api.get().updateState(refContainer.code, state, RConstant.USER);
			
			assertEquals(state.code, cont.state.code);
		});
	}
		
	@Test
	public void listTest() throws Exception {
		logger.debug("List test");
		usingData.accept((refContainer, data) -> {
			//---------- default mode ----------
			logger.debug("default mode");
			final String projCode = refContainer.projectCodes.iterator().next();
			ListFormWrapper<Container> wrapper = TestContainerFactory.wrapper(projCode);
			
			Consumer<Container> containerAssertions = 
					  c -> {
						  assertEquals(refContainer.code,         c.code);
						  assertEquals(refContainer.categoryCode, c.categoryCode);
					  };
					  
			assertOne(api.get().listObjects(wrapper), containerAssertions);
			
			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestContainerFactory.wrapper(projCode, QueryMode.REPORTING, null);
			assertOne(api.get().listObjects(wrapper), containerAssertions);

		});
	}
	
}
