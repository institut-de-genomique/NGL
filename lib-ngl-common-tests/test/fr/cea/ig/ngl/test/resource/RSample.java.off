package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import java.util.List;

import fr.cea.ig.ngl.test.dao.api.factory.TestSampleFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.F1;
import fr.cea.ig.util.function.T;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

// Sample functions split from TUResouces
/**
 * Sample actions.
 * 
 * @author vrd
 *
 */
public class RSample {

	/**
	 * Persisted project and created and persisted sample.
	 */
	public static final CC3<Project,Sample,Sample> createSampleRaw = 
			RProject.createProject
			.nest2(project -> customSample(project, s -> s));

	/**
	 * New persisted project and sample.
	 */
	public static final CC2<Project,Sample> createSample = 
			createSampleRaw
			.cc2((project, refSample, sample) -> T.t2(project,sample));
	
	/**
	 * Create a custom sample for a project.
	 * @param project   project
	 * @param customize sample customization
	 * @return          CC sample
	 */
	public static CC2<Sample,Sample> customSample(Project project, F1<Sample,Sample> customize) {
		return Actions.using2(USER, () -> customize.apply(TestSampleFactory.sample(USER, project.code)));
	}
	
	/**
	 * Create a project and a sample, providing only the created sample.
	 */
	public static final CC1<Sample> create1Sample =
			createSample.cc1((p, s) -> s);

	/**
	 * Creates 4 samples from a single fresh project.
	 */
	public static final CC1<List<Sample>> create4Samples =
			RProject.createProject
			.nest((p)     -> Actions.repeat(4, USER,() -> TestSampleFactory.sample(USER, p.code)))
			.cc1 ((p, ss) -> ss);

}
