package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.processes.api.Processes;
import controllers.processes.api.ProcessesBatchElement;
import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.test.dao.api.factory.TestExperimentFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestProcessFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import ngl.refactoring.state.ContainerStateNames;
import ngl.refactoring.state.ExperimentStateNames;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Experiment actions.
 * 
 * @author vrd
 *
 */
public class RExperiment {

//	private static final play.Logger.ALogger logger = play.Logger.of(RExperiment.class);
	
	/**
	 * QC Experiment with tube.
	 * @param ctx test context
	 * @return    experiment creation action
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithTube(C ctx) {
		return RContainer.createIWPTube(ctx)
               .nest((container) -> Actions.cleaning(() -> {
            	   Process p = TestProcessFactory.processQC(container.code, container.support.code);
            	   return ctx.apis().process().createProcessesFromContainer(p, USER);
               }))
               .cc1  ((container, processes) -> ctx.apis().container().get(container.code))
               .then2((container) 			 -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, container)));
	}
	
	/**
	 * QC Experiment with plate
	 * @param ctx test context
	 * @return    experiment creation action
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlate(C ctx) {
		return RContainer
			.createIWPPlate(ctx)
			.nest((containers) -> Actions.cleaning(() -> {
				return FIterables.flatMap(containers, c -> {
					Process p = TestProcessFactory.processQC(c.code, c.support.code);
					return ctx.apis().process().createProcessesFromContainer(p, USER);
				});
			}))
			.cc1  ((containers, processes)                 -> FIterables.map(containers, c -> ctx.apis().container().get(c.code)))
			.then2((containers) 						   -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
	}
	
//	/**
//	 * QC Experiment with plate and tube
//	 * @param ctx test context
//	 * @return    experiment creation action
//	 */
//	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlateAndTube(C ctx) {
//		return RContainer
//			.createIWPPlateAndTube(ctx)
//			// There is a crappy side effect as the containers list is modified
//			.nest ((containers, tube, contTube) -> Actions.cleaning(() -> {
//				List<Process> procs = new ArrayList<>();
//				containers.add(contTube);
//				for(Container c : containers) {
//					Process p = TestProcessFactory.processQC(c.code, c.support.code);
//					procs.addAll(ctx.apis().process().createProcessesFromContainer(p, USER));
//				}
//				return procs;
//			}))
//			.cc1  ((containers, tube, contTube, processes) -> FIterables.map(containers, c -> ctx.apis().container().get(c.code)))
//			.then2((containers) 						   -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
//	}
//	/**
//	 * QC Experiment with plate and tube
//	 * @param ctx test context
//	 * @return    experiment creation action
//	 */
//	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlateAndTube(C ctx) {
//		return RContainer
//			.createIWPPlateAndTube(ctx)
//			// There is a crappy side effect as the containers list is modified
//			.nest ((containers, tube, contTube) -> Actions.cleaning(() -> {
//				List<Process> procs = new ArrayList<>();
//				containers.add(contTube);
//				for(Container c : containers) {
//					Process p = TestProcessFactory.processQC(c.code, c.support.code);
//					procs.addAll(ctx.apis().process().createProcessesFromContainer(p, USER));
//				}
//				return procs;
//			}))
//			.cc1  ((containers, tube, contTube, processes) -> FIterables.map(containers, c -> ctx.apis().container().get(c.code)))
//			.then2((containers) 						   -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
//	}
	/**
	 * QC Experiment with plate and tube
	 * @param ctx test context
	 * @return    experiment creation action
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlateAndTube(C ctx) {
		return RContainer
			.createIWPPlateAndTubeContainers(ctx)
			.nest ((containers) -> Actions.cleaning(() ->
				FIterables.flatMap(containers, c -> {
					Process p = TestProcessFactory.processQC(c.code, c.support.code);
					return ctx.apis().process().createProcessesFromContainer(p, USER);
				})))
			.cc1  ((containers, processes) -> FIterables.map(containers, c -> ctx.apis().container().get(c.code)))
			.then2((containers)            -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
	}
	
	/*
	 * Experiment Transfert Plate to Tube
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpTransfertWithPlate(C ctx) {
		return RProject.createProject
			.nest ( project -> RContainer.createIWPPlate(ctx, project, 12))
			.nest ((project, containers) -> Actions.cleaning(() -> 
				FIterables.flatMap(containers,  c -> {
					Process p = TestProcessFactory.processQC(c.code, c.support.code);
					return ctx.apis().process().createProcessesFromContainer(p, USER);
				})))
			.cc1  ((project, containers, processes)        -> FIterables.map(containers, c -> ctx.apis().container().get(c.code)))
			.nest2((containers)                            -> Actions.using2(USER, () -> TestExperimentFactory.experimentPlateToTubes(USER, containers)))
			.cc2  ((containers, refExperience, experience) -> T.t2(refExperience, experience));
	}
	
	/*
	 * Experiment depot illumina with plate (special because it don't use a flowcell as input)
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpDepotWithPlate(C ctx) {
		return RProject.createProject
			.nest ((project)               -> RContainer.createIWPPlate(ctx, project, 12))
			.cc1  ((project, containers)   -> containers)
			.nest ((containers)            -> Actions.cleaning(() -> FIterables.flatMap(containers, c -> {
					Process p = TestProcessFactory.processQC(c.code, c.support.code);
					return ctx.apis().process().createProcessesFromContainer(p, USER);
				})))
			.cc1  ((containers, processes) -> Iterables.map(containers, c -> ctx.apis().container().get(c.code)).toList())
			.then2((containers)            -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(USER, containers)));
	}

	// -- Sequencing Experiments
	
	/**
	 * Experiment Prep Flowcell Illumina with one sample/container/support (tube).
	 * @param ctx test context
	 * @return    experiment creation action
	 */
	public static final <C extends TestContext> CC3<Container, Experiment, Experiment> createExpPrepFCIllumina(C ctx) {
		return RContainer.createIWPTube(ctx)
			.nest ((container) -> Actions.cleaning(() -> {
				Process p = TestProcessFactory.processTransformationIllumina(container.code, container.support.code);
				return ctx.apis().process().createProcessesFromContainer(p, USER);
			}))
			.cc1  ((container, processes) -> ctx.apis().container().get(container.code))
			.nest2((container)            -> Actions.using2(USER, () -> TestExperimentFactory.experimentPrepFCIllumina(USER, container)));
	}
		
