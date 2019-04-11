package fr.cea.ig.ngl.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.processes.api.Processes;
import controllers.processes.api.ProcessesBatchElement;
import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.test.authentication.Identity;
import fr.cea.ig.ngl.test.dao.api.factory.TestAnalysesFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestExperimentFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestProcessFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestProjectFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestRunFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestSampleFactory;
import fr.cea.ig.play.test.ApplicationFactory;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CC4;
import fr.cea.ig.util.function.CC5;
import fr.cea.ig.util.function.CC6;
import fr.cea.ig.util.function.CCActions;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import ngl.common.Global;
import ngl.refactoring.state.ContainerStateNames;
import ngl.refactoring.state.ExperimentStateNames;
import ngl.refactoring.state.ReadSetStateNames;
import ngl.refactoring.state.RunStateNames;
import play.Application;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Configured entities lifecycle.
 * @author ajosso
 *
 */
public class TUResources {

	/**
	 * user used for all tests
	 */
	public static final String USER = "ngsrg";

	private static final play.Logger.ALogger logger = play.Logger.of(TUResources.class);
	
	// Resources using APIs layer (create method only) directly without application context
	/* ------------------------------------------------------------------------------------ */
	
	// -- Project
	/**
	 * Created and persisted project. 
	 */
	public static final CC2<Project,Project> createFullProject = Actions.using2(USER,() -> TestProjectFactory.project(USER));
	
	/**
	 * New persisted project.
	 */
	public static final CC1<Project> createProject = createFullProject.cc1((refProject,project) -> project);

	
	// -- Sample
	/**
	 * Persisted project and created and persisted sample.
	 */
	public static final CC3<Project,Sample,Sample> createFullSample = createProject
			.nest2(project -> Actions.using2(USER, () -> TestSampleFactory.sample(USER, project.code)));

	/**
	 * New persisted project and sample.
	 */
	public static final CC2<Project,Sample> createSample = createFullSample
			.cc2((project,refSample,sample) -> T.t2(project,sample));
	
	
	// -- Container & ContainerSupport
	/**
	 * New persisted  one sample, created and persisted containerSupportTube, created and persisted container.
	 * 
	 */
	public static final CC5<Sample, ContainerSupport,ContainerSupport,Container,Container> createFullContainerOnTubeResource = createSample
			.cc1(  (project,sample) 							 -> sample)
			.nest2( sample 										 -> Actions.using2(USER,() -> TestContainerFactory.containerSupportTube(USER, sample)))
			.nest2((sample,refContainerSupport,containerSupport) -> Actions.using2(USER,() -> TestContainerFactory.container(USER, 
																															 sample,
																															 refContainerSupport)));

	/**
	 * New persisted project, sample, container and support. 
	 */
	public static final CC2<ContainerSupport,Container> createContainerOnTubeResource = createFullContainerOnTubeResource
			.cc2((sample, iContSup,containerSupport,iCont,container) -> T.t2(containerSupport, container));
	
	
	
	// TestContext resources to expose WSClient/APIs 
	/* ---------------------------------------------------- */	
	/**
	 * Resource with an application and web client
	 */
	public static final CC2<Application, NGLWSClient> appResource = ApplicationFactory.Actions.withAppWS(Global.afSq.as(Identity.ReadWrite));

	/**
	 * Resource with an application context
	 */
	public static final CC1<TestContext> contextResource = appResource.cc1((app, client) -> new TestContext(app, client));
	
	
	
	// Resources with a TestContext to expose WSClient/APIs 
	/* ---------------------------------------------------- */	
	// -- Project	
	/**
	 * Resource with an application context and one project
	 */
	public static final CC2<TestContext, Project> createProjectWithContext = contextResource
			.and(Actions.using(USER,() -> TestProjectFactory.project(USER)));

