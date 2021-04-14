package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.PLATE_96;
import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.TUBE;
import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.containerSupport;
import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.containers1Sample;
import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.coordinates11;
import static fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory.coordinates96;
import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import fr.cea.ig.ngl.test.dao.api.factory.TestContainerFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestSampleFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CC4;
import fr.cea.ig.util.function.CC5;
import fr.cea.ig.util.function.T;
import fr.cea.ig.util.function.T2;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import ngl.refactoring.state.ContainerStateNames;

/**
 * Container actions.
 * 
 * @author vrd
 *
 */
public class RContainer {
	
	/**
	 * New persisted one sample, created and persisted containerSupportTube, created and persisted container.
	 */
	public static final CC5<Sample, ContainerSupport, ContainerSupport, Container, Container> createContainerOnTubeFull = 
			RSample.create1Sample
			.nest4(sample -> cont(() -> containerSupport(USER, TUBE, 
					                                     containers1Sample(USER, TUBE, 
					                                    		           coordinates11(Arrays.asList(sample))))))
			.cc5  ((sa, s, ds, cs, dcs) -> T.t5(sa, s, ds, cs.get(0), dcs.get(0)));

	/**
	 * Create tube with (2,2) coordinates (expected to fail when used but does not).
	 */
	public static final CC2<Container, Container> createTubeAt22 = 	
			RSample.create1Sample
			.nest4(sample -> cont(() -> containerSupport(USER, TUBE, 
			                                             containers1Sample(USER, TUBE, 
			                                                               Arrays.asList(T.t3("2","2", sample))))))
			.cc2  ((sa, s, ds, cs, dcs) -> T.t2(cs.get(0), dcs.get(0)));
	
	/**
	 * Create well on tube, the test fails as expected. The reverse (tube on plate) has not been tested.
	 */
	public static final CC2<Container, Container> createWellOnTube = 	
			RSample.create1Sample
			.nest4(sample -> cont(() -> containerSupport(USER, TUBE, 
			                                             containers1Sample(USER, TUBE, 
			                                            		 coordinates96(Arrays.asList(sample)),
			                                            		 c -> { c.categoryCode = "well"; return c; }))))
			.cc2  ((sa, s, ds, cs, dcs) -> T.t2(cs.get(0), dcs.get(0)));
			
	/**
	 * Create well outside plate 96 valid coordinates (expected to fail when used but does not).
	 */
	public static final CC2<Container, Container> createWellAt99 = 	
			RSample.create1Sample
			.nest4(sample -> cont(() -> containerSupport(USER, PLATE_96, 
			                                             containers1Sample(USER, PLATE_96, 
			                                                               Arrays.asList(T.t3("I","9", sample))))))
			.cc2  ((sa, s, ds, cs, dcs) -> T.t2(cs.get(0), dcs.get(0)));
	
	/**
	 * New persisted project, sample, container and support. 
	 */
	public static final CC2<ContainerSupport, Container> createContainerOnTube = 
			createContainerOnTubeFull
			.cc2((sample, iContSup, containerSupport, iCont, container) -> T.t2(containerSupport, container));
	
	// -- Container & ContainerSupport
	
	/**
	 * Create tube in {@link ContainerStateNames#IW_P} state.
	 * @param ctx test context
	 * @return    tube creation action
	 */
	public static final <C extends TestContext> CC1<Container> createIWPTube(C ctx) {
		return createContainerOnTube
			   .cc1((support, container) -> ctx.apis().container().updateState(container.code, ContainerStateNames.IW_P, RConstant.USER));
	}
	
	/**
	 * Create plate with one sample per container (sample count is equals to the container count).
	 * @param project      project
	 * @param nbContainers sample or container count
	 * @return             plate creation action
	 */
	public static final CC2<ContainerSupport, List<Container>> createPlate(Project project, int nbContainers) {
		return Actions
			.repeat(nbContainers, USER, ()          -> TestSampleFactory.sample(USER, project.code))
			.nest  ((samples)		  			    -> Actions.using(USER, () -> TestContainerFactory.containerSupportPlate96(USER, samples)))
			.nest  ((samples, containerSupport)     -> Actions.usings(USER, TestContainerFactory.containers(USER, samples, containerSupport)))
			.cc2   ((samples, containerSupport, cs) -> T.t2(containerSupport, cs));
	}