	/**
	 * Experiment Depot Illumina with Flowcell from Prep Flowcell Illumina experiment ({@link #createExpPrepFCIllumina(TestContext)})
	 * @param ctx test context
	 * @return    experiment creation action
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createExpDepotIllumina(C ctx) {
		return RExperiment.createExpPrepFCIllumina(ctx)
			.cc2  ((container, refExperiment, experiment) -> 
				T.t2(container, ctx.apis().experiment().updateState(experiment.code, ExperimentStateNames.F, USER)))
			.nest ((container, experiment) -> Actions.cleaning(() -> FIterables.map(experiment.atomicTransfertMethods, atm ->
			                 ctx.apis().container().updateState(atm.outputContainerUseds.get(0).code, ContainerStateNames.A_TM, USER))))
			// add fc support from prep fc experiment to entities lifecycle
			.nest ((container, experiment, fcContainers)            -> 
				Actions.cleaningOne(() -> 
					ctx.apis().containerSupport().get((String)experiment.instrumentProperties.get("containerSupportCode").value)))
			.cc2  ((container, experiment, fcContainers, fcSupport)  -> T.t2(experiment, fcContainers))
			// create Depot Illumina experiment using containers of FC
			.then2((experiment, fcContainers)                      -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(RConstant.USER, fcContainers)));
	}
	
	/**
	 * Transformation Experiment: Depot Nanopore with one tube as input and one flowcell as output
	 * WARNING: can't be used as resource to test because of Drools rules triggering asynchronously will generate Run and ReadSet automatically
	 *          * this is fixed by using synchronous drools execution (application level binding) *
	 * @param ctx test context
	 * @return    nanopore ended depot
	 */
	public static final <C extends TestContext> CC2<Experiment, Experiment> createEndedExpDepotNanopore(C ctx) {
		return RContainer.createIWPPlateAndTube(ctx)
			.nest((plate, tubeSupport, tubeContainer)              -> Actions.cleaning(() -> {
				Process p = TestProcessFactory.processTransformationNanopore(tubeContainer.code, tubeSupport.code);
				return ctx.apis().process().createProcessesFromContainer(p, USER);
			}))
			.cc1  ((plate, tubeSupport, tubeContainer, processes) -> ctx.apis().container().get(tubeContainer.code))
			.nest2((tubeContainer)                          -> 
			            Actions.using2(USER, () -> TestExperimentFactory.experimentNanoporeDepot(RConstant.USER, Arrays.asList(tubeContainer))))
			.cc2  ((tubeContainer, refExperiment, experiment) -> T.t2(refExperiment, experiment))
			.cc2  ((refExperiment, experiment)                -> {
				// Add output flow cell to the experiment
				OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
				ocu.instrumentProperties = TestExperimentFactory.depotNanoporeOCUInstrumentProperties(ocu.code);
				experiment = ctx.apis().experiment().update(experiment, USER);

				// simulate ending of experiment
				// updating state of the experiment to "F" triggers the creation of container and support of flow cell in DB
				// warning: at this moment these objects are not taken into account in life cycle of entities (no cleaning after test)
				experiment = ctx.apis().experiment().updateState(experiment.code, ExperimentStateNames.F, USER);
				return T.t2(refExperiment, experiment);
			})
			.nest ((refExperiment, experiment)                -> Actions.cleaningOne(() -> {
				// include the flowcell container into lifecycle of entities (db cleaning purpose)
				OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
				Container flowcell = ctx.apis().container().get(ocu.code);
				return flowcell;
			}))
			.nest ((refExperiment, experiment, flowcell)      -> Actions.cleaningOne(() -> {
				// include the flowcell support into lifecycle of entities (db cleaning purpose)
				OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
				ContainerSupport flowcellSupport = ctx.apis().containerSupport().get(ocu.code);
				return flowcellSupport;
			}))
			.cc2 ((refExperiment, experiment, fc, fcSupport)   -> T.t2(refExperiment, experiment));
	}

