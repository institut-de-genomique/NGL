package fr.cea.ig.ngl.test.dao.api;

import static fr.cea.ig.play.test.TestAssertions.assertOne;
import static fr.cea.ig.play.test.TestAssertions.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.Test;

import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestExperimentFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RExperiment;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.experiment.instance.Experiment;
import ngl.refactoring.state.ExperimentStateNames;

public class ExperimentsAPITest {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsAPITest.class);
	
	// Tested API
	private static APIRef<ExperimentsAPI> api 	 = APIRef.experiment;
	
	public static final CC2<Experiment, Experiment> createExpQCWithTube =
			RApplication.contextResource
//			.nest2(RExperiment::createExpQCWithTubeRWC)
//			.cc2  ((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
			.then2(RExperiment::createExpQCWithTube);
	
	public static final CC2<Experiment, Experiment> createExpQCWithPlate = 
			RApplication.contextResource
//			.nest2(RExperiment::createExpQCWithPlateRWC)
//			.cc2  ((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
			.then2(RExperiment::createExpQCWithPlate);
	
	public static final CC2<Experiment, Experiment> createExpQCWithPlateAndTube = 
			RApplication.contextResource
//			.nest2(RExperiment::createExpQCWithPlateAndTubeRWC)
//			.cc2  ((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
			.then2(RExperiment::createExpQCWithPlateAndTube);
	
//	public static final CC2<Experiment, Experiment> createExpDepotWithPlateRWC(CC1<TestContext> cc) {
////		RExperiment.createExpDepotWithPlateRWC
//		return RExperiment.createExpDepotWithPlateRWC(cc)
//			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
//	}
	
//	// This is an alias
//	@Deprecated
//	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpDepotWithPlateRWC(C ctx) {
//		return RExperiment.createExpDepotWithPlateRWC(ctx);
//	}

//	public static final CC2<Experiment, Experiment> createExpTransfertWithPlateRWC(CC1<TestContext> cc) {
////		RExperiment.createExpTransfertWithPlateRWC(RApplication.contextResource)
//		return RExperiment.createExpTransfertWithPlateRWC(cc)
//		.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
//	}
//	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpTransfertWithPlateRWC(C ctx) {
//		return RExperiment.createExpTransfertWithPlateRWC(ctx);
//	}
	
	@Test
	public void createTest() throws Exception {
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			logger.debug("Creation test");
			assertEquals(refExperiment.categoryCode, experiment.categoryCode);
			assertEquals(refExperiment.code,         experiment.code);
			assertEquals(refExperiment.projectCodes, experiment.projectCodes);
			assertEquals(refExperiment.sampleCodes,  experiment.sampleCodes);
			assertEquals(refExperiment.instrument,   experiment.instrument);
		});
	}
	
	@Test
	public void deleteTest() throws Exception {
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			logger.debug("Delete test");
			api.get().delete(refExperiment.code);
			assertNull(api.get().get(refExperiment.getCode()));
		});
	}
	
	@Test
	public void getTest() throws Exception {
		logger.debug("Get test");
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			Experiment exp = api.get().get(refExperiment.code);
			assertNotNull(exp);
			assertEquals(experiment.get_id(),     exp.get_id());
			assertEquals(refExperiment.getCode(), exp.getCode());
		});
	}
	
    @Test
    public void updateTest() throws Exception {
        createExpQCWithTube.accept((refExperiment, experiment) -> {
            logger.debug("Update Test");
            experiment.comments = new ArrayList<>();
            String text = "TEST";
            experiment.comments.add(new Comment(text, RConstant.USER));
            Experiment exp = api.get().update(experiment, RConstant.USER);

            assertEquals(1, exp.comments.size());
            assertEquals(text, exp.comments.get(0).comment);
        });
    } 
	
	@Test
	public void listTest() throws Exception {
		logger.debug("List test");
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			//---------- default mode ----------
			logger.debug("default mode");
			final String projCode = refExperiment.projectCodes.iterator().next();
			ListFormWrapper<Experiment> wrapper = TestExperimentFactory.wrapper(projCode);
			
			Consumer<Experiment> experimentAssertions =
			 		  exp -> {
			 			  assertEquals(refExperiment.code, exp.code);
			 			  assertEquals(refExperiment.categoryCode, exp.categoryCode);
			 		  };
			
			assertOne(api.get().listObjects(wrapper), experimentAssertions);

			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestExperimentFactory.wrapper(projCode, QueryMode.REPORTING, null);

			assertOne(api.get().listObjects(wrapper), experimentAssertions);
		});
	}
	
	@Test
	public void updateStateTest() throws Exception {
		logger.debug("update state test");
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			State state = new State(ExperimentStateNames.IP, RConstant.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, RConstant.USER);

			assertEquals(state.code, exp.state.code);
			
			// AJ: we can add here a test on state of container to check is IU state code
			// AJ: Test it into a workflow TU
		});
	}
	
	/**
	 * Check allowed definitions of storageCode
	 * @throws Exception test failed
	 */
	@Test
	public void storageCodeManagementTest() throws Exception { // NGL-2037, NGL-2065, SUPSQ-3166
		logger.debug("Test storageCode management");
		createExpQCWithTube.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: ExpQCWithTube");
			// Change Experiment to InProgress state
			State state = new State("IP", RConstant.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, RConstant.USER);
			
			// change the storageCode of each inputContainerUseds to a different random string
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(exp, RConstant.USER);			
		});
		
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: ExpQCWithPlate");
			// Change Experiment to InProgress state
			State state = new State("IP", RConstant.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, RConstant.USER);
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = "myStorageCode";
				});
			});
			// Update Exp 
			api.get().update(exp, RConstant.USER);
		});
		
		createExpQCWithPlateAndTube.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: ExpQCWithPlateAndTube");
			// Only specify the storageCode for the tube
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if(TestContainerFactory.TUBE.equals(icu.locationOnContainerSupport.categoryCode)) {
						icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
					}
				});
			});
			// Update Exp 
			api.get().update(experiment, RConstant.USER);
			
			// Only specify a storageCOde for the plate
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if(TestContainerFactory.PLATE_96.equals(icu.locationOnContainerSupport.categoryCode)) {
						icu.locationOnContainerSupport.storageCode = "myStorageCode";
					}
				});
			});
			// Update Exp 
			api.get().update(experiment, RConstant.USER);
		});
		