	/**
	 * Create a plate with x containers with IW-P state.
	 * For each container, one sample is created.
	 * @param context      application context
	 * @param project      project
	 * @param nbContainers container count
	 * @return             creating container action
	 */
	public static final CC1<List<Container>> createIWPPlate(TestContext context, Project project, int nbContainers) {
		return createPlate(project, nbContainers)
				.cc1((support, containers) -> 
				           FIterables.map(containers, c-> context.apis().container().updateState(c.code, ContainerStateNames.IW_P, USER)));
	}

//	/**
//	 * Create pb!!!!!
//	 */
//	public static final CC4<ContainerSupport, ContainerSupport, Container, Container> createPlateFullResourceWithContext = 
//			RSample.create4Samples
//			.nest2((samples) -> 
//			          Actions.using2(USER, () -> TestContainerFactory.containerSupportPlate96(USER, samples)))
//			.nest2((samples, refSupport, containerSupport) ->
//			          Actions.using2(USER, () -> TestContainerFactory.container(USER, samples.get(0), containerSupport)))
//			.cc4  ((samples, refSupport,containerSupport, refContainer, container) -> T.t4(refSupport, containerSupport, refContainer, container));

	/**
	 * Create a plate with 4 samples, 1 project, 4 wells with one sample each.
	 */
	public static final CC2<ContainerSupport, List<Container>> createPlateResource = 
			RSample.create4Samples
			.nest((samples) -> 
				     Actions.using(USER, () -> TestContainerFactory.containerSupportPlate96(USER, samples)))
			.nest((samples, containerSupport) ->
			         Actions.usings(USER, TestContainerFactory.containers(USER, samples, containerSupport)))
			.cc2 ((samples, containerSupport, containers) -> T.t2(containerSupport, containers));
	
	/*
	 * 4 containers on plate<br>
	 * 1 container <=> 1 sample<br>
	 * State IWP
	 */
	public static final <C extends TestContext> CC1<List<Container>> createIWPPlate(C ctx) {
		return createPlateResource
			.cc1 ((support, containers) -> 
			          FIterables.map(containers, c -> ctx.apis().container().updateState(c.code, ContainerStateNames.IW_P, USER)));
	}
	
	/*
	 * 4 containers on plate and 1 container in tube (the 2 last objects in tuple)<br>
	 * 1 container <=> 1 sample<br>
	 * State IWP
	 */
	public static final <C extends TestContext> CC3<List<Container>, ContainerSupport, Container> createIWPPlateAndTube(C ctx) {
		return createIWPPlate(ctx)
			.and2(createContainerOnTube)
			.cc3 ((containers, tube, cont) -> 
			           T.t3(containers, tube, ctx.apis().container().updateState(cont.code, ContainerStateNames.IW_P, USER)));
	}
	
	/**
	 * Containers that result from {@link #createIWPPlate(TestContext)}.
	 * @param ctx test context
	 * @return    containers creation action
	 */
	public static final <C extends TestContext> CC1<List<Container>> createIWPPlateAndTubeContainers(C ctx) {
		return createIWPPlateAndTube(ctx)
			.cc1((containers, tube, cont) -> FIterables.concat(containers, cont));
	}
	
	// --------------------------------------------------------------------------------------------------
	
	public static final CC4<ContainerSupport, ContainerSupport, List<Container>, List<Container>> cont(Supplier<T2<ContainerSupport, List<Container>>> f) {
		return CC1.from (f)
				  .cc2  (t -> t)
				  .nest ((s, cs)          -> Actions.use(USER, s))
				  .nest ((s, cs, ds)      -> Actions.uses(USER, cs))
				  .cc4  ((s, cs, ds, dcs) -> T.t4(s, ds, cs, dcs));
	}
	
}
