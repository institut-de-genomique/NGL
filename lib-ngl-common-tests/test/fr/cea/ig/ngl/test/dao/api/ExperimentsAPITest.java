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
import fr.cea.ig.ngl.test.TUResources;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestExperimentFactory;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.experiment.instance.Experiment;
import ngl.refactoring.state.ExperimentStateNames;

public class ExperimentsAPITest {
private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsAPITest.class);
	
	//Tested API
	private static APIRef<ExperimentsAPI> api 	 = APIRef.experiment;
	
	public static final CC2<Experiment, Experiment> createExpQCWithTube = TUResources.createExpQCWithTubeRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
	public static final CC2<Experiment, Experiment> createExpQCWithPlate = TUResources.createExpQCWithPlateRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
	public static final CC2<Experiment, Experiment> createExpQCWithPlateAndTube = TUResources.createExpQCWithPlateAndTubeRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
	public static final CC2<Experiment, Experiment> createExpDepotWithPlateRWC = TUResources.createExpDepotWithPlateRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
	public static final CC2<Experiment, Experiment> createExpTransfertWithPlateRWC = TUResources.createExpTransfertWithPlateRWC
			.cc2((context, refExperiment, experiment) -> T.t2(refExperiment, experiment));
			
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
            experiment.comments.add(new Comment(text, TUResources.USER));
            Experiment exp = api.get().update(experiment, TUResources.USER);

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
			State state = new State(ExperimentStateNames.IP, TUResources.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, TUResources.USER);

			assertEquals(state.code, exp.state.code);
			
			// TODO we can add here a test on state of container to check is IU state code
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
			State state = new State("IP", TUResources.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, TUResources.USER);
			
			// change the storageCode of each inputContainerUseds to a different random string
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(exp, TUResources.USER);			
		});
		
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: ExpQCWithPlate");
			// Change Experiment to InProgress state
			State state = new State("IP", TUResources.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, TUResources.USER);
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = "myStorageCode";
				});
			});
			// Update Exp 
			api.get().update(exp, TUResources.USER);
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
			api.get().update(experiment, TUResources.USER);
			
			// Only specify a storageCOde for the plate
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if(TestContainerFactory.PLATE_96.equals(icu.locationOnContainerSupport.categoryCode)) {
						icu.locationOnContainerSupport.storageCode = "myStorageCode";
					}
				});
			});
			// Update Exp 
			api.get().update(experiment, TUResources.USER);
		});
		
		createExpDepotWithPlateRWC.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: createExpDepotWithPlateRWC");
			// define a different storageCode for each support of input containers (not allowed for human user on this kind of experiment but possible in server side)
			// => no validation on the storageCode => no error expected
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(experiment, TUResources.USER); 
		});
		
		createExpTransfertWithPlateRWC.accept((refExperiment, experiment) -> {
			logger.debug("Test storageCode management: createExpTransfertWithPlateRWC");
			// define a different storageCode for each support of output containers (here tubes)
			// => no validation on the storageCode => no error expected
			experiment.atomicTransfertMethods.forEach(atm -> {
				atm.outputContainerUseds.forEach(ocu -> {
					ocu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			// Update Exp 
			api.get().update(experiment, TUResources.USER); 
		});
	}
	
	
	@Test
	public void NGL2037_storageCodeDiffOnPlateTest() throws Exception {		// NGL-2037, NGL-2065, SUPSQ-3166
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("different storageCodes for containers on same plate into a QC Experiment Test");
			State state = new State("IP", TUResources.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, TUResources.USER);
			
			exp.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					icu.locationOnContainerSupport.storageCode = DevAppTesting.newCode();
				});
			});
			assertThrows(APIValidationException.class, () -> api.get().update(exp, TUResources.USER));
		});
	}
	
	@Test
	public void NGL2037_oneStorageCodeDefinedOnPlateTest() throws Exception {		// NGL-2037, NGL-2065, SUPSQ-3166
		createExpQCWithPlate.accept((refExperiment, experiment) -> {
			logger.debug("specify a storageCode for only one container on the plate into a QC Experiment Test");
			State state = new State("IP", TUResources.USER);
			Experiment exp = api.get().updateState(refExperiment.code, state, TUResources.USER);
			
			exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.storageCode = DevAppTesting.newCode();
			assertThrows(APIValidationException.class, () -> api.get().update(exp, TUResources.USER));
		});
	}
}