	// -- Sample
	/**
	 * Resource with an application context and one sample
	 */
	public static final CC2<TestContext, Sample> createSampleResourceWithContext = createProjectWithContext
			.nest((context, project) 		  -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
			.cc2(  (context, project, sample) -> T.t2(context, sample));
	
	// -- Container & ContainerSupport
	/**
	 * Resource with an application context and one container (tube).
	 */
	public static CC3<TestContext, ContainerSupport, Container> createTubeResourceWithContext = createSampleResourceWithContext
			.nest((context, sample)								  -> Actions.using(USER,() -> TestContainerFactory.containerSupportTube(USER, sample)))
			.nest((context, sample, containerSupport) 			  -> Actions.using(USER,() -> TestContainerFactory.container(USER, 
																													 	     sample,
																														     containerSupport)))
			.cc3( (context, sample, containerSupport, container)  -> T.t3(context, containerSupport, container));
	
	/**
	 * Resource with an application context and one created and persisted container (tube).
	 */
	public static final CC2<TestContext, Container> createIWPTubeResourceWithContext = createTubeResourceWithContext
			.cc2((context, support, container) -> T.t2(context, context.apis().container().updateState(container.code, new State("IW-P", USER), USER)));
	
	
	/**
	 * Create a plate with x containers. 
	 * For each container, one sample is created. 
	 * @param projectRWC   resource with an application context and one project 
	 * @param nbContainers number of containers wanted on the plate 
	 * @return 			   resource with an application context, one project, x samples, x container, one support (plate)
	 */
	public static final CC3<TestContext, ContainerSupport, List<Container>> createPlateRWC(CC2<TestContext, Project> projectRWC, int nbContainers) {
		CC3<TestContext, Project, List<Sample>> sampleRWC = projectRWC
				.nest((context, project) 		 -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
		  		.cc3( (context, project, sample) -> T.t3(context, project, Arrays.asList(sample)));
		
		for (int i = 1; i < nbContainers; i++) {
			sampleRWC = sampleRWC.nest((context, project, samples) 		   -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
								 .cc3( (context, project, samples, sample) -> T.t3(context, project, Actions.app(samples, sample)));
		}
		return sampleRWC.cc2( (context, project, samples) 			   -> T.t2(context, samples))
						.nest((context, samples)		  			   -> Actions.using(USER,() -> TestContainerFactory.containerSupportPlate(USER, samples)))
						.nest((context, samples, containerSupport) 	   -> CCActions.unwrap(TestContainerFactory.containers(USER, samples, containerSupport)
																										     .stream()
																										     .map(c -> Actions.use(USER, c))
																										     .collect(Collectors.toCollection(ArrayList::new))))
						.cc3( (context, samples, containerSupport, cs) -> T.t3(context, containerSupport, cs));
	}
	
	/**
	 * Create a plate with x containers with IW-P state.
	 * For each container, one sample is created.
	 * @param projectRWC   resource with an application context and one project 
	 * @param nbContainers number of containers wanted on the plate 
	 * @return 			   resource with an application context, one project, x samples, x container, one support (plate)
	 */
	public static final CC2<TestContext, List<Container>> createIWPPlateRWC(CC2<TestContext, Project> projectRWC, int nbContainers) {
		CC3<TestContext, ContainerSupport, List<Container>> plateRWC = createPlateRWC(projectRWC, nbContainers);
		return plateRWC.cc2((context, support, containers) -> T.t2(context, containers.stream()
					   .map(c -> {
						   try {
							   return context.apis().container().updateState(c.code, 
									   										 new State("IW-P", USER), 
									   										 USER);
						   } catch (APIException e) {
							   e.printStackTrace();
							   return c;
						   }
					   }).collect(Collectors.toList())));
	}
	
	/**
	 * Update State of all containers from a plate to IW-P state.
	 * @param plateRWC  resource with an application context and x projects, y samples, z container, one support (plate)
	 * @return 			resource with an application context, x projects, y samples, z container, one support (plate)
	 */
	public static final CC2<TestContext, List<Container>> createIWPPlateRWC(CC3<TestContext, ContainerSupport, List<Container>> plateRWC) {
		return plateRWC.cc2((context, support, containers) -> T.t2(context, containers.stream()
				   .map(c -> {
					   try {
						   return context.apis().container().updateState(c.code, 
								   										 new State("IW-P", USER), 
								   										 USER);
					   } catch (APIException e) {
						   e.printStackTrace();
						   return c;
					   }
				   }).collect(Collectors.toList())));
	}
	
	/**
	 * Create 4 samples, 1 project
	 */
	public static final CC2<TestContext, List<Sample>> createSamplesWithContext = createProjectWithContext
			.nest((context, project) 		  							 -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
			.nest((context, project, sample1) 		  					 -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
			.nest((context, project, sample1, sample2) 		  			 -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
			.nest((context, project, sample1, sample2, sample3) 		 -> Actions.using(USER,() -> TestSampleFactory.sample(USER, project.code)))
			.cc5( (context, project, sample1, sample2, sample3, sample4) -> T.t5(context, sample1, sample2, sample3, sample4))
			.cc2( (context, sample1, sample2, sample3, sample4) 		 -> T.t2(context, Arrays.asList(sample1, sample2, sample3, sample4)));
	
	/**
	 * Create pb!!!!!
	 */
	public static final CC5<TestContext, ContainerSupport, ContainerSupport, Container, Container> createPlateFullResourceWithContext = createSamplesWithContext
			.nest2((context, samples)									 -> Actions.using2(USER,() -> TestContainerFactory.containerSupportPlate(USER, samples)))
			.nest2((context, samples, refSupport, containerSupport) 	 -> Actions.using2(USER,() -> TestContainerFactory.container(USER, 
																														 	     	 samples.get(0),
																														 	     	 containerSupport)))
			.cc5( (context, samples, refSupport,containerSupport, refContainer, container)       -> T.t5(context, refSupport,containerSupport, refContainer, container));
	
	public static final CC4<TestContext, List<Sample>, ContainerSupport, ContainerSupport> temp = createSamplesWithContext
			.nest2((context, samples)									 -> Actions.using2(USER,() -> TestContainerFactory.containerSupportPlate(USER, samples)));
	
	/**
	 * Create a plate with 4 samples, 1 project
	 */
	public static final CC3<TestContext, ContainerSupport, List<Container>> createPlateResourceWithContext = createSamplesWithContext
			.nest((context, samples)									 -> Actions.using(USER,() -> TestContainerFactory.containerSupportPlate(USER, samples)))
			.nest((context, samples, containerSupport) 					 -> CCActions.unwrap(TestContainerFactory.containers(USER, samples, containerSupport)
																											   .stream()
																											   .map(c -> Actions.use(USER, c))
																											   .collect(Collectors.toCollection(ArrayList::new))))
			.cc3( (context, samples, containerSupport, containers) 		 -> T.t3(context, containerSupport, containers));
	
	/**
	 * 4 containers on plate<br>
	 * 1 container <=> 1 sample<br>
	 * State IWP
	 */
	public static final CC2<TestContext, List<Container>> createIWPPlateResourceWithContext = createIWPPlateRWC(createPlateResourceWithContext);
//			.cc2((context, support, containers) -> T.t2(context, containers.stream()
//																		   .map(c -> {
//																			   try {
//																				   return context.apis().container().updateState(c.code, 
//																						   										 new State("IW-P", USER), 
//																						   										 USER);
//																			   } catch (APIException e) {
//																				   e.printStackTrace();
//																				   return c;
//																			   }
//																		   }).collect(Collectors.toList())));
	
	/**
	 * 4 containers on plate and 1 container in tube (the 2 last objects in tuple<br>
	 * 1 container <=> 1 sample<br>
	 * State IWP
	 */
	public static final CC4<TestContext, List<Container>, ContainerSupport, Container> createIWPPlateAndTubeResourceWC = createIWPPlateResourceWithContext
			.nest2((context, containers) 			 -> createContainerOnTubeResource)
			.cc4(  (context, containers, tube, cont) -> T.t4(context, containers, tube, context.apis().container().updateState(cont.code, 
																															   new State("IW-P", USER), 
																															   USER)));
	
	// -- Processes
	public static final CC4<TestContext, Container, Process, List<Process>> createProcessQCRWC = createIWPTubeResourceWithContext
			.nest((context, container) -> T.t1(TestProcessFactory.processQC(container.code, container.support.code)).cc())    
	        .nest((context, container, refProcess) -> Actions.cleaning(() -> {
//                return context.apis().process().createProcesses(refProcess, USER, "from-container");
                return context.apis().process().createProcessesFromContainer(refProcess, USER);
			}))
			.cc4((context, container, refProcess, processes) -> T.t4(context, context.apis().container().get(container.code), refProcess, processes));
	
	// -- Experiments
	/**
	 * QC Experiment with tube
	 */
	public static final CC3<TestContext, Experiment, Experiment> createExpQCWithTubeRWC = createIWPTubeResourceWithContext
            .nest((context, container) -> Actions.cleaning(() -> {
                Process p = TestProcessFactory.processQC(container.code, container.support.code);
//                return context.apis().process().createProcesses(p, USER, "from-container");
                return context.apis().process().createProcessesFromContainer(p, USER);
            }))
            .cc2((context, container, processes)                 -> T.t2(context, context.apis().container().get(container.code)))
			.nest2((context, container) 						 -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, Arrays.asList(container))))
			.cc3((context, container, refExperience, experience) -> T.t3(context, refExperience, experience));

	/**
	 * QC Experiment with plate
	 */
	public static final CC3<TestContext, Experiment, Experiment> createExpQCWithPlateRWC = createIWPPlateResourceWithContext
	        .nest((context, containers) -> Actions.cleaning(() -> {
	            List<Process> procs = new ArrayList<>();
	            for(Container c : containers) {
                    Process p = TestProcessFactory.processQC(c.code, c.support.code);
//                    procs.addAll(context.apis().process().createProcesses(p, USER, "from-container"));
                    procs.addAll(context.apis().process().createProcessesFromContainer(p, USER));
                }
	            return procs;
            }))
			.cc2((context, containers, processes) -> {
				return T.t2(context, containers.stream()
											   .map(c -> context.apis().container().get(c.code))
											   .collect(Collectors.toList()));
			})
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));
	
	
	/**
	 * QC Experiment with plate and tube
	 */
	public static final CC3<TestContext, Experiment, Experiment> createExpQCWithPlateAndTubeRWC = createIWPPlateAndTubeResourceWC
	        .nest((context, containers, tube, contTube) -> Actions.cleaning(() -> {
                List<Process> procs = new ArrayList<>();
                containers.add(contTube);
                for(Container c : containers) {
                    Process p = TestProcessFactory.processQC(c.code, c.support.code);
//                    procs.addAll(context.apis().process().createProcesses(p, USER, "from-container"));
                    procs.addAll(context.apis().process().createProcessesFromContainer(p, USER));
                }
                return procs;
            }))
            .cc2((context, containers, tube, contTube, processes) -> {
                return T.t2(context, containers.stream()
                                               .map(c -> context.apis().container().get(c.code))
                                               .collect(Collectors.toList()));
            })
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));
		