//		createExpDepotWithPlateRWC(RApplication.contextResource).accept((refExperiment, experiment) -> {
//		RApplication.contextResource.then2(ExperimentsAPITest::createExpDepotWithPlateRWC).accept((refExperiment, experiment) -> {
		RApplication.contextResource.then2(RExperiment::createExpDepotWithPlate).accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: createExpDepotWithPlateRWC");
			// define a different storageCode for each support of input containers (not allowed for human user on this kind of experiment but possible in server side)
			// => no validation on the storageCode => no error expected
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(experiment, RConstant.USER); 
		});
		
//		createExpTransfertWithPlateRWC(RApplication.contextResource).accept((refExperiment, experiment) -> {
		RApplication.contextResource.then2(RExperiment::createExpTransfertWithPlate).accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: createExpTransfertWithPlateRWC");
			// define a different storageCode for each support of output containers (here tubes)
			// => no validation on the storageCode => no error expected
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.outputContainerUseds.forEach(ocu -> {
					ocu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(experiment, RConstant.USER); 
		});
	}
	
	
	@Test
	public void NGL2037_storageCodeDiffOnPlateTest() throws Exception {		// NGL-2037, NGL-2065, SUPSQ-3166
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("different storageCodes for containers on same plate into a QC Experiment Test");
			State state = new State("IP", RConstant.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, RConstant.USER);
			
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			assertThrows(APIValidationException.class, () -> api.get().update(exp, RConstant.USER));
		});
	}
	
	@Test
	public void NGL2037_oneStorageCodeDefinedOnPlateTest() throws Exception {		// NGL-2037, NGL-2065, SUPSQ-3166
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("specify a storageCode for only one container on the plate into a QC Experiment Test");
			State state = new State("IP", RConstant.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, RConstant.USER);
			
			exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.storageCode = DevAppTesting.newCode();
			assertThrows(APIValidationException.class, () -> api.get().update(exp, RConstant.USER));
		});
	}
	
}