	/*
	 * QC Experiment with tube
	 */
	@Deprecated // Never used or tested
	public static <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithTube_UC(C ctx) { 
		return RContainer.createIWPTube(ctx)
			.cc1((container) -> {
				ProcessesBatchElement form = new ProcessesBatchElement();
				form.index = new Integer(0);
				form.data = TestProcessFactory.processQC(container.code, container.support.code);
				RExperiment.createProcessesUsingController(ctx, Arrays.asList(form));
				return T.t1(ctx.apis().container().get(container.code));
			})
			.then2((container) -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, Arrays.asList(container))));
	}
	
	/*
	 * QC Experiment with plate
	 */
	@Deprecated // Never used or tested
	public static <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlate_UC(C ctx) {
		return RContainer.createIWPPlate(ctx)
			.cc1((containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for (int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				RExperiment.createProcessesUsingController(ctx, form);
				return T.t1(FIterables.map(containers, c -> ctx.apis().container().get(c.code)));
			})
			.then2((containers) -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
	}
	
	/*
	 * QC Experiment with plate and tube
	 */
	@Deprecated // Never used or tested
	public static <C extends TestContext> CC2<Experiment, Experiment> createExpQCWithPlateAndTube_UC(C ctx) {
		return RContainer.createIWPPlateAndTube(ctx)
			.cc1  ((containers, tube, contTube) -> {
				containers.add(contTube);
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				RExperiment.createProcessesUsingController(ctx, form);
				return FIterables.map(containers, c -> ctx.apis().container().get(c.code));
			})
			.then2((containers) -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)));
	}
	
	public static void createProcessesUsingController(TestContext context, List<ProcessesBatchElement> elements) {
		Processes controller = context.app().injector().instanceOf(Processes.class);

		/* Add user information into the HTTP Context of fake request
		 * because we access to the method of controller directly without authentication 
		 */ 
		Map<String, String> session = new HashMap<>();
		session.put(Authentication.SESSION_USER_KEY, USER);
		RequestBuilder requestBuilder = Helpers.fakeRequest().bodyJson(Json.toJson(elements))
				.session(session);

		Result result = Helpers.invokeWithContext(requestBuilder, 
				Helpers.contextComponents(), 
				() -> controller.saveBatch("from-container"));	
		assertEquals(result.status(), Status.OK);
	}

	/*
	 * Experiment depot illumina with plate
	 */
	@Deprecated // Never used or tested
	public static <C extends TestContext> CC2<Experiment, Experiment> createExpDepotWithPlate_UC(C ctx) {
		return RProject.createProject
			.nest ((project) -> RContainer.createIWPPlate(ctx, project, 12))
			.cc1  ((project, containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for (int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processTransformationIllumina(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(ctx, form);
				return FIterables.map(containers, c -> ctx.apis().container().get(c.code));
			})
			.then2((containers) -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(USER, containers)));
	}
	
	/*
	 * Experiment Transfert Plate to Tube
	 */
	@Deprecated // Never used or tested
	public static <C extends TestContext> CC2<Experiment, Experiment> createExpTransfertWithPlate_UC(C ctx) {
		return RProject.createProject
			.nest((project) -> RContainer.createIWPPlate(ctx, project, 12))
			.cc1 ((project, containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(ctx, form);
				return FIterables.map(containers, c -> ctx.apis().container().get(c.code));
			})
			.then2((containers) -> Actions.using2(USER, () -> TestExperimentFactory.experimentPlateToTubes(USER, containers)));
	}
	
}