	/**
     * Experiment Transfert Plate to Tube
     */
    public static final CC3<TestContext, Experiment, Experiment> createExpTransfertWithPlateRWC = createIWPPlateRWC(createProjectWithContext, 12)
            .nest((context, containers) -> Actions.cleaning(() -> {
                List<Process> procs = new ArrayList<>();
                for(Container c : containers) {
                    Process p = TestProcessFactory.processQC(c.code, c.support.code);
//                    procs.addAll(context.apis().process().createProcesses(p, USER, "from-container"));
                    procs.addAll(context.apis().process().createProcessesFromContainer(p, USER));
                }
                return procs;
            }))
            .cc2((context, containers, processes) -> {
                return T.t2(context, containers.stream()
                                               .map(c -> context.apis().container().get(c.code))
                                               .collect(Collectors.toList()));
            })
            .nest2((context, containers)                          -> Actions.using2(USER, () -> TestExperimentFactory.experimentPlateToTubes(USER, 
                                                                                                                                             containers)))
            .cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));

    
    /**
     * Experiment depot illumina with plate (special because it don't use a flowcell as input)
     */
    public static final CC3<TestContext, Experiment, Experiment> createExpDepotWithPlateRWC = createIWPPlateRWC(createProjectWithContext, 12)
            .nest((context, containers) -> Actions.cleaning(() -> {
                List<Process> procs = new ArrayList<>();
                for(Container c : containers) {
                    Process p = TestProcessFactory.processQC(c.code, c.support.code);
//                    procs.addAll(context.apis().process().createProcesses(p, USER, "from-container"));
                    procs.addAll(context.apis().process().createProcessesFromContainer(p, USER));
                }
                return procs;
            }))
            .cc2((context, containers, processes) -> {
                return T.t2(context, containers.stream()
                                               .map(c -> context.apis().container().get(c.code))
                                               .collect(Collectors.toList()));
            })
            .nest2((context, containers)                          -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(USER, 
                                                                                                                                              containers)))
            .cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));

    
    // -- Sequencing Experiments
    
    /**
     * Experiment Prep Flowcell Illumina with one sample/container/support (tube).
     */
    public static final CC4<TestContext, Container, Experiment, Experiment> createExpPrepFCIlluminaRWC = createIWPTubeResourceWithContext
            .nest((context, container) -> Actions.cleaning(() -> {
                Process p = TestProcessFactory.processTransformationIllumina(container.code, container.support.code);
//                return context.apis().process().createProcesses(p, USER, "from-container");
                return context.apis().process().createProcessesFromContainer(p, USER);
            }))
            .cc2((context, container, processes) -> T.t2(context, context.apis().container().get(container.code)))
            .nest2((context, container) -> Actions.using2(USER, () -> TestExperimentFactory.experimentPrepFCIllumina(USER, container)));
    
    /**
     * Experiment Depot Illumina with Flowcell from Prep Flowcell Illumina experiment ({@link #createExpPrepFCIlluminaRWC})
     */
    public static final CC3<TestContext, Experiment, Experiment> createExpDepotIlluminaRWC = createExpPrepFCIlluminaRWC
            .cc3((context, container, refExperiment, experiment) -> {
                // Terminate prep fc experiment to generate outputs
                State state = new State();
                state.code = ExperimentStateNames.F;
                state.user = TUResources.USER;
                state.date = new Date();
                return T.t3(context, container, context.apis().experiment().updateState(experiment.code, state, USER));   
            })
            .nest((context, container, experiment) -> Actions.cleaning(() -> experiment.atomicTransfertMethods.stream()
                        .map(atm -> {
                            // get output containers from prep fc experiment, update their state and add them to entities lifecycle
                            try {
                                State state = new State();
                                state.code = ContainerStateNames.A_TM;
                                state.user = TUResources.USER;
                                state.date = new Date();
                                context.apis().container().updateState(atm.outputContainerUseds.get(0).code, state, USER);
                            } catch (Exception e) {
                                logger.error("fail to update state of containers of FC", e);
                            }
                            return context.apis().container().get(atm.outputContainerUseds.get(0).code);
                        }).collect(Collectors.toList())))
            // add fc support from prep fc experiment to entities lifecycle
            .nest((context, container, experiment, fcContainers)            -> Actions.cleaningOne(() -> context.apis()
                                                                                                                .containerSupport()
                                                                                                                .get((String)experiment.instrumentProperties
                                                                                                                                       .get("containerSupportCode")
                                                                                                                                       .value)))
            .cc3((context, container, experiment, fcContainers, fcSupport)  -> T.t3(context, experiment, fcContainers))
            // create Depot Illumina experiment using containers of FC
            .nest2((context, experiment, fcContainers)                      -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(USER, 
                                                                                                                                                        fcContainers)))
            .cc3((context, experiment, fcContainers, depotRef, depot)       -> T.t3(context, depotRef, depot));
 
	
	/**
	 * Transformation Experiment: Depot Nanopore with one tube as input and one flowcell as output
	 * WARNING: can't be used as resource to test because of Drools rules triggering asynchronously will generate Run and ReadSet automatically
	 */
	private static final CC3<TestContext, Experiment, Experiment> createEndedExpDepotNanoporeRWC = createIWPPlateAndTubeResourceWC
            .cc3((context, plate, tubeSupport, tubeContainer)        -> T.t3(context, tubeSupport, tubeContainer))
            .nest((context, tubeSupport, tubeContainer)              -> Actions.cleaning(() -> {
                Process p = TestProcessFactory.processTransformationNanopore(tubeContainer.code, tubeSupport.code);
//                return context.apis().process().createProcesses(p, USER, "from-container");
                return context.apis().process().createProcessesFromContainer(p, USER);
            }))
            .cc2((context, tubeSupport, tubeContainer, processes)    -> T.t2(context, context.apis().container().get(tubeContainer.code)))
            .nest2((context, tubeContainer)                          -> Actions.using2(USER, () -> TestExperimentFactory.experimentNanoporeDepot(USER, 
                                                                                                                                                 Arrays.asList(tubeContainer))))
            .cc3((context, tubeContainer, refExperiment, experiment) -> T.t3(context, refExperiment, experiment))
            .cc3((context, refExperiment, experiment)                -> {
                // Add output flowcell to the experiment
                OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
                ocu.instrumentProperties = TestExperimentFactory.depotNanoporeOCUInstrumentProperties(ocu.code);
                experiment = context.apis().experiment().update(experiment, TUResources.USER);
                
                // simulate ending of experiment
                // updating state of the experiment to "F" triggers the creation of container and support of flowcell in db
                // warning: at this moment these objects are not taked into account in lifecycle of entities (no cleaning after test)
                State state = new State();
                state.code = ExperimentStateNames.F;
                state.user = TUResources.USER;
                state.date = new Date();
                experiment = context.apis().experiment().updateState(experiment.code, state, TUResources.USER);
                Thread.sleep(1000); // wait 1 sec. (firing rules)
                return T.t3(context, refExperiment, experiment);
            })
            .nest((context, refExperiment, experiment)                -> Actions.cleaningOne(() -> {
                // include the flowcell container into lifecycle of entities (db cleaning purpose)
                OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
                Container flowcell = context.apis().container().get(ocu.code);
                while(flowcell == null) {
                    flowcell = context.apis().container().get(ocu.code);
                    Thread.sleep(1000); // wait 1 sec. (firing rules)
                }
                return flowcell;
            }))
            .nest((context, refExperiment, experiment, flowcell)      -> Actions.cleaningOne(() -> {
                // include the flowcell support into lifecycle of entities (db cleaning purpose)
                OutputContainerUsed ocu = experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0);
                ContainerSupport flowcellSupport = context.apis().containerSupport().get(ocu.code);
                while(flowcell == null) {
                    flowcellSupport = context.apis().containerSupport().get(ocu.code);
                    Thread.sleep(1000); // wait 1 sec. (firing rules)
                }
                return flowcellSupport;
            }))
            .cc3((context, refExperiment, experiment, fc, fcSupport)   -> T.t3(context, refExperiment, experiment));	
	        
	/**
	 * Generate a run and a readset from Nanopore Depot experiment ({@link #createEndedExpDepotNanoporeRWC})
	 */
	public static final CC4<TestContext, Experiment, Run, ReadSet> createNanoporeRunAndReadSetRWC = createEndedExpDepotNanoporeRWC
	        .nest((context, refExperiment, exp) -> Actions.cleaningOne(() -> {
	            logger.info("retrieve Nanopore Run");
	            return context.apis().run().listObjects(TestRunFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next();
	        }))
	        .cc3((context, refExperiment, exp, run) -> T.t3(context, exp, run))
	        .nest((context, exp, run) -> Actions.cleaningOne(() -> {
                logger.info("retrieve Nanopore ReadSet");
                return context.apis().readset().listObjects(TestReadsetFactory.wrapper(exp.projectCodes.iterator().next())).iterator().next();
            }));       
	
	/**
	 * Create a run from Illumina Depot experiment ({@link #createExpDepotIlluminaRWC})
	 */
	public static final CC4<TestContext, Experiment, Run, Run> createIlluminaRunRWC = createExpDepotIlluminaRWC
	        .cc2((context, refExperience, experience) -> {
	            State s = new State();
	            s.user = USER;
	            s.code = ExperimentStateNames.IP;
	            s.date = new Date();
	            experience = context.apis().experiment().updateState(experience.code, s, USER);
	            return T.t2(context, experience);
	        })
	        .nest2((context, experiment) -> Actions.using2(USER, () -> TestRunFactory.run(experiment, USER)));
	
	/**
	 * Create a ReadSet from Illumina run ({@link #createIlluminaRunRWC})
	 */
	public static final CC6<TestContext, Experiment, Run, Run, ReadSet, ReadSet> createIlluminaRunAndReadSetRWC = createIlluminaRunRWC
	        .cc4((context, exp, refRun, run) -> {
	            // finish experiment
	            State s = new State();
                s.user = USER;
                s.code = ExperimentStateNames.F;
                s.date = new Date();
	            context.apis().experiment().updateState(exp.code, s, USER);
	            
	            // create the lane 1
	            context.apis().lane().save(run, TestRunFactory.lane(1, null), USER);
	            run = context.apis().run().get(run.code);
	            // add project and sample codes to the run (update after creation of readset due to validation)
	            run.projectCodes = exp.projectCodes;
	            run.sampleCodes = exp.sampleCodes;
	            refRun.projectCodes = exp.projectCodes;
	            refRun.sampleCodes = exp.sampleCodes;
	            return T.t4(context, exp, refRun, run);
	        })
	        .nest2((context, exp, refRun, run) -> Actions.using2(USER, () -> TestReadsetFactory.readsetIllumina(USER, run, 1)))
	        .cc6((context, exp, refRun, run, refReadset, readset) -> {
	            // update the run (project and sample codes)
                run = context.apis().run().update(run, USER);
	            // add readset in lane 1 of run (required if we want to update the readset later (validation purpose))
	            Lane lane = run.lanes.stream().filter(l -> l.number == 1).findFirst().get();
	            lane.readSetCodes = new ArrayList<>();
	            lane.readSetCodes.add(readset.code);
	            context.apis().lane().update(run, lane, USER);
	            run = context.apis().run().get(run.code);
	            return T.t6(context, exp, refRun, run, refReadset, readset);
	        });
	
	
	// -- Analyses
	
	/**
	 * Create an analysis from one illumina run and readset ({@link #createIlluminaRunAndReadSetRWC})
	 */
	// TODO EJACOBY Non fonctionnel car il faut faie évoluer run et readset pour avoir un état satisfaisant pour la création d'une analyse
	public static CC6<TestContext, Experiment, Run, ReadSet, Analysis, Analysis> createIlluminaAnalysisRWC = createIlluminaRunAndReadSetRWC
	        .cc4((context, exp, refRun, run, refReadset, readset) -> {
	            //add file to readset
	            File input = TestReadsetFactory.rawFile();
                context.apis().readsetFile().save(readset, input, TUResources.USER);
	            logger.info("############################read state " + readset.state.code);
	            State s = new State();
                s.user = USER;
                s.code = ReadSetStateNames.IW_VBA;
                s.date = new Date();
                readset = context.apis().readset().updateState(readset.code, s, USER);
                logger.info("############################updateState");
                
                readset =  context.apis().readset().valuation(readset.code, TestReadsetFactory.valuation(TUResources.USER), USER);
                logger.info("############################valuation");
                s.code = RunStateNames.F_V;
                run = context.apis().run().updateState(run.code, s, USER);
                logger.info("############################read state " + readset.state.code);
	            return T.t4(context, exp, run, readset); 
	        })
	        .nest2((context, exp, run, readset) -> Actions.using2(USER, () -> TestAnalysesFactory.analysis(USER, readset)))
	        .cc6((context, exp, run, readset, refa, a) -> {
	            logger.info("creation faire!!!!############################");
	            return T.t6(context, exp, run, readset, refa, a);
	        });
	        
	
	/* ************************************************************************************************************************** */
	
	/* ****************************** Examples of resource configured using controllers ***************************************** */
	/* -------------------------------- Tests on these resources have been deleted ---------------------------------------------- */
	/* -------------------------------- and need to be reviewed before reactivation --------------------------------------------- */
	private static void createProcessesUsingController(TestContext context, List<ProcessesBatchElement> elements) {
		Processes controller = context.app().injector().instanceOf(Processes.class);

		/* Add user information into the HTTP Context of fake request
		 * because we access to the method of controller directly without authentication 
		 */ 
		Map<String, String> session = new HashMap<>();
		session.put(Authentication.SESSION_USER_KEY,  USER);
		RequestBuilder requestBuilder = Helpers.fakeRequest().bodyJson(Json.toJson(elements))
															 .session(session);
		
		Result result = Helpers.invokeWithContext(requestBuilder, 
												  Helpers.contextComponents(), 
												  () -> controller.saveBatch("from-container"));	
		assertEquals(result.status(), Status.OK);
	}
	
	/**
	 * QC Experiment with tube
	 */
	public static CC3<TestContext, Experiment, Experiment> createExpQCWithTubeRWC_UC = createIWPTubeResourceWithContext
			.cc2((context, container) -> {
				ProcessesBatchElement form = new ProcessesBatchElement();
				form.index = new Integer(0);
				form.data = TestProcessFactory.processQC(container.code, container.support.code);
				createProcessesUsingController(context, Arrays.asList(form));
				return T.t2(context, context.apis().container().get(container.code));
			})
			.nest2((context, container) 						 -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, Arrays.asList(container))))
			.cc3((context, container, refExperience, experience) -> T.t3(context, refExperience, experience));

	/**
	 * QC Experiment with plate
	 */
	public static CC3<TestContext, Experiment, Experiment> createExpQCWithPlateRWC_UC = createIWPPlateResourceWithContext
			.cc2((context, containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(context, form);
				return T.t2(context, containers.stream()
											   .map(c -> context.apis().container().get(c.code))
											   .collect(Collectors.toList()));
			})
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));
	
	
	/**
	 * QC Experiment with plate and tube
	 */
	public static CC3<TestContext, Experiment, Experiment> createExpQCWithPlateAndTubeRWC_UC = createIWPPlateAndTubeResourceWC
			.cc2((context, containers, tube, contTube) -> {
				containers.add(contTube);
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(context, form);
				return T.t2(context, containers.stream()
											   .map(c -> context.apis().container().get(c.code))
											   .collect(Collectors.toList()));
			})
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentQC(USER, containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));
		
	
	/**
	 * Experiment depot illumina with plate
	 */
	public static CC3<TestContext, Experiment, Experiment> createExpDepotWithPlateRWC_UC = createIWPPlateRWC(createProjectWithContext, 12)
			.cc2((context, containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processTransformationIllumina(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(context, form);
				return T.t2(context, containers.stream()
											   .map(c -> context.apis().container().get(c.code))
											   .collect(Collectors.toList()));
			})
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentIlluminaDepot(USER, 
																																			  containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));

	
	/**
	 * Experiment Transfert Plate to Tube
	 */
	public static CC3<TestContext, Experiment, Experiment> createExpTransfertWithPlateRWC_UC = createIWPPlateRWC(createProjectWithContext, 12)
			.cc2((context, containers) -> {
				ArrayList<ProcessesBatchElement> form = new ArrayList<>();
				for(int i = 0; i < containers.size(); i++) {
					Container container = containers.get(i);
					ProcessesBatchElement batch = new ProcessesBatchElement();
					batch.index = new Integer(i);
					batch.data = TestProcessFactory.processQC(container.code, container.support.code);
					form.add(batch);
				}
				createProcessesUsingController(context, form);
				return T.t2(context, containers.stream()
											   .map(c -> context.apis().container().get(c.code))
											   .collect(Collectors.toList()));
			})
			.nest2((context, containers) 						  -> Actions.using2(USER, () -> TestExperimentFactory.experimentPlateToTubes(USER, 
																																			 containers)))
			.cc3((context, containers, refExperience, experience) -> T.t3(context, refExperience, experience));


	/* ************************************************************************************************************************** */

}
